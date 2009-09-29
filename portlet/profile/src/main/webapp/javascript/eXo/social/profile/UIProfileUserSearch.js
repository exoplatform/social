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
var eXo = window.eXo;
eXo.social = eXo.social || {};
eXo.social.profile = eXo.social.profile || {};


function UIProfileUserSearch() {
	var posActivated = false;
	var compActivated = false;
	var gendActivated = false;
};

UIProfileUserSearch.prototype.activeFilterText = function(filter) {
	if ((filter.value == 'Position') || (filter.value == 'Company') || (filter.value == 'Gender')) {
		filter.style.color="#000000";
		filter.value='';
		if (filter.id == 'Position') {
			this.posActivated=true;
		} else if (filter.id == 'Company') {
			this.compActivated=true;
		} else {
			this.gendActivated=true;
		}
	}
}

UIProfileUserSearch.prototype.onBlurFilterText = function(filter) {
	if (filter.value == '') {
		filter.style.color="#C7C7C7";
		if (filter.id == 'Position') {
			filter.value='Position';
			this.posActivated=false;
		} else if (filter.id == 'Company') {
			filter.value='Company';
			this.compActivated=false;
		} else {
			filter.value='Gender';
			this.gendActivated=false;
		}
	}
}

UIProfileUserSearch.prototype.toggleFilter = function(oldLabel, newLabel, filterId, moreSearchId) {
	var filter = document.getElementById(filterId);
	var moreSearch = document.getElementById(moreSearchId);
	if (filter.style.display == 'none') {
		moreSearch.innerHTML=newLabel;
		filter.style.display = "block";
    } else {
    	moreSearch.innerHTML=oldLabel;
    	filter.style.display = "none";
    }
};

UIProfileUserSearch.prototype.searchProfileUser = function(element, id) {
  var DOMUtil = eXo.core.DOMUtil;
  var searchEl = DOMUtil.findAncestorByClass(element, 'UIProfileUserSearch');
  
  var userContact="";
  
  if (id == null) {
	  userContact = element.value;
  } else {
	  var searchSpacesEl = DOMUtil.findDescendantById(searchEl, id);
	  userContact = searchSpacesEl.value;
  }
  
//  var position = document.getElementById("Position").value;
//  var company = document.getElementById("Company").value;
//  var gender = document.getElementById("Gender").value;
  
  
  
  if(searchEl != null ) {
	var portletFragment = DOMUtil.findAncestorByClass(searchEl, "PORTLET-FRAGMENT");
	if (portletFragment != null) {
		var compId = portletFragment.parentNode.id;
		var href = eXo.env.server.portalBaseURL + "?portal:componentId=" + compId;
		href += "&portal:type=action&uicomponent=" + searchEl.id;
		href += "&op=Search";
		if ((userContact != null) && (userContact.length != 0)) {
			href += "&userContact=" + userContact;
		}
		
//		if ((this.posActivated) && (position != null) && (position.length != 0)) {
//			href += "&position=" + position;
//		}
//		if ((this.compActivated) && (company != null) && (company.length != 0)) {
//			href += "&company=" + company;
//		}
//		if ((this.gendActivated) && (gender != null) && (gender.length != 0)) {
//			href += "&gender=" + gender;
//		}
		
		href += "&ajaxRequest=true";
		ajaxGet(href,true);
	} 
  }
}

UIProfileUserSearch.prototype.setEnterKey = function(event) {
	var e = event || window.event;
	var elementInput = e.srcElement || e.target;
	var keynum = e.keyCode || e.which;  
	
	if(keynum == 13) {
		eXo.social.profile.UIProfileUserSearch.searchProfileUser(elementInput, null);
	}						
}

/*===================================================================*/
eXo.social.profile.UIProfileUserSearch = new UIProfileUserSearch();
