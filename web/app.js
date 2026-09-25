// if the page is not opened from the java server, the api is still on port 8080
let server = '';
if (location.port !== '8080') {
    server = 'http://localhost:8080';
}

let data = null;
let autoTimer = null;
let cardsCreated = false;

const statusColors = {
    ACTIVE: 'bg-primary',
    STANDBY: 'bg-secondary',
    ANTI_FROST: 'bg-info text-dark',
    FROST_HOLD: 'bg-info text-dark',
    DENIED: 'bg-warning text-dark',
    ERROR: 'bg-danger'
};

const typeColors = {
    WATER: 'bg-primary',
    FROST: 'bg-info text-dark',
    WARN: 'bg-warning text-dark',
    ERROR: 'bg-danger',
    INFO: 'bg-secondary'
};

function call(url) {
    fetch(server + url)
        .then(response => response.json())
        .then(result => {
            if (result.error) {
                showError(result.error);
                return;
            }
            document.getElementById('error').classList.add('d-none');
            document.getElementById('connection').textContent = 'Connected';
            data = result;
            showData();
        })
        .catch(() => {
            document.getElementById('connection').textContent = 'Disconnected';
            showError('Cannot connect with the Java server. Run Main.java and open http://localhost:8080');
        });
}

function showError(message) {
    const error = document.getElementById('error');
    error.textContent = message;
    error.classList.remove('d-none');
}

function showData() {
    let dayText = data.isDay ? 'day' : 'night';
    document.getElementById('time').textContent = 'Day ' + data.day + ' - ' + String(data.hour).padStart(2, '0') + ':00 (' + dayText + ')';

    document.getElementById('temperature').textContent = data.temperature.toFixed(1) + ' °C';
    let frost = document.getElementById('frost');
    if (data.temperature <= 0) {
        frost.innerHTML = '<span class="badge bg-info text-dark">FROST</span>';
    } else if (data.temperature <= 3) {
        frost.innerHTML = '<span class="badge bg-warning text-dark">Frost risk</span>';
    } else {
        frost.innerHTML = '<span class="badge bg-success">No frost</span>';
    }

    let percentage = data.reservoir / data.capacity * 100;
    document.getElementById('reservoirText').textContent = Math.round(data.reservoir) + ' / ' + Math.round(data.capacity) + ' L';
    document.getElementById('reservoirBar').style.width = percentage + '%';

    let total = 0;
    for (let parcel of data.parcels) {
        total = total + parcel.waterUsed;
    }
    document.getElementById('totalWater').textContent = Math.round(total) + ' L';

    if (!cardsCreated) {
        createCards();
        cardsCreated = true;
    }
    for (let parcel of data.parcels) {
        updateCard(parcel);
    }

    showLog();
}

function createCards() {
    let html = '';
    for (let parcel of data.parcels) {
        html += `
        <div class="col-md-4">
            <div class="card h-100">
                <div class="card-header">
                    <b>${parcel.name}</b> - ${parcel.crop} (${Math.round(parcel.area)} m2)
                    <span class="badge float-end" id="status-${parcel.id}"></span>
                </div>
                <div class="card-body">
                    <div class="progress mb-2" style="height: 20px">
                        <div class="progress-bar" id="moisture-${parcel.id}"></div>
                    </div>
                    <p class="small mb-2" id="water-${parcel.id}"></p>

                    <label class="small">Irrigation method</label>
                    <select class="form-select form-select-sm mb-2" id="irrigation-${parcel.id}"
                            onchange="call('/api/irrigation?parcel=${parcel.id}&method=' + this.value)">
                        <option value="Drip">Drip (90%)</option>
                        <option value="Sprinkler">Sprinkler (75%)</option>
                        <option value="Furrow">Furrow (55%)</option>
                    </select>

                    <label class="small">Growth stage</label>
                    <select class="form-select form-select-sm mb-2" id="stage-${parcel.id}"
                            onchange="call('/api/stage?parcel=${parcel.id}&stage=' + this.value)">
                        <option value="SOWING">Sowing</option>
                        <option value="VEGETATIVE">Vegetative</option>
                        <option value="FLOWERING">Flowering</option>
                        <option value="MATURATION">Maturation</option>
                    </select>

                    <div class="alert alert-light border small message mb-2" id="message-${parcel.id}"></div>
                    <p class="small text-muted mb-0"><i class="bi bi-cpu"></i> ${parcel.device}</p>
                </div>
            </div>
        </div>`;
    }
    document.getElementById('parcels').innerHTML = html;
}

function updateCard(parcel) {
    let status = document.getElementById('status-' + parcel.id);
    status.textContent = parcel.status;
    status.className = 'badge float-end ' + statusColors[parcel.status];

    let bar = document.getElementById('moisture-' + parcel.id);
    bar.style.width = parcel.moisture + '%';
    bar.textContent = parcel.moisture.toFixed(1) + ' %';
    if (parcel.moisture < 30) {
        bar.className = 'progress-bar bg-danger';
    } else if (parcel.moisture < 40) {
        bar.className = 'progress-bar bg-warning';
    } else {
        bar.className = 'progress-bar bg-success';
    }

    document.getElementById('water-' + parcel.id).textContent = 'Water used: ' + Math.round(parcel.waterUsed) + ' L';
    document.getElementById('message-' + parcel.id).textContent = parcel.message;

    //dont change the select while the user is choosing an option
    let irrigation = document.getElementById('irrigation-' + parcel.id);
    if (document.activeElement !== irrigation) {
        irrigation.value = parcel.irrigation;
    }
    let stage = document.getElementById('stage-' + parcel.id);
    if (document.activeElement !== stage) {
        stage.value = parcel.stage;
    }
}

function showLog() {
    if (data == null) {
        return;
    }
    let filter = document.getElementById('logFilter').value;
    let rows = '';
    for (let entry of data.log) {
        if (filter === '' || entry.type === filter) {
            rows += `<tr>
                <td class="small text-nowrap">${entry.time}</td>
                <td class="small text-nowrap">${entry.parcel}</td>
                <td><span class="badge ${typeColors[entry.type]}">${entry.type}</span></td>
                <td class="small">${entry.message}</td>
            </tr>`;
        }
    }
    if (rows === '') {
        rows = '<tr><td colspan="4" class="text-center text-muted">No events yet</td></tr>';
    }
    document.getElementById('log').innerHTML = rows;
}

function toggleAuto() {
    if (document.getElementById('auto').checked) {
        autoTimer = setInterval(() => call('/api/advance?hours=1'), 1500);
    } else {
        clearInterval(autoTimer);
    }
}

call('/api/state');
