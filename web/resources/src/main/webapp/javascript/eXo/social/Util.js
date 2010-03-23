/**
 * Util.js
 * Utility class
 * @author	<a href="mailto:hoatlevan@gmail.com">hoatle</a>
 * @since	Oct 20, 2009
 * @copyright	eXo Platform SEA
 */
 
//namespace
eXo.social = eXo.social || {};

/**
 * class definition
 */
 eXo.social.Util = function() {
 	//do not allow creating new object
 	if (this instanceof eXo.social.Util) {
		throw ("static class does not allow constructing a new object");
 	}
 }
 

 
 /**
  * gets element by id
  * @static 
  */
 eXo.social.Util.getElementById = function(id) {
 	var el = document.getElementById(id);
 	if (!el && console) console.info("can not find element with id: " + id);
 	return el;
 }
 
 /**
  * gets element by tagName
  * @param	tagName
  * @param	parent element
  * @static
  */
 eXo.social.Util.getElementsByTagName = function(tagName, parent) {
 	var parent = parent || document;
 	var els = parent.getElementsByTagName(tagName);
 	if (!els && console) console.info("can not find elements with tagName: " + tagName);
 	return els;
 }
 
 /**
 * Returns true if element has the css clazz
 * Uses a regular expression to search more quickly
 * @param	element
 * @param	clazz
 * @return	boolean
 * @static
 */
eXo.social.Util.hasClass = function(element, clazz) {
	var reg = new RegExp('(^|\\s+)' + clazz + '(\\s+|$)');
	return reg.test(element['className']);
} ;
 
 /**
  * gets element by clazz
  * @param	clazz
  * @param	parentElement
  * @return	array
  * @static
  */
 eXo.social.Util.getElementsByClass = function(root, tagName, clazz) {
 	var Util = eXo.social.Util;
	var list = [];
	var nodes = root.getElementsByTagName(tagName);
	for (var i = 0, l = nodes.length; i < l; i++)  {
		if (Util.hasClass(nodes[i], clazz)) list.push(nodes[i]);
	}
  	return list;
 }
 
 
/**
 * adds element with specified parentId, tagName, elementId and html content
 * @param	parentId
 * @param	tagName
 * @param	elementId
 * @param	html
 * @return	newElement 
 * @static
 */
eXo.social.Util.addElement = function(parentId, tagName, elementId, html) {
    if (parentId === null || tagName === null || html === null) {
    	if (console) {
    		console.warn("Do not provide all params");
    	}
    	return;
    }
    if (document.getElementById(elementId)) {
    	return; //do not create repeated element
    }
    var parent = document.getElementById(parentId);
    if (parent === null) return;
    var newElement = document.createElement(tagName);
    if (elementId) {
    	newElement.setAttribute('id', elementId);
    }
    newElement.innerHTML = html;
    parent.appendChild(newElement);
    return newElement;
}

/**
 * removes element from DOM by its id
 * @param	elementId
 * @static
 */
eXo.social.Util.removeElementById = function(elementId) {
    var element = document.getElementById(elementId);
    if (element === null) {
    	return;
    }
    element.parentNode.removeChild(element);
}

/**
 * hides element by its id
 * @param	elementId
 * @static
 */
eXo.social.Util.hideElement = function(elementId) {
	var element = document.getElementById(elementId);
	if (element === null) {
		return;
	} 
	element.style.display='none';
}

/**
 * shows element by id
 * @param	elementI
 * @display	can be "inline" or "block" with default = "block" 
 * @static
 */
eXo.social.Util.showElement = function(elementId, display) {
	if (display !== 'inline') {
		display = 'block';
	}
	var element = document.getElementById(elementId);
	if (element == null) {
		return;
	} 
	element.style.display = display;
}

/**
 * inserts an element after an element
 * @param	newNode the node/ element to be inserted
 * @param	refNode the reference node/ element
 * @static 
 */
eXo.social.Util.insertAfter = function(newNode, refNode) {
	if (!newNode || !refNode) {
		return;
	}
	//checks if refNode.nextSibling is null
	refNode.parentNode.insertBefore(newNode, refNode.nextSibling);
}


/**
 * checks if keyNum == ENTER key
 * @param	event
 * @static
 */
eXo.social.Util.isEnterKey = function(e) {
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
 * @static
 * @deprecated
 */
eXo.social.Util.getAttributeValue = function(element, attrName) {
	for(var x = 0, l = element.attributes.length; x < l; x++) {
	  if(element.attributes[x].nodeName.toLowerCase() == attrName) {
		return element.attributes[x].nodeValue;
	  }
	}
	return null;
}

/**
 * Cross browser add event listener method. For 'evt' pass a string value with the leading "on" omitted
 * e.g. Util.addEventListener(window,'load',myFunctionNameWithoutParenthesis,false);
 * @param	obj object to attach event
 * @param	evt event name: click, mouseover, focus, blur...
 * @param	func	function name
 * @param	useCapture	true or false; if false => use bubbling
 * @static
 * @see		http://phrogz.net/JS/AttachEvent_js.txt
 */
eXo.social.Util.addEventListener = function(obj, evt, fnc, useCapture) {
	if (obj === null || evt === null || fnc ===  null || useCapture === null) {
		debug.warn('all params is required from Util.addEventListener!');
		return;
	}
	if (!useCapture) useCapture = false;
	if (obj.addEventListener){
		obj.addEventListener(evt, fnc, useCapture);
	} else if (obj.attachEvent) {
		obj.attachEvent('on'+evt, function(evt) {
			fnc.call(obj, evt);
		});
	} else{
		myAttachEvent(obj, evt, fnc);
		obj['on'+evt] = function() { myFireEvent(obj,evt) };
	}

	//The following are for browsers like NS4 or IE5Mac which don't support either
	//attachEvent or addEventListener
	var myAttachEvent = function(obj, evt, fnc) {
		if (!obj.myEvents) obj.myEvents={};
		if (!obj.myEvents[evt]) obj.myEvents[evt]=[];
		var evts = obj.myEvents[evt];
		evts[evts.length] = fnc;
	}
	
	var myFireEvent = function(obj, evt) {
		if (!obj || !obj.myEvents || !obj.myEvents[evt]) return;
		var evts = obj.myEvents[evt];
		for (var i=0,len=evts.length;i<len;i++) evts[i]();
	}
}

/**
 * removes event listener. 
 * @param	obj element
 * @param	evt event name, 'click', 'blur'. 'focus'...
 * @func	function name to be removed if found
 * @static
 * //TODO make sure method cross-browsered
 */
eXo.social.Util.removeEventListener = function(obj, evt, func, useCapture) {
	if (!useCapture) useCapture = false;
	if (obj.removeEventListener) {
		obj.removeEventListener(evt, func, useCapture);
	} else if (obj.detachEvent) {//IE
		obj.detachEvent('on'+evt, func)
	}
}