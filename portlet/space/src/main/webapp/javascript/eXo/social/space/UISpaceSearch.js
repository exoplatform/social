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
};

UISpaceSearch.prototype.onLoad = function(uicomponentId) {
	var DOMUtil = eXo.core.DOMUtil;
	var spaceSearch = document.getElementById(uicomponentId);
	var spaceSearchEl = DOMUtil.findDescendantById(spaceSearch, 'SpaceSearch');
	var defaultSpaceName = document.getElementById('defaultSpaceName').value;
	var defaultUIVal = "Space name";
	if (spaceSearchEl.value == defaultUIVal) spaceSearchEl.value = defaultSpaceName;
	(spaceSearchEl.value != defaultSpaceName) ? (spaceSearchEl.style.color = '#000000') : (spaceSearchEl.style.color = '#C7C7C7');
};

/**
 *	Change status some component when it is activated.
 *	
 *	@var filter {Object} Object is activated.
 *	@return void						
 */
UISpaceSearch.prototype.activeSearchText = function(elementId, componentId) {
	var DOMUtil = eXo.core.DOMUtil;
	var spaceSearch = document.getElementById(componentId);
	var searchBox = DOMUtil.findDescendantById(spaceSearch, elementId);
	var defaultValue = document.getElementById('defaultSpaceName').value;
	searchBox.style.color="#000000";
	searchBox.focus();
	if (searchBox.value == defaultValue) {
		searchBox.value='';
	}
};

/**
 *	Change status some component when it is blurred.
 *	
 *	@var filter {Object} Object is on blurred.
 *	@return void						
 */
UISpaceSearch.prototype.onBlurSearchText = function(elementId, componentId) {
	var DOMUtil = eXo.core.DOMUtil;
	var spaceSearch = document.getElementById(componentId);
	var searchBox = DOMUtil.findDescendantById(spaceSearch, elementId);
	var defaultValue = document.getElementById('defaultSpaceName').value;
	if ((searchBox.value.trim() == '') || (searchBox.value.trim() == defaultValue)) {
		searchBox.style.color="#C7C7C7";
		searchBox.value=defaultValue;
	} 
};

/**
 *	Call function when Enter Key is pressed in search text box.
 *	
 *	@var event {Object} Event.
 *	@return void						
 */
UISpaceSearch.prototype.setEnterKey = function(event, elementInputId) {
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

/*===================================================================*/
if(!eXo.social) eXo.social = {};
if(!eXo.social.profile) eXo.social.space = {};
eXo.social.space.UISpaceSearch = new UISpaceSearch();