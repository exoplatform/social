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
 * Profile user search.
 * @class
 * @scope public
 */
function UIProfileUserSearch() {
   /**
    * Contact name search text box object.
    * @scope private.
    */
   this.nameTextObj = null;
   /**
    * Contact name search text box object.
    * @scope private.
    */
   this.posTextObj = null;
   
   /**
    * Contact name search text box object.
    * @scope private.
    */
   this.profTextObj = null;
   
   /**
    * Contact name search text box object.
    * @scope private.
    */
   this.genderSelObj = null;
   
   /**
    * 
    */
   this.filterBlock = null;
   
   /**
    * All Contact name for suggesting.
    * @scope private.
    */
   this.allContactName = null;
};

/**
 * When form load at the first time, init controls.
 */
UIProfileUserSearch.prototype.onLoad = function(uicomponentId, allContactNames) {
	var DOMUtil = eXo.core.DOMUtil;
	var profileSearch = document.getElementById(uicomponentId);
	var searchEl = DOMUtil.findDescendantById(profileSearch, 'Search');
	var posEl = DOMUtil.findDescendantById(profileSearch, 'position');
	var profEl = DOMUtil.findDescendantById(profileSearch, 'professional');
	var filterBlock = DOMUtil.findDescendantById(profileSearch, 'Filter');
	var genderEl = DOMUtil.findDescendantsByTagName(profileSearch, 'select');
	var moreSearchEl = DOMUtil.findDescendantById(profileSearch, 'MoreSearch');
	var hideMoreSearchEl = DOMUtil.findDescendantById(profileSearch,'HideMoreSearch');
	// Get default value
	var defaultUserContact = document.getElementById('defaultUserContact').value;
	var defaultPos = document.getElementById('defaultPos').value;
	var defaultProf = document.getElementById('defaultProf').value;
	var defaultGender = document.getElementById('defaultGender').value;
	// Default value set in component
	var defaultNameVal = "name";
	var defaultPosVal = "position";
	var defaultProfVal = "professional";
	var defaultGenderVal = "Gender";
	// filter has input value or not
	var hasFilter = false;
	
	this.nameTextObj = searchEl;
	this.posTextObj = posEl;
	this.profTextObj = profEl;
	this.genderSelObj = genderEl;
	this.filterBlock = filterBlock;
	
	if ((posEl.value != defaultPos) || (profEl.value != defaultProf) || (genderEl[0].value != defaultGender)) {
		hasFilter = true;
	}
	
	// Need for the first run time or component is empty then initialize the component value.
	if ((searchEl.value == defaultNameVal) || (searchEl.value.trim().length == 0)) searchEl.value = defaultUserContact;
	if ((posEl.value == defaultPosVal) || (posEl.value.trim().length == 0)) posEl.value = defaultPos;
	if ((profEl.value == defaultProfVal) || (profEl.value.trim().length == 0)) profEl.value = defaultProf;
	
	(searchEl.value != defaultUserContact) ? (searchEl.style.color = '#000000') : (searchEl.style.color = '#C7C7C7');
	(posEl.value != defaultPos) ? (posEl.style.color = '#000000') : (posEl.style.color = '#C7C7C7');
	(profEl.value != defaultProf) ? (profEl.style.color = '#000000') : (profEl.style.color = '#C7C7C7');
	
	if (hasFilter) {
		moreSearchEl.style.display='none';
		hideMoreSearchEl.style.display='block';
		filterBlock.style.display='block';
	} else {
		posEl.value = defaultPos;
		profEl.value = defaultProf;
		genderEl[0].value= defaultGender;
		moreSearchEl.style.display='block';
		hideMoreSearchEl.style.display='none';
		filterBlock.style.display='none';
	}
	
	this.setAllContactName(allContactNames);
	this.initTextBox();
};

/**
 * Set init value and event for control.
 * @scope private.
 */
UIProfileUserSearch.prototype.initTextBox = function() {
	var nameEl = this.nameTextObj;
	var posEl = this.posTextObj;
	var profEl = this.profTextObj;
	var genderEl = this.genderSelObj;
	var DOMUtil = eXo.core.DOMUtil;
	var UIForm = eXo.webui.UIForm;
	var searchId = 'Search';
	var positionId = 'position';
	var professionalId = 'professional';
	var genderId = 'gender';
	var filterId = 'Filter';
	var defaultUserContact = document.getElementById('defaultUserContact').value;
	var defaultPos = document.getElementById('defaultPos').value;
	var defaultProf = document.getElementById('defaultProf').value;
	var defaultGender = document.getElementById('defaultGender').value;
	var uiProfileUserSearchObj = eXo.social.webui.UIProfileUserSearch;
	var suggestControlObj = eXo.social.webui.UIAutoSuggestControl;
	
	// Turn off auto-complete attribute of text-box control
	nameEl.setAttribute('autocomplete','off');
	posEl.setAttribute('autocomplete','off');
	profEl.setAttribute('autocomplete','off');
	
	// Add focus event for control
	nameEl.onfocus = posEl.onfocus = profEl.onfocus = function(event) {
		var e = event || window.event;
		var filter = e.srcElement || e.target;
		var elementId = filter.id;
		var defaultValue = '';
		if (filter != null) {
			filter.style.color="#000000";
			if (elementId == searchId) { 
				defaultValue = defaultUserContact;
			} else if (elementId == positionId) {
				defaultValue = defaultPos;
			} else {
				defaultValue = defaultProf;
			}
			if (filter.value == defaultValue) {
				filter.value='';
			}
		}
	}

	// Add blur event for control
	nameEl.onblur = posEl.onblur = profEl.onblur = function(event) {
		var e = event || window.event;
		var filter = e.srcElement || e.target;
		var elementId = filter.id;
		
		if (filter != null) {
			
			if (elementId == searchId) { 
				defaultValue = defaultUserContact;
				// If current text-box is contact name apply suggestion 
				suggestControlObj.hideSuggestions();
			} else if (elementId == positionId) {
				defaultValue = defaultPos;
			} else {
				defaultValue = defaultProf;
			}
			
			if (filter.value.trim() == '') {
				filter.style.color="#C7C7C7";
				filter.value=defaultValue;
			}
			
			if (filter.value.trim() == defaultGender) filter.style.color="#C7C7C7";
		}
	}
	
	// Add keydown event for control
	nameEl.onkeydown = function(event) {
		var e = event || window.event;
		var textBox = e.srcElement || e.target;
		var keynum = e.keyCode || e.which;  
		var searchForm = DOMUtil.findAncestorByClass(textBox, 'UIForm');
		
		if(keynum == 13) {
			posEl.value = defaultPos;
			profEl.value = defaultProf;
			genderEl[0].value= defaultGender;
			
			suggestControlObj.hideSuggestions();
			uiProfileUserSearchObj.submitSearchForm(textBox);
		} else if (textBox.id == searchId) {
			// Other keys (up and down key)
			suggestControlObj.handleKeyDown(e);
		} else {
			// ignore
		}
	}
	
	// Add keydown event for control
	posEl.onkeydown = profEl.onkeydown = genderEl.onkeydown = function(event) {
		var e = event || window.event;
		var textBox = e.srcElement || e.target;
		var keynum = e.keyCode || e.which;  
		var searchForm = DOMUtil.findAncestorByClass(textBox, 'UIForm');
		
		if(keynum == 13) {
			suggestControlObj.hideSuggestions();
			uiProfileUserSearchObj.submitSearchForm(textBox);
		} else if (textBox.id == searchId) {
			// Other keys (up and down key)
			suggestControlObj.handleKeyDown(e);
		} else {
			// ignore
		}
	}
	
	// Add suggestion capability for user contact name search text-box.
	suggestControlObj.load(nameEl, uiProfileUserSearchObj);
}

/**
 * Set default values for filter block component if it not display.<br>
 */
UIProfileUserSearch.prototype.clickSearch = function(uicomponentId) {
	var posEl = this.posTextObj;
	var profEl = this.profTextObj;
	var genderEl = this.genderSelObj;
	var filterBlock = this.filterBlock;
	var defaultPos = document.getElementById('defaultPos').value;
	var defaultProf = document.getElementById('defaultProf').value;
	var defaultGender = document.getElementById('defaultGender').value;
	var DOMUtil = eXo.core.DOMUtil;
	var UIForm = eXo.webui.UIForm;
	var profileSearch = document.getElementById(uicomponentId);
	var searchEl = DOMUtil.findDescendantById(profileSearch, 'Search');
	
	var searchForm = DOMUtil.findAncestorByClass(searchEl, 'UIForm');

	if (filterBlock.style.display == 'none') {
		posEl.value = defaultPos;
		profEl.value = defaultProf;
		genderEl[0].value= defaultGender;
	}
	
	if (searchForm != null ) UIForm.submitForm(searchForm.id, 'Search', true);
}

/**
 * Submit the search form.
 * @scope private.
 */
UIProfileUserSearch.prototype.submitSearchForm = function(searchEl /*input text box*/) {
	var DOMUtil = eXo.core.DOMUtil;
	var UIForm = eXo.webui.UIForm;
	var searchForm = DOMUtil.findAncestorByClass(searchEl, 'UIForm');
	if (searchForm != null ) UIForm.submitForm(searchForm.id, 'Search', true);
}

/**
 *	Display or not for advance search block.
 *	
 *	@var newLabel {String} Label to be change when it is displayed.
 *       filterId {String} Id of filter block.
 *       elementId {String} Id of element that is displayed
 *       currentEl {Object} Element is clicked.
 *	@return void					
 *  @scope private.	
 */
UIProfileUserSearch.prototype.toggleFilter = function(newLabel, filterBlockId, elementId, currentEl) {
	var filter = document.getElementById(filterBlockId);
	
	var element = document.getElementById(elementId);
	
	if (filter.style.display == 'none') { // Click More
		currentEl.style.display = 'none';
		element.innerHTML = newLabel;
		element.style.display = 'block';
		filter.style.display = 'block';
    } else { // Click Hide
    	currentEl.style.display = 'none';
    	element.innerHTML = newLabel;
    	element.style.display = 'block';
    	filter.style.display = 'none';
    }
};

/**
 * Set all contact name to allContactName variable.
 * @scope private.
 */
UIProfileUserSearch.prototype.setAllContactName = function(allName) {
	var allContactNames = allName.substring(1, allName.length-1);
	var allSN = allContactNames.split(',');
	var allNames = [];
	for (var i=0; i < allSN.length; i++) {
		(function(idx) {
			allNames.push(allSN[idx].trim());
		})(i);
	}
	this.allContactName = allNames;
};

/**
 * Request suggestions for the given autosuggest control. 
 * @scope protected
 * @param oAutoSuggestControl The autosuggest control to provide suggestions for.
 */
UIProfileUserSearch.prototype.requestSuggestions = function (oAutoSuggestControl /*:AutoSuggestControl*/) {
    var aSuggestions = [];
    var sTextboxValue = oAutoSuggestControl.textbox.value;
    
    if (sTextboxValue.length > 0){
    
        //convert value in textbox to lowercase
        var sTextboxValueLC = sTextboxValue.toLowerCase();
        
        //search for matching states
        for (var i=0; i < this.allContactName.length; i++) { 

            //convert state name to lowercase
            var sStateLC = this.allContactName[i].toLowerCase();
            
            //compare the lowercase versions for case-insensitive comparison
            if (sStateLC.indexOf(sTextboxValueLC) == 0) {
                //add a suggestion using what's already in the textbox to begin it                
            	aSuggestions.push(this.allContactName[i]);

                // Check if user type like the last suggestion in drop-down list.
                if ((this.allContactName[i].substring(sTextboxValue.length) == "") && (aSuggestions.length == 1)) {
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
if(!eXo.social.webui) eXo.social.webui = {};
eXo.social.webui.UIProfileUserSearch = new UIProfileUserSearch();
