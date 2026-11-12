package org.delcom.ifs25020springbootcashflow;

import org.delcom.ifs25020springbootcashflow.adapter.repository.UserRepository;
import org.delcom.ifs25020springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25020springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25020springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25020springbootcashflow.usecase.AuthUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Melengkapi cabang/baris yang belum tersentuh tes lain agar Jacoco mencapai 100%. */
class CoverageGapTest {

    @Test
    void dotEnvHandlesEmptyValueHalfQuotesAndExistingEnvironment(@TempDir Path dir) throws Exception {
        Path env = dir.resolve(".env");
        Files.writeString(env, "CF_GAP_EMPTY=\nCF_GAP_DQ=\"abc\nCF_GAP_SQ='abc\nPATH=diabaikan\n");
        try {
            DotEnv.load(env);
            assertEquals("", System.getProperty("CF_GAP_EMPTY"));
            assertEquals("\"abc", System.getProperty("CF_GAP_DQ"));
            assertEquals("'abc", System.getProperty("CF_GAP_SQ"));
            // PATH sudah ada di environment, jadi tidak boleh ditimpa
            assertNull(System.getProperty("PATH"));
        } finally {
            System.clearProperty("CF_GAP_EMPTY");
            System.clearProperty("CF_GAP_DQ");
            System.clearProperty("CF_GAP_SQ");
            System.clearProperty("PATH");
        }
    }

    @Test
    void dotEnvIgnoresUnreadableFile(@TempDir Path dir) throws Exception {
        Path bad = dir.resolve("bad.env");
        Files.write(bad, new byte[]{(byte) 0xC3, (byte) 0x28, '\n'}); // UTF-8 tidak valid
        assertDoesNotThrow(() -> DotEnv.load(bad));
    }

    @Test
    void dotEnvDefaultLoadReadsDotEnvInWorkingDirectory() {
        Set<String> before = new HashSet<>(System.getProperties().stringPropertyNames());
        try {
            assertDoesNotThrow(() -> {
                DotEnv.load();
            });
        } finally {
            for (String key : new HashSet<>(System.getProperties().stringPropertyNames())) {
                if (!before.contains(key)) {
                    System.clearProperty(key);
                }
            }
        }
    }

    @Test
    void fileStorageDeleteIgnoresDirectoryThatCannotBeDeleted(@TempDir Path dir) throws Exception {
        FileStorage fs = new FileStorage(dir.toString());
        Path sub = Files.createDirectories(dir.resolve("sub"));
        Files.writeString(sub.resolve("x.txt"), "x");
        assertDoesNotThrow(() -> fs.delete("sub")); // folder tidak kosong -> gagal dihapus, diabaikan
        assertTrue(Files.exists(sub));
    }

    @Test
    void loginRejectsEmptyPassword() {
        AuthUseCase auth = new AuthUseCase(new UserRepository());
        assertThrows(ValidationException.class, () -> auth.login("budi", ""));
    }

    @Test
    void passwordHasherFailsForUnknownAlgorithm() {
        assertThrows(IllegalStateException.class, () -> PasswordHasher.hash("x", "TIDAK-ADA"));
    }
}
