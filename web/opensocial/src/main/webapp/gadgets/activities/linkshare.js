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
	if (link != null) eXo.social.LinkShare.data.link = link;
	if (lang != null) eXo.social.LinkShare.data.lang = lang;
    //content extracted from eXo.social.LinkShare.data
    this.content = null;
    //privileged methods
    this.makeRequest = function() {
        var ts = new Date().getTime();
        //encode link
        var link = escape(encodeURIComponent(eXo.social.LinkShare.data.link));
        var url = [eXo.social.LinkShare.settings.LINKSHARE_REST_URL, "/", link, "/", eXo.social.LinkShare.data.lang, "?", "nocache="+ts].join("");
        var params = {};
        params[gadgets.io.RequestParameters.METHOD] = gadgets.io.MethodType.GET;
        params[gadgets.io.RequestParameters.CONTENT_TYPE] = gadgets.io.ContentType.JSON;
        gadgets.io.makeRequest(url, callbackHandler, params);
    }
    //private methods
    /**
     * callback handler 
     */
    var callbackHandler = function(res) {
        if (res == null || res.data == null) {
       		//TODO: alert more friendly
       		alert('no data response, please retry.'); return;
        }
        var data = res.data;
        //binds data
        for (var key in eXo.social.LinkShare.data) {
			if (data[key] == null || data[key] == "" || data[key] == "undefined") continue;
			eXo.social.LinkShare.data[key] = data[key];
        }
        if (data.images.length == 0) {
        	eXo.social.LinkShare.data.images = data.images;
        	eXo.social.LinkShare.data.noThumbnail = true;
        }
        //set selectedImage
        if (eXo.social.LinkShare.data.images.length > 0) {
        	eXo.social.LinkShare.data.selectedImageIndex = 0;
        	eXo.social.LinkShare.data.noThumbnail = false;
        }
        //display attachDisplay
        eXo.social.linkShare.displayAttach(eXo.social.LinkShare.settings.ATTACH_DISPLAY_ID);
    }
    return this;
}

/**
 * static settings object for LINKSHARE_REST_URL
 */
eXo.social.LinkShare.settings = {
    //change to the right url
    LINKSHARE_REST_URL : "http://localhost:8080/rest/social/activities/linkshare",
    //the div's id in gadget to work on in this div tag
    WORKSPACE_ID : "extension",
    ATTACH_OPTION_ID : "attachOption",
    ATTACH_ACTION_ID : "attachAction",
    ATTACH_DISPLAY_ID : "attachDisplay",
    THUMBNAIL_DISPLAY_ID : "thumbnailDisplay",
    EDIABLE_TEXT_ID : "editableText"
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
 * adds element with specified parentId, tagName, elementId and html content 
 */
eXo.social.LinkShare.addElement = function(parentId, tagName, elementId, html) {
    if (parentId == null || tagName == null || elementId == null || html == null) {
    	return;
    }
    if (document.getElementById(elementId)) return; //do not create repeated element
    var parent = document.getElementById(parentId);
    if (parent == null) return;
    var newElement = document.createElement(tagName);
    newElement.setAttribute('id', elementId);
    newElement.innerHTML = html;
    parent.appendChild(newElement);
}

/**
 * removes element from dom by its id 
 */
eXo.social.LinkShare.removeElement = function(elementId) {
    var element = document.getElementById(elementId);
    if (element == null) return;
    element.parentNode.removeChild(element);
}

/**
 * hides element by id
 */
eXo.social.LinkShare.hideElement = function(elementId) {
	var element = document.getElementById(elementId);
	if (element == null) return;
	element.style.display="none";
}

/**
 * shows element by id 
 */
eXo.social.LinkShare.showElement = function(elementId) {
	var element = document.getElementById(elementId);
	if (element == null) return;
	element.style.display = "block";
}

/**
 * inserts an element after an element
 * @param	newNode
 * @param	refNode
 */
eXo.social.LinkShare.insertAfter = function(newNode, refNode) {
	//checks if oldEl.nextSibling is null?
	refNode.parentNode.insertBefore(newNode, refNode.nextSibling);
}

/**
 * checks if a provided string is a correct url 
 */
eXo.social.LinkShare.isUrl = function(url) {
	var regexp = /(ftp|http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?/
	return regexp.test(url);
}

/**
 * checks if keyNum == ENTER key 
 */
eXo.social.LinkShare.isEnterKey = function(e) {
	var keyNum;
	var ENTER_KEY_NUM = 13;
	if(window.event) {// IE
  		keyNum = e.keyCode;
  	} else if (e.which) { // Netscape/Firefox/Opera
  		keyNum = e.which;
  	}
  	if (ENTER_KEY_NUM == keyNum) {
		return true;
  	}
  	return false;
}

/**
 * gets value of an attribute name from an element
 * @param	dom element
 * @param	attribute name
 * @return	attribute value 
 */
eXo.social.LinkShare.getAttributeValue = function(element, attrName) {
	for(var x = 0; x < element.attributes.length; x++) {
	  if(element.attributes[x].nodeName.toLowerCase() == attrName) {
		return element.attributes[x].nodeValue;
	  }
	}
	return null;
}

/**
 * gets mimetype from provided link
 * @param	link String 
 * @return	mimetype string = type/extension
 * //TODO hoatle: complete this method
 */
eXo.social.LinkShare.getMimeType = function(link) {
	//currently gets mimeType from images
	var imagesArr = ['jpg', 'jpeg', 'gif', 'png'];
	
	var startIndex = link.lastIndexOf('.');
	var extension = link.substring((startIndex + 1), link.length);
	var type = null;

	if (imagesArr.indexOf(extension) > -1) type = 'image';
	if (type == null) {
		alert('type not dectected');
		return;
	}
	return type + '/' + extension;
}

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
		alert('no attachId set');
		return;
	}
	//remove all attachs
	eXo.social.LinkShare.removeElement(eXo.social.LinkShare.settings.ATTACH_OPTION_ID);
	eXo.social.LinkShare.removeElement(eXo.social.LinkShare.settings.ATTACH_ACTION_ID);
	eXo.social.LinkShare.removeElement(eXo.social.LinkShare.settings.ATTACH_DISPLAY_ID);
	if (id == eXo.social.LinkShare.settings.ATTACH_OPTION_ID) {
		this.addAttachOption();
	} else if (id == eXo.social.LinkShare.settings.ATTACH_ACTION_ID) {
		this.addAttachAction();
	} else if (id == eXo.social.LinkShare.settings.ATTACH_DISPLAY_ID) {
		this.addAttachDisplay();	
	}
	gadgets.window.adjustHeight();
}

/**
 * adds
 * <div id="attachOption">
 *   Attach:
 *   <ul>
 * 		<li><a href="#">Link</a></li>
 *   </ul>
 * </div> 
 */
eXo.social.LinkShare.prototype.addAttachOption = function() {
	//reset data
	for (var key in eXo.social.LinkShare.data) {
		if (key == "lang") continue;
		eXo.social.LinkShare.data[key] = null;
	}
	this.content = null;
	
	//create a div elemenet with id = attachOption for users to click on to share
	var atOptionTagName = "div";
	var atOptionId = eXo.social.LinkShare.settings.ATTACH_OPTION_ID;
	var atOptionHtml = [];
	atOptionHtml.push("Attach:");
	atOptionHtml.push("<ul>");
		atOptionHtml.push("<li><a onclick=\"eXo.social.linkShare.displayAttach(eXo.social.LinkShare.settings.ATTACH_ACTION_ID);\" href=\"#\">Link</a></li>");
	atOptionHtml.push("</ul>");
	//add element
	eXo.social.LinkShare.addElement(eXo.social.LinkShare.settings.WORKSPACE_ID, atOptionTagName, atOptionId, atOptionHtml.join(""));
}

/**
 * adds 
 * <div id="attachAction">
 * 	<div>Link: <span class="Close">Close</span></div>
 *	<div class="AttachAction"><input type="text" /><a href="#">Attach</a></div> 
 * </div>
 * creates a text input to paste the link, a attach button to get info from the link
 */
eXo.social.LinkShare.prototype.addAttachAction = function() {
	var atActionTagName = "div";
	var atActionId = eXo.social.LinkShare.settings.ATTACH_ACTION_ID;
	var atActionHtml = [];
	atActionHtml.push("<div class=\"LinkAttachAction\">Link <span class=\"Close\" onclick=\"eXo.social.linkShare.displayAttach(eXo.social.LinkShare.settings.ATTACH_OPTION_ID)\"><a href=\"#\">Close</a></span></div>");
	atActionHtml.push("<div class=\"AttachAction\">");
		atActionHtml.push("<input type=\"text\" value=\"http://\" onkeypress=\"if(eXo.social.LinkShare.isEnterKey(event)){eXo.social.linkShare.getInfo();};\" onfocus=\"if (this.value == 'http://') this.value=''\" onblur=\"if(this.value == '' || this.value == 'http://') this.value='http://'\" />");
		atActionHtml.push("<a onclick=\"eXo.social.linkShare.getInfo();\" href=\"#\">Attach</a>");
	atActionHtml.push("</div>");
	eXo.social.LinkShare.addElement(eXo.social.LinkShare.settings.WORKSPACE_ID, atActionTagName, atActionId, atActionHtml.join(""));
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
	var atDisplayTagName = "div";
	var atDisplayId = eXo.social.LinkShare.settings.ATTACH_DISPLAY_ID;
	var atDisplayHtml = [];
	atDisplayHtml.push("<div>Content to display: <span onclick=\"eXo.social.linkShare.displayAttach(eXo.social.LinkShare.settings.ATTACH_OPTION_ID)\" class=\"Close\"><a href=\"#\">Close</a></span></div>");
		atDisplayHtml.push("<div id=\"" + eXo.social.LinkShare.settings.THUMBNAIL_DISPLAY_ID + "\" class=\"ThumbnailDisplay\">");
			atDisplayHtml.push(this.getThumbnailDisplay());
		atDisplayHtml.push("</div>");
		if (eXo.social.LinkShare.data.images.length > 0) {
		atDisplayHtml.push("<div id=\"noThumbnail\">");
			atDisplayHtml.push("<input onchange=\"eXo.social.linkShare.enableThumbnailDisplay(this);\" type=\"checkbox\" /> No thumbnail.");
		atDisplayHtml.push("</div>");
		} //end if
		atDisplayHtml.push("<div class=\"ContentDisplay\">");
			atDisplayHtml.push("<p class=\"ContentTitle Editable\" onclick=\"eXo.social.linkShare.addEditableText(this, 'input');\">" + eXo.social.LinkShare.data.title + "</p>");
			atDisplayHtml.push("<p class=\"ContentLink\">" + eXo.social.LinkShare.data.link + "</p>");
			atDisplayHtml.push("<p class=\"ContentDescription Editable\" onclick=\"eXo.social.linkShare.addEditableText(this, 'textarea');\">" + eXo.social.LinkShare.data.description + "</p>");
		atDisplayHtml.push("</div>");
	eXo.social.LinkShare.addElement(eXo.social.LinkShare.settings.WORKSPACE_ID, atDisplayTagName, atDisplayId, atDisplayHtml.join(""));
	//construct content
	this.constructContent();
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
	if (eXo.social.LinkShare.data.selectedImageIndex == null) return;
	var thumbnailDisplay = [];
	thumbnailDisplay.push("<div class=\"Thumbnail\">");
		thumbnailDisplay.push("<img src=\"" + eXo.social.LinkShare.data.images[eXo.social.LinkShare.data.selectedImageIndex] + "\" />");
	thumbnailDisplay.push("</div>")
	thumbnailDisplay.push("<div class=\"ImageSelector\">");
		thumbnailDisplay.push("<span class=\"ImageStats\">" + (eXo.social.LinkShare.data.selectedImageIndex + 1) + "/" + eXo.social.LinkShare.data.images.length + "</span>");
		thumbnailDisplay.push("<a onclick=\"eXo.social.linkShare.previousImage()\" href=\"#\">Previous</a> |");
		thumbnailDisplay.push("<a onclick=\"eXo.social.linkShare.nextImage()\" href=\"#\">Next</a>");
	thumbnailDisplay.push("</div>");
	return thumbnailDisplay.join("");
}

/**
 * shows or hides #thumbnailDisplay 
 */
eXo.social.LinkShare.prototype.enableThumbnailDisplay = function(el) {
	var checked = el.checked;
	if (checked == true) {
		//hides
		eXo.social.LinkShare.data.noThumbnail = true;
		eXo.social.LinkShare.hideElement(eXo.social.LinkShare.settings.THUMBNAIL_DISPLAY_ID);
	} else {
		//shows
		eXo.social.LinkShare.data.noThumbnail = false;
		eXo.social.LinkShare.showElement(eXo.social.LinkShare.settings.THUMBNAIL_DISPLAY_ID);
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
	var textContent = oldEl.innerText; //IE
	if (textContent == null) {
		textContent = oldEl.textContent;
	}
	var editableEl = document.createElement(tagName);
	editableEl.setAttribute('id', eXo.social.LinkShare.settings.EDITABLE_TEXT_ID);
	editableEl.value = textContent;
	//insertafter and hide oldEl
	eXo.social.LinkShare.insertAfter(editableEl, oldEl);
	oldEl.style.display="none";
	editableEl.focus();
	//ENTER -> done
	editableEl.onkeypress = function(e) {
		var keyNum;
		var ENTER_KEY_NUM = 13;
		if(window.event) {// IE
	  		keyNum = e.keyCode;
	  	} else if (e.which) { // Netscape/Firefox/Opera
	  		keyNum = e.which;
	  	}
	  	if (ENTER_KEY_NUM == keyNum) {
			updateElement(this);
	  	}
	}
	//lose focus -> done
	editableEl.onblur = function(e) {
		updateElement(this);
	}
	
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
		var classValue = eXo.social.LinkShare.getAttributeValue(oldEl, 'class');
		if (classValue == null) return;
		var values = classValue.split(" ");
		if(values.indexOf("ContentTitle") > -1) {
			eXo.social.LinkShare.data.title = editableEl.value;
		} else if (values.indexOf("ContentDescription" > -1)) {
			eXo.social.LinkShare.data.description = editableEl.value;
		}
		oldEl.style.display="block";
		editableEl.parentNode.removeChild(editableEl);
	}
}


/**
 * processes to get information
 * Required: link property
 */
eXo.social.LinkShare.prototype.getInfo = function() {
	var attachActionEl = document.getElementById(eXo.social.LinkShare.settings.ATTACH_ACTION_ID);
	if (attachActionEl == null) return;
	var inputEl = attachActionEl.getElementsByTagName('input')[0];
	if (inputEl == null) {
		alert('err: no input element in attachAction');
		return;
	}
	if (inputEl.value == null) return;
	if (!eXo.social.LinkShare.isUrl(inputEl.value)) return; 
	eXo.social.LinkShare.data.link = inputEl.value;
    this.makeRequest();
}

/**
 * updates LinkShare object's attributes 
 */
eXo.social.LinkShare.prototype.update = function(attrName, attrValue) {
    if (attrName == null || attrValue == null) return;
    if (eXo.social.LinkShare.data[attrName] != null) {
        eXo.social.LinkShare.data[attrName] = attrValue;
    }
}

/**
 * views previous image 
 */
eXo.social.LinkShare.prototype.previousImage = function() {
	if (eXo.social.LinkShare.data.selectedImageIndex == 0) return;
	eXo.social.LinkShare.data.selectedImageIndex -= 1;
	this.updateThumbnailDisplay();
}

/**
 * views next image 
 */
eXo.social.LinkShare.prototype.nextImage = function() {
	if (eXo.social.LinkShare.data.selectedImageIndex == (eXo.social.LinkShare.data.images.length -1)) return;
	eXo.social.LinkShare.data.selectedImageIndex += 1;
	this.updateThumbnailDisplay();
}

/**
 * updates thumbnailDislay fragment 
 */
eXo.social.LinkShare.prototype.updateThumbnailDisplay = function() {
	var thumbnailDisplayEl = document.getElementById(eXo.social.LinkShare.settings.THUMBNAIL_DISPLAY_ID);
	thumbnailDisplayEl.innerHTML = this.getThumbnailDisplay();
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
 *  
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
	//create activity params
	var params = {};
	params[opensocial.Activity.Field.TITLE] = eXo.social.LinkShare.data.title;
	params[opensocial.Activity.Field.URL] = eXo.social.LinkShare.data.link;
	//add owner's comment to description
	var body = {};
	body.description = eXo.social.LinkShare.data.description;
	body.comment = status;
	params[opensocial.Activity.Field.BODY] = gadgets.json.stringify(body);
	//thumbnail
	if (eXo.social.LinkShare.data.noThumbnail == false) {
		var mediaItems = [];
		var opt_params = {};
		opt_params[opensocial.Activity.MediaItem.Field.TYPE] = opensocial.Activity.MediaItem.Type.IMAGE;
		//opt_params[opensocial.Activity.MediaItem.Field.URL] = eXo.social.LinkShare.data.images[eXo.social.LinkShare.data.selectedImageIndex];
		var imageLink = eXo.social.LinkShare.data.images[eXo.social.LinkShare.data.selectedImageIndex];
		var mediaItem = opensocial.newActivityMediaItem(eXo.social.LinkShare.getMimeType(imageLink), imageLink, opt_params);
		mediaItems.push(mediaItem);
		params[opensocial.Activity.Field.MEDIA_ITEMS] = mediaItems;
	}
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
