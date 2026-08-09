// $(document).ready(function () {
//     $('.dropdown').on('show.bs.dropdown', function () {
//         var popperInstanceName = 'popper' + this.querySelector('.dropdown-menu').id;
//         if (window[popperInstanceName] == null) {
//             window[popperInstanceName] = Popper.createPopper(this, this.querySelector('.dropdown-menu'), {placement: 'bottom-end',});
//         }
//         window[popperInstanceName].update();
//     });
// });


function tryBindPopperDropdown(contextMenu) {
    var dropdown = contextMenu.parent().closest('.dropdown');
    if (dropdown) {
        var popperInstanceName = 'popper' + contextMenu.attr('id');
        console.log(contextMenu);
        if (window[popperInstanceName] == null) {
            window[popperInstanceName] = Popper.createPopper(dropdown[0], contextMenu[0], { placement: 'right-end'});
        }
        dropdown.on('show.bs.dropdown', function () {
            window[popperInstanceName].update();
        });
    }
}