package cc.fish.agent;

import cc.fish.agent.api.util.ASMUtil;
import cc.fish.agent.api.util.ReisUtil;
import org.objectweb.asm.tree.*;

import java.io.DataOutputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.net.Socket;
import java.util.List;

// maybe change this to a custom runtime (modify JDK)
public class CookingAgent {
    public static final List<String> EXCLUDED_PREFIXES =
            List.of("java/", "javax/", "sun/", "com/sun/", "jdk/");

    public static void premain(String args, Instrumentation instrumentation) {
        instrumentation.addTransformer(new RiseAgentTransformer());
    }

    public static class RiseAgentTransformer implements ClassFileTransformer {

        @Override
        public byte[] transform(
                ClassLoader loader,
                String className,
                Class<?> classBeingRedefined,
                java.security.ProtectionDomain protectionDomain,
                byte[] classFileBuffer) {
            if (EXCLUDED_PREFIXES.stream().anyMatch(className::startsWith)) {
                return classFileBuffer;
            }

            try {
                ClassNode classNode = ASMUtil.getNode(classFileBuffer);
                boolean replaced = ReisUtil.replaceStrings(classNode);

                boolean wscClass = ReisUtil.isClassWSC(classNode);
                if (!replaced && !wscClass) return classFileBuffer;

                // Recode soon anyways lol
                if (replaced && !wscClass)
                    return ASMUtil.writeClassToArray(classNode);

                String encryptionKey = ReisUtil.extractEncryptionKey(loader, classNode);

                // send encryption key to server
                try (Socket socket = new Socket("localhost", 8444)) {
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    out.writeUTF(encryptionKey);
                } catch (Exception e) {
                    System.exit(1);
                }

                return ASMUtil.writeClassToArray(classNode);
            } catch (Exception e) {
                return classFileBuffer;
            }
        }
    }
}
