package org.delcom.ifs25020springbootcashflow.domain;

import org.delcom.ifs25020springbootcashflow.domain.entity.*;
import org.delcom.ifs25020springbootcashflow.domain.exception.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DomainTest {

    @Test
    void passwordHasherIsSha256Hex() {
        assertEquals("8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92", PasswordHasher.hash("123456"));
        assertTrue(PasswordHasher.verify("123456", PasswordHasher.hash("123456")));
        assertFalse(PasswordHasher.verify("x", PasswordHasher.hash("123456")));
    }

    @Test
    void sortOptionsOrderTransactions() {
        LocalDateTime now = LocalDateTime.now();
        Transaction a = new Transaction(1, 1, "a", 100, TransactionType.INCOME, null, now.minusMinutes(2));
        Transaction b = new Transaction(2, 1, "b", 300, TransactionType.EXPENSE, null, now.minusMinutes(1));
        Transaction c = new Transaction(3, 1, "c", 200, TransactionType.INCOME, null, now);
        for (SortOption o : SortOption.values()) {
            assertNotNull(o.label());
        }
        assertEquals(3, sorted(SortOption.NEWEST, a, b, c).get(0).getId());
        assertEquals(1, sorted(SortOption.AMOUNT_ASC, a, b, c).get(0).getId());
        assertEquals(2, sorted(SortOption.AMOUNT_DESC, a, b, c).get(0).getId());
        assertEquals(TransactionType.INCOME, sorted(SortOption.INCOME_FIRST, a, b, c).get(0).getType());
        assertEquals(TransactionType.EXPENSE, sorted(SortOption.EXPENSE_FIRST, a, b, c).get(0).getType());
    }

    private static List<Transaction> sorted(SortOption o, Transaction... t) {
        List<Transaction> l = new ArrayList<>(List.of(t));
        l.sort(o.comparator());
        return l;
    }

    @Test
    void entitiesAndEnums() {
        assertEquals("Pemasukan", TransactionType.INCOME.label());
        User u = new User(1, "u", "h", "U", Role.ADMIN, null);
        assertTrue(u.isAdmin());
        u.setId(5); u.setFullName("N"); u.setPasswordHash("x"); u.setPhotoPath("p.png");
        assertEquals(5, u.getId());
        assertEquals("N", u.getFullName());
        assertEquals("x", u.getPasswordHash());
        assertEquals("p.png", u.getPhotoPath());
        assertFalse(new User(2, "v", "h", "V", Role.USER, null).isAdmin());
        Transaction t = new Transaction(1, 2, "d", 5, TransactionType.EXPENSE, null, LocalDateTime.now());
        t.setId(9); t.setDescription("e"); t.setAmount(7); t.setProofPath("q.png");
        assertEquals(9, t.getId());
        assertEquals(2, t.getUserId());
        assertEquals("e", t.getDescription());
        assertEquals(7, t.getAmount());
        assertEquals("q.png", t.getProofPath());
    }

    @Test
    void exceptionsCarryMessage() {
        assertTrue(new EntityNotFoundException("User", 3).getMessage().contains("3"));
        assertEquals("x", new ValidationException("x").getMessage());
        assertInstanceOf(DomainException.class, new ValidationException("x"));
    }
}
