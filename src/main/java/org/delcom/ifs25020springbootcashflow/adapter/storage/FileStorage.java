package org.delcom.ifs25020springbootcashflow.adapter.storage;

import org.delcom.ifs25020springbootcashflow.domain.exception.ValidationException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/** Simpan upload (foto profil + bukti transaksi) ke UPLOAD_DIR. */
public class FileStorage {
    private static final Set<String> ALLOWED = Set.of("png", "jpg", "jpeg", "gif", "webp");

    private final Path root;

    public FileStorage(String uploadDir) {
        this.root = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    public Path root() {
        return root;
    }

    /** @return nama file relatif (UUID + ekstensi); URL publiknya /uploads/{nama} */
    public String store(InputStream in, String originalName) throws ValidationException, IOException {
        String ext = extensionOf(originalName);
        if (!ALLOWED.contains(ext)) {
            throw new ValidationException("File harus berupa gambar (png, jpg, jpeg, gif, webp)");
        }
        Files.createDirectories(root);
        String name = UUID.randomUUID() + "." + ext;
        Files.copy(in, root.resolve(name), StandardCopyOption.REPLACE_EXISTING);
        return name;
    }

    public String store(byte[] data, String originalName) throws ValidationException, IOException {
        return store(new java.io.ByteArrayInputStream(data), originalName);
    }

    public void delete(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) return;
        Path target = root.resolve(relativePath).normalize();
        if (!target.startsWith(root)) return;
        try {
            Files.deleteIfExists(target);
        } catch (IOException ignored) {
            // file bersifat best-effort
        }
    }

    public static String urlOf(String relativePath) {
        return relativePath == null ? null : "/uploads/" + relativePath;
    }

    private static String extensionOf(String name) {
        if (name == null) return "";
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
