/**
 * Copyright (C) 2009 eXo Platform SAS.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
function UISpaceAppNameEdition() {};

/**
 * Rename space application label.
 */
UISpaceAppNameEdition.prototype.renameAppLabel = function(e) {
		var backupElement = eXo.social.webui.UISpaceAppNameEdition.backupElement;
		var isValidInput = true;
		if(!e){
			e = window.event;
		}
		var keyNum = e.keyCode;
		
		//If user presses on ENTER button, then rename the space application name label
		if(keyNum == 13){
			var inputElement = eXo.core.Browser.getEventSource(e);
			var newSpaceAppName = inputElement.value;
						
			var DOMUtil = eXo.core.DOMUtil;
			var portletFrag = DOMUtil.findAncestorByClass(inputElement, "PORTLET-FRAGMENT");
			var compId = portletFrag.parentNode.id;
			
			//Change the space application name label
			var spanElement = document.createElement("span");
			spanElement.innerHTML = newSpaceAppName;
			inputElement.parentNode.replaceChild(spanElement, inputElement);
			
			//Send request to server to update space application name.
			var href = eXo.env.server.portalBaseURL + "?portal:componentId=" + compId;
			href += "&portal:type=action";
			href += "&portal:isSecure=false";
			href += "&uicomponent=UISpaceMenuPortlet";
			href += "&op=RenameSpaceAppName";
			href += "&newSpaceAppName=" + encodeURIComponent(newSpaceAppName);
			window.location = href;
		}
		//If user presses on the ESCAPE key reset the original space application name.
		else if(keyNum == 27){
			var inputElement = eXo.core.Browser.getEventSource(e);
			if(eXo.social.webui.UISpaceAppNameEdition.backupElement) {
 				inputElement.parentNode.replaceChild(eXo.social.webui.UISpaceAppNameEdition.backupElement, inputElement);
 				eXo.social.webui.UISpaceAppNameEdition.backupElement = null;
			}
		}
};

/**
 * Change label into editable status for input new space application label. 
 * @param selectedElement Label is edited.
 * @param nodeIndex 
 * @param currentContent
 */
UISpaceAppNameEdition.prototype.showEditLabelInput = function(selectedElement, currentContent){
		eXo.social.webui.UISpaceAppNameEdition.backupElement = selectedElement;
		var prNode = selectedElement.parentNode;
		var selectedElementId = selectedElement.id;
		
		var inputElement = document.createElement("input");
		inputElement.type = "text";
		inputElement.id = selectedElementId;
		inputElement.name = selectedElementId; // To store old value
		inputElement.value = currentContent;
		inputElement.style.border = "1px solid #b7b7b7";
		inputElement.style.width = "95px";
		inputElement.onkeypress = eXo.social.webui.UISpaceAppNameEdition.renameAppLabel;
		inputElement.setAttribute('maxLength', 50);
		inputElement.onblur = function() {
			prNode.replaceChild(eXo.social.webui.UISpaceAppNameEdition.backupElement, inputElement);
		};
		
		prNode.replaceChild(inputElement, selectedElement);
		inputElement.focus();
};

if(!eXo.social) eXo.social = {};
if(!eXo.social.webui) eXo.social.webui = {};
eXo.social.webui.UISpaceAppNameEdition = new UISpaceAppNameEdition();