package irrigation;

public class DripIrrigation implements IrrigationStrategy {
    @Override
    public IrrigationDecision activate(double moisture, double temperature) {
        if (temperature <= 0) {
            return IrrigationDecision.frostHold("Drip suspended: hoses at risk of freezing");
        }
        return moisture < 40
                ? IrrigationDecision.active("Drip irrigation ACTIVATED (low flow, avoids wetting leaves)", 3)
                : IrrigationDecision.standby("Drip irrigation on standby, moisture sufficient");
    }

    @Override
    public String code() {
        return "DRIP";
    }

    @Override
    public String name() {
        return "Drip";
    }

    @Override
    public double efficiency() {
        return 0.9;
    }
}
