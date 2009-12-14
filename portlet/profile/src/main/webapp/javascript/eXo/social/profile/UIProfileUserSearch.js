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

function UIProfileUserSearch() {
   this.searchAll = false;
   this.nameTextObj = null;
   this.posTextObj = null;
   this.profTextObj = null;
   this.genderSelObj = null;
};

UIProfileUserSearch.prototype.onLoad = function(uicomponentId) {
	var DOMUtil = eXo.core.DOMUtil;
	var profileSearch = document.getElementById(uicomponentId);
	var searchEl = DOMUtil.findDescendantById(profileSearch, 'Search');
	var posEl = DOMUtil.findDescendantById(profileSearch, 'position');
	var profEl = DOMUtil.findDescendantById(profileSearch, 'professional');
	var filterBlock = DOMUtil.findDescendantById(profileSearch, 'Filter');
	var genderEl = DOMUtil.findDescendantsByTagName(profileSearch, 'select');
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
	
	this.nameTextObj = searchEl;
	this.posTextObj = posEl;
	this.profTextObj = profEl;
	this.genderSelObj = genderEl;
	if (searchEl.value == defaultNameVal) searchEl.value = defaultUserContact;
	(searchEl.value != defaultUserContact) ? (searchEl.style.color = '#000000') : (searchEl.style.color = '#C7C7C7');
	
	posEl.value = defaultPos;
	profEl.value = defaultProf;
	genderEl[0].value= defaultGender;
	
	this.initTextBox();
};

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
	
	nameEl.onfocus = posEl.onfocus = profEl.onfocus = function(event) {
		var e = event || window.event;
		var filter = e.srcElement || e.target;
		var elementId = filter.id;
		var defaultValue = '';
		if (filter != null) {
			filter.style.color="#000000";
			if (elementId === searchId) { 
				defaultValue = defaultUserContact;
			} else if (elementId === positionId) {
				defaultValue = defaultPos;
			} else {
				defaultValue = defaultProf;
			}
			if (filter.value == defaultValue) {
				filter.value='';
			}
		}
	}
	
	nameEl.onblur = posEl.onblur = profEl.onblur = function(event) {
		var e = event || window.event;
		var filter = e.srcElement || e.target;
		var elementId = filter.id;
		
		if (filter != null) {
			if (filter.id == searchId) { 
				defaultValue = defaultUserContact;
			} else if (filter.id == positionId) {
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
	
	// Event when enter is pressed.
	nameEl.onkeydown = posEl.onkeydown = profEl.onkeydown = genderEl.onkeydown = function(event) {
		var e = event || window.event;
		var textBox = e.srcElement || e.target;
		var keynum = e.keyCode || e.which;  
		var searchForm = DOMUtil.findAncestorByClass(textBox, 'UIForm');
		  
		if(keynum == 13) {
			if (searchForm != null ) UIForm.submitForm(searchForm.id, 'Search', true);
		}	
	}
}

/**
 *	Display or not for advance search block.
 *	
 *	@var newLabel {String} Label to be change when it is displayed.
 *       filterId {String} Id of filter block.
 *       elementId {String} Id of element that is displayed
 *       currentEl {Object} Element is clicked.
 *	@return void						
 */
UIProfileUserSearch.prototype.toggleFilter = function(newLabel, filterBlockId, elementId, currentEl) {
	var filter = document.getElementById(filterBlockId);
	
	var element = document.getElementById(elementId);
	
	if (filter.style.display == 'none') {
		currentEl.innerHTML = "";
		element.innerHTML = newLabel;
		filter.style.display = 'block';
    } else {
    	currentEl.innerHTML = "";
    	element.innerHTML = newLabel;
    	filter.style.display = 'none';
    }
};

/*===================================================================*/
if(!eXo.social) eXo.social = {};
if(!eXo.social.profile) eXo.social.profile = {};
eXo.social.profile.UIProfileUserSearch = new UIProfileUserSearch();
