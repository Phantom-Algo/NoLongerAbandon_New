package com.nolongerabandon.backend.common.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Enumeration;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class CryptoUtil {

    private static final String AES_ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int NONCE_LENGTH = 12;
    private static final SecretKeySpec SECRET_KEY = new SecretKeySpec(deriveKey(), "AES");

    private CryptoUtil() {
    }

    public static String encrypt(String plainText) {
        try {
            byte[] nonce = new byte[NONCE_LENGTH];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(nonce);

            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, SECRET_KEY, new GCMParameterSpec(GCM_TAG_LENGTH, nonce));
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] output = new byte[nonce.length + encrypted.length];
            System.arraycopy(nonce, 0, output, 0, nonce.length);
            System.arraycopy(encrypted, 0, output, nonce.length, encrypted.length);
            return Base64.getEncoder().encodeToString(output);
        } catch (Exception ex) {
            throw new IllegalStateException("API Key 加密失败", ex);
        }
    }

    public static String decrypt(String encryptedText) {
        try {
            byte[] allBytes = Base64.getDecoder().decode(encryptedText);
            if (allBytes.length <= NONCE_LENGTH) {
                throw new IllegalStateException("密文格式非法");
            }

            byte[] nonce = Arrays.copyOfRange(allBytes, 0, NONCE_LENGTH);
            byte[] cipherBytes = Arrays.copyOfRange(allBytes, NONCE_LENGTH, allBytes.length);

            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, SECRET_KEY, new GCMParameterSpec(GCM_TAG_LENGTH, nonce));
            byte[] plainBytes = cipher.doFinal(cipherBytes);
            return new String(plainBytes, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new IllegalStateException("API Key 解密失败", ex);
        }
    }

    private static byte[] deriveKey() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String machineIdentity = resolveMachineIdentity();
            return digest.digest(machineIdentity.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new IllegalStateException("无法派生本机加密密钥", ex);
        }
    }

    private static String resolveMachineIdentity() {
        String macAddress = resolveMacAddress();
        String hostName = resolveHostName();
        String seed = (macAddress + "|" + hostName).trim();
        if (seed.equals("|")) {
            return "nolongerabandon-default-machine";
        }
        return seed;
    }

    private static String resolveMacAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || networkInterface.isVirtual() || !networkInterface.isUp()) {
                    continue;
                }
                byte[] hardwareAddress = networkInterface.getHardwareAddress();
                if (hardwareAddress == null || hardwareAddress.length == 0) {
                    continue;
                }
                StringBuilder builder = new StringBuilder();
                for (byte value : hardwareAddress) {
                    builder.append(String.format("%02X", value));
                }
                if (!builder.isEmpty()) {
                    return builder.toString();
                }
            }
            return "";
        } catch (Exception ex) {
            return "";
        }
    }

    private static String resolveHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception ex) {
            return "";
        }
    }
}
