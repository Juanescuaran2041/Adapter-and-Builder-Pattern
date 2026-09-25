package irrigation;

public class DripIrrigation implements IrrigationStrategy {
    @Override
    public IrrigationDecision activate(double moisture, double temperature) {
        if (temperature <= 0) {
            return IrrigationDecision.frostHold("Goteo suspendido: riesgo de congelamiento en las mangueras");
        }
        return moisture < 40
                ? IrrigationDecision.active("Riego por goteo ACTIVADO (bajo caudal, no moja las hojas)", 3)
                : IrrigationDecision.standby("Goteo en espera, humedad suficiente");
    }

    @Override
    public String code() {
        return "DRIP";
    }

    @Override
    public String name() {
        return "Goteo";
    }

    @Override
    public double efficiency() {
        return 0.9;
    }
}
