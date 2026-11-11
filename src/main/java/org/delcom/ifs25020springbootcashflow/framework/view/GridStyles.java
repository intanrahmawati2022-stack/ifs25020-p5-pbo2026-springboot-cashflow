package org.delcom.ifs25020springbootcashflow.framework.view;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;

public final class GridStyles {
    private GridStyles() {
    }

    public static void apply(Grid<?> grid) {
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_WRAP_CELL_CONTENT);
        grid.addClassName("cf-grid");
        grid.setAllRowsVisible(false);
        grid.setMinHeight("260px");
    }
}
