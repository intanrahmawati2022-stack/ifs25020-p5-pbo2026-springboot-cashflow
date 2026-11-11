package org.delcom.ifs25020springbootcashflow.usecase;

import org.delcom.ifs25020springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25020springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25020springbootcashflow.adapter.repository.UserRepository;
import org.delcom.ifs25020springbootcashflow.domain.entity.*;
import org.delcom.ifs25020springbootcashflow.domain.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UseCaseTest {
    UserRepository users;
    TransactionRepository txs;
    AuthUseCase auth;
    UserUseCase userUseCase;
    FinanceUseCase finance;
    User budi;
    User sari;

    @BeforeEach
    void setUp() throws Exception {
        users = new UserRepository();
        txs = new TransactionRepository();
        auth = new AuthUseCase(users);
        userUseCase = new UserUseCase(users);
        finance = new FinanceUseCase(txs);
        budi = auth.register("budi", "rahasia", "Budi");
        sari = auth.register("sari", "rahasia", "Sari");
    }

    @Test
    void registerAndLogin() throws Exception {
        assertEquals(Role.USER, budi.getRole());
        assertEquals(budi.getId(), auth.login(" budi ", "rahasia").getId());
        assertThrows(ValidationException.class, () -> auth.login("budi", "salah"));
        assertThrows(ValidationException.class, () -> auth.login("nobody", "rahasia"));
        assertThrows(ValidationException.class, () -> auth.login("", "x"));
        assertThrows(ValidationException.class, () -> auth.login("budi", null));
        assertThrows(ValidationException.class, () -> auth.login(null, "x"));
        assertThrows(ValidationException.class, () -> auth.register("budi", "rahasia", "X"));
        assertThrows(ValidationException.class, () -> auth.register(" ", "rahasia", "X"));
        assertThrows(ValidationException.class, () -> auth.register(null, "rahasia", "X"));
        assertThrows(ValidationException.class, () -> auth.register("z", "rahasia", " "));
        assertThrows(ValidationException.class, () -> auth.register("z", "rahasia", null));
        assertThrows(ValidationException.class, () -> auth.register("z", "abc", "Z"));
        assertThrows(ValidationException.class, () -> auth.register("z", null, "Z"));
    }

    @Test
    void rootAdminSeededOnce() {
        User a = auth.ensureRootAdmin("admin", "admin");
        assertTrue(a.isAdmin());
        assertEquals(a.getId(), auth.ensureRootAdmin("admin", "other").getId());
    }

    @Test
    void userUseCase() throws Exception {
        assertEquals(2, userUseCase.listUsers().size());
        assertEquals("Budi", userUseCase.getUser(budi.getId()).getFullName());
        assertThrows(EntityNotFoundException.class, () -> userUseCase.getUser(999));
        String oldHash = budi.getPasswordHash();
        userUseCase.updateProfile(budi.getId(), " Budi S ", null);
        assertEquals("Budi S", userUseCase.getUser(budi.getId()).getFullName());
        assertEquals(oldHash, userUseCase.getUser(budi.getId()).getPasswordHash());
        userUseCase.updateProfile(budi.getId(), "Budi S", "");
        assertEquals(oldHash, userUseCase.getUser(budi.getId()).getPasswordHash());
        userUseCase.updateProfile(budi.getId(), "Budi S", "baru123");
        assertNotEquals(oldHash, userUseCase.getUser(budi.getId()).getPasswordHash());
        assertThrows(ValidationException.class, () -> userUseCase.updateProfile(budi.getId(), " ", null));
        assertThrows(ValidationException.class, () -> userUseCase.updateProfile(budi.getId(), "X", "abc"));
        assertThrows(ValidationException.class, () -> userUseCase.updateProfile(budi.getId(), null, null));
        userUseCase.updatePhoto(budi.getId(), "p.png");
        assertEquals("p.png", userUseCase.getUser(budi.getId()).getPhotoPath());
    }

    @Test
    void balanceFollowsIncomeAndExpense() throws Exception {
        finance.addIncome(budi.getId(), "Gaji", "5000000", null);
        finance.addExpense(budi.getId(), "Makan", "25000", "bukti.png");
        FinanceUseCase.Summary s = finance.getSummary(budi.getId());
        assertEquals(2, s.count());
        assertEquals(5000000, s.income());
        assertEquals(25000, s.expense());
        assertEquals(4975000, s.balance());
        assertEquals(4975000, finance.getBalance(budi.getId()));
        assertEquals(0, finance.getBalance(sari.getId()));
    }

    @Test
    void searchSortAndScope() throws Exception {
        finance.addIncome(budi.getId(), "Gaji Bulanan", "100", null);
        finance.addExpense(budi.getId(), "Makan", "50", null);
        finance.addIncome(sari.getId(), "Gaji Sari", "999", null);
        assertEquals(1, finance.search(budi.getId(), "gAjI", SortOption.NEWEST).size());
        assertEquals(2, finance.search(budi.getId(), null, null).size());
        assertEquals(2, finance.search(budi.getId(), "  ", SortOption.AMOUNT_ASC).size());
        assertEquals(50, finance.search(budi.getId(), "", SortOption.AMOUNT_ASC).get(0).getAmount());
        assertEquals(0, finance.search(budi.getId(), "zzz", null).size());
    }

    @Test
    void validation() {
        assertThrows(ValidationException.class, () -> finance.addIncome(budi.getId(), " ", "10", null));
        assertThrows(ValidationException.class, () -> finance.addIncome(budi.getId(), null, "10", null));
        assertThrows(ValidationException.class, () -> finance.addIncome(budi.getId(), "x", null, null));
        assertThrows(ValidationException.class, () -> finance.addIncome(budi.getId(), "x", " ", null));
        assertThrows(ValidationException.class, () -> finance.addIncome(budi.getId(), "x", "abc", null));
        assertThrows(ValidationException.class, () -> finance.addIncome(budi.getId(), "x", "10.5", null));
        assertThrows(ValidationException.class, () -> finance.addExpense(budi.getId(), "x", "0", null));
        assertThrows(ValidationException.class, () -> finance.addExpense(budi.getId(), "x", "-1", null));
    }

    @Test
    void updateAndDeleteRespectOwnership() throws Exception {
        Transaction t = finance.addIncome(budi.getId(), "Gaji", "100", null);
        finance.update(budi.getId(), t.getId(), "Gaji baru", "200");
        assertEquals(200, finance.findOwned(budi.getId(), t.getId()).getAmount());
        assertThrows(ValidationException.class, () -> finance.update(budi.getId(), t.getId(), "", "1"));
        assertThrows(ValidationException.class, () -> finance.update(budi.getId(), t.getId(), "x", "0"));
        assertThrows(ValidationException.class, () -> finance.delete(sari.getId(), t.getId()));
        assertThrows(ValidationException.class, () -> finance.delete(budi.getId(), 0));
        assertThrows(ValidationException.class, () -> finance.delete(budi.getId(), -3));
        assertThrows(EntityNotFoundException.class, () -> finance.delete(budi.getId(), 999));
        finance.delete(budi.getId(), t.getId());
        assertEquals(0, finance.getBalance(budi.getId()));
    }

    @Test
    void presenterFormatsMoneyAndLines() throws Exception {
        FinancePresenter p = new FinancePresenter();
        assertEquals("Rp 1,000,000.00", p.formatMoney(1_000_000));
        assertEquals("Rp 0.00", p.formatMoney(0));
        Transaction t = finance.addIncome(budi.getId(), "Gaji", "1000000", null);
        assertEquals("[" + t.getId() + "] Gaji | Rp 1,000,000.00 | [Pemasukan]", p.formatLine(t));
        assertEquals("Saldo: Rp 4,975,000.00", p.formatBalance(4_975_000));
        assertEquals("Saldo: Rp 1,000,000.00", p.formatSummary(finance.getSummary(budi.getId())));
        assertEquals("-", p.formatDate(null));
        assertNotEquals("-", p.formatDate(t.getCreatedAt()));
        assertEquals("Pengeluaran", p.typeLabel(TransactionType.EXPENSE));
        assertTrue(p.successAdded(t).contains("Gaji"));
        assertTrue(p.successDeleted(3).contains("3"));
        assertTrue(p.failure("x").contains("x"));
    }
}
