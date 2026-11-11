package org.delcom.ifs25020springbootcashflow.framework.view;

import org.delcom.ifs25020springbootcashflow.domain.entity.User;
import org.delcom.ifs25020springbootcashflow.usecase.UserUseCase;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/** Khusus ADMIN: daftar seluruh pengguna. */
@Route(value = "users", layout = MainLayout.class)
@PageTitle("Pengguna | Cashflow")
public class UsersView extends VerticalLayout implements AdminView {
    private final UserUseCase userUseCase;
    private final Grid<User> grid = new Grid<>(User.class, false);

    public UsersView(UserUseCase userUseCase) {
        this.userUseCase = userUseCase;
        setSizeFull();
        setPadding(true);
        GridStyles.apply(grid);
        grid.addColumn(User::getId).setHeader("ID").setSortable(true).setFlexGrow(0).setAutoWidth(true);
        grid.addColumn(User::getUsername).setHeader("Username").setSortable(true);
        grid.addColumn(User::getFullName).setHeader("Nama lengkap").setSortable(true);
        grid.addColumn(u -> u.getRole().name()).setHeader("Role").setSortable(true).setAutoWidth(true);
        grid.addComponentColumn(u -> {
            Button b = new Button("Lihat transaksi", VaadinIcon.LIST.create(),
                    e -> UI.getCurrent().navigate(UserTransactionsView.class, u.getId()));
            b.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            return b;
        }).setHeader("Aksi").setAutoWidth(true).setFlexGrow(0);
        add(grid);
        expand(grid);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        grid.setItems(userUseCase.listUsers());
    }
}
