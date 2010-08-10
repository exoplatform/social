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
 * Space member suggestion.
 * @class
 * @scope public
 */
function UISpaceMemberSuggest() {
   /**
    * User name search text box object.
    * @scope private.
    */
   this.userNameTextObj = null;
   
   /**
    * All user name for suggesting.
    * @scope private.
    */
   this.allUserName = null;
};

/**
 * When form load at the first time, init controls.
 */
UISpaceMemberSuggest.prototype.onLoad = function(allUserNames) {
	var DOMUtil = eXo.core.DOMUtil;
	var suggestEl = document.getElementById('user');

	this.userNameTextObj = suggestEl;
	
	this.setAllUserName(allUserNames);
	this.initTextBox();
};

/**
 * Set init value and event for control.
 * @scope private.
 */
UISpaceMemberSuggest.prototype.initTextBox = function() {
	var nameEl = this.userNameTextObj;
	var DOMUtil = eXo.core.DOMUtil;
	var UIForm = eXo.webui.UIForm;
	var suggestElId = 'user';
	var UISpaceMemberSuggestObj = eXo.social.webui.UISpaceMemberSuggest;
	var suggestControlObj = eXo.social.webui.UIAutoSuggestMultiValueControl;
	
	// Turn off auto-complete attribute of text-box control
	nameEl.setAttribute('autocomplete','off');
	
	// Add keydown event for control
	nameEl.onkeydown = function(event) {
		var e = event || window.event;
		var textBox = e.srcElement || e.target;
		var keynum = e.keyCode || e.which;  
		
		if(keynum == 13) {
			suggestControlObj.hideSuggestions();
			return;
//			UISpaceMemberSuggestObj.submitSearchForm(textBox);
		} else if (textBox.id == suggestElId) {
			// Other keys (up and down key)
			suggestControlObj.handleKeyDown(e);
		} else {
			// ignore
		}
	}
	
	nameEl.onblur = function(event) {
		suggestControlObj.hideSuggestions();
	}
	
	// Add suggestion capability for user contact name search text-box.
	suggestControlObj.load(nameEl, UISpaceMemberSuggestObj);
}

/**
 * Submit the search form.
 * @scope private.
 */
UISpaceMemberSuggest.prototype.setValueIntoTextBox = function(suggestEl /*input text box*/) {
		
}

/**
 * Submit the search form.
 * @scope private.
 */
UISpaceMemberSuggest.prototype.submitSearchForm = function(suggestEl /*input text box*/) {
	var DOMUtil = eXo.core.DOMUtil;
	var UIForm = eXo.webui.UIForm;
	var searchForm = DOMUtil.findAncestorByClass(suggestEl, 'UIForm');
	if (searchForm != null ) UIForm.submitForm(searchForm.id, 'Search', true);
}

/**
 * Set all contact name to allUserName variable.
 * @scope private.
 */
UISpaceMemberSuggest.prototype.setAllUserName = function(allName) {
	var allUserNames = allName.substring(1, allName.length-1);
	var allSN = allUserNames.split(',');
	var allNames = [];
	for (var i=0; i < allSN.length; i++) {
		(function(idx) {
			allNames.push(allSN[idx].trim());
		})(i);
	}
	this.allUserName = allNames;
};

/**
 * Request suggestions for the given autosuggest control. 
 * @scope protected
 * @param oAutoSuggestControl The autosuggest control to provide suggestions for.
 */
UISpaceMemberSuggest.prototype.requestSuggestions = function (oAutoSuggestControl /*:AutoSuggestControl*/) {
    var aSuggestions = [];
//    var sTextboxValue = oAutoSuggestControl.textbox.value;
    var sTextboxValue = oAutoSuggestControl.storeText;
    if (sTextboxValue.length > 0){
    
        //convert value in textbox to lowercase
        var sTextboxValueLC = sTextboxValue.toLowerCase();
        
        //search for matching states
        for (var i=0; i < this.allUserName.length; i++) { 

            //convert state name to lowercase
            var sStateLC = this.allUserName[i].toLowerCase();
            
            //compare the lowercase versions for case-insensitive comparison
            if (sStateLC.indexOf(sTextboxValueLC) == 0) {
                //add a suggestion using what's already in the textbox to begin it                
            	aSuggestions.push(this.allUserName[i]);

                // Check if user type like the last suggestion in drop-down list.
                if ((this.allUserName[i].substring(sTextboxValue.length) == "") && (aSuggestions.length == 1)) {
                	oAutoSuggestControl.hideSuggestions();
                	return;
                }
            } 
        }
    }
    
    //provide suggestions to the control
    oAutoSuggestControl.autosuggest(aSuggestions);
};
/*===================================================================*/
if(!eXo.social) eXo.social = {};
if(!eXo.social.webui) eXo.social.webui = {};
eXo.social.webui.UISpaceMemberSuggest = new UISpaceMemberSuggest();
