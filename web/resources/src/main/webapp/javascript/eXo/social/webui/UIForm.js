/**
 * UIForm.js (eXo.social.webui.UIForm)
 * Wrapper for eXo.webui.UIForm to submit form without any update
 *
 */

(function() {
  var window_ = this;
  function UIForm() {
    
  }
  /**
   * submits form
   * @param  formId
   * @param  action
   * @param  callback
   * @param  async
   */
  UIForm.submitForm = function(formId, action, callback, async) {
	var form = eXo.webui.UIForm.getFormElemt(formId);
	form.elements['formOp'].value = action;
	var queryString = eXo.webui.UIForm.serializeForm(form);
	var url = form.action + "&ajaxRequest=true";
	eXo.social.PortalHttpRequest.ajaxPostRequest(url, queryString, async, callback);
  }
  
  
  //expose
  window_.eXo = window_.eXo || {};
  window_.eXo.social = window_.eXo.social || {};
  window_.eXo.social.webui = window_.eXo.social.webui || {};
  window_.eXo.social.webui.UIForm = UIForm;
})();