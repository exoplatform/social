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
  var SocialUtil = eXo.social.SocialUtil;
  var statusUpdate = this.statusUpdate;
  //BUG SOC-386
  var el = this.inputEl || el; //ie fix, el = window not dom element
  statusUpdate.shareable = true;
  el.style.outlineStyle= 'none';
  if (statusUpdate.currentView == 'home') {
    if (el.value === this.DEFAULT_INPUT) {
      el.style.color="#000000";
      el.value = '';
    }
    return;
  }
  if(el.value === this.DEFAULT_INPUT) {
    el.style.color="#000000";
    el.style.height="50px";
    el.value = '';
  }
  
  SocialUtil.adjustHeight(statusUpdate.contentForAdjustHeight);
}

/**
 * element when onblur
 */
eXo.social.UIComposer.prototype.blurInput = function(el) {
  var SocialUtil = eXo.social.SocialUtil;
  var statusUpdate = this.statusUpdate;
  var el = this.inputEl || el;
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
    el.value = this.DEFAULT_INPUT;
    el.style.height="20px";
  } else {
    this.inputTextContent = content;
    statusUpdate.shareable = true;
  }
  
  SocialUtil.adjustHeight(statusUpdate.contentForAdjustHeight);
}

/**
 * When element is pressed. If keyCode is Enter, Delete or Back Space then adjust height of gadget.
 */
eXo.social.UIComposer.prototype.onKeyPress = function(evt) {
  var keyNum;
  var ENTER_KEY_NUM = 13;
  var BACK_SPACE_KEY_NUM = 8;
  var DELETE_KEY_NUM = 46;
  var SocialUtil = eXo.social.SocialUtil;
  if(window.event) {// IE
      keyNum = evt.keyCode;
    } else if (evt.which) { // Netscape/Firefox/Opera
      keyNum = evt.which;
    }
    if ((ENTER_KEY_NUM == keyNum) || (BACK_SPACE_KEY_NUM == keyNum) || (DELETE_KEY_NUM == keyNum)){
      SocialUtil.adjustHeight(this.statusUpdate.contentForAdjustHeight);
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
  var textContent = inputEl.value;
  if (textContent === undefined) {
    textContent = inputEl.textContent;
  }
  if (!textContent) return null;
  return textContent.replace(/&nbsp;/g,' ').replace(/<br>/gi, " ").replace(/<p>/gi, " ").replace(/<\/\p>/gi, " ");
}
