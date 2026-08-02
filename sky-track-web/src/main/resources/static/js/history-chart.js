(() => {
    const canvas = document.getElementById('history-chart');
    if (!canvas) {
        return;
    }

    const ctx = canvas.getContext('2d');
    const cityId = canvas.dataset.cityId;
    let points = [];
    let hoverIndex = -1;
    let chartWidth = 0;
    let chartHeight = 0;

    function resize() {
        const rect = canvas.getBoundingClientRect();
        const dpr = window.devicePixelRatio || 1;
        canvas.width = rect.width * dpr;
        canvas.height = rect.height * dpr;
        ctx.scale(dpr, dpr);
        chartWidth = rect.width;
        chartHeight = rect.height;
    }

    function draw() {
        ctx.clearRect(0, 0, chartWidth, chartHeight);
        if (points.length < 2) {
            ctx.fillStyle = 'rgba(255, 255, 255, 0.6)';
            ctx.font = '14px Inter, sans-serif';
            ctx.textAlign = 'center';
            ctx.fillText('Not enough historical data yet', chartWidth / 2, chartHeight / 2);
            return;
        }

        const style = getComputedStyle(document.body);
        const lineColor = style.getPropertyValue('--color-primary').trim() || '#FF7E00';
        const gridColor = 'rgba(255, 255, 255, 0.1)';
        const textColor = 'rgba(255, 255, 255, 0.7)';

        const temps = points.map((p) => p.temperature);
        const minTemp = Math.min(...temps);
        const maxTemp = Math.max(...temps);
        const pad = 40;
        const range = maxTemp - minTemp || 1;

        const xFor = (i) => pad + (i * (chartWidth - pad * 2)) / (points.length - 1);
        const yFor = (t) => pad + ((maxTemp - t) / range) * (chartHeight - pad * 2);

        for (let i = 0; i <= 4; i++) {
            const y = pad + (i * (chartHeight - pad * 2)) / 4;
            ctx.strokeStyle = gridColor;
            ctx.beginPath();
            ctx.moveTo(pad, y);
            ctx.lineTo(chartWidth - pad, y);
            ctx.stroke();
            const label = maxTemp - (i * range) / 4;
            ctx.fillStyle = textColor;
            ctx.font = '11px Inter, sans-serif';
            ctx.textAlign = 'right';
            ctx.fillText(label.toFixed(1) + ' C', pad - 8, y + 4);
        }

        ctx.strokeStyle = lineColor;
        ctx.lineWidth = 2;
        ctx.beginPath();
        points.forEach((p, i) => {
            const x = xFor(i);
            const y = yFor(p.temperature);
            if (i === 0) {
                ctx.moveTo(x, y);
            } else {
                ctx.lineTo(x, y);
            }
        });
        ctx.stroke();

        ctx.fillStyle = lineColor;
        points.forEach((p, i) => {
            const x = xFor(i);
            const y = yFor(p.temperature);
            ctx.beginPath();
            ctx.arc(x, y, 3.5, 0, Math.PI * 2);
            ctx.fill();
        });

        if (hoverIndex >= 0 && hoverIndex < points.length) {
            const p = points[hoverIndex];
            const x = xFor(hoverIndex);
            const y = yFor(p.temperature);
            ctx.strokeStyle = textColor;
            ctx.setLineDash([4, 4]);
            ctx.beginPath();
            ctx.moveTo(x, pad);
            ctx.lineTo(x, chartHeight - pad);
            ctx.stroke();
            ctx.setLineDash([]);

            ctx.fillStyle = lineColor;
            ctx.beginPath();
            ctx.arc(x, y, 5, 0, Math.PI * 2);
            ctx.fill();

            ctx.fillStyle = 'rgba(0, 0, 0, 0.7)';
            const label = p.temperature + ' C';
            ctx.fillRect(x - 30, 6, 60, 18);
            ctx.fillStyle = '#ffffff';
            ctx.font = '11px Inter, sans-serif';
            ctx.textAlign = 'center';
            ctx.fillText(label, x, 19);
        }
    }

    function onMove(event) {
        const rect = canvas.getBoundingClientRect();
        const x = event.clientX - rect.left;
        const pad = 40;
        if (points.length < 2) {
            hoverIndex = -1;
            draw();
            return;
        }
        const index = Math.round(((x - pad) / (chartWidth - pad * 2)) * (points.length - 1));
        hoverIndex = Math.max(0, Math.min(points.length - 1, index));
        draw();
    }

    function onLeave() {
        hoverIndex = -1;
        draw();
    }

    canvas.addEventListener('mousemove', onMove);
    canvas.addEventListener('mouseleave', onLeave);
    window.addEventListener('resize', () => {
        resize();
        draw();
    });

    resize();
    draw();

    if (cityId) {
        fetch('/api/weather/' + cityId + '/snapshots?days=7')
            .then((response) => response.json())
            .then((data) => {
                points = data;
                draw();
            })
            .catch(() => {
            });
    }
})();
