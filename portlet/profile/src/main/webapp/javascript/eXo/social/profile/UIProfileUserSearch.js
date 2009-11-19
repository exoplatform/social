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
   this.searchId = null;
   this.positionId = null;
   this.professionalId = null;
   this.genderId = null;
   this.filterId = null;
   this.defaultUserContact = "";
   this.defaultPos = "";
   this.defaultComp = "";
   this.defaultProf = "";
   this.defaultGender = "";
   this.searchAll = false;
};

UIProfileUserSearch.prototype.onLoad = function(uicomponentId) {
	var DOMUtil = eXo.core.DOMUtil;
	var profileSearch = document.getElementById(uicomponentId);
	var searchEl = DOMUtil.findDescendantById(profileSearch, 'Search');
	var posEl = DOMUtil.findDescendantById(profileSearch, 'position');
	var profEl = DOMUtil.findDescendantById(profileSearch, 'professional');
	var filterBlock = DOMUtil.findDescendantById(profileSearch, 'Filter');
	var genderEl = DOMUtil.findDescendantsByTagName(profileSearch, 'select');
	var defaultUserContact = document.getElementById('defaultUserContact').value;
	var defaultPos = document.getElementById('defaultPos').value;
	var defaultProf = document.getElementById('defaultProf').value;
	var defaultGender = document.getElementById('defaultGender').value;
	var defaultNameVal = "name";
	var defaultPosVal = "position";
	var defaultProfVal = "professional";
	var defaultGenderVal = "Gender";
	if (searchEl.value == defaultNameVal) searchEl.value = defaultUserContact;
	(searchEl.value != defaultUserContact) ? (searchEl.style.color = '#000000') : (searchEl.style.color = '#C7C7C7');
	
	posEl.value = defaultPos;
	profEl.value = defaultProf;
	genderEl[0].value=defaultGender;
};

/**
 *	Change status some component when it is activated.
 *	
 *	@var filter {Object} Object is activated.
 *	@return void						
 */

UIProfileUserSearch.prototype.activeFilterText = function(elementId, componentId) {
	var DOMUtil = eXo.core.DOMUtil;
	var profileSearch = document.getElementById(componentId);
	var filter = DOMUtil.findDescendantById(profileSearch, elementId);
	//if (filter == null) filter = DOMUtil.findDescendantsByTagName(profileSearch, 'select');
	this.createId(componentId);
	this.setDefaultValue();
	if (filter != null) {
		filter.style.color="#000000";
		if (elementId == this.searchId) { 
			defaultValue = this.defaultUserContact;
		} else if (elementId == this.positionId) {
			defaultValue = this.defaultPos;
		} else {
			defaultValue = this.defaultProf;
		}
		
		if (filter.value == defaultValue) {
			filter.value='';
		}
	}
}

/**
 *	Change status some component when it is blurred.
 *	
 *	@var filter {Object} Object is on blurred.
 *	@return void						
 */
UIProfileUserSearch.prototype.onBlurFilterText = function(elementId, componentId) {
	var DOMUtil = eXo.core.DOMUtil;
	var profileSearch = document.getElementById(componentId);
	var filter = DOMUtil.findDescendantById(profileSearch, elementId);
	this.createId(componentId);
	this.setDefaultValue();
	if (filter != null) {
		if (filter.id == this.searchId) { 
			defaultValue = this.defaultUserContact;
		} else if (filter.id == this.positionId) {
			defaultValue = this.defaultPos;
		} else {
			defaultValue = this.defaultProf;
		}
		
		if (filter.value.trim() == '') {
			filter.style.color="#C7C7C7";
			filter.value=defaultValue;
		}
		
		if (filter.value.trim() == this.defaultGender) filter.style.color="#C7C7C7";
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
		currentEl.innerHTML="";
		element.innerHTML=newLabel;
		filter.style.display = 'block';
    } else {
    	currentEl.innerHTML="";
    	element.innerHTML=newLabel;
    	filter.style.display = 'none';
    }
};

/**
 *	Call function when Enter Key is pressed in search text box.
 *	
 *	@var event {Object} Event.
 *	@return void						
 */
UIProfileUserSearch.prototype.setEnterKey = function(event, elementInputId) {
	var e = event || window.event;
	var elementInput = e.srcElement || e.target;
	var keynum = e.keyCode || e.which;  
	var element = document.getElementById(elementInputId);
	var DOMUtil = eXo.core.DOMUtil;
	var UIForm = eXo.webui.UIForm;
	var searchEl = DOMUtil.findAncestorByClass(element, 'UIForm');
	  
	if(keynum == 13) {
		if (searchEl != null ) UIForm.submitForm(searchEl.id, 'Search', true);
	}							
};

UIProfileUserSearch.prototype.setDefaultValue = function() {
	this.defaultUserContact = document.getElementById('defaultUserContact').value;
	this.defaultPos = document.getElementById('defaultPos').value;
	this.defaultProf = document.getElementById('defaultProf').value;
	this.defaultGender = document.getElementById('defaultGender').value;
}

/**
 *	Create id of elements with input component ID.
 *	
 *	@var componentId {String} Current component ID.
 *	@return void						
 */
UIProfileUserSearch.prototype.createId = function(componentId) {
	this.searchId = 'Search';
	this.positionId = 'position';
	this.professionalId = 'professional';
	this.genderId = 'gender';
	this.filterId = 'Filter';
};

/*===================================================================*/
if(!eXo.social) eXo.social = {};
if(!eXo.social.profile) eXo.social.profile = {};
eXo.social.profile.UIProfileUserSearch = new UIProfileUserSearch();
