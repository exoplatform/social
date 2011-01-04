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
 * Space search.
 * @class
 * @scope public
 */
function UISpaceSearch() {
	this.inputTextBoxObj = null;
	this.allSpaceName = null;
};

/**
 * Initialize ui controls when the form is loaded.
 * @ uicomponentId Id of current component
 * @ spaceNames All space name of current search.
 */
UISpaceSearch.prototype.onLoad = function(uicomponentId) {
	var DOMUtil = eXo.core.DOMUtil;
	var spaceSearch = document.getElementById(uicomponentId);
	var spaceSearchEl = DOMUtil.findDescendantById(spaceSearch, 'SpaceSearch');
	var defaultSpaceNameAndDesc = document.getElementById('defaultSpaceNameAndDesc').value;
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
	var defaultValue = document.getElementById('defaultSpaceNameAndDesc').value;
	var uiSpaceSearchObj = eXo.social.webui.UISpaceSearch;
	var suggestControlObj = eXo.social.webui.UIAutoSuggestControl;
	// Turn off auto-complete attribute of text-box control
	searchEl.setAttribute('autocomplete','off');
	
	// Add focus event for control
	searchEl.onfocus = function() {
		searchEl.style.color="#000000";
		searchEl.focus();
		if (searchEl.value == defaultValue) {
			searchEl.value='';
		}
	}
	
	// Add blur event for control
	searchEl.onblur = function() {
		if ((searchEl.value.trim() == '') || (searchEl.value.trim() == defaultValue)) {
			searchEl.style.color="#C7C7C7";
			searchEl.value = defaultValue;
		}
		suggestControlObj.hideSuggestions();
	}
	
	// Add keydown event for control
	searchEl.onkeydown = function(event) {
		var e = event || window.event;
		var keynum = e.keyCode || e.which;  
		  
		if(keynum == 13) { //Enter key
			suggestControlObj.hideSuggestions();
			uiSpaceSearchObj.submitSearchForm(searchEl);
		} else { // Other keys (up and down key)
			suggestControlObj.handleKeyDown(e);
		}
	}
	
	suggestControlObj.load(searchEl, uiSpaceSearchObj);
}

/**
 * Submit the search form.
 * @scope private.
 */
UISpaceSearch.prototype.submitSearchForm = function(searchEl) {
	var DOMUtil = eXo.core.DOMUtil;
	var UIForm = eXo.webui.UIForm;
	var searchForm = DOMUtil.findAncestorByClass(searchEl, 'UIForm');
	if (searchForm != null ) UIForm.submitForm(searchForm.id, 'Search', true);
}

/*===================================================================*/
if(!eXo.social) eXo.social = {};
if(!eXo.social.webui) eXo.social.webui = {};
eXo.social.webui.UISpaceSearch = new UISpaceSearch();