package org.delcom.ifs25020springbootcashflow.framework.view;

import org.delcom.ifs25020springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25020springbootcashflow.domain.entity.User;
import org.delcom.ifs25020springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25020springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25020springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25020springbootcashflow.usecase.UserUseCase;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.io.IOException;

/** Pengaturan akun: nama, password, foto (USER & ADMIN). */
@Route(value = "profile", layout = MainLayout.class)
@PageTitle("Profil | Cashflow")
public class ProfileView extends VerticalLayout {
    private final UserUseCase userUseCase;
    private final IUserRepository users;
    private final FileStorage storage;

    private final Avatar avatar = new Avatar();
    private final TextField username = new TextField("Username");
    private final TextField fullName = new TextField("Nama lengkap");
    private final PasswordField password = new PasswordField("Password baru (kosongkan bila tidak diubah)");
    private final ImageUpload photo = new ImageUpload("Pilih foto profil");
    private final PageCard card = new PageCard("Profil");

    public ProfileView(UserUseCase userUseCase, IUserRepository users, FileStorage storage) {
        this.userUseCase = userUseCase;
        this.users = users;
        this.storage = storage;
        setPadding(true);

        avatar.setHeight("96px");
        avatar.setWidth("96px");
        username.setReadOnly(true);
        username.setWidthFull();
        fullName.setWidthFull();
        password.setWidthFull();

        Button save = new Button("Simpan", e -> save());
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        card.with(avatar, username, fullName, password, photo, save);
        card.setWidth("min(92vw, 480px)");
        add(card);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        User u = AuthSession.currentUser(users);
        if (u == null) return;
        username.setValue(u.getUsername());
        fullName.setValue(u.getFullName());
        avatar.setName(u.getFullName());
        avatar.setImage(FileStorage.urlOf(u.getPhotoPath()));
    }

    private void save() {
        User u = AuthSession.currentUser(users);
        if (u == null) return;
        card.setBusy(true);
        String newPhoto = null;
        try {
            String oldPhoto = u.getPhotoPath();
            userUseCase.updateProfile(u.getId(), fullName.getValue(), password.getValue());
            if (photo.hasFile()) {
                newPhoto = storage.store(photo.bytes(), photo.fileName());
                userUseCase.updatePhoto(u.getId(), newPhoto);
                storage.delete(oldPhoto);
            }
            Dialogs.success("Profil diperbarui");
            UI.getCurrent().getPage().reload();
        } catch (DomainException ex) {
            storage.delete(newPhoto);
            Dialogs.error(ex.getMessage());
        } catch (IOException ex) {
            Dialogs.error("File foto tidak dapat disimpan");
        } finally {
            card.setBusy(false);
        }
    }
}
