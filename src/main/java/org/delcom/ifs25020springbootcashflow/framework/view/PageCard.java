package org.delcom.ifs25020springbootcashflow.framework.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.progressbar.ProgressBar;

/** Kartu putih berjudul; opsional dengan progress bar untuk aksi yang menunggu. */
public class PageCard extends Div {
    private final ProgressBar progress = new ProgressBar();

    public PageCard(String title) {
        addClassName("cf-card");
        if (title != null) {
            H2 h = new H2(title);
            h.addClassName("cf-card-title");
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
