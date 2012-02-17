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
 * PortalHttpRequest.js (eXo.social.PortalHttpRequest)
 * a patch for PortalHttpRequest to have ajaxPostRequest(url, async);
 */

(function() {
  var window_ = this;
  function PortalHttpRequest() {
    
  }
  /**
   * Makes ajaxPostRequest
   * 
   */
  PortalHttpRequest.ajaxPostRequest = function(url, queryString, async, callback) {
    if (async !== false) async = true;
    var request = eXo.core.Browser.createHttpRequest();
    request.open('POST', url, async);
    request.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    request.setRequestHeader("Content-length", queryString.length);
    request.setRequestHeader("Connection", "close");
    request.onreadystatechange = function() {
      if(request.readyState === 4) {
        if (callback) {
          callback(request);
        }
      }
    }
    request.send(queryString);
  }
  
  //expose
  window_.eXo = window_.eXo || {};
  window_.eXo.social = window_.eXo.social || {};
  window_.eXo.social.PortalHttpRequest = PortalHttpRequest;
})();
