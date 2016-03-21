$(function () {
    'use strict';
    var contentDiv = $('#content');
    $(window).on('hashchange', function () {
        var hash = location.hash.substr(1);
        if (hash != '') {
            var contentChild = contentDiv.children('#' + hash);
            if (contentChild.length > 0) {

            } else {
                $.ajax(hash + '.html').done(function (html) {
                    contentChild = contentDiv.append('<div id="' + hash + '">' + html + '</div>');
                });
            }
            contentDiv.children().hide();
            contentChild.show();
        }
    }).trigger('hashchange');
});