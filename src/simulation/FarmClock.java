package simulation;

import java.util.Random;

// Simulated clock of an Andean highland farm (~3800 m a.s.l.).
// Each tick is one hour; nights can drop below 0 °C (frost risk).
public class FarmClock {

    private static final int START_HOUR = 6;

    private int totalHours = START_HOUR;
    private final Random random = new Random();
    private double dailyOffset = 0;

    public void advance() {
        totalHours++;
        if (hour() == 0) {
            // every day is a bit colder or warmer than the previous one
            dailyOffset = random.nextDouble() * 6 - 3;
        }
    }

    public int day() {
        return totalHours / 24 + 1;
    }

    public int hour() {
        return totalHours % 24;
    }

    // Daily cycle: warmest around 14:00, coldest around 02:00
    public double ambientTemperature() {
        double phase = (hour() - 14) / 24.0 * 2 * Math.PI;
        return 7 + 10 * Math.cos(phase) + dailyOffset;
    }

    public boolean isDaytime() {
        return hour() >= 6 && hour() < 18;
    }
}
