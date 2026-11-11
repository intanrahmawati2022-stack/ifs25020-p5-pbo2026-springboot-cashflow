package org.delcom.ifs25020springbootcashflow.framework.view;

import org.delcom.ifs25020springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25020springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25020springbootcashflow.domain.entity.User;
import org.delcom.ifs25020springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25020springbootcashflow.usecase.UserUseCase;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/** Khusus ADMIN: transaksi pengguna lain (read-only). */
@Route(value = "user-transactions", layout = MainLayout.class)
@PageTitle("Transaksi Pengguna | Cashflow")
public class UserTransactionsView extends VerticalLayout implements AdminView, HasUrlParameter<Integer> {
    private final FinanceUseCase finance;
    private final UserUseCase userUseCase;

    private final Select<User> userSelect = new Select<>();
    private final TextField search = new TextField();
    private final Select<SortOption> sort = new Select<>();
    private final SummaryCards summary;
    private final TransactionGrid grid;

    public UserTransactionsView(FinanceUseCase finance, UserUseCase userUseCase, FinancePresenter presenter) {
        this.finance = finance;
        this.userUseCase = userUseCase;
        setSizeFull();
        setPadding(true);

        summary = new SummaryCards(presenter);
        grid = new TransactionGrid(presenter, null); // read-only

        userSelect.setLabel("Pengguna");
        userSelect.setItemLabelGenerator(u -> u.getFullName() + " (" + u.getUsername() + ")");
        userSelect.setItems(userUseCase.listUsers());
        userSelect.addValueChangeListener(e -> refresh());

        search.setPlaceholder("Cari keterangan...");
        search.setClearButtonVisible(true);
        search.setValueChangeMode(ValueChangeMode.LAZY);
        search.addValueChangeListener(e -> refresh());

        sort.setLabel("Urutkan");
        sort.setItems(SortOption.values());
        sort.setItemLabelGenerator(SortOption::label);
        sort.setValue(SortOption.NEWEST);
        sort.addValueChangeListener(e -> refresh());

        FlexLayout toolbar = new FlexLayout(userSelect, search, sort);
        toolbar.addClassName("cf-toolbar");
        add(toolbar, summary, grid);
        expand(grid);
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter Integer userId) {
        if (userId == null) return;
        userUseCase.listUsers().stream().filter(u -> u.getId() == userId).findFirst()
                .ifPresent(userSelect::setValue);
    }

    private void refresh() {
        User u = userSelect.getValue();
        if (u == null) {
            grid.setItems(java.util.List.of());
            summary.update(new FinanceUseCase.Summary(0, 0, 0));
            return;
        }
        grid.setItems(finance.search(u.getId(), search.getValue(), sort.getValue()));
        summary.update(finance.getSummary(u.getId()));
    }
}
