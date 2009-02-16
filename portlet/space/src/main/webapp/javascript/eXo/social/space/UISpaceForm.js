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
 * @author <a href="mailto:tungcnw@gmail.com">dang.tung</a>
 */

eXo.social = eXo.social || {};
eXo.social.space = eXo.social.space || {};

function UISpaceForm() {};

UISpaceForm.prototype.moreOptions = function(selectedEl) {
	var DOMUtil = eXo.core.DOMUtil;
	var buttonHide = DOMUtil.findNextElementByTagName(selectedEl,'div');
	var optionWraps = DOMUtil.findNextElementByTagName(buttonHide,'div');
	if(buttonHide.style.display == 'none') {
		selectedEl.style.display = 'none';
		buttonHide.style.display = 'block';
		optionWraps.style.display = 'block';
		var firstOption = DOMUtil.findFirstDescendantByClass(optionWraps, 'div', 'SpaceOptionRadio');
		firstOption.className = 'SpaceOptionRadio selected';
		DOMUtil.findFirstDescendantByClass(firstOption,'input','radio').checked = 'checked';
	}
}

UISpaceForm.prototype.hideMoreOptions = function(selectedEl) {
	var DOMUtil = eXo.core.DOMUtil;
	var buttonMore = DOMUtil.findPreviousElementByTagName(selectedEl,'div');
	var optionsWrap = DOMUtil.findNextElementByTagName(selectedEl,'div');
	if(buttonMore.style.display == 'none') {
		selectedEl.style.display = 'none';
		buttonMore.style.display = 'block';
		optionsWrap.style.display = 'none';
		var options = DOMUtil.findDescendantsByClass(optionsWrap,'div','SpaceOptionRadio');
		for(i=0; i < options.length; i++) {
			var selectedOption = options[i];
			if(selectedOption.className.match('selected')) {
				selectedOption.className = 'SpaceOptionRadio';
				DOMUtil.findFirstDescendantByClass(selectedOption,'input','radio').checked = '';
				break;
			}
		}
	}
}

UISpaceForm.prototype.checkRadio = function(selectedEl) {
	var DOMUtil = eXo.core.DOMUtil;
	var input = DOMUtil.findFirstDescendantByClass(selectedEl,'input','radio');
	var parentWrap = selectedEl.parentNode;
	var optionsWrap = DOMUtil.findDescendantsByClass(parentWrap,'div','SpaceOptionRadio');
	for(i=0; i < optionsWrap.length; i++) {
		var selectedOption = optionsWrap[i];
		if(selectedOption.className.match('selected')) {
			selectedOption.className = 'SpaceOptionRadio';
			DOMUtil.findFirstDescendantByClass(selectedOption,'input','radio').checked = '';
			break;
		}
	}
	if(selectedEl.className == 'SpaceOptionRadio') {
		selectedEl.className = 'SpaceOptionRadio selected';
		input.checked = 'checked';
	}
}

eXo.social.space.UISpaceForm = new UISpaceForm();