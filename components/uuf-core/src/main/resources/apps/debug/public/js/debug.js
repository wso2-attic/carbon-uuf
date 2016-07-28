$( document ).ready(function() {
    showPages();
    $("#left-sidebar .nav-pills a").on("click",function(e){
        $("#left-sidebar .nav-pills a").parent().removeClass("active");
        var thisPage = $(this).data("href");
        switch(thisPage){
            case "pages": showPages(); $(this).parent().addClass("active");break;
            case "fragments": showFragments();$(this).parent().addClass("active");break;
            case "layouts": showLayouts();$(this).parent().addClass("active");break;
            case "themes": showThemes();$(this).parent().addClass("active");break;
            default: break;
        }
        e.preventDefault();
    });

});



function showPages(){
    requestUtil.makeRequest("GET", "/pages/", null, function(data) {
        $(".info-container").hide();
        $(".preloader").preloader("hide");
        $("#pages pre code").text(JSON.stringify(data, null, ' '));
        $("#pages").show();
        $(".lead").text("Pages");
    });
}
function showFragments(){
    requestUtil.makeRequest("GET", "/fragments/", null, function(data) {
        $(".info-container").hide();
        $(".preloader").preloader("hide");
        $("#fragments pre code").text(JSON.stringify(data, null, ' '));
        $("#fragments").show();
        $(".lead").text("Fragments");
    });
}
function showLayouts(){
    requestUtil.makeRequest("GET", "/layouts/", null, function(data) {
        $(".info-container").hide();
        $(".preloader").preloader("hide");
        $("#layouts pre code").text(JSON.stringify(data, null, ' '));
        $("#layouts").show();
        $(".lead").text("Layouts");
    });
}
function showThemes(){
    requestUtil.makeRequest("GET", "/themes/", null, function(data) {
        $(".info-container").hide();
        $(".preloader").preloader("hide");
        $("#themes pre code").text(JSON.stringify(data, null, ' ')).show();
        $("#themes").show();
        $(".lead").text("Themes");
    });
}

requestUtil = new function() {
    this.makeRequest = function(type, url, data, callback) {
        var requestUrl  = "https://localhost:9292/features-app/debug/api" + url
        $.ajax({
            type: type,
            url: requestUrl,
            data: data,
            success: callback,
            beforeSend: function (request)
            {
                $(".preloader").preloader("show");
            }
        });
    };
}

//preloader
(function($) {
    $.fn.preloader = function(action) {
        if (action === 'show') {
            this.html(
                '<div class="preloaderContainer">Loading...</div>'
            );
            $(".preloaderContainer").addClass('preloader');
        }
        if (action === 'hide') {
            $(".preloaderContainer").remove();
        }
        return this;
    };
}(jQuery));