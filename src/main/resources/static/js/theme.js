const themeToggle = document.querySelector(".theme-toggle");


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

if (themeToggle) {

    themeToggle.addEventListener("click", () => {

        document.body.classList.toggle("dark-mode");

        if (document.body.classList.contains("dark-mode")) {

            localStorage.setItem("theme", "dark");

        } else {

            localStorage.setItem("theme", "light");

        }

    });

}