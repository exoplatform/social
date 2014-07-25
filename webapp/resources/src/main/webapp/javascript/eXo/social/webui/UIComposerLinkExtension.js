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
 * UIComposerLinkExtension.js
 */

(function($) { 
	var UIComposerLinkExtension = {
	  HTTP: "http://",
	  GRAY_COLOR: "gray",
	  BLACK_COLOR: "black",
	  SINGLE_COMMA: "single_comma",
    DOUBLE_COMMA: "double_comma",
    ENTER_KEY_CODE: 13,
	  changeLinkContent: function () {
	    var link = this.linkData.link || '',
	    title = this.linkData.title;
	    image = this.linkData.image;
	    description = this.linkData.description;
      var queryString = 'link=' + link + '&title=' + title 
          + '&description=' + description;
	    
	    if(image != null){
	        queryString += '&image='+encodeURIComponent(image)
	    }
	
	    var url = this.changeLinkContentUrl.replace(/&amp;/g, "&") + "&ajaxRequest=true";
	    ajaxRequest('POST', url, true, queryString);
	  },
	  addEditableText: function (oldEl, tagName, title) {
	    var dataElement = $(oldEl);
	    var content = dataElement.html().trim();
	    var editableEl = $("<" + tagName + " />").attr("title", title).val(content);
	    if('input' === tagName){
	      editableEl.attr('type', 'text');
	      editableEl.attr('size', 50);
	      editableEl.attr('class', 'InputTitle');
	      editableEl.attr('className', 'InputTitle');
	    } else if ('textarea' === tagName) {
	      editableEl.attr('cols', 50);
	      editableEl.attr('rows', 5);
	      editableEl.attr('class', 'InputDescription');
	      editableEl.attr('className', 'InputDescription');
	    }
	
	    editableEl.insertAfter(dataElement);
	    dataElement.hide();
	    editableEl.focus();
	    editableEl.keypress(function(e){
	      if (13 == (e.which ? e.which : e.keyCode)) {
	        updateElement(this);
	      }
	    });
	    editableEl.blur(function(e){
	      updateElement(this);
	    });
	
	    var updateElement = function(editableEl) {
	        //hide this, set new value and display
	        var oldEl = $(editableEl).prev();
	        oldEl.text($(editableEl).val()).html();
	        
	        //updates data
	        //detects element by class, if class contains ContentTitle -> update title,
	        // if class contains ContentDescription -> update description
	        oldEl.css('display',"block")
	        if (oldEl.hasClass('title')) {
	          UIComposerLinkExtension.linkData.title = $(editableEl).val();
	          UIComposerLinkExtension.changeLinkContent.apply(UIComposerLinkExtension);
	        } else if (oldEl.hasClass('content')) {
	          UIComposerLinkExtension.linkData.description = $(editableEl).val();
	          UIComposerLinkExtension.changeLinkContent.apply(UIComposerLinkExtension);
	        }
	        $(editableEl).remove();
	    }
	  },
	  onLoad: function(params) {
	    UIComposerLinkExtension.configure(params);
	    UIComposerLinkExtension.init();
	  },
	  configure: function(params) {
	    this.titleEditable = params.titleEditable || "";
	    this.linkInfoDisplayed = params.linkInfoDisplayed || false;
	    this.inputLinkId = params.inputLinkId || 'inputLink';
	    this.attachButtonId = params.attachButtonId || 'attachButton';
	    this.attachUrl = encodeURI(decodeURIComponent(params.attachUrl || ""));
	    this.changeLinkContentUrl = encodeURI(decodeURIComponent(params.changeLinkContentUrl || ""));
	    this.shownThumbnailIndex = params.shownThumbnailIndex || 0;
	    this.uiThumbnailDisplayId = params.uiThumbnailDisplayId || 'UIThumbnailDisplay';
	    this.thumbnailsId = params.thumbnailsId || 'Thumbnails';
	    this.backThumbnailId = params.backThumbnailId || 'BackThumbnail';
	    this.nextThumbnailId = params.nextThumbnailId || 'NextThumbnail';
	    this.statsId = params.statsId || 'Stats';
	    this.thumbnailCheckboxId = params.thumbnailCheckboxId || 'ThumbnailCheckbox';
	    this.linkData = params.linkData || {};

	    if (!this.attachUrl) {
	      alert('error: attachUrl is null!');
	    }
	  },
	  resetIsReady: function() {
	    
	    if (this.linkInfoDisplayed) {
	      
	    } else {
	
	    }
	  },
	  init: function() {
	
	    function showThumbnail() {
	      for (var i = 0, l = this.images.length; i < l; i++) {
	        $(this.images[i]).css({'display': 'none', 'height': '100px','width': '100px', 'padding': '10px'});
	      }
	      $(this.images[this.shownThumbnailIndex]).css({'display': 'block', 'height': '100px','width': '100px', 'padding': '10px'});
	      doStats.apply(this);
	    }
	    
	    function doStats() {
	      this.stats.html((this.shownThumbnailIndex + 1) + ' / ' + this.images.length);
	    }
	
	    UIComposerLinkExtension = this;
	    if (this.linkInfoDisplayed) {
	      this.uiThumbnailDisplay = $('#' + this.uiThumbnailDisplayId);
	      this.thumbnails = $('#' + this.thumbnailsId);
	      this.backThumbnail = $('#' + this.backThumbnailId);
	      this.nextThumbnail = $('#' + this.nextThumbnailId);
	      this.stats = $('#' + this.statsId);
	      this.linkTitle = $('#' + 'LinkTitle');
	      this.linkDescription = $('#' + 'LinkDescription');
	      
	      var titleParam = this.titleEditable;
	      if (this.linkTitle) {
	        this.linkTitle.on('dblclick', function(evt) {
	          UIComposerLinkExtension.addEditableText(this, 'input', titleParam);
	        });
	      }
	      
	      if (this.linkDescription) {
	        this.linkDescription.on('dblclick', function(evt) {
	          UIComposerLinkExtension.addEditableText(this, 'textarea', titleParam);
	        });
	      }
	      
	      if (this.thumbnails) {
	        this.thumbnailCheckbox = $('#' + this.thumbnailCheckboxId);
	        this.images = $('img',this.thumbnails);
	        doStats.apply(this);
	        showThumbnail.apply(UIComposerLinkExtension);
	
	        this.backThumbnail.on('click', function(evt) {
	          if (UIComposerLinkExtension.shownThumbnailIndex > 0) {
	            UIComposerLinkExtension.shownThumbnailIndex--;
	            showThumbnail.apply(UIComposerLinkExtension);
	            UIComposerLinkExtension.linkData.image = $(UIComposerLinkExtension.images[UIComposerLinkExtension.shownThumbnailIndex]).attr('src');
	            UIComposerLinkExtension.changeLinkContent.apply(UIComposerLinkExtension);
	          }
	        });
	        
	        this.nextThumbnail.on('click', function(evt) {
	          if (UIComposerLinkExtension.shownThumbnailIndex < UIComposerLinkExtension.images.length - 1) {
	            UIComposerLinkExtension.shownThumbnailIndex++;
	            showThumbnail.apply(UIComposerLinkExtension);
	            UIComposerLinkExtension.linkData.image = $(UIComposerLinkExtension.images[UIComposerLinkExtension.shownThumbnailIndex]).attr('src');
	            UIComposerLinkExtension.changeLinkContent.apply(UIComposerLinkExtension);
	          }
	        });
	        
	        this.thumbnailCheckbox.on('click', function(evt) {
	          if (UIComposerLinkExtension.thumbnailCheckbox.attr('checked') == 'checked') {
	            UIComposerLinkExtension.linkData.image = '';
	            $('#UIRightBox').removeClass('contentRight');
	            $('#UIRightBox').addClass('resetMargin');
	            $('#UIThumbnailLeftBox').toggle();
	          } else {
	            UIComposerLinkExtension.linkData.image = $(UIComposerLinkExtension.images[UIComposerLinkExtension.shownThumbnailIndex]).attr('src');
	            $('#UIRightBox').removeClass('resetMargin');
	            $('#UIRightBox').addClass('contentRight');
	            $('#UIThumbnailLeftBox').toggle();
	          }
	          UIComposerLinkExtension.changeLinkContent.apply(UIComposerLinkExtension);
	        });
	      } else {
	        this.images = [];
	      }
	
	    } else {
	      this.inputLink = $('#' + this.inputLinkId);
	      this.attachButton = $('#' + this.attachButtonId);
	      this.inputLink.val(UIComposerLinkExtension.HTTP);
	      this.inputLink.css('color', UIComposerLinkExtension.GRAY_COLOR);
	      var UIComposerLinkExtension = this;
	      var inputLink = this.inputLink;
	      var attachBtn = this.attachButton;
	      inputLink.on('focus', function(evt) {
	        if (inputLink.val() === UIComposerLinkExtension.HTTP) {
	          inputLink.val('');
	          inputLink.css('color', UIComposerLinkExtension.BLACK_COLOR);
	        }
	      });
	      
	      this.inputLink.on('blur', function(evt) {
	        if (inputLink.val() === '') {
	          inputLink.val(UIComposerLinkExtension.HTTP);
	          inputLink.css('color', UIComposerLinkExtension.GRAY_COLOR);
	        }
	      });
	      
	      this.inputLink.on('keypress', function(evt) {
	        //if enter submit link
	        if (UIComposerLinkExtension.ENTER_KEY_CODE == (evt.which ? evt.which : evt.keyCode)) {
	          $(attachBtn).click();
	        }
	      });
	      
	      this.attachButton.removeAttr('disabled');
	      this.attachButton.on( 'click', function(evt) {
	        if (inputLink.val() === '' || inputLink.val() === UIComposerLinkExtension.HTTP) {
	          return;
	        }
	        var urlInput =  encodeURIComponent(encodeURI(inputLink.val()));
	        var url = UIComposerLinkExtension.attachUrl.replace(/&amp;/g, "&") + '&objectId=' + urlInput + '&ajaxRequest=true';
	        ajaxGet(url, function(){
	          try {
	            $('textarea#composerInput').exoMentions('showButton', function() {});
	          } catch (e) {}
	        });
	      });
	      
	    }
	    
	           
	    var closeButton = $('#UIActivityComposerContainer').find('a.uiIconClose:first');
	    if(closeButton.length > 0) {
	       closeButton.on('click', function() {
	          $('textarea#composerInput').exoMentions('clearLink', function() { });
	       })
	    }
	  }
	};
 
  return UIComposerLinkExtension;
})($);
