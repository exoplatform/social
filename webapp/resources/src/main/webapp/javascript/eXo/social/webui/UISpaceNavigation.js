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

var UISpaceNavigation = {
	addEditability: function(id, moreLabel) {
    var editedTab = $("#" + id);

    //
    var uiSpaceMenu = $('#UISpaceMenu');
    var tabContainer = uiSpaceMenu.find('ul#spaceMenuTab');
    var tabs = tabContainer.find('li');
    
    var dropDownMenu = $('<ul/>', {
      'class' : 'dropdown-menu'
    });

    var dropDownToggle = $('<a/>', {
      'href' : '#',
      'class' : 'dropdown-toggle',
      'data-toggle' : 'dropdown'
    });
    
    // need re-define this value to match spec
    if (tabs.length > 8) {
		  dropDownToggle.append($('<b/>', {
									           'text' : '+'
									         }))
									  .append($('<span/>', {
									           'text' : moreLabel
									         }));
    };

    var dropDown = $('<li/>', {
      'class' : 'dropdown'
    }).append(dropDownToggle).append(dropDownMenu);

    // clear
    tabContainer.empty();

    // rebuild
    $.each(tabs, function(idx, el) {
      if (idx < 8) {
        tabContainer.append(el);
      } else {
        dropDownMenu.append(el);
      }
    });
    
    tabContainer.append(dropDown);
    
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
    
    
    editedTab.on("dblclick", ".active span", function() {
      var span = $(this);
      showEditLabelInput(this, span.attr("id"), span.text()); 
    });
    
	  function showEditLabelInput(target, nodeName, currentLabel) {
	    var jqObj = $(target);
	
	    var input = $("<input>").attr({type : "text", id : nodeName, name : currentLabel, value : currentLabel, maxLength : 50});
	    input.css("border", "1px solid #b7b7b7").css("width", (target.offsetWidth - 2) + "px");
	
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
	}
};

_module.UISpaceNavigation = UISpaceNavigation;
