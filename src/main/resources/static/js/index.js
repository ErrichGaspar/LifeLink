document.querySelectorAll('.feature-item').forEach((feature) => {
    feature.addEventListener('click', () => {
        const expanded = feature.classList.toggle('expanded');
        feature.setAttribute('aria-expanded', String(expanded));
    });
});
