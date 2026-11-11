package org.delcom.ifs25020springbootcashflow.usecase;

import org.delcom.ifs25020springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25020springbootcashflow.domain.entity.Role;
import org.delcom.ifs25020springbootcashflow.domain.entity.User;
import org.delcom.ifs25020springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25020springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25020springbootcashflow.domain.repository.IUserRepository;

public class AuthUseCase {
    private final IUserRepository users;

    public AuthUseCase(IUserRepository users) {
        this.users = users;
    }

    public User register(String username, String password, String fullName) throws DomainException {
        String u = username == null ? "" : username.trim();
        String n = fullName == null ? "" : fullName.trim();
        if (u.isEmpty()) throw new ValidationException("Username tidak boleh kosong");
        if (n.isEmpty()) throw new ValidationException("Nama lengkap tidak boleh kosong");
        if (password == null || password.length() < 4) throw new ValidationException("Password minimal 4 karakter");
        if (users.findByUsername(u).isPresent()) throw new ValidationException("Username sudah dipakai");
        return users.save(u, PasswordHasher.hash(password), n, Role.USER);
    }

    public User login(String username, String password) throws DomainException {
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            throw new ValidationException("Username dan password wajib diisi");
        }
        User user = users.findByUsername(username.trim()).orElse(null);
        if (user == null || !PasswordHasher.verify(password, user.getPasswordHash())) {
            throw new ValidationException("Username atau password salah");
        }
        return user;
    }

    /** Seed admin dari ROOT_USERNAME / ROOT_PASSWORD bila belum ada. */
    public User ensureRootAdmin(String username, String password) {
        return users.findByUsername(username).orElseGet(
                () -> users.save(username, PasswordHasher.hash(password), "Administrator", Role.ADMIN));
    }
}
