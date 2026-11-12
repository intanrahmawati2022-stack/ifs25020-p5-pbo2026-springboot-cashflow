package org.delcom.ifs25020springbootcashflow.framework.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.progressbar.ProgressBar;

/** Kartu putih berjudul; opsional dengan progress bar untuk aksi yang menunggu. */
public class PageCard extends Div {
    private final ProgressBar progress = new ProgressBar();

    public PageCard(String title) {
        this(title, false);
    }

    /** @param pageHeading true = judul kartu menjadi satu-satunya H1 halaman */
    public PageCard(String title, boolean pageHeading) {
        addClassName("cf-card");
        if (title != null) {
            Component h = pageHeading ? new H1(title) : new H2(title);
            h.getElement().getClassList().add("cf-card-title");
            add(h);
        }
        progress.setIndeterminate(true);
        progress.setVisible(false);
        add(progress);
    }

    public void setBusy(boolean busy) {
        progress.setVisible(busy);
    }

    public PageCard with(Component... components) {
        add(components);
        return this;
    }
}
