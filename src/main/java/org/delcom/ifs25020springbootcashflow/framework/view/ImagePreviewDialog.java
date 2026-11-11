package org.delcom.ifs25020springbootcashflow.framework.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;

/** Modal zoom interaktif: klik gambar (toggle) atau tombol kontrol. */
public class ImagePreviewDialog extends Dialog {
    private static final double[] LEVELS = {1.0, 1.5, 2.0, 3.0};
    private final Image image;
    private int level = 0;

    public ImagePreviewDialog(String url, String title) {
        addClassName("cf-dialog");
        setHeaderTitle(title);
        setModal(true);
        setWidth("min(92vw, 760px)");

        image = new Image(url, title);
        image.addClassName("cf-preview-image");
        image.addClickListener(e -> setLevel(level == 0 ? 2 : 0));
        Div box = new Div(image);
        box.addClassName("cf-preview-box");
        add(box);

        Button out = new Button(VaadinIcon.SEARCH_MINUS.create(), e -> setLevel(Math.max(0, level - 1)));
        Button reset = new Button("100%", e -> setLevel(0));
        Button in = new Button(VaadinIcon.SEARCH_PLUS.create(), e -> setLevel(Math.min(LEVELS.length - 1, level + 1)));
        Button close = new Button("Tutup", e -> close());
        out.setAriaLabel("Perkecil");
        in.setAriaLabel("Perbesar");
        getFooter().add(out, reset, in, close);
    }

    private void setLevel(int newLevel) {
        level = newLevel;
        image.getStyle().set("transform", "scale(" + LEVELS[level] + ")");
        image.getStyle().set("cursor", level == 0 ? "zoom-in" : "zoom-out");
    }

    public static void show(String url, String title) {
        new ImagePreviewDialog(url, title).open();
    }
}
