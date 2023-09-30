$('#exampleModal').on('show.bs.modal', function () {
        $.get("/modals/modal1", function (data) {
            $('#exampleModal').find('.modal-body').html(data);
        })
    });