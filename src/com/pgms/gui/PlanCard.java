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
    private final int gradient;
    private boolean selected;

    /** gradient = index into Theme.GRADS; onClick runs when the card is clicked. */
    public PlanCard(MessPlan plan, int tenants, int gradient, final Runnable onClick) {
        this.plan = plan;
        this.tenants = tenants;
        this.gradient = gradient;
        setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        setToolTipText("Click to select this plan");
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                onClick.run();
            }
        });
        setPreferredSize(new Dimension(220, 150));
    }

    public MessPlan plan() {
        return plan;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        Color[] gr = Theme.GRADS[gradient % Theme.GRADS.length];
        g.setPaint(new GradientPaint(0, 0, gr[0], w, h, gr[1]));
        g.fillRoundRect(0, 0, w - 1, h - 1, 18, 18);
        java.awt.Shape old = g.getClip();
        g.clip(new java.awt.geom.RoundRectangle2D.Float(0, 0, w - 1, h - 1, 18, 18));
        g.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.12f));
        g.setColor(Color.WHITE);
        g.fillOval(w - 80, -50, 140, 140);
        g.setComposite(java.awt.AlphaComposite.SrcOver);
        g.setClip(old);

        g.setColor(Theme.INK);
        g.setFont(Theme.H2);
        g.drawString(plan.getName(), 16, 34);
        g.setFont(Theme.SMALL);
        g.drawString(selected ? "PLAN #" + plan.getId() + "  |  SELECTED" : "PLAN #" + plan.getId(), 16, 56);

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
        g.setColor(new Color(0, 0, 0, 70));
        g.fillRoundRect(px, 38, pw, 26, 26, 26);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(1.4f));
        g.drawRoundRect(px, 38, pw, 26, 26, 26);
        g.setColor(Theme.INK);
        g.drawString(count, px + 11, 56);
        if (selected) {
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(3f));
            g.drawRoundRect(2, 2, w - 5, h - 5, 16, 16);
        }
        g.dispose();
    }
}
