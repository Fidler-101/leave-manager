// ─── Mini Calendar ────────────────────────────────────────────────────────────
const MONTHS = ["January","February","March","April","May","June",
                "July","August","September","October","November","December"];

let calYear, calMonth;

function initCalendar() {
    const now = new Date();
    calYear  = now.getFullYear();
    calMonth = now.getMonth();
    renderCalendar();
}

function prevMonth() {
    if (calMonth === 0) { calMonth = 11; calYear--; }
    else calMonth--;
    renderCalendar();
}

function nextMonth() {
    if (calMonth === 11) { calMonth = 0; calYear++; }
    else calMonth++;
    renderCalendar();
}

function renderCalendar() {
    const label = document.getElementById('calMonthLabel');
    const grid  = document.getElementById('calGrid');
    if (!label || !grid) return;

    label.textContent = MONTHS[calMonth] + ' ' + calYear;

    const firstDay    = new Date(calYear, calMonth, 1).getDay();
    const daysInMonth = new Date(calYear, calMonth + 1, 0).getDate();

    const today = new Date();
    const isCurrentMonth = calYear === today.getFullYear() && calMonth === today.getMonth();

    grid.innerHTML = '';

    // Empty cells
    for (let i = 0; i < firstDay; i++) {
        const cell = document.createElement('div');
        cell.className = 'cal-cell cal-empty';
        grid.appendChild(cell);
    }

    // Day cells
    for (let d = 1; d <= daysInMonth; d++) {
        const cell   = document.createElement('div');
        const dayOfWeek = new Date(calYear, calMonth, d).getDay();
        const isWeekend = dayOfWeek === 0 || dayOfWeek === 6;
        const isTodayDay = isCurrentMonth && d === today.getDate();

        cell.className = 'cal-cell';
        cell.textContent = d;

        if (isTodayDay)    cell.classList.add('cal-today');
        else if (isWeekend) cell.classList.add('cal-weekend');

        grid.appendChild(cell);
    }
}

document.addEventListener('DOMContentLoaded', function() {
    if (document.getElementById('calGrid')) {
        initCalendar();
    }
});
