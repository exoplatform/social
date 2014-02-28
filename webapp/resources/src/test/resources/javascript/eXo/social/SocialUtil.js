(function (eXo, $, uiMaskLayer, popupWindow) {
  
  if(!String.prototype.trim) {
    String.prototype.trim = function () {
      return this.replace(/^\s+|\s+$/g,'');
    };
  }
  
  eXo.social = eXo.social || {};
  
  if (eXo.env) {
    var portal = eXo.env.portal
    
    eXo.social.portal = {
      rest : (portal.rest) ? portal.rest : 'rest-socialdemo',
      portalName : (portal.portalName) ? portal.portalName : 'classic',
      context : (portal.context) ? portal.context : '/socialdemo',
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

  setTimeout(PopupConfirmation.executeCurrentConfirm, 220);
  eXo.social.PopupConfirmation = eXo.social.PopupConfirmation || PopupConfirmation;
  eXo.social.SocialUtil = SocialUtils;
  return SocialUtils;

})(window.eXo, gj, uiMaskLayer, popupWindow);