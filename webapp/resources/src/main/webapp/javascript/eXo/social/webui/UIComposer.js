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
    regexpURL : /(http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?/,
    validateWWWURL : function(url) {
      if (url.indexOf('www.') > 0) {
        return /(https?:\/\/)?(www\.[\w+]+\.[\w+]+\.?(:\d+)?)/.test(url);
      }
      return true;
    },
    searchFirstURL : function(x) {
      var result = String(x).match(UIComposer.regexpURL);
      if (result && result.length > 0) {
        for ( var i = 0; i < result.length; ++i) {
          if (result[i].length > 0 && x.indexOf('@'+result[i]) < 0 && UIComposer.validateWWWURL(result[i])) {
            return result[i];
          }
        }
      }
      return "";
    },
    showedLink: false,    
    MAX_LENGTH : 2000,
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
        var composerInput = $('#composerInput');
        // TODO this line is mandatory when a custom skin is defined, it should not be mandatory
        CKEDITOR.basePath = '/commons-extension/ckeditor/';
        composerInput.ckeditor({
          customConfig: '/social-resources/javascript/eXo/social/ckeditorCustom/config.js',
          on : {
            instanceReady : function ( evt ) {
              // Hide the editor top bar.
              var windowWidth = $(window).width();
              var windowHeight = $(window).height();
              if (windowWidth > 767 || windowWidth > 481 && windowWidth > windowHeight) {
                  document.getElementById( evt.editor.id + '_bottom' ).style.display = 'none';
              }
              $("#ShareButton").prop("disabled", true);
            },
            focus : function ( evt ) {
              // Show the editor top bar.
              document.getElementById( evt.editor.id + '_bottom' ).style.display = 'block';
            },
            blur : function ( evt ) {
              // Hide the editor top bar.
              var windowWidth = $(window).width();
              var windowHeight = $(window).height();
              if (windowWidth > 767 || windowWidth > 481 && windowWidth > windowHeight) {
                  document.getElementById( evt.editor.id + '_bottom' ).style.display = 'none';
              }
            },
            change: function( evt) {
                var newData = evt.editor.getData();
                var pureText = newData? newData.replace(/<[^>]*>/g, "").replace(/&nbsp;/g,"").trim() : "";
                if (pureText.length > 0) {
                    $(".share-button").removeAttr("disabled");
                } else {
                    $(".share-button").prop("disabled", true);
                }
            },
            key: function( evt) {
                var newData = evt.editor.getData();
                var pureText = newData? newData.replace(/<[^>]*>/g, "").replace(/&nbsp;/g,"").trim() : "";
                if (pureText.length > UIComposer.MAX_LENGTH) {
                    if ([8, 46, 33, 34, 35, 36, 37,38,39,40].indexOf(evt.data.keyCode) < 0) {
                        evt.cancel();
                    }
                }
                if (!$(".uiLinkShareDisplay").length && (evt.data.keyCode == 32 || evt.data.keyCode == 13)) {
                    var firstUrl = UIComposer.searchFirstURL(pureText);
                    if (firstUrl !== "") {
                        console.log(firstUrl);
                        $('#InputLink').val(firstUrl);
                        $('#AttachButton').trigger('click');
                        UIComposer.showedLink = true;
                    }
                }
            }
            
          }
        });



        var actionLink = $('#actionLink');
        if(actionLink.length > 0 && $(UIComposer.clickOn).hasClass('uidocactivitycomposer') === false) {
          if ($('#InputLink').length == 0) {
            if (UIComposer.clickOn == null || UIComposer.clickOn == "") {
              var UIComposerComp = $('#UIPageCreationWizard');
              if (UIComposerComp.find('#UIPagePreview').length == 0) {
                //actionLink.trigger('click');
              }
            }
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

      /*
       //
       $('textarea#' + UIComposer.textareaId).exoMentions({
       onDataRequest : function(mode, query, callback) {
       var url = window.location.protocol + '//' + window.location.host + '/' + eXo.social.portal.rest + '/social/people/getprofile/data.json?search=' + query;
       $.getJSON(url, function(responseData) {
       responseData = _.filter(responseData, function(item) {
       return item.name.toLowerCase().indexOf(query.toLowerCase()) > -1;
       });
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
       */
    },
    post : function() {
      UIComposer.isReady = false;
      UIComposer.currentValue = "";
    },
    getValue : function() {
      return (UIComposer.currentValue) ? UIComposer.currentValue : '';
    },
    setCurrentValue : function() {
      var uiInputText = $('textarea#' + UIComposer.textareaId);
      UIComposer.currentValue = uiInputText.val();
    },
    setSelectedComposer : function(elm) {
      // remove ActivityComposerExtItemSelected class from previous selected composer
      $(UIComposer.composer).find('.ActivityComposerExtItem').removeClass('ActivityComposerExtItemSelected');
      // add ActivityComposerExtItemSelected class to newly selected composer
      UIComposer.clickOn = elm;
      $(UIComposer.clickOn).closest('.ActivityComposerExtItem').addClass('ActivityComposerExtItemSelected');

    },
    showLink : function() {
      var container = $('#ComposerContainer')
      var link = container.find('#LinkExtensionContainer');
      if (link.length > 0) {
        if (link.css('display') !== 'none') {
          link.hide();
        } else {
          link.show();
        }
      } else {
        var cmp = container.find('.uiLinkShareDisplay');
        if (cmp.length > 0) {
          $('textarea#composerInput').exoMentions('clearLink', function() {
          });
        } else {
          $('#actionLink').trigger('click');
        }
      }
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
