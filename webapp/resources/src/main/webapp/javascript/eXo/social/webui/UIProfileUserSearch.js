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

		var COLOR = {
		  FOCUS : "#000000",
		  BLUR : "#C7C7C7"
		};
		
		var INPUT_ID = {
		  NAME : '#Search',
		  POSITION : '#position',
		  SKILLS : '#skills',
		  SEARCH : '#SearchButton'
		};
		
		var KEY = {
		  ENTER : 13
		};
		
		var DEFAULT_REST_INFO = {
      CONTEXT_NAME : 'rest-socialdemo',
      PATH : '/social/people/suggest.json'
    };
		
		function UIProfileUserSearch(params) {
		  this.defaultUserContact = params.defaultUserContact || null;
      this.defaultPos = params.defaultPos || null;
      this.defaultSkills = params.defaultSkills || null;
    
      var profileSearch = gj("#" + params.uicomponentId);
      this.nameTextObj = gj(INPUT_ID.NAME, profileSearch);
      this.posTextObj = gj(INPUT_ID.POSITION, profileSearch);
      this.skillTextObj = gj(INPUT_ID.SKILLS, profileSearch);
      this.searchButton = gj(INPUT_ID.SEARCH, profileSearch);

      this.initTextBox();
		};
    
   
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

	    var searchBtn = this.searchButton;
	    var uiProfileUserSearch = this;
	    
	    // Turn off auto-complete attribute of text-box control
	    gj(nameEl).attr('autocomplete','off');
	    gj(posEl).attr('autocomplete','off');
	    gj(skillEl).attr('autocomplete','off');
	
	    if(nameEl.val().trim() === this.defaultUserContact){
	      nameEl.css('color', COLOR.BLUR);
	    }
	    
	    if(posEl.val().trim() === this.defaultPos){
	      posEl.css('color', COLOR.BLUR);
	    }
	    
	    if(skillEl.val().trim() === this.defaultSkills){
	      skillEl.css('color', COLOR.BLUR);
	    }
	    
			posEl.focus(function() {
			  if (gj(this).val().trim() == defaultPos) {
			    gj(this).val('');              
			  }
			  gj(this).css('color', COLOR.FOCUS);
			});
			 
			skillEl.focus(function() {
			  if (gj(this).val() == defaultSkills) {
			    gj(this).val('');              
			  }
			  gj(this).css('color', COLOR.FOCUS);
			});
				        
		  posEl.blur(function() {
			  if (gj(this).val() && gj(this).val() != '') {
			    gj(this).css('color', COLOR.FOCUS);                               
			  } else {
			    gj(this).css('color', COLOR.BLUR);  
			    gj(this).val(defaultPos);
			  }
			});
			
	    skillEl.blur(function() {
			  if (gj(this).val() && gj(this).val() != '') {
			    gj(this).css('color', COLOR.FOCUS);                               
			  } else {
			    gj(this).css('color', COLOR.BLUR);  
			    gj(this).val(defaultSkills);
			  }
			});
			
			posEl.keydown(function(event) {
		    keyDownAction(event);
		  });
			
			skillEl.keydown(function(event) {
		    keyDownAction(event);
		  });

			gj(nameEl).autosuggest(buildURL(), {onSelect:callback, defaultVal:defaultUserContact});
			
			function callback() {
			  searchBtn.click();
			};

			function buildURL() {
        var restContext = eXo.social.webui.restContextName;
        var currentUser = eXo.social.webui.currentUserName;
        var typeOfRelation = eXo.social.webui.typeOfRelation;
			  restContext = (restContext) ? restContext : DEFAULT_REST_INFO.CONTEXT_NAME;
			  var restURL = "/" + restContext + DEFAULT_REST_INFO.PATH;
			  
			  restURL = restURL + '?nameToSearch=input_value';

				if (currentUser) {
				  restURL += "&currentUser=" + currentUser;
				}
			  
			  if (typeOfRelation) {
          restURL += "&typeOfRelation=" + typeOfRelation;
        }

        return restURL;
      };

			function keyDownAction(event) {
				  //var searchBtn = this.searchButton;
	        var e = event || window.event;
	        var textBox = e.srcElement || e.target;
	        var keynum = e.keyCode || e.which;  
	        if(keynum == KEY.ENTER) {
	          searchBtn.click();
	        } else {
	        }
        }
		 };
		        
		 window_.eXo.social.webui.UIProfileUserSearch = UIProfileUserSearch;
})();
