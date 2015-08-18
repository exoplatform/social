(function ($){
  var UIActivityLoader = {
    delta : 65,    
    hasMore: false,
    loaderButton: null,
    scrollBottom : function() {
      return $(document).height() - $(window).scrollTop() - $(window).height();  
    },
    init: function (parentId, hasMore) {
      var me = UIActivityLoader;
      me.hasMore = (hasMore === true || hasMore === 'true');
      me.loaderButton = $('#ActivitiesLoader');

      $(document).ready(function() {
        // check onLoad page.
        if(me.scrollBottom() <= me.delta) {
          $(window).scrollTop($(document).height() - $(window).height() - (me.delta+1));
        }
        $(window).scroll(function(e) {
          var distanceToBottom = me.scrollBottom();
          
          var loadAnimation = $('#UIActivitiesLoader').find('div.ActivityIndicator'); 
          if (me.hasMore === true &&
                distanceToBottom <= me.delta &&
                  loadAnimation.css("display") === 'none') {
            //
            var time = ($('.activityStream').length - me.loaderButton.data('loading-capacity') >= 0) ? 500 : 200;
            loadAnimation.css('visibility', 'hidden');
            loadAnimation.stop(true, true).fadeIn(time, function() {
              setTimeout(function(){loadAnimation.css('visibility', 'visible'); }, 800 - time);
              $('div.bottomContainer:last')[0].scrollIntoView(true);
              var action = me.loaderButton.data('action');
              window.ajaxGet(action, function(data) {
                $('div.ActivityIndicator').hide();
              });
            });
          }
        });
        //check if need to load more
        me.processLoadMore();
        //
        me.processBottomTimeLine();
      });
    },
    //check the distance between the last activity and the bottom of screen size 
    processLoadMore : function() {
      var mustLoadMore = ($(window).height() - $('div.bottomContainer:last').offset().top > 0);
      if (mustLoadMore === true) {
        var t = setTimeout(function() {
          UIActivityLoader.loaderButton.click();
        }, 200);
      }
    },
    setStatus : function(hasMore) {
      var me = UIActivityLoader;
      if(me.scrollBottom() <= me.delta) {
        $(window).scrollTop($(window).scrollTop()-5);
      }
      me.hasMore = (hasMore === true || hasMore === 'true');
      me.processBottomTimeLine();
    },
    processBottomTimeLine : function() {
      var me = UIActivityLoader;
      if (me.hasMore) {
        $('div.activityBottom').hide();
        //
        var isShow = $('.activityStream').length - me.loaderButton.data('loading-capacity') >= 0;
        if(isShow === true) {
          me.loaderButton.parent().show();
        }
      } else {
        $('div.activityBottom').show();
        me.loaderButton.parent().hide();
      }
    },
    loaddingActivity : function(id) {
      var container = $('#' + id);
      var url = container.find('div.uiActivitiesLoaderURL:first').data('url');
      container.find('div.activity-loadding').each(function(i) {
        var item = $(this);
        window.ajaxGet(url + item.attr('id'), function(data) {
          item.attr('style', '');
        });
      });
    }
  };
  return UIActivityLoader;
})($);