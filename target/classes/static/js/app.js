// ─── Sidebar Collapse ─────────────────────────────────────────────────────────
function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    const icon    = document.getElementById('collapseIcon');
    if (!sidebar) return;
    sidebar.classList.toggle('collapsed');
    if (sidebar.classList.contains('collapsed')) {
        icon.textContent = '›';
    } else {
        icon.textContent = '‹';
    }
    localStorage.setItem('sidebarCollapsed', sidebar.classList.contains('collapsed'));
}

// Restore sidebar state on load
document.addEventListener('DOMContentLoaded', function() {
    const collapsed = localStorage.getItem('sidebarCollapsed') === 'true';
    const sidebar   = document.getElementById('sidebar');
    const icon      = document.getElementById('collapseIcon');
    if (sidebar && collapsed) {
        sidebar.classList.add('collapsed');
        if (icon) icon.textContent = '›';
    }

    // Date validation: end date >= start date
    const startDate = document.querySelector('input[name="startDate"]');
    const endDate   = document.querySelector('input[name="endDate"]');
    if (startDate && endDate) {
        startDate.addEventListener('change', function() {
            endDate.min = this.value;
            if (endDate.value && endDate.value < this.value) {
                endDate.value = this.value;
            }
        });
    }

    // Auto-dismiss alerts
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(function(alert) {
        setTimeout(function() {
            alert.style.transition = 'opacity 0.5s';
            alert.style.opacity = '0';
            setTimeout(function() { alert.remove(); }, 500);
        }, 4000);
    });
});
