package com.pgms.gui;

import com.pgms.model.MessPlan;
import com.pgms.util.Money;

import javax.swing.JComponent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/** A mess plan as a card: name, one bowl per meal, monthly price and how many tenants are on it. */
public class PlanCard extends JComponent {

    private static final long serialVersionUID = 1L;
    private final MessPlan plan;
    private final int tenants;
    private final Color accent;

    public PlanCard(MessPlan plan, int tenants, Color accent) {
        this.plan = plan;
        this.tenants = tenants;
        this.accent = accent;
        setPreferredSize(new Dimension(220, 150));
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        g.setPaint(new GradientPaint(0, 0, Theme.RAISE, 0, h, Theme.PANEL));
        g.fillRoundRect(0, 0, w - 1, h - 1, 16, 16);
        g.setColor(Theme.LINE);
        g.drawRoundRect(0, 0, w - 1, h - 1, 16, 16);
        g.setColor(accent);
        g.fillRoundRect(16, 0, 40, 4, 4, 4);

        g.setColor(Theme.INK);
        g.setFont(Theme.H2);
        g.drawString(plan.getName(), 16, 34);
        g.setFont(Theme.SMALL);
        g.drawString("PLAN #" + plan.getId(), 16, 56);

        int x = 16;
        for (int i = 0; i < plan.getMealsPerDay(); i++) {
            Icons.of("bowl", 22, Theme.INK).paintIcon(this, g, x, 64);
            x += 30;
        }
        g.drawString(plan.getMealsPerDay() + " MEAL" + (plan.getMealsPerDay() > 1 ? "S" : "") + " / DAY", x + 6, 81);

        g.setFont(Theme.font(java.awt.Font.BOLD, 22));
        String price = Money.inr(plan.monthlyCharge());
        g.drawString(price, 16, h - 20);
        int pw0 = g.getFontMetrics().stringWidth(price);
        g.setFont(Theme.SMALL);
        g.drawString("/ MONTH", 16 + pw0 + 6, h - 20);

        String count = tenants + (tenants == 1 ? " TENANT" : " TENANTS");
        FontMetrics fm = g.getFontMetrics();
        int pw = fm.stringWidth(count) + 22;
        int px = w - pw - 14;
        g.setColor(Theme.BG2);
        g.fillRoundRect(px, 38, pw, 26, 26, 26);
        g.setColor(accent);
        g.setStroke(new BasicStroke(1.4f));
        g.drawRoundRect(px, 38, pw, 26, 26, 26);
        g.setColor(Theme.INK);
        g.drawString(count, px + 11, 56);
        g.dispose();
    }
}
