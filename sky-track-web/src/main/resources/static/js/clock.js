(() => {
    const clock = document.getElementById('live-clock');
    if (!clock) {
        return;
    }

    function update() {
        const now = new Date();
        const options = {
            weekday: 'short',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        };
        clock.textContent = now.toLocaleTimeString(undefined, options);
    }

    update();
    setInterval(update, 1000);
})();
