package org.delcom.ifs25020springbootcashflow.framework.view;

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

@Route("register")
@PageTitle("Daftar | Cashflow")
public class RegisterView extends VerticalLayout implements BeforeEnterObserver {
    private final AuthUseCase auth;
    private final IUserRepository users;

    private final TextField fullName = new TextField("Nama lengkap");
    private final TextField username = new TextField("Username");
    private final PasswordField password = new PasswordField("Password");
    private final PasswordField confirm = new PasswordField("Ulangi password");
    private final PageCard card = new PageCard("Daftar akun Cashflow", true);

    public RegisterView(AuthUseCase auth, IUserRepository users) {
        this.auth = auth;
        this.users = users;
        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        addClassName("cf-auth-page");
        getElement().setAttribute("role", "main");

        fullName.setWidthFull();
        username.setWidthFull();
        password.setWidthFull();
        confirm.setWidthFull();

        Button submit = new Button("Daftar", e -> doRegister());
        submit.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submit.setWidthFull();

        Paragraph login = new Paragraph("Sudah punya akun? ");
        login.add(new Anchor("login", "Masuk"));

        card.with(fullName, username, password, confirm, submit, login);
        card.setWidth("min(92vw, 400px)");
        add(card);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (AuthSession.currentUser(users) != null) {
            event.rerouteTo("");
        }
    }

    private void doRegister() {
        if (!password.getValue().equals(confirm.getValue())) {
            Dialogs.error("Konfirmasi password tidak sama");
            return;
        }
        card.setBusy(true);
        try {
            auth.register(username.getValue(), password.getValue(), fullName.getValue());
            Dialogs.success("Registrasi berhasil, silakan masuk");
            UI.getCurrent().navigate("login");
        } catch (DomainException ex) {
            Dialogs.error(ex.getMessage());
        } finally {
            card.setBusy(false);
        }
    }
}
