(function($) {
  var IntranetNotification = {
    popupItem : null,
    markReadLink : '',
    removeLink : '',
    delta : 65,    
    hasMore: false,
    resourceURL : "",
    scrollBottom : function() {
      return $(document).height() - $(window).scrollTop() - $(window).height();  
    },
    init : function(componentId, hasMore) {
      var portlet = $('#' + componentId); 
      portlet.find('span.remove-item').remove();
      //
      IntranetNotification.markReadLink = portlet.find('#MarkRead').text();
      IntranetNotification.removeLink = portlet.find('#Remove').text();
      //
      IntranetNotification.popupItem = portlet.find('ul.displayItems:first');
      IntranetNotification.popupItem.find('li').each(function(i) {
        IntranetNotification.applyAction($(this));
      });
      
      IntranetNotification.dataLoadMore =  portlet.find('#ShowMoreLoader').hide();
      //
      IntranetNotification.initIndicator();
    },
    initIndicator : function() {
      $('#UIIntranetNotificationsPortlet').find('div.ShowAllIndicator').remove();
      var activityIndicator = $('<div class="ShowAllIndicator" id="ShowAllIndicator" style="display:none"></div>');
      for (var i=1; i < 9; i++) {
        activityIndicator.append($('<div id="rotateG_0' + i + '" class="blockG"></div>'));
      }
      activityIndicator.appendTo('#UIIntranetNotificationsPortlet');
      
      $(window).scroll(function(e) {
        //
        if(IntranetNotification.scrollBottom() <= IntranetNotification.delta) {
          // check status
          if(IntranetNotification.dataLoadMore.data('more') == true) {
            //
            IntranetNotification.dataLoadMore.show();
            // call ajax
            $.ajax({
              url: IntranetNotification.dataLoadMore.data('url')
            }).done(function(data) {
              var html = data.context;
              IntranetNotification.popupItem.append($('<ul></ul>').html(html).find('li'));
              
              IntranetNotification.popupItem.find('li').each(function(i) {
                IntranetNotification.applyAction($(this));
              });
              
              //
              IntranetNotification.dataLoadMore.hide().data('more', data.hasMore);
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
