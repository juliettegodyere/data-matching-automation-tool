document.addEventListener("DOMContentLoaded", function() {
    const customLinks = document.querySelectorAll(".custom-link");
    let openMenu = null; // To keep track of the currently open menu

    customLinks.forEach(function(customLink) {
        const menu = customLink.nextElementSibling; // Get the next sibling element, which is the <ul>
        menu.style.display = "none";

        customLink.addEventListener("click", function(event) {
            event.stopPropagation(); // Prevent the click event from propagating to document

            // Close the previously open menu
            if (openMenu && openMenu !== menu) {
                openMenu.style.display = "none";
            }

            // Toggle the menu visibility for the clicked menu
            if (menu.style.display === "block") {
                menu.style.display = "none";
                openMenu = null; // No menu is open
            } else {
                menu.style.display = "block";
                openMenu = menu; // Set the currently open menu
            }
        });

        // Close the menu when clicking outside of it
        document.addEventListener("click", function(event) {
            if (openMenu && !openMenu.contains(event.target)) {
                openMenu.style.display = "none";
                openMenu = null; // No menu is open
            }
        });
    });
});
