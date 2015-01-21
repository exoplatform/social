(function($, web) {
  var IntranetNotification = {
    popupItem : null,
    delta : 65,
    hasMore : false,
    portlet : null,
    scrollBottom : function() {
      return $(document).height() - $(window).scrollTop() - $(window).height();
    },
    init : function(componentId, hasMore) {
      var me = IntranetNotification;
      me.portlet = $('#' + componentId);
      //
      me.popupItem = me.portlet.find('ul.displayItems:first');
      me.popupItem.find('li').each(function(i) {
        me.applyAction($(this));
      });
      me.dataLoadMore = me.portlet.find('#ShowMoreLoader');
      me.hasMore = me.dataLoadMore.data('more');
      //
      me.initIndicator();
    },
    initIndicator : function() {
      var me = IntranetNotification;
      //
      if (me.hasMore) {
        $.ajaxSetup({
          beforeSend : function() {
            me.portlet.find('div.ShowAllIndicator:first').data('show', true).show();
          },
          complete : function() {
            me.portlet.find('div.ShowAllIndicator:first').data('show', false).hide();
          }
        });
      }
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
      var loadAnimation = me.portlet.find('div.ShowAllIndicator');
      var isLoading = loadAnimation.css("display") != "none";
      if ((me.scrollBottom() <= me.delta) && !isLoading) {
        if (me.hasMore) {
          me.hasMore = false;
          //
          $.ajax({
            url : me.dataLoadMore.data('url')
          }).done(function(data) {
            var html = data.context;
            if (html && html.length > 0) {
              me.popupItem.append($('<ul></ul>').html(html).find('li'));
              me.popupItem.find('li').each(function(i) {
                me.applyAction($(this));
              });
            }
            //
            me.hasMore = data.hasMore;
          });
        }
      }
    },
    applyAction : function(item) {
      var me = IntranetNotification;
      item.find('.contentSmall:first').on('click', function(evt) {
        evt.stopPropagation();
        // mark read
        web.markItemRead($(this).parents('li:first')).openURL($(this).data('link'));
      });
      // remove 'remove-icon'
      item.find('.remove-item').remove();
      // click action
      item.find('.action-item').off('click').on('click', function(evt) {
        evt.stopPropagation();
        web.doAction($(this));
      });
      // cancel action
      item.find('.cancel-item').off('click').on('click', function(evt) {
        evt.stopPropagation();
        var id = $(this).parents('li:first').data('id');
        web.doCancelAction(id, $(this).data('rest'));
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
      web.showElm(IntranetNotification.applyAction(newItem));
    },
    doCancelAction : function(object) {
      var elm = IntranetNotification.portlet.find('li[data-id=' + object.id + ']');
      web.removeElm(elm, function(elm) {
        var ul = elm.parents('ul:first');
        if (ul.find('li').length == 1) {
          ul.find('li.no-items').show();
        }
      });
    },
    markAllRead : function() {
      IntranetNotification.portlet.find('ul.displayItems:first').find('li.unread').removeClass('unread');
    }
  };
  //
  web.register(IntranetNotification);
  return IntranetNotification;
})(gj, webNotifications);
