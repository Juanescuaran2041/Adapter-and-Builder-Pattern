# SmartIrrigation — Riego inteligente en una finca andina

## Caso de estudio (en una frase)

Una comunidad campesina a 3 800 m de altura quiere **automatizar el riego de sus parcelas**,
pero cada parcela tiene **un cultivo distinto**, **un método de riego distinto** y **un sensor
de humedad de marca distinta** (cada uno "habla" un formato diferente).

| Parcela (en la app) | Cultivo | Método de riego | Sensor (lo que entrega) |
|---|---|---|---|
| High Field    | Papa (Potato)     | Aspersión (Sprinkler) | Modbus → número crudo 0–1023 |
| South Terrace | Quinua (Quinoa)   | Goteo (Drip)          | Serial viejo → texto `HUM:41.2;TEMP:8.3;CHK:50` |
| East Slope    | Haba (Fava bean)  | Surcos (Furrow)       | Tensiómetro → centibares (al revés: más = más seco) |

Cada hora simulada el sistema lee la humedad, decide si riega o no y descuenta el agua
de un reservorio comunal que solo se llena una vez al día (06:00).

Reglas extra que lo hacen realista:
- **Helada (≤ 1 °C):** la aspersión riega para proteger las plantas del frío; goteo y surcos se apagan.
- **Etapa del cultivo:** en floración se riega antes y en maduración más tarde.
- **Agua limitada:** si el reservorio no alcanza, el riego se **deniega**.

> El código y las interfaces están en inglés; esta documentación está en español.

---

## Patrón 1: ADAPTER → los sensores

**Problema:** cada sensor entrega el dato en un formato distinto, pero el sistema
solo quiere preguntar *"¿cuánta humedad hay en %?"*.

**Solución:** una interfaz común y un adaptador por sensor que traduce.

```
SoilMoistureSensor  (interfaz que el sistema entiende)
   └─ readSoilPercentage(): double   → siempre 0–100 %

ModbusSensorAdapter   → envuelve ModbusMoistureSensor  (convierte 0–1023 a %, corrige calibración)
LegacySerialAdapter   → envuelve LegacySerialSensor    (parsea el texto y valida el checksum)
TensiometerAdapter    → envuelve AnalogTensiometer     (convierte centibares a %)
```

| Rol del patrón | Clase |
|---|---|
| Target (lo que el cliente usa) | `SoilMoistureSensor` |
| Adaptees (clases incompatibles, no se modifican) | `ModbusMoistureSensor`, `LegacySerialSensor`, `AnalogTensiometer` |
| Adapters (implementan el Target y **contienen** al Adaptee) | `ModbusSensorAdapter`, `LegacySerialAdapter`, `TensiometerAdapter` |
| Cliente | `IrrigationController` |

**Resultado:** `IrrigationController` llama `sensor.readSoilPercentage()` y **no sabe qué
sensor hay detrás**. Para agregar una marca nueva solo se crea otro adapter.

---

## Patrón 2: BRIDGE → cultivo ⟷ método de riego

**Problema:** hay 3 cultivos y 3 métodos de riego. Con herencia normal saldrían
3 × 3 = **9 clases** (`PapaConGoteo`, `PapaConAspersion`, `QuinuaConGoteo`...).

**Solución:** separar las dos cosas en dos jerarquías y unirlas con un "puente"
(un atributo): el cultivo **tiene** un método de riego.

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

```java
Crop potato = new PotatoCrop(new SprinklerIrrigation());
potato.evaluateIrrigation(moisture, temperature);    // papa + aspersión
potato.setIrrigationStrategy(new DripIrrigation());  // mismo cultivo, otro método, sin clases nuevas
```

**Resultado:** 3 + 3 = 6 clases en vez de 9, y cada lado crece por separado.
En la interfaz se ve en vivo: al cambiar el "Irrigation method" de una parcela se cambia el puente.

> Nota: la interfaz del implementador se llama `IrrigationStrategy`, pero el patrón es **Bridge**
> (no Strategy): lo que se separa son **dos jerarquías** (cultivos y métodos), no solo un algoritmo.

---

## Cómo ejecutarlo

Requisito: **JDK 17 o superior** (el proyecto está configurado con JDK 21). No usa librerías externas.

### Desde IntelliJ
1. Abrir la carpeta `SmartIrrigation` como proyecto.
2. Ejecutar `src/Main.java` (botón ▶ junto a `main`).
3. Se abren **dos interfaces** conectadas a la misma finca:
   - Una **ventana Swing** (escritorio).
   - Un **dashboard web** (HTML + Bootstrap): abre **http://localhost:8080** en el navegador.

### Desde la terminal (en la carpeta del proyecto)
```bash
javac -encoding UTF-8 -d out $(find src -name "*.java")     # Git Bash / Linux / Mac
java -cp out Main
```
En PowerShell:
```powershell
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp out Main
```

> Si ves **"Unexpected token"** o **"Cannot connect with the Java server"** en la web, es porque abriste
> `web/index.html` sin tener `Main` corriendo. Primero ejecuta `Main` y luego abre http://localhost:8080.

---

## Cómo probarlo (paso a paso)

Cada prueba se puede hacer en la ventana **Swing** o en la **web** (tienen los mismos botones).

### Prueba 1 — Adapter: tres sensores distintos, un solo formato
1. Mira la última línea de cada tarjeta de parcela: dice qué sensor real hay y qué adapter lo traduce
   (`ModbusSensorAdapter`, `LegacySerialAdapter`, `TensiometerAdapter`).
2. Pulsa **+1 hour** varias veces.
3. **Esperado:** las tres parcelas muestran la humedad en **%** (0–100), aunque un sensor
   entrega 0–1023, otro texto y otro centibares.

### Prueba 2 — Adapter: validación de tramas corruptas
1. Activa **Auto** y deja correr la simulación un rato (o pulsa **+1 day** varias veces).
2. En el **Event log**, filtra por **ERROR**.
3. **Esperado:** aparecen eventos de *South Terrace* tipo
   `Reading discarded: Corrupted serial frame: checksum mismatch.`. El `LegacySerialAdapter`
   detectó que la trama llegó dañada y la descartó en lugar de regar con un dato falso.

### Prueba 3 — Bridge: cambiar el método de riego en caliente
1. En **High Field** (papa), cambia *Irrigation method* de **Sprinkler** a **Drip**.
2. **Esperado:**
   - En el log aparece `Irrigation method changed to Drip`.
   - El mensaje de la tarjeta cambia a textos de goteo (`Drip irrigation ...`).
   - El cultivo sigue siendo papa: **no se creó ninguna clase `PapaConGoteo`**, solo se cambió el objeto del puente.

### Prueba 4 — Bridge: el mismo método se comporta distinto según el cultivo
1. Pon **Drip** en *High Field* (papa) y en *South Terrace* (quinua), ambas en la misma etapa.
2. Avanza unas horas.
3. **Esperado:** con humedades parecidas, la quinua riega **menos** que la papa porque `QuinoaCrop`
   suma +5 al valor (es sensible a hongos). Misma implementación, distinta abstracción.

### Prueba 5 — Heladas
1. Avanza hasta la noche (entre las 23:00 y las 05:00 suele bajar de 0 °C). Mira *Weather station*.
2. **Esperado** cuando la temperatura baja de 0 (aparece **FROST**):
   - Parcela con **Sprinkler** → estado **ANTI_FROST** (riega para proteger del frío y gasta agua).
   - Parcelas con **Drip** o **Furrow** → estado **FROST_HOLD** (no riegan para que no se congelen las mangueras o el agua).

### Prueba 6 — Etapa del cultivo
1. En *East Slope* (haba), cambia *Growth stage* a **Flowering**.
2. **Esperado:** empieza a regar con más humedad que antes (la floración pide más agua y el haba tiene +5 extra).
3. Cámbiala a **Maturation**: deja de regar aunque el suelo esté más seco (evita que se pudra).

### Prueba 7 — Agua racionada
1. Pon las tres parcelas en **Sprinkler** y en etapa **Flowering** (todas piden agua a la vez)
   y pulsa **+6 hours** hasta completar un día (4 veces).
2. **Esperado:** el reservorio se vacía y en el log (filtro **WARN**) aparecen eventos
   `... DENIED: not enough water in the reservoir`.
3. Pulsa **Extra water turn** o espera a las 06:00 (`Communal water turn`) y el riego vuelve a funcionar.

### Prueba 8 — Web y Swing están sincronizadas
1. Ejecuta `Main` sin argumentos.
2. Cambia algo en la ventana Swing (por ejemplo el método de una parcela).
3. **Esperado:** al recargar o avanzar la simulación en la web se ve el mismo cambio, y viceversa
   (la ventana Swing se refresca sola cada segundo).

---

## Estructura

```
src/
  sensors/     → patrón ADAPTER (target, adaptees y adapters)
  irrigation/  → patrón BRIDGE (cultivos, métodos de riego, etapa del cultivo)
  farm/        → IrrigationController (cliente de ambos patrones), parcelas, reservorio, log
  simulation/  → reloj y suelo simulados (reemplazan el hardware real)
  ui/          → interfaz de escritorio en Swing
  web/         → servidor HTTP del JDK + API JSON
  Main.java    → crea las parcelas, los cultivos y los sensores, y abre las interfaces
web/           → front en HTML + Bootstrap (index.html, app.js, styles.css)
```
