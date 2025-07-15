package cc.fish.rfl.api.reis;

import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.tyrus.server.Server;

import java.io.DataInputStream;
import java.net.ServerSocket;
import java.net.Socket;

@SuppressWarnings("LoggingSimilarMessage")
@Getter @Setter
public class ReisDistributor {

    public static final Logger LOGGER = LogManager.getLogger("Reis Distributor");
    public static final int PORT = 8443;

    public static String encryptionKey;
    public static boolean loggedIn;
    public static long lastKeepAlive;
    public static boolean shouldDebugPackets;

    public void startServer(boolean shouldDebugPackets) {
        ReisDistributor.shouldDebugPackets = shouldDebugPackets;

        // create temporary socket for the agent to give us the encryption key
        new Thread(
                        () -> {
                            try (ServerSocket distributorSocket = new ServerSocket(8444)) {
                                Socket agentSocket = distributorSocket.accept();
                                LOGGER.info("Agent connected...");

                                DataInputStream in =
                                        new DataInputStream(agentSocket.getInputStream());
                                encryptionKey = in.readUTF();
                                LOGGER.info("Received key: {}", encryptionKey);

                                agentSocket.close();
                            } catch (Exception e) {
                                e.printStackTrace(System.err);
                                LOGGER.error("Failed to start distributor: {}", e.getMessage());
                            }
                        }, "agent")
                .start();

        try {
            LOGGER.info("Starting distributor...");
            Server distributor = new Server("localhost", PORT, "/", null, ReisDistributionEndpoint.class);
            distributor.start();
            LOGGER.info("distributor running on port {}", PORT);

            // keep the retarded distributor alive
            while (!loggedIn || (System.currentTimeMillis() - lastKeepAlive) < 1000 * 20) {
                Thread.sleep(100);
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
