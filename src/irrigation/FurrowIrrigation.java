package irrigation;

// Traditional Andean gravity irrigation through furrows (no pump needed)
public class FurrowIrrigation implements IrrigationStrategy {
    @Override
    public IrrigationDecision activate(double moisture, double temperature) {
        if (temperature <= 0) {
            return IrrigationDecision.frostHold("Surcos cerrados: el agua estancada se congelaría sobre las raíces");
        }
        return moisture < 30
                ? IrrigationDecision.active("Riego por surcos ABIERTO (gravedad, alto consumo)", 8)
                : IrrigationDecision.standby("Surcos cerrados, humedad suficiente");
    }

    @Override
    public String code() {
        return "FURROW";
    }

    @Override
    public String name() {
        return "Surcos (gravedad)";
    }

    @Override
    public double efficiency() {
        return 0.55;
    }
}
