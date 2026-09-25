package ui;

import farm.IrrigationController;
import farm.Parcel;
import irrigation.GrowthStage;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import java.awt.Color;
import java.awt.GridLayout;

public class ParcelPanel extends JPanel {

    private final Parcel parcel;

    private final JLabel statusLabel = new JLabel();
    private final JProgressBar moistureBar = new JProgressBar(0, 100);
    private final JLabel waterLabel = new JLabel();
    private final JComboBox<String> irrigationCombo = new JComboBox<>(new String[]{"Drip", "Sprinkler", "Furrow"});
    private final JComboBox<GrowthStage> stageCombo = new JComboBox<>(GrowthStage.values());
    private final JTextArea messageArea = new JTextArea(3, 20);
    private final JLabel deviceLabel = new JLabel();

    //to know if the combo changed because of the user or because of refresh()
    private boolean refreshing = false;

    public ParcelPanel(IrrigationController controller, Parcel parcel) {
        this.parcel = parcel;

        setLayout(new GridLayout(0, 1, 4, 4));
        setBorder(BorderFactory.createTitledBorder(parcel.getName() + " - " + parcel.getCrop().getName()
                + " (" + (int) parcel.getArea() + " m2)"));

        statusLabel.setOpaque(true);
        moistureBar.setStringPainted(true);
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setFont(deviceLabel.getFont());

        irrigationCombo.addActionListener(e -> {
            if (!refreshing) {
                controller.changeIrrigation(parcel.getId(), (String) irrigationCombo.getSelectedItem());
                refresh();
            }
        });
        stageCombo.addActionListener(e -> {
            if (!refreshing) {
                controller.changeStage(parcel.getId(), stageCombo.getSelectedItem().toString());
                refresh();
            }
        });

        add(statusLabel);
        add(moistureBar);
        add(waterLabel);
        add(new JLabel("Irrigation method:"));
        add(irrigationCombo);
        add(new JLabel("Growth stage:"));
        add(stageCombo);
        add(messageArea);
        add(deviceLabel);
    }

    public void refresh() {
        refreshing = true;

        String status = parcel.getResult().getStatus();
        statusLabel.setText(" " + status);
        if (status.equals("ACTIVE")) {
            statusLabel.setBackground(new Color(120, 170, 255));
        } else if (status.equals("ANTI_FROST") || status.equals("FROST_HOLD")) {
            statusLabel.setBackground(new Color(150, 230, 250));
        } else if (status.equals("DENIED")) {
            statusLabel.setBackground(new Color(255, 210, 100));
        } else if (status.equals("ERROR")) {
            statusLabel.setBackground(new Color(255, 130, 130));
        } else {
            statusLabel.setBackground(new Color(210, 210, 210));
        }

        int moisture = (int) parcel.getMoisture();
        moistureBar.setValue(moisture);
        moistureBar.setString("Moisture: " + moisture + " %");

        waterLabel.setText("Water used: " + (int) parcel.getWaterUsed() + " L");
        irrigationCombo.setSelectedItem(parcel.getCrop().getIrrigationName());
        stageCombo.setSelectedItem(parcel.getCrop().getStage());
        messageArea.setText(parcel.getResult().getMessage());
        deviceLabel.setText(parcel.getSensor().getDeviceName());

        refreshing = false;
    }
}
