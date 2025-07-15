package cc.fish.agent.api.util;

import lombok.experimental.UtilityClass;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.swing.*;
import java.lang.reflect.Field;

@UtilityClass
public class ReisUtil implements Opcodes {

    // check if the class is the WebSocketClient,
    // it is the only class that uses the ClientManager.connectToServer method
    public boolean isClassWSC(ClassNode classNode) {
        for (MethodNode methodNode : classNode.methods)
            for (AbstractInsnNode abstractInsnNode : methodNode.instructions)
                if (abstractInsnNode instanceof MethodInsnNode methodInsnNode
                    && methodInsnNode.owner.equals("org/glassfish/tyrus/client/ClientManager")
                    && methodInsnNode.name.equals("connectToServer")
                    && abstractInsnNode.getPrevious()
                    instanceof MethodInsnNode previousMethodInsnNode
                    && previousMethodInsnNode.owner.equals("java/net/URI")
                    && previousMethodInsnNode.name.equals("create")) return true;

        return false;
    }

    public boolean replaceStrings(ClassNode classNode) {
        for (MethodNode methodNode : classNode.methods) {
            for (AbstractInsnNode abstractInsnNode : methodNode.instructions) {
                if (abstractInsnNode instanceof LdcInsnNode ldcInsnNode
                    && ldcInsnNode.cst instanceof String string) {

                    if (string.startsWith("wss://")) {
                        ldcInsnNode.cst = "ws://localhost:8443";
                        return true;
                    }
                    if (string.contains("Fly Edition")) {
                        ldcInsnNode.cst = "Free Edition";
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public String extractEncryptionKey(ClassLoader loader, ClassNode wscClass) {
        for (MethodNode methodNode : wscClass.methods) {
            for (AbstractInsnNode abstractInsnNode : methodNode.instructions) {
                if (abstractInsnNode instanceof MethodInsnNode methodInsnNode
                    && methodInsnNode.owner.equals("javax/websocket/RemoteEndpoint$Async")
                    && methodInsnNode.name.equals("sendText")
                    && abstractInsnNode.getPrevious()
                    instanceof MethodInsnNode previousMethodInsnNode) {

                    String className = previousMethodInsnNode.owner.replace("/", ".");
                    try {
                        Class<?> clazz = loader.loadClass(className);
                        for (Field field : clazz.getDeclaredFields()) {
                            if (field.getType().equals(String.class)) {
                                field.setAccessible(true);
                                return (String) field.get(null);
                            }
                        }
                    } catch (Exception e) {
                        JOptionPane.showConfirmDialog(
                            null, "Failed to extract encryption key: " + e.getMessage());
                        System.exit(1);
                    }
                }
            }
        }

        return null;
    }
}
