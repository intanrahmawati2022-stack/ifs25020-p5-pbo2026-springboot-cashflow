package org.delcom.ifs25020springbootcashflow.adapter;

import org.delcom.ifs25020springbootcashflow.DotEnv;
import org.delcom.ifs25020springbootcashflow.adapter.repository.*;
import org.delcom.ifs25020springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25020springbootcashflow.domain.entity.*;
import org.delcom.ifs25020springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class AdapterTest {

    private static JdbcTemplate h2(String name) {
        var ds = new DriverManagerDataSource("jdbc:h2:mem:" + name + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", "");
        return new JdbcTemplate(ds);
    }

    @Test
    void jdbcRepositoriesRoundTrip() {
        JdbcTemplate jdbc = h2("rt");
        SchemaInitializer schema = new SchemaInitializer(jdbc);
        schema.init();
        schema.init(); // idempotent
        JdbcUserRepository users = new JdbcUserRepository(jdbc);
        JdbcTransactionRepository txs = new JdbcTransactionRepository(jdbc);

        User u = users.save("budi", "hash", "Budi", Role.USER);
        assertTrue(u.getId() > 0);
        assertEquals("Budi", users.findByUsername("budi").orElseThrow().getFullName());
        assertTrue(users.findByUsername("none").isEmpty());
        u.setFullName("Budi S");
        u.setPhotoPath("a.png");
        users.update(u);
        assertEquals("a.png", users.findById(u.getId()).orElseThrow().getPhotoPath());
        assertEquals(1, users.findAll().size());
        assertEquals(1, users.findBy(x -> x.getRole() == Role.USER).size());

        Transaction t = txs.save(u.getId(), "Gaji", 5_000_000L, TransactionType.INCOME, null);
        txs.save(u.getId(), "Makan", 25_000L, TransactionType.EXPENSE, "b.png");
        assertEquals(2, txs.findByUserId(u.getId()).size());
        assertEquals(2, txs.findAll().size());
        assertEquals(0, txs.findByUserId(999).size());
        assertEquals(1, txs.findBy(x -> x.getType() == TransactionType.EXPENSE).size());
        assertEquals(5_000_000L, txs.findById(t.getId()).orElseThrow().getAmount());
        t.setAmount(6_000_000L);
        t.setDescription("Gaji+");
        txs.update(t);
        assertEquals("Gaji+", txs.findById(t.getId()).orElseThrow().getDescription());
        txs.deleteById(t.getId());
        assertTrue(txs.findById(t.getId()).isEmpty());
        users.deleteById(u.getId() + 100); // no-op
    }

    @Test
    void inMemoryRepositories() {
        UserRepository users = new UserRepository();
        User u = users.save("a", "h", "A", Role.ADMIN);
        assertEquals(1, u.getId());
        assertEquals(2, users.save("b", "h", "B", Role.USER).getId());
        assertTrue(users.findByUsername("a").isPresent());
        assertTrue(users.findByUsername("zz").isEmpty());
        users.update(u);
        users.deleteById(2);
        assertEquals(1, users.findAll().size());
        TransactionRepository txs = new TransactionRepository();
        Transaction t = txs.save(1, "x", 1, TransactionType.INCOME, null);
        assertEquals(1, txs.findByUserId(1).size());
        assertTrue(txs.findById(t.getId()).isPresent());
        txs.deleteById(t.getId());
        assertTrue(txs.findAll().isEmpty());
    }

    @Test
    void fileStorageStoresImagesOnly(@TempDir Path dir) throws Exception {
        FileStorage fs = new FileStorage(dir.toString());
        String name = fs.store(new byte[]{1, 2, 3}, "Bukti.PNG");
        assertTrue(name.endsWith(".png"));
        assertTrue(Files.exists(dir.resolve(name)));
        assertEquals("/uploads/" + name, FileStorage.urlOf(name));
        assertNull(FileStorage.urlOf(null));
        assertThrows(ValidationException.class, () -> fs.store(new byte[]{1}, "virus.exe"));
        assertThrows(ValidationException.class, () -> fs.store(new byte[]{1}, "noext"));
        assertThrows(ValidationException.class, () -> fs.store(new byte[]{1}, null));
        fs.delete("../../etc/passwd"); // path traversal diabaikan
        fs.delete(null);
        fs.delete(" ");
        fs.delete(name);
        assertFalse(Files.exists(dir.resolve(name)));
        assertEquals(dir.toAbsolutePath().normalize(), fs.root());
    }

    @Test
    void dotEnvLoadsKeysWithoutOverridingExisting(@TempDir Path dir) throws Exception {
        Path env = dir.resolve(".env");
        Files.writeString(env, "# komentar\n\nCF_TEST_A=hello\nCF_TEST_B=\"quoted\"\nbroken\n=novalue\nCF_TEST_C='single'\n");
        System.setProperty("CF_TEST_C", "keep");
        try {
            DotEnv.load(env);
            assertEquals("hello", System.getProperty("CF_TEST_A"));
            assertEquals("quoted", System.getProperty("CF_TEST_B"));
            assertEquals("keep", System.getProperty("CF_TEST_C"));
            DotEnv.load(dir.resolve("missing.env"));
            DotEnv.load(dir); // bukan file
        } finally {
            System.clearProperty("CF_TEST_A");
            System.clearProperty("CF_TEST_B");
            System.clearProperty("CF_TEST_C");
        }
    }
}
