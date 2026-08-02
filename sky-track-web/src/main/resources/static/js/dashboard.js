(() => {
    const hero = document.getElementById('hero');
    if (!hero) {
        return;
    }

    const heroFields = {
        icon: document.getElementById('hero-icon'),
        city: document.getElementById('hero-city'),
        temp: document.getElementById('hero-temp'),
        condition: document.getElementById('hero-condition'),
        high: document.getElementById('hero-high'),
        low: document.getElementById('hero-low'),
        feels: document.getElementById('hero-feels'),
        humidity: document.getElementById('hero-humidity'),
        wind: document.getElementById('hero-wind'),
        pressure: document.getElementById('hero-pressure'),
        delta: document.getElementById('hero-delta')
    };

    document.querySelectorAll('.city-card').forEach((card) => {
        card.addEventListener('click', (event) => {
            if (event.target.closest('form')) {
                return;
            }
            document.querySelectorAll('.city-card').forEach((c) => c.classList.remove('active'));
            card.classList.add('active');
            updateHero(card);
        });
    });

    function updateHero(card) {
        const city = card.dataset;

        heroFields.icon.innerHTML = '';
        if (city.icon) {
            window.renderWeatherIcon(heroFields.icon, parseInt(city.icon, 10));
        }

        heroFields.city.textContent = city.name;
        heroFields.temp.textContent = city.temp ? city.temp + ' C' : '--';
        heroFields.condition.textContent = city.condition || '';

        heroFields.high.textContent = city.high ? 'H ' + city.high : '';
        heroFields.low.textContent = city.low ? 'L ' + city.low : '';
        heroFields.feels.textContent = city.feels ? 'Feels like ' + city.feels : '';
        heroFields.humidity.textContent = city.humidity ? 'Humidity ' + city.humidity + '%' : '';
        heroFields.wind.textContent = city.wind ? 'Wind ' + city.wind + ' m/s' : '';
        heroFields.pressure.textContent = city.pressure ? 'Pressure ' + city.pressure + ' hPa' : '';

        if (city.delta) {
            const value = parseFloat(city.delta);
            const direction = value >= 0 ? 'up' : 'down';
            heroFields.delta.textContent = 'Shift ' + (value > 0 ? '+' : '') + value + ' C';
            heroFields.delta.className = 'delta-badge ' + direction;
        } else {
            heroFields.delta.textContent = '';
            heroFields.delta.className = 'delta-badge';
        }
    }
})();
