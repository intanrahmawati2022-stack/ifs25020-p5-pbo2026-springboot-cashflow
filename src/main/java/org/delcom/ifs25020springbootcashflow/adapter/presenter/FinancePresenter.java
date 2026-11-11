package org.delcom.ifs25020springbootcashflow.adapter.presenter;

import org.delcom.ifs25020springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25020springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25020springbootcashflow.usecase.FinanceUseCase;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Format uang / teks; bukan tanggung jawab entity. */
public class FinancePresenter {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", Locale.ENGLISH);

    /** 1000000 -> "Rp 1,000,000.00" */
    public String formatMoney(long amount) {
        DecimalFormat df = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
        return "Rp " + df.format(BigDecimal.valueOf(amount));
    }

    public String typeLabel(TransactionType type) {
        return type.label();
    }

    /** [id] keterangan | Rp 1,000,000.00 | [Pemasukan] */
    public String formatLine(Transaction t) {
        return "[" + t.getId() + "] " + t.getDescription() + " | " + formatMoney(t.getAmount())
                + " | [" + typeLabel(t.getType()) + "]";
    }

    public String formatBalance(long balance) {
        return "Saldo: " + formatMoney(balance);
    }

    public String formatDate(LocalDateTime time) {
        return time == null ? "-" : DATE_FORMAT.format(time);
    }

    public String formatSummary(FinanceUseCase.Summary s) {
        return formatBalance(s.balance());
    }

    public String successAdded(Transaction t) {
        return "Transaksi ditambahkan: " + formatLine(t);
    }

    public String successDeleted(int id) {
        return "Transaksi [" + id + "] dihapus";
    }

    public String failure(String message) {
        return "Gagal: " + message;
    }
}
