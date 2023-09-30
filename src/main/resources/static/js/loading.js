/*<![CDATA[*/
document.addEventListener("DOMContentLoaded", function() {

    var showLoadingButton = document.getElementById("showLoadingButton");
    var loadingOverlay = document.getElementById("loading-overlay");
    loadingOverlay.style.display = "none";

    if (showLoadingButton && loadingOverlay) {
        showLoadingButton.addEventListener("click", function() {
            loadingOverlay.style.display = "flex"; // Show the loading overlay
        });
    }
});
/*]]>*/