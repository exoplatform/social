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
      IntranetNotification.dataLoadMore = IntranetNotification.portlet.find('#ShowMoreLoader');
      IntranetNotification.hasMore =  IntranetNotification.dataLoadMore.data('more');
      
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
      //
      if (IntranetNotification.hasMore) {
        $.ajaxSetup({
          beforeSend:function(){
            IntranetNotification.portlet.find('div.ShowAllIndicator').show();
          },
          complete:function(){
            IntranetNotification.portlet.find('div.ShowAllIndicator').hide();
          }
        });
      };
      $(window).scroll(function(e) {
        var loadAnimation = IntranetNotification.portlet.find('div.ShowAllIndicator'); 
        var isLoading = loadAnimation.css("display") != "none";
        if ((IntranetNotification.scrollBottom() <= IntranetNotification.delta) && !isLoading) {
      
          if (IntranetNotification.hasMore) {
            IntranetNotification.hasMore = false;
            //IntranetNotification.displayIndicator();
            $.ajax({
              url: IntranetNotification.dataLoadMore.data('url')
            }).done(function(data) {
              var html = data.context;
              if (html && html.length > 0) {
                IntranetNotification.popupItem.append($('<ul></ul>').html(html).find('li'));
                IntranetNotification.popupItem.find('li').each(function(i) {
                  IntranetNotification.applyAction($(this));
                });
          }
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
        IntranetNotification.markItemRead($(this).parents('li:first'))
                            .openURL($(this).data('link'));
      });
      //remove 'remove-icon'
      item.find('.remove-item').remove();

      item.find('.action-item').off('click')
          .on('click', function(evt) { evt.stopPropagation(); IntranetNotification.doAction($(this)); });
     item.find('.cancel-item').off('click')
            .on('click', function(evt) { evt.stopPropagation(); IntranetNotification.doCancelAction($(this)); });
      return item;
    },
    appendMessage : function(message) {
      console.log(message.body);
      var newItem = $($('<ul></ul>').html(message.body).html());
      var id = newItem.data('id');
      newItem.find('.remove-item').remove();
        //
      var existItem = IntranetNotification.popupItem.find('li[data-id=' + id + ']');
      var isExisting = existItem.length > 0;
      if (isExisting) {
        //this process only mentions case like or comment, 
        //the content must be updated and NotificationID still kept
        existItem.hide();
        existItem.replaceWith(newItem);
        IntranetNotification.showElm(IntranetNotification.applyAction(newItem));
      }
    },
    ajaxRequest : function (url, callBack) {
      if(url && url.length > 0) {
        $.ajax(url).done(function(data) {
          if(callBack && typeof callBack === 'function') {
            callBack(data);
          }
        });
      }
      
      return this;
    },
    openURL : function (url) {
      if(url && url.length > 0) {
        IntranetNotification.T = setTimeout(function() {
          clearTimeout(IntranetNotification.T);
          window.open(url, "_self");
        }, 500);
      }
      return this;
    },
    doAction : function(elm) {
      //call ajax to remove this notification, and do something in commons side
      //call rest on social side: for example accept/refuse relationship
      //remove this element on UI
      //redirect to the uri, for example: view activity detail
      IntranetNotification.ajaxRequest(elm.data('rest'), function(data) {IntranetNotification.appendMessage(data)})
                          .openURL(elm.data('link'));
    },
    doCancelAction : function(elm) {
      var id = elm.parents('li:first').data('id');
      IntranetNotification.ajaxRequest(elm.data('rest') + "/" + id)
                          .removeElm(elm.parents('li:first'));
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
      return this;
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
      return this;
    }
  };

  return IntranetNotification;
})(gj);
