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

function UISpaceSearch() {
   this.searchId = null;
};

/**
 *	Change status some component when it is activated.
 *	
 *	@var filter {Object} Object is activated.
 *	@return void						
 */
UISpaceSearch.prototype.activeSearchText = function(searchBox) {
	searchBox.style.color="#000000";
	searchBox.focus();
	if (searchBox.value == 'Space name') {
		searchBox.value='';
	}
};

/**
 *	Change status some component when it is blurred.
 *	
 *	@var filter {Object} Object is on blurred.
 *	@return void						
 */
UISpaceSearch.prototype.onBlurSearchText = function(searchBox) {
	if ((searchBox.value.trim() == '') || (searchBox.value.trim() == 'Space name')) {
		searchBox.style.color="#C7C7C7";
		searchBox.value='Space name';
	} 
};

/**
 *	Create and send action when search button is clicked.
 *	
 *	@var element {Object} Search text box.
 *	@return void						
 */
UISpaceSearch.prototype.searchSpaceByName = function(element) {
  var DOMUtil = eXo.core.DOMUtil;
  var searchEl = DOMUtil.findAncestorByClass(element, 'UISpaceSearch');
  var spaceName="";

  spaceName = element.value;
  
  if(searchEl != null ) {
	var portletFragment = DOMUtil.findAncestorByClass(searchEl, "PORTLET-FRAGMENT");
	if (portletFragment != null) {
		var compId = portletFragment.parentNode.id;
		var href = eXo.env.server.portalBaseURL + "?portal:componentId=" + compId;
		href += "&portal:type=action&uicomponent=" + searchEl.id;
		href += "&op=Search";
		href += "&spaceName=" + spaceName.trim();
		href += "&isFirstCharOfSpaceName=false";
		href += "&ajaxRequest=true";
		ajaxGet(href,true);
	} 
  }
};

/**
 *	Create and send action when search button is clicked.
 *	
 *	@var element {Object} Search text box.
 *	@return void						
 */
UISpaceSearch.prototype.searchSpaceByFirstCharOfName = function(element, ch) {
  var DOMUtil = eXo.core.DOMUtil;
  var searchEl = DOMUtil.findAncestorByClass(element, 'UISpaceSearch');
  
  if(searchEl != null ) {
	var portletFragment = DOMUtil.findAncestorByClass(searchEl, "PORTLET-FRAGMENT");
	if (portletFragment != null) {
		var compId = portletFragment.parentNode.id;
		var href = eXo.env.server.portalBaseURL + "?portal:componentId=" + compId;
		href += "&portal:type=action&uicomponent=" + searchEl.id;
		href += "&op=Search";
		href += "&spaceName=" + ch;
		href += "&isFirstCharOfSpaceName=true";
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
UISpaceSearch.prototype.setEnterKey = function(event) {
	var e = event || window.event;
	var elementInput = e.srcElement || e.target;
	var keynum = e.keyCode || e.which;  
	if(keynum == 13) {
		eXo.social.space.UISpaceSearch.searchSpaceByName(elementInput);
	}						
};

/**
 *	Create id of elements with input component ID.
 *	
 *	@var componentId {String} Current component ID.
 *	@return void						
 */
UISpaceSearch.prototype.createId = function(componentId) {
	this.searchId = 'Search' + componentId;
};
/*===================================================================*/
if(!eXo.social) eXo.social = {};
if(!eXo.social.profile) eXo.social.space = {};
eXo.social.space.UISpaceSearch = new UISpaceSearch();