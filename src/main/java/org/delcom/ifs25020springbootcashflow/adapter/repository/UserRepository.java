package org.delcom.ifs25020springbootcashflow.adapter.repository;

import org.delcom.ifs25020springbootcashflow.domain.entity.Role;
import org.delcom.ifs25020springbootcashflow.domain.entity.User;
import org.delcom.ifs25020springbootcashflow.domain.repository.IUserRepository;

import java.util.Optional;

/** InMemory IUserRepository (unit test). */
public class UserRepository extends InMemoryRepository<User> implements IUserRepository {

    @Override
    protected int idOf(User entity) {
        return entity.getId();
    }

    @Override
    protected void assignId(User entity, int id) {
        entity.setId(id);
    }

    @Override
    public User save(String username, String passwordHash, String fullName, Role role) {
        return put(new User(0, username, passwordHash, fullName, role, null));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return store.values().stream().filter(u -> u.getUsername().equals(username)).findFirst();
    }
}
