/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */

/**
 * UIComposer.js
 *
 */

(function($, _) {
  var UIComposer = {
    clickOn : null,
    onLoadI18n : function(i18n) {
      window.eXo.social.I18n.mentions = $.extend(true, {}, window.eXo.social.I18n.mentions, i18n);
    },
    onLoad : function(params) {
      UIComposer.configure(params);
      UIComposer.init();
    },
    configure : function(params) {
      UIComposer.composerId = params.composerId;
      UIComposer.textareaId = params.textareaId;
      UIComposer.mentionBtnLabel = params.mentionBtnLabel;
      UIComposer.userTyped = false;
    },
    init : function() {

      UIComposer.composer = $('#' + UIComposer.composerId);

      $(document).ready(function() {
        var actionLink = $('#actionLink');
        if(actionLink.length > 0 && (UIComposer.clickOn === null || $(UIComposer.clickOn).hasClass('uidocactivitycomposer') === false)) {
          if ($('#InputLink').length == 0) {
            actionLink.trigger('click');
          } else {
            var container = $('#ComposerContainer');
            if (container.find('#LinkExtensionContainer').length > 0) {
              container.find('#LinkExtensionContainer').hide().data('isShow', {
                isShow : false
              });
            }
          }
        }
      });

      //
      $('textarea#' + UIComposer.textareaId).exoMentions({
        onDataRequest : function(mode, query, callback) {
          var url = window.location.protocol + '//' + window.location.host + '/' + eXo.social.portal.rest + '/social/people/getprofile/data.json?search=' + query;
          $.getJSON(url, function(responseData) {
            callback.call(this, responseData);
          });
        },
        idAction : 'ShareButton',
        actionLink : 'AttachButton',
        elasticStyle : {
          maxHeight : '64px',
          minHeight : '35px',
          marginButton: '4px',
          enableMargin: false
        },
        messages : window.eXo.social.I18n.mentions
      });
    },
    post : function() {
      UIComposer.isReady = false;
      UIComposer.currentValue = "";
    },
    getValue : function() {
      return (UIComposer.currentValue) ? UIComposer.currentValue : '';
    },
    setCurrentValue : function(elm) {
      var uiInputText = $('textarea#' + UIComposer.textareaId);
      UIComposer.clickOn = elm;
      UIComposer.currentValue = uiInputText.val();
    },
    activeShareButton : function() {
	    try {
	      $('textarea#composerInput').exoMentions('showButton', function() {});
	    } catch (e) {}
    }
  };

  window.UIComposer = UIComposer;
  return UIComposer;
})($, mentions._);
