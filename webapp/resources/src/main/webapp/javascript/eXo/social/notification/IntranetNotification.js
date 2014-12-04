(function($) {
  var IntranetNotification = {
    popupItem : null,
    markReadLink : '',
    removeLink : '',
    init : function(componentId) {
      $('#' + componentId).find('span.remove-item').remove();
      //
      IntranetNotification.markReadLink = $('#' + componentId)
          .find('#MarkRead').text();
      IntranetNotification.removeLink = $('#' + componentId).find('#Remove')
          .text();
      //
      IntranetNotification.popupItem = $('#' + componentId).find(
          'ul.displayItems:first');
      IntranetNotification.popupItem.find('li').each(function(i) {
        IntranetNotification.applyAction($(this));
      });
    },
    applyAction : function(item) {
      item.find('.contentSmall:first').on('click', function(evt) {
        evt.stopPropagation();
        // mark read
        IntranetNotification.markItemRead($(this).parents('li:first'));
        //
        var link = $(this).data('link');
        if (link && link.length > 0) {
          window.location.href = link;
        }
      }).find('a').click(function(evt) {
        evt.stopPropagation();
        var href = $(this).attr('href');
        if (href && href.indexOf('javascript') !== 0) {
          window.location.href = href;
        }
      });
      //
      item.find('.remove-item').off('click').on('click', function(evt) {
        evt.stopPropagation();
        //
        var elm = $(this);
        IntranetNotification.removeItem(elm.parents('li:first'));
        //
        var rest = elm.data('rest');
        if (rest && rest.length > 0) {
          $.ajax(rest);
        }
        //
        var link = elm.data('link');
        if (link && link.length > 0) {
          window.location.href = link;
        }
        //
        IntranetNotification.removeElm(elm.parents('li:first'));
      });
      //
      return item;
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
      var action = IntranetNotification.removeLink + item.data('id');
      window.ajaxGet(action);
    }
  };

  return IntranetNotification;
})(gj);
