const STATUS = {
    ACTIVE: {label: 'Regando', cls: 'text-bg-primary', icon: 'bi-droplet-fill'},
    STANDBY: {label: 'En espera', cls: 'text-bg-secondary', icon: 'bi-pause-circle'},
    FROST_PROTECTION: {label: 'Antihelada', cls: 'text-bg-info', icon: 'bi-snow'},
    FROST_HOLD: {label: 'Suspendido por helada', cls: 'text-bg-info', icon: 'bi-snow2'},
    DENIED: {label: 'Denegado', cls: 'text-bg-warning', icon: 'bi-exclamation-triangle'},
    SENSOR_ERROR: {label: 'Error de sensor', cls: 'text-bg-danger', icon: 'bi-x-octagon'}
};

const LOG_LEVEL = {
    WATER: {label: 'Riego', cls: 'text-bg-primary'},
    FROST: {label: 'Helada', cls: 'text-bg-info'},
    WARN: {label: 'Denegado', cls: 'text-bg-warning'},
    ERROR: {label: 'Sensor', cls: 'text-bg-danger'},
    INFO: {label: 'Info', cls: 'text-bg-light border'}
};

const CROP_ICON = {Papa: '🥔', Quinua: '🌾', Haba: '🫘'};

let state = null;
let autoTimer = null;
let busy = false;
const builtCards = new Set();

const $ = (id) => document.getElementById(id);
const fmt = (n, d = 1) => n === null || n === undefined ? '—' : Number(n).toLocaleString('es', {maximumFractionDigits: d, minimumFractionDigits: d});
const pad = (n) => String(n).padStart(2, '0');
const esc = (s) => String(s ?? '').replace(/[&<>"']/g, c => ({'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'}[c]));

// If the page is opened from the file system or from IntelliJ's built-in preview,
// the API still lives in the Java server on port 8080.
const API_BASE = location.protocol === 'file:' || location.port !== '8080' ? 'http://localhost:8080' : '';

async function api(path, method = 'GET') {
    let res;
    try {
        res = await fetch(API_BASE + path, {method});
    } catch (e) {
        throw new Error('No se pudo conectar con el servidor Java. Ejecuta Main.java y abre http://localhost:8080');
    }
    const text = await res.text();
    let body;
    try {
        body = JSON.parse(text);
    } catch (e) {
        throw new Error(`Respuesta inesperada del servidor (${res.status}). Abre la página desde http://localhost:8080`);
    }
    if (!res.ok) throw new Error(body.error || res.statusText);
    return body;
}

async function run(path, method = 'POST') {
    if (busy) return;
    busy = true;
    try {
        render(await api(path, method));
        setConnection(true);
    } catch (e) {
        showError(e.message);
        setConnection(false);
    } finally {
        busy = false;
    }
}

function setConnection(ok) {
    $('connection').innerHTML = ok
        ? '<i class="bi bi-circle-fill text-success"></i> Conectado'
        : '<i class="bi bi-circle-fill text-danger"></i> Sin conexión';
}

function showError(msg) {
    $('errorText').textContent = msg;
    bootstrap.Toast.getOrCreateInstance($('errorToast')).show();
}

function render(s) {
    state = s;
    renderHeader(s);
    s.parcels.forEach(p => renderParcel(p, s));
    renderLog();
}

function renderHeader(s) {
    const {clock, reservoir} = s;
    $('clock').textContent = `Día ${clock.day} · ${pad(clock.hour)}:00`;
    $('daytime').innerHTML = clock.daytime
        ? '<i class="bi bi-sun text-warning"></i> Día'
        : '<i class="bi bi-moon-stars text-primary"></i> Noche';

    $('ambient').textContent = `${fmt(clock.temperature)} °C`;
    const t = clock.temperature;
    $('frostBadge').innerHTML = t <= 0
        ? '<span class="badge text-bg-info"><i class="bi bi-snow"></i> Helada en curso</span>'
        : t <= 3
            ? '<span class="badge text-bg-warning"><i class="bi bi-exclamation-triangle"></i> Riesgo de helada</span>'
            : '<span class="badge text-bg-success"><i class="bi bi-check-circle"></i> Sin riesgo</span>';

    const pct = reservoir.liters / reservoir.capacity * 100;
    $('reservoirText').textContent = `${fmt(reservoir.liters, 0)} / ${fmt(reservoir.capacity, 0)} L`;
    const bar = $('reservoirBar');
    bar.style.width = `${pct}%`;
    bar.className = 'progress-bar ' + (pct < 15 ? 'bg-danger' : pct < 35 ? 'bg-warning' : 'bg-primary');
    $('reservoirNote').textContent = `Turno comunal diario a las 06:00 (+${fmt(reservoir.turnVolume, 0)} L)`;

    const total = s.parcels.reduce((acc, p) => acc + p.waterUsed, 0);
    $('totalWater').textContent = `${fmt(total, 0)} L`;
}

function buildCard(p, s) {
    const col = document.createElement('div');
    col.className = 'col-md-6 col-xl-4';
    col.innerHTML = `
      <div class="card parcel-card shadow-sm h-100">
        <div class="card-header d-flex justify-content-between align-items-center">
          <div>
            <span class="fs-5">${CROP_ICON[p.crop] || '🌱'}</span>
            <strong>${esc(p.name)}</strong>
            <span class="text-body-secondary small">· ${esc(p.crop)} · ${fmt(p.area, 0)} m²</span>
          </div>
          <span class="badge" data-f="status"></span>
        </div>
        <div class="card-body">
          <div class="d-flex justify-content-between small mb-1">
            <span>Humedad del suelo</span><strong data-f="moisture"></strong>
          </div>
          <div class="progress mb-2" role="progressbar">
            <div class="progress-bar" data-f="moistureBar"></div>
          </div>
          <svg class="sparkline mb-2" viewBox="0 0 200 60" preserveAspectRatio="none" data-f="spark"></svg>

          <div class="row g-2 small mb-3">
            <div class="col-6"><i class="bi bi-thermometer-half"></i> <span data-f="temp"></span></div>
            <div class="col-6 text-end"><i class="bi bi-bucket"></i> <span data-f="water"></span></div>
          </div>

          <div class="row g-2 mb-3">
            <div class="col-6">
              <label class="form-label small mb-1">Método de riego</label>
              <select class="form-select form-select-sm" data-f="strategy">
                ${s.strategies.map(st => `<option value="${st.code}">${esc(st.name)} (${Math.round(st.efficiency * 100)} %)</option>`).join('')}
              </select>
            </div>
            <div class="col-6">
              <label class="form-label small mb-1">Etapa fenológica</label>
              <select class="form-select form-select-sm" data-f="stage">
                ${s.stages.map(st => `<option value="${st.code}">${esc(st.label)}</option>`).join('')}
              </select>
            </div>
          </div>

          <div class="alert alert-light border small py-2 status-msg mb-2" data-f="message"></div>
          <div class="small text-body-secondary"><i class="bi bi-cpu"></i> <span data-f="device"></span></div>
        </div>
      </div>`;
    $('parcels').appendChild(col);
    col.id = `parcel-${p.id}`;

    col.querySelector('[data-f=strategy]').addEventListener('change', e =>
        run(`/api/parcels/${p.id}/strategy?code=${encodeURIComponent(e.target.value)}`));
    col.querySelector('[data-f=stage]').addEventListener('change', e =>
        run(`/api/parcels/${p.id}/stage?code=${encodeURIComponent(e.target.value)}`));
    builtCards.add(p.id);
}

function renderParcel(p, s) {
    if (!builtCards.has(p.id)) buildCard(p, s);
    const card = $(`parcel-${p.id}`);
    const f = (name) => card.querySelector(`[data-f=${name}]`);

    const st = STATUS[p.status] || STATUS.STANDBY;
    f('status').className = `badge ${st.cls}`;
    f('status').innerHTML = `<i class="bi ${st.icon}"></i> ${st.label}`;

    f('moisture').textContent = p.moisture === null ? '—' : `${fmt(p.moisture)} %`;
    const bar = f('moistureBar');
    const m = p.moisture ?? 0;
    bar.style.width = `${m}%`;
    bar.className = 'progress-bar ' + (m < 30 ? 'bg-danger' : m < 40 ? 'bg-warning' : 'bg-success');

    f('temp').textContent = p.temperature === null ? '—'
        : `${fmt(p.temperature)} °C ${p.hasTemperatureProbe ? '(sonda)' : '(estación)'}`;
    f('water').textContent = `${fmt(p.waterUsed, 0)} L usados`;
    f('message').textContent = p.message || '—';
    f('device').textContent = p.device;

    const strategy = f('strategy');
    if (document.activeElement !== strategy) strategy.value = p.strategy;
    const stage = f('stage');
    if (document.activeElement !== stage) stage.value = p.stage;

    drawSparkline(f('spark'), p.history);
}

function drawSparkline(svg, values) {
    if (!values.length) {
        svg.innerHTML = '';
        return;
    }
    const w = 200, h = 60, n = Math.max(values.length - 1, 1);
    const pts = values.map((v, i) => [i / n * w, h - v / 100 * h]);
    const line = pts.map(([x, y]) => `${x.toFixed(1)},${y.toFixed(1)}`).join(' ');
    const y40 = h - 0.4 * h;
    svg.innerHTML = `
      <line class="threshold" x1="0" x2="${w}" y1="${y40}" y2="${y40}"></line>
      <polygon class="area" points="0,${h} ${line} ${w},${h}"></polygon>
      <polyline class="line" points="${line}"></polyline>
      <title>Humedad de las últimas ${values.length} horas (línea roja: 40 %)</title>`;
}

function renderLog() {
    if (!state) return;
    const filter = $('logFilter').value;
    const rows = state.log
        .filter(e => !filter || e.level === filter)
        .map(e => {
            const lvl = LOG_LEVEL[e.level] || LOG_LEVEL.INFO;
            return `<tr>
              <td class="text-nowrap small">D${e.day} ${pad(e.hour)}:00</td>
              <td class="text-nowrap small">${esc(e.parcel)}</td>
              <td><span class="badge ${lvl.cls}">${lvl.label}</span></td>
              <td class="small">${esc(e.message)}</td>
            </tr>`;
        });
    $('log').innerHTML = rows.join('') ||
        '<tr><td colspan="4" class="text-center text-body-secondary py-3">Sin eventos todavía. Avanza la simulación.</td></tr>';
}

document.querySelectorAll('[data-hours]').forEach(btn =>
    btn.addEventListener('click', () => run(`/api/tick?hours=${btn.dataset.hours}`)));

$('btnTurn').addEventListener('click', () => run('/api/reservoir/turn'));
$('logFilter').addEventListener('change', renderLog);

$('autoPlay').addEventListener('change', e => {
    clearInterval(autoTimer);
    if (e.target.checked) autoTimer = setInterval(() => run('/api/tick?hours=1'), 1500);
});

run('/api/state', 'GET');
