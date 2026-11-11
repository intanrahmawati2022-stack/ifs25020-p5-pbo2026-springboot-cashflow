package org.delcom.ifs25020springbootcashflow.framework.view;

import org.delcom.ifs25020springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25020springbootcashflow.usecase.FinanceUseCase;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;

/** Kartu ringkasan: total transaksi, pemasukan, pengeluaran, saldo. */
public class SummaryCards extends Div {
    private final FinancePresenter presenter;
    private final Span total = new Span();
    private final Span income = new Span();
    private final Span expense = new Span();
    private final Span balance = new Span();

    public SummaryCards(FinancePresenter presenter) {
        this.presenter = presenter;
        addClassName("cf-summary");
        add(card("Total transaksi", total, ""), card("Pemasukan", income, "cf-income"),
                card("Pengeluaran", expense, "cf-expense"), card("Saldo", balance, "cf-balance"));
        update(new FinanceUseCase.Summary(0, 0, 0));
    }

    public void update(FinanceUseCase.Summary s) {
        total.setText(String.valueOf(s.count()));
        income.setText(presenter.formatMoney(s.income()));
        expense.setText(presenter.formatMoney(s.expense()));
        balance.setText(presenter.formatMoney(s.balance()));
    }

    private static Div card(String label, Span value, String cssClass) {
        Span l = new Span(label);
        l.addClassName("cf-stat-label");
        value.addClassName("cf-stat-value");
        Div d = new Div(l, value);
        d.addClassNames("cf-stat", cssClass.isEmpty() ? "cf-total" : cssClass);
        return d;
    }
}
