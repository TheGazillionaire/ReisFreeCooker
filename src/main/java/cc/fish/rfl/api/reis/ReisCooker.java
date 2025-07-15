package cc.fish.rfl.api.reis;

import lombok.experimental.UtilityClass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@UtilityClass
public class ReisCooker {

    public final Logger LOGGER = LogManager.getLogger("Reis Cooker");

    public void cook(String java, boolean mcOutput) {
        LOGGER.info("Cooking Reis with {}...", java);

         try {
           ProcessBuilder processBuilder = new ProcessBuilder(java, "-javaagent:agent.jar",
                   "-XX:+DisableAttachMechanism", "-noverify",
                   "-Djava.library.path=" + ReisUpdater.NATIVE_PATH, "-cp", ReisUpdater.COMPRESSED_PATH, "Start");
           if (mcOutput)
            processBuilder.inheritIO();
           processBuilder.start();
         } catch (Exception e) {
           LOGGER.error("Failed to cook Reis: {}", e.getMessage());
         }
    }
}
