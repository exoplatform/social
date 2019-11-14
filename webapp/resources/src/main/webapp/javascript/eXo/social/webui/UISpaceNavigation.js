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

(function($, bannerUploader) {
var UISpaceNavigation = {
    init: function(id, moreLabel, addEditability) {
    var editedTab = $("#" + id);
  
    function autoMoveApps(){
      var _w = $(window).width();
      if ( _w  < 1025) {
        var uiSpaceMenu = $('#UISpaceMenu');
        var tabContainer = uiSpaceMenu.find('ul#spaceMenuTab');
        tabContainer.css('visibility', 'visible');
        UISpaceNavigation.initStickyBanner();
        return;
      }

	    var ul = $('#spaceMenuTab');

      var $rightBody = $('#RightBody');
      var delta = 130;
      if ($rightBody.hasClass('sticky')) {
        var $avt = $('.uiSpaceMenu .userAvt');
        var $navHeader = $('.uiSpaceMenu .spaceMenuNavHeader');
        delta = $avt.width() + $navHeader.width() + 20;
      }

	    var index = calculateIndex(ul, delta);
      if (index < ul.find('li.item').length) {
        index = calculateIndex(ul, delta + 109);
      }
	    UISpaceNavigation.initNavigation(index, moreLabel);
    };

    function calculateIndex(ul, delta) {
      var maxWith = ul.innerWidth() - delta;
      var liElements = ul.find('li.item');
      var w = 0, index = 0;
      for (var i = 0; i < liElements.length; ++i) {
        var wElm = liElements.eq(i).width();
        if((w + wElm) < maxWith) {
          w += wElm;
          index++;
        } else {
          break;
        }
      }
      return index;
    }

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

    if (addEditability) {
      editedTab.on("dblclick", ".active span", function() {
        var span = $(this);
        showEditLabelInput(this, span.attr("id"), span.text());
      });
    }

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
      'href' : '',
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
      if (idx < index) {
        tabContainer.append(el);
      } else {
        dropDownMenu.append(el);
      }
    });

    if (dropDownMenu.children().length > 0) {
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
    UISpaceNavigation.initStickyBanner();
    var $tab = $('.uiSpaceMenu .spaceMenuTab');
    var $selectedTab = $tab.find('.active');
    if ($selectedTab && $selectedTab.length) {
      var left = $selectedTab.position().left;
      var screenWidth = $(window).width();

      if (left > (screenWidth / 2) && left < ($tab[0].scrollWidth - screenWidth / 2)) {
        $tab.scrollLeft(left - screenWidth / 2);
      } else if (left > $tab.width() - screenWidth / 2) {
        $tab.scrollLeft(left);
      }
    }
  },

  initStickyBanner: function() {
    const spaceMenu = $('#UISpaceMenu');
    $(window).off('scroll.uiSpaceMenu').on('scroll.uiSpaceMenu', function() {
      const $rightBody = $('#RightBody');
      if(spaceMenu.css('display') === 'none') {
        $rightBody.removeClass('sticky');
        return;
      }
      if ($(window).scrollTop() > 130) {
        if (!$rightBody.hasClass('sticky')) {
          $rightBody.addClass('sticky');
          $(window).trigger('resize');
        }
      } else {
        if ($rightBody.hasClass('sticky')) {
          $rightBody.removeClass('sticky');
          $(window).trigger('resize');
        }
      }
    });
  },

  initAvatar : function(uploaderId) {   
      $('.userAvt .uiUploadFile').on('click',function() {
        bannerUploader.selectFile(uploaderId);
      });
  },

  initBanner : function(uploaderId) {
    $('.bannerControls .uiUploadFile').on('click', function() {
        bannerUploader.selectFile(uploaderId);
    });
    $('.bannerControls [data-toggle="popover"]').popover();
  }
};

return UISpaceNavigation;
})(jq, bannerUploader);