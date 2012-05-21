/**
 * UIProfile.js
 */
(function() {
  // private class
  function UIProfile(params) {
    // use this to mark are local variables
    this.positionId = params.positionId || null;
    this.saveButtonId = params.saveButtonId || null;
    
    // initialize
    this.init();
  }
  
  var keys = {
    ENTER_KEY : 13
  }
  
  UIProfile.prototype.init = function() {
    var Util = eXo.social.Util;
    var positionEl = Util.getElementById(this.positionId);
    var saveButtonEl = Util.getElementById(this.saveButtonId);
    
    if (positionEl && saveButtonEl) {
      Util.addEventListener(positionEl, ['keydown'], function(event) {
        var keynum = event.keyCode || event.which;
        if (keynum == keys.ENTER_KEY) {
          (saveButtonEl.onclick || saveButtonEl.click)();
          eXo.core.EventManager.cancelEvent(event);
          return;
        }
      }, false);
    }
  }
  
  //namespace and exposion
  eXo = eXo || {};
  eXo.social = eXo.social || {};
  eXo.social.webui = eXo.social.webui || {};
  eXo.social.webui.UIProfile = UIProfile;
})();
