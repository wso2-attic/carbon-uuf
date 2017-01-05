$( document ).ready(function() {

    var dataUrl = "https://api.myjson.com/bins/b2r97";
    $.ajax({
        type: "GET",
        url: dataUrl,
        success: function(data){
            $(".preloader").preloader("hide");
            initTimeline(data);
        },
        beforeSend: function (request) {
            $(".preloader").preloader("show");
        }
    });

    function initTimeline(dataSet){

        var container = $('#visualization').get(0);
        var groups = new vis.DataSet(dataSet.groups);
        var items = new vis.DataSet(dataSet.items);


        var options = {
            start: '0000',
            end: '2100',
            min: '0000',
            //max: '2000',
            selectable: false,
            stack:true,
            showCurrentTime:false,
            //timeAxis: {scale: 'millisecond'},
            horizontalScroll:false,
            verticalScroll:false,
            orientation:{
                axis: 'top',
                item:'bottom'
            },
            moment: function(date) {
                var d = vis.moment(date).utc();
                return d;
            },
            order:function(a,b){
                return a.id - b.id;
            },
            template: function (item) {
                var html = '<div id="'+item.id+'" class="custom-vis-item-content" data-pop-title="'+ item.content +
                            '" data-pop-content-start="'+ item.popoverContent.start +
                            '" data-pop-content-end="'+ item.popoverContent.end + '"></div>';
                return html;

            },
            groupTemplate: function (item) {
                var html = '<div class="left-label" data-toggle="tooltip" data-placement="right" title="' +item.title+
                           '">' +item.content+ '</div><div class="right-label">' +item.duration+ ' ms</div>';
                return html;
            },
            onUpdate: function (item, callback) {
                //console.log(item);
            },
            format: {
                minorLabels: {
                    millisecond:'SSS',
                    second:'s'
                },
                majorLabels: {
                    millisecond:'HH:mm:ss',
                    second:'D MMMM HH:mm'
                }
            }
        };

        var timeline = new vis.Timeline(container);
        timeline.setOptions(options);
        timeline.setGroups(groups);
        timeline.setItems(items);


        $.each(dataSet.items, function( k, v ) {
            var lightenColor = ColorLuminance('#446CB3',v.childLevel/10);
            var colorElem = $("#"+ k).parent().parent().parent();
            if(v.childLevel != "-1"){
                $(colorElem).css("background", lightenColor);
            }
        });

        var pop;
        timeline.on('itemover', function(properties) {
            var popContentStart = $(properties.event.target).data("pop-content-start");
            var popContentEnd = $(properties.event.target).data("pop-content-end");
            var popContent = '<div class="table-responsive"><table class="table"><tbody><tr><td><strong>Start</strong></td><td>'+ popContentStart +
                             '</td></tr><tr><td><strong>End</strong></td><td>'+ popContentEnd +'</td></tr></tbody></table></div>';
            pop = $(properties.event.target);
            var popOverSettings = {
                placement: 'auto top',
                container: '#visualization',
                html: true,
                trigger: 'hover',
                content: function () {
                    return popContent;
                }
            }
            $(pop).popover(popOverSettings);
            setTimeout(function(){
                $(pop).popover('show');
            }, 500);
        });

        timeline.on('rangechange', function(properties) {
            $(pop).each(function(i,e) {
                $(e).popover('hide');
            });
        });

        timeline.on('changed', function() {
            var p = $(".vis-panel.vis-center");
            var position = p.position();
            $(".vis-panel.vis-top").before('<div class="time-lbl" style="left:' + (position.left-50) + 'px;top:' + (position.top-24) + 'px;">Time</div>');
        });

    }


    $('[data-toggle="tooltip"]').tooltip({
        container: 'body'
    });

    function ColorLuminance(hex, lum) {
        // validate hex string
        hex = String(hex).replace(/[^0-9a-f]/gi, '');
        if (hex.length < 6) {
            hex = hex[0]+hex[0]+hex[1]+hex[1]+hex[2]+hex[2];
        }
        lum = lum || 0;
        // convert to decimal and change luminosity
        var rgb = "#", c, i;
        for (i = 0; i < 3; i++) {
            c = parseInt(hex.substr(i*2,2), 16);
            c = Math.round(Math.min(Math.max(0, c + (c * lum)), 255)).toString(16);
            rgb += ("00"+c).substr(c.length);
        }
        return rgb;
    }


});