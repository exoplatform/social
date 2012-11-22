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
    elastic : true,
    elasticStyle : {},
    idAction : "",
    classes : {
      autoCompleteItemActive : "active"
    },
    cacheResult : {
      hasUse : true,
      live : 30000 // MiniSeconds
    },
    templates : {
      wrapper : _.template('<div class="exo-mentions"></div>'),
      autocompleteList : _.template('<div class="autocomplete-menu"></div>'),
      autocompleteListItem : _.template('<li data-ref-id="<%= id %>" data-ref-type="<%= type %>" data-display="<%= display %>"><%= content %></li>'),
      autocompleteListItemAvatar : _.template('<img  src="<%= avatar %>" />'),
      autocompleteListItemIcon : _.template('<div class="icon <%= icon %>"></div>'),
      mentionItemSyntax : _.template('<%=id%>'),
      mentionItemHighlight : _.template('<strong><span><%= value %></span></strong>')
    }
  };

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
  }

  
  var utils = {
    htmlEncode : function(str) {
      return _.escape(str);
    },
    highlightTerm : function(value, term) {
      if (!term && !term.length) {
        return value;
      }
      return value.replace(new RegExp("(?![^&;]+;)(?!<[^<>]*)(" + term + ")(?![^<>]*>)(?![^&;]+;)", "gi"), "<b>$1</b>");
    },
    rtrim : function(string) {
      return string.replace(/\s+$/, "");
    },
    getSimpleValue : function(val) {
      return val.replace(/&amp;/g, '&').replace(/&nbsp;/g, ' ')
                .replace(/<span.*?>/gi, '').replace(/<\/span>/gi, '')
                .replace(/<br.*?>/g, '').replace(/\n/g, '<br />');
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
        if ($.trim(before.substr(t)).toLowerCase().indexOf('<span') === 0) {
          return t;
        }
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
          info.from = i - 1;
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
    isIE : ($.browser.msie),
    isFirefox : ($.browser.mozilla)
  };

  var eXoMentions = function(settings) {

    var jElmTarget, elmInputBox, elmInputWrapper, elmAutocompleteList, elmWrapperBox, elmActiveAutoCompleteItem;
    var mentionsCollection = [];
    var autocompleteItemCollection = {};
    var inputBuffer = [];
    var currentDataQuery = '';

    settings = $.extend(true, {}, defaultSettings, settings);

    KEY.MENTION = settings.triggerChar.charCodeAt(0);

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
      elmInputBox.on('input', onInputBoxInput);
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
      elmAutocompleteList.on('mousedown', 'li', onAutoCompleteItemClick);
      elmAutocompleteList.on('mouseover', 'li', selectAutoCompleteItem);
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
      var regex = new RegExp("\\" + settings.triggerChar + currentDataQuery, "gi");
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
      var updatedMessageText = start + addItemMention(mention.value) + end;

      elmInputBox.val(updatedMessageText);

      initClickMention();
      setCaratPosition(elmInputBox);

    }


    function addItemMention(value) {
      var val = '<span contenteditable="false">' + value + '<span class="icon"' + ((utils.isFirefox) ? 'contenteditable="true"' : '') + '>x</span></span>'
          + '&nbsp;<div id="cursorText"></div>';
      return val;
    }

    function initClickMention() {
      var sp = elmInputBox.find('> span');
      if (sp.length > 0) {
        $.each(sp, function(index, item) {
          var sp = $(item).find('span');
          sp.data('indexMS', {
            'indexMS' : index
          }).off('click');
          sp.on('click', function(e) {
            var t = $(this).data('indexMS').indexMS;
            mentionsCollection.splice(t, 1);
            $(this).parent().remove();
            updateValues();
            saveCacheData();
            initClickMention();
            e.stopPropagation();
          });
          $(item).on('click', function() {
            var selection = getSelection();
            if (selection) {
              try {
                var range = document.createRange();
                range.selectNodeContents(this);
                range.selectNode(this);

                selection.removeAllRanges();
                selection.addRange(range);
              } catch(err) {}
            }
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

    function setCaratPosition(inputField) {
      if (inputField) {
        var cursorText = inputField.find('#cursorText');
        if (inputField.val().length != 0) {
          
          var elm = inputField[0];
          var selection = getSelection();
          if (selection) {
            cursorText.attr('contenteditable', 'true').css({
              'display' : 'inline',
              'height' : '14px'
            }).html('&nbsp;&nbsp;&nbsp;');
            cursorText.focus();
            try{
              var range = document.createRange();
              range.selectNode(cursorText[0]);
              range.selectNodeContents(cursorText[0]);

              selection.removeAllRanges();
              selection.addRange(range);
            }catch(err) {
              inputField.focus();
            }
          }
        }
        cursorText.remove();
        inputField.focus();
        updateValues();
      }
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
      }, 100, function() {
        var after = $.trim(elmInputBox.value());
        var info = utils.getIndexChange(before, after);
        var text = after.substr(info.from, info.to);
        var nt = text.replace(new RegExp("(<[a-z0-9].*?>)(.*)(</[a-z0-9].*?>)", "gi"), "$2");
        if (nt.length < text.length) {
          after = after.substr(0, info.from) + $('<div/>').html(text).text() + after.substr(info.to) + ' <div id="cursorText"></div>';
          elmInputBox.val(after);
          setCaratPosition(elmInputBox);
        }
        elmInputBox.css('cursor', 'text');
        elmInputBox.parent().find('.placeholder').hide();
      });

      // 
      return;
    }

    function onInputBoxClick(e) {
      resetBuffer();
    }

    function onInputBoxBlur(e) {
      hideAutoComplete();
      saveCacheData();
      var plsd = $(this).parent().find('div.placeholder:first');
      if (plsd.length > 0 && $.trim(elmInputBox.val()).length === 0) {
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
      if (e.keyCode !== KEY.BACKSPACE) {
        var typedValue = String.fromCharCode(e.which || e.keyCode);
        inputBuffer.push(typedValue);
        if (utils.isIE) {
          onInputBoxInput(e);
        }
      }
      var plsd = $(this).parent().find('div.placeholder:first');
      if (plsd.length > 0) {
        plsd.hide();
      }
    }

    function onInputBoxKeyDown(e) {
      // Run without IE
      if (String.fromCharCode(e.which || e.keyCode) === settings.triggerChar) {
        onInputBoxInput(e);
      }
      
      // This also matches HOME/END on OSX which is CMD+LEFT, CMD+RIGHT
      if (e.keyCode == KEY.LEFT || e.keyCode == KEY.RIGHT || e.keyCode == KEY.HOME || e.keyCode == KEY.END) {
        // Defer execution to ensure carat pos has changed after HOME/END keys
        _.defer(resetBuffer);
        
        // IE9 doesn't fire the oninput event when backspace or delete is
        // pressed. This causes the highlighting
        // to stay on the screen whenever backspace is pressed after a
        // highlighed word. This is simply a hack
        // to force updateValues() to fire when backspace/delete is pressed in
        // IE9.
        if (navigator.userAgent.indexOf("MSIE 9") > -1) {
          _.defer(updateValues);
        }
        return;
      }
      
      if (e.keyCode == KEY.SPACE) {
        inputBuffer = [];
      }
      if (e.keyCode == KEY.BACKSPACE) {
        inputBuffer.splice((inputBuffer.length - 1), 1);
        if (utils.isIE) {
          if (inputBuffer.length > 1 || (inputBuffer.length == 1 && $.browser.version < 9)) {
            onInputBoxInput();
          } else {
            hideAutoComplete();
          }
        }
        var plsd = $(this).parent().find('div.placeholder:first');
        if (plsd.length > 0 && $.trim(elmInputBox.val()).length === 1) {
          plsd.show();
        } else {
          var before = elmInputBox.value();
          elmInputBox.animate({
            'cursor' : 'wait'
          }, 100, function() {
            var after = elmInputBox.value();
            var delta = before.length - after.length;
            var textSizeMention = 63;
            if (delta > textSizeMention) {
              var i = utils.getCursorIndexOfText(before, after);
              if (i >= 0) {
                after = after.substr(0, i) + ' @<div id="cursorText"></div>' + after.substr(i, after.length);
                after = after.replace(/  @/g, ' @');
                elmInputBox.val(after);
                autoSetKeyCode(elmInputBox);
                setCaratPosition(elmInputBox);
              }
            } else if (delta == 1 && after[after.length - 1] === '@') {
              autoSetKeyCode(elmInputBox);
            }
            elmInputBox.css('cursor', 'text');
          });
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
          return false;
          
        case KEY.RETURN:
        case KEY.TAB:
          if (elmActiveAutoCompleteItem && elmActiveAutoCompleteItem.length) {
            elmActiveAutoCompleteItem.trigger('mousedown');
            e.stopPropagation();
            return false;
          }
        default: {
          return true;
        }
      }
      return true;
    }

    function autoSetKeyCode(elm) {
      try {
        if(utils.isIE && $.browser.version < 9) {
          onInputBoxInput();
        } else {
          var e = jQuery.Event("keypress", {
            keyCode : KEY.MENTION,
            charCode : settings.triggerChar
          });
          var e1 = jQuery.Event("keydown", {
            keyCode : KEY.MENTION,
            charCode : settings.triggerChar
          });
          elm.triggerHandler(e);
          elm.trigger(e1);
        }
        resetBuffer();
        inputBuffer[0] = settings.triggerChar;
      } catch(err) {}
    }

    function hideAutoComplete() {
      elmActiveAutoCompleteItem = null;
      elmAutocompleteList.empty().hide();
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

    function populateDropdown(query, results) {
      elmAutocompleteList.show();

      if (!results.length) {
        hideAutoComplete();
        return;
      }

      elmAutocompleteList.empty();
      var elmDropDownList = $("<ul>").appendTo(elmAutocompleteList).hide();

      _.each(results, function(item, index) {
        var itemUid = _.uniqueId('mention_');

        autocompleteItemCollection[itemUid] = _.extend({}, item, {
          value : item.name
        });

        var elmListItem = $(settings.templates.autocompleteListItem({
          'id' : utils.htmlEncode(item.id),
          'display' : utils.htmlEncode(item.name),
          'type' : utils.htmlEncode(item.type),
          'content' : (utils.highlightTerm(utils.htmlEncode((item.name + ' (' + item.id.replace('@', '') + ')')), query))
        })).attr('data-uid', itemUid);

        if (index === 0) {
         // selectAutoCompleteElement(elmListItem);
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
      elmAutocompleteList.show();
      elmDropDownList.show();
    }

    function resetInput() {
      elmInputBox.val('');
      mentionsCollection = [];
      updateValues();
    }

    function doSearch(query) {
      if (query === '' || String(query) === 'undefined')  query = ' ';
      if (query.length >= settings.minChars) {
        if(settings.cacheResult.hasUse) {
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
        'cursor' : 'none'
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
      return $('<div contenteditable="true" g_editable="true" class="ReplaceTextArea editable"></div>');
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
          temp.find('.icon').remove();
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
        var val = $(this).html().replace(/&amp;/g, '&').replace(/&nbsp;/g, ' ').replace(/<br.*?>/g, '').replace(/\n/g, '<br />');
        return val;
      };
      return displayInput;
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

        // add placeholder
        if ($.trim(elmInputBox.val()).length == 0) {
          var title = jElmTarget.attr('title');
          var placeholder = $('<div class="placeholder">' + title + '</div>').attr('title', title);
          placeholder.on('click', function() {
            elmInputBox.focus();
          });
          placeholder.appendTo(elmInputBox.parent());
        }
        // prefill mentions
        if (settings.prefillMention) {
          addMention(settings.prefillMention);
        }
        // action submit
        if (settings.idAction && settings.idAction.length > 0) {
          $('#' + settings.idAction).on('mousedown', function() {
            var value = mentionsCollection.length ? elmInputBox.data('messageText') : getInputBoxValue();

            value = value.replace(/&lt;/gi, '<').replace(/&gt;/gi, '>');
            jElmTarget.val(value);
            clearCacheData();
            resetInput();
          });
        }
      },

      val : function(callback) {
        if (!_.isFunction(callback)) {
          return;
        }
        var value = mentionsCollection.length ? elmInputBox.data('messageText') : getInputBoxValue();
        callback.call(this, value);
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
      if (elasticStyle && typeof elasticStyle === 'object') {
        return this.each(function() {
          var delta = parseInt(elasticStyle.maxHeight) - parseInt(elasticStyle.minHeight);
          $(this).css({
            'height' : elasticStyle.minHeight,
            'marginBottom' : (delta + 4) + 'px'
          });
          $(this).data('elasticStyle', {
            'maxHeight' : elasticStyle.maxHeight,
            'minHeight' : elasticStyle.minHeight,
            'delta' : delta
          }).on('focus keyup', function() {
            var maxH = $(this).data('elasticStyle').maxHeight;
            if ($(this).height() < parseInt(maxH)) {
              $(this).animate({
                'height' : maxH,
                'marginBottom' : '4px'
              }, 100, function() {});
            }
          }).on('blur', function() {
            var val = $.trim($(this).html());
            val = utils.getSimpleValue(val);
            if (val.length == 0) {
              $(this).animate({
                'height' : $(this).data('elasticStyle').minHeight,
                'marginBottom' : ($(this).data('elasticStyle').delta + 4) + 'px'
              }, 300, function() {});
            }
          });
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

})(jQuery, mentions.underscore);
