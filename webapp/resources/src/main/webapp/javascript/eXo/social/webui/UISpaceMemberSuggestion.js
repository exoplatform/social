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
 * Space member suggestion.
 * @class
 * @scope public
 */
function UISpaceMemberSuggest() {
   /**
    * User name search text box object.
    * @scope private.
    */
   this.userNameTextObj = null;
};

/**
 * When form load at the first time, init controls.
 */
UISpaceMemberSuggest.prototype.onLoad = function() {
	var suggestEl = document.getElementById('user');

	this.userNameTextObj = suggestEl;
	
	this.initTextBox();
};

/**
 * Set init value and event for control.
 * @scope private.
 */
UISpaceMemberSuggest.prototype.initTextBox = function() {
	var nameEl = this.userNameTextObj;
	var UIForm = eXo.webui.UIForm;
	var suggestElId = 'user';
	var UISpaceMemberSuggestObj = eXo.social.webui.UISpaceMemberSuggest;
	var suggestControlObj = eXo.social.webui.UIAutoSuggestMultiValueControl;
	
	// Turn off auto-complete attribute of text-box control
	nameEl.setAttribute('autocomplete','off');
	
	// Add keydown event for control
	nameEl.onkeydown = function(event) {
		var e = event || window.event;
		var textBox = e.srcElement || e.target;
		var keynum = e.keyCode || e.which;  
		
		if(keynum == 13) {
			suggestControlObj.hideSuggestions();
			return;
//			UISpaceMemberSuggestObj.submitSearchForm(textBox);
		} else if (textBox.id == suggestElId) {
			// Other keys (up and down key)
			suggestControlObj.handleKeyDown(e);
		} else {
			// ignore
		}
	}
	
	nameEl.onblur = function(event) {
		suggestControlObj.hideSuggestions();
	}
	
	// Add suggestion capability for user contact name search text-box.
	suggestControlObj.load(nameEl, UISpaceMemberSuggestObj);
}

/**
 * Submit the search form.
 * @scope private.
 */
UISpaceMemberSuggest.prototype.setValueIntoTextBox = function(suggestEl /*input text box*/) {
		
}

/**
 * Submit the search form.
 * @scope private.
 */
UISpaceMemberSuggest.prototype.submitSearchForm = function(suggestEl /*input text box*/) {
	var UIForm = eXo.webui.UIForm;
	var searchForm = gj(suggestEl).closest('.UIForm'); 
	if (searchForm != null ) UIForm.submitForm(searchForm.id, 'Search', true);
}
/*===================================================================*/
if(!eXo.social) eXo.social = {};
if(!eXo.social.webui) eXo.social.webui = {};
eXo.social.webui.UISpaceMemberSuggest = new UISpaceMemberSuggest();
