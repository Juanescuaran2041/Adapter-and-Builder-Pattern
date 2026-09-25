package ui;

import javax.swing.JComponent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.util.List;

// Small line chart with the moisture of the last hours (0-100 %)
class MoistureChart extends JComponent {

    private static final Color LINE = new Color(13, 110, 253);
    private static final Color AREA = new Color(13, 110, 253, 35);
    private static final Color THRESHOLD = new Color(220, 53, 69);

    private List<Double> values = List.of();

    MoistureChart() {
        setPreferredSize(new Dimension(260, 60));
        setToolTipText("Moisture over the last hours (red line: 40 %)");
    }

    void setValues(List<Double> values) {
        this.values = values;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();

        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, w, h);

        int thresholdY = (int) (h - 0.4 * h);
        g2.setColor(THRESHOLD);
        g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[]{4, 3}, 0));
        g2.drawLine(0, thresholdY, w, thresholdY);

        if (values.size() >= 2) {
            int n = values.size();
            int[] xs = new int[n];
            int[] ys = new int[n];
            for (int i = 0; i < n; i++) {
                xs[i] = (int) Math.round(i / (double) (n - 1) * (w - 1));
                ys[i] = (int) Math.round(h - values.get(i) / 100.0 * h);
            }

            Polygon area = new Polygon();
            area.addPoint(0, h);
            for (int i = 0; i < n; i++) {
                area.addPoint(xs[i], ys[i]);
            }
            area.addPoint(w - 1, h);
            g2.setColor(AREA);
            g2.fillPolygon(area);

            g2.setColor(LINE);
            g2.setStroke(new BasicStroke(2));
            g2.drawPolyline(xs, ys, n);
        }
        g2.dispose();
    }
}
