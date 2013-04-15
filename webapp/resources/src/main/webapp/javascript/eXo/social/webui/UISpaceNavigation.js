/**
 * Copyright (C) 2009 eXo Platform SAS.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

(function($) {
var UISpaceNavigation = {
    addEditability: function(id, moreLabel) {
    var editedTab = $("#" + id);
  
    function autoMoveApps(){
	    var ul = $('#spaceMenuTab');
	    
	    var maxWith = ul.outerWidth();
	    var liElements = ul.find('li.item');
	    var w = 0, index = 0;
	    for (var i = 0; i < liElements.length; ++i) {
	        var wElm = liElements.eq(i).outerWidth();
	        if((w + wElm) < maxWith) {
	            w += wElm;
	            index++;
	        } else {
	            break;
	        }
	    }

	    UISpaceNavigation.initNavigation(index, moreLabel);
    };
  
    function reset() {
	    var ul = $('#spaceMenuTab');
	    var liElements = ul.find('li.item');
	
	    var temp = $('<ul></ul>');
	    temp.append(liElements);
	    ul.empty().append(temp.find('li.item'));
    };
    
    $(document).ready(function(){
	    var ul = $('#spaceMenuTab');
	    var liElements = ul.find('> li');
	    liElements.addClass('item');
	    autoMoveApps(); 
    });

    $(window).resize(function(){
        reset();
        autoMoveApps(); 
    });
    
    
    
    editedTab.on("dblclick", ".active span", function() {
      var span = $(this);
      showEditLabelInput(this, span.attr("id"), span.text()); 
    });
    
	  function showEditLabelInput(target, nodeName, currentLabel) {
	    var jqObj = $(target);
	
	    var input = $("<input>").attr({type : "text", id : nodeName, name : currentLabel, value : currentLabel, maxLength : 50});
	    input.css("border", "1px solid #b7b7b7").css("width", (target.offsetWidth - 2) + "px").css("display", "block").css("height", "20px");
	
	    jqObj = jqObj.replaceWith(input);
	    input.blur(function() {
	      $(this).replaceWith(jqObj);
	    });
	
	    input.keypress(function(e) {
	      var keyNum = e.keyCode ? e.keyCode : e.which;
	      if (keyNum == 13) {
	        renameAppLabel($(this));
	      } else if (keyNum == 27) {
	        $(this).replaceWith(jqObj);
	      }
	    });
	
	    input.closest(".UITab").addClass("EditTab");
	    input.focus();
	  };
	  
	  function renameAppLabel(input) {
	    var newLabel = input.val();
	    if (newLabel && newLabel.length > 0) {
	      var portletID = input.closest(".PORTLET-FRAGMENT").parent().attr("id");
	
	      var href = eXo.env.server.portalBaseURL + "?portal:componentId=" + portletID;
	      href += "&portal:type=action";
	      href += "&portal:isSecure=false";
	      href += "&uicomponent=UISpaceMenu";
	      href += "&op=RenameSpaceAppName";
	      href += "&newSpaceAppName=" + encodeURIComponent(newLabel);
	      window.location = href;
	    }
	  };
	},
	initNavigation : function(index, moreLabel) {
	  //
    var uiSpaceMenu = $('#UISpaceMenu');
    var tabContainer = uiSpaceMenu.find('ul#spaceMenuTab');
    var tabs = tabContainer.find('li.item');
    
    var dropDownMenu = $('<ul/>', {
      'class' : 'dropdown-menu'
    });

    var dropDownToggle = $('<a/>', {
      'href' : '#',
      'class' : 'dropdown-toggle',
      'data-toggle' : 'dropdown'
    }).append($('<i/>', {
                          'class' : 'uiIconAppMoreButton'
                        }))
      .append($('<span/>', {
                             'text' : moreLabel
                           })
      );

    // clear
    tabContainer.empty();

    // rebuild
    $.each(tabs, function(idx, el) {
      if (idx < index - 1) {
        tabContainer.append(el);
      } else {
        dropDownMenu.append(el);
      }
    });
    
    if (dropDownMenu.children().length == 1) {
      var el = dropDownMenu.children(':first');
      dropDownMenu.remove();
      tabContainer.append(el);
    } else if (dropDownMenu.children().length > 1) {
	    var dropDown = $('<li/>', {
	      'class' : 'dropdown pull-right'
	    }).append(dropDownToggle).append(dropDownMenu);
      
      tabContainer.append(dropDown);
    };
    
    // swap position if needed
    var swappedEl = $(dropDown).find('li.active');
    if ( swappedEl.length > 0 ) {
      var targetEl = $(dropDown).prevAll('li:first');
      var copy_to = $(swappedEl).clone(true);
      var copy_from = $(targetEl).clone(true);
      $(swappedEl).replaceWith(copy_from);
      $(targetEl).replaceWith(copy_to);
    }
    
    $(tabContainer).css({"visibility":"visible"});
	}
};

return UISpaceNavigation;
})(jQuery);
