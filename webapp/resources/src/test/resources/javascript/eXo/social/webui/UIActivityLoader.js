(function ($){
  var UIActivityLoader = {
    delta : 65,
    responsiveId : null,
    numberOfReqsPerSec : 10,//Perfect range: 5 -> 20
    hasMore: false,
    parentContainer : $('#UIActivitiesLoader'),
    scrollBottom : function() {
    return $(document).height() - $(window).scrollTop() - $(window).height();  
    },
    init: function (parentId, hasMore) {
      UIActivityLoader.hasMore = (hasMore === true || hasMore === 'true') ? true : false;
      UIActivityLoader.initIndicator();
  
      $(document).ready(function() {
        // check onLoad page.
        if(UIActivityLoader.scrollBottom() <= UIActivityLoader.delta) {
          $(window).scrollTop($(document).height() - $(window).height() - (UIActivityLoader.delta+1));
        }
        UIActivityLoader.processBottomTimeLine();
      });
  
    },
    setStatus : function(hasMore) {
      var me = UIActivityLoader;
      if(me.scrollBottom() <= me.delta) {
        $(window).scrollTop($(window).scrollTop()-5);
      }
      me.hasMore = (hasMore === true || hasMore === 'true');
      UIActivityLoader.initIndicator();
      me.processBottomTimeLine();
      
    },
    initIndicator : function() {
      $('#UIActivitiesLoader').find('div.ActivityIndicator').remove();
      var activityIndicator = $('<div class="ActivityIndicator" id="ActivityIndicator" style="display:none"></div>');
      for (var i=1; i < 9; i++) {
        activityIndicator.append($('<div id="rotateG_0' + i + '" class="blockG"></div>'));
      }
      activityIndicator.appendTo('#UIActivitiesLoader');
    },
    processBottomTimeLine : function() {
      //
      var me = UIActivityLoader;
      var loaderButton = $('#ActivitiesLoader');
      if ( me.hasMore ) {
        $('div.activityBottom').hide();
        loaderButton.parent().show();
      } else {
        $('div.activityBottom').show();
        loaderButton.parent().hide();
      }
    },
    renderActivity : function(activityItem) {
      var url = activityItem.data('url');
      if (url && url.indexOf('objectId') > 0) {
        url += activityItem.attr('id') + ((UIActivityLoader.getRequestParam().length > 0) ? UIActivityLoader.getRequestParam() : "");
        window.ajaxGet(url, function(data) {
          activityItem.attr('style', '').removeClass('activity-loadding');
          if (UIActivityLoader.responsiveId) {
            eXo.social.SocialUtil.onViewActivity(UIActivityLoader.responsiveId);
          }
        });
      }
    },
    getRequestParam : function() {
      var me = UIActivityLoader;
      if (me.requestParams === undefined || me.requestParams === null) {
        var h = window.location.href;
        if(h.indexOf('?') > 0 && h.indexOf('portal:componentId') < 0) {
          me.requestParams = '&' + h.substring(h.indexOf('?') + 1);
        } else {
          me.requestParams = "";
        }
      }
      return me.requestParams;
    },
    loadingActivities : function(id, numberOfReqsPerSec) {
      var me = UIActivityLoader;
      if (numberOfReqsPerSec === undefined) {
        numberOfReqsPerSec = me.numberOfReqsPerSec;
      }
      me.requestParams = null;
      var container = $('#' + id);
      var url = container.find('div.uiActivitiesLoaderURL:first').data('url');
      if (url === undefined || url.length === 0) {
        return;
      }
      var items = container.find('div.activity-loadding').data('url', url);
      var batchDelay = 1000;// 1000ms ~ 1s
      me.renderActivity(items.eq(0));
      var index = 1;
      var interval = window.setInterval(function() {
        if (index < items.length) {
          me.renderActivity(items.eq(index));
        } else {
          window.clearInterval(interval);
        }
        ++index;
      }, batchDelay / numberOfReqsPerSec);
    },
    addTop : function(activityItemId, responsiveId) {
      UIActivityLoader.responsiveId = responsiveId;
      var parentContainer = $('#UIActivitiesLoader');
      var activityContainer = parentContainer.find('div.uiActivitiesContainer:first');
      if($('#welcomeActivity').length === 0) {
        var url = activityContainer.find('div.uiActivitiesLoaderURL:first').data('url');
        if(activityContainer.find('#' + activityItemId).length <= 0 && url && url.length > 0) {
          var activityItem = $('<div class="uiActivityLoader activity-loadding" style="position:relative;" id="' + activityItemId + '"></div>');
          activityItem.data('url', url);
          //
          activityContainer.prepend(activityItem);
          UIActivityLoader.requestParams = null;
          if(UIActivityLoader.hasMore) {
            //Remove last
            parentContainer.find('.uiActivitiesContainer:last').find('.uiActivityLoader:last').remove();
          }
          //
          UIActivityLoader.renderActivity(activityItem);
        }
      }
    }
  };
  return UIActivityLoader;
})($);