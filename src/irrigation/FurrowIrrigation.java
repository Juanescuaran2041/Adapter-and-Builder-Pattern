package irrigation;

// Traditional Andean gravity irrigation through furrows (no pump needed)
public class FurrowIrrigation implements IrrigationStrategy {
    @Override
    public IrrigationDecision activate(double moisture, double temperature) {
        if (temperature <= 0) {
            return IrrigationDecision.frostHold("Furrows closed: standing water would freeze over the roots");
        }
        return moisture < 30
                ? IrrigationDecision.active("Furrow irrigation OPEN (gravity, high consumption)", 8)
                : IrrigationDecision.standby("Furrows closed, moisture sufficient");
    }

    @Override
    public String code() {
        return "FURROW";
    }

    @Override
    public String name() {
        return "Furrow (gravity)";
    }

    @Override
    public double efficiency() {
        return 0.55;
    }
}
