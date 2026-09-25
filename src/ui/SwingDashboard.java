package ui;

import farm.IrrigationController;
import farm.LogEntry;
import farm.Parcel;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

public class SwingDashboard extends JFrame {

    private final IrrigationController controller;
    private final List<ParcelPanel> parcelPanels = new ArrayList<>();

    private final JLabel timeLabel = new JLabel();
    private final JLabel temperatureLabel = new JLabel();
    private final JProgressBar reservoirBar = new JProgressBar(0, 100);
    private final JTextArea logArea = new JTextArea(10, 80);

    public SwingDashboard(IrrigationController controller) {
        this.controller = controller;

        setTitle("SmartIrrigation");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        //top: time, temperature, reservoir and buttons
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        reservoirBar.setStringPainted(true);
        top.add(timeLabel);
        top.add(temperatureLabel);
        top.add(new JLabel("Reservoir:"));
        top.add(reservoirBar);

        JButton oneHour = new JButton("+1 hour");
        JButton sixHours = new JButton("+6 hours");
        JButton oneDay = new JButton("+1 day");
        JButton extraTurn = new JButton("Extra water turn");
        JCheckBox auto = new JCheckBox("Auto");

        oneHour.addActionListener(e -> advance(1));
        sixHours.addActionListener(e -> advance(6));
        oneDay.addActionListener(e -> advance(24));
        extraTurn.addActionListener(e -> {
            controller.extraWaterTurn();
            refresh();
        });

        Timer autoTimer = new Timer(1500, e -> advance(1));
        auto.addActionListener(e -> {
            if (auto.isSelected()) {
                autoTimer.start();
            } else {
                autoTimer.stop();
            }
        });

        top.add(oneHour);
        top.add(sixHours);
        top.add(oneDay);
        top.add(extraTurn);
        top.add(auto);
        add(top, BorderLayout.NORTH);

        //center: one panel for each parcel
        JPanel center = new JPanel(new GridLayout(1, 3, 10, 10));
        for (Parcel parcel : controller.getParcels()) {
            ParcelPanel panel = new ParcelPanel(controller, parcel);
            parcelPanels.add(panel);
            center.add(panel);
        }
        add(center, BorderLayout.CENTER);

        //bottom: event log
        logArea.setEditable(false);
        add(new JScrollPane(logArea), BorderLayout.SOUTH);

        //refresh every second, so the changes made from the web page also appear here
        new Timer(1000, e -> refresh()).start();

        refresh();
        setSize(1100, 650);
        setLocationRelativeTo(null);
    }

    public static void open(IrrigationController controller) {
        SwingUtilities.invokeLater(() -> new SwingDashboard(controller).setVisible(true));
    }

    private void advance(int hours) {
        controller.advance(hours);
        refresh();
    }

    private void refresh() {
        int hour = controller.getClock().getHour();
        String dayOrNight = controller.getClock().isDay() ? "day" : "night";
        timeLabel.setText("Day " + controller.getClock().getDay() + " - " + String.format("%02d", hour) + ":00 (" + dayOrNight + ")");

        double temperature = controller.getClock().getTemperature();
        String text = "Temperature: " + String.format("%.1f", temperature) + " C";
        if (temperature <= 0) {
            text = text + " - FROST";
        }
        temperatureLabel.setText(text);

        double liters = controller.getReservoir().getLiters();
        double capacity = controller.getReservoir().getCapacity();
        reservoirBar.setValue((int) (liters / capacity * 100));
        reservoirBar.setString((int) liters + " / " + (int) capacity + " L");

        for (ParcelPanel panel : parcelPanels) {
            panel.refresh();
        }

        StringBuilder log = new StringBuilder();
        for (LogEntry entry : controller.getLog()) {
            log.append(entry.getTime()).append("  ")
                    .append(entry.getType()).append("  ")
                    .append(entry.getParcel()).append(": ")
                    .append(entry.getMessage()).append("\n");
        }
        if (!logArea.getText().equals(log.toString())) {
            logArea.setText(log.toString());
            logArea.setCaretPosition(0);
        }
    }
}
