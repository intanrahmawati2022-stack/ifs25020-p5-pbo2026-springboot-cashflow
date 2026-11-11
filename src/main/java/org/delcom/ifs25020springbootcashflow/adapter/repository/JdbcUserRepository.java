package org.delcom.ifs25020springbootcashflow.adapter.repository;

import org.delcom.ifs25020springbootcashflow.domain.entity.Role;
import org.delcom.ifs25020springbootcashflow.domain.entity.User;
import org.delcom.ifs25020springbootcashflow.domain.repository.IUserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class JdbcUserRepository implements IUserRepository {
    private static final RowMapper<User> MAPPER = (rs, i) -> new User(
            rs.getInt("id"),
            rs.getString("username"),
            rs.getString("password_hash"),
            rs.getString("full_name"),
            Role.valueOf(rs.getString("role")),
            rs.getString("photo_path"));

    private final JdbcTemplate jdbc;

    public JdbcUserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public User save(String username, String passwordHash, String fullName, Role role) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            var ps = con.prepareStatement(
                    "INSERT INTO users (username, password_hash, full_name, role) VALUES (?, ?, ?, ?)",
                    new String[]{"id"});
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.setString(3, fullName);
            ps.setString(4, role.name());
            return ps;
        }, keys);
        return new User(keys.getKey().intValue(), username, passwordHash, fullName, role, null);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jdbc.query("SELECT * FROM users WHERE username = ?", MAPPER, username).stream().findFirst();
    }

    @Override
    public List<User> findAll() {
        return jdbc.query("SELECT * FROM users ORDER BY id", MAPPER);
    }

    @Override
    public Optional<User> findById(Integer id) {
        return jdbc.query("SELECT * FROM users WHERE id = ?", MAPPER, id).stream().findFirst();
    }

    @Override
    public List<User> findBy(Predicate<User> filter) {
        return findAll().stream().filter(filter).toList();
    }

    @Override
    public User update(User u) {
        jdbc.update("UPDATE users SET password_hash = ?, full_name = ?, photo_path = ? WHERE id = ?",
                u.getPasswordHash(), u.getFullName(), u.getPhotoPath(), u.getId());
        return u;
    }

    @Override
    public void deleteById(Integer id) {
        jdbc.update("DELETE FROM users WHERE id = ?", id);
    }
}
