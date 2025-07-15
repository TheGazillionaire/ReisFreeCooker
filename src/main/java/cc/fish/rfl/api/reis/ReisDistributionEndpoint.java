package cc.fish.rfl.api.reis;

import cc.fish.rfl.RflMain;
import cc.fish.rfl.api.utils.EncryptionUtil;
import jakarta.websocket.OnMessage;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import org.json.JSONObject;

@ServerEndpoint("/")
public class ReisDistributionEndpoint {

    @OnMessage
    public void onMessage(String message, Session session) {
        if (ReisDistributor.encryptionKey == null) {
            RflMain.LOGGER.error(
                    "Cannot read message because encryption key is missing. Restart the client.");
            return;
        }
        String decrypted = EncryptionUtil.decrypt(message, ReisDistributor.encryptionKey);
        JSONObject jsonObject = new JSONObject(decrypted);
        if (!jsonObject.has("id")) {
            ReisDistributor.LOGGER.error("Returned because of unknown id");
            return;
        }

        int id = jsonObject.getInt("id");

        if (ReisDistributor.shouldDebugPackets)
            ReisDistributor.LOGGER.info("Received message with data: {}", jsonObject.toString());

        if (id > 2) return;

        JSONObject output = new JSONObject();

        output.put("id", id);

        ReisDistributor.lastKeepAlive = System.currentTimeMillis();

        if (id == 1) {
            output.put("a", true);
            output.put("b", Math.PI);
            output.put("c", 90.0f);
            output.put("d", System.currentTimeMillis());
            output.put("e", "Hello, World!");
            ReisDistributor.loggedIn = true;

            ReisDistributor.LOGGER.info("Client logged in successfully. Have fun!");
        }
        if (id == 2) {
            output.put("a", ReisConfigConverter.convert(jsonObject.getString("a")));
        }

        session.getAsyncRemote().sendText(output.toString());
    }
}
