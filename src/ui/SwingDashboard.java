package ui;

import farm.IrrigationController;
import farm.LogEntry;
import farm.Parcel;
import farm.WaterReservoir;
import simulation.FarmClock;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Desktop alternative to the web dashboard. It uses the same IrrigationController,
// so both views show (and modify) the same farm.
public class SwingDashboard extends JFrame {

    private static final String[] LOG_FILTERS = {"All", "WATER", "FROST", "WARN", "ERROR", "INFO"};

    private final IrrigationController controller;
    private final List<ParcelPanel> parcelPanels = new ArrayList<>();

    private final JLabel clockLabel = new JLabel();
    private final JLabel temperatureLabel = new JLabel();
    private final JLabel frostLabel = new JLabel();
    private final JProgressBar reservoirBar = new JProgressBar(0, 100);
    private final JLabel totalWaterLabel = new JLabel();
    private final JComboBox<String> logFilter = new JComboBox<>(LOG_FILTERS);
    private final DefaultTableModel logModel = new DefaultTableModel(new String[]{"Time", "Source", "Type", "Event"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final Timer autoTimer;

    public SwingDashboard(IrrigationController controller) {
        super("SmartIrrigation · Andean Farm");
        this.controller = controller;
        this.autoTimer = new Timer(1500, e -> advance(1));

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildParcels(), BorderLayout.CENTER);
        add(buildLog(), BorderLayout.SOUTH);

        // keeps the window in sync with changes made from the web dashboard
        new Timer(1000, e -> refresh()).start();

        refresh();
        setMinimumSize(new Dimension(1000, 700));
        pack();
        setLocationRelativeTo(null);
    }

    public static void open(IrrigationController controller) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // the default look and feel is fine
            }
            new SwingDashboard(controller).setVisible(true);
        });
    }

    private JPanel buildHeader() {
        Font big = clockLabel.getFont().deriveFont(Font.BOLD, 18f);
        clockLabel.setFont(big);
        temperatureLabel.setFont(big);
        totalWaterLabel.setFont(big);
        reservoirBar.setStringPainted(true);
        reservoirBar.setPreferredSize(new Dimension(220, 22));

        JPanel indicators = new JPanel(new GridLayout(1, 4, 8, 0));
        indicators.add(card("Farm clock", clockLabel));
        JPanel weather = new JPanel(new GridLayout(2, 1));
        weather.add(temperatureLabel);
        weather.add(frostLabel);
        indicators.add(card("Weather station", weather));

        JButton turnButton = new JButton("Extra water turn");
        turnButton.addActionListener(e -> {
            controller.emergencyTurn();
            refresh();
        });
        JPanel reservoir = new JPanel(new BorderLayout(0, 4));
        reservoir.add(reservoirBar, BorderLayout.CENTER);
        reservoir.add(turnButton, BorderLayout.SOUTH);
        indicators.add(card("Community reservoir", reservoir));
        indicators.add(card("Water used", totalWaterLabel));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.add(new JLabel("Simulation:"));
        controls.add(stepButton("+1 hour", 1));
        controls.add(stepButton("+6 hours", 6));
        controls.add(stepButton("+1 day", 24));
        JCheckBox auto = new JCheckBox("Auto (1 h every 1.5 s)");
        auto.addActionListener(e -> {
            if (auto.isSelected()) {
                autoTimer.start();
            } else {
                autoTimer.stop();
            }
        });
        controls.add(auto);

        JPanel header = new JPanel(new BorderLayout(0, 6));
        header.add(indicators, BorderLayout.CENTER);
        header.add(controls, BorderLayout.SOUTH);
        return header;
    }

    private JPanel buildParcels() {
        JPanel panel = new JPanel(new GridLayout(1, 0, 8, 0));
        for (Parcel parcel : controller.getParcels()) {
            ParcelPanel parcelPanel = new ParcelPanel(controller, parcel, this::refresh);
            parcelPanels.add(parcelPanel);
            panel.add(parcelPanel);
        }
        return panel;
    }

    private JPanel buildLog() {
        JTable table = new JTable(logModel);
        table.getColumnModel().getColumn(0).setMaxWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(110);
        table.getColumnModel().getColumn(1).setMaxWidth(140);
        table.getColumnModel().getColumn(2).setMaxWidth(70);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(900, 180));

        logFilter.addActionListener(e -> refreshLog());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Event log — filter:"));
        top.add(logFilter);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(top, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel card(String title, java.awt.Component content) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(title), BorderFactory.createEmptyBorder(2, 6, 6, 6)));
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JButton stepButton(String text, int hours) {
        JButton button = new JButton(text);
        button.addActionListener(e -> advance(hours));
        return button;
    }

    private void advance(int hours) {
        controller.tick(hours);
        refresh();
    }

    private void refresh() {
        synchronized (controller) {
            FarmClock clock = controller.getClock();
            clockLabel.setText(String.format("Day %d · %02d:00 %s", clock.day(), clock.hour(), clock.isDaytime() ? "☀" : "☾"));

            double t = clock.ambientTemperature();
            temperatureLabel.setText(String.format(Locale.US, "%.1f °C", t));
            if (t <= 0) {
                frostLabel.setText("Frost now");
                frostLabel.setForeground(new Color(13, 110, 253));
            } else if (t <= 3) {
                frostLabel.setText("Frost risk");
                frostLabel.setForeground(new Color(200, 120, 0));
            } else {
                frostLabel.setText("No frost risk");
                frostLabel.setForeground(new Color(25, 135, 84));
            }

            WaterReservoir reservoir = controller.getReservoir();
            int pct = (int) Math.round(reservoir.getLiters() / reservoir.getCapacity() * 100);
            reservoirBar.setValue(pct);
            reservoirBar.setString(String.format(Locale.US, "%,.0f / %,.0f L", reservoir.getLiters(), reservoir.getCapacity()));
            reservoirBar.setForeground(pct < 15 ? new Color(220, 53, 69) : pct < 35 ? new Color(255, 193, 7) : new Color(13, 110, 253));

            double total = controller.getParcels().stream().mapToDouble(Parcel::getWaterUsedLiters).sum();
            totalWaterLabel.setText(String.format(Locale.US, "%,.0f L", total));

            parcelPanels.forEach(ParcelPanel::refresh);
            refreshLog();
        }
    }

    private void refreshLog() {
        String filter = (String) logFilter.getSelectedItem();
        logModel.setRowCount(0);
        for (LogEntry entry : controller.getLog()) {
            if ("All".equals(filter) || entry.level().equals(filter)) {
                logModel.addRow(new Object[]{
                        String.format("D%d %02d:00", entry.day(), entry.hour()),
                        entry.parcel(), entry.level(), entry.message()});
            }
        }
    }
}
