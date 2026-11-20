package com.pgms.gui;

import com.pgms.ds.DynamicArray;
import com.pgms.service.DueLine;
import com.pgms.service.RentService;
import com.pgms.service.RoomService;
import com.pgms.service.TenantService;
import com.pgms.util.Dates;
import com.pgms.util.Money;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

/** First page: four headline numbers, the bed map, two charts and who still owes. */
public class DashboardPanel extends JPanel implements Screen {

    private static final long serialVersionUID = 1L;

    private final RoomService rooms = new RoomService();
    private final TenantService tenants = new TenantService();
    private final RentService rent = new RentService();

    private final StatCard occupancy = new StatCard("Occupancy", "bed", 0);
    private final StatCard staying = new StatCard("Tenants", "users", 1);
    private final StatCard collected = new StatCard("Collected", "receipt", 2);
    private final StatCard pending = new StatCard("Pending", "rupee", 3);
    private final OccupancyChart bedMap = new OccupancyChart();
    private final DonutChart donut = new DonutChart();
    private final MonthBars bars = new MonthBars();
    private final DefaultTableModel owing = Ui.model("ID", "Tenant", "Room", "Pending", "Status");
    private final RoundPanel owingCard;

    public DashboardPanel() {
        super(new BorderLayout(0, 18));
        setOpaque(false);

        JPanel stats = new JPanel(new GridLayout(1, 4, 16, 0));
        stats.setOpaque(false);
        stats.add(occupancy);
        stats.add(staying);
        stats.add(collected);
        stats.add(pending);
        add(stats, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;

        g.gridx = 0;
        g.gridy = 0;
        g.gridheight = 2;
        g.weightx = 0.27;
        g.weighty = 1;
        g.insets = new Insets(0, 0, 0, 16);
        body.add(Ui.card("Beds in each room", "bed", Ui.clearScroll(bedMap)), g);

        JPanel charts = new JPanel(new GridLayout(1, 2, 16, 0));
        charts.setOpaque(false);
        charts.add(Ui.card("This month", "rupee", donut));
        charts.add(Ui.card("Last 6 months", "calendar", bars));
        g.gridx = 1;
        g.gridy = 0;
        g.gridheight = 1;
        g.weightx = 0.73;
        g.weighty = 0.52;
        g.insets = new Insets(0, 0, 16, 0);
        body.add(charts, g);

        JTable t = Ui.table(owing);
        Ui.widths(t, 56, 200, 70, 120, 130);
        owingCard = Ui.card("Still to pay this month", "list", Ui.scroll(t));
        g.gridy = 1;
        g.weighty = 0.48;
        g.insets = new Insets(0, 0, 0, 0);
        body.add(owingCard, g);
        add(body, BorderLayout.CENTER);
    }

    @Override
    public void refresh() {
        try {
            int[] b = rooms.bedSummary();
            int free = b[0] - b[1];
            occupancy.set(b[1] + " / " + b[0], free + " beds free", b[0] == 0 ? 0 : (double) b[1] / b[0]);
            staying.set(String.valueOf(tenants.activeTenants().size()), "staying now", -1);
            bedMap.setRooms(rooms.allRooms());

            String month = Dates.currentMonth();
            DynamicArray<DueLine> dues = rent.duesForMonth(month);
            double bill = 0;
            double paid = 0;
            double left = 0;
            int unpaid = 0;
            owing.setRowCount(0);
            for (DueLine l : dues) {
                bill += l.due();
                paid += l.paid();
                left += l.pending();
                if (l.pending() > 0) {
                    unpaid++;
                    owing.addRow(new Object[] {l.getTenant().getId(), l.getTenant().getName(),
                            l.getTenant().getRoomNo(), Money.inr(l.pending()), l.status()});
                }
            }
            collected.set(Money.inr(paid), "of " + Money.inr(bill) + " billed", bill == 0 ? 0 : paid / bill);
            pending.set(Money.inr(left), "from " + unpaid + " tenant" + (unpaid == 1 ? "" : "s"), -1);
            donut.setData(paid, left);

            String[] months = new String[6];
            double[] billed = new double[6];
            double[] got = new double[6];
            String m = month;
            for (int i = 5; i >= 0; i--) {
                months[i] = m;
                for (DueLine l : rent.duesForMonth(m)) {
                    billed[i] += l.due();
                    got[i] += l.paid();
                }
                m = previous(m);
            }
            bars.setData(months, billed, got);
        } catch (Exception e) {
            Ui.problem(this, e);
        }
    }

    static String previous(String m) {
        int y = Integer.parseInt(m.substring(0, 4));
        int mo = Integer.parseInt(m.substring(5)) - 1;
        if (mo == 0) {
            mo = 12;
            y--;
        }
        return String.format("%04d-%02d", y, mo);
    }
}
