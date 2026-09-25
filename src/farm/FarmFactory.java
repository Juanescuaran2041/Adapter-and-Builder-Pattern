package farm;

import irrigation.DripIrrigation;
import irrigation.FavaBeanCrop;
import irrigation.FurrowIrrigation;
import irrigation.GrowthStage;
import irrigation.PotatoCrop;
import irrigation.QuinoaCrop;
import irrigation.SprinklerIrrigation;
import sensors.AnalogTensiometer;
import sensors.LegacySerialAdapter;
import sensors.LegacySerialSensor;
import sensors.ModbusMoistureSensor;
import sensors.ModbusSensorAdapter;
import sensors.TensiometerAdapter;
import simulation.FarmClock;
import simulation.SoilEnvironment;

import java.util.List;

// Builds the demo case: a highland community farm with three heterogeneous parcels
public final class FarmFactory {

    private FarmFactory() {
    }

    public static IrrigationController createAndeanFarm() {
        FarmClock clock = new FarmClock();

        SoilEnvironment highField = new SoilEnvironment(clock, 42);
        SoilEnvironment terrace = new SoilEnvironment(clock, 38);
        SoilEnvironment slope = new SoilEnvironment(clock, 45);

        PotatoCrop potato = new PotatoCrop(new SprinklerIrrigation());
        QuinoaCrop quinoa = new QuinoaCrop(new DripIrrigation());
        quinoa.setGrowthStage(GrowthStage.FLOWERING);
        FavaBeanCrop favaBean = new FavaBeanCrop(new FurrowIrrigation());
        favaBean.setGrowthStage(GrowthStage.SOWING);

        List<Parcel> parcels = List.of(
                new Parcel("P1", "High Field", 500, potato,
                        new ModbusSensorAdapter(new ModbusMoistureSensor(1, highField)), highField),
                new Parcel("P2", "South Terrace", 350, quinoa,
                        new LegacySerialAdapter(new LegacySerialSensor("COM3", terrace)), terrace),
                new Parcel("P3", "East Slope", 250, favaBean,
                        new TensiometerAdapter(new AnalogTensiometer("T-07", slope)), slope)
        );

        WaterReservoir reservoir = new WaterReservoir(30000, 18000, 22000);
        return new IrrigationController(clock, reservoir, parcels);
    }
}
