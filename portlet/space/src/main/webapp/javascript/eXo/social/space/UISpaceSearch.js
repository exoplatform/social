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
	this.inputTextBoxObj = null;
};

UISpaceSearch.prototype.onLoad = function(uicomponentId) {
	var DOMUtil = eXo.core.DOMUtil;
	var spaceSearch = document.getElementById(uicomponentId);
	var spaceSearchEl = DOMUtil.findDescendantById(spaceSearch, 'SpaceSearch');
	var defaultSpaceName = document.getElementById('defaultSpaceName').value;
	var defaultUIVal = "Space name";
	if (spaceSearchEl.value == defaultUIVal) spaceSearchEl.value = defaultSpaceName;
	(spaceSearchEl.value != defaultSpaceName) ? (spaceSearchEl.style.color = '#000000') : (spaceSearchEl.style.color = '#C7C7C7');
	this.inputTextBoxObj = spaceSearchEl;
	this.initTextBox();
};

UISpaceSearch.prototype.initTextBox = function() {
	var searchEl = this.inputTextBoxObj;
	var DOMUtil = eXo.core.DOMUtil;
	var UIForm = eXo.webui.UIForm;
	
	searchEl.onfocus = function() {
		var defaultValue = document.getElementById('defaultSpaceName').value;
		searchEl.style.color="#000000";
		searchEl.focus();
		if (searchEl.value == defaultValue) {
			searchEl.value='';
		}
	}
	
	searchEl.onblur = function() {
		var defaultValue = document.getElementById('defaultSpaceName').value;
		if ((searchEl.value.trim() == '') || (searchEl.value.trim() == defaultValue)) {
			searchEl.style.color="#C7C7C7";
			searchEl.value=defaultValue;
		} 
	}
	
	searchEl.onkeydown = function(event) {
		var e = event || window.event;
		var keynum = e.keyCode || e.which;  
		var searchForm = DOMUtil.findAncestorByClass(searchEl, 'UIForm');
		  
		if(keynum == 13) {
			if (searchForm != null ) UIForm.submitForm(searchForm.id, 'Search', true);
		}	
	}
}

/*===================================================================*/
if(!eXo.social) eXo.social = {};
if(!eXo.social.space) eXo.social.space = {};
eXo.social.space.UISpaceSearch = new UISpaceSearch();