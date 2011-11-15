/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

(function() {
    var window_ = this;
    var DOMUtil = eXo.core.DOMUtil;
    var Util = eXo.social.Util;    
    var FOCUS_COLOR = "#000000",
        BLUR_COLOR = "#C7C7C7";
    
    function UIProfileUserSearch(params) {
        this.init(params);
    };
    
    UIProfileUserSearch.prototype.init = function(params) {
	    this.defaultUserContact = params.defaultUserContact || null;
	    this.defaultPos = params.defaultPos || null;
	    this.defaultSkills = params.defaultSkills || null;
	    this.defaultGender = params.defaultGender || null;
	    this.defaultMale = params.defaultMale || null;
	    this.defaultFeMale = params.defaultFeMale || null;
	    
	    var profileSearch = document.getElementById(params.uicomponentId);
	    this.nameTextObj = DOMUtil.findDescendantById(profileSearch, 'Search');
	    this.searchButton = DOMUtil.findDescendantById(profileSearch, 'SearchButton');
	    this.posTextObj = DOMUtil.findDescendantById(profileSearch, 'position');
	    this.skillTextObj = DOMUtil.findDescendantById(profileSearch, 'skills');
	    this.genderSelObj = DOMUtil.findDescendantsByTagName(profileSearch, 'select');
	    this.filterBlock = DOMUtil.findDescendantById(profileSearch, 'Filter');
	    this.moreSearchEl = DOMUtil.findDescendantById(profileSearch, 'MoreSearch');
	    this.hideMoreSearchEl = DOMUtil.findDescendantById(profileSearch,'HideMoreSearch');
	    this.onLoad();
   }
    
    /**
	 * When form load at the first time, init controls.
	 * TODO : remove. autosuggest must be implemenented by an ajax call! not by pushing all names in the client!!
	 */
   UIProfileUserSearch.prototype.onLoad = function(uicomponentId) {
        var searchEl = this.nameTextObj;
        var posEl = this.posTextObj;
        var skillEl = this.skillTextObj;
        var genderEl = this.genderSelObj;
        
        searchEl.style.color = BLUR_COLOR;
        posEl.style.color = BLUR_COLOR;
        skillEl.style.color = BLUR_COLOR;
        
        this.initTextBox();
        this.initToggleFilter();
    }
   
	/**
	 * Set init value and event for control.
	 * @scope private.
	 */
	UIProfileUserSearch.prototype.initTextBox = function() {
        var nameEl = this.nameTextObj;
        var posEl = this.posTextObj;
        var skillEl = this.skillTextObj;
        var genderEl = this.genderSelObj;
        var defaultUserContact = this.defaultUserContact;
		var defaultPos = this.defaultPos;
		var defaultSkills = this.defaultSkills;
		var defaultGender = this.defaultGender;
		var uiProfileUserSearchObj = eXo.social.webui.UIProfileUserSearch;
		var suggestControlObj = eXo.social.webui.UIAutoSuggestControl;
        
        var searchBtn = this.searchButton;
        var uiProfileUserSearch = this;
        
	    // Turn off auto-complete attribute of text-box control
	    nameEl.setAttribute('autocomplete','off');
	    posEl.setAttribute('autocomplete','off');
	    skillEl.setAttribute('autocomplete','off');
    
        Util.addEventListener(this.nameTextObj, 'focus', function() {
          if (this.value == defaultUserContact) {
            this.value = '';              
          }
          this.style.color=FOCUS_COLOR;
        }, false);

		Util.addEventListener(this.nameTextObj, 'blur', function() {
		  if (this.value && this.value != '') {
		    this.style.color=FOCUS_COLOR;                               
		  } else {
		    this.style.color=BLUR_COLOR;
		    this.value = defaultUserContact;
		  }
		}, false);
	
		Util.addEventListener(this.nameTextObj, 'keydown', function(event) {
	      uiProfileUserSearch.keyDownAction(event);
	    }, false);
		 
		Util.addEventListener(this.posTextObj, 'focus', function() {
		  if (this.value == defaultPos) {
		    this.value = '';              
		  }
		  this.style.color=FOCUS_COLOR;
		}, false);
		 
		Util.addEventListener(this.skillTextObj, 'focus', function() {
		  if (this.value == defaultSkills) {
		    this.value = '';              
		  }
		  this.style.color=FOCUS_COLOR;
		}, false);
			        
	    Util.addEventListener(this.posTextObj, 'blur', function() {
		  if (this.value && this.value != '') {
		    this.style.color=FOCUS_COLOR;                               
		  } else {
		    this.style.color=BLUR_COLOR;
		    this.value = defaultPos;
		  }
		}, false);
		
    	Util.addEventListener(this.skillTextObj, 'blur', function() {
		  if (this.value && this.value != '') {
		    this.style.color=FOCUS_COLOR;                               
		  } else {
		    this.style.color=BLUR_COLOR;
		    this.value = defaultSkills;
		  }
		}, false);
		
		Util.addEventListener(this.posTextObj, 'keydown', function(event) {
	      uiProfileUserSearch.keyDownAction(event);
	    }, false);
		
		Util.addEventListener(this.skillTextObj, 'keydown', function(event) {
	      uiProfileUserSearch.keyDownAction(event);
	    }, false);
			        
		suggestControlObj.load(nameEl, uiProfileUserSearchObj);
	 
	 }
	        
	 UIProfileUserSearch.prototype.keyDownAction = function(event) {
	    var nameEl = this.nameTextObj;
        var posEl = this.posTextObj;
        var skillEl = this.skillTextObj;
        var genderEl = this.genderSelObj;
        var defaultUserContact = this.defaultUserContact;
		var defaultPos = this.defaultPos;
		var defaultSkills = this.defaultSkills;
		var defaultGender = this.defaultGender;
		var uiProfileUserSearchObj = eXo.social.webui.UIProfileUserSearch;
		var suggestControlObj = eXo.social.webui.UIAutoSuggestControl;
        
        var searchBtn = this.searchButton;
	    var e = event || window.event;
        var textBox = e.srcElement || e.target;
        var keynum = e.keyCode || e.which;  
        if(keynum == 13) {
          suggestControlObj.hideSuggestions();
          searchBtn.onclick();
	    } else if (textBox.id == this.nameTextObj.id) {
	      // Other keys (up and down key)
	      suggestControlObj.handleKeyDown(e);
	    } else {
		}
	 }
	 
	 /**
	  *  Display or not for advance search block.
	  *      
	  *  @scope private.     
	  */
	 UIProfileUserSearch.prototype.initToggleFilter = function() {
	     var filterBlock = this.filterBlock;
	     var moreSearchEl = this.moreSearchEl;
	     var hideMoreSearchEl = this.hideMoreSearchEl;
	     //Util.hideElement(
	     Util.addEventListener(moreSearchEl, 'click', function() {
	       Util.showElement(filterBlock.id);
	       Util.showElement(hideMoreSearchEl.id);
	       Util.hideElement(moreSearchEl.id);
	     }, false);
	     
	     Util.addEventListener(hideMoreSearchEl, 'click', function() {
	       Util.hideElement(filterBlock.id);
	       Util.hideElement(hideMoreSearchEl.id);
	       Util.showElement(moreSearchEl.id);
	     }, false);
	 }
		        
	 window_.eXo = window_.eXo || {};
	 window_.eXo.social = window_.eXo.social || {};
	 window_.eXo.social.webui = window_.eXo.social.webui || {};
	 window_.eXo.social.webui.UIProfileUserSearch = UIProfileUserSearch;
})();
