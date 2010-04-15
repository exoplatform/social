/**
 * PortalHttpRequest.js (eXo.social.PortalHttpRequest)
 * a patch for PortalHttpRequest to have ajaxPostRequest(url, async);
 */

(function() {
  var window_ = this;
  function PortalHttpRequest() {
    
  }
  /**
   * makes ajaxPostRequest
   */
  PortalHttpRequest.ajaxPostRequest = function(url, queryString, async, callback) {
    if (async != false) async = true;
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