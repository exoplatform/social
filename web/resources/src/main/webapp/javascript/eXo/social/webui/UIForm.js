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