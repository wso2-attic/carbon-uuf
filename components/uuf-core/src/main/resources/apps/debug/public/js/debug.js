$(document).ready(function () {
    showPages();
    $("#left-sidebar .nav-pills a").on("click", function (e) {
        $("#left-sidebar .nav-pills a").parent().removeClass("active");
        var thisPage = $(this).data("href");
        switch (thisPage) {
            case "pages":
                showPages();
                $(this).parent().addClass("active");
                break;
            case "fragments":
                showFragments();
                $(this).parent().addClass("active");
                break;
            case "layouts":
                showLayouts();
                $(this).parent().addClass("active");
                break;
            case "themes":
                showThemes();
                $(this).parent().addClass("active");
                break;
        }
        e.preventDefault();
    });

    $('.nav a').click(function (e) {
        e.preventDefault();
        $(this).tab('show');
    });
});

function showPages() {
    requestUtil.makeRequest("GET", "/pages/", null, function (data) {
        $(".info-container").hide();
        $(".preloader").preloader("hide");
        $("#pages-details").html('');
        populatePages(data);
        $("#pages-json pre code").text(JSON.stringify(data, null, ' '));
        $("#pages").show();
        $("#main-header").text("Pages");
    });
}
function populatePages(dataForPages){
    Object.keys(dataForPages).forEach(function(key,index) {
        var value = dataForPages[key];
        if(key == "/root"){
            key = "/"
        }
        var sections = '<div class="page-header"><p class="lead">'+ key +'</p></div><ul id="pages-tree'+ index +'" class="tree-view tree-view-lines add-margin-bottom-5x"></ul>';
        var treeId = "#pages-tree"+ index;
        $("#pages-details").append(sections);
        populatePageHelper(value,treeId);
        $(treeId).tree_view();
    });

}


function populatePageHelper(co,tree){
    var string0 = '';
    $.each(co, function(k,v){
        if (v !== null && typeof v === 'object'){
            string0 += '<li class="branch">';
            string0 += '</i><a href="#"><span class="badge add-margin-right-1x">'+ (k+1) +'</span></a>';
            string0 += '<ul>';
            if ((v.layout !== null && typeof v.layout === 'object') || typeof v.layout !== 'undefined'){
                string0 += '<li class="branch">';
                string0 += '</i><a href="#">layout</a>';
                string0 += '<ul>';
                string0 += '<li><b>name: </b>'+ v.layout.name +'</li>';
                if (v.layout.renderer !== null && typeof v.layout.renderer === 'object'){
                    string0 += '<li class="branch">';
                    string0 += '<a href="#">renderer</a>';
                    string0 += '<ul>';
                    string0 += '<li><b>path: </b>'+ v.layout.renderer.path +'</li>';
                    string0 += '</ul>';
                    string0 += '</li>';
                }
                string0 += '</ul>';
            }
            if (v.renderer !== null && typeof v.renderer === 'object'){
                string0 += '<li class="branch">';
                string0 += '</i><a href="#">renderer</a>';
                string0 += '<ul>';
                string0 += '<li><b>js: </b>'+ v.renderer.js +'</li>';
                string0 += '<li><b>path: </b>'+ v.renderer.path +'</li>';
                string0 += '</ul>';
            }
            string0 += '<li><b>secured: </b>'+ v.secured +'</li>';
            if (v.uriPattern !== null && typeof v.uriPattern === 'object'){
                string0 += '<li class="branch">';
                string0 += '</i><a href="#">uriPattern</a>';
                string0 += '<ul>';
                string0 += '<li><b>pattern: </b>'+ v.uriPattern.pattern +'</li>';
                string0 += '<li><b>regex: </b>'+ v.uriPattern.regex +'</li>';
                string0 += '</ul>';
            }
            string0 += '</ul>';
        }
    });

    $(tree).html(string0);
}

function showFragments(){
    requestUtil.makeRequest("GET", "/fragments/", null, function(data) {
        $(".info-container").hide();
        $(".preloader").preloader("hide");
        populateFragments(data);
        $("#fragments-json pre code").text(JSON.stringify(data, null, ' '));
        $("#fragments").show();
        $("#main-header").text("Fragments");
    });
}

function populateFragments(d) {
    var stringMid = '';
    $.each(d, function (k, v) {
        if (v !== null && typeof v === 'object') {
            stringMid += '<li aria-expanded="true" class="branch">';
            stringMid +=
                '</i><a href="#"><span class="badge add-margin-right-1x">' + (k + 1) + '</span>' + v.name + '</a>';
            stringMid += '<ul>';
            if (v.renderer !== null && typeof v.renderer === 'object') {
                stringMid += '<li aria-expanded="true" class="branch">';
                stringMid += '<a href="#">renderer</a>';
                stringMid += '<ul>';
                if (v.renderer.js !== null && typeof v.renderer.js === 'object') {
                    stringMid += '<li aria-expanded="true" class="branch">';
                    stringMid += '<a href="#">js</a>';
                    stringMid += '<ul>';
                    stringMid += '<li><b>path: </b>' + v.renderer.js.path + '</li>';
                    stringMid += '</ul>';
                    stringMid += '</li>';
                    stringMid += '<li><b>path: </b>' + v.renderer.path + '</li>';
                } else {
                    stringMid += '<li><b>js: </b>' + v.renderer.js + '</li>';
                    stringMid += '<li><b>path: </b>' + v.renderer.path + '</li>';
                }
                stringMid += '</ul>';
                stringMid += '</li>';
                stringMid += '<li><b>secured: </b>' + v.secured + '</li>';
            } else {
                stringMid += '<li><b>renderer: </b>' + v.renderer + '</li>';
                stringMid += '<li><b>secured: </b>' + v.secured + '</li>';
            }
            stringMid += '</ul>';
            stringMid += '</li>';
        }
    });
    $('#fragments-tree').html(stringMid);
    $('#fragments-tree').tree_view();
}

function showLayouts() {
    requestUtil.makeRequest("GET", "/layouts/", null, function (data) {
        $(".info-container").hide();
        $(".preloader").preloader("hide");
        populateLayouts(data);
        $("#layouts pre code").text(JSON.stringify(data, null, ' '));
        $("#layouts").show();
        $("#main-header").text("Layouts");
    });
}

function populateLayouts(d) {
    var stringMid = '';
    $.each(d, function (k, v) {
        if (v !== null && typeof v === 'object') {
            stringMid += '<li aria-expanded="true" class="branch">';
            stringMid +=
                '</i><a href="#"><span class="badge add-margin-right-1x">' + (k + 1) + '</span>' + v.name + '</a>';
            stringMid += '<ul>';
            if (v.renderer !== null && typeof v.renderer === 'object') {
                stringMid += '<li aria-expanded="true" class="branch">';
                stringMid += '<a href="#">renderer</a>';
                stringMid += '<ul>';
                stringMid += '<li><b>path: </b>' + v.renderer.path + '</li>';
                stringMid += '</ul>';
                stringMid += '</li>';
            } else {
                stringMid += '<li><b>renderer: </b>' + v.renderer + '</li>';
            }
            stringMid += '</ul>';
            stringMid += '</li>';
        }
    });
    $('#layouts-tree').html(stringMid);
    $('#layouts-tree').tree_view();
}

function showThemes() {
    requestUtil.makeRequest("GET", "/themes/", null, function (data) {
        $(".info-container").hide();
        $(".preloader").preloader("hide");
        populateThemes(data);
        $("#themes pre code").text(JSON.stringify(data, null, ' ')).show();
        $("#themes").show();
        $("#main-header").text("Themes");
    });
}

function populateThemes(d) {
    var stringMid = '';
    $.each(d, function (k, v) {
        if (v !== null && typeof v === 'object') {
            stringMid += '<li aria-expanded="true" class="branch">';
            stringMid +=
                '</i><a href="#"><span class="badge add-margin-right-1x">' + (k + 1) + '</span>' + v.name + '</a>';
            stringMid += '<ul>';
            stringMid += '<li><b>path: </b>' + v.path + '</li>';
            stringMid += '</ul>';
            stringMid += '</li>';
        }
    });
    $('#themes-tree').html(stringMid);
    $('#themes-tree').tree_view();
}

requestUtil = new function () {
    this.makeRequest = function (type, url, data, callback) {
        var currentUrl = '';
        currentUrl = currentUrl.replace(/#[^#]*$/, "").replace(/\?[^\?]*$/, "").replace(/^https:/, "http:");
        var requestUrl = currentUrl + "api" + url;
        //console.log(requestUrl);
        $.ajax({
                   type: type,
                   url: requestUrl,
                   data: data,
                   success: callback,
                   beforeSend: function (request) {
                       $(".preloader").preloader("show");
                   }
               });
    };
};

//preloader
(function ($) {
    $.fn.preloader = function (action) {
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