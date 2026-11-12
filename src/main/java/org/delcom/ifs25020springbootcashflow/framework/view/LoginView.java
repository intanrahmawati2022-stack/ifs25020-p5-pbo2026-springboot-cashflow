package org.delcom.ifs25020springbootcashflow.framework.view;

import org.delcom.ifs25020springbootcashflow.domain.entity.User;
import org.delcom.ifs25020springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25020springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25020springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25020springbootcashflow.usecase.AuthUseCase;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("login")
@PageTitle("Masuk | Cashflow")
public class LoginView extends VerticalLayout implements BeforeEnterObserver {
    private final AuthUseCase auth;
    private final IUserRepository users;

    private final TextField username = new TextField("Username");
    private final PasswordField password = new PasswordField("Password");
    private final PageCard card = new PageCard("Masuk ke Cashflow", true);

    public LoginView(AuthUseCase auth, IUserRepository users) {
        this.auth = auth;
        this.users = users;
        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        addClassName("cf-auth-page");
        getElement().setAttribute("role", "main");

        username.setId("login-username");
        username.setWidthFull();
        username.setAutofocus(true);
        password.setId("login-password");
        password.setWidthFull();

        Button submit = new Button("Masuk", e -> doLogin());
        submit.setId("login-submit");
        submit.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submit.setWidthFull();
        submit.addClickShortcut(com.vaadin.flow.component.Key.ENTER);

        Paragraph register = new Paragraph("Belum punya akun? ");
        register.add(new Anchor("register", "Daftar"));

        card.with(username, password, submit, register);
        card.setWidth("min(92vw, 400px)");
        add(card);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (AuthSession.currentUser(users) != null) {
            event.rerouteTo("");
        }
    }

    private void doLogin() {
        card.setBusy(true);
        try {
            User user = auth.login(username.getValue(), password.getValue());
            AuthSession.login(user);
            UI.getCurrent().navigate("");
        } catch (DomainException ex) {
            Dialogs.error(ex.getMessage());
        } finally {
            card.setBusy(false);
        }
    }
}
