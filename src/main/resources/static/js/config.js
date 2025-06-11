let currentParams = [];

async function fetchActiveDevices() {
    const res = await fetch('/api/devices/active');
    const devices = await res.json();
    const select = document.getElementById('deviceSelect');
    select.innerHTML = '';
    devices.forEach(id => {
        const opt = document.createElement('option');
        opt.value = id;
        opt.textContent = id;
        select.appendChild(opt);
    });
}

async function fetchConfiguration() {
    const button = document.getElementById('fetchButton');
    const deviceId = document.getElementById('deviceSelect').value;

    button.disabled = true;
    button.classList.add('btn-disabled');

    try {
        const response = await fetch(`/esp32/configurable-params?deviceId=${deviceId}`);
        const data = await response.json();
        currentParams = data;
        renderConfigurationTable(data);
    } catch (error) {
        console.error('Error al obtener la configuración:', error);
        // Aquí puedes mostrar un mensaje de error al usuario si lo deseas
    } finally {
        button.disabled = false;
        button.classList.remove('btn-disabled');
    }
}

function showAddForm() {
    const ssid = prompt("Introduce el SSID:");
    const mac = prompt("Introduce la dirección MAC:");

    if (ssid && mac) {
        addWhitelistItem(ssid.trim(), mac.trim());
    }
}

async function addWhitelistItem(ssid, mac) {
    const token = document.querySelector('meta[name="_csrf"]').content;
    const headerName = document.querySelector('meta[name="_csrf_header"]').content;
    const response = await fetch('/api/whitelist/add', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [headerName]: token
        },
        body: JSON.stringify({ ssid, mac })
    });

    if (response.ok) {
        fetchWhitelist(); // recarga la tabla
    } else {
        alert("Error al añadir el elemento a la whitelist.");
    }
}

async function toggleWhitelist(mac, whitelisted) {
    const token = document.querySelector('meta[name="_csrf"]').content;
    const headerName = document.querySelector('meta[name="_csrf_header"]').content;
    const response = await fetch('/api/whitelist/mac/update', {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            [headerName]: token
        },
        body: JSON.stringify({ mac, whitelisted })
    });

    if (!response.ok) {
        const msg = await response.text();
        alert("Error: " + msg);
    }
}


async function deleteWhitelistItem(index) {
    const confirmed = confirm("¿Estás seguro de que quieres eliminar este elemento?");
    if (!confirmed) return;

    const token = document.querySelector('meta[name="_csrf"]').content;
    const headerName = document.querySelector('meta[name="_csrf_header"]').content;

    const { ssid, mac } = evilTwinList[index];

    const response = await fetch('/api/whitelist/remove', {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json',
            [headerName]: token
        },
        body: JSON.stringify({ ssid, mac })
    });

    if (response.ok) {
        fetchWhitelist();
    } else {
        alert("Error al eliminar el elemento.");
    }
}

async function sendWhitelist() {
    const token = document.querySelector('meta[name="_csrf"]').content;
    const headerName = document.querySelector('meta[name="_csrf_header"]').content;
    const response = await fetch('/api/whitelist/send', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [headerName]: token
        },
    });

    if (response.ok) {
        alert("Lista enviada a los dispositivos.");
    } else {
        alert("Error al enviar la lista.");
    }
}


function renderConfigurationTable(data) {
    const configContainer = document.getElementById('configContainer');
    configContainer.innerHTML = '';

    if (!data || data.length === 0) {
        configContainer.innerHTML = `<p>No se recibió configuración. ¿El dispositivo está respondiendo?</p>`;
        return;
    }

    const table = document.createElement('table');
    table.innerHTML = `
        <thead>
            <tr>
                <th>Nombre</th>
                <th>Valor</th>
                <th>Descripción</th>
            </tr>
        </thead>
        <tbody></tbody>
    `;
    const tbody = table.querySelector('tbody');

    data.forEach((param, index) => {
        const row = document.createElement('tr');

        // Determinamos si el valor es booleano
        const isBoolean = typeof param.defaultValue === 'boolean';

        // Construcción del campo: <select> para booleanos, <input> para el resto
        let fieldHtml;
        if (isBoolean) {
            const selectedTrue = param.defaultValue === true ? 'selected' : '';
            const selectedFalse = param.defaultValue === false ? 'selected' : '';
            fieldHtml = `
                <select onchange="updateParamValue(${index}, this.value)">
                  <option value="true"  ${selectedTrue}>Sí</option>
                  <option value="false" ${selectedFalse}>No</option>
                </select>
            `;
        } else {
            // Escapamos el valor para evitar problemas con comillas
            const safeValue = param.defaultValue != null
                ? String(param.defaultValue).replace(/"/g, '&quot;')
                : '';
            fieldHtml = `<input 
                            type="text" 
                            value="${safeValue}" 
                            onchange="updateParamValue(${index}, this.value)" 
                          />`;
        }

        row.innerHTML = `
            <td>${param.name}</td>
            <td>${fieldHtml}</td>
            <td>${param.description || ''}</td>
        `;
        tbody.appendChild(row);
    });

    configContainer.appendChild(table);

    const saveButton = document.createElement('button');
    saveButton.textContent = "Guardar configuración";
    saveButton.className = "save-button";
    saveButton.onclick = submitConfiguration;
    configContainer.appendChild(saveButton);
}

function updateParamValue(index, newValue) {
    currentParams[index].defaultValue = newValue;
}

async function submitConfiguration() {
    const deviceId = document.getElementById('deviceSelect').value;
    const token = document.querySelector('meta[name="_csrf"]').content;
    const headerName = document.querySelector('meta[name="_csrf_header"]').content;

    const saveButton = document.querySelector(".save-button");
    saveButton.disabled = true;
    saveButton.textContent = "Guardando...";

    const response = await fetch(`/api/devices/save-config/${deviceId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [headerName]: token
        },
        body: JSON.stringify(currentParams),
    });

    saveButton.disabled = false;
    saveButton.textContent = "Guardar configuración";

    if (response.ok) {
        alert("Configuración guardada y enviada al dispositivo.");
    } else {
        alert("Error al guardar y enviar la configuración.");
    }
}

window.onload = fetchActiveDevices;

document.getElementById('tab-config').addEventListener('click', () => {
    document.getElementById('tab-config').classList.add('active');
    document.getElementById('configSection').classList.remove('hidden');

    document.getElementById('tab-dashboard').classList.remove('active');
    document.getElementById('tab-dashboard-desglosado').classList.remove('active');
    document.getElementById('dashboardSection').classList.add('hidden');
    document.getElementById('dashboardDetailSection').classList.add('hidden');
    document.getElementById('tab-whitelist').classList.remove('active');
    document.getElementById('whitelistSection').classList.add('hidden');
    document.getElementById('tab-esp-telemetria').classList.remove('active');
    document.getElementById('esp_telemetry').classList.add('hidden');

});
document.getElementById('tab-dashboard').addEventListener('click', () => {
    document.getElementById('tab-dashboard').classList.add('active');
    document.getElementById('dashboardSection').classList.remove('hidden');

    document.getElementById('tab-dashboard-desglosado').classList.remove('active');
    document.getElementById('tab-config').classList.remove('active');
    document.getElementById('dashboardDetailSection').classList.add('hidden');
    document.getElementById('configSection').classList.add('hidden');
    document.getElementById('tab-whitelist').classList.remove('active');
    document.getElementById('whitelistSection').classList.add('hidden');
    document.getElementById('tab-esp-telemetria').classList.remove('active');
    document.getElementById('esp_telemetry').classList.add('hidden');
});

document.getElementById('tab-dashboard-desglosado').addEventListener('click', () => {
    document.getElementById('tab-dashboard-desglosado').classList.add('active');
    document.getElementById('dashboardDetailSection').classList.remove('hidden');

    document.getElementById('tab-dashboard').classList.remove('active');
    document.getElementById('tab-config').classList.remove('active');
    document.getElementById('dashboardSection').classList.add('hidden');
    document.getElementById('configSection').classList.add('hidden');
    document.getElementById('tab-whitelist').classList.remove('active');
    document.getElementById('whitelistSection').classList.add('hidden');
    document.getElementById('tab-esp-telemetria').classList.remove('active');
    document.getElementById('esp_telemetry').classList.add('hidden');
});

document.getElementById('tab-esp-telemetria').addEventListener('click', () => {
    document.getElementById('tab-esp-telemetria').classList.add('active');
    document.getElementById('esp_telemetry').classList.remove('hidden');

    document.getElementById('tab-dashboard').classList.remove('active');
    document.getElementById('tab-config').classList.remove('active');
    document.getElementById('dashboardSection').classList.add('hidden');
    document.getElementById('configSection').classList.add('hidden');
    document.getElementById('tab-whitelist').classList.remove('active');
    document.getElementById('whitelistSection').classList.add('hidden');
    document.getElementById('tab-dashboard-desglosado').classList.remove('active');
    document.getElementById('dashboardDetailSection').classList.add('hidden');
});

let evilTwinList = [];

document.getElementById('tab-whitelist').addEventListener('click', () => {

    document.getElementById('tab-whitelist').classList.add('active');
    document.getElementById('whitelistSection').classList.remove('hidden');
    document.querySelectorAll('li').forEach(li => li.classList.remove('hidden'));
    document.querySelectorAll('section').forEach(sec => sec.classList.add('active'));

    document.getElementById('tab-config').classList.remove('active');
    document.getElementById('tab-dashboard').classList.remove('active');
    document.getElementById('tab-dashboard-desglosado').classList.remove('active');
    document.getElementById('dashboardSection').classList.add('hidden');
    document.getElementById('dashboardDetailSection').classList.add('hidden');
    document.getElementById('configSection').classList.add('hidden');
    document.getElementById('tab-esp-telemetria').classList.remove('active');
    document.getElementById('esp_telemetry').classList.add('hidden');



    fetchWhitelist();
});

async function fetchWhitelist() {
    const res = await fetch('/api/whitelist/registeredtwins');
    evilTwinList = await res.json();
    renderWhitelistTable();
}

function renderWhitelistTable() {
    const container = document.getElementById('whitelistContainer');
    container.innerHTML = '';

    const table = document.createElement('table');
    table.innerHTML = `
      <thead>
        <tr><th>SSID</th><th>MAC</th><th>Whitelisted</th><th>Acciones</th></tr>
      </thead>
      <tbody></tbody>
    `;

    const tbody = table.querySelector('tbody');

    evilTwinList.forEach((item, index) => {
        const row = document.createElement('tr');

        row.innerHTML = `
          <td>${item.ssid}</td>
          <td>${item.mac}</td>
          <td>
            <input type="checkbox" ${item.whitelisted ? 'checked' : ''} 
              onchange="toggleWhitelist('${item.mac}', this.checked)">
          </td>
          <td>
            <button onclick="deleteWhitelistItem(${index})">Eliminar</button>
          </td>
        `;
        tbody.appendChild(row);
    });

    container.appendChild(table);

    // Botones adicionales
    const addButton = document.createElement('button');
    addButton.textContent = 'Añadir manualmente';
    addButton.onclick = showAddForm;

    const sendButton = document.createElement('button');
    sendButton.textContent = 'Enviar lista a todos los dispositivos';
    sendButton.onclick = sendWhitelist;

    container.appendChild(addButton);
    container.appendChild(sendButton);
}
