package org.delcom.ifs25020springbootcashflow.framework.view;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.springframework.stereotype.Component;

/** Indikator loading global yang responsif (muncul cepat, naik status bila lama). */
@Component
public class LoadingIndicatorInitListener implements VaadinServiceInitListener {

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiEvent -> {
            var config = uiEvent.getUI().getLoadingIndicatorConfiguration();
            config.setFirstDelay(50);
            config.setSecondDelay(1000);
            config.setThirdDelay(3000);
            // <html lang> wajib untuk aksesibilitas (Axe: html-has-lang)
            uiEvent.getUI().getPage().executeJs("document.documentElement.lang = 'id'");
        });
    }
}
