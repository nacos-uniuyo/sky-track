(() => {
    const canvas = document.getElementById('liquid-canvas');
    if (!canvas) {
        return;
    }

    const ctx = canvas.getContext('2d');
    let width = 0;
    let height = 0;
    let startColor = '#0D1B2A';
    let endColor = '#1B2838';
    let time = 0;

    const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    function resize() {
        width = canvas.width = window.innerWidth;
        height = canvas.height = window.innerHeight;
    }

    function readThemeColors() {
        const style = getComputedStyle(document.body);
        startColor = style.getPropertyValue('--color-background-start').trim() || startColor;
        endColor = style.getPropertyValue('--color-background-end').trim() || endColor;
    }

    function wave(x, amplitude, frequency, phase) {
        return amplitude * Math.sin(x * frequency + time * 0.02 + phase)
                + amplitude * 0.5 * Math.sin(x * frequency * 2.3 + time * 0.03 + phase);
    }

    function draw() {
        ctx.clearRect(0, 0, width, height);

        const gradient = ctx.createLinearGradient(0, 0, 0, height);
        gradient.addColorStop(0, startColor);
        gradient.addColorStop(1, endColor);
        ctx.fillStyle = gradient;
        ctx.fillRect(0, 0, width, height);

        const layers = [
            { amplitude: 60, frequency: 0.002, phase: 0, alpha: 0.18 },
            { amplitude: 90, frequency: 0.0012, phase: 2, alpha: 0.12 },
            { amplitude: 40, frequency: 0.003, phase: 4, alpha: 0.10 }
        ];

        layers.forEach((layer) => {
            ctx.beginPath();
            ctx.moveTo(0, height);
            for (let x = 0; x <= width; x += 16) {
                const y = height * 0.7 + wave(x, layer.amplitude, layer.frequency, layer.phase);
                ctx.lineTo(x, y);
            }
            ctx.lineTo(width, height);
            ctx.closePath();
            ctx.fillStyle = `rgba(255, 255, 255, ${layer.alpha})`;
            ctx.fill();
        });
    }

    function animate() {
        draw();
        if (!reducedMotion) {
            time += 1;
            window.requestAnimationFrame(animate);
        }
    }

    window.addEventListener('resize', () => {
        resize();
        if (reducedMotion) {
            draw();
        }
    });

    const observer = new MutationObserver(() => {
        readThemeColors();
        if (reducedMotion) {
            draw();
        }
    });
    observer.observe(document.body, { attributes: true, attributeFilter: ['class'] });

    resize();
    readThemeColors();
    animate();
})();
