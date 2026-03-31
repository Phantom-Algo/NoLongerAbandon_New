package com.nolongerabandon.backend.common.util;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class CryptoUtil {

    private static final String AES_ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int NONCE_LENGTH = 12;
    private static final int AES_KEY_LENGTH = 32;
    /** 密钥文件路径，与 SQLite 数据库同目录 */
    private static final Path KEY_FILE_PATH = Paths.get("./data/.encryption-key");
    private static final SecretKeySpec SECRET_KEY = new SecretKeySpec(loadOrGenerateKey(), "AES");

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

    /**
     * 从文件加载持久化密钥；若文件不存在则生成随机密钥并写入文件。
     * 保证同一台机器上每次启动使用相同的密钥。
     */
    private static byte[] loadOrGenerateKey() {
        try {
            if (Files.exists(KEY_FILE_PATH)) {
                byte[] storedKey = Files.readAllBytes(KEY_FILE_PATH);
                if (storedKey.length == AES_KEY_LENGTH) {
                    return storedKey;
                }
            }
            // 首次运行：生成 256 位随机密钥并持久化
            byte[] newKey = new byte[AES_KEY_LENGTH];
            new SecureRandom().nextBytes(newKey);
            Files.createDirectories(KEY_FILE_PATH.getParent());
            Files.write(KEY_FILE_PATH, newKey);
            return newKey;
        } catch (Exception ex) {
            throw new IllegalStateException("无法加载或生成加密密钥文件: " + KEY_FILE_PATH, ex);
        }
    }
}
