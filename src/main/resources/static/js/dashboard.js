document.addEventListener("DOMContentLoaded", function () {

    const currentPath = window.location.pathname;

    const menuLinks = document.querySelectorAll(".menu a");

    menuLinks.forEach(function (link) {

        const linkPath = link.getAttribute("href");

        link.classList.remove("active");

        if (linkPath === currentPath) {
            link.classList.add("active");
        }

    });

});