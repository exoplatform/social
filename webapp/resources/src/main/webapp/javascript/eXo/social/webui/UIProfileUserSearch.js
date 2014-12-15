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

(function($) {
var UIProfileUserSearch = { 
    uicomponentId : '',

		INPUT_ID : {
		  NAME : '#Search',
		  POSITION : '#position',
		  SKILLS : '#skills',
		  SEARCH : '#SearchButton'
		},
		KEY : {
		  ENTER : 13
		},
		DEFAULT_REST_INFO : {
      CONTEXT_NAME : 'rest-socialdemo',
      PATH : '/social/people/suggest.json'
    },
		init: function(params) {
		  var defaultUserContact = params.defaultUserContact || null;
      var defaultPos = params.defaultPos || null;
      var defaultSkills = params.defaultSkills || null;
      var restContextName = params.restContextName || null;
      var currentUserName = params.currentUserName || null;
      var typeOfRelation = params.typeOfRelation || null;
    
      this.uicomponentId = params.uicomponentId;
      var profileSearch = $("#" + params.uicomponentId);
      var nameEl = $(UIProfileUserSearch.INPUT_ID.NAME, profileSearch);
      var posEl = $(UIProfileUserSearch.INPUT_ID.POSITION, profileSearch);
      var skillEl = $(UIProfileUserSearch.INPUT_ID.SKILLS, profileSearch);
      var searchBtn = $(UIProfileUserSearch.INPUT_ID.SEARCH, profileSearch);
      
	    // Turn off auto-complete attribute of text-box control
	    $(nameEl).attr('autocomplete','off');
	    $(posEl).attr('autocomplete','off');
	    $(skillEl).attr('autocomplete','off');
	

		  posEl.keydown(function(event) {
		    keyDownAction(event);
		  });
			
			skillEl.keydown(function(event) {
		    keyDownAction(event);
		  });

			$(nameEl).autosuggest(buildURL(), {onSelect:function(){searchBtn.click();}, defaultVal:defaultUserContact});
			
			function buildURL() {
			  restContextName = (restContextName) ? restContextName : UIProfileUserSearch.DEFAULT_REST_INFO.CONTEXT_NAME;
			  var restURL = "/" + restContextName + UIProfileUserSearch.DEFAULT_REST_INFO.PATH;
			  
			  restURL = restURL + '?nameToSearch=input_value';

				if (currentUserName) {
				  restURL += "&currentUser=" + currentUserName;
				}
			  
			  if (typeOfRelation) {
          restURL += "&typeOfRelation=" + typeOfRelation;
        }

        return restURL;
      };

			function keyDownAction(event) {
	        var e = event || window.event;
	        var textBox = e.srcElement || e.target;
	        var keynum = e.keyCode || e.which;  
	        if(keynum == UIProfileUserSearch.KEY.ENTER) {
	          searchBtn.click();
	          event.preventDefault();
	        } else {
	        }
      }
      //
      UIProfileUserSearch.resizeForm();
    },

    resizeForm : function() {
      var parent = $("#" + UIProfileUserSearch.uicomponentId);
      var label = parent.find('label:[for=Search]');
      var searchBtn = $(UIProfileUserSearch.INPUT_ID.SEARCH, parent);
      var staticSize = label.outerWidth() + searchBtn.outerWidth() + 64;
      var inputSize = (parent.width() - staticSize) / 3;
      parent.find('input[type=text]').css( {'width': inputSize + 'px', 'minWidth' : '80px'});
    }
};

$(window).on('resize', UIProfileUserSearch.resizeForm );

return UIProfileUserSearch;
})($);