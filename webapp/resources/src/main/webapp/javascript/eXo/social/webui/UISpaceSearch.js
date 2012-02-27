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
    var DOMUtil = eXo.core.DOMUtil;
    var Util = eXo.social.Util;    
    var FOCUS_COLOR = "#000000",
        BLUR_COLOR = "#C7C7C7";
        
		/**
		 * Space search.
		 * @class
		 * @scope public
		 */
		function UISpaceSearch(params) {
			this.inputTextBoxObj = null;
			this.searchButton = null;
			this.allSpaceName = params.allSpacesName || null;
			this.uicomponentId = params.uicomponentId || null;
			this.defaultSpaceNameAndDesc = params.defaultSpaceNameAndDesc || null;
			this.onLoad();
		};
		
		/**
		 * Initialize ui controls when the form is loaded.
		 */
		UISpaceSearch.prototype.onLoad = function() {
			var spaceSearch = document.getElementById(this.uicomponentId);
			var spaceSearchEl = DOMUtil.findDescendantById(spaceSearch, 'SpaceSearch');
			this.searchButton = DOMUtil.findDescendantById(spaceSearch, 'SearchButton');
			var defaultSpaceNameAndDesc = this.defaultSpaceNameAndDesc;
			var defaultUIVal = "name or description";
			
			if ((spaceSearchEl.value == defaultUIVal) || (spaceSearchEl.value.trim().length == 0)) spaceSearchEl.value = defaultSpaceNameAndDesc;
			(spaceSearchEl.value != defaultSpaceNameAndDesc) ? (spaceSearchEl.style.color = '#000000') : (spaceSearchEl.style.color = '#C7C7C7');
			
			this.inputTextBoxObj = spaceSearchEl;
			// Initialize the input textbox
			this.initTextBox();
		};
		
		/**
		 * Initialize the text-box control. 
		 * @scope private.
		 */
		UISpaceSearch.prototype.initTextBox = function() {
			var searchEl = this.inputTextBoxObj;
			var defaultValue = this.defaultSpaceNameAndDesc;
			var uiSpaceSearchObj = eXo.social.webui.UISpaceSearch;
			var suggestControlObj = eXo.social.webui.UIAutoSuggestControl;
			// Turn off auto-complete attribute of text-box control
			searchEl.setAttribute('autocomplete','off');
			
			Util.addEventListener(searchEl, 'focus', function() {
				this.style.color="#000000";
				if (this.value == defaultValue) {
					this.value='';
				}
			}, false);
			
			Util.addEventListener(searchEl, 'blur', function() {
				if ((this.value.trim() == '') || (this.value.trim() == defaultValue)) {
					this.style.color="#C7C7C7";
					this.value = defaultValue;
				}
				suggestControlObj.hideSuggestions();
		  }, false);
		  
			var searchBtn = this.searchButton;
			
			Util.addEventListener(searchEl, 'keydown', function(event) {
				var e = event || window.event;
				var keynum = e.keyCode || e.which;  
				
				if(keynum == 13) { //Enter key
					suggestControlObj.hideSuggestions();
					searchBtn.onclick();
					return false;
				} else { // Other keys (up and down key)
					suggestControlObj.handleKeyDown(e);
				}
			}, false);
				
			suggestControlObj.load(searchEl, uiSpaceSearchObj);
	 }

	 window_.eXo = window_.eXo || {};
	 window_.eXo.social = window_.eXo.social || {};
	 window_.eXo.social.webui = window_.eXo.social.webui || {};
	 window_.eXo.social.webui.UISpaceSearch = UISpaceSearch;
})();