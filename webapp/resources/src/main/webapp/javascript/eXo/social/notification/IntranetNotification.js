(function($) {
  var IntranetNotification = {
    popupItem : null,
    markReadLink : '',
    takeEventLink : '',
    delta : 65,    
    hasMore: false,
    resourceURL : "",
    portlet : null,
    scrollBottom : function() {
      return $(document).height() - $(window).scrollTop() - $(window).height();  
    },
    init : function(componentId, hasMore) {
      IntranetNotification.portlet = $('#' + componentId); 
      //
      IntranetNotification.markReadLink = IntranetNotification.portlet.find('#MarkRead').text();
      IntranetNotification.takeEventLink = IntranetNotification.portlet.find('#TakeEvent').text();
      //
      IntranetNotification.popupItem = IntranetNotification.portlet.find('ul.displayItems:first');
      IntranetNotification.popupItem.find('li').each(function(i) {
        IntranetNotification.applyAction($(this));
      });
      
      IntranetNotification.hasMore =  IntranetNotification.portlet.find('#ShowMoreLoader').data('more') === 'true';
      //
      IntranetNotification.initIndicator();
    },
    initIndicator : function() {
      IntranetNotification.portlet.find('div.ShowAllIndicator').remove();
      var activityIndicator = $('<div class="ShowAllIndicator" id="ShowAllIndicator" style="display:none"></div>');
      for (var i=1; i < 9; i++) {
        activityIndicator.append($('<div id="rotateG_0' + i + '" class="blockG"></div>'));
      }
      activityIndicator.appendTo(IntranetNotification.portlet);
      
      $(window).scroll(function(e) {
        if (IntranetNotification.scrollBottom() <= IntranetNotification.delta) {
          if (IntranetNotification.hasMore) {
            IntranetNotification.hasMore = false;
            $.ajax({
              url: IntranetNotification.dataLoadMore.data('url')
            }).done(function(data) {
              var html = data.context;
              IntranetNotification.popupItem.append($('<ul></ul>').html(html).find('li'));
              IntranetNotification.popupItem.find('li').each(function(i) {
                IntranetNotification.applyAction($(this));
              });
              //
              IntranetNotification.hasMore = data.hasMore;
            });
          }
        }
      });
    },
    applyAction : function(item) {
      item.find('.contentSmall:first').on('click', function(evt) {
        evt.stopPropagation();
        // mark read
        IntranetNotification.markItemRead($(this).parents('li:first'));
        //
        IntranetNotification.openURL($(this).data('link'));
      });
      //remove 'remove-icon'
      item.find('.remove-item').remove();

      item.find('.action-item').off('click')
          .on('click', function(evt) { evt.stopPropagation(); IntranetNotification.doAction($(this), IntranetNotification.takeEventLink); });
      return item;
    },
    ajaxRequest : function (url, callBack) {
      if(url && url.length > 0) {
        $.ajax(url).done(function(data) {
          if(callBack && typeof callBack === 'function') {
            callBack(data);
          }
        });
      }
    },
    openURL : function (url) {
      if(url && url.length > 0) {
        IntranetNotification.T = setTimeout(function() {
          clearTimeout(IntranetNotification.T);
          window.open(url, "_self");
        }, 500);
      }
    },
    doAction : function(elm, link) {
      //call ajax to remove this notification, and do something in commons side
      IntranetNotification.removeItem(elm.parents('li:first'), link);
      //call rest on social side: for example accept/refuse relationship
      IntranetNotification.ajaxRequest(elm.data('rest'));
      //remove this element on UI
      IntranetNotification.removeElm(elm.parents('li:first'));
      //redirect to the uri, for example: view activity detail
      IntranetNotification.openURL(elm.data('link'));
    },
    removeElm : function(elm) {
      elm.css('overflow', 'hidden').animate({
        height : '0px'
      }, 300, function() {
        if ($(this).parents('ul:first').find('li').length == 1) {
          IntranetNotification.portlet.find('li.no-items').show();
        }
        $(this).remove();
      });
    },
    showElm : function(elm) {
      elm.css({
        'visibility' : 'hidden',
        'overflow' : 'hidden'
      }).show();
      var h = elm.height();
      elm.css({
        'height' : '0px',
        'visibility' : 'visible'
      }).animate({
        'height' : h + 'px'
      }, 300, function() {
        $(this).css({
          'height' : '',
          'overflow' : 'hidden'
        });
      });
      return elm;
    },
    markItemRead : function(item) {
      var action = IntranetNotification.markReadLink + item.data('id');
      window.ajaxGet(action);
    },
    removeItem : function(item) {
      var action = IntranetNotification.takeEventLink + item.data('id');
      window.ajaxGet(action);
    }
  };

  return IntranetNotification;
})(gj);
