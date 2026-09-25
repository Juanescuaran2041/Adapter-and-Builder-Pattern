# SmartIrrigation — Riego inteligente en una finca andina

## Caso de estudio

Una comunidad campesina a 3 800 m de altura quiere **automatizar el riego de sus parcelas**,
pero cada parcela tiene **un cultivo distinto**, **un método de riego distinto** y **un sensor
de humedad de marca distinta**

| Parcela (en la app) | Cultivo | Método de riego | Sensor (lo que entrega) |
|---|---|---|---|
| High Field    | Papa (Potato)     | Aspersión (Sprinkler) | Modbus → número crudo 0–1023 |
| South Terrace | Quinua (Quinoa)   | Goteo (Drip)          | Serial viejo → texto `HUM:41.2;TEMP:8.3;CHK:50` |
| East Slope    | Haba (Fava bean)  | Surcos (Furrow)       | Tensiómetro → centibares (al revés: más = más seco) |

Cada hora simulada el sistema lee la humedad, decide si riega o no y descuenta el agua
de un reservorio comunal que solo se llena una vez al día (06:00).


## Patrón 1: ADAPTER → los sensores

**Problema:** cada sensor entrega el dato en un formato distinto.

**Solución:** una interfaz común y un adaptador por sensor que traduce.

```
SoilMoistureSensor  (interfaz que el sistema entiende)
   └─ readSoilPercentage(): double   → siempre 0–100 %

ModbusSensorAdapter   → envuelve ModbusMoistureSensor  
LegacySerialAdapter   → envuelve LegacySerialSensor    
TensiometerAdapter    → envuelve AnalogTensiometer     
```

| Rol del patrón | Clase |
|---|---|
| Target (lo que el cliente usa) | `SoilMoistureSensor` |
| Adaptees (clases incompatibles, no se modifican) | `ModbusMoistureSensor`, `LegacySerialSensor`, `AnalogTensiometer` |
| Adapters (implementan el Target y **contienen** al Adaptee) | `ModbusSensorAdapter`, `LegacySerialAdapter`, `TensiometerAdapter` |
| Cliente | `IrrigationController` |



## Patrón 2: BRIDGE → cultivo ⟷ método de riego

**Problema:** hay 3 cultivos y 3 métodos de riego.

**Solución:** separar las dos cosas en dos jerarquías y unirlas con un bridge

```
     ABSTRACCIÓN                         IMPLEMENTACIÓN
        Crop  ──── irrigationStrategy ────►  IrrigationStrategy
         ├─ PotatoCrop                         ├─ DripIrrigation
         ├─ QuinoaCrop                         ├─ SprinklerIrrigation
         └─ FavaBeanCrop                       └─ FurrowIrrigation
```

| Rol del patrón | Clase |
|---|---|
| Abstracción | `Crop` |
| Abstracciones refinadas | `PotatoCrop`, `QuinoaCrop`, `FavaBeanCrop` |
| Implementador | `IrrigationStrategy` |
| Implementadores concretos | `DripIrrigation`, `SprinklerIrrigation`, `FurrowIrrigation` |
| Cliente | `IrrigationController` (solo habla con `Crop`, nunca con el método directamente) |

- El **cultivo** decide *cuánta agua necesita* (la quinua suma +5 por ser sensible a hongos; el haba pide más agua en floración).
- El **método** decide *cómo se riega* (umbral, litros por m², qué hacer si hay helada).


## Cómo ejecutarlo

1. Abrir la carpeta `SmartIrrigation` como proyecto.
2. Ejecutar `src/Main.java` (botón ▶ junto a `main`).
3. Se abren **dos interfaces** conectadas a la misma finca:
   - Una **ventana Swing** (escritorio).
   - Un **dashboard web** (HTML + Bootstrap): abre **http://localhost:8080** en el navegador.
