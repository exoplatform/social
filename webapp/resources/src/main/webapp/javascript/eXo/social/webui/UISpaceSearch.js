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
    var COLOR = {
          FOCUS : "#000000",
          BLUR : "#C7C7C7"
        };

    var DEFAULT_REST_INFO = {
      CONTEXT_NAME : 'rest-socialdemo',
      PATH : '/social/spaces/suggest.json'
    };

		/**
		 * Space search.
		 * @class
		 * @scope public
		 */
		function UISpaceSearch(params) {
			this.uicomponentId = params.uicomponentId || null;
			this.defaultSpaceNameAndDesc = params.defaultSpaceNameAndDesc || null;
			this.onLoad();
		};
		
		/**
		 * Initialize ui controls when the form is loaded.
		 */
		UISpaceSearch.prototype.onLoad = function() {
			var spaceSearch = document.getElementById(this.uicomponentId);
			var searchEl  = gj(spaceSearch).find('#SpaceSearch');
			var searchBtn = gj(spaceSearch).find('#SearchButton');
			var uiSpaceSearchObj = eXo.social.webui.UISpaceSearch;
			var portalName = eXo.social.webui.portalName;
			var defaultUserContact = gj(searchEl).attr('placeholder');

			// Turn off auto-complete attribute of text-box control
			searchEl.attr('autocomplete','off');
			
			if (searchEl.val().trim() ==  this.defaultSpaceNameAndDesc) {
			  searchEl.css('color', COLOR.BLUR);
			}
      searchEl.placeholder();
			
		  gj(searchEl).autosuggest(buildURL(), {onSelect:callback, defaultVal:defaultUserContact});

		  function callback() {
		    searchBtn.click();
		  };

      function buildURL() {
        var restContext = eXo.social.webui.restContextName;
        var currentUser = eXo.social.webui.currentUserName;
        var typeOfRelation = eXo.social.webui.typeOfRelation;
        var spaceURL = eXo.social.webui.spaceURL;
        var typeOfSuggest = eXo.social.webui.typeOfSuggest;

	      restContext = (restContext) ? restContext : DEFAULT_REST_INFO.CONTEXT_NAME;
	      var restURL = "/" + restContext + "/" + portalName + DEFAULT_REST_INFO.PATH;

	      restURL = restURL + '?conditionToSearch=input_value';

        if (currentUser) {
          restURL += "&currentUser=" + currentUser;
        }

	      if (typeOfRelation) {
	        restURL += "&typeOfRelation=" + typeOfRelation;
	      }

        if (spaceURL) {
          if(typeOfSuggest == 'people') {
            restURL += "&spaceURL=" + spaceURL;
          }
        }

        return restURL;
      };

	 }

	 window_.eXo.social.webui.UISpaceSearch = UISpaceSearch;
})();