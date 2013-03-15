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

(function($) {	
	var UISpaceSearch = {
    COLOR : {
      FOCUS : "#000000",
      BLUR : "#C7C7C7"
    },
    DEFAULT_REST_INFO : {
      CONTEXT_NAME : 'rest-socialdemo',
      PATH : '/social/spaces/suggest.json'
    },
    init: function(params) {
			var uicomponentId = params.uicomponentId || null;
			var defaultSpaceNameAndDesc = params.defaultSpaceNameAndDesc || null;
			var restContextName = params.restContextName || null;
			var currentUserName = params.currentUserName || null;
			var typeOfRelation = params.typeOfRelation || null;
			var spaceURL = params.spaceURL || null;
			var typeOfSuggest = params.typeOfSuggest || null;
			var portalName = params.portalName || null;
      
			var spaceSearch = document.getElementById(uicomponentId);
			var searchEl  = $(spaceSearch).find('#SpaceSearch');
			var searchBtn = $(spaceSearch).find('#SearchButton');

      
			// Turn off auto-complete attribute of text-box control
			searchEl.attr('autocomplete','off');
			
			if (searchEl.val().trim() ==  defaultSpaceNameAndDesc) {
			  searchEl.css('color', UISpaceSearch.COLOR.BLUR);
			}
			
      searchEl.placeholder();
			
		  $(searchEl).autosuggest(buildURL(), {onSelect:function(){searchBtn.click();}, defaultVal:defaultSpaceNameAndDesc});
      
      $(searchEl).keydown(function(event) {
        var e = event || window.event;
        var keynum = e.keyCode || e.which;  
        if(keynum == 13) {
          searchBtn.click();
          event.preventDefault();
        } else {
        }
      });
      
      function buildURL() {
	      restContextName = (restContextName) ? restContextName : UISpaceSearch.DEFAULT_REST_INFO.CONTEXT_NAME;
	      var restURL = "/" + restContextName + "/" + portalName + UISpaceSearch.DEFAULT_REST_INFO.PATH;

	      restURL = restURL + '?conditionToSearch=input_value';

        if (currentUserName) {
          restURL += "&currentUser=" + currentUserName;
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
      }
	 }
};

return UISpaceSearch;
})($);

