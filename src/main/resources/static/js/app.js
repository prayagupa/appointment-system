const tenantSelect = document.getElementById("tenantSelect");
const providerSelect = document.getElementById("providerSelect");
const bookForm = document.getElementById("bookForm");
const bookDate = document.getElementById("bookDate");
const slotSelect = document.getElementById("slotSelect");
const durationInput = document.getElementById("duration");
const bookBtn = document.getElementById("bookBtn");
const bookMessage = document.getElementById("bookMessage");
const appointmentList = document.getElementById("appointmentList");
const refreshBtn = document.getElementById("refreshBtn");
const toast = document.getElementById("toast");

let slots = [];

function showToast(message, isError = false) {
  toast.textContent = message;
  toast.classList.toggle("error", isError);
  toast.hidden = false;
  setTimeout(() => {
    toast.hidden = true;
  }, 3500);
}

function tenantHeaders() {
  const tenantId = tenantSelect.value;
  if (!tenantId) {
    throw new Error("Select a tenant");
  }
  return { "X-Tenant-Id": tenantId };
}

async function api(path, options = {}) {
  const headers = {
    "Content-Type": "application/json",
    ...(options.skipTenant ? {} : tenantHeaders()),
    ...options.headers,
  };
  const response = await fetch(path, { ...options, headers });
  if (!response.ok) {
    const body = await response.json().catch(() => ({}));
    throw new Error(body.message || `Request failed (${response.status})`);
  }
  if (response.status === 204) {
    return null;
  }
  return response.json();
}

async function loadTenants() {
  const tenants = await api("/api/tenants", { skipTenant: true });
  tenantSelect.innerHTML =
    '<option value="">Select tenant…</option>' +
    tenants
      .map((t) => `<option value="${t.id}">${t.name} (${t.type})</option>`)
      .join("");
}

async function loadProviders() {
  const tenantId = tenantSelect.value;
  providerSelect.disabled = !tenantId;
  bookBtn.disabled = true;
  refreshBtn.disabled = true;
  if (!tenantId) {
    providerSelect.innerHTML = '<option value="">Select tenant first</option>';
    return;
  }
  const providers = await api("/api/providers");
  providerSelect.innerHTML =
    '<option value="">Select provider…</option>' +
    providers
      .map((p) => `<option value="${p.id}">${p.displayName}${p.specialty ? ` — ${p.specialty}` : ""}</option>`)
      .join("");
  providerSelect.disabled = false;
}

async function loadSlots() {
  slotSelect.innerHTML = '<option value="">Loading…</option>';
  slotSelect.disabled = true;
  const providerId = providerSelect.value;
  const date = bookDate.value;
  if (!providerId || !date) {
    slotSelect.innerHTML = '<option value="">Select provider and date</option>';
    return;
  }
  const from = new Date(`${date}T00:00:00.000Z`).toISOString();
  const to = new Date(`${date}T23:59:59.999Z`).toISOString();
  const slotMinutes = Number(durationInput.value) || 30;
  slots = await api(
    `/api/availability?providerId=${providerId}&from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}&slotMinutes=${slotMinutes}`
  );
  if (slots.length === 0) {
    slotSelect.innerHTML = '<option value="">No slots available</option>';
    return;
  }
  slotSelect.innerHTML = slots
    .map((s, i) => {
      const start = new Date(s.startTime);
      const end = new Date(s.endTime);
      const label = `${start.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })} – ${end.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })} UTC`;
      return `<option value="${i}">${label}</option>`;
    })
    .join("");
  slotSelect.disabled = false;
  bookBtn.disabled = false;
}

async function loadAppointments() {
  const providerId = providerSelect.value;
  if (!tenantSelect.value || !providerId) {
    return;
  }
  refreshBtn.disabled = false;
  bookBtn.disabled = false;
  const appointments = await api(`/api/appointments?providerId=${providerId}`);
  if (appointments.length === 0) {
    appointmentList.innerHTML = '<li class="empty-state">No appointments yet.</li>';
    return;
  }
  appointmentList.innerHTML = appointments
    .map((a) => {
      const start = new Date(a.startTime);
      const status = a.status.toLowerCase();
      const cancelBtn =
        status === "booked"
          ? `<button type="button" class="btn btn-danger" data-cancel="${a.id}">Cancel</button>`
          : "";
      return `<li class="appointment-card ${status}">
        <div class="appointment-meta">
          <strong>${a.patientRef}</strong>
          <span>${start.toLocaleString()} · ${a.durationMinutes} min</span>
          <span class="status-pill ${status}">${a.status}</span>
        </div>
        ${cancelBtn}
      </li>`;
    })
    .join("");
  appointmentList.querySelectorAll("[data-cancel]").forEach((btn) => {
    btn.addEventListener("click", () => cancelAppointment(btn.dataset.cancel));
  });
}

async function cancelAppointment(id) {
  try {
    await api(`/api/appointments/${id}/cancel`, { method: "POST" });
    showToast("Appointment cancelled");
    await loadAppointments();
  } catch (err) {
    showToast(err.message, true);
  }
}

tenantSelect.addEventListener("change", async () => {
  try {
    await loadProviders();
    appointmentList.innerHTML =
      '<li class="empty-state">Select a provider to view appointments.</li>';
    slotSelect.innerHTML = '<option value="">Select a date first</option>';
    slotSelect.disabled = true;
  } catch (err) {
    showToast(err.message, true);
  }
});

providerSelect.addEventListener("change", async () => {
  try {
    await loadSlots();
    await loadAppointments();
  } catch (err) {
    showToast(err.message, true);
  }
});

bookDate.addEventListener("change", () => loadSlots().catch((e) => showToast(e.message, true)));
durationInput.addEventListener("change", () => loadSlots().catch((e) => showToast(e.message, true)));
refreshBtn.addEventListener("click", () => loadAppointments().catch((e) => showToast(e.message, true)));

bookForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  bookMessage.textContent = "";
  bookMessage.className = "message";
  try {
    const slot = slots[Number(slotSelect.value)];
    if (!slot) {
      throw new Error("Select an available slot");
    }
    const body = {
      providerId: providerSelect.value,
      patientRef: document.getElementById("patientRef").value.trim(),
      startTime: slot.startTime,
      durationMinutes: Number(durationInput.value),
    };
    await api("/api/appointments", { method: "POST", body: JSON.stringify(body) });
    bookMessage.textContent = "Appointment booked successfully.";
    bookMessage.className = "message success";
    showToast("Booked!");
    bookForm.reset();
    durationInput.value = "30";
    await loadSlots();
    await loadAppointments();
  } catch (err) {
    bookMessage.textContent = err.message;
    bookMessage.className = "message error";
    showToast(err.message, true);
  }
});

const today = new Date().toISOString().slice(0, 10);
bookDate.min = today;

loadTenants().catch((err) => showToast(err.message, true));
