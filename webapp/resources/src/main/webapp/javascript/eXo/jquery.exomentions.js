/*
 * Mentions Input use for ExoPlatform
 * Version 1.0.
 * Written by: Vu Duy Tu
 *
 * Using underscore.js
 *
 */

(function($, _, undefined) {
  // Settings
  var KEY = {
    BACKSPACE : 8,
    TAB : 9,
    RETURN : 13,
    ESC : 27,
    LEFT : 37,
    UP : 38,
    RIGHT : 39,
    DOWN : 40,
    DELETE : 46,
    MENTION : 64,
    COMMA : 188,
    SPACE : 32,
    HOME : 36,
    END : 35
  }; // Keys "enum"

  var defaultSettings = {
    triggerChar : '@',
    onDataRequest : $.noop,
    minChars : 1,
    showAvatars : true,
    firstShowAll : false,
    selectFirst : true,
    elastic : true,
    elasticStyle : {
      maxHeight : '0px',
      minHeight : '0px',
      marginButton: '4px',
      enableMargin: false
    },
    idAction : "",
    actionLink : null,
    classes : {
      autoCompleteItemActive : "active"
    },
    cacheResult : {
      hasUse : true,
      live : 30000 // MiniSeconds
    },
    messages : {
      helpSearch: 'Type to start searching for users.',
      searching: 'Searching for ',
      foundNoMatch : 'Found no matching users for '
    },
    templates : {
      wrapper : _.template('<div class="exo-mentions"></div>'),
      autocompleteList : _.template('<div class="autocomplete-menu"></div>'),
      autocompleteListItem : _.template('<li class="data" data-ref-id="<%= id %>" data-ref-type="<%= type %>" data-display="<%= display %>"><%= content %></li>'),
      autocompleteListItemAvatar : _.template('<div class="avatarSmall"><img  src="<%= avatar %>" /></div>'),
      autocompleteListItemIcon : _.template('<div class="icon <%= icon %>"></div>'),
      mentionItemSyntax : _.template('<%=id%>'),
      mentionItemHighlight : _.template('<strong><span><%= value %></span></strong>')
    }
  };

  var regexpURL = /(https?:\/\/)?((www\.[\w+]+\.[\w+]+\.?(:\d+)?)|([\w+]+\.[\w+]+\.?(\w+)?(:\d+)?))(\/\S*)?/g;
  // /^(ht|f)tps?:\/\/[a-z0-9-\.]+\.[a-z]{2,4}\/?([^\s<>\#%"\,\{\}\\|\\\^\[\]`]+)?$/
  // /(ftp|http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?/

  function log(v) {
    if(window.console && window.console.log) {
      window.console.log(v);
    }
  }

  function cacheMention() {
    var mentionCache = {
      id : '',
      val : '',
      mentions : [],
      data : '',
      actionLink : {}
    };
    return mentionCache;
  }

  var utils = {
    htmlEncode : function(str) {
      return _.escape(str);
    },
    highlightTerm : function(value, term) {
      if (!term && !term.length) {
        return value;
      }
      return value.replace(new RegExp("(?![^&;]+;)(?!<[^<>]*)(" + term + ")(?![^<>]*>)(?![^&;]+;)", "gi"), "<strong>$1</strong>");
    },
    rtrim : function(string) {
      return string.replace(/\s+$/, "");
    },
    replaceFirst : function(string, by){
      while(string.indexOf(by) === 0) {
        string = string.substring(1);
      }
      return string;
    },
    validateWWWURL : function(url) {
      if (url.indexOf('www.') > 0) {
        return /(https?:\/\/)?(www\.[\w+]+\.[\w+]+\.?(:\d+)?)/.test(url);
      }
      return true;
    },
    searchFirstURL : function(x) {
      var result = String(x).match(regexpURL);
      if (result && result.length > 0) {
        for ( var i = 0; i < result.length; ++i) {
          if (result[i].length > 0 && x.indexOf('@'+result[i]) < 0 && this.validateWWWURL(result[i])) {
            return result[i];
          }
        }
      }
      return "";
    },
    removeLastBr : function(val) {
      if (val) {
        var l = val.length;
        if (l > 0) {
          val = val.replace(/<br\/?>$/gi, '');
          if (l > val.length) {
            return utils.removeLastBr(val);
          }
        }
        return val;
      }
      return '';
    },
    getSimpleValue : function(val) {
      if (val) {
        val = val.replace(/&amp;/g, '&').replace(/&nbsp;/g, ' ')
                 .replace(/<span.*?>/gi, '').replace(/<\/span>/gi, '');
        return utils.removeLastBr(val);
      }
      return '';
    },
    getCursorIndexOfText : function(before, after) {
      var t = 0;
      for ( var i = 0; i < after.length; ++i) {
        if (before[i] === after[i]) {
          ++t;
        } else {
          break;
        }
      }
      if (t >= 0) {
        return t;
      }
      return -1;
    },
    getIndexChange : function(before, after) {// before , after
      var info = {
        from : (before.length > 0) ? before.length : 0,
        to : after.length
      };
      for ( var i = 0; i < before.length; ++i) {
        if (before[i] != after[i]) {
          info.from = (i > 1) ? (i - 1) : 0;
          break;
        }
      }
      if (before.length > 0) {
        var t = after.length - 1;
        for ( var i = before.length - 1; i >= 0; --i) {
          if (before[t] != after[i]) {
            info.to = t + 1;
            break;
          }
          --t;
        }
      }
      return info;
    },
    isIE : ($.browser.msie === true),
    isFirefox : ($.browser.mozilla === true),
    brVersion : $.browser.version
  };

  var eXoMentions = function(settings) {

    var jElmTarget, elmInputBox, elmInputWrapper, elmAutocompleteList, elmWrapperBox, elmActiveAutoCompleteItem;
    var valueBeforMention = '';
    var mentionsCollection = [];
    var autocompleteItemCollection = {};
    var inputBuffer = [];
    var currentDataQuery = '';
    var cursor = '<div class="cursorText"></div>' + ((utils.isIE === false) ? '&nbsp;' : '');
    var currentSelection = {elm: null, offset : 0};
    var isBlockMenu = false;
    var isInput = false;
    // action add link
    var ActionLink = {
      isRun : false,
      actionLink : null,
      hasNotLink : true,
      linkSaved : ''
    };
    
    settings = $.extend(true, {}, defaultSettings, settings);

    KEY.MENTION = settings.triggerChar.charCodeAt(0);

    ActionLink.isRun = (settings.actionLink && settings.actionLink.length > 0);
    ActionLink.actionLink = settings.actionLink;

    function initTextarea() {

      if (elmInputBox.attr('data-mentions') == 'true') {
        return;
      }

      elmInputWrapper = elmInputBox.parent();
      elmWrapperBox = $(settings.templates.wrapper());
      elmInputBox.wrapAll(elmWrapperBox);
      elmWrapperBox = elmInputWrapper.find('> div');

      elmInputBox.attr('data-mentions', 'true');
      elmInputBox.on('keydown', onInputBoxKeyDown);
      elmInputBox.on('keypress', onInputBoxKeyPress);
      elmInputBox.on('keyup', onInputBoxKeyUp);
      //elmInputBox.on('input', onInputBoxInput);
      elmInputBox.on('click', onInputBoxClick);
      elmInputBox.on('paste', onInputBoxPaste);
      elmInputBox.on('blur', onInputBoxBlur);

      if (settings.elastic) {
        elmInputBox.elastic(settings);
      }

    }

    function initAutocomplete() {
      elmAutocompleteList = $(settings.templates.autocompleteList());
      elmAutocompleteList.appendTo(elmWrapperBox);
      elmAutocompleteList.on('mousedown', 'li.data', onAutoCompleteItemClick);
      elmAutocompleteList.on('mouseover', 'li.data', selectAutoCompleteItem);
    }

    function updateValues() {
      var syntaxMessage = getInputBoxValue();

      _.each(mentionsCollection, function(mention) {
        var textSyntax = settings.templates.mentionItemSyntax(mention);
        syntaxMessage = syntaxMessage.replace(mention.value, textSyntax);
      });

      elmInputBox.data('messageText', syntaxMessage);
    }

    function resetBuffer() {
      inputBuffer = [];
    }

    function updateMentionsCollection() {
      var inputText = getInputBoxValue();

      mentionsCollection = _.reject(mentionsCollection, function(mention, index) {
        return !mention.value || inputText.indexOf(mention.value) == -1;
      });
      mentionsCollection = _.compact(mentionsCollection);
    }

    function addMention(mention) {

      var currentMessage = getInputBoxFullValue();

      // Using a regex to figure out positions
      var strReg = settings.triggerChar + currentDataQuery;
      if(currentMessage.indexOf(strReg) === 0) {
        strReg = "\\"+strReg;
      } else if(currentMessage.indexOf('>'+strReg) > 0) {
        strReg = "\\>"+strReg;
      } else {
        strReg = "\\ "+strReg;
      }
      var regex = new RegExp(strReg, "gi");
      regex.exec(currentMessage);
      
      var startCaretPosition = regex.lastIndex - currentDataQuery.length - 1;
      var currentCaretPosition = regex.lastIndex;

      var start = currentMessage.substr(0, startCaretPosition);
      var end = currentMessage.substr(currentCaretPosition, currentMessage.length);
      var startEndIndex = (start + mention.value).length + 1;

      mentionsCollection.push(mention);

      // Cleaning before inserting the value, otherwise auto-complete would be
      // triggered with "old" inputbuffer
      resetBuffer();
      currentDataQuery = '';
      hideAutoComplete();

      // Mentions & syntax message
      var point = '<span class="none point"></span>';
      var updatedMessageText = utils.removeLastBr(start + addItemMention(mention.value) + point + end);

      elmInputBox.val(updatedMessageText);
      point = elmInputBox.find('span.point')
      addElmCaret(point);
      point.remove();
      initClickMention();
      setCaretPosition(elmInputBox);
    }


    function addItemMention(value) {
      var val = '<span contenteditable="false">' + value + '<i class="uiIconClose uiIconLightGray"' + ((utils.isFirefox) ? 'contenteditable="true"' : '') + '>x</i></span>';
      return insertCursorText(val, -1, false);
    }

    function insertCursorText(value, index, add) {
      value = $.trim(value);
      var cursor_ = ' ' + ((add && add === true) ? (settings.triggerChar + cursor) : cursor);
      var val = (index == 0) ? ($.trim(cursor_) + value) :
                 ((index < 0) ? (value + cursor_) : 
                  ($.trim(value.substring(0, index)) + cursor_ + $.trim(value.substring(index, value.length))));
      return val;
    }

    function initClickMention() {
      var sp = elmInputBox.find('> span');
      if (sp.length > 0) {
        $.each(sp, function(index, item) {
          var sp = $(item).find('i');

          sp.data('indexMS', {
            'indexMS' : index
          }).off('click');
          sp.on('click', function(e) {
            var t = $(this).data('indexMS').indexMS;
            mentionsCollection.splice(t, 1);
            var parent = $(this).parent();
            elmInputBox.find('.cursorText').remove();
            $('<div class="cursorText"></div>').insertAfter(parent);
            var tx = document.createTextNode(settings.triggerChar);
            $(tx).insertAfter(parent);
            //
            addElmCaret(parent);
            parent.remove();
            updateValues();
            saveCacheData();
            initClickMention();
            e.stopPropagation();
            autoSetKeyCode(elmInputBox);
            setCaretPosition(elmInputBox);
          });
          $(item).on('click', function() {
            setCaretSelection(this);
          });
        });
      }
    }

    function getSelection() {
      var selection = null;
      if (window.getSelection) {
        selection = window.getSelection();
      } else if (document.getSelection) {
        selection = document.getSelection();
      } else if (document.selection) {
        selection = document.selection;
      }
      return selection;
    }

    function setCaretPosition(inputField) {
      if (inputField != null && inputField.length > 0) {
        var cursorText = inputField.find('.cursorText');
        if (inputField.val().length != 0) {
          var elm = inputField[0];
          var selection = getSelection();
          if (selection) {
            cursorText.attr('contenteditable', 'true').css({
              'display' : 'inline',
              'height' : '14px'
            }).html('&nbsp;&nbsp;&nbsp;');
            setCaretSelection(cursorText[0]);
          }
        }
        cursorText.remove();
        inputField.focus();
        updateValues();
      }
    }

    function setCaretSelection(elm) {
      try {
        if (elm) {
          elm.focus();
          if (document.selection) {
            var range = document.selection.createRange();
          } else {
            var range = document.createRange();
            range.selectNode(elm);
            range.selectNodeContents(elm);

            var selection = getSelection();
            selection.removeAllRanges();
            selection.addRange(range);
          }
        }
      } catch (err) {
        log(err);
      }
    }

    function addElmCaret(elm) {
      elmInputBox.find('span.none').remove();
      var tx = document.createElement('span');
      $(tx).insertAfter(elm);
      $(tx).addClass('none');
      saveCaretPosition(tx, 0);
    }
    
    function setCaretPositionIE (inputField, tx) {

      // IE Support
        var selection = getSelection();
        // Set focus on the element
        inputField.focus ();
        var val = inputField.value();
        // Create empty selection range
        var oSel = document.selection.createRange();
   
        // Move selection start and end to 0 position
        oSel.moveStart ('character', -1 * val.length);
   
        // Move selection start and end to desired position
        oSel.moveStart ('character', val.indexOf(tx.outerHTML));
        oSel.moveEnd ('character', 0);
        oSel.select ();
    }

    function getInputBoxFullValue() {
      return $.trim(elmInputBox.value());
    }

    function getInputBoxValue() {
      return $.trim(elmInputBox.val());
    }

    function onAutoCompleteItemClick(e) {
      var elmTarget = $(this);
      var mention = autocompleteItemCollection[elmTarget.attr('data-uid')];
      addMention(mention);
      saveCacheData();
      return false;
    }

    function onInputBoxPaste(e) {
      var before = $.trim(elmInputBox.value());
      elmInputBox.animate({
        'cursor' : 'wait'
      }, 50, function() {
        var after = $.trim(elmInputBox.value());
        var info = utils.getIndexChange(before, after);
        if(after.indexOf('<img src="data:image/') >= 0) {
          elmInputBox.val(before.substring(0, info.from) + '<div class="cursorText"></div>' + before.substring(info.from));
          setCaretPosition(elmInputBox);
          return;
        }
        var text = after.substr(info.from, info.to);
        var textValidated = $('<div/>').html(text).text();
        if (textValidated.length < text.length) {
          textValidated = textValidated.replace(/</gi, '&lt;').replace(/>/gi, '&gt;');
          after = after.substr(0, info.from) + textValidated + ' ' + cursor + after.substr(info.to);
          elmInputBox.val(after);
          setCaretPosition(elmInputBox);
        }
        autoAddLink(textValidated);
        elmInputBox.css('cursor', 'text');
        disabledPlaceholder();
      });

      // 
      return;
    }

    function onInputBoxClick(e) {
      if(elmAutocompleteList.find('li').length > 0) {
        setCaretPosition(elmInputBox);
        showDropdown();
      } else {
        e.stopPropagation();
        resetBuffer();
        saveCaretPosition();
      }
    }

    function onInputBoxBlur(e) {
      hideAutoComplete(false);
      if(elmAutocompleteList.find('li').length > 0) {
        elmInputBox.find('.cursorText').remove();
        var value = elmInputBox.value();
        var typed = inputBuffer.join('');
        var reBy = $.trim(typed);
        currentDataQuery = reBy.replace(settings.triggerChar, '');
        inputBuffer = reBy.split('');
        if(value.indexOf(typed) > 0) {
          typed = ' ' + typed;
          reBy = ' ' + reBy;
        }
        value = value.replace(typed, reBy+'<div class="cursorText"></div>');
        elmInputBox.val(value);
      }
      saveCacheData();
      if (getInputBoxValue().length === 0) {
        enabledPlaceholder();
      }
    }

    function onInputBoxInput(e) {
      
      if(isInput) return;
      isInput = true;
      updateValues();
      updateMentionsCollection();
      inputBuffer = utils.replaceFirst(inputBuffer.join(''), ' ').split('');
      var triggerCharIndex = _.lastIndexOf(inputBuffer, settings.triggerChar);
      if (triggerCharIndex === 0) {
        if (isBlockMenu === false) {
          var after = elmInputBox.value();
          var indexChanged = utils.getCursorIndexOfText(valueBeforMention, after);
          if (indexChanged > 0) {
            var val = after.substring(indexChanged - 1, indexChanged);
            var isRun = (val === ' ') || (val === '') || (val === '&nbsp;') || (val === '>');
            if (!isRun) {
              return;
            }
          }
        }
        currentDataQuery = inputBuffer.slice(triggerCharIndex + 1).join('');
        // fix bug firefox auto added <br> last text.
        currentDataQuery = utils.removeLastBr(currentDataQuery);
        inputBuffer = String(settings.triggerChar+currentDataQuery).split('');
        doSearch(currentDataQuery);
      } else {
        hideAutoComplete();
      }
    }

    function onInputBoxKeyPress(e) {
      var keyCode = (e.which || e.keyCode);
      if (keyCode !== KEY.BACKSPACE && keyCode !== KEY.LEFT && keyCode !== KEY.RIGHT &&
          keyCode !== KEY.DELETE && keyCode !== KEY.UP && keyCode !== KEY.DOWN) {
        var typedValue = String.fromCharCode(keyCode);
        if(keyCode === KEY.RETURN) {
          inputBuffer = [];
          typedValue = ' ';
        }
        inputBuffer.push(typedValue);
      }
      disabledPlaceholder();
    }

    function onInputBoxKeyDown(e) {
      isInput = false;
      if (utils.isIE) {
        elmInputBox.find('.cursorText').remove();
      }
      // 
      valueBeforMention = elmInputBox.value();

      // This also matches HOME/END on OSX which is CMD+LEFT, CMD+RIGHT
      if (((e.keyCode == KEY.LEFT || e.keyCode == KEY.RIGHT) && isBlockMenu === false) 
                                           || e.keyCode == KEY.HOME || e.keyCode == KEY.END) {
        // Defer execution to ensure carat pos has changed after HOME/END keys
        _.defer(resetBuffer);
        
        if (navigator.userAgent.indexOf("MSIE 9") > -1) {
          _.defer(updateValues);
        }
        return;
      }

      if (e.keyCode == KEY.BACKSPACE) {
        //inputBuffer.splice((inputBuffer.length - 1), 1);
        if (utils.isIE) {
          if (inputBuffer.length > 1 || (inputBuffer.length == 1 && utils.brVersion < 9)) {
            onInputBoxInput(e);
          } else {
            hideAutoComplete();
          }
        }
        
        return;
      }
      
      if (utils.isIE && mentionsCollection.length) {
        updateValues();
      }
      
      if (!elmAutocompleteList.is(':visible')) {
        return true;
      }
      
      switch (e.keyCode) {
        case KEY.UP:
        case KEY.DOWN:
          if(elmAutocompleteList.find('li.msg').length > 0) return false;
          var elmCurrentAutoCompleteItem = null;
          if (e.keyCode == KEY.DOWN) {
            if (elmActiveAutoCompleteItem && elmActiveAutoCompleteItem.length) {
              elmCurrentAutoCompleteItem = elmActiveAutoCompleteItem.next();
              if(elmCurrentAutoCompleteItem.length == 0) {
                elmCurrentAutoCompleteItem = elmAutocompleteList.find('li:first');
              }
            } else {
              elmCurrentAutoCompleteItem = elmAutocompleteList.find('li').first();
            }
          } else {
            elmCurrentAutoCompleteItem = $(elmActiveAutoCompleteItem).prev();
            if(elmCurrentAutoCompleteItem.length == 0) {
              elmCurrentAutoCompleteItem = elmAutocompleteList.find('li:last');
            }
          }
          
          if (elmCurrentAutoCompleteItem.length) {
            selectAutoCompleteElement(elmCurrentAutoCompleteItem);
          }
          isInput = true;
          return false;
          
        case KEY.RETURN:
        case KEY.TAB:
          if (elmActiveAutoCompleteItem && elmActiveAutoCompleteItem.length) {
            elmActiveAutoCompleteItem.trigger('mousedown');
          } else {
            var eN = $.Event("keydown", { keyCode : KEY.DOWN });
            $(this).trigger(eN);
          }
          e.stopPropagation();
          isInput = true;
          return false;
        default: {
          return true;
        }
      }
      return true;
    }

    function onInputBoxKeyUp(e) {
      //
      saveCaretPosition();
      
      checkRemoveMoreOneText(e);

      backspceBroswerFix(e);

      onInputBoxInput();

      checkAutoAddLink(e);

      if(elmInputBox.val().length === 0) {
        enabledPlaceholder();
      } else {
        disabledPlaceholder();
      }
    }

    function checkRemoveMoreOneText(e) {
      var currentValue = elmInputBox.value();
      var delta = valueBeforMention.length - currentValue.length;
      var isBackKey = (e.keyCode === KEY.BACKSPACE || e.keyCode === KEY.DELETE);
      var isReset = true;
      if (delta > 1 && (isBlockMenu === true) || (isBlockMenu === false && delta == 1 && isBackKey)) {
        triggerOninputMention(valueBeforMention, currentValue);
        isReset = false;
      } else if (isBackKey === true) {
        var textSizeMention = 63;
        if (delta > textSizeMention && !utils.isFirefox) {
          var indexChanged = utils.getCursorIndexOfText(valueBeforMention, currentValue);
          if (indexChanged >= 0 && 
                    $.trim(valueBeforMention.substr(indexChanged)).toLowerCase().indexOf('<span') === 0) {
            currentValue = insertCursorText(currentValue, indexChanged, true);
            elmInputBox.val(currentValue);
            autoSetKeyCode(elmInputBox);
            if (utils.isIE === false) {
              setCaretPosition(elmInputBox);
            }
            isReset = false;
          }
        } else if(delta == 1) {
          var buffer = inputBuffer.join('');
          var index = getIndexBufferChange(valueBeforMention, currentValue, buffer);
          index = (index <= 0) ? (inputBuffer.length - 1) : index;
          inputBuffer.splice(index, 1);
          isInput = false;
        }

      } else if (delta === -1) {

        if (isBlockMenu) {
          var buffer = inputBuffer.join('');
          if (currentValue.indexOf(buffer) < 0) {
            var index = getIndexBufferChange(valueBeforMention, currentValue, buffer);
            if (index > 0 && index < buffer.length) {
              var char_ = inputBuffer[inputBuffer.length - 1];
              inputBuffer.splice(index, 0, char_);
              inputBuffer.pop();
              isInput = false;
              isReset = false;
            } else {
              resetBuffer();
            }
          }
        }

      }
      
      if (e.keyCode === KEY.SPACE && 
          ((isBlockMenu === false) || 
              (isReset && (elmAutocompleteList.find('li.msg').length > 0 || inputBuffer.join().indexOf('  ') >= 0)))) {
        resetBuffer();
      }
      
    }
    
    function getIndexBufferChange(valueBeforMention, currentValue, buffer) {
      var indexChanged = utils.getCursorIndexOfText(valueBeforMention, currentValue);
      var index = valueBeforMention.indexOf(buffer.substring(0, buffer.length - 1));
      return indexChanged - index;
    }

    function triggerOninputMention(before, after) {
      var val = '';
      var cr = getCaretPosition();
      if(cr  > 0) {
        val = currentSelection.elm.textContent.substr(0, currentSelection.offset);
      }
      if(val.length <= 0) {
        var indexChanged = utils.getCursorIndexOfText(before, after);
        var fVal = after.substring(0, indexChanged);
        var lVal = after.substring(indexChanged, after.length);
        if(lVal.length > 0) {
          var t = lVal.indexOf(' ');
          var k = lVal.indexOf('<br>');
          t = (t > 0 && t < k) ? t : k;
          t = (t < 0) ? lVal.length : t;
          lVal = lVal.substring(0, t);
          
          fVal += $.trim(lVal);
        }
        val = fVal;
      }
      var hasTrigger = hasTriggerChar(val);
      if (hasTrigger != false) {
        inputBuffer = hasTrigger;
        isInput = false;
        valueBeforMention = '';
        onInputBoxInput();
      } else {
        resetBuffer();
      }
    }
    
    function hasTriggerChar(val) {
      //
      if (val && val.length > 0 && val.indexOf(settings.triggerChar) >= 0) {
        val = val.substring(val.indexOf(settings.triggerChar));
        var c = val[val.length-1].charCodeAt();
        if(val.indexOf(' ') < 0 && val.indexOf('&nbsp;') < 0 && val.indexOf('<') < 0 
            && c  !== 32 && c !== 159 && c !== 164 && c !== 160) {
          return val.split('');
        }
      }
      return false;
    }

    function backspceBroswerFix(e) {
      if (utils.isFirefox) {
        var selection = getSelection();
        var node = $(selection.focusNode);
        if (node.is('i') && node.hasClass('uiIconClose')) {
          node.trigger('click');
        }
      } else if (utils.isIE) {
        //var cRange = document.selection.createRange();
        //var node = $(cRange.parentElement());
      }
    }
    
    function checkAutoAddLink(e) {
      var keyCode = (e.which || e.keyCode);
      if (keyCode && keyCode === KEY.SPACE) {
        var val = getInputBoxFullValue();
        autoAddLink(val);
      }
    }
    
    function autoAddLink(val) {
      if (ActionLink.isRun && ActionLink.hasNotLink) {
        ActionLink.linkSaved = utils.searchFirstURL(val);
        if (ActionLink.linkSaved && ActionLink.linkSaved.length > 0) {
          var action = ActionLink.actionLink;
          action = $((typeof action === 'string') ? ('#' + action) : action);
          if (action.length > 0) {
            var input = $('#InputLink');
            if (input.length > 0) {
              ActionLink.hasNotLink = false;
              saveCacheData();
              input.val(ActionLink.linkSaved);
              action.trigger('click');
            }
          }
        }
      }
    }
    
    function autoSetKeyCode(elm) {
      disabledPlaceholder();
      resetBuffer();
      inputBuffer[0] = settings.triggerChar;
      valueBeforMention = '';
      //
      isInput = false;
      onInputBoxInput();
    }

    function hideAutoComplete(isClear) {
      if(isClear || isClear == null || isClear == undefined) {
        elmActiveAutoCompleteItem = null;
        hideDropdown().empty();
      } else {
        hideDropdown();
      }
    }

    function selectAutoCompleteElement(elmItem) {
      elmItem.addClass(settings.classes.autoCompleteItemActive);
      elmItem.siblings().removeClass(settings.classes.autoCompleteItemActive);
      elmActiveAutoCompleteItem = elmItem;
    }

    function selectAutoCompleteItem(e) {
      var elmItem = $(this);
      selectAutoCompleteElement(elmItem);
      e.stopPropagation();
    }

    function addMessageMenu(parent, msg) {
      $('<li class="msg"></li>')
          .html('<em>'+msg+'</em>')
          .appendTo(parent)
          .on('click mousedown', function(e) {
            e.stopPropagation();
            e.preventDefault();
          });
    }

    function showDropdown() {
      isBlockMenu = true;
      return elmAutocompleteList.show();
    }

    function hideDropdown() {
      isBlockMenu = false;
      return elmAutocompleteList.hide();
    }

    function populateDropdown(query, results) {
      showDropdown();
      elmAutocompleteList.empty();
      var elmDropDownList = $("<ul>").appendTo(elmAutocompleteList).hide();
      var isShow = true;
      //
      if(query === '' && !settings.firstShowAll) {
        addMessageMenu(elmDropDownList, settings.messages.helpSearch);
      }

      //
      if(query != '' && results === 'searching') {
        addMessageMenu(elmDropDownList, (settings.messages.searching + ' <strong>' + query + '</strong>.'));
      }

      //
      if ((results === null || results === undefined || results.length === 0) && query != '') {
        if(inputBuffer[inputBuffer.length - 1] != ' ') {
          addMessageMenu(elmDropDownList, (settings.messages.foundNoMatch + ' <strong>' + query + '</strong>.'));
        } else {
          resetBuffer();
          hideDropdown();
        }
      }
      
      if (results && results.length > 0 && results != 'searching' && (query != '' || settings.firstShowAll)) {
        _.each(results, function(item, index) {
          var itemUid = _.uniqueId('mention_');

          autocompleteItemCollection[itemUid] = _.extend({}, item, {
            value : item.name
          });

          var elmListItem = $(settings.templates.autocompleteListItem({
            'id' : utils.htmlEncode(item.id),
            'display' : utils.htmlEncode(item.name),
            'type' : utils.htmlEncode(item.type),
            'content' : (utils.highlightTerm(utils.htmlEncode(item.name), query) + ' (' + item.id.replace('@', '') + ')')
          })).attr('data-uid', itemUid);

          if (index === 0 && settings.selectFirst) {
            selectAutoCompleteElement(elmListItem);
          }

          if (settings.showAvatars) {
            var elmIcon;

            if (item.avatar) {
              elmIcon = $(settings.templates.autocompleteListItemAvatar({
                avatar : item.avatar
              }));
            } else {
              elmIcon = $(settings.templates.autocompleteListItemIcon({
                icon : item.icon
              }));
            }
            elmIcon.prependTo(elmListItem);
          }
          elmListItem = elmListItem.appendTo(elmDropDownList);
        });
      }
      
      elmDropDownList.show();
    }

    function resetInput() {
      elmInputBox.val('');
      mentionsCollection = [];
      updateValues();
    }

    function doSearch(query) {
      if ((typeof query === 'undefined' || $.trim(query) === '') && !settings.firstShowAll) {
        populateDropdown('', null);
      } else if (query.length >= settings.minChars) {
        if (settings.cacheResult.hasUse) {
          var data = getCaseSearch(query);
          if (data) {
            populateDropdown(query, data);
          } else {
            search(query);
          }
          clearCaseSearch();
        } else {
          search(query);
        }
      }
    }

    function search(query) {
      populateDropdown(query, 'searching');
      settings.onDataRequest.call(this, 'search', query, function(responseData) {
        populateDropdown(query, responseData);
        saveCaseSearch(query, responseData);
      });
    }

    function saveCaseSearch(id, obj) {
      if(settings.cacheResult.hasUse) {
        id = 'result' + ((id === ' ') ? '_20' : id);
        var data = elmInputBox.parent().data("CaseSearch");
        if (String(data) === "undefined") data = {};
        data[id] = obj;
        elmInputBox.parent().data("CaseSearch", data);
      }
    }

    function getCaseSearch(id) {
      id = 'result' + ((id === ' ') ? '_20' : id);
      var data = elmInputBox.parent().data("CaseSearch");
      return (String(data) === "undefined") ? data : data[id];
    }

    function clearCaseSearch() {
      elmInputBox.parent().stop().animate({
        'cursor' : 'default'
      }, settings.cacheResult.live, function() {
        $(this).data("CaseSearch", {});
      });
    }

    function saveCacheData() {
      var key = jElmTarget.attr('id');
      if (key) {
        var parentForm = jElmTarget.parents('form:first').parent();
        if (parentForm.length > 0) {
          var dataCache = parentForm.data(key);
          if (dataCache == null) {
            dataCache = new cacheMention();
          }
          dataCache.mentions = mentionsCollection;
          dataCache.val = getInputBoxFullValue();
          dataCache.data = mentionsCollection.length > 0 ? elmInputBox.data('messageText') : getInputBoxValue();
          dataCache.actionLink = ActionLink;
          parentForm.data(key, dataCache);
        }
      }
    }

    function updateCacheData() {
      var parentForm = jElmTarget.parents('form:first').parent();
      var key = jElmTarget.attr('id');
      if (key) {
        var dataCache = parentForm.data(key);
        if (dataCache == null) {
          resetInput();
        } else {
          mentionsCollection = dataCache.mentions;
          elmInputBox.val(dataCache.val);
          elmInputBox.data('messageText', dataCache.data);
          ActionLink = dataCache.actionLink;
          updateValues();
        }
      }
    }

    function clearCacheData() {
      var parentForm = jElmTarget.parents('form:first').parent();
      var key = jElmTarget.attr('id');
      if (key) {
        var dataCache = parentForm.data(key);
        if (dataCache != null) {
          parentForm.data(key, null);
        }
      }
    }

    function getTemplate() {
      var editableType = ($.browser.webkit) ? 'plaintext-only' : 'true';
      return $('<div contenteditable="' + editableType + '" g_editable="true" class="replaceTextArea editable"></div>');
    }

    function initDisplay(id, target) {
      var id_ = "Display" + id;
      var displayInput = target.find('#' + id_);
      if (displayInput.length === 0) {
        displayInput = getTemplate().attr('id', id_);
        displayInput.appendTo(target);
      }
      displayInput.val = function(v) {
        if (v === null || typeof v === "undefined") {
          var temp = $(this).clone();
          temp.find('span').find('i').remove();
          temp.find('.cursorText').remove();
          return utils.getSimpleValue(temp.html());
        } else {
          if (typeof v === 'object') {
            $(this).html('').append(v);
          } else {
            $(this).html(v);
          }

        }
      };
      displayInput.value = function() {
        var temp = $(this).clone();
        temp.find('span.none').remove();
        var val = temp.html(); temp.remove();
        val = val.replace(/&amp;/g, '&').replace(/&nbsp;/g, ' ');
        return val;
      };
      return displayInput;
    }
    
    function enabledPlaceholder() {
      var parent = elmInputBox.parent();
      parent.find('div.placeholder:first').show().css('top', '5px');;
      
      var isLinked = ($('#LinkTitle').length > 0);
      var action = $('#' + settings.idAction);
      if(isLinked === false && action.length > 0 && action.attr('disabled') === undefined) {
        $('#' + settings.idAction).attr('disabled', 'disabled').addClass('DisableButton');
      }
    }
    
    function disabledPlaceholder() {
      elmInputBox.parent().find('div.placeholder:first').hide().css('top', '-100px');
      var action = $('#' + settings.idAction);
      if (action.length > 0 && action.attr('disabled') === 'disabled') {
        action.removeAttr('disabled').removeClass('DisableButton');
      }
    }

// keep committed id: e7afc555114f27e20acc343b35c56e609958c534
    function getCaretPosition() {
      if(currentSelection.elm === null) {
        return (currentSelection.offset === null) ? -1 : currentSelection.offset;
      } else if(typeof currentSelection.elm === 'object') {
        if(currentSelection.elm === elmInputBox[0]) {
          return currentSelection.offset;
        }
        var tem = $('<div/>');
        var childs = elmInputBox[0].childNodes;
        for (var i = 0; i < childs.length; ++i) {
          var it = childs[i];
          if(it === currentSelection.elm) {
            break;
          } else {
            tem.append($(it).clone());
          }
        }
        var i = tem.html().length + currentSelection.offset;
        return i;
      }
      return -1;
    }
    
    function saveCaretPosition(elm, offset) {
      if(elm && (typeof offset) === 'number'){
        currentSelection.elm = elm;
        currentSelection.offset = offset;
      } else {
        elmInputBox.find('span.none').remove();
        var select = getSelection();
        var idx = select.baseOffset;
        var selectNode = select.anchorNode;
        if(selectNode && (selectNode === elmInputBox[0] || selectNode.parentNode === elmInputBox[0])) {
          currentSelection.elm = selectNode;
          if(selectNode === elmInputBox[0]) {
            currentSelection.offset = utils.removeLastBr(elmInputBox.value()).length;
          } else {
            currentSelection.offset = (select.baseOffset) ? select.baseOffset : ((select.anchorOffset) ? select.anchorOffset : 0);
          }
        }
      }
      saveCaretPositionIE();
    }

    function saveCaretPositionIE() {
      try {
        if($.browser.msie) {
          var selection= document.selection;
          var range = selection.createRange();
          var node = range.parentElement();
          var range = range.duplicate();
          var val = elmInputBox.value();
          range.moveEnd("character", val.length);
          var s = (range.text == "" ? val.length : val.lastIndexOf(range.text));
          range = selection.createRange().duplicate();
          range.moveStart("character", -val.length);
          
          var text = range.htmlText;
          if(text != null) {
            text = String(text).replace(/ id\=/, ' id_=').replace(/jQuery/g, 'jq');
            if(text.indexOf('</') > 0) {
              var jEml = $(text);
              text = (jEml.length > 0) ? jEml.find('.ReplaceTextArea').html() : text;
            }
            currentSelection.elm = node;
            if(text != null) {
              currentSelection.offset = text.length;
            } else {
              currentSelection.offset = -1;
            }
          }
        }
      } catch (err) {log(err); }
    }
    
    // Public methods
    return {
      init : function(domTarget) {
        window.jq = $;
        jElmTarget = $(domTarget);
        jElmTarget.css({
          'visibility' : 'hidden',
          'display' : 'none'
        });
        //
        jElmTarget.val('');

        elmInputBox = initDisplay(jElmTarget.attr('id'), jElmTarget.parent());

        initTextarea();
        initAutocomplete();
        updateCacheData();
        
        // set tabindex for input-box and action-button
        var tabIndex = String(new Date().getTime()).substring(10)*1+ 1;
        elmInputBox.attr('tabindex', tabIndex);

        // prefill mentions
        if (settings.prefillMention) {
          addMention(settings.prefillMention);
        }

        // action submit
        if (settings.idAction && settings.idAction.length > 0) {
          
          var actionLink = $('#' + settings.idAction);
          actionLink.on('mousedown keydown', function(evt) {
            evt.stopPropagation();
            if ($(this).hasClass('DisableButton')) {
              return;
            }
            if (evt.type === 'keydown') {
              var number = (evt.which || evt.keyCode);
              if (number !== KEY.RETURN && number !== KEY.SPACE) {
                return;
              }
            }
            if (evt.type === 'mousedown' && evt.button === 2) {
              return;
            }
            evt.preventDefault();
            var value = mentionsCollection.length ? elmInputBox.data('messageText') : getInputBoxValue();
            value = value.replace(/<br\/?>/gi, '\n').replace(/&lt;/gi, '<').replace(/&gt;/gi, '>');
            jElmTarget.val(value);
            clearCacheData();
            resetInput();
            $.globalEval($(this).data('actionLink'));
          });
          
          actionLink.attr('disabled', 'disabled').addClass('DisableButton');
          var actionClick = String(actionLink.attr('onclick')).replace('javascript:', '');
          actionLink.attr('data-action-link', actionClick);
          actionLink.removeAttr('onclick');
          actionLink.data('actionLink', actionClick);
          actionLink.attr('tabindex', tabIndex+1);
        }

        // add placeholder
        var title = jElmTarget.attr('title');
        if ($.trim(title).length > 0) {
          
          var placeholder = elmInputBox.parent().find('.placeholder');
          if(placeholder.length == 0) {
            placeholder = $('<div class="placeholder">' + title + '</div>');
            placeholder.appendTo(elmInputBox.parent());
          } else {
            placeholder.html(title);
          }
          placeholder.off('click').on('click', function() {
            elmInputBox.focus();
          }); 
          
          if(getInputBoxValue().length > 0) {
            disabledPlaceholder();
          } else {
            enabledPlaceholder();
          }
        }
        
        // action mention
        if (settings.actionMention && settings.actionMention.length > 0) {
          var action = null;
          if(typeof settings.actionMention ==='string') {
            action = $('#'+settings.actionMention)
          } else if(typeof settings.actionMention === 'object') {
            action = $(settings.actionMention);
          }
          if(action && action.length > 0) {
            action.on('click', function(e) {
              e.stopPropagation();

              var val = elmInputBox.value();
              if(val.indexOf('class="cursorText"') < 0) {
                
                /**
                 * case 1: 
                 *  + Selected element is not menstion-input
                 *  + Browser is can not support HTML5.
                 *  + Value of input is empty.
                 *  ==> Add key @ last of content and auto show suggestion list.
                 */
                var caretIndex = getCaretPosition();
                if(val.length == 0 || utils.brVersion < 7 || caretIndex == undefined || caretIndex < 0) {
                  val = insertCursorText(val.replace(/&nbsp;/g, ' '), -1, true).replace(/&nbsp;/, '');
                } else {
                /**
                 * case 2: 
                 *  + Selected element is menstion-input
                 *  + Browser can support HTML5.
                 *  ==> Add key @ has posision is index of caret text and auto show suggestion list.
                 */
                  val = insertCursorText(val, caretIndex, true);
                }
                elmInputBox.val(val);
                autoSetKeyCode(elmInputBox);
                setCaretPosition(elmInputBox);
              }

            });
          }
        }

      },

      val : function(callback) {
        if (!_.isFunction(callback)) {
          return;
        }
        var value = mentionsCollection.length ? elmInputBox.data('messageText') : getInputBoxValue();
        callback.call(this, value);
      },

      clearLink : function(callback) {
        ActionLink.hasNotLink = true;
        ActionLink.linkSaved = '';
        saveCacheData();
      },

      showButton : function(callback) {
        var action = $('#' + settings.idAction);
        if (action.length > 0 && action.attr('disabled') === 'disabled') {
          action.removeAttr('disabled').removeClass('DisableButton');
        }
      },

      reset : function() {
        resetInput();
      },

      getMentions : function(callback) {
        if (!_.isFunction(callback)) {
          return;
        }
        callback.call(this, mentionsCollection);
      }
    };
  };

  // elastic the mention content
  $.fn.extend({
    elastic : function(settings) {
      elasticStyle = settings.elasticStyle;
      if (elasticStyle && typeof elasticStyle === 'object' && parseInt(elasticStyle.maxHeight) > 0) {
        return this.each(function() {
          
          var delta = parseInt(elasticStyle.maxHeight) - parseInt(elasticStyle.minHeight);
          $(this).css({
            'height' : elasticStyle.minHeight,
            'marginBottom' : (delta + elasticStyle.marginButton) + 'px'
          });
          
          if(delta > 0) {
            $(this).data('elasticStyle', {
              'maxHeight' : elasticStyle.maxHeight,
              'minHeight' : elasticStyle.minHeight,
              'marginButton' : elasticStyle.marginButton,
              'enableMargin' : String(elasticStyle.enableMargin),
              'delta' : delta
            });
            $(this).on('focus keyup', function() {
              var elasticStyle_ = $(this).data('elasticStyle');
              if ($(this).height() < parseInt(elasticStyle_.maxHeight)) {
                var animateCSS = {'height' : elasticStyle_.maxHeight};
                if(elasticStyle_.enableMargin === 'true') {
                  animateCSS = {
                    'height' : elasticStyle_.maxHeight,
                    'marginBottom' : elasticStyle_.marginButton
                  };
                }
                $(this).animate(animateCSS, 100, function() {});
              }
            }).on('blur', function() {
              var val = $.trim($(this).html());
              val = utils.getSimpleValue(val);
              if (val.length == 0) {
                var elasticStyle_ = $(this).data('elasticStyle');
                var animateCSS = {'height' : elasticStyle_.minHeight};
                if(elasticStyle_.enableMargin === 'true') {
                  animateCSS = {
                    'height' : elasticStyle_.maxHeight,
                    'marginBottom' : (elasticStyle_.delta + parseInt(elasticStyle_.marginButton)) + 'px'
                  };
                }
                $(this).animate(animateCSS, 300, function() {});
              }
            });
          }
        });
      }
    }
  });

  $.fn.exoMentions = function(method, settings) {

    var outerArguments = arguments;

    if (typeof method === 'object' || !method) {
      settings = method;
    }

    return this.each(function() {
      var instance = $.data(this, 'exoMentions') || $.data(this, 'exoMentions', new eXoMentions(settings));
      if (_.isFunction(instance[method])) {
        return instance[method].apply(this, Array.prototype.slice.call(outerArguments, 1));
      } else if (typeof method === 'object' || !method) {
        return instance.init.call(this, this);
      } else {
        $.error('Method ' + method + ' does not exist');
      }
    });
  };

})(jQuery, mentions._);
