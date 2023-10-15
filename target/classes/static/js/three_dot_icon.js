document.addEventListener("DOMContentLoaded", function() {
    const customLink = document.querySelector(".custom-link");
    const menu = document.querySelector(".menu");

    customLink.addEventListener("click", function(event) {
        event.stopPropagation(); // Prevent the click event from propagating to document

        // Toggle the menu visibility
        if (menu.style.display === "block") {
            menu.style.display = "none";
        } else {
            menu.style.display = "block";
        }
    });

    // Close the menu when clicking outside of it
    document.addEventListener("click", function(event) {
        if (!menu.contains(event.target) && menu.style.display === "block") {
            menu.style.display = "none";
        }
    });
});
