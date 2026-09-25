package simulation;

import java.util.Random;

public class FarmClock {

    private int day = 1;
    private int hour = 6;
    private double dayVariation = 0;
    private final Random random = new Random();

    //temperatures of a normal day in the highlands, from 00:00 to 23:00
    private final double[] temperatures = {
            -1, -2, -3, -3, -2, -1, 1, 4, 7, 10, 13, 15,
            16, 17, 17, 16, 14, 11, 8, 6, 4, 2, 1, 0
    };

    public void nextHour() {
        hour++;
        if (hour == 24) {
            hour = 0;
            day++;
            dayVariation = random.nextDouble() * 6 - 3;
        }
    }

    public double getTemperature() {
        return temperatures[hour] + dayVariation;
    }

    public boolean isDay() {
        return hour >= 6 && hour < 18;
    }

    public int getDay() {
        return day;
    }

    public int getHour() {
        return hour;
    }
}
