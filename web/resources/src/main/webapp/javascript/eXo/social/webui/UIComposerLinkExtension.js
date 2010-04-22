/**
 * UIComposerLinkExtension.js
 */
 
 (function() {
  var window_ = this,
      Util = eXo.social.Util,
      HTTP = "http://",
      GRAY_COLOR = "gray",
      BLACK_COLOR = "black";
  
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
  
  function UIComposerLinkExtension(params) {
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
      if (this.thumbnails) {
        this.thumbnailCheckbox = Util.getElementById(this.thumbnailCheckboxId);
        this.images = this.thumbnails.getElementsByTagName('img');
        doStats.apply(this);

        var uiComposerLinkExtension = this;
        
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