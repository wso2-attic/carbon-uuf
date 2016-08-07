$(function () {
    'use strict';
    var contentDiv = $('#content');

    var updateMenu = function (menuItemName, contentElm) {
        $('.sidebar-nav li').removeClass('active');
        $('a[href$="#' + menuItemName + '"]').parent().addClass('active');
        contentDiv.children().hide();
        contentElm.show();

    };

    $(window).on('hashchange', function () {
        var hash = location.hash.substr(1);
        if (hash != '') {
            var existingContent = contentDiv.children('#' + hash);
            if (existingContent.length > 0) {
                updateMenu(hash, existingContent);
            } else {
                $.ajax(hash + '.html').done(function (html) {
                    var newlyAdded = contentDiv.append('<div id="' + hash + '">' + html + '</div>');
                    updateMenu(hash, newlyAdded);
                });
            }
        }
    }).trigger('hashchange');

    $('#menu-toggle').click(function (e) {
        e.preventDefault();
        $('#wrapper').toggleClass('toggled');
    });

});

$.sidebar_toggle = function (action, target, container) {
    var elem = '[data-toggle=sidebar]',
        button,
        container,
        conrainerOffsetLeft,
        conrainerOffsetRight,
        target,
        targetOffsetLeft,
        targetOffsetRight,
        targetWidth,
        targetSide,
        relationship,
        pushType,
        buttonParent;

    var sidebar_window = {
        update: function (target, container, button) {
            conrainerOffsetLeft = $(container).data('offset-left') ? $(container).data('offset-left') : 0,
                conrainerOffsetRight = $(container).data('offset-right') ? $(container).data('offset-right') : 0,
                targetOffsetLeft = $(target).data('offset-left') ? $(target).data('offset-left') : 0,
                targetOffsetRight = $(target).data('offset-right') ? $(target).data('offset-right') : 0,
                targetWidth = $(target).data('width'),
                targetSide = $(target).data("side"),
                pushType = $(container).parent().is('body') == true ? 'padding' : 'margin';

            if (button !== undefined) {
                relationship = button.attr('rel') ? button.attr('rel') : '';
                buttonParent = $(button).parent();
            }
        },
        show: function () {

            if ($(target).data('sidebar-fixed') == true) {
                $(target).height($(window).height() - $(target).data('fixed-offset'));
            }

            $(target).trigger('show.sidebar');
            if (targetWidth !== undefined) {
                $(target).css('width', targetWidth);
            }
            $(target).addClass('toggled');

            if (button !== undefined) {
                if (relationship !== '') {
                    // Removing active class from all relative buttons
                    $(elem + '[rel=' + relationship + ']:not([data-handle=close])').removeClass("active");
                    $(elem + '[rel=' + relationship + ']:not([data-handle=close])').attr('aria-expanded', 'false');
                }

                // Adding active class to button
                if (button.attr('data-handle') !== 'close') {
                    button.addClass("active");
                    button.attr('aria-expanded', 'true');
                }

                if (buttonParent.is('li')) {
                    if (relationship !== '') {
                        $(elem + '[rel=' + relationship + ']:not([data-handle=close])').parent().removeClass("active");
                        $(elem + '[rel=' + relationship + ']:not([data-handle=close])').parent().attr('aria-expanded',
                                                                                                      'false');
                    }
                    buttonParent.addClass("active");
                    buttonParent.attr('aria-expanded', 'true');
                }
            }

            // Sidebar open function
            if (targetSide == 'left') {
                if ((button !== undefined) && (button.attr('data-container-divide'))) {
                    $(container).css(pushType + '-' + targetSide, targetWidth + targetOffsetLeft);
                }
                $(target).css(targetSide, targetOffsetLeft);
            }
            else if (targetSide == 'right') {
                if ((button !== undefined) && (button.attr('data-container-divide'))) {
                    $(container).css(pushType + '-' + targetSide, targetWidth + targetOffsetRight);
                }
                $(target).css(targetSide, targetOffsetRight);
            }

            $(target).trigger('shown.sidebar');
        },
        hide: function () {

            $(target).trigger('hide.sidebar');
            $(target).removeClass('toggled');

            if (button !== undefined) {
                if (relationship !== '') {
                    // Removing active class from all relative buttons
                    $(elem + '[rel=' + relationship + ']:not([data-handle=close])').removeClass("active");
                    $(elem + '[rel=' + relationship + ']:not([data-handle=close])').attr('aria-expanded', 'false');
                }
                // Removing active class from button
                if (button.attr('data-handle') !== 'close') {
                    button.removeClass("active");
                    button.attr('aria-expanded', 'false');
                }

                if ($(button).parent().is('li')) {
                    if (relationship !== '') {
                        $(elem + '[rel=' + relationship + ']:not([data-handle=close])').parent().removeClass("active");
                        $(elem + '[rel=' + relationship + ']:not([data-handle=close])').parent().attr('aria-expanded',
                                                                                                      'false');
                    }
                }
            }

            // Sidebar close function
            if (targetSide == 'left') {
                if ((button !== undefined) && (button.attr('data-container-divide'))) {
                    $(container).css(pushType + '-' + targetSide, targetOffsetLeft);
                }
                $(target).css(targetSide, -Math.abs(targetWidth + targetOffsetLeft));
            }
            else if (targetSide == 'right') {
                if ((button !== undefined) && (button.attr('data-container-divide'))) {
                    $(container).css(pushType + '-' + targetSide, targetOffsetRight);
                }
                $(target).css(targetSide, -Math.abs(targetWidth + targetOffsetRight));
            }

            $(target).trigger('hidden.sidebar');
        }
    };

    if (action === 'show') {
        sidebar_window.update(target, container);
        sidebar_window.show();
    }
    if (action === 'hide') {
        sidebar_window.update(target, container);
        sidebar_window.hide();
    }

    // binding click function
    $('body').off('click', elem);
    $('body').on('click', elem, function (e) {
        e.preventDefault();

        button = $(this);
        container = button.data('container');
        target = button.data('target');

        sidebar_window.update(target, container, button);

        /**
         * Sidebar function on data container divide
         * @return {Null}
         */
        if (button.attr('aria-expanded') == 'false') {
            sidebar_window.show();
        }
        else if (button.attr('aria-expanded') == 'true') {
            sidebar_window.hide();
        }

    });
};

$('.sidebar-wrapper[data-fixed-offset-top]').on('affix.bs.affix', function () {
    $(this).css('top', $(this).data('fixed-offset-top'));
});

$(window).resize(function () {
    $('.sidebar-wrapper').each(function () {
        $(this).height($(window).height() - ($(this).offset().top - $(window).scrollTop()));
    });
});

$(window).load(function () {
    $('.sidebar-wrapper').each(function () {
        $(this).height($(window).height() - ($(this).offset().top - $(window).scrollTop()));
    });
});

$(window).scroll(function () {
    $('.sidebar-wrapper').each(function () {
        $(this).height($(window).height() - ($(this).offset().top - $(window).scrollTop()));
    });
});

/**
 * @description Attribute toggle function
 * @param  {String} attr    Attribute Name
 * @param  {String} val     Value to be matched
 * @param  {String} val2    Value to be replaced with
 */
$.fn.toggleAttr = function (attr, val, val2) {
    return this.each(function () {
        var self = $(this);
        if (self.attr(attr) == val) {
            self.attr(attr, val2);
        } else {
            self.attr(attr, val);
        }
    });
};

/**
 * Tree view function
 * @return {Null}
 */
$.fn.tree_view = function () {
    var tree = $(this);
    tree.find('li').has("ul").each(function () {
        var branch = $(this); //li with children ul
        branch.prepend('<i class="icon"></i>');
        branch.addClass('branch');
        branch.on('click', function (e) {
            if (this == e.target) {
                var icon = $(this).children('i:first');
                icon.closest('li').toggleAttr('aria-expanded', 'true', 'false');
            }
        });
    });

    tree.find('.branch .icon').each(function () {
        $(this).on('click', function () {
            $(this).closest('li').click();
        });
    });

    tree.find('.branch > a').each(function () {
        $(this).on('click', function (e) {
            $(this).closest('li').click();
            e.preventDefault();
        });
    });

    tree.find('.branch > button').each(function () {
        $(this).on('click', function (e) {
            $(this).closest('li').click();
            e.preventDefault();
        });
    });
};
