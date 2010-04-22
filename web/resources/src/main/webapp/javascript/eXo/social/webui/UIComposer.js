/**
 * UIComposer.js
 * 
 * Requires: eXo.social.Util
 *
 */


(function () {
  var Util = eXo.social.Util;
  var window_ = this;

  function UIComposer(params) {
    this.configure(params);
    this.init();
  }

  UIComposer.prototype.configure = function(params) {
    this.composerId = params.composerId || 'composerInput';
    this.defaultInput = params.defaultInput || "";
    this.minCharactersRequired = params.minCharactersRequired || 0;
    this.maxCharactersAllowed = params.maxCharactersAllowed || 0;
    this.focusColor = params.focusColor || '#000000';
    this.blurColor = params.blurColor || '#777777';
    this.minHeight = params.minHeight || '20px';
    this.focusHeight = params.focusHeight || '35px';
    this.maxHeight = params.maxHeight || '50px';
    this.focusCallback = params.focusCallback;
    this.blurCallback = params.blurCallback;
    this.keypressCallback = params.keypressCallback;
    this.postMessageCallback = params.postMessageCallback;
    this.userTyped = false;
  }


  UIComposer.prototype.init = function() {
    this.composer = Util.getElementById(this.composerId);
    this.shareButton = Util.getElementById('ShareButton');
    if (!(this.composer && this.shareButton)) {
      alert('error: can not find composer or shareButton!');
    }

    this.composer.value = this.defaultInput;
    this.composer.style.height = this.minHeight;
    this.composer.style.color = this.blurColor;
    this.shareButton.style.background = 'white'
    this.shareButton.disabled = false;
    var uiComposer = this;
    Util.addEventListener(this.composer, 'focus', function() {
      if (uiComposer.composer.value === uiComposer.defaultInput) {
        uiComposer.composer.value = '';
        uiComposer.composer.style.height = uiComposer.maxHeight;
        uiComposer.composer.style.color = uiComposer.focusColor;
      }
      if (uiComposer.focusCallback) {
        uiComposer.focusCallback();
      }
    }, false);

    Util.addEventListener(this.composer, 'blur', function() {
      if (uiComposer.composer.value === '') {
        uiComposer.composer.value = uiComposer.defaultInput;
        uiComposer.composer.style.height = uiComposer.minHeight;
        uiComposer.composer.style.color = uiComposer.blurColor;
        //uiComposer.shareButton.style.background = 'white';
        //uiComposer.shareButton.disabled = true;
      }
      if (uiComposer.blurCallback) {
        uiComposer.blurCallback();
      }
    }, false);

    Util.addEventListener(this.composer, 'keypress', function() {
      if (uiComposer.minCharactersRequired !== 0) {
        //TODO hoatle handle backspace problem
        if (uiComposer.composer.value.length >= uiComposer.minCharactersRequired) {
          uiComposer.shareButton.style.background = 'white';
          uiComposer.shareButton.disabled = false;
        } else {
          uiComposer.shareButton.style.background = '';
          uiComposer.shareButton.disabled = true;
        }
      } else {
        uiComposer.shareButton.style.background = 'white';
        uiComposer.shareButton.disabled = false;
      }
      
      if (uiComposer.maxCharactersAllowed !== 0) {
        if (uiComposer.composer.value.length >= uiComposer.maxCharactersAllowed) {
          //substitue it
          //TODO hoatle have a countdown displayed on the form
          uiComposer.composer.value = uiComposer.composer.value.substring(0, uiComposer.maxCharactersAllowed);
        }
      }
      if (uiComposer.keypressCallback) {
        uiComposer.keypressCallback();
      }
    }, false);
  }

  UIComposer.prototype.getValue = function() {
    return composer.value;
  }

  //expose
  window_.eXo = window_.eXo || {};
  window_.eXo.social = window_.eXo.social || {};
  window_.eXo.social.webui = window_.eXo.social.webui || {};
  window_.eXo.social.webui.UIComposer = UIComposer;
})();