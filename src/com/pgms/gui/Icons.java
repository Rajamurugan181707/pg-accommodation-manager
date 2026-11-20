package com.pgms.gui;

import javax.swing.Icon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

/** Small line icons drawn with Java2D, so the project needs no image files. */
public final class Icons {

    private Icons() {
    }

    public static Icon of(final String name, final int size, final Color color) {
        return new Icon() {
            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }

            @Override
            public void paintIcon(Component c, Graphics g0, int x, int y) {
                Graphics2D g = (Graphics2D) g0.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.translate(x, y);
                g.setColor(color);
                g.setStroke(new BasicStroke(Math.max(1.6f, size / 9f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                draw(g, name, size);
                g.dispose();
            }
        };
    }

    static void draw(Graphics2D g, String name, double s) {
        if ("home".equals(name)) {
            g.draw(rr(s, .10, .10, .34, .34));
            g.draw(rr(s, .56, .10, .34, .34));
            g.draw(rr(s, .10, .56, .34, .34));
            g.draw(rr(s, .56, .56, .34, .34));
        } else if ("bed".equals(name)) {
            g.draw(line(s, .08, .25, .08, .85));
            g.draw(rr(s, .08, .52, .84, .2));
            g.draw(line(s, .92, .72, .92, .85));
            g.draw(rr(s, .17, .37, .22, .15));
        } else if ("users".equals(name)) {
            g.draw(new Ellipse2D.Double(s * .14, s * .12, s * .32, s * .32));
            g.draw(new Arc2D.Double(s * .04, s * .52, s * .52, s * .6, 0, 180, Arc2D.OPEN));
            g.draw(new Ellipse2D.Double(s * .58, s * .2, s * .26, s * .26));
            g.draw(new Arc2D.Double(s * .56, s * .54, s * .4, s * .5, 0, 160, Arc2D.OPEN));
        } else if ("receipt".equals(name)) {
            Path2D p = new Path2D.Double();
            p.moveTo(s * .2, s * .08);
            p.lineTo(s * .8, s * .08);
            p.lineTo(s * .8, s * .92);
            p.lineTo(s * .65, s * .82);
            p.lineTo(s * .5, s * .92);
            p.lineTo(s * .35, s * .82);
            p.lineTo(s * .2, s * .92);
            p.closePath();
            g.draw(p);
            g.draw(line(s, .33, .3, .67, .3));
            g.draw(line(s, .33, .48, .67, .48));
            g.draw(line(s, .33, .64, .55, .64));
        } else if ("bowl".equals(name)) {
            g.draw(new Arc2D.Double(s * .08, s * .2, s * .84, s * .7, 180, 180, Arc2D.CHORD));
            g.draw(line(s, .35, .1, .35, .35));
            g.draw(line(s, .5, .06, .5, .35));
            g.draw(line(s, .65, .1, .65, .35));
        } else if ("plus".equals(name)) {
            g.draw(line(s, .5, .15, .5, .85));
            g.draw(line(s, .15, .5, .85, .5));
        } else if ("search".equals(name)) {
            g.draw(new Ellipse2D.Double(s * .1, s * .1, s * .55, s * .55));
            g.draw(line(s, .58, .58, .9, .9));
        } else if ("edit".equals(name)) {
            Path2D p = new Path2D.Double();
            p.moveTo(s * .15, s * .85);
            p.lineTo(s * .2, s * .65);
            p.lineTo(s * .68, s * .17);
            p.lineTo(s * .83, s * .32);
            p.lineTo(s * .35, s * .8);
            p.closePath();
            g.draw(p);
        } else if ("trash".equals(name)) {
            g.draw(line(s, .12, .25, .88, .25));
            g.draw(line(s, .38, .12, .62, .12));
            Path2D p = new Path2D.Double();
            p.moveTo(s * .22, s * .25);
            p.lineTo(s * .28, s * .9);
            p.lineTo(s * .72, s * .9);
            p.lineTo(s * .78, s * .25);
            g.draw(p);
        } else if ("move".equals(name)) {
            g.draw(line(s, .12, .33, .85, .33));
            g.draw(line(s, .68, .18, .85, .33));
            g.draw(line(s, .68, .48, .85, .33));
            g.draw(line(s, .88, .68, .15, .68));
            g.draw(line(s, .32, .53, .15, .68));
            g.draw(line(s, .32, .83, .15, .68));
        } else if ("exit".equals(name)) {
            g.draw(rr(s, .1, .1, .45, .8));
            g.draw(line(s, .4, .5, .92, .5));
            g.draw(line(s, .76, .34, .92, .5));
            g.draw(line(s, .76, .66, .92, .5));
        } else if ("info".equals(name)) {
            g.draw(new Ellipse2D.Double(s * .08, s * .08, s * .84, s * .84));
            g.draw(line(s, .5, .45, .5, .72));
            g.fill(new Ellipse2D.Double(s * .45, s * .24, s * .1, s * .1));
        } else if ("calendar".equals(name)) {
            g.draw(rr(s, .1, .18, .8, .72));
            g.draw(line(s, .1, .4, .9, .4));
            g.draw(line(s, .32, .08, .32, .26));
            g.draw(line(s, .68, .08, .68, .26));
        } else if ("list".equals(name)) {
            for (double y : new double[] {.25, .5, .75}) {
                g.fill(new Ellipse2D.Double(s * .1, s * (y - .05), s * .1, s * .1));
                g.draw(line(s, .32, y, .9, y));
            }
        } else if ("check".equals(name)) {
            Path2D p = new Path2D.Double();
            p.moveTo(s * .16, s * .52);
            p.lineTo(s * .4, s * .76);
            p.lineTo(s * .84, s * .26);
            g.draw(p);
        } else if ("db".equals(name)) {
            g.draw(new Ellipse2D.Double(s * .15, s * .08, s * .7, s * .22));
            g.draw(line(s, .15, .19, .15, .8));
            g.draw(line(s, .85, .19, .85, .8));
            g.draw(new Arc2D.Double(s * .15, s * .69, s * .7, s * .22, 180, 180, Arc2D.OPEN));
            g.draw(new Arc2D.Double(s * .15, s * .42, s * .7, s * .22, 180, 180, Arc2D.OPEN));
        } else if ("rupee".equals(name)) {
            g.draw(line(s, .25, .15, .78, .15));
            g.draw(line(s, .25, .37, .78, .37));
            g.draw(new Arc2D.Double(s * .02, s * .15, s * .52, s * .44, 90, -180, Arc2D.OPEN));
            g.draw(line(s, .28, .59, .7, .9));
        }
    }

    private static RoundRectangle2D rr(double s, double x, double y, double w, double h) {
        return new RoundRectangle2D.Double(s * x, s * y, s * w, s * h, s * .12, s * .12);
    }

    private static Line2D line(double s, double x1, double y1, double x2, double y2) {
        return new Line2D.Double(s * x1, s * y1, s * x2, s * y2);
    }
}
