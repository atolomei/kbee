(function ($) {

    function debounce(fn, delay) {
        var t;
        return function () {
            clearTimeout(t);
            var ctx = this;
            var args = arguments;
            t = setTimeout(function () {
                fn.apply(ctx, args);
            }, delay);
        };
    }

    function isOpen($root) {
        return $root.hasClass('show') || $root.hasClass('open');
    }

    function bindDropupSmart(root) {
        var $root = $(root);
        var $toggle = $root.find('[data-toggle="dropdown"], [data-bs-toggle="dropdown"]');
        var $menu = $root.find('.dropdown-menu');

        if (!$toggle.length || !$menu.length) return;

        $root.removeClass('dropup');

        var el = $toggle[0];
        var rect = el.getBoundingClientRect();

        var spaceBelow = window.innerHeight - rect.bottom;
        var spaceAbove = rect.top;

        // FIX IMPORTANTE: forzar render real del menu
        var $clone = $menu
            .clone()
            .css({
                visibility: 'hidden',
                display: 'block',
                position: 'absolute',
                top: 0,
                left: 0
            })
            .appendTo('body');

        var menuHeight = $clone.outerHeight(true);
        $clone.remove();

        // decisión estable
        if (spaceBelow < menuHeight && spaceAbove > spaceBelow) {
            $root.addClass('dropup');
        }
    }

    // ------------------------------------------------------------
    // Bootstrap hook (compatible v4 + v5)
    // ------------------------------------------------------------
    $(document).on('show.bs.dropdown', '.dropdown', function () {
        bindDropupSmart(this);
    });

    // ------------------------------------------------------------
    // Scroll / resize SOLO si está abierto
    // ------------------------------------------------------------
    $(window).on('scroll resize', debounce(function () {
        $('.dropdown.show, .dropdown.open').each(function () {
            bindDropupSmart(this);
        });
    }, 50));

    // ------------------------------------------------------------
    // Wicket AJAX
    // ------------------------------------------------------------
    if (typeof Wicket !== "undefined") {
        Wicket.Event.subscribe('/ajax/call/complete', function () {
            $('.dropdown').each(function () {
                bindDropupSmart(this);
            });
        });
    }

    // ------------------------------------------------------------
    // fix: click dinámico (re-eval justo antes de abrir visualmente)
    // ------------------------------------------------------------
    $(document).on('click', '.dropdown [data-toggle="dropdown"], .dropdown [data-bs-toggle="dropdown"]', function () {
        var $root = $(this).closest('.dropdown');
        setTimeout(function () {
            bindDropupSmart($root);
        }, 0);
    });

    window.bindDropupSmart = bindDropupSmart;

})(jQuery);