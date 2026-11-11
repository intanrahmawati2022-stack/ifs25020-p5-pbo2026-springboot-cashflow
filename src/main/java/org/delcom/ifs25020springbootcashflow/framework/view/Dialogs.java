package org.delcom.ifs25020springbootcashflow.framework.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

/** Dialog modal (header biru, footer abu-abu, tombol rata kanan) + notifikasi. */
public final class Dialogs {
    private Dialogs() {
    }

    public static Dialog base(String title) {
        Dialog dialog = new Dialog();
        dialog.addClassName("cf-dialog");
        dialog.setHeaderTitle(title);
        dialog.setModal(true);
        dialog.setCloseOnOutsideClick(false);
        return dialog;
    }

    public static void confirm(String title, String message, String confirmLabel, Runnable onConfirm) {
        Dialog dialog = base(title);
        dialog.add(new Span(message));
        Button cancel = new Button("Batal", e -> dialog.close());
        Button ok = new Button(confirmLabel, e -> {
            dialog.close();
            onConfirm.run();
        });
        ok.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        dialog.getFooter().add(cancel, ok);
        dialog.open();
    }

    public static void success(String message) {
        Notification n = Notification.show(message, 3000, Notification.Position.TOP_CENTER);
        n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    public static void error(String message) {
        Notification n = Notification.show(message, 4500, Notification.Position.TOP_CENTER);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
