package ui;

import farm.IrrigationController;
import farm.Parcel;
import irrigation.GrowthStage;
import irrigation.IrrigationDecision;
import irrigation.IrrigationStrategies;
import irrigation.IrrigationStrategy;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Locale;

// Card that shows one parcel and lets the user swap its irrigation method (bridge) and growth stage
class ParcelPanel extends JPanel {

    private final IrrigationController controller;
    private final Parcel parcel;
    private final Runnable onChange;

    private final JLabel statusLabel = new JLabel();
    private final JProgressBar moistureBar = new JProgressBar(0, 100);
    private final MoistureChart chart = new MoistureChart();
    private final JLabel temperatureLabel = new JLabel();
    private final JLabel waterLabel = new JLabel();
    private final JComboBox<IrrigationStrategy> strategyCombo;
    private final JComboBox<GrowthStage> stageCombo = new JComboBox<>(GrowthStage.values());
    private final JTextArea messageArea = new JTextArea(3, 20);
    private final JLabel deviceLabel = new JLabel();

    private boolean updating = false;

    ParcelPanel(IrrigationController controller, Parcel parcel, Runnable onChange) {
        this.controller = controller;
        this.parcel = parcel;
        this.onChange = onChange;
        this.strategyCombo = new JComboBox<>(IrrigationStrategies.all().toArray(new IrrigationStrategy[0]));

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(String.format(Locale.US, "%s — %s (%.0f m²)",
                        parcel.getName(), parcel.getCrop().name(), parcel.getAreaM2())),
                BorderFactory.createEmptyBorder(4, 8, 8, 8)));

        statusLabel.setOpaque(true);
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));

        moistureBar.setStringPainted(true);

        strategyCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                IrrigationStrategy s = (IrrigationStrategy) value;
                String text = s == null ? "" : String.format(Locale.US, "%s (%.0f %%)", s.name(), s.efficiency() * 100);
                return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
            }
        });
        stageCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                String text = value == null ? "" : ((GrowthStage) value).label();
                return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
            }
        });

        strategyCombo.addActionListener(e -> {
            IrrigationStrategy selected = (IrrigationStrategy) strategyCombo.getSelectedItem();
            if (!updating && selected != null) {
                controller.changeStrategy(parcel.getId(), selected.code());
                onChange.run();
            }
        });
        stageCombo.addActionListener(e -> {
            GrowthStage selected = (GrowthStage) stageCombo.getSelectedItem();
            if (!updating && selected != null) {
                controller.changeStage(parcel.getId(), selected.name());
                onChange.run();
            }
        });

        messageArea.setEditable(false);
        messageArea.setFont(deviceLabel.getFont());
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setBackground(new Color(248, 249, 250));
        messageArea.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        deviceLabel.setFont(deviceLabel.getFont().deriveFont(Font.ITALIC, 11f));

        JPanel info = new JPanel(new GridLayout(1, 2));
        info.setOpaque(false);
        info.add(temperatureLabel);
        info.add(waterLabel);

        JPanel selectors = new JPanel(new GridLayout(2, 2, 6, 2));
        selectors.setOpaque(false);
        selectors.add(new JLabel("Irrigation method"));
        selectors.add(new JLabel("Growth stage"));
        selectors.add(strategyCombo);
        selectors.add(stageCombo);

        for (Component c : new Component[]{statusLabel, moistureBar, chart, info, selectors, messageArea, deviceLabel}) {
            ((javax.swing.JComponent) c).setAlignmentX(LEFT_ALIGNMENT);
            add(c);
            add(javax.swing.Box.createVerticalStrut(6));
        }
    }

    void refresh() {
        updating = true;
        try {
            IrrigationDecision decision = parcel.getLastDecision();
            StatusStyle style = StatusStyle.of(decision == null ? null : decision.status());
            statusLabel.setText(style.label);
            statusLabel.setBackground(style.background);
            statusLabel.setForeground(style.foreground);

            Double moisture = parcel.getLastMoisture();
            int value = moisture == null ? 0 : (int) Math.round(moisture);
            moistureBar.setValue(value);
            moistureBar.setString(moisture == null ? "—" : String.format(Locale.US, "Soil moisture %.1f %%", moisture));
            moistureBar.setForeground(value < 30 ? new Color(220, 53, 69) : value < 40 ? new Color(255, 193, 7) : new Color(25, 135, 84));

            chart.setValues(parcel.getMoistureHistory());

            Double temperature = parcel.getLastTemperature();
            temperatureLabel.setText(temperature == null ? "Temp: —" : String.format(Locale.US, "Temp: %.1f °C %s",
                    temperature, parcel.getSensor().readTemperature().isPresent() ? "(probe)" : "(station)"));
            waterLabel.setText(String.format(Locale.US, "Water used: %,.0f L", parcel.getWaterUsedLiters()));

            selectStrategy(parcel.getCrop().irrigationMethodCode());
            stageCombo.setSelectedItem(parcel.getCrop().getGrowthStage());

            messageArea.setText(decision == null ? "—" : decision.message());
            deviceLabel.setText(parcel.getSensor().deviceInfo());
        } finally {
            updating = false;
        }
    }

    private void selectStrategy(String code) {
        for (int i = 0; i < strategyCombo.getItemCount(); i++) {
            if (strategyCombo.getItemAt(i).code().equals(code)) {
                strategyCombo.setSelectedIndex(i);
                return;
            }
        }
    }

    private enum StatusStyle {
        ACTIVE("Irrigating", new Color(13, 110, 253), Color.WHITE),
        STANDBY("Standby", new Color(108, 117, 125), Color.WHITE),
        FROST_PROTECTION("Anti-frost", new Color(13, 202, 240), Color.BLACK),
        FROST_HOLD("Frost hold", new Color(13, 202, 240), Color.BLACK),
        DENIED("Denied", new Color(255, 193, 7), Color.BLACK),
        SENSOR_ERROR("Sensor error", new Color(220, 53, 69), Color.WHITE);

        final String label;
        final Color background;
        final Color foreground;

        StatusStyle(String label, Color background, Color foreground) {
            this.label = label;
            this.background = background;
            this.foreground = foreground;
        }

        static StatusStyle of(IrrigationDecision.Status status) {
            return status == null ? STANDBY : valueOf(status.name());
        }
    }
}
