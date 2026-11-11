package org.delcom.ifs25020springbootcashflow.domain.entity;

import java.util.Comparator;

/** Opsi pengurutan transaksi; tiap opsi membawa Comparator. */
public enum SortOption {
    NEWEST("Terbaru",
            Comparator.comparing(Transaction::getCreatedAt).thenComparingInt(Transaction::getId).reversed()),
    AMOUNT_ASC("Jumlah naik",
            Comparator.comparingLong(Transaction::getAmount).thenComparingInt(Transaction::getId)),
    AMOUNT_DESC("Jumlah turun",
            Comparator.comparingLong(Transaction::getAmount).reversed().thenComparingInt(Transaction::getId)),
    INCOME_FIRST("Pemasukan dulu",
            Comparator.comparing((Transaction t) -> t.getType() != TransactionType.INCOME)
                    .thenComparingInt(Transaction::getId)),
    EXPENSE_FIRST("Pengeluaran dulu",
            Comparator.comparing((Transaction t) -> t.getType() != TransactionType.EXPENSE)
                    .thenComparingInt(Transaction::getId));

    private final String label;
    private final Comparator<Transaction> comparator;

    SortOption(String label, Comparator<Transaction> comparator) {
        this.label = label;
        this.comparator = comparator;
    }

    public String label() { return label; }
    public Comparator<Transaction> comparator() { return comparator; }
}
