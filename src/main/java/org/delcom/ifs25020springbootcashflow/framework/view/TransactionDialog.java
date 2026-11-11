package org.delcom.ifs25020springbootcashflow.framework.view;

import org.delcom.ifs25020springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25020springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25020springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25020springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25020springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25020springbootcashflow.usecase.FinanceUseCase;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextField;

import java.io.IOException;

/** Form tambah pemasukan / pengeluaran + upload bukti opsional. */
public class TransactionDialog extends Dialog {
    private final TextField description = new TextField("Keterangan");
    private final TextField amount = new TextField("Jumlah (Rp)");
    private final ImageUpload proof = new ImageUpload("Unggah bukti (opsional)");
    private final ProgressBar progress = new ProgressBar();

    public TransactionDialog(TransactionType type, int userId, FinanceUseCase finance, FinancePresenter presenter,
                             FileStorage storage, Runnable onSaved) {
        addClassName("cf-dialog");
        setModal(true);
        setCloseOnOutsideClick(false);
        setHeaderTitle(type == TransactionType.INCOME ? "Tambah Pemasukan" : "Tambah Pengeluaran");
        setWidth("min(92vw, 440px)");

        description.setWidthFull();
        amount.setWidthFull();
        amount.setPlaceholder("contoh: 25000");
        progress.setIndeterminate(true);
        progress.setVisible(false);
        add(new VerticalLayout(description, amount, proof, progress));

        Button cancel = new Button("Batal", e -> close());
        Button save = new Button("Simpan");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickListener(e -> {
            save.setEnabled(false);
            progress.setVisible(true);
            String stored = null;
            try {
                if (proof.hasFile()) {
                    stored = storage.store(proof.bytes(), proof.fileName());
                }
                Transaction t = type == TransactionType.INCOME
                        ? finance.addIncome(userId, description.getValue(), amount.getValue(), stored)
                        : finance.addExpense(userId, description.getValue(), amount.getValue(), stored);
                Dialogs.success(presenter.successAdded(t));
                close();
                onSaved.run();
            } catch (DomainException ex) {
                storage.delete(stored);
                Dialogs.error(presenter.failure(ex.getMessage()));
            } catch (IOException ex) {
                Dialogs.error(presenter.failure("file bukti tidak dapat disimpan"));
            } finally {
                save.setEnabled(true);
                progress.setVisible(false);
            }
        });
        getFooter().add(cancel, save);
    }
}
