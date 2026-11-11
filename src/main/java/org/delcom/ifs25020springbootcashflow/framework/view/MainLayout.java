package org.delcom.ifs25020springbootcashflow.framework.view;

import org.delcom.ifs25020springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25020springbootcashflow.domain.entity.User;
import org.delcom.ifs25020springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25020springbootcashflow.framework.auth.AuthSession;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;

/** Layout terproteksi: guard login + guard admin, sidebar, dan menu avatar (Profil / Keluar). */
public class MainLayout extends AppLayout implements BeforeEnterObserver {
    private final IUserRepository users;
    private final HorizontalLayout navbar = new HorizontalLayout();
    private final Div drawerBox = new Div();

    public MainLayout(IUserRepository users) {
        this.users = users;
        setPrimarySection(Section.DRAWER);

        navbar.setWidthFull();
        navbar.setAlignItems(FlexComponent.Alignment.CENTER);
        navbar.addClassName("cf-navbar");
        addToNavbar(new DrawerToggle(), navbar);

        drawerBox.addClassName("cf-drawer");
        addToDrawer(drawerBox);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User user = AuthSession.currentUser(users);
        if (user == null) {
            event.rerouteTo("login");
            return;
        }
        if (AdminView.class.isAssignableFrom(event.getNavigationTarget()) && !user.isAdmin()) {
            event.rerouteTo("");
            return;
        }
        build(user);
    }

    private void build(User user) {
        navbar.removeAll();
        drawerBox.removeAll();

        H1 title = new H1("Cashflow");
        title.addClassName("cf-brand");
        drawerBox.add(title);

        SideNav nav = new SideNav();
        nav.addItem(new SideNavItem("Transaksi", FinanceView.class, VaadinIcon.WALLET.create()));
        nav.addItem(new SideNavItem("Profil", ProfileView.class, VaadinIcon.USER.create()));
        if (user.isAdmin()) {
            nav.addItem(new SideNavItem("Pengguna", UsersView.class, VaadinIcon.USERS.create()));
            nav.addItem(new SideNavItem("Transaksi Pengguna", UserTransactionsView.class,
                    VaadinIcon.LIST_SELECT.create()));
        }
        drawerBox.add(nav);

        Span greeting = new Span(user.getFullName() + (user.isAdmin() ? " (Admin)" : ""));
        greeting.addClassName("cf-greeting");

        Avatar avatar = new Avatar(user.getFullName());
        if (user.getPhotoPath() != null) {
            avatar.setImage(FileStorage.urlOf(user.getPhotoPath()));
        }
        MenuBar menu = new MenuBar();
        menu.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
        var root = menu.addItem(avatar);
        root.getSubMenu().addItem("Profil", e -> UI.getCurrent().navigate(ProfileView.class));
        root.getSubMenu().addItem("Keluar", e -> {
            AuthSession.logout();
            UI.getCurrent().navigate("login");
        });

        Div spacer = new Div();
        navbar.add(spacer, greeting, menu);
        navbar.expand(spacer);
    }
}
