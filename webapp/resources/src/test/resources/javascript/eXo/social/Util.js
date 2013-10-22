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
 * Util.js
 * Utility class
 * @author	<a href="mailto:hoatlevan@gmail.com">hoatle</a>
 * @since	Oct 20, 2009
 * @copyright	eXo Platform SEA
 */


/*
*Social jQuery plugin
*/ 
// Placeholder plugin for HTML 5
(function($) {
  
  var portal = window.eXo.env.portal

  window.eXo.social = window.eXo.social || {};
  window.eXo.social.portal = {
    rest : (portal.rest) ? portal.rest : 'rest',
    portalName : (portal.portalName) ? portal.portalName : 'classic',
    context : (portal.context) ? portal.context : '/intranet',
    accessMode: (portal.accessMode) ? portal.accessMode : 'public',
    userName : (portal.userName) ? portal.userName : ''
  };
   
	function Placeholder(input) {
    this.input = input;

    // In case of submitting, ignore placeholder value
    $(input[0].form).submit(function() {
        if (input.hasClass('placeholder') && input.val() == input.attr('placeholder')) {
            input.val('');
        }
    });
	}
	
	Placeholder.prototype = {
    show : function(loading) {
        if (this.input.val() === '' || (loading && this.isDefaultPlaceholderValue())) {
            this.input.addClass('placeholder');
            this.input.val(this.input.attr('placeholder'));
        }
    },
    hide : function() {
        if (this.isDefaultPlaceholderValue() && this.input.hasClass('placeholder')) {
            this.input.removeClass('placeholder');
            this.input.val('');
        }
    },
    isDefaultPlaceholderValue : function() {
        return this.input.val() == this.input.attr('placeholder');
    }
	};
	
	var HAS_SUPPORTED = !!('placeholder' in document.createElement('input'));
	
	$.fn.placeholder = function() {
    return HAS_SUPPORTED ? this : this.each(function() {
        var input = $(this);
        var placeholder = new Placeholder(input);
        
        placeholder.show(true);
        
        input.focus(function() {
            placeholder.hide();
        });
        
        input.blur(function() {
            placeholder.show(false);
        });
    });
	};


    $.fn.autosuggest = function(url, options) {
      var KEYS = {
        ENTER : 13,
        DOWN : 40,
        UP : 38
      },
      DELIMITER = ',',
      DELIMITER_AND_SPACE = ', ';

	    var COLOR = {
	      FOCUS : "#000000",
	      BLUR : "#C7C7C7"
	    };

      var defaults = {
        defaultVal: undefined,
        onSelect: undefined,
        maxHeight: 150,
        multisuggestion : false,
        width: undefined
      };

      options = $.extend(defaults, options);

     return this.each(function() {

	      var input = $(this),
	          results = $('<div />'),
	          currentSelectedItem, posX, posY;

        $(results).addClass('suggestions')
                    .css({
                      'top': input.position().top + input.outerHeight() + 'px',
                      'left': input.position().left + 'px',
                      'width': input.outerWidth() + 'px'
                    })
                    .hide();

        // append to target input
	      input.after(results)
	           .keyup(keysActionListener)
	           .blur(function(e) {
	                var resPos = $(results).offset();
	                
	                resPosBottom = resPos.top + $(results).height();
	                resPosRight = resPos.left + $(results).width();
	                
	                if (posY < resPos.top || posY > resPosBottom || posX < resPos.left || posX > resPosRight) {
                    $(results).hide();
	                }

	                if ($(this).val().trim().length == 0) {
	                  $(input).val(options.defaultVal);
	                  $(input).css('color', COLOR.BLUR);
	                }
	           })
	           .focus(function(e) {
	                $(results).css({
	                  'top': input.position().top + input.outerHeight() + 'px',
	                  'left': input.position().left + 'px'
	                });

	                if ($('div', results).length > 0) {
	                  $(results).show();
	                }
	                
	                if (options.defaultVal && $(this).val() == options.defaultVal) {
	                  $(this).val('');
	                  $(this).css('color', COLOR.FOCUS);
	                } 
	                
	           })
	           .attr('autocomplete', 'off');

	        function buildResults(searchedResults) {
	            var i, iFound = 0;

	            $(results).html('').hide();

              if (searchedResults == null) return;

	            // build list of item over searched result
	            for (i = 0; i < searchedResults.length; i += 1) {
                var item = $('<div />'),
                    text = searchedResults[i];

                $(item).append('<p class="text">' + text + '</p>');

                if (typeof searchedResults[i].extra === 'string') {
                  $(item).append('<p class="extra">' + searchedResults[i].extra + '</p>');
                }

                $(item).addClass('resultItem')
                    .click(function(n) { return function() {
                      selectResultItem(searchedResults[n]);
                    };}(i))
                    .mouseover(function(el) { return function() {
                      changeHover(el);
                    };}(item));

                $(results).append(item);

                iFound += 1;
                if (typeof options.maxResults === 'number' && iFound >= options.maxResults) {
                  break;
                }
	            }

	            if ($('div', results).length > 0) { // if have any element then display the list
                currentSelectedItem = undefined;
                $(results).show().css('height', 'auto');
                if ($(results).height() > options.maxHeight) {
                    $(results).css({'overflow': 'auto', 'height': options.maxHeight + 'px'});
                }
	            }
	        };

	        function reloadData() {
	          var val = input.val();
	          var search_str;
	          
	          if (val.length > 0) val = $.trim(val);
	          
	          if (options.multisuggestion) {
	            search_str = getSearchString(val);
	          } else {
	            search_str = val;
	          }
	          
	          var restUrl = url.replace('input_value', search_str);
	          $.ajax({
	                  type: "GET",
	                  url: restUrl,
	                  complete: function(jqXHR) {
					            if(jqXHR.readyState === 4) {
					              buildResults($.parseJSON(jqXHR.responseText).names);
					            }
	                  }
	          })
	        };

	        function selectResultItem(item) {

	          setValues(item);
	          
	          $(results).html('').hide();
	          if (typeof options.onSelect === 'function') {
	            options.onSelect(item);
	          }
	        };

          function getSearchString(val) {
			      var arr = val.split(DELIMITER);
			      return $.trim(arr[arr.length - 1]);
			    };

	        function changeHover(element) {
            $('div.resultItem', results).removeClass('hover');
            $(element).addClass('hover');
            currentSelectedItem = element;
          };

          function setValues(item) {
            var currentVals = $.trim(input.val());
            var selectedVals;
            
            if (options.multisuggestion) {
	            if(currentVals.indexOf(DELIMITER) >= 0) {
	              selectedVals = currentVals.substr(0, currentVals.lastIndexOf(DELIMITER)) + DELIMITER_AND_SPACE + item;
	              input.val(selectedVals);
	            } else {
	              input.val(item);
	            }
	          } else {
	            input.val(item);
	          }
          };
          
	        function keysActionListener(event) {
	          var keyCode = event.keyCode || event.which;

	          switch (keyCode) {
	            case KEYS.ENTER:
	                if (options.multisuggestion) {
	                   $(currentSelectedItem).trigger('click');
                     return false;
	                }
	                
	                if (currentSelectedItem) {
                    $(currentSelectedItem).trigger('click');
	                } else {
	                  options.onSelect();
	                }

	                return false;
	            case KEYS.DOWN:
	                if (typeof currentSelectedItem === 'undefined') {
	                  currentSelectedItem = $('div.resultItem:first', results).get(0);
	                } else {
	                  currentSelectedItem = $(currentSelectedItem).next().get(0);
	                }

	                changeHover(currentSelectedItem);
	                if (currentSelectedItem) {
	                  $(results).scrollTop(currentSelectedItem.offsetTop);
	                }

	                return false;
	            case KEYS.UP:
	                if (typeof currentSelectedItem === 'undefined') {
	                  currentSelectedItem = $('div.resultItem:last', results).get(0);
	                } else {
	                  currentSelectedItem = $(currentSelectedItem).prev().get(0);
	                }

	                changeHover(currentSelectedItem);
	                if (currentSelectedItem) {
	                  $(results).scrollTop(currentSelectedItem.offsetTop);
	                }

	                return false;
	            default:
	                reloadData.apply(this, [event]);
	          }
	        };

	        $('body').mousemove(function(e) {
            posX = e.pageX;
            posY = e.pageY;
          });
	    });
    };

    $.fn.toolTip = function(url, settings) {

        var defaultSettings = {
	        className   : 'UserName',
	        color       : 'yellow',
	        onHover     : undefined,
	        timeout     : 300
        };
        
        /* Combining the default settings object with the supplied one */
        settings = $.extend(defaultSettings, settings);

        return this.each(function() {

            var elem = $(this);
            
            // Continue with the next element in case of not effected element
            if(!elem.hasClass(settings.className)) return true;
            
            var scheduleEvent = new eventScheduler();
            var tip = new Tip();

            elem.append(tip.generate()).addClass('UIToolTipContainer');

            elem.addClass(settings.color);
            
            elem.hover(function() {
              reLoadPopup();
	            tip.show();
	            scheduleEvent.clear();
            },function(){
	            scheduleEvent.set(function(){
	              tip.hide();
	            }, settings.timeout);
            });
            
            function reLoadPopup() {
              var hrefValue = elem.attr('href');
              var personId = hrefValue.substr(hrefValue.lastIndexOf("/") + 1);
              
              var restUrl = url.replace('person_Id', personId);
              
              $.ajax({
                      type: "GET",
                      url: restUrl,
                      complete: function(jqXHR) {
                                if(jqXHR.readyState === 4) {
                                  var avatarURL = ($.parseJSON(jqXHR.responseText)).avatarURL;
										              var activityTitle = ($.parseJSON(jqXHR.responseText)).activityTitle;
										              var relationStatus = ($.parseJSON(jqXHR.responseText)).relationshipType;
										              
										              var html = [];
						                      html.push('<div style="float: right; cursor:pointer;">');
						                      html.push('  <div id="ClosePopup" class="ClosePopup" title="Close">[x]</div>');
						                      html.push('</div>');
						                      html.push('<div id="UserAvatar" class="UserAvatar">');
						                      html.push('  <img title="Avatar" alt="Avatar" src="' + avatarURL + '"></img>'); 
						                      html.push('</div>');
						                      html.push('<div id="UserTitle" class="UserTitle">');
						                      html.push('  <span>');
						                      html.push(     activityTitle);
						                      html.push('  </span>');
						                      html.push('</div>');
						                      html.push('<div id="UserAction" class="UserAction">');
						                      html.push('<span>');
										              html.push('</span>');
						                      html.push('</div>');
						                      $('.UIToolTip').html(html.join(''));
                                }
                      }
              })
            };
            
            function buildContent(resp) {
              
            }
        });
    };


    function eventScheduler(){};
    
    eventScheduler.prototype = {
      set : function (func,timeout){
        this.timer = setTimeout(func,timeout);
      },
      clear: function(){
        clearTimeout(this.timer);
      }
    };

    function Tip(){
	    this.shown = false;
    };
    
    Tip.prototype = {
	    generate: function(){
	        return this.tip || (this.tip = $('<span class="UIToolTip"><span class="pointyTipShadow"></span><span class="pointyTip"></span></span>'));
	    },
	    show: function(){
	        if(this.shown) return;
	        
	        this.tip.css('margin-left',-this.tip.outerWidth()/2).fadeIn('fast');
	        this.shown = true;
	    },
	    hide: function(){
	        this.tip.fadeOut();
	        this.shown = false;
	    }
    };
})(jQuery);