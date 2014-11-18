(function (eXo, $, uiMaskLayer, popupWindow) {
  
  if(!String.prototype.trim) {
    String.prototype.trim = function () {
      return this.replace(/^\s+|\s+$/g,'');
    };
  }
  if (!Array.prototype.indexOf) {
    Array.prototype.indexOf = function(searchElement) {
      if (this == null) {
        throw new TypeError();
      }
      return $.inArray(searchElement, this);
    };
  }

  eXo.social = eXo.social || {};
  
  if (eXo.env) {
    var portal = eXo.env.portal
    
    eXo.social.portal = {
      rest : (portal.rest) ? portal.rest : 'rest',
      portalName : (portal.portalName) ? portal.portalName : 'intranet',
      context : (portal.context) ? portal.context : '/portal',
      accessMode : (portal.accessMode) ? portal.accessMode : 'public',
      userName : (portal.userName) ? portal.userName : ''
    };
  }
  eXo.social.I18n = eXo.social.I18n || {};
  eXo.social.I18n.mentions = eXo.social.I18n.mentions || {
    helpSearch: 'Type to start searching for users.',
    searching: 'Searching for ',
    foundNoMatch : 'Found no matching users for '
  };

  
  var SocialUtils = {
    /**
     * Constants
     */
    ADDED_MARGIN_BOTTOM : 10,
    ITEM_BOX_MAX_WIDTH : 380,
    ITEM_BOX_MIN_WIDTH : 300,
    
    currentBrowseWidth : 0,
    onResizeWidth : new Array(),
    upFreeSpace : new Array(),
    dynamicItems : new Array(),

    /**
     * Browsers for checking
     */
    dataBrowser : [
            {
                string: navigator.userAgent,
                subString: "Chrome",
                identity: "Chrome"
            },
            { string: navigator.userAgent,
                subString: "OmniWeb",
                versionSearch: "OmniWeb/",
                identity: "OmniWeb"
            },
            {
                string: navigator.vendor,
                subString: "Apple",
                identity: "Safari",
                versionSearch: "Version"
            },
            {
                prop: window.opera,
                identity: "Opera"
            },
            {
                string: navigator.vendor,
                subString: "iCab",
                identity: "iCab"
            },
            {
                string: navigator.vendor,
                subString: "KDE",
                identity: "Konqueror"
            },
            {
                string: navigator.userAgent,
                subString: "Firefox",
                identity: "Firefox"
            },
            {
                string: navigator.vendor,
                subString: "Camino",
                identity: "Camino"
            },
            {       // for newer Netscapes (6+)
                string: navigator.userAgent,
                subString: "Netscape",
                identity: "Netscape"
            },
            {
                string: navigator.userAgent,
                subString: "MSIE",
                identity: "Explorer",
                versionSearch: "MSIE"
            },
            {
                string: navigator.userAgent,
                subString: "Gecko",
                identity: "Mozilla",
                versionSearch: "rv"
            },
            {       // for older Netscapes (4-)
                string: navigator.userAgent,
                subString: "Mozilla",
                identity: "Netscape",
                versionSearch: "Mozilla"
            }
    ],
    
    /**
     * Adjust height belong to browser
     */
    adjustHeight : function(contentContainer) {
      var browser = this.getCurrentBrowser();
      
      if (browser != null) {
        if ((browser == "Safari")   || (browser == "Chrome")) {
            gadgets.window.adjustHeight(contentContainer.offsetHeight + this.ADDED_MARGIN_BOTTOM);
        } else {
            gadgets.window.adjustHeight();
        }
      } else {
        gadgets.window.adjustHeight();
      }
    },
    
    applyConfirmPopup : function(id) { 
      $('#' + id).find('.confirmPopup').on('click', function() {
          var thizz = $(this);
          var action_ = thizz.attr('data-onclick'); 
          var label_ = thizz.attr('data-labelAction') || 'OK';
          var close_ = thizz.attr('data-labelClose') || 'Close'; 
          var title_ = thizz.attr('data-title') || 'Confirmation';
          var message_ = thizz.attr('data-message');
          eXo.social.PopupConfirmation.confirm('demo', [{action: action_, label : label_}], title_, message_, close_);
        }); 
      },
      
    feedbackMessagePopup : function(title, message, closeLabel) { 
      var popup = PopupConfirmation.makeTemplate();
      popup.find('.popupTitle').html(title);
      message = message.replace("${simpleQuote}", "'");
      popup.find('.contentMessage').removeClass('confirmationIcon').addClass('infoIcon').html(message);
      var uiAction = popup.find('.uiAction');
      uiAction.append(PopupConfirmation.addAction(null, closeLabel));
      //
      PopupConfirmation.show(popup);
     },
     
     alertEvent : function(comId) {
	 	var socketUrl = 'ws://' + location.hostname + ':8181/channels/notification-web/' + window.eXo.env.portal.userName;
	 	var socket = new WebSocket(socketUrl);
	 	
	 	socket.onmessage = function(evt) {
	 		var obj = JSON.parse(evt.data);
	 		SocialUtils.updateNotificationList(comId, obj.message);
		}
	 	
	 	socket.onopen = function(evt) {
	 		if (socket.readyState == WebSocket.OPEN) {
	            socket.send('{"action": "subscribe", "identifier" : "notification-web"}');
	        } else {
	            alert("The socket is not open.");
	        }
		}
	 	
	 	socket.onclose = function(evt) {
	 		alert("Web Socket closed.");
		}
     },
     updateNotificationList : function(parentId, message) { 
         var msgEl = $('#feedbackmessageInline');

         if(msgEl.length === 0) {
           msgEl = $('<div id="feedbackMessageInline">' +
                     '  <span class="message"></span>' +
                     '</div>');
           msgEl.prependTo($('#'+ parentId));
         }

         if($(window).scrollTop() > msgEl.offset().top) {
           msgEl[0].scrollIntoView(true);
         }
         msgEl.stop().hide().find("span.message").html(message);
         msgEl.show('fast').delay(450000).hide('slow');
       },
     
     feedbackMessageInline : function(parentId, message) { 
       message = message.replace("${simpleQuote}", "'");

       var msgEl = $('#feedbackmessageInline');

       if(msgEl.length === 0) {
         msgEl = $('<div id="feedbackMessageInline" class="alert alert-success">' +
                   '  <i class="uiIconSuccess"></i><span class="message"></span>' +
                   '</div>');
         //
         msgEl.prependTo($('#'+ parentId));
       }

       if($(window).scrollTop() > msgEl.offset().top) {
         msgEl[0].scrollIntoView(true);
       }
       msgEl.stop().hide().find("span.message").text(message);
       msgEl.show('fast').delay(4500).hide('slow');
     },

    /**
     * Get current Browser
     */
    getCurrentBrowser : function() {
        function searchString(data) {
            for (var i=0;i<data.length;i++) {
                var dataString = data[i].string;
                var dataProp = data[i].prop;
                this.versionSearchString = data[i].versionSearch || data[i].identity;
                if (dataString) {
                    if (dataString.indexOf(data[i].subString) != -1)
                        return data[i].identity;
                }
                else if (dataProp)
                    return data[i].identity;
            }
        }
        
        var browser = searchString(this.dataBrowser) || null; 
        
        return browser;
    },
    setCookies : function(name, value, expiredays) {
      var exdate = new Date();
      exdate.setDate(exdate.getDate() + expiredays);
      expiredays = ((expiredays == null) ? "" : ";expires=" + exdate.toGMTString());
      var path = ';path=/portal';
      document.cookie = name + "=" + escape(value) + expiredays + path;
    },
    updateRelationship : function(identityId) {
      var identityBox = $('#identity' + identityId);
      var relationshipInfo = $('#UIUpdateRelationship > div');
      if (relationshipInfo.length > 0) {
        //
        identityBox.find('span.statusLabel:first').text(relationshipInfo.data('status'));
        var actionLabel = identityBox.find('button.actionLabel:first');
        actionLabel.attr('onclick', relationshipInfo.data('action'));
        actionLabel.attr('class', 'btn ' + relationshipInfo.data('bt-class') + ' pull-right actionLabel');
        actionLabel.text(relationshipInfo.text());
        var clazz = relationshipInfo.data('class');
        if(clazz.length > 0) {
          identityBox.addClass(clazz);
        } else {
          identityBox.removeClass('checkedBox');
        }
      }
      identityBox.find('button.btn-confirm:first').remove();
    },
    addOnResizeWidth : function(callback) {
      if (callback && String(typeof callback) === "function") {
        var name = String(callback.name + new Date().getTime());
        SocialUtils.onResizeWidth[name] = callback;
      }
    },
    addDynamicItemLayout : function(comId) {
      if (comId && String(typeof comId) === "string") {
        if(SocialUtils.dynamicItems.indexOf(comId) < 0) {
          SocialUtils.dynamicItems.push(comId);
        }
        SocialUtils.dynamicItemLayout(comId);
      }
    },
    onResizeDynamicItemLayout : function() {
      var dynamicItems = SocialUtils.dynamicItems;
      $.each(dynamicItems, function( index, comId ) {
        SocialUtils.dynamicItemLayout(comId);
      });
    },
    limitTextLine : function(item) {
      if(item.length > 0) {
        var lineNumbers = parseInt(item.attr('data-line'));
        if(lineNumbers) {
          var originalText = item.attr('data-text') || item.text().trim();
          item.attr('data-text', originalText);
          item.text(originalText);
          var wText = item.attr('data-width-text') || SocialUtils.getTextWidth(originalText);
          item.attr('data-width-text', wText);
          //
          var originalSize = originalText.length;
          if(originalText.indexOf(' ') < 0) {
            item.css({'word-break': 'break-word', 'word-wrap':'break-word'});
          }
          //
          var key = item.attr('data-key') + lineNumbers;
          var maxSize = eXo.social.DATA_LIMIT_TEXT[key] || 0;
          var wTextOk = eXo.social.DATA_LIMIT_TEXT[key + 'previousWidth'] || 0;
          var threeDots = '...';
          if (maxSize > 0) {
            if (originalSize > maxSize) {
              item.text(originalText.substring(0, maxSize - threeDots.length) + threeDots);
            }
          } else if (wText > wTextOk) {
            // set default height for text container.
            item.css({
              'margin-top' : '0px', 'margin-bottom' : '0px',
              'padding-bottom' : '0px', 'padding-top' : '0px',
              'height' : 'auto', 'min-height' : '0px'
            });
            var decreaseSize = originalSize, maxLineHeight = (parseInt(item.css('line-height')) + 1) * lineNumbers;
            while (parseInt(item.height()) > maxLineHeight) {
              --decreaseSize;
              item.text(originalText.substring(0, decreaseSize - threeDots.length) + threeDots);
            }
            if (decreaseSize < originalSize) {
              eXo.social.DATA_LIMIT_TEXT[key] = decreaseSize;
            }
            //
            eXo.social.DATA_LIMIT_TEXT[key + 'previousWidth'] = SocialUtils.getTextWidth(item.text());
            // reset CSS height style for text container
            item.css({
              'margin-top' : '', 'margin-bottom' : '',
              'padding-bottom' : '', 'padding-top' : '',
              'height' : '', 'min-height' : ''
            });
          }
        }
      }
    },
    getTextWidth : function(text) {
      var jtext = $('body').find('> .sampleText');
      if (jtext.length == 0) {
        jtext = $('<div class="sampleText" style="display:inline-block;visibility:hidden"></div>');
        jtext.appendTo($('body'));
      }
      jtext.text(text);
      return jtext.width();
    },
    dynamicItemLayout : function(comId) {
      var container = $('#'+comId);
      if(container.length === 0) {
        return;
      }
      var listContainer = container.find('div.itemList:first');
      var widthContainer = listContainer.css('margin-right', '').width();
      listContainer.css({'margin-right': '-10px'});
      //
      var listBoxs = listContainer.find('div.itemContainer');
      var maxItemInline = parseInt(widthContainer / SocialUtils.ITEM_BOX_MIN_WIDTH);
      var minItemInline = parseInt(widthContainer / SocialUtils.ITEM_BOX_MAX_WIDTH);
      var width = 0;
      //
      maxItemInline = Math.min(maxItemInline, listBoxs.length);
      if(maxItemInline === minItemInline || minItemInline >= listBoxs.length) {
        width = SocialUtils.ITEM_BOX_MAX_WIDTH;
      } else {
        width = SocialUtils.ITEM_BOX_MIN_WIDTH;
      }
      //
      var delta = (widthContainer - (width * maxItemInline))/maxItemInline;
      width += delta;
      //
      var d = (listBoxs.length > 3) ? 10/maxItemInline : 1.5;
      listBoxs.each(function(index) {
        if((index + 1) % maxItemInline === 0) {
          $(this).width(parseInt(width + d - 10)).find('.spaceBox:first').css({'margin-right': '0px'});
        } else {
          $(this).width(parseInt(width + d)).find('.spaceBox:first').css({'margin-right': '10px'});
        }
      });
      
      var execute = $('#execute');
      if(execute.length === 0) {
        execute = $('<div id="execute" style="display:none"></div>');
        $('body').append(execute);
        execute.on('execute', function(evt) {
          eXo.social.DATA_LIMIT_TEXT = [];
          $('body').find('.limitText').each(function(index) {SocialUtils.limitTextLine($(this));});
        });
      }
      if(window.T) {
        window.clearTimeout(window.T);
      }
      window.T = window.setTimeout(function() {
        //
        $('#execute').trigger('execute');
        window.clearTimeout(window.T);
        window.T = null;
      },50);
      
      //
      var moreButton = container.find('.load-more-items:first');
      if(moreButton.length > 0) {
        moreButton.css({'width' : (width*2 + d) + 'px', 'margin' : 'auto', 'display':'block'})
      }
    },
    addfillUpFreeSpace : function(comId) {
      if (comId && String(typeof comId) === "string") {
        if(SocialUtils.upFreeSpace.indexOf(comId) < 0) {
          SocialUtils.upFreeSpace.push(comId);
        }
        SocialUtils.fillUpFreeSpace(comId);
      }
    },
    onResizeFillUpFreeSpace : function() {
      var upFreeSpaces = SocialUtils.upFreeSpace;
      $.each(upFreeSpaces, function( index, comId ) {
        SocialUtils.fillUpFreeSpace(comId);
      });
    },
    fillUpFreeSpace : function(comId) {
      var container = $('#'+comId);
      if(container.length > 0) {
        var windowH = $(window).height();
        //
        container.height('');
        var topH = 0;
        var top = $('#NavigationPortlet');
        if(top.length > 0) {
          topH = top.height();
        }
        var wH = windowH - topH;
        var tdLeftNavi = $('.LeftNavigationTDContainer:first').css('height', wH);
        if(tdLeftNavi.find('div:first').height()  > wH) {
          tdLeftNavi.height('');
        }
        //
        var parent = container.parents('td.RightBodyTDContainer:first').css('position', 'relative');
        parent.append($('<div class="max-width-fake" style="bottom:0px; width:1px; position:absolute"></div>'));
        
        var fake = parent.find('.max-width-fake:first').css('top', parent.find('div:first').outerHeight());
        var fakeH = fake.height();
        if(fakeH > 2) {
          container.height(container.height() + fakeH - 5);
        }
        fake.remove();
        parent.css('position', '');
      }
    }
  };

  PopupConfirmation = {
    actions : [],
    title : '',
    message : '',

    makeTemplate : function() {
      $('#UISocialPopupConfirmation').remove();
      var popup = $('.UISocialConfirmation:first').clone();
      popup.attr('id', 'UISocialPopupConfirmation');
      popup.find('.uiIconClose:first').on('click', PopupConfirmation.hiden);
      return popup;
    },
  
    confirm : function(id, actions, title, message, closeLabel) {
      SocialUtils.setCookies('currentConfirm', id, 300);
      var popup = PopupConfirmation.makeTemplate();
      popup.find('.popupTitle').html(title);
      popup.find('.contentMessage').html(message);
      var uiAction = popup.find('.uiAction');
      for ( var i = 0; i < actions.length; ++i) {
        uiAction.append(PopupConfirmation.addAction(actions[i].action, actions[i].label));
      }
      uiAction.append(PopupConfirmation.addAction(null, closeLabel));
  
      //
      PopupConfirmation.show(popup);
    },
  
    addAction : function(action, label) {
      var btn = $('<a href="javascript:void(0);" class="btn">' + label + '</a>');
      btn.on('click', PopupConfirmation.hiden);
      if (typeof action === 'function') {
        btn.on('mouseup', action);
        btn.html(label);
      } else if (action !== null) {
        btn.attr('onclick', action)
      }
      return btn;
    },
  
    show : function(popup) {
      $('#UIPortalApplication').append(popup);
      popup.css({
        height : 'auto',
        width : '400px',
        visibility : 'hidden',
        display : 'block'
      });
      var pHeight = popup.height();
      var top = ($(window).height() - pHeight) / 2 - 30;
      top = ((top > 10) ? top : 10) + $(window).scrollTop();
      var left = ($(window).width() - popup.width()) / 2;
      popup.css({
        'top' : top + 'px',
        'left' : left + 'px',
        'visibility' : 'visible',
        'overflow' : 'hidden'
      });
      popup.animate({ height : pHeight + 'px' }, 500, function() { });
      uiMaskLayer.createMask(popup[0].parentNode, popup[0], 1);
      popupWindow.initDND(popup.find('.popupTitle')[0], popup[0]);
    },
  
    hiden : function(e) {
      var thiz = $(this);
      var popup = thiz.parents('#UISocialPopupConfirmation')
      if (popup.length > 0) {
        uiMaskLayer.removeMask(popup[0].previousSibling);
        popup.animate({
          height : '0px'
        }, 300, function() {
          $(this).remove();
        });
      }
      SocialUtils.setCookies('currentConfirm', '', -300);
    },

    executeCurrentConfirm : function() {
      var currentConfirm = eXo.core.Browser.getCookie('currentConfirm');
      if (currentConfirm && String(currentConfirm).length > 0) {
        var jcurrentConfirm = $('#' + currentConfirm);
        if (jcurrentConfirm.length > 0) {
          if(currentConfirm === 'SocialCurrentConfirm') {
            jcurrentConfirm.removeAttr('id');
          }
          jcurrentConfirm.trigger('click');
        }
      }
    }
  };
  
  gj(window).resize(function(evt) {
    eXo.core.Browser.managerResize();
    if (SocialUtils.currentBrowseWidth != document.documentElement.clientWidth) {
      try {
        var callback = SocialUtils.onResizeWidth;
        for ( var name in callback) {
          var method = callback[name];
          if (typeof (method) == "function") {
            method(evt);
          }
        }
      } catch (e) {}
    }
    SocialUtils.currentBrowseWidth = document.documentElement.clientWidth;
    //
    SocialUtils.onResizeFillUpFreeSpace();
  });
  //
  SocialUtils.addOnResizeWidth(SocialUtils.onResizeDynamicItemLayout);
  
  setTimeout(PopupConfirmation.executeCurrentConfirm, 220);
  eXo.social.PopupConfirmation = eXo.social.PopupConfirmation || PopupConfirmation;
  SocialUtils.PopupConfirmation = eXo.social.PopupConfirmation;
  eXo.social.SocialUtil = SocialUtils;
  return SocialUtils;

})(window.eXo, gj, uiMaskLayer, popupWindow);