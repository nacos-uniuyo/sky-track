(() => {
    const SVG_NS = 'http://www.w3.org/2000/svg';

    const icons = {
        thunder: '<path d="M7 2 L3 12 H8 L6 20 L13 9 H8 L10 2 Z" fill="currentColor"/>',
        rain: '<path d="M4 10 H14 C16 10 18 8 18 6 C18 4 16 2 14 2 C13 0 11 -1 9 0 C7 1 7 2 6 3 C5 3 3 4 3 6 C3 8 4 10 4 10 Z" fill="currentColor"/><line x1="7" y1="16" x2="6" y2="20" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/><line x1="12" y1="16" x2="11" y2="20" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/><line x1="17" y1="16" x2="16" y2="20" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>',
        snow: '<circle cx="6" cy="6" r="4" fill="none" stroke="currentColor" stroke-width="1.6"/><circle cx="14" cy="14" r="4" fill="none" stroke="currentColor" stroke-width="1.6"/><line x1="6" y1="6" x2="14" y2="14" stroke="currentColor" stroke-width="1.6"/>',
        mist: '<line x1="3" y1="6" x2="21" y2="6" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/><line x1="3" y1="10" x2="21" y2="10" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/><line x1="3" y1="14" x2="21" y2="14" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/><line x1="3" y1="18" x2="21" y2="18" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>',
        sun: '<circle cx="12" cy="12" r="4.5" fill="none" stroke="currentColor" stroke-width="1.6"/><line x1="12" y1="2" x2="12" y2="5" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/><line x1="12" y1="19" x2="12" y2="22" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/><line x1="2" y1="12" x2="5" y2="12" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/><line x1="19" y1="12" x2="22" y2="12" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/><line x1="4.5" y1="4.5" x2="6.5" y2="6.5" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/><line x1="17.5" y1="17.5" x2="19.5" y2="19.5" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/><line x1="4.5" y1="19.5" x2="6.5" y2="17.5" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/><line x1="17.5" y1="6.5" x2="19.5" y2="4.5" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>',
        cloud: '<path d="M4 15 H16 C17.7 15 19 13.7 19 12 C19 10.3 17.7 9 16 9 C15.8 6.8 13.9 5 11.5 5 C9.3 5 7.5 6.6 7 8.7 C6.4 8.3 5.7 8.1 5 8.1 C3 8.1 1.5 9.6 1.5 11.6 C1.5 13.5 2.6 15 4 15 Z" fill="currentColor"/>'
    };

    function iconFor(conditionCode) {
        if (conditionCode >= 200 && conditionCode < 300) {
            return icons.thunder;
        }
        if (conditionCode >= 300 && conditionCode < 600) {
            return icons.rain;
        }
        if (conditionCode >= 600 && conditionCode < 700) {
            return icons.snow;
        }
        if (conditionCode >= 700 && conditionCode < 800) {
            return icons.mist;
        }
        if (conditionCode === 800) {
            return icons.sun;
        }
        if (conditionCode > 800 && conditionCode < 900) {
            return icons.cloud;
        }
        return icons.sun;
    }

    function render(container, conditionCode) {
        const svg = document.createElementNS(SVG_NS, 'svg');
        svg.setAttribute('viewBox', '0 0 24 24');
        svg.setAttribute('class', 'weather-icon');
        svg.innerHTML = iconFor(conditionCode);
        container.appendChild(svg);
    }

    document.querySelectorAll('[data-weather-icon]').forEach((element) => {
        const code = element.getAttribute('data-weather-icon');
        if (code) {
            render(element, parseInt(code, 10));
        }
    });

    window.renderWeatherIcon = render;
})();
