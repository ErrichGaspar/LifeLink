const themeToggles = document.querySelectorAll(".theme-toggle");

function setTheme(isDark) {
    document.body.classList.toggle("dark-mode", isDark);
    themeToggles.forEach((toggle) => toggle.setAttribute("aria-pressed", String(isDark)));
}

/* =========================================================
   APPLY SAVED THEME
   ========================================================= */

const savedTheme = localStorage.getItem("theme");
setTheme(savedTheme === "dark");


/* =========================================================
   THEME TOGGLE
   ========================================================= */

themeToggles.forEach((themeToggle) => {
    themeToggle.addEventListener("click", () => {
        const darkMode = !document.body.classList.contains("dark-mode");
        setTheme(darkMode);
        localStorage.setItem("theme", darkMode ? "dark" : "light");
    });
});
