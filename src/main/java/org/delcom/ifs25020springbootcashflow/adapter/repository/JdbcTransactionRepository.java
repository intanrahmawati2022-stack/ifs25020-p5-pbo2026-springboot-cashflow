package org.delcom.ifs25020springbootcashflow.adapter.repository;

import org.delcom.ifs25020springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25020springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25020springbootcashflow.domain.repository.ITransactionRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class JdbcTransactionRepository implements ITransactionRepository {
    private static final RowMapper<Transaction> MAPPER = (rs, i) -> new Transaction(
            rs.getInt("id"),
            rs.getInt("user_id"),
            rs.getString("description"),
            rs.getLong("amount"),
            TransactionType.valueOf(rs.getString("type")),
            rs.getString("proof_path"),
            rs.getTimestamp("created_at").toLocalDateTime());

    private final JdbcTemplate jdbc;

    public JdbcTransactionRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Transaction save(int userId, String description, long amount, TransactionType type, String proofPath) {
        LocalDateTime now = LocalDateTime.now();
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            var ps = con.prepareStatement(
                    "INSERT INTO transactions (user_id, description, amount, type, proof_path, created_at) "
                            + "VALUES (?, ?, ?, ?, ?, ?)",
                    new String[]{"id"});
            ps.setInt(1, userId);
            ps.setString(2, description);
            ps.setLong(3, amount);
            ps.setString(4, type.name());
            ps.setString(5, proofPath);
            ps.setTimestamp(6, Timestamp.valueOf(now));
            return ps;
        }, keys);
        return new Transaction(keys.getKey().intValue(), userId, description, amount, type, proofPath, now);
    }

    @Override
    public List<Transaction> findByUserId(int userId) {
        return jdbc.query("SELECT * FROM transactions WHERE user_id = ? ORDER BY id", MAPPER, userId);
    }

    @Override
    public List<Transaction> findAll() {
        return jdbc.query("SELECT * FROM transactions ORDER BY id", MAPPER);
    }

    @Override
    public Optional<Transaction> findById(Integer id) {
        return jdbc.query("SELECT * FROM transactions WHERE id = ?", MAPPER, id).stream().findFirst();
    }

    @Override
    public List<Transaction> findBy(Predicate<Transaction> filter) {
        return findAll().stream().filter(filter).toList();
    }

    @Override
    public Transaction update(Transaction t) {
        jdbc.update("UPDATE transactions SET description = ?, amount = ?, proof_path = ? WHERE id = ?",
                t.getDescription(), t.getAmount(), t.getProofPath(), t.getId());
        return t;
    }

    @Override
    public void deleteById(Integer id) {
        jdbc.update("DELETE FROM transactions WHERE id = ?", id);
    }
}
