(function($, webNotif) {
  var IntranetNotification = {
      portletId : 'UIIntranetNotificationsPortlet',
      popupItem : null,
      delta : 65,
      hasMore : false,
      portlet : null,
      scrollBottom : function() {
        return $(document).height() - $(window).scrollTop() - $(window).height();
      },
      init : function() {
        var me = IntranetNotification;
        me.portlet = $('#' + me.portletId);
        //
        me.popupItem = me.portlet.find('ul.displayItems:first');
        me.popupItem.find('li').each(function(i) {
          me.applyAction($(this));
        });
        me.dataLoadMore = me.portlet.find('#ShowMoreLoader');
        me.hasMore = me.dataLoadMore.data('more');
        me.removeNotifURL = me.portlet.find('#removeNotif').text();
        me.addNotifURL = me.portlet.find('#addNotif').text();
        // Do not load more when open/reload browser
        $(window).on('beforeunload', function(){
          $(window).scrollTop(0);
        });
        // Only run scroll to load more when the DOM is fully loaded.
        $(document).ready(function() {
          me.initIndicator();
        });
        //
        if(me.hasMore == false) {
          me.portlet.find('div.bottomContainer').hide();
        }
      },
      initIndicator : function() {
        var me = IntranetNotification;
        //
        $(window).scroll(function(e) {
          // wait 200ms before call load more.
          if (me.T) {
            clearTimeout(me.T);
          }
          me.T = setTimeout(me.processScroll, 200);
        });
      },
      processScroll : function() {
        var me = IntranetNotification;
        if (me.hasMore && (me.scrollBottom() <= me.delta)) {
          me.hasMore = false;
          var loadAnimation = me.portlet.find('div.loadingIndicator:first').show();
          //
          webNotif.ajaxReq(me.dataLoadMore.data('url'), function(data) {
            loadAnimation.hide();
            var html = data.context;
            if (html && html.length > 0) {
              me.popupItem.append($('<ul></ul>').html(html).find('li'));
              me.popupItem.find('li').each(function(i) {
                me.applyAction($(this));
              });
            }
            //
            me.hasMore = data.hasMore;
            if(me.hasMore == false) {
              me.portlet.find('div.bottomContainer').hide();
            }
          });
        }
      },
      applyAction : function(item) {
        var me = IntranetNotification;
        item.find('.contentSmall:first').on('click', function(evt) {
          evt.stopPropagation();
          // mark read
          webNotif.markItemRead($(this).parents('li:first')).openURL($(this).data('link'));
        });
        // remove 'remove-icon'
        item.find('.remove-item').remove();
        // click action
        item.find('.action-item').off('click').on('click', function(evt) {
          evt.stopPropagation();
          webNotif.doAction($(this));
        });
        // cancel action
        item.find('.cancel-item').off('click').on('click', function(evt) {
          evt.stopPropagation();
          var id = $(this).parents('li:first').data('id');
          webNotif.doCancelAction(id, $(this).data('rest'));
        });
        return item;
      },
      appendMessage : function(message) {
        var newItem = $($('<ul></ul>').html(message.body).html());
        var id = newItem.data('id');
        newItem.find('.remove-item').remove();
        //
        var existItem = IntranetNotification.popupItem.find('li[data-id=' + id + ']');
        var isExisting = existItem.length > 0;
        if (isExisting) {
          // this process only mentions case like or comment,
          // the content must be updated and NotificationID still kept
          existItem.hide();
          existItem.replaceWith(newItem);
        } else {
          IntranetNotification.popupItem.prepend(newItem.hide());
        }
        webNotif.showElm(IntranetNotification.applyAction(newItem));
        //
        webNotif.ajaxReq(IntranetNotification.addNotifURL);
        //
        IntranetNotification.popupItem.find('li.no-items').hide();
      },
      doCancelAction : function(object) {
        var elm = IntranetNotification.portlet.find('li[data-id=' + object.id + ']');
        webNotif.removeElm(elm, function(elm) {
          var ul = elm.parents('ul:first');
          if (ul.find('li').length == 2) {
            ul.find('li.no-items').show();
          }
        });
        //
        webNotif.ajaxReq(IntranetNotification.removeNotifURL);
      },
      markAllRead : function() {
        IntranetNotification.portlet.find('ul.displayItems:first').find('li.unread').removeClass('unread');
      }
  };
  //
  webNotif.register(IntranetNotification);
  return IntranetNotification;
})(gj, webNotifications);
