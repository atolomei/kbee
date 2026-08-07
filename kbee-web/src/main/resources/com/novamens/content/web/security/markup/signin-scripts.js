angular.module('idoc.signin', []);
angular.module('idoc.common', []);
angular.module('idoc', ['idoc.signin', 'idoc.common']);

//  Timeout Service

(function () {
    'use strict';

    function service($rootScope) {
        function timeout(fn, time) {
            return setTimeout(function () {
                $rootScope.$apply(fn);
            }, time);
        }

        timeout.cancel = function (timer) {
            clearTimeout(timer);
        };

        return timeout;
    }

    angular
        .module('idoc.common')
        .factory('timeout', ['$rootScope', service]);
})();

//  Input Directive

(function () {
    'use strict';

    function inputText(timeout) {
        var isIE9 = angular.element('html').hasClass('ie9');

        function link(scope, elem, attr) {
            var wrap = elem.parent();

            function onFocus() {
                wrap.addClass('focus');
            }

            function onBlur() {
                wrap.removeClass('focus');
            }

            function onMouseOver() {
                wrap.addClass('hover');
            }

            function onMouseOut() {
                wrap.removeClass('hover');
            }

            elem
                .on('blur', onBlur)
                .on('focus', onFocus)
                .on('mouseout', onMouseOut)
                .on('mouseover', onMouseOver);

            function setPlaceholder() {
                var hasVal = elem.val().length === 0;
                wrap[hasVal ? 'addClass' : 'removeClass']('is-empty');
            }

            function onKeyDown() {
                timeout(setPlaceholder);
            }

            if (isIE9) {
                elem
                    .on('keydown input propertychange', onKeyDown);
            }

            onKeyDown();
        }

        return {
            link: link,
            restrict: 'C'
        };
    }

    angular
        .module('idoc.common')
        .directive('inputText', ['timeout', inputText]);
})();

//  Select Menu Directive

(function () {
    'use strict';

    function selectMenu(timeout) {
        var body,
            index = 1;

        function link(scope, elem, attr) {
            var click, dir;

            click = 'click.selectMenu' + index++;

            body = body || angular.element('body');

            dir = {
                state: {
                    active: false
                },

                off: function () {
                    body.off(click);
                    scope.$apply(function () {
                        dir.state.active = false;
                    });
                },

                toggle: function ($event) {
                    $event.preventDefault();
                    $event.stopPropagation();
                    dir.state.active = !dir.state.active;

                    timeout(function () {
                        if (dir.state.active) {
                            body.on(click, dir.off);
                        }
                        else {
                            body.off(click);
                        }
                    });
                }
            };

            scope.selectMenu = dir;
        }

        return {
            link: link,
            restrict: 'C'
        };
    }

    angular
        .module('idoc.signin')
        .directive('selectMenu', ['timeout', selectMenu]);
})();

