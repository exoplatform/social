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
UISpaceSearch.prototype.onLoad = function(uicomponentId, spaceNames) {
	var DOMUtil = eXo.core.DOMUtil;
	var spaceSearch = document.getElementById(uicomponentId);
	var spaceSearchEl = DOMUtil.findDescendantById(spaceSearch, 'SpaceSearch');
	var defaultSpaceName = document.getElementById('defaultSpaceName').value;
	var defaultUIVal = "Space name";
	if (spaceSearchEl.value == defaultUIVal) spaceSearchEl.value = defaultSpaceName;
	(spaceSearchEl.value != defaultSpaceName) ? (spaceSearchEl.style.color = '#000000') : (spaceSearchEl.style.color = '#C7C7C7');
	this.inputTextBoxObj = spaceSearchEl;
	this.setAllSpaceName(spaceNames);
	// Initialize the input textbox
	this.initTextBox();
};

/**
 * Initialize the text-box control. 
 * @scope private.
 */
UISpaceSearch.prototype.initTextBox = function() {
	var searchEl = this.inputTextBoxObj;
	var defaultValue = document.getElementById('defaultSpaceName').value;
	var uiSpaceSearchObj = eXo.social.space.UISpaceSearch;
//	var suggestControlObj = eXo.social.space.AutoSuggestControl;
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

/**
 * Set all space name to allSpaceNames variable.
 * @scope private.
 */
UISpaceSearch.prototype.setAllSpaceName = function(allName) {
	var allSpaceNames = allName.substring(1, allName.length-1);
	var allSN = allSpaceNames.split(',');
	var allNames = [];
	for (var i=0; i < allSN.length; i++) {
		(function(idx) {
			allNames.push(allSN[idx].trim());
		})(i);
	}
	this.allSpaceName = allNames;
};

/**
 * Request suggestions for the given autosuggest control. 
 * @scope protected
 * @param oAutoSuggestControl The autosuggest control to provide suggestions for.
 */
UISpaceSearch.prototype.requestSuggestions = function (oAutoSuggestControl /*:AutoSuggestControl*/) {
    var aSuggestions = [];
    var sTextboxValue = oAutoSuggestControl.textbox.value;
    
    if (sTextboxValue.length > 0){
    
        //convert value in textbox to lowercase
        var sTextboxValueLC = sTextboxValue.toLowerCase();
        
        //search for matching states
        for (var i=0; i < this.allSpaceName.length; i++) { 

            //convert state name to lowercase
            var sStateLC = this.allSpaceName[i].toLowerCase();
            
            //compare the lowercase versions for case-insensitive comparison
            if (sStateLC.indexOf(sTextboxValueLC) == 0) {
                //add a suggestion using what's already in the textbox to begin it                
            	aSuggestions.push(this.allSpaceName[i]);

                // Check if user type like the last suggestion in drop-down list.
                if ((this.allSpaceName[i].substring(sTextboxValue.length) == "") && (aSuggestions.length == 1)) {
                	oAutoSuggestControl.hideSuggestions();
                	return;
                }
            } 
        }
    }
    
    //provide suggestions to the control
    oAutoSuggestControl.autosuggest(aSuggestions);
};

/*===================================================================*/
if(!eXo.social) eXo.social = {};
if(!eXo.social.space) eXo.social.space = {};
eXo.social.space.UISpaceSearch = new UISpaceSearch();