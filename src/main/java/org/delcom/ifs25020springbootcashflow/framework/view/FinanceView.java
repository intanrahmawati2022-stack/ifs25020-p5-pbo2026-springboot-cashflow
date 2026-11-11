package org.delcom.ifs25020springbootcashflow.framework.view;

import org.delcom.ifs25020springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25020springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25020springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25020springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25020springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25020springbootcashflow.domain.entity.User;
import org.delcom.ifs25020springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25020springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25020springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25020springbootcashflow.usecase.FinanceUseCase;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/** Dashboard transaksi pribadi (USER & ADMIN). */
@Route(value = "", layout = MainLayout.class)
@PageTitle("Transaksi | Cashflow")
public class FinanceView extends VerticalLayout {
    private final FinanceUseCase finance;
    private final FinancePresenter presenter;
    private final FileStorage storage;
    private final IUserRepository users;

    private final SummaryCards summary;
    private final TransactionGrid grid;
    private final TextField search = new TextField();
    private final Select<SortOption> sort = new Select<>();

    public FinanceView(FinanceUseCase finance, FinancePresenter presenter, FileStorage storage, IUserRepository users) {
        this.finance = finance;
        this.presenter = presenter;
        this.storage = storage;
        this.users = users;
        setSizeFull();
        setPadding(true);

        summary = new SummaryCards(presenter);
        grid = new TransactionGrid(presenter, this::confirmDelete);

        search.setPlaceholder("Cari keterangan...");
        search.setPrefixComponent(VaadinIcon.SEARCH.create());
        search.setClearButtonVisible(true);
        search.setValueChangeMode(ValueChangeMode.LAZY);
        search.addValueChangeListener(e -> refresh());

        sort.setItems(SortOption.values());
        sort.setItemLabelGenerator(SortOption::label);
        sort.setValue(SortOption.NEWEST);
        sort.setLabel("Urutkan");
        sort.addValueChangeListener(e -> refresh());

        Button reload = new Button("Muat ulang", VaadinIcon.REFRESH.create(), e -> refresh());
        Button income = new Button("Pemasukan", VaadinIcon.PLUS.create(), e -> openForm(TransactionType.INCOME));
        income.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);
        Button expense = new Button("Pengeluaran", VaadinIcon.MINUS.create(), e -> openForm(TransactionType.EXPENSE));
        expense.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

        FlexLayout toolbar = new FlexLayout(search, sort, reload, income, expense);
        toolbar.addClassName("cf-toolbar");
        add(summary, toolbar, grid);
        expand(grid);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        refresh();
    }

    private User user() {
        return AuthSession.currentUser(users);
    }

    private void refresh() {
        User u = user();
        if (u == null) return;
        grid.setItems(finance.search(u.getId(), search.getValue(), sort.getValue()));
        summary.update(finance.getSummary(u.getId()));
    }

    private void openForm(TransactionType type) {
        User u = user();
        if (u == null) return;
        new TransactionDialog(type, u.getId(), finance, presenter, storage, this::refresh).open();
    }

    private void confirmDelete(Transaction t) {
        Dialogs.confirm("Hapus transaksi", "Hapus " + presenter.formatLine(t) + " ?", "Hapus", () -> {
            User u = user();
            if (u == null) return;
            try {
                finance.delete(u.getId(), t.getId());
                storage.delete(t.getProofPath());
                Dialogs.success(presenter.successDeleted(t.getId()));
                refresh();
            } catch (DomainException ex) {
                Dialogs.error(presenter.failure(ex.getMessage()));
            }
        });
    }
}
