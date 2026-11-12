package org.delcom.ifs25020springbootcashflow.framework.view;

import org.delcom.ifs25020springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25020springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25020springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25020springbootcashflow.domain.entity.TransactionType;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;

import java.util.Comparator;
import java.util.function.Consumer;

/** Grid transaksi: ID, keterangan, jumlah, tipe, bukti (thumbnail), waktu, hapus (opsional). */
public class TransactionGrid extends Grid<Transaction> {

    /** @param onDelete null = read-only (tanpa kolom tindakan) */
    public TransactionGrid(FinancePresenter presenter, Consumer<Transaction> onDelete) {
        super(Transaction.class, false);
        GridStyles.apply(this);

        addColumn(Transaction::getId).setHeader("ID").setSortable(true).setAutoWidth(true).setFlexGrow(0);
        addColumn(Transaction::getDescription).setHeader("Keterangan").setSortable(true).setFlexGrow(1);
        addColumn(t -> presenter.formatMoney(t.getAmount())).setHeader("Jumlah")
                .setComparator(Comparator.comparingLong(Transaction::getAmount))
                .setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.END).setAutoWidth(true);
        addComponentColumn(t -> {
            Span badge = new Span(presenter.typeLabel(t.getType()));
            badge.getElement().getThemeList().add("badge " + (t.getType() == TransactionType.INCOME ? "success" : "error"));
            return badge;
        }).setHeader("Tipe").setComparator(Comparator.comparing(Transaction::getType)).setAutoWidth(true);
        addComponentColumn(t -> {
            if (t.getProofPath() == null) return new Span("-");
            Image img = new Image(FileStorage.urlOf(t.getProofPath()), "Bukti transaksi " + t.getId());
            img.addClassName("cf-thumb");
            Button open = new Button(img, e -> ImagePreviewDialog.show(FileStorage.urlOf(t.getProofPath()),
                    "Bukti transaksi [" + t.getId() + "]"));
            open.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            open.setAriaLabel("Perbesar bukti transaksi " + t.getId());
            return open;
        }).setHeader("Bukti").setAutoWidth(true);
        addColumn(t -> presenter.formatDate(t.getCreatedAt())).setHeader("Waktu")
                .setComparator(Comparator.comparing(Transaction::getCreatedAt)).setAutoWidth(true);

        if (onDelete != null) {
            addComponentColumn(t -> {
                Button del = new Button(VaadinIcon.TRASH.create(), e -> onDelete.accept(t));
                del.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
                del.setAriaLabel("Hapus transaksi " + t.getId());
                return del;
            }).setHeader("Aksi").setAutoWidth(true).setFlexGrow(0);
        }
    }
}
