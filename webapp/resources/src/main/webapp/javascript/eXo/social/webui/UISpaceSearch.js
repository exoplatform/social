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
    var FOCUS_COLOR = "#000000",
        BLUR_COLOR = "#C7C7C7";
        
		/**
		 * Space search.
		 * @class
		 * @scope public
		 */
		function UISpaceSearch(params) {
			this.uicomponentId = params.uicomponentId || null;
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
			var suggestControlObj = eXo.social.webui.UIAutoSuggestControl;
			// Turn off auto-complete attribute of text-box control
			searchEl.attr('autocomplete','off');
			
      searchEl.placeholder();
      
			searchEl.blur(function() {
			  suggestControlObj.hideSuggestions();
			});
			
			searchEl.on('keydown', function(event) {
				var e = event || window.event;
				var keynum = e.keyCode || e.which;  
				
				if(keynum == 13) { //Enter key
					suggestControlObj.hideSuggestions();
					searchBtn.click();
					return false;
				} else { // Other keys (up and down key)
					suggestControlObj.handleKeyDown(e);
				}
			});
			
			// Note: change this input when migrate to jQuery
			suggestControlObj.load(searchEl.get(0), uiSpaceSearchObj);
	 }

	 window_.eXo = window_.eXo || {};
	 window_.eXo.social = window_.eXo.social || {};
	 window_.eXo.social.webui = window_.eXo.social.webui || {};
	 window_.eXo.social.webui.UISpaceSearch = UISpaceSearch;
})();