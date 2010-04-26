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
 * UISpaceMenuPortlet class
 * - Show/ hide UISpaceMenuPortlet
 */
function UISpaceMenuPortlet() {
	
}

UISpaceMenuPortlet.init = function() {
	var Util = eXo.social.Util,
		shown = true,
		uiSpaceMenuPortlet = document.getElementById('UISpaceMenuPortlet'),
		uiContainerMenu = document.getElementById('UIContainer-Menu'),
		menuTdContainer = uiContainerMenu.parentNode,
		tdToggleElement = document.createElement('td');
		
	tdToggleElement.setAttribute('id', 'ToggleElement');
	tdToggleElement.setAttribute('class', 'ToggleElement');
	tdToggleElement.setAttribute('className', 'ToggleElement');
	
	Util.insertAfter(tdToggleElement, menuTdContainer);
	tdToggleElement.innerHTML = '<div class="CloseSpaceMenuPortlet"><span>&nbsp;</span></div>';
	
	//set event handler
	Util.addEventListener(tdToggleElement, 'click', function() {
		if (shown) { //hide
			uiSpaceMenuPortlet.style.display='none';
			menuTdContainer.style.width='0px';
			shown = false;
			tdToggleElement.innerHTML = '<div class="OpenSpaceMenuPortlet"><span>&nbsp;</span></div>';
		} else { //show
			uiSpaceMenuPortlet.style.display='block';
			menuTdContainer.style.width='162px';
			shown = true;
			tdToggleElement.innerHTML = '<div class="CloseSpaceMenuPortlet"><span>&nbsp;</span></div>';
		}
	});
	
}

eXo.social = eXo.social || {};
eXo.social.webui = eXo.social.webui || {};
eXo.social.webui.UISpaceMenuPortlet = UISpaceMenuPortlet;