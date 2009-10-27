/**
 * UIComposer.js - controls the text area.
 * @author	<a href="mailto:hoatlevan@gmail.com">hoatle</a>
 * @since	Oct 20, 2009
 * @copyright	eXo Platform SEA
 */
//namespace
var eXo = eXo || {};
eXo.social = eXo.social || {};

/**
 * constructor
 */

eXo.social.UIComposer = function(el) {
	this.inputEl = el || null;
	this.statusUpdate = null; //refers to statusUpdate object
	this.DEFAULT_INPUT = (function() {
		var prefs = new gadgets.Prefs();
		return prefs.getMsg('what_are_you_doing');
	})();
	this.inputTextContent = null;
}

/**
 * element when onfocus
 * 
 */
eXo.social.UIComposer.prototype.focusInput = function(el) {
	var statusUpdate = this.statusUpdate;
	statusUpdate.shareable = true;
	el.style.outlineStyle= 'none';
	if (statusUpdate.currentView == 'home') {
		if (el.value === this.DEFAULT_INPUT) {
			el.style.color="#000000";
			el.value = '';
		}
		return;
	}
	if(el.innerHTML === this.DEFAULT_INPUT) {
		el.style.color="#000000";
		el.style.minHeight="35px";
		el.innerHTML = "";
		el.appendChild(document.createElement('br'));
	}
}

/**
 * element when onblur
 */
eXo.social.UIComposer.prototype.blurInput = function(el) {
	var statusUpdate = this.statusUpdate;
	if (statusUpdate.currentView === 'home') {
		var text = el.value;
		if (text === '') {
			statusUpdate.shareable = false;
			el.style.color="#777777";
			el.value = this.DEFAULT_INPUT;
		} else {
			this.inputTextContent = el.value;
			statusUpdate.shareable = true;
		}
		return;
	}
	var text = this.getInputContent();
	var content = "";
	if (text !== null) {
		content = text.replace(/^\s+/g, '').replace(/\s+$/g, '');
	}
	if((text === null) || (content === "")) {
		this.statusUpdate.shareable = false;
		el.style.color="#777777";
		el.innerHTML = this.DEFAULT_INPUT;
		el.style.minHeight="20px";
	} else {
		this.inputTextContent = content;
		statusUpdate.shareable = true;
	}
}

/**
 *  
 */
eXo.social.UIComposer.prototype.getInputContent = function() {
	if (!this.inputEl) {
		debug.warn("Can not find uiComposer.inputEl");
		return null;
	}
	var inputEl = this.inputEl;
	var textContent = inputEl.innerText;
	if (textContent === undefined) {
		textContent = inputEl.textContent;
	}
	if (!textContent) return null;
	return textContent.replace(/&nbsp;/g,' ').replace(/<br>/gi, " ").replace(/<p>/gi, " ").replace(/<\/\p>/gi, " ");
}

 
