(function ($){
  var UIActivityLoader = {
    delta : 65,
    responsiveId : null,
    numberOfReqsPerSec : 10,//Perfect range: 5 -> 20
    hasMore: false,
    parentContainer : $('#UIActivitiesLoader'),
    scrolling: false,
    scrollingTimeout: null,
    readingCheckDelayInSeconds: 2,
    markAsReadTimeInSeconds: 3,
    monitoredActivities: [],
    visibleActivities: [],
    readActivities: [],
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
          UIActivityLoader.findActivitiesToMonitor();
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

      me.initCheckRead();
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
    },
    initCheckRead: function() {
      UIActivityLoader.findActivitiesToMonitor();

      // Scroll handler
      window.removeEventListener('scroll', UIActivityLoader.scrollHandler);
      window.addEventListener('scroll', UIActivityLoader.scrollHandler);

      // visibility check interval
      const readingCheckInterval = setInterval(function() {
        UIActivityLoader.monitoredActivities.forEach(function(element) {
          if(UIActivityLoader.isInViewport(element)) {
            // The element is visible
            const activityContainer = element.closest('.uiNewsActivity');
            if(activityContainer) {
              const foundElement = UIActivityLoader.visibleActivities.find(visibleElement => visibleElement.id === activityContainer.id);
              if(foundElement) {
                var now = new Date().getTime();
                var readTime = ((now-foundElement.time)/1000).toFixed(1);
                if(readTime > UIActivityLoader.markAsReadTimeInSeconds) {
                  let activityId = foundElement.id.substring('activityContainer'.length, foundElement.id.length);
                  UIActivityLoader.markActivityAsRead(activityId);
                }
              }
            }
          } else {
            // The element is not visible
            // Remove it from the visibleActivities array if it's there
            UIActivityLoader.visibleActivities = UIActivityLoader.visibleActivities.filter(visibleElement => visibleElement.id !== element.id);
          }
        });
      }, UIActivityLoader.readingCheckDelayInSeconds * 1000);
    },
    findActivitiesToMonitor: function() {
      UIActivityLoader.monitoredActivities = document.querySelectorAll('.uiActivityLoader .uiNewsActivity .description');
    },
    scrollHandler: function() {
      UIActivityLoader.scrolling = true;

      clearTimeout(UIActivityLoader.scrollingTimeout);
      UIActivityLoader.scrollingTimeout = setTimeout(function() {
        UIActivityLoader.scrolling = false;

        // User stopped scrolling, check all element for visibility
        UIActivityLoader.monitoredActivities.forEach(function(element) {
          if(UIActivityLoader.isInViewport(element)) {

            // Check if it's already logged in the visibleActivities array
            const activityContainer = element.closest('.uiNewsActivity');
            if(activityContainer) {
              var found = UIActivityLoader.visibleActivities.some(visibleElement => visibleElement.id === activityContainer.id);

              if(!found){
                // Push an object with the visible element id and the actual time
                UIActivityLoader.visibleActivities.push({id: activityContainer.id, time: new Date().getTime()});
              }
            }
          }
        });
      }, 200);
    },
    isInViewport: function(element) {
      if(document.hidden) {
        return false;
      }

      var rect = element.getBoundingClientRect();

      return (
        rect.top >= 0 &&
        rect.left >= 0 &&
        rect.bottom <= (window.innerHeight || document.documentElement.clientHeight) &&
        rect.right <= (window.innerWidth || document.documentElement.clientWidth)
      );
    },
    markActivityAsRead: function(activityId) {
      if(this.readActivities.includes(activityId)) {
        return;
      } else {
        this.readActivities.push(activityId);
      }

      const activity = {
        id: activityId,
        type: 'news',
        read: true
      };
      fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/activities/${activityId}`, {
          headers: {
            'Content-Type': 'application/json'
          },
          credentials: 'include',
          method: 'PUT',
          body: JSON.stringify(activity)
        });
    }
  };
  return UIActivityLoader;
})($);