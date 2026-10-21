const themeToggles = document.querySelectorAll(".theme-toggle");


/* =========================================================
   APPLY SAVED THEME
   ========================================================= */

const savedTheme = localStorage.getItem("theme");

if (savedTheme === "dark") {
    document.body.classList.add("dark-mode");
}


/* =========================================================
   THEME TOGGLE
   ========================================================= */

themeToggles.forEach((themeToggle) => {
    themeToggle.addEventListener("click", () => {
        const darkMode = document.body.classList.toggle("dark-mode");
        localStorage.setItem("theme", darkMode ? "dark" : "light");
    });
});
