// $(document).ready(function () {
//     updateButtonContainer();
//     var selectedRecordIds = [];

//     // Handle individual record checkboxes
//     $(".recordCheckbox").change(function () {
//         var recordIds = $(this).val();

//         if ($(this).is(":checked")) {
//             selectedRecordIds.push(recordIds);
//         } else {
//             selectedRecordIds = selectedRecordIds.filter(id => id !== recordIds);
//         }
//         // Update the "Select All" checkbox state
//         updateSelectAllCheckbox();
//         updateButtonContainer();
//     });

//     // Handle the "Select All" checkbox change event
//     $("#selectAllCheckbox").change(function () {
//         // Get the state of the "Select All" checkbox
//         var isChecked = $(this).is(":checked");

//         // Select all record checkboxes and highlight them
//         $(".recordCheckbox").prop("checked", isChecked);

//         // Show/hide buttons based on the checkbox state
//         updateButtonContainer();
//     });

//     // Handle the "Download Selected" button click
//     $("#downloadButton").click(function () {
//         // Set the selected record IDs as a hidden field value
//         $("#selectedRecordIds").val(selectedRecordIds);
//     });

//     // Function to update the "Select All" checkbox state
//     function updateSelectAllCheckbox() {
//         var anyChecked = $(".recordCheckbox:checked").length > 0;
//         $("#selectAllCheckbox").prop("checked", anyChecked);
//     }

//     // Function to show/hide buttons based on the checkbox state
//     function updateButtonContainer() {
//         var isChecked = $("#selectAllCheckbox").is(":checked");
//         var anyChecked = $(".recordCheckbox:checked").length > 0;

//         if (isChecked || anyChecked) {
//             $("#buttonContainer").show(); // Show buttons
//         } else {
//             $("#buttonContainer").hide(); // Hide buttons
//         }
//     }
//     console.log(selectedRecordIds);
// });

$(document).ready(function () {
    updateButtonContainer();
    var selectedRecordIds = [];

    // Function to update the "Select All" checkbox state
    function updateSelectAllCheckbox() {
        var anyChecked = $(".recordCheckbox:checked").length > 0;
        $("#selectAllCheckbox").prop("checked", anyChecked);
    }

    // Function to show/hide buttons based on the checkbox state
    function updateButtonContainer() {
        var isChecked = $("#selectAllCheckbox").is(":checked");
        var anyChecked = $(".recordCheckbox:checked").length > 0;

        if (isChecked || anyChecked) {
            $("#buttonContainer").show(); // Show buttons
        } else {
            $("#buttonContainer").hide(); // Hide buttons
        }
    }

    // Handle individual record checkboxes
    $(".recordCheckbox").change(function () {
        var recordIds = $(this).val();
        // var recordIds = parseInt($(this).val(), 10);

        if ($(this).is(":checked")) {
            selectedRecordIds.push(recordIds);
        } else {
            selectedRecordIds = selectedRecordIds.filter(id => id !== recordIds);
        }

        // Update the "Select All" checkbox state
        updateSelectAllCheckbox();
        updateButtonContainer();

        // Log the selectedRecordIds after it has been updated
        console.log(selectedRecordIds);
    });

    // Handle the "Select All" checkbox change event
    $("#selectAllCheckbox").change(function () {
        // Get the state of the "Select All" checkbox
        var isChecked = $(this).is(":checked");

        // Select all record checkboxes and highlight them
        $(".recordCheckbox").prop("checked", isChecked);

        if (isChecked) {
            // Add all record IDs to the selectedRecordIds
            selectedRecordIds = $(".recordCheckbox:checked").map(function () {
                return $(this).val();
            }).get();
        } else {
            // Clear the selectedRecordIds array
            selectedRecordIds = [];
        }

        // Update the "Select All" checkbox state
        updateSelectAllCheckbox();
        // Show/hide buttons based on the checkbox state
        updateButtonContainer();
        console.log(selectedRecordIds);
    });

    // Handle the "Accept Selected" button click
    $("#acceptButton").click(function () {
        // Set the selected record IDs as a hidden field value
        $("#acceptSelectedRecordIds").val(selectedRecordIds);
    });

     // Handle the "Accept Selected" button click
     $("#rejectButton").click(function () {
        // Set the selected record IDs as a hidden field value
        console.log("Rejected")
        $("#rejectSelectedRecordIds").val(selectedRecordIds);
    });
     // Handle the "Download Selected" button click
     $("#downloadButton").click(function () {
        // Set the selected record IDs as a hidden field value
        $("#downloadSelectedRecordIds").val(selectedRecordIds);
    });

    
});

