(function ($) {

    // ------------------------------------------------------------
    // UTIL: debounce
    // ------------------------------------------------------------
    function debounce(fn, delay) {
        var t;
        return function () {
            clearTimeout(t);
            var args = arguments;
            var ctx = this;
            t = setTimeout(function () {
                fn.apply(ctx, args);
            }, delay);
        };
    }

    // ------------------------------------------------------------
    // CORE: decide dropup / dropdown
    // ------------------------------------------------------------
function bindDropupSmart(root) {
    var $root = $(root);
    var $toggle = $root.find('[data-toggle="dropdown"], [data-bs-toggle="dropdown"]');
    var $menu = $root.find('.dropdown-menu');

    if (!$toggle.length || !$menu.length) return;

    $root.removeClass('dropup');

    var el = $toggle[0];
    var rect = el.getBoundingClientRect();

    var footerTop = getFooterTopViewport();

    // espacio real usable abajo = mínimo entre viewport y footer
    var viewportBottom = window.innerHeight;
    var limitBottom = Math.min(viewportBottom, footerTop);

    var spaceBelow = limitBottom - rect.bottom - 30;
    var spaceAbove = rect.top;

    // medir altura real del menú (clonado offscreen)
    var $clone = $menu.clone().css({
        visibility: 'hidden',
        display: 'block',
        position: 'absolute',
        height: 'auto'
    }).appendTo('body');

    var menuHeight = $clone.outerHeight();
    $clone.remove();

    // decisión inteligente
    if (spaceBelow < menuHeight && spaceAbove > spaceBelow) {
        $root.addClass('dropup');
    }
}
function getFooterTopViewport() {
    var footer = document.querySelector("footer");
    if (!footer) return window.innerHeight;

    return footer.getBoundingClientRect().top;
}
    // ------------------------------------------------------------
    // 1. Bootstrap event: antes de abrir dropdown
    // ------------------------------------------------------------
    $(document).on('show.bs.dropdown', '.dropdown', function () {
        bindDropupSmart(this);
    });


$(document).on('shown.bs.dropdown', '.dropdown', function () {
    var root = this;

    requestAnimationFrame(function () {
        requestAnimationFrame(function () {
            bindDropupSmart(root);
        });
    });
});
    // ------------------------------------------------------------
    // 2. Scroll + resize (solo abiertos)
    // ------------------------------------------------------------
    $(window).on('scroll resize', debounce(function () {
        $('.dropdown.open').each(function () {
            bindDropupSmart(this);
        });
    }, 50));

    // ------------------------------------------------------------
    // 3. WICKET AJAX hook
    // ------------------------------------------------------------
    if (typeof Wicket !== "undefined") {
        Wicket.Event.subscribe('/ajax/call/complete', function () {
            $('.dropdown').each(function () {
                bindDropupSmart(this);
            });
        });
    }

    // ------------------------------------------------------------
    // expose manual API (opcional útil en Wicket)
    // ------------------------------------------------------------
    window.bindDropupSmart = bindDropupSmart;

})(jQuery);