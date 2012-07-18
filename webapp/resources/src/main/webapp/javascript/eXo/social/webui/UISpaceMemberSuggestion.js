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
(function() {
  var window_ = this;
	var DEFAULT_REST_INFO = {
	      CONTEXT_NAME : 'rest-socialdemo',
	      PATH : '/social/people/suggest.json'
	};
	    
	/**
	 * Space member suggestion.
	 * @class
	 * @scope public
	 */
	function UISpaceMemberSuggest() {};
	
	/**
	 * When form load at the first time, init controls.
	 */
	UISpaceMemberSuggest.prototype.onLoad = function() {
		var suggestEl = gj('#user');
		
		gj(suggestEl).autosuggest(buildURL(), {multisuggestion:true, defaultVal:""});
		
	  function buildURL() {
	    var restContext = eXo.social.webui.restContextName;
	    var currentUser = eXo.social.webui.currentUserName;
	    var typeOfRelation = eXo.social.webui.typeOfRelation;
	          
	    restContext = (restContext) ? restContext : DEFAULT_REST_INFO.CONTEXT_NAME;
	    
	    var restURL = "/" + restContext + DEFAULT_REST_INFO.PATH;
	          
	    restURL = restURL + '?nameToSearch=input_value';
	
      if (currentUser) {
        restURL += "&currentUser=" + currentUser;
      }
      
      if (typeOfRelation) {
	      restURL += "&typeOfRelation=" + typeOfRelation;
	    }
	
	    return restURL;
	  };
	}
	
	/*===================================================================*/
	window_.eXo.social.webui.UISpaceMemberSuggest = new UISpaceMemberSuggest();
})();