/*
 * Mentions Input
 * Version 1.0.2
 * Written by: Kenneth Auchenberg (Podio)
 *
 * Using underscore.js
 *
 * License: MIT License - http://www.opensource.org/licenses/mit-license.php
 */

(function ($, _, undefined) {
  window.gj = $;
  // Settings
  var KEY = { BACKSPACE : 8, TAB : 9, RETURN : 13, ESC : 27, LEFT : 37, UP : 38, RIGHT : 39, DOWN : 40, MENTION : 50, COMMA : 188, SPACE : 32, HOME : 36, END : 35 }; // Keys "enum"

  var defaultSettings = {
    triggerChar   : '@',
    onDataRequest : $.noop,
    minChars      : 1,
    showAvatars   : true,
    elastic       : true,
    elasticStyle  : {},
    idAction      : "",
    classes       : {
      autoCompleteItemActive : "active"
    },
    templates     : {
      wrapper                    : _.template('<div class="mentions-input-box"></div>'),
      autocompleteList           : _.template('<div class="mentions-autocomplete-list"></div>'),
      autocompleteListItem       : _.template('<li data-ref-id="<%= id %>" data-ref-type="<%= type %>" data-display="<%= display %>"><%= content %></li>'),
      autocompleteListItemAvatar : _.template('<img  src="<%= avatar %>" />'),
      autocompleteListItemIcon   : _.template('<div class="icon <%= icon %>"></div>'),
      mentionsOverlay            : _.template('<div class="mentions"><div></div></div>'),
      mentionItemSyntax          : _.template('<%=id%>'),
      mentionItemHighlight       : _.template('<strong><span><%= value %></span></strong>')
    }
  };
  //--tuvd--
  function log(v) {
    window.console.log(v);
  }
  
  function cacheMention() {
    var mentionCache = {
      id : '',
      val : '',
      mentions : [],
      data : ''
    };
    return mentionCache;
  };
  //--/tuvd--
  var utils = {
    htmlEncode : function (str) {
      return _.escape(str);
    },
    highlightTerm : function (value, term) {
      if (!term && !term.length) {
        return value;
      }
      return value.replace(new RegExp("(?![^&;]+;)(?!<[^<>]*)(" + term + ")(?![^<>]*>)(?![^&;]+;)", "gi"), "<b>$1</b>");
    },
    rtrim : function(string) {
      return string.replace(/\s+$/,"");
    },
    getSimpleValue : function (val) {
      return val.replace(/&amp;/g,'&').replace(/&nbsp;/g, ' ')
            .replace(/<span.*?>/gi, '').replace(/<\/span>/gi, '')
            .replace(/<br.*?>/g, '').replace(/\n/g, '<br />');
    },
    getCursorIndexOfText : function (val1, val2) {
      var t = val2.length;
      for(var i = 0; i < val2.length; ++i) {
        if(val1[i] != val2[i]) {
            t = i-1;
            break;
        }
      }

      if(t >=0) {
        if($.trim(val1.substr(t)).indexOf('<span') === 0) {
          return t;
        }
      }
      
      return -1;
    }
  };

  var MentionsInput = function (settings) {

    var jElmTarget, elmInputBox, elmInputWrapper, elmAutocompleteList, elmWrapperBox, elmMentionsOverlay, elmActiveAutoCompleteItem;
    var mentionsCollection = [];
    var autocompleteItemCollection = {};
    var inputBuffer = [];
    var currentDataQuery;

    settings = $.extend(true, {}, defaultSettings, settings );

    function initTextarea() {

      if (elmInputBox.attr('data-mentions-input') == 'true') {
        return;
      }

      elmInputWrapper = elmInputBox.parent();
      elmWrapperBox = $(settings.templates.wrapper());
      elmInputBox.wrapAll(elmWrapperBox);
      elmWrapperBox = elmInputWrapper.find('> div');

      elmInputBox.attr('data-mentions-input', 'true');
      elmInputBox.on('keydown', onInputBoxKeyDown);
      elmInputBox.on('keypress', onInputBoxKeyPress);
      elmInputBox.on('input', onInputBoxInput);
      elmInputBox.on('click', onInputBoxClick);
      elmInputBox.on('paste', onInputBoxPaste);
      elmInputBox.on('blur', onInputBoxBlur);

      // Elastic textareas, internal setting for the Dispora guys
      if( settings.elastic ) {
        elmInputBox.elastic(settings);
      }

    }

    function initAutocomplete() {
      elmAutocompleteList = $(settings.templates.autocompleteList());
      elmAutocompleteList.appendTo(elmWrapperBox);
      elmAutocompleteList.delegate('li', 'mousedown', onAutoCompleteItemClick);
    }

    function initMentionsOverlay() {
      elmMentionsOverlay = $(settings.templates.mentionsOverlay());
      elmMentionsOverlay.prependTo(elmWrapperBox);
    }

    function updateValues() {
      var syntaxMessage = getInputBoxValue();
      
      _.each(mentionsCollection, function (mention) {
        var textSyntax = settings.templates.mentionItemSyntax(mention);
        syntaxMessage = syntaxMessage.replace(mention.value, textSyntax);
      });

      var mentionText = utils.htmlEncode(syntaxMessage);

      _.each(mentionsCollection, function (mention) {
        var formattedMention = _.extend({}, mention, {value: utils.htmlEncode(mention.value)});
        var textSyntax = settings.templates.mentionItemSyntax(formattedMention);
        var textHighlight = settings.templates.mentionItemHighlight(formattedMention);

        mentionText = mentionText.replace(textSyntax, textHighlight);
      });

      mentionText = mentionText.replace(/\n/g, '<br />');
      mentionText = mentionText.replace(/ {2}/g, '&nbsp; ');

      elmInputBox.data('messageText', syntaxMessage);
      elmMentionsOverlay.find('div').html(mentionText);
    }

    function resetBuffer() {
      inputBuffer = [];
    }

    function updateMentionsCollection() {
      var inputText = getInputBoxValue();

      mentionsCollection = _.reject(mentionsCollection, function (mention, index) {
        return !mention.value || inputText.indexOf(mention.value) == -1;
      });
      mentionsCollection = _.compact(mentionsCollection);
    }

    function addMention(mention) {

      var currentMessage = getInputBoxFullValue();

      // Using a regex to figure out positions
      var regex = new RegExp("\\" + settings.triggerChar + currentDataQuery, "gi");
      regex.exec(currentMessage);

      var startCaretPosition = regex.lastIndex - currentDataQuery.length - 1;
      var currentCaretPosition = regex.lastIndex;

      var start = currentMessage.substr(0, startCaretPosition);
      var end = currentMessage.substr(currentCaretPosition, currentMessage.length);
      var startEndIndex = (start + mention.value).length + 1;

      mentionsCollection.push(mention);

      // Cleaning before inserting the value, otherwise auto-complete would be triggered with "old" inputbuffer
      resetBuffer();
      currentDataQuery = '';
      hideAutoComplete();

      // Mentions & syntax message
      var updatedMessageText = start + addItemMention(mention.value) + end;

      elmInputBox.val(updatedMessageText);
      jElmTarget.focus();
      
      setCaratPosition(elmInputBox);
      
      initClickMention();
      updateValues();

    }

    function addItemMention(value) {
      var val = '<span contenteditable="false">'+ value + 
                '<span class="icon"'+(($.browser.mozilla)?'contenteditable="true"':'')+'>&nbsp;</span></span>' +
                '&nbsp;<div id="cursorText"></div>';
      return val;
    }

  // --tuvd--   keypress keydown change cut paste
    function initClickMention() {
      var sp = elmInputBox.find('> span');
      if(sp.length > 0) {
        $.each(sp, function(index, item) {
          var sp = $(item).find('span');
          sp.data('indexMS', {'indexMS':index}).off('click');
          sp.on('click', function(e) {
            var t = $(this).data('indexMS').indexMS;
            mentionsCollection.splice(t, 1);
            $(this).parent().remove();
            updateValues();
            saveCacheMention();
            initClickMention();
            e.stopPropagation();
          });
          $(item).on('click', function() {
            var selection = getSelection();
            if (selection) {
            var range = document.createRange();
            range.selectNodeContents(this);
            range.selectNode(this);
            
            selection.removeAllRanges();
            selection.addRange(range);
            }
          });
        });
      }
    }

    function getSelection() {
      var selection;
      if (window.getSelection) {
        selection = window.getSelection();
      } else if (document.getSelection) {
        selection = document.getSelection();
      } else if (document.selection) {
        selection = document.selection;
      }
      return selection;
    };

    function setCaratPosition(inputField) {
      if(inputField) {
        var cursorText = inputField.find('#cursorText');
        if (inputField.val().length != 0) {
          var elm = inputField[0]; 
          var selection = getSelection();
          if (selection) {
            cursorText.attr('contenteditable', 'true')
              .css({'display':'inline', 'height':'14px'})
              .html('&nbsp;&nbsp;&nbsp;');
              
          cursorText.focus();
          var range = document.createRange();;
            range.selectNode(elm);
            range.selectNodeContents(cursorText[0]);
            
            selection.removeAllRanges();
            selection.addRange(range);
          }
        } else {
          inputField.focus();
        }
        cursorText.remove();
      }
    }

    function saveCacheMention() {
      var key = jElmTarget.attr('id');
      if(key) {
        var parentForm = jElmTarget.parents('form:first').parent();
        if(parentForm.length > 0) {
          var dataCache = parentForm.data(key);
          if(dataCache == null) {
            dataCache = new cacheMention();
          }
          dataCache.mentions = mentionsCollection;
          dataCache.val = getInputBoxFullValue();
          dataCache.data = mentionsCollection.length > 0 ? elmInputBox.data('messageText') : getInputBoxValue() ;
          parentForm.data(key, dataCache);
        }
      }
    }
//--/tuvd--

    function getInputBoxValue() {
      return $.trim(elmInputBox.val());
    }

    function getInputBoxFullValue() {
      return $.trim(elmInputBox.value());
    }

    function onAutoCompleteItemClick(e) {
      var elmTarget = $(this);
      var mention = autocompleteItemCollection[elmTarget.attr('data-uid')];
      addMention(mention);
      saveCacheMention();
      return false;
    }

    function copyToClipboard() {
      var text = '';
      if (window.clipboardData) { // Internet Explorer
        window.clipboardData.setData("Text", text);
      } else {
        unsafeWindow.netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
        var clipboardHelper = Components.classes["@mozilla.org/widget/clipboardhelper;1"].getService(Components.interfaces.nsIClipboardHelper);
        clipboardHelper.copyString(text);
      }
      return text;
    }
    
    function onInputBoxPaste(e) {
      var elmTarget = $(this);
      $.each(elmTarget.find('span'), function(index, item) {
        if($(item).attr('background-color') != undefined) {
          $(item).remove();
        }
      });
      var plsd = $(this).parent().find('div.placeholder:first');
      if(plsd.length > 0 && $.trim(elmInputBox.val()).length === 0) {
        plsd.show();
      }
    }

    function onInputBoxClick(e) {
      resetBuffer();
    }

    function onInputBoxBlur(e) {
      hideAutoComplete();
      saveCacheMention();
      var plsd = $(this).parent().find('div.placeholder:first');
      if(plsd.length > 0 && $.trim(elmInputBox.val()).length === 0) {
        plsd.show();
      }
    }

    function onInputBoxInput(e) {
      updateValues();
      updateMentionsCollection();
      hideAutoComplete();

      var triggerCharIndex = _.lastIndexOf(inputBuffer, settings.triggerChar);
      if (triggerCharIndex > -1) {
        currentDataQuery = inputBuffer.slice(triggerCharIndex + 1).join('');
        currentDataQuery = utils.rtrim(currentDataQuery);

        _.defer(_.bind(doSearch, this, currentDataQuery));
      }
    }

    function onInputBoxKeyPress(e) {

      if(e.keyCode !== KEY.BACKSPACE) {
        var typedValue = String.fromCharCode(e.which || e.keyCode);
        inputBuffer.push(typedValue);
        var plsd = $(this).parent().find('div.placeholder:first');
        if(plsd.length > 0) {
          plsd.hide();
        }
      }
    }

    function onInputBoxKeyDown(e) {

      if ( e.keyCode == 86 && e.ctrlKey) {
         alert('Can not support paste !!!!');
         return;
      }

      //
      if ( e.keyCode == KEY.MENTION ) {
        var query = '';
        settings.onDataRequest.call(this, 'search', query, function (responseData) {
          populateDropdown(query, responseData);
        });
      }
      
      // This also matches HOME/END on OSX which is CMD+LEFT, CMD+RIGHT
      if (e.keyCode == KEY.LEFT || e.keyCode == KEY.RIGHT || e.keyCode == KEY.HOME || e.keyCode == KEY.END) {
        // Defer execution to ensure carat pos has changed after HOME/END keys
        _.defer(resetBuffer);

        // IE9 doesn't fire the oninput event when backspace or delete is pressed. This causes the highlighting
        // to stay on the screen whenever backspace is pressed after a highlighed word. This is simply a hack
        // to force updateValues() to fire when backspace/delete is pressed in IE9.
        if (navigator.userAgent.indexOf("MSIE 9") > -1) {
          _.defer(updateValues);
        }

        return;
      }

      if (e.keyCode == KEY.SPACE) {
        inputBuffer = [];
      }
      if (e.keyCode == KEY.BACKSPACE) {
        inputBuffer = inputBuffer.slice(0, -1 + inputBuffer.length); // Can't use splice, not available in IE

        var plsd = $(this).parent().find('div.placeholder:first');
        if(plsd.length > 0 && $.trim(elmInputBox.val()).length === 1) {
          plsd.show();
        } else {
          var before = elmInputBox.value();
          elmInputBox.animate({'cursor': 'wait'}, 200 , function() {
            var after = elmInputBox.value();
            var delta = before.length - after.length;
            var textSizeMention = 63;
            if(delta > textSizeMention) {
              var i = utils.getCursorIndexOfText(before, after);
              log('index: ' + i);
              if(i >= 0) {
                after = after.substr(0, i) + ' @<div id="cursorText"></div>' + after.substr(i, after.length);
                after = after.replace(/  @/g, ' @');
                elmInputBox.val(after);
                autoSetKeyCode(elmInputBox);
                setCaratPosition(elmInputBox);
              }
            } else if(delta == 1 && after[after.length-1] === '@'){
              autoSetKeyCode(elmInputBox);
            }
            elmInputBox.css('cursor', 'text');
          });

        }
        return;
      }

      if (!elmAutocompleteList.is(':visible')) {
        return true;
      }

      switch (e.keyCode) {
        case KEY.UP:
        case KEY.DOWN:
          var elmCurrentAutoCompleteItem = null;
          if (e.keyCode == KEY.DOWN) {
            if (elmActiveAutoCompleteItem && elmActiveAutoCompleteItem.length) {
              elmCurrentAutoCompleteItem = elmActiveAutoCompleteItem.next();
            } else {
              elmCurrentAutoCompleteItem = elmAutocompleteList.find('li').first();
            }
          } else {
            elmCurrentAutoCompleteItem = $(elmActiveAutoCompleteItem).prev();
          }

          if (elmCurrentAutoCompleteItem.length) {
            selectAutoCompleteItem(elmCurrentAutoCompleteItem);
          }

          return false;

        case KEY.RETURN:
        case KEY.TAB:
          if (elmActiveAutoCompleteItem && elmActiveAutoCompleteItem.length) {
            elmActiveAutoCompleteItem.trigger('mousedown');
            return false;
          }
          break;
      }

      return true;
    }

    function autoSetKeyCode(elm) {
      var event = document.createEvent("KeyboardEvent");
      if(event.initKeyboardEvent) {
        event.initKeyboardEvent("keypress", true, true, null, false, false, false, false, 50, 0);
      }else {
        event.initUIEvent("keypress", true, true, window, 1);
        event.keyCode = 50;
      }
      var e = jQuery.Event("keydown", { keyCode: 50, charCode : 50 });
      elm.triggerHandler(e);
      elm.trigger(e);
      resetBuffer();
      inputBuffer[0] = '@';
    }

    function hideAutoComplete() {
      elmActiveAutoCompleteItem = null;
      elmAutocompleteList.empty().hide();
    }

    function selectAutoCompleteItem(elmItem) {
      elmItem.addClass(settings.classes.autoCompleteItemActive);
      elmItem.siblings().removeClass(settings.classes.autoCompleteItemActive);

      elmActiveAutoCompleteItem = elmItem;
    }

    function populateDropdown(query, results) {
      elmAutocompleteList.show();

      // Filter items that has already been mentioned
      //var mentionValues = _.pluck(mentionsCollection, 'value');
//      results = _.reject(results, function (item) {
//        return _.include(mentionValues, item.name);
//      });

      if (!results.length) {
        hideAutoComplete();
        return;
      }

      elmAutocompleteList.empty();
      var elmDropDownList = $("<ul>").appendTo(elmAutocompleteList).hide();

      _.each(results, function (item, index) {
        var itemUid = _.uniqueId('mention_');

        autocompleteItemCollection[itemUid] = _.extend({}, item, {value: item.name});

        var elmListItem = $(settings.templates.autocompleteListItem({
          'id'      : utils.htmlEncode(item.id),
          'display' : utils.htmlEncode(item.name),
          'type'    : utils.htmlEncode(item.type),
          'content' : utils.highlightTerm(utils.htmlEncode((item.name)), query)
        })).attr('data-uid', itemUid);

        if (index === 0) {
          selectAutoCompleteItem(elmListItem);
        }

        if (settings.showAvatars) {
          var elmIcon;

          if (item.avatar) {
            elmIcon = $(settings.templates.autocompleteListItemAvatar({ avatar : item.avatar }));
          } else {
            elmIcon = $(settings.templates.autocompleteListItemIcon({ icon : item.icon }));
          }
          elmIcon.prependTo(elmListItem);
        }
        elmListItem = elmListItem.appendTo(elmDropDownList);
      });

      elmAutocompleteList.show();
      elmDropDownList.show();
    }

    function doSearch(query) {
      if (query && query.length && query.length >= settings.minChars) {
        settings.onDataRequest.call(this, 'search', query, function (responseData) {
          populateDropdown(query, responseData);
        });
      }
    }

    function resetInput() {
      elmInputBox.val('');
      mentionsCollection = [];
      updateValues();
    }

    //--tuvd--    
    function updateCacheData() {
      var parentForm = jElmTarget.parents('form:first').parent();
      var key = jElmTarget.attr('id');
      if(key) {
        var dataCache = parentForm.data(key);
        if(dataCache == null) {
          resetInput();
        } else {
          mentionsCollection = dataCache.mentions;
          elmInputBox.val(dataCache.val);
          elmInputBox.data('messageText', dataCache.data);
          updateValues();
        }
      }
    }
    
    function clearCacheData() {
      var parentForm = jElmTarget.parents('form:first').parent();
      var key = jElmTarget.attr('id');
      if(key) {
        var dataCache = parentForm.data(key);
        if(dataCache != null) {
          parentForm.data(key, null);
        }
      }
    }
    
    function getTemplate() {
      return $('<div contenteditable="true" g_editable="true" class="ReplaceTextArea editable"></div>');
    }

    function initDisplay(id, target) {
      var id_ = "Display"+id;
      var displayInput = target.find('#'+id_);
      if(displayInput.length === 0) {
          displayInput = getTemplate().attr('id', id_);
          displayInput.appendTo(target);
      }
      displayInput.val = function(v) {
        if(v === null || typeof v === "undefined") {
          var temp = $(this).clone();
          temp.find('.icon').remove();
          return utils.getSimpleValue(temp.html());
        } else {
          if(typeof v === 'object') {
            $(this).html('').append(v);
          } else {
            $(this).html(v);
          }
          
        }
      };
      displayInput.value = function() {
        var val = $(this).html().replace(/&amp;/g,'&').replace(/&nbsp;/g, ' ')
                          .replace(/<br>/g, '').replace(/\n/g, '<br />');
        return val;
      };
      return displayInput;
    }
    //--/tuvd--

    // Public methods
    return {
      init : function (domTarget) {
        window.jq = $;
        jElmTarget = $(domTarget);
        jElmTarget.css({'visibility':'hidden', 'display':'none'});
        //
        jElmTarget.val('');
        
        elmInputBox = initDisplay(jElmTarget.attr('id'), jElmTarget.parent());;
        
        initTextarea();
        initAutocomplete();
        initMentionsOverlay();
        updateCacheData();
    
        // add placeholder
        if($.trim(elmInputBox.val()).length == 0) {
          var title = jElmTarget.attr('title');
          $('<div class="placeholder">'+title+'</div>').attr('title', title).appendTo(elmInputBox.parent());
        }
        // prefill mentions
        if( settings.prefillMention ) {
          addMention( settings.prefillMention );
        }
        // action submit
        if(settings.idAction && settings.idAction.length > 0) {
          $('#'+settings.idAction).on('mousedown', function() {
            var value = mentionsCollection.length ? elmInputBox.data('messageText') : getInputBoxValue();
            value = value.replace(/&lt;/gi, '<').replace(/&gt;/gi, '>');
            jElmTarget.val(value);
            clearCacheData();
            resetInput();
          });
        } 
      },

      val : function (callback) {
        if (!_.isFunction(callback)) {
          return;
        }
        var value = mentionsCollection.length ? elmInputBox.data('messageText') : getInputBoxValue();
        callback.call(this, value);
      },

      valClear : function (callback) {
        if (!_.isFunction(callback)) {
          return;
        }
        var value = mentionsCollection.length ? elmInputBox.data('messageText') : getInputBoxValue();
        callback.call(this, value);
        clearCacheData();
        resetInput();
      },
      
      reset : function () {
        resetInput();
      },

      getMentions : function (callback) {
        if (!_.isFunction(callback)) {
          return;
        }
        callback.call(this, mentionsCollection);
      }
    };
  };
  // elastic the mention content
  $.fn.extend({  
    elastic: function(settings) {
      elasticStyle = settings.elasticStyle;
      if(elasticStyle && typeof elasticStyle === 'object') {
        return this.each(function () {
          var delta = parseInt(elasticStyle.maxHeight) - parseInt(elasticStyle.minHeight);
          $(this).css({
              'height': elasticStyle.minHeight,
              'marginBottom': (delta + 4) + 'px'
          });
          $(this).data('elasticStyle', {'maxHeight': elasticStyle.maxHeight, 'minHeight':elasticStyle.minHeight, 'delta': delta})
          .on('focus', function() {
            var maxH = $(this).data('elasticStyle').maxHeight;
            if($(this).height() < parseInt(maxH)) {
              $(this).animate({
                'height': maxH,
                'marginBottom': '4px'
              }, 100, function(){});
            }
          }).on('blur',function(){
            var val = $.trim($(this).html());
            val = utils.getSimpleValue(val);
            if(val.length == 0) {
              $(this).animate({
                'height': $(this).data('elasticStyle').minHeight,
                'marginBottom': ($(this).data('elasticStyle').delta + 4) + 'px'
              }, 300, function() {});
            }
          });
        });
      }
    }
  });

  $.fn.mentionsInput = function (method, settings) {

    var outerArguments = arguments;

    if (typeof method === 'object' || !method) {
      settings = method;
    }

    return this.each(function () {
      var instance = $.data(this, 'mentionsInput') || $.data(this, 'mentionsInput', new MentionsInput(settings));
      if (_.isFunction(instance[method])) {
        return instance[method].apply(this, Array.prototype.slice.call(outerArguments, 1));
      } else if (typeof method === 'object' || !method) {
        return instance.init.call(this, this);
      } else {
        $.error('Method ' + method + ' does not exist');
      }
    });
  };
  
})(jQuery, mentions.underscore);

