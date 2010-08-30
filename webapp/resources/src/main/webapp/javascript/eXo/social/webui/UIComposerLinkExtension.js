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
 
 (function() {
  var window_ = this,
      Util = eXo.social.Util,
      HTTP = "http://",
      GRAY_COLOR = "gray",
      BLACK_COLOR = "black",
      uiComposerLinkExtension;
  
  function changeLinkContent() {
    var link = this.linkData.link,
        image = this.linkData.image;
        title = this.linkData.title;
        description = this.linkData.description;
    var queryString = 'link='+encodeURIComponent(link)
                    + '&image='+encodeURIComponent(image)
                    + '&title='+encodeURIComponent(title)
                    + '&description='+encodeURIComponent(description);
    var url = this.changeLinkContentUrl.replace(/&amp;/g, "&") + "&ajaxRequest=true";
    eXo.social.PortalHttpRequest.ajaxPostRequest(url, queryString, true, function(req) {
     //callbacked
    });
  }
  
  /**
   * creates input/ textarea element for edit inline
   * if tagName = input 
   * <input type="text" id="editableText" value="" />
   * if tagName = textarea
   * <textarea cols="10" rows="3">value</textarea>
   */
  function addEditableText(oldEl, tagName) {
    var textContent = oldEl.innerText; //IE
    if (textContent === undefined) {
        textContent = oldEl.textContent;
    }
    textContent = textContent.trim();
    var editableEl = document.createElement(tagName);
    if ('input' === tagName) {
      editableEl.setAttribute('type', 'text');
      editableEl.setAttribute('size', 50);
      editableEl.setAttribute('class', 'InputTitle');
      editableEl.setAttribute('className', 'InputTitle');
      
    } else if ('textarea' === tagName) {
      editableEl.setAttribute('cols', 50);
      editableEl.setAttribute('rows', 5);
      editableEl.setAttribute('class', 'InputDescription');
      editableEl.setAttribute('className', 'InputDescription');
    }
    //editableEl.setAttribute('id', "UIEditableText");
    editableEl.value = textContent;
    //insertafter and hide oldEl
    Util.insertAfter(editableEl, oldEl);
    oldEl.style.display='none';
    editableEl.focus();
    //ENTER -> done
    Util.addEventListener(editableEl, 'keypress', function(e) {
        if (Util.isEnterKey(e)) {
            updateElement(this);
            return false;
        }
    }, false);
    
    Util.addEventListener(editableEl, 'blur', function() {
        updateElement(this);
    }, false);
    
    var updateElement = function(editableEl) {
        //hide this, set new value and display
        var oldEl = editableEl.previousSibling;
        if (oldEl.innerText != null) { //IE
            oldEl.innerText = editableEl.value;
        } else {
            oldEl.textContent = editableEl.value;
        }
        //updates data
        //detects element by class, if class contains ContentTitle -> update title,
        // if class contains ContentDescription -> update description
        oldEl.style.display="block";
        if (Util.hasClass(oldEl, 'Title')) {
          uiComposerLinkExtension.linkData.title = editableEl.value;
          changeLinkContent.apply(uiComposerLinkExtension);
        } else if (Util.hasClass(oldEl, 'Content')) {
          uiComposerLinkExtension.linkData.description = editableEl.value;
          changeLinkContent.apply(uiComposerLinkExtension);
        }
        editableEl.parentNode.removeChild(editableEl);
    }
}
  
  function UIComposerLinkExtension(params) {
    uiComposerLinkExtension = this;
    this.configure(params);
    this.init();
  }
  
  UIComposerLinkExtension.prototype.configure = function(params) {
    this.linkInfoDisplayed = params.linkInfoDisplayed || false;
    this.inputLinkId = params.inputLinkId || 'inputLink';
    this.attachButtonId = params.attachButtonId || 'attachButton';
    this.attachUrl = params.attachUrl || null;
    this.changeLinkContentUrl = params.changeLinkContentUrl || null;
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
  }
  
  UIComposerLinkExtension.prototype.resetIsReady = function() {
    
    if (this.linkInfoDisplayed) {
      
    } else {

    }
  }
  
  UIComposerLinkExtension.prototype.init = function() {
  
    function showThumbnail() {
      for (var i = 0, l = this.images.length; i < l; i++) {
        this.images[i].style.display = 'none';
      }
      this.images[this.shownThumbnailIndex].style.display = 'block';
      doStats.apply(this);
    }
    
    function doStats() {
      this.stats.innerHTML = (this.shownThumbnailIndex + 1) + ' / ' + this.images.length;
    }
    
    var shareButton = Util.getElementById('ShareButton');

    uiComposerLinkExtension = this;
    if (this.linkInfoDisplayed) {
      //trick: enable share button
      if (shareButton) {
        shareButton.disabled = false;
      }
      
      this.uiThumbnailDisplay = Util.getElementById(this.uiThumbnailDisplayId);
      this.thumbnails = Util.getElementById(this.thumbnailsId);
      this.backThumbnail = Util.getElementById(this.backThumbnailId);
      this.nextThumbnail = Util.getElementById(this.nextThumbnailId);
      this.stats = Util.getElementById(this.statsId);
      this.linkTitle = Util.getElementById('LinkTitle');
      this.linkDescription = Util.getElementById('LinkDescription');
      Util.addEventListener(this.linkTitle, 'click', function(evt) {
        addEditableText(this, 'input');
      }, false);
      
      Util.addEventListener(this.linkDescription, 'click', function(evt) {
        addEditableText(this, 'textarea');
      }, false);
      
      if (this.thumbnails) {
        this.thumbnailCheckbox = Util.getElementById(this.thumbnailCheckboxId);
        this.images = this.thumbnails.getElementsByTagName('img');
        doStats.apply(this);

        Util.addEventListener(this.backThumbnail, 'click', function(evt) {
          if (uiComposerLinkExtension.shownThumbnailIndex > 0) {
            uiComposerLinkExtension.shownThumbnailIndex--;
            showThumbnail.apply(uiComposerLinkExtension);
            uiComposerLinkExtension.linkData.image = Util.getAttributeValue(uiComposerLinkExtension.images[uiComposerLinkExtension.shownThumbnailIndex], 'src');
            changeLinkContent.apply(uiComposerLinkExtension);
          }
        }, false);
        
        Util.addEventListener(this.nextThumbnail, 'click', function(evt) {
          if (uiComposerLinkExtension.shownThumbnailIndex < uiComposerLinkExtension.images.length - 1) {
            uiComposerLinkExtension.shownThumbnailIndex++;
            showThumbnail.apply(uiComposerLinkExtension);
            uiComposerLinkExtension.linkData.image = Util.getAttributeValue(uiComposerLinkExtension.images[uiComposerLinkExtension.shownThumbnailIndex], 'src');
            changeLinkContent.apply(uiComposerLinkExtension);
          }
        }, false);
        
        Util.addEventListener(this.thumbnailCheckbox, 'click', function(evt) {
          if (uiComposerLinkExtension.thumbnailCheckbox.checked == true) {
            uiComposerLinkExtension.uiThumbnailDisplay.parentNode.style.height = '50px';
            uiComposerLinkExtension.uiThumbnailDisplay.style.display = 'none';
            uiComposerLinkExtension.linkData.image = '';
          } else {
            uiComposerLinkExtension.uiThumbnailDisplay.parentNode.style.height = '';
            uiComposerLinkExtension.uiThumbnailDisplay.style.display = 'block';
            uiComposerLinkExtension.linkData.image = Util.getAttributeValue(uiComposerLinkExtension.images[uiComposerLinkExtension.shownThumbnailIndex], 'src');
          }
          changeLinkContent.apply(uiComposerLinkExtension);
        }, false);
      } else {
        this.images = [];
      }

    } else {

      if (shareButton) {
        shareButton.disabled = true;
      }
      this.inputLink = Util.getElementById(this.inputLinkId);
      this.attachButton = Util.getElementById(this.attachButtonId);
      this.inputLink.value = HTTP;
      this.inputLink.style.color = GRAY_COLOR;
      var uiComposerLinkExtension = this;
      var inputLink = this.inputLink;
      Util.addEventListener(inputLink, 'focus', function(evt) {
        if (inputLink.value === HTTP) {
          inputLink.value = '';
          inputLink.style.color = BLACK_COLOR;
        }
      }, false);
      
      Util.addEventListener(this.inputLink, 'blur', function(evt) {
        if (inputLink.value === '') {
          inputLink.value = HTTP;
          inputLink.style.color = GRAY_COLOR;
        }
      }, false);
      
      Util.addEventListener(this.inputLink, 'keypress', function(evt) {
        //if enter submit link
      }, false);
      this.attachButton.disabled = false;
      Util.addEventListener(this.attachButton, 'click', function(evt) {
        if (inputLink.value === '' || inputLink.value === HTTP) {
          return;
        }
        var url = uiComposerLinkExtension.attachUrl.replace(/&amp;/g, "&") + '&objectId='+ encodeURIComponent(inputLink.value) + '&ajaxRequest=true';
        ajaxGet(url);
      }, false);
      
    }
  }
  
  //expose
  window_.eXo = window_.eXo || {};
  window_.eXo.social = window_.eXo.social || {};
  window_.eXo.social.webui = window_.eXo.social.webui || {};
  window_.eXo.social.webui.UIComposerLinkExtension = UIComposerLinkExtension;
 })();