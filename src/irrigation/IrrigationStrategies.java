package irrigation;

import java.util.List;

// Catalog of available implementors, used to swap them at runtime
public final class IrrigationStrategies {

    private IrrigationStrategies() {
    }

    public static List<IrrigationStrategy> all() {
        return List.of(new DripIrrigation(), new SprinklerIrrigation(), new FurrowIrrigation());
    }

    public static IrrigationStrategy byCode(String code) {
        return switch (code) {
            case "DRIP" -> new DripIrrigation();
            case "SPRINKLER" -> new SprinklerIrrigation();
            case "FURROW" -> new FurrowIrrigation();
            default -> throw new IllegalArgumentException("Unknown irrigation strategy: " + code);
        };
    }
}
