/**
 * LinkShare.js
 * gets information of a provided link,
 * edits this information, save to status update
 * @author  <a href="mailto:hoatlevan@gmail.com">hoatle</a>
 * @since   Oct 12, 2009
 * @copyright   eXo Platform SEA
 */

//uses namespace
var eXo = eXo || {};
eXo.social = eXo.social || {};

/**
 * LinkShare constructor 
 */
eXo.social.LinkShare = function(link, lang) {
	//alias imports
	var LinkShare = eXo.social.LinkShare;
	var Util = eXo.social.Util;
	if (link != null) LinkShare.data.link = link;
	if (lang != null) LinkShare.data.lang = lang;
    //content constructed from eXo.social.LinkShare.data
    this.content = null;
    //privileged methods
    this.makeRequest = function() {
        //encode link
        var link = escape(encodeURIComponent(LinkShare.data.link));
        var url = [LinkShare.config.LINKSHARE_REST_URL, "/", link, "/", LinkShare.data.lang].join("");
        var linkShare = this;
        Util.makeRequest(url, function(res) {linkShare.callbackHandler(res);});
    }
    /**
     * callback handler 
     */
    this.callbackHandler = function(res) {
        if (res === null || res.data === null) {
       		//TODO: alert more friendly, use mini message
       		debug.warn("no data response");
       		return;
        }
        var data = res.data;
        //binds data
        for (var key in LinkShare.data) {
			if (data[key] === null || data[key] === "" || data[key] === undefined) continue;
			LinkShare.data[key] = data[key];
        }
        if (data.images.length == 0) {
        	LinkShare.data.images = data.images;
        	LinkShare.data.noThumbnail = true;
        }
        //sets selectedImage
        if (LinkShare.data.images.length > 0) {
        	LinkShare.data.selectedImageIndex = 0;
        	LinkShare.data.noThumbnail = false;
        }
        //displays attachDisplay
        this.displayAttach(LinkShare.config.ATTACH_DISPLAY_ID);
    }
    return this;
}

/**
 * static settings object for LINKSHARE_REST_URL
 */
eXo.social.LinkShare.config = {
    //change to the right url
    LINKSHARE_REST_URL : "http://localhost:8080/rest/social/activities/linkshare",
    //the div's id in gadget to work on in this div tag
    WORKSPACE_ID : "UIComposerExtension",
    ATTACH_OPTION_ID : "UIAttachOption",
    ATTACH_ACTION_ID : "UIAttachAction",
    ATTACH_DISPLAY_ID : "UIAttachDisplay",
    THUMBNAIL_DISPLAY_ID : "UIThumbnailDisplay",
    EDITABLE_TEXT_ID : "UIEditableText"
};

//static object for holding data
eXo.social.LinkShare.data = {
	link : null,
	lang : "en",
	title : null,
	description : null,
	images : null,
	//additional info
    mediumType : null,
    mediaType : null,
    mediaSrc : null,
    mediaAlbum : null,
    mediaArtist : null,
    mediaTitle : null,
    mediaHeight : null,
    mediaWidth : null,
    selectedImageIndex : null,
    noThumbnail : true
};

/**
 * init function to create user interface 
 */
eXo.social.LinkShare.prototype.init = function() {
	this.addAttachOption();
}

/**
 * displays the element from linkShare.displayedAttachId 
 */
eXo.social.LinkShare.prototype.displayAttach = function(id) {
	if (id == null) {
		debug.warn("No id specified!");
		return;
	}
	//alias 
	var Util = eXo.social.Util;
	var LinkShare = eXo.social.LinkShare;
	//removes all attachs
	//TODO: Should hide for faster performance instead of remmoving
	Util.removeElement(LinkShare.config.ATTACH_OPTION_ID);
	Util.removeElement(LinkShare.config.ATTACH_ACTION_ID);
	Util.removeElement(LinkShare.config.ATTACH_DISPLAY_ID);
	if (id === LinkShare.config.ATTACH_OPTION_ID) {
		this.addAttachOption();
	} else if (id === LinkShare.config.ATTACH_ACTION_ID) {
		this.addAttachAction();
	} else if (id === LinkShare.config.ATTACH_DISPLAY_ID) {
		this.addAttachDisplay();	
	}
	gadgets.window.adjustHeight();
}

/**
 * adds
 * <div class="Label">$_(attach)</div>
 * <div class="ImageFile AttachIcon"><span></span></div> //disabled now
 * <div class="VideoFile AttachIcon"><span></span></div> //disabled now
 * <div class="FileAttach AttachIcon"><span></span></div> //TODO: change class to LinkAttach
 * <div class="ClearLeft"><span></span></div>
 */
eXo.social.LinkShare.prototype.addAttachOption = function() {
	var Locale = eXo.social.Locale;
	var Util = eXo.social.Util;
	var config = eXo.social.LinkShare.config;
	//reset data
	for (var key in eXo.social.LinkShare.data) {
		if (key === 'lang' || key === 'noThumbnail') continue;
		eXo.social.LinkShare.data[key] = null;
	}
	this.content = null;
	
	//creates a div elemenet with id = attachOption for users to click on to share
	var atOptionTagName = 'div';
	var atOptionId = config.ATTACH_OPTION_ID;
	var atOptionHtml = [];
	atOptionHtml.push('<div class="Label">' + Locale.getMsg('attach') + '</div>');
	atOptionHtml.push('<div class="ImageFile AttachIcon"><span></span></div>'); //disabled now
 	atOptionHtml.push('<div class="VideoFile AttachIcon"><span></span></div>'); //disabled now
 	atOptionHtml.push('<div class="FileAttach AttachIcon"><span></span></div>'); //TODO: change class to LinkAttach
 	atOptionHtml.push('<div class="ClearLeft"><span></span></div>');
	//adds element
	var newElement = Util.addElement(config.WORKSPACE_ID, atOptionTagName, atOptionId, atOptionHtml.join(''));
	var aElement = Util.getElementsByTagName('a', newElement)[0];
	if (!aElement) {
		debug.warn('aElement is null');
		return;
	}
	var linkShare = this;
	Util.addEventListener(aElement, 'click', function() {
		linkShare.displayAttach(config.ATTACH_ACTION_ID)
	}, false);
}

/**
 * adds 
 * <div id="attachAction">
 * 	<div>Link: <span class="Close"><a href="#">Close</a></span></div>
 *	<div class="AttachAction"><input type="text" /><a href="#">Attach</a></div> 
 * </div>
 * creates a text input to paste the link, a attach button to get info from the link
 */
eXo.social.LinkShare.prototype.addAttachAction = function() {
	var Locale = eXo.social.Locale;
	var Util = eXo.social.Util;
	var config = eXo.social.LinkShare.config;
	var atActionTagName = 'div';
	var atActionId = config.ATTACH_ACTION_ID;
	var atActionHtml = [];
	atActionHtml.push('<div class="LinkAttachAction">' + Locale.getMsg('link') + ' <span class="Close"><a href="#linkShare.displayAttachOption">' + Locale.getMsg('close') + '</a></span></div>');
	atActionHtml.push('<div class="AttachAction">');
		atActionHtml.push('<input type="text" value="http://"');
		atActionHtml.push('<a href="#linkShare.getInfo">' + Locale.getMsg('attach') + '</a>');
	atActionHtml.push('</div>');
	var newElement = Util.addElement(config.WORKSPACE_ID, atActionTagName, atActionId, atActionHtml.join(''));
	//event attach
	var spanCloseElement = Util.getElementsByClass(newElement, 'span', 'Close')[0];
	var linkShare = this;
	Util.addEventListener(spanCloseElement, 'click', function() {
		linkShare.displayAttach(config.ATTACH_OPTION_ID);
	}, false);
	var divAttachActionElement = Util.getElementsByClass(newElement, 'div', 'AttachAction')[0];
	var inputElement = Util.getElementsByTagName('input', divAttachActionElement)[0];
	Util.addEventListener(inputElement, 'focus', function() {
		if (this.value === 'http://') {
			this.value = '';
		}
	}, false);
	Util.addEventListener(inputElement, 'blur', function() {
		if (this.value === '') {
			this.value = 'http://'
		}
	}, false);
	Util.addEventListener(inputElement, 'keypress', function(e) {
		if (Util.isEnterKey(e)) {
			linkShare.getInfo();
		}
	}, false);
	var aAttachElement = Util.getElementsByTagName('a', divAttachActionElement)[0];
	Util.addEventListener(aAttachElement, 'click', function() {
		linkShare.getInfo();
	}, false);
}

/**
 * gets info from eXo.social.LinkShare.data
 * generates right html format
 * saves to this.content
 * <div id="attachDisplay">
 *	 <div>Display Content <span class="Close"><a href="#">Close</a></span></div>
 *   <div id="ThumbnailDisplay" class="ThumbnailDisplay">
 *	 	<!-- getThumbnailDisplay -->		
 *   </div>
 *	 <div id="NoThumbnail">
 * 	 	<input type="checkbox" /> No thumbnail.
 *   </div>
 *	 <div class="ContentDisplay">
 * 	 	<p class="ContentTitle"></p>
 *      <p class="ContentLink"></p>
 *      <p class="ContentDescription"><p>
 * 	 </div> 
 * </div> 
 */
eXo.social.LinkShare.prototype.addAttachDisplay = function() {
	var Locale = eXo.social.Locale;
	var Util = eXo.social.Util;
	var LinkShare = eXo.social.LinkShare;
	var config = LinkShare.config;
	var linkShare = this;
	var atDisplayTagName = "div";
	var atDisplayId = config.ATTACH_DISPLAY_ID;
	var atDisplayHtml = [];
	atDisplayHtml.push('<div>' + Locale.getMsg('content_to_display') + ': <span class="Close"><a href="#linkShare.displayAttachOption">' + Locale.getMsg('close') + '</a></span></div>');
		atDisplayHtml.push('<div id="' + config.THUMBNAIL_DISPLAY_ID + '" class="ThumbnailDisplay">');
			atDisplayHtml.push(this.getThumbnailDisplay());
		atDisplayHtml.push("</div>");
		
		if (LinkShare.data.images.length > 0) {
		atDisplayHtml.push('<div id="NoThumbnail">');
			atDisplayHtml.push('<input type="checkbox" />' + Locale.getMsg('no_thumbnail'));
		atDisplayHtml.push("</div>");
		} //end if
		
		atDisplayHtml.push('<div class="ContentDisplay">');
			atDisplayHtml.push('<p class="ContentTitle Editable">' + LinkShare.data.title + '</p>');
			atDisplayHtml.push('<p class="ContentLink">' + LinkShare.data.link + '</p>');
			atDisplayHtml.push('<p class="ContentDescription Editable">' + LinkShare.data.description + '</p>');
		atDisplayHtml.push('</div>');
	var newElement = Util.addElement(config.WORKSPACE_ID, atDisplayTagName, atDisplayId, atDisplayHtml.join(''));
	//attach event
	var spanCloseEl = Util.getElementsByClass(newElement, 'span', 'Close')[0];
	Util.addEventListener(spanCloseEl, 'click', function() {
		linkShare.displayAttach(config.ATTACH_OPTION_ID);
	}, false);
	
	var divNoThumbnail = Util.getElementById('NoThumbnail');
	if (divNoThumbnail) {
		var cbNoThumbnail = divNoThumbnail.getElementsByTagName('input')[0];
		Util.addEventListener(cbNoThumbnail, 'click', function() {
			linkShare.enableThumbnailDisplay(this);
		}, false);
		this.addEventListenerToSelector();
	}

	
	var pContentTitle = Util.getElementsByClass(newElement, 'p', 'ContentTitle')[0];
	Util.addEventListener(pContentTitle, 'click', function() {
		linkShare.addEditableText(this, 'input');
	}, false);
	var pContentDescription = Util.getElementsByClass(newElement, 'p', 'ContentDescription')[0];
	Util.addEventListener(pContentDescription, 'click', function() {
		linkShare.addEditableText(this, 'textarea');
	}, false);
	
	//constructs content
	//this.constructContent();
	this.content = true;
}

/**
 * gets ThumbnailDisplay fragment
 *   <div class="Thumbnail">
 * 	 	<img src="" />
 *   </div>
 *	 <div class="ImageSelector">
 * 		<span class="ImageStats"></span>
 * 		<a href="#">Previous </a> |
 *		<a href="#">Next </a> 
 * 	 </div>
 */
eXo.social.LinkShare.prototype.getThumbnailDisplay = function() {
	var Locale = eXo.social.Locale;
	var Util = eXo.social.Util;
	var LinkShare = eXo.social.LinkShare;
	var config = LinkShare.config;
	if (LinkShare.data.selectedImageIndex === null) return;
	var thumbnailDisplay = [];
	thumbnailDisplay.push('<div class="Thumbnail">');
		thumbnailDisplay.push('<img width="70px" src="' + LinkShare.data.images[LinkShare.data.selectedImageIndex] + '" />');
	thumbnailDisplay.push('</div>')
	thumbnailDisplay.push('<div class="ImageSelector">');
		thumbnailDisplay.push('<span class="ImageStats">' + (LinkShare.data.selectedImageIndex + 1) + '/' + LinkShare.data.images.length + '</span>');
		thumbnailDisplay.push('<a href="#linkShare.previousImage">' + Locale.getMsg('previous') + '</a> |');
		thumbnailDisplay.push('<a href="#linkShare.nextImage">' + Locale.getMsg('next') + '</a>');
	thumbnailDisplay.push('</div>');
	return thumbnailDisplay.join('');
}

/**
 * shows or hides #thumbnailDisplay 
 */
eXo.social.LinkShare.prototype.enableThumbnailDisplay = function(el) {
	var LinkShare = eXo.social.LinkShare;
	var Util = eXo.social.Util;
	var checked = el.checked;
	if (checked === true) {
		//hides
		LinkShare.data.noThumbnail = true;
		Util.hideElement(LinkShare.config.THUMBNAIL_DISPLAY_ID);
	} else {
		//shows
		LinkShare.data.noThumbnail = false;
		Util.showElement(eXo.social.LinkShare.config.THUMBNAIL_DISPLAY_ID);
	}
}

/**
 * creates input/ textarea element for edit inline
 * if tagName = input 
 * <input type="text" id="editableText" value="" />
 * if tagName = textarea
 * <textarea cols="10" rows="3">value</textarea>
 */
eXo.social.LinkShare.prototype.addEditableText = function(oldEl, tagName) {
	var LinkShare = eXo.social.LinkShare;
	var Util = eXo.social.Util;
	var textContent = oldEl.innerText; //IE
	if (textContent === undefined) {
		textContent = oldEl.textContent;
	}
	var editableEl = document.createElement(tagName);
	editableEl.setAttribute('id', LinkShare.config.EDITABLE_TEXT_ID);
	editableEl.value = textContent;
	//insertafter and hide oldEl
	Util.insertAfter(editableEl, oldEl);
	oldEl.style.display='none';
	editableEl.focus();
	//ENTER -> done
	Util.addEventListener(editableEl, 'keypress', function(e) {
		if (Util.isEnterKey(e)) {
			updateElement(this);
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
		if (Util.hasClass(oldEl, 'ContentTitle')) {
			LinkShare.data.title = editableEl.value;
		} else if (Util.hasClass(oldEl, 'ContentDescription')) {
			LinkShare.data.description = editableEl.value;
		}
		editableEl.parentNode.removeChild(editableEl);
	}
}


/**
 * processes to get information
 * Required: link property
 */
eXo.social.LinkShare.prototype.getInfo = function() {
	var Util = eXo.social.Util;
	var config = eXo.social.LinkShare.config;
	var attachActionEl = Util.getElementById(config.ATTACH_ACTION_ID);
	if (attachActionEl === null) return;
	var inputEl = attachActionEl.getElementsByTagName('input')[0];
	if (inputEl == null) {
		debug.warn('err: no input element in attachAction');
		return;
	}
	if (inputEl.value === null) return;
	if (!Util.isUrl(inputEl.value)) return; 
	eXo.social.LinkShare.data.link = inputEl.value;
    this.makeRequest();
}

///**
// * updates LinkShare object's attributes 
// */
//eXo.social.LinkShare.prototype.update = function(attrName, attrValue) {
//    if (attrName == null || attrValue == null) return;
//    if (eXo.social.LinkShare.data[attrName] != null) {
//        eXo.social.LinkShare.data[attrName] = attrValue;
//    }
//}

/**
 * views previous image 
 */
eXo.social.LinkShare.prototype.previousImage = function() {
	if (eXo.social.LinkShare.data.selectedImageIndex === 0) return;
	eXo.social.LinkShare.data.selectedImageIndex -= 1;
	this.updateThumbnailDisplay();
}

/**
 * views next image 
 */
eXo.social.LinkShare.prototype.nextImage = function() {
	if (eXo.social.LinkShare.data.selectedImageIndex === (eXo.social.LinkShare.data.images.length -1)) return;
	eXo.social.LinkShare.data.selectedImageIndex += 1;
	this.updateThumbnailDisplay();
}

/**
 * updates thumbnailDislay fragment 
 */
eXo.social.LinkShare.prototype.updateThumbnailDisplay = function() {
	var thumbnailDisplayEl = eXo.social.Util.getElementById(eXo.social.LinkShare.config.THUMBNAIL_DISPLAY_ID);
	if (!thumbnailDisplayEl) return;
	thumbnailDisplayEl.innerHTML = this.getThumbnailDisplay();
	this.addEventListenerToSelector();
}

/**
 * attach event handler to images selector
 */
eXo.social.LinkShare.prototype.addEventListenerToSelector = function() {
	var Util = eXo.social.Util;
	var config = eXo.social.LinkShare.config;
	var linkShare = this;
	var divThumbnailDisplay = Util.getElementById(config.THUMBNAIL_DISPLAY_ID);
	var aImageSelectors = Util.getElementsByTagName('a', divThumbnailDisplay); //2
	Util.addEventListener(aImageSelectors[0], 'click', function() {
		linkShare.previousImage();
	}, false);
	Util.addEventListener(aImageSelectors[1], 'click', function() {
		linkShare.nextImage();
	}, false);
}

/**
 * constructs content from LinkShare object to update status
 * saves to this.content
 * The body of activity should be:
 * <div class="Content">
 * 	<div class="Status"></div>
 *  <div class="Extension LinkShare"> <!-- constructs the content from this tag -->
 *		<div class="Link"></div> 
 *  	<div class="Thumbnail">
 * 			<img src="" />
 * 		</div> 
 * 		<div class="Detail">
 * 			<p class="Title"></p>
 *			<p class="Description"></p>
 * 		</div>
 *  </div>
 * </div> 
 * @warning('notused')
 */
eXo.social.LinkShare.prototype.constructContent = function() {
    var content = [];
    content.push("<div class=\"Extension LinkShare\">");
    	content.push("<div class=\"Link\">" + eXo.social.LinkShare.data.link + "</div>");
    	if (eXo.social.LinkShare.data.images.length > 0) {
    		if (eXo.social.LinkShare.data.noThumbnail == null || eXo.social.LinkShare.data.noThumbnail == false) {
    	content.push("<div class=\"Thumbnail\">");
    		content.push("<img title=\"" + eXo.social.LinkShare.data.title + "\" src=\"" + eXo.social.LinkShare.data.images[eXo.social.LinkShare.data.selectedImageIndex] + "\" />");
    	content.push("</div>");
    		}//end if
    	} //end if
    	content.push("<div class=\"Detail\">");
    		content.push("<p class=\"Title\">" + eXo.social.LinkShare.data.title +"</p>");
    		if (eXo.social.LinkShare.data.description != null) {
    		content.push("<p class=\"Description\">" + eXo.social.LinkShare.data.description +"</p>");
    		} //end if
    	content.push("</div>");
    content.push("</div>");
    this.content = content.join("");
}

/**
 * saves activity
 * @param	status text input from status
 * @param	callback function callback after sending activity
 */

eXo.social.LinkShare.prototype.save = function(status, callback) {
	var LinkShare = eXo.social.LinkShare;
	//create activity params
	var params = {};
	//params[opensocial.Activity.Field.TITLE] = eXo.social.LinkShare.data.title;
	//params[opensocial.Activity.Field.URL] = eXo.social.LinkShare.data.link;
	//add owner's comment to description
	//save all info to body tag
	/**
	 * body = {
	 * 	data : LinkShare.data
	 * } 
	 */
	var body = {};
	body.data = LinkShare.data;
	body.comment = status;
	//body.comment = status;
	params[opensocial.Activity.Field.TITLE] = body.data.title;
	params[opensocial.Activity.Field.BODY] = gadgets.json.stringify(body);
	debug.info(params[opensocial.Activity.Field.BODY]);
	//thumbnail
//	if (eXo.social.LinkShare.data.noThumbnail == false) {
//		var mediaItems = [];
//		var opt_params = {};
//		opt_params[opensocial.Activity.MediaItem.Field.TYPE] = opensocial.Activity.MediaItem.Type.IMAGE;
//		//opt_params[opensocial.Activity.MediaItem.Field.URL] = eXo.social.LinkShare.data.images[eXo.social.LinkShare.data.selectedImageIndex];
//		var imageLink = eXo.social.LinkShare.data.images[eXo.social.LinkShare.data.selectedImageIndex];
//		var mediaItem = opensocial.newActivityMediaItem(eXo.social.LinkShare.getMimeType(imageLink), imageLink, opt_params);
//		mediaItems.push(mediaItem);
//		params[opensocial.Activity.Field.MEDIA_ITEMS] = mediaItems;
//	}
	var activity = opensocial.newActivity(params);
	opensocial.requestCreateActivity(activity, opensocial.CreateActivityPriority.HIGH, callback);
	//resets
	this.content = null;
	eXo.social.LinkShare.data.link = null;
	eXo.social.LinkShare.data.images = null;
	eXo.social.LinkShare.data.title = null;
	eXo.social.LinkShare.data.description = null;
	eXo.social.LinkShare.data.selectedImageIndex = null;
	eXo.social.LinkShare.data.noThumbnail = true;
}