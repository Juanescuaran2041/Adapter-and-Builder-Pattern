package irrigation;

public class SprinklerIrrigation implements IrrigationStrategy {
    @Override
    public IrrigationResult activate(double moisture, double temperature) {
        //when there is frost the sprinkler waters the plants so the ice doesnt burn them
        if (temperature <= 1) {
            return new IrrigationResult("ANTI_FROST", "Sprinkler ANTI-FROST mode activated", 2);
        }
        if (moisture < 35) {
            return new IrrigationResult("ACTIVE", "Sprinkler irrigation ACTIVATED (wide coverage)", 5);
        }
        return new IrrigationResult("STANDBY", "Sprinkler irrigation on standby, moisture sufficient", 0);
    }

    @Override
    public String getName() {
        return "Sprinkler";
    }

    @Override
    public double getEfficiency() {
        return 0.75;
    }
}
