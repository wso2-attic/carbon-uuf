/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

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
            case "profiler":
                showProfiler();
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
        createPagesTree(value,treeId);
        $(treeId).tree_view();
    });

}


function createPagesTree(treeData,treeId){
    var pagesTreeString = '';
    $.each(treeData, function(k,v){
        if (v !== null && typeof v === 'object'){
            pagesTreeString += '<li class="branch">';
            pagesTreeString += '</i><a href="#"><span class="badge add-margin-right-1x">'+ (k+1) +'</span></a>';
            pagesTreeString += '<ul>';
            if ((v.layout !== null && typeof v.layout === 'object') || typeof v.layout !== 'undefined'){
                pagesTreeString += '<li class="branch">';
                pagesTreeString += '</i><a href="#">layout</a>';
                pagesTreeString += '<ul>';
                pagesTreeString += '<li><b>name: </b>'+ v.layout.name +'</li>';
                if (v.layout.renderer !== null && typeof v.layout.renderer === 'object'){
                    pagesTreeString += '<li class="branch">';
                    pagesTreeString += '<a href="#">renderer</a>';
                    pagesTreeString += '<ul>';
                    pagesTreeString += '<li><b>path: </b><a title="Click to view" target="_blank" href="file://'+ v.layout.renderer.path.absolute +'">'+ v.layout.renderer.path.relative +'</a></li>';
                    pagesTreeString += '</ul>';
                    pagesTreeString += '</li>';
                }
                pagesTreeString += '</ul>';
            }
            if (v.renderer !== null && typeof v.renderer === 'object'){
                pagesTreeString += '<li class="branch">';
                pagesTreeString += '</i><a href="#">renderer</a>';
                pagesTreeString += '<ul>';
                if (v.renderer.js){
                    pagesTreeString += '<li><b>js: </b><a title="Click to view" target="_blank" href="file://'+ v.renderer.js.path.absolute +'">'+ v.renderer.js.path.relative +'</a></li>';
                }else{
                    pagesTreeString += '<li><b>js: </b> - </li>';
                }
                pagesTreeString += '<li><b>path: </b><a title="Click to view" target="_blank" href="file://'+ v.renderer.path.absolute +'">'+ v.renderer.path.relative +'</a></li>';
                pagesTreeString += '</ul>';
            }
            pagesTreeString += '<li><b>secured: </b>'+ v.secured +'</li>';
            if (v.uriPattern !== null && typeof v.uriPattern === 'object'){
                pagesTreeString += '<li class="branch">';
                pagesTreeString += '</i><a href="#">uriPattern</a>';
                pagesTreeString += '<ul>';
                pagesTreeString += '<li><b>pattern: </b>'+ v.uriPattern.pattern +'</li>';
                pagesTreeString += '<li><b>regex: </b>'+ v.uriPattern.regex +'</li>';
                pagesTreeString += '</ul>';
            }
            pagesTreeString += '</ul>';
        }
    });

    $(treeId).html(pagesTreeString);
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

function populateFragments(treeData) {
    var fragmentsTreeString = '';
    $.each(treeData, function (k, v) {
        if (v !== null && typeof v === 'object') {
            fragmentsTreeString += '<li aria-expanded="true" class="branch">';
            fragmentsTreeString += '</i><a href="#"><span class="badge add-margin-right-1x">' + (k + 1) + '</span>' + v.name + '</a>';
            fragmentsTreeString += '<ul>';
            if (v.renderer !== null && typeof v.renderer === 'object') {
                fragmentsTreeString += '<li aria-expanded="true" class="branch">';
                fragmentsTreeString += '<a href="#">renderer</a>';
                fragmentsTreeString += '<ul>';
                if (v.renderer.js){
                    fragmentsTreeString += '<li><b>js: </b><a title="Click to view" target="_blank" href="file://'+ v.renderer.js.path.absolute +'">'+ v.renderer.js.path.relative +'</a></li>';
                }else{
                    fragmentsTreeString += '<li><b>js: </b> - </li>';
                }
                fragmentsTreeString += '<li><b>path: </b><a title="Click to view" target="_blank" href="file://'+ v.renderer.path.absolute +'">'+ v.renderer.path.relative +'</a></li>';
                fragmentsTreeString += '</ul>';
                fragmentsTreeString += '</li>';
                fragmentsTreeString += '<li><b>secured: </b>' + v.secured + '</li>';
            } else {
                fragmentsTreeString += '<li><b>renderer: </b>' + v.renderer + '</li>';
                fragmentsTreeString += '<li><b>secured: </b>' + v.secured + '</li>';
            }
            fragmentsTreeString += '</ul>';
            fragmentsTreeString += '</li>';
        }
    });
    $('#fragments-tree').html(fragmentsTreeString);
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

function populateLayouts(treeData) {
    var layoutsTreeString = '';
    $.each(treeData, function (k, v) {
        if (v !== null && typeof v === 'object') {
            layoutsTreeString += '<li aria-expanded="true" class="branch">';
            layoutsTreeString += '</i><a href="#"><span class="badge add-margin-right-1x">' + (k + 1) + '</span>' + v.name + '</a>';
            layoutsTreeString += '<ul>';
            if (v.renderer !== null && typeof v.renderer === 'object') {
                layoutsTreeString += '<li aria-expanded="true" class="branch">';
                layoutsTreeString += '<a href="#">renderer</a>';
                layoutsTreeString += '<ul>';
                layoutsTreeString += '<li><b>path: </b><a title="Click to view" target="_blank" href="file://'+ v.renderer.path.absolute +'">'+ v.renderer.path.relative +'</a></li>';
            } else {
                layoutsTreeString += '<li><b>renderer: </b>' + v.renderer + '</li>';
            }
            layoutsTreeString += '</ul>';
            layoutsTreeString += '</li>';
        }
    });
    $('#layouts-tree').html(layoutsTreeString);
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

function populateThemes(treeData) {
    var themesTreeString = '';
    $.each(treeData, function (k, v) {
        if (v !== null && typeof v === 'object') {
            themesTreeString += '<li aria-expanded="true" class="branch">';
            themesTreeString += '</i><a href="#"><span class="badge add-margin-right-1x">' + (k + 1) + '</span>' + v.name + '</a>';
            themesTreeString += '<ul>';
            themesTreeString += '<li><b>path: </b>' + v.path + '</li>';
            themesTreeString += '</ul>';
            themesTreeString += '</li>';
        }
    });
    $('#themes-tree').html(themesTreeString);
    $('#themes-tree').tree_view();
}

function showProfiler() {
    $(".info-container").hide();
    $("#profiler").show();
    $("#main-header").text("Profiler");
}


requestUtil = new function () {
    this.makeRequest = function (type, url, data, callback) {
        var currentUrl = '';
        currentUrl = currentUrl.replace(/#[^#]*$/, "").replace(/\?[^\?]*$/, "").replace(/^https:/, "http:");
        var requestUrl = currentUrl + "api" + url;
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