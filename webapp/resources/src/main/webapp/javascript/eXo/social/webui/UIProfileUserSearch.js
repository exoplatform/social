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
	    
	    var profileSearch = document.getElementById(params.uicomponentId);
	    this.nameTextObj = (gj(profileSearch).find('#Search'))[0];
	    this.searchButton = (gj(profileSearch).find('#SearchButton'))[0];
	    this.posTextObj = (gj(profileSearch).find('#position'))[0];
	    this.skillTextObj = (gj(profileSearch).find('#skills'))[0];
	    this.onLoad();
   }
    
    /**
	 * When form load at the first time, init controls.
	 * TODO : remove. autosuggest must be implemenented by an ajax call! not by pushing all names in the client!!
	 */
   UIProfileUserSearch.prototype.onLoad = function() {
        var searchEl = this.nameTextObj;
        var posEl = this.posTextObj;
        var skillEl = this.skillTextObj;
        
        if( searchEl.value === this.defaultUserContact){
        	searchEl.style.color = BLUR_COLOR;
        }
        if( posEl.value === this.defaultPos){
        	posEl.style.color = BLUR_COLOR;
        }
        if( skillEl.value === this.defaultSkills){
        	skillEl.style.color = BLUR_COLOR;
        }
        
        this.initTextBox();
    }
   
	/**
	 * Set init value and event for control.
	 * @scope private.
	 */
	UIProfileUserSearch.prototype.initTextBox = function() {
        var nameEl = this.nameTextObj;
        var posEl = this.posTextObj;
        var skillEl = this.skillTextObj;
        var defaultUserContact = this.defaultUserContact;
		var defaultPos = this.defaultPos;
		var defaultSkills = this.defaultSkills;
		var uiProfileUserSearchObj = eXo.social.webui.UIProfileUserSearch;
		var suggestControlObj = eXo.social.webui.UIAutoSuggestControl;
        
        var searchBtn = this.searchButton;
        var uiProfileUserSearch = this;
        
		    // Turn off auto-complete attribute of text-box control
		    gj(nameEl).attr('autocomplete','off');
		    gj(posEl).attr('autocomplete','off');
		    gj(skillEl).attr('autocomplete','off');
    
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
        var defaultUserContact = this.defaultUserContact;
		var defaultPos = this.defaultPos;
		var defaultSkills = this.defaultSkills;
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
	 
	 window_.eXo = window_.eXo || {};
	 window_.eXo.social = window_.eXo.social || {};
	 window_.eXo.social.webui = window_.eXo.social.webui || {};
	 window_.eXo.social.webui.UIProfileUserSearch = UIProfileUserSearch;
})();
