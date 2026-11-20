package com.pgms.gui;

import com.pgms.service.SettingsService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/** SETTINGS: the names shown in the sidebar and the window title, all editable. */
public class SettingsPanel extends JPanel implements Screen {

    private static final long serialVersionUID = 1L;

    private final SettingsService settings = new SettingsService();
    private final PgField pgName = new PgField("", 28);
    private final PgField tagline = new PgField("", 28);
    private final PgField owner = new PgField("", 28);
    private final PgField ownerLine = new PgField("", 28);
    private final Runnable onSaved;

    public SettingsPanel(Runnable onSaved) {
        super(new BorderLayout(0, 16));
        this.onSaved = onSaved;
        setOpaque(false);
        pgName.setName("PG name");
        tagline.setName("Tagline");
        owner.setName("Owner / manager name");
        ownerLine.setName("Footer line");

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        row(form, 0, "PG name", pgName, "Shown at the top of the sidebar and in the window title");
        row(form, 1, "Tagline", tagline, "The small line under the PG name");
        row(form, 2, "Owner / manager name", owner, "Shown in the card at the bottom of the sidebar");
        row(form, 3, "Footer line", ownerLine, "A second line in that card, e.g. your class or address");

        JButton save = Ui.primary("Save names", "check");
        JButton undo = Ui.secondary("Undo changes", "move");
        save.addActionListener(e -> save());
        undo.addActionListener(e -> refresh());

        RoundPanel card = Ui.card("Names in the app", "gear", form);
        card.add(Ui.row(save, undo), BorderLayout.SOUTH);
        JPanel holder = new JPanel(new BorderLayout());
        holder.setOpaque(false);
        holder.add(card, BorderLayout.NORTH);
        add(holder, BorderLayout.CENTER);
    }

    private static void row(JPanel form, int y, String label, PgField field, String help) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = y * 2;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(10, 0, 0, 24);
        form.add(Ui.bold(label), g);
        g = new GridBagConstraints();
        g.gridx = 1;
        g.gridy = y * 2;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        g.insets = new Insets(10, 0, 0, 0);
        form.add(field, g);
        g = new GridBagConstraints();
        g.gridx = 1;
        g.gridy = y * 2 + 1;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(4, 4, 4, 0);
        form.add(Ui.label(help), g);
    }

    @Override
    public void refresh() {
        try {
            pgName.setText(settings.get(SettingsService.PG_NAME));
            tagline.setText(settings.get(SettingsService.PG_TAGLINE));
            owner.setText(settings.get(SettingsService.OWNER_NAME));
            ownerLine.setText(settings.get(SettingsService.OWNER_LINE));
        } catch (Exception e) {
            Ui.problem(this, e);
        }
    }

    /** Used by tests and by the Save button. */
    void save() {
        try {
            settings.save(pgName.getText(), tagline.getText(), owner.getText(), ownerLine.getText());
            onSaved.run();
            Toast.show(this, "Names saved.");
        } catch (Exception e) {
            Ui.problem(this, e);
        }
    }

    void setFields(String a, String b, String c, String d) {
        pgName.setText(a);
        tagline.setText(b);
        owner.setText(c);
        ownerLine.setText(d);
    }
}
