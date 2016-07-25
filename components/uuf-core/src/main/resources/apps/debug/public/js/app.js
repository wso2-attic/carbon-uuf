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