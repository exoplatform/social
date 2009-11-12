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

/**
 *	Change status some component when it is activated.
 *	
 *	@var filter {Object} Object is activated.
 *	@return void						
 */
UIProfileUserSearch.prototype.activeFilterText = function(filter) {
	var DOMUtil = eXo.core.DOMUtil;
	var searchEl = DOMUtil.findAncestorByClass(filter, 'UIProfileUserSearch');
	var componentId = searchEl.id;
	var defaultValue="";
	this.createId(componentId);
	this.setDefaultValue();
	filter.style.color="#000000";
	filter.focus();
	if (filter.id == this.searchId) { 
		defaultValue = this.defaultUserContact;
	} else if (filter.id == this.positionId) {
		defaultValue = this.defaultPos;
	} else {
		defaultValue = this.defaultProf;
	}
	
	if (filter.value == defaultValue) {
		filter.value='';
	}
}

/**
 *	Change status some component when it is blurred.
 *	
 *	@var filter {Object} Object is on blurred.
 *	@return void						
 */
UIProfileUserSearch.prototype.onBlurFilterText = function(filter) {
	var defaultValue="";
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
 *	Create and send action when search button is clicked.
 *	
 *	@var element {Object} Search text box.
 *	@return void						
 */
UIProfileUserSearch.prototype.searchProfileUser = function(element) {
  var DOMUtil = eXo.core.DOMUtil;
  var searchEl = DOMUtil.findAncestorByClass(element, 'UIProfileUserSearch');
  var componentId = searchEl.id;
  this.createId(componentId);
  var filterEl = document.getElementById(this.filterId);
  var userContact="";
  var position="";
  var professional="";
  var gender="";
  
  if (element.id != 'searchAll') {
	  userContact = document.getElementById(this.searchId).value;
	  this.searchAll = false;
  } else {
	  this.searchAll = true;
  }

  if (filterEl.style.display != 'none') {
	  position = document.getElementById(this.positionId).value;
	  professional = document.getElementById(this.professionalId).value;
	  gender = document.getElementById(this.genderId).value;
  } 
  
  if (userContact == this.defaultUserContact)  userContact = "";
  if (position == this.defaultPos)  position = "";
  if (professional == this.defaultProf)  professional = "";
  if (gender == this.defaultGender) gender = "";
  
  if(searchEl != null ) {
	var portletFragment = DOMUtil.findAncestorByClass(searchEl, "PORTLET-FRAGMENT");
	if (portletFragment != null) {
		var compId = portletFragment.parentNode.id;
		var href = eXo.env.server.portalBaseURL + "?portal:componentId=" + compId;
		href += "&portal:type=action&uicomponent=" + searchEl.id;
		href += "&op=Search";
		href += "&userContact=" + userContact.trim();
		href += "&position=" + position.trim();
	    href += "&professional=" + professional.trim();
	    href += "&isSearchAll=" + this.searchAll;
		href += "&gender=" + gender;
		href += "&ajaxRequest=true";
		ajaxGet(href,true);
	} 
  }
};

UIProfileUserSearch.prototype.searchProfileUserByAlphaBet = function(element, ch) {
	  var DOMUtil = eXo.core.DOMUtil;
	  var searchEl = DOMUtil.findAncestorByClass(element, 'UIProfileUserSearch');
	  var componentId = searchEl.id;
	  this.createId(componentId);
	  var filterEl = document.getElementById(this.filterId);
	  
	  if(searchEl != null ) {
		var portletFragment = DOMUtil.findAncestorByClass(searchEl, "PORTLET-FRAGMENT");
		if (portletFragment != null) {
			var compId = portletFragment.parentNode.id;
			var href = eXo.env.server.portalBaseURL + "?portal:componentId=" + compId;
			href += "&portal:type=action&uicomponent=" + searchEl.id;
			href += "&op=Search";
			href += "&charSearch=" + ch;
			href += "&isSearchAlphaBet=true";
			href += "&ajaxRequest=true";
			ajaxGet(href,true);
		} 
	  }
};

/**
 *	Call function when Enter Key is pressed in search text box.
 *	
 *	@var event {Object} Event.
 *	@return void						
 */
UIProfileUserSearch.prototype.setEnterKey = function(event) {
	var e = event || window.event;
	var elementInput = e.srcElement || e.target;
	var keynum = e.keyCode || e.which;  
	if(keynum == 13) {
		eXo.social.profile.UIProfileUserSearch.searchProfileUser(elementInput);
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
	this.searchId = 'Search' + componentId;
	this.positionId = 'Position' + componentId;
	this.professionalId = 'Professional' + componentId; 
	this.genderId = 'Gender' + componentId;
	this.filterId = 'Filter' + componentId;
};

/*===================================================================*/
if(!eXo.social) eXo.social = {};
if(!eXo.social.profile) eXo.social.profile = {};
eXo.social.profile.UIProfileUserSearch = new UIProfileUserSearch();
