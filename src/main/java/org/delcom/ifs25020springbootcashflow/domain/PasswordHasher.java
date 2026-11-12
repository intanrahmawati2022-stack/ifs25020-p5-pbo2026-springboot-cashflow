package org.delcom.ifs25020springbootcashflow.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** Hash / verifikasi password dengan SHA-256 (hex). */
public final class PasswordHasher {

    private static final String ALGORITHM = "SHA-256";

    private PasswordHasher() {
    }

    public static String hash(String raw) {
        return hash(raw, ALGORITHM);
    }

    /** Dipisah agar kegagalan algoritma bisa diuji. */
    public static String hash(String raw, String algorithm) {
        try {
            byte[] digest = MessageDigest.getInstance(algorithm).digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(algorithm + " tidak tersedia", e);
        }
    }

    public static boolean verify(String raw, String hash) {
        return hash(raw).equals(hash);
    }
}
