import farm.IrrigationController;
import farm.Parcel;
import farm.WaterReservoir;
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
import simulation.Soil;
import ui.SwingDashboard;
import web.WebServer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        FarmClock clock = new FarmClock();

        Soil highFieldSoil = new Soil(42);
        Soil southTerraceSoil = new Soil(38);
        Soil eastSlopeSoil = new Soil(45);

        //bridge: each crop receives the irrigation method it will use
        PotatoCrop potato = new PotatoCrop(new SprinklerIrrigation());
        QuinoaCrop quinoa = new QuinoaCrop(new DripIrrigation());
        quinoa.setStage(GrowthStage.FLOWERING);
        FavaBeanCrop favaBean = new FavaBeanCrop(new FurrowIrrigation());
        favaBean.setStage(GrowthStage.SOWING);

        //adapter: each sensor is different but all of them are used as SoilMoistureSensor
        ModbusSensorAdapter modbusSensor = new ModbusSensorAdapter(new ModbusMoistureSensor(1, highFieldSoil));
        LegacySerialAdapter serialSensor = new LegacySerialAdapter(new LegacySerialSensor("COM3", southTerraceSoil, clock));
        TensiometerAdapter tensiometer = new TensiometerAdapter(new AnalogTensiometer("T-07", eastSlopeSoil));

        List<Parcel> parcels = new ArrayList<>();
        parcels.add(new Parcel("P1", "High Field", 500, potato, modbusSensor, highFieldSoil));
        parcels.add(new Parcel("P2", "South Terrace", 350, quinoa, serialSensor, southTerraceSoil));
        parcels.add(new Parcel("P3", "East Slope", 250, favaBean, tensiometer, eastSlopeSoil));

        WaterReservoir reservoir = new WaterReservoir(30000, 22000);
        IrrigationController controller = new IrrigationController(clock, reservoir, parcels);

        //web page
        File webFolder = new File("web");
        if (!webFolder.exists()) {
            webFolder = new File("../web");
        }
        try {
            WebServer server = new WebServer(controller, webFolder);
            server.start(8080);
            System.out.println("Web page: http://localhost:8080");
        } catch (Exception e) {
            System.out.println("The web server could not start: " + e.getMessage());
        }

        //desktop window
        SwingDashboard.open(controller);
    }
}
