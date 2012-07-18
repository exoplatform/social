if(!eXo.social) eXo.social = {};
if(!eXo.social.webui) eXo.social.webui = {};

/**
 * An autosuggest textbox control.
 * @class
 * @scope public
 */
function UIAutoSuggestMultiValueControl() {
};

/**
 * Constant values.
 */
UIAutoSuggestMultiValueControl.config = {
  DEFAULT_REST_CONTEXT_NAME : 'rest-socialdemo',
	PEOPLE_REST_PATH : "/social/people/suggest.json?nameToSearch="
}

/**
 * Load the initialization for textbox control.
 * @scope private
 */
UIAutoSuggestMultiValueControl.prototype.load = function(oTextbox /*:HTMLInputElement*/, 
                                             oProvider /*:SuggestionProvider*/) {
    
    /**
     * The currently selected suggestions.
     * @scope private
     */   
    this.cur /*:int*/ = -1;

    /**
     * The dropdown list layer.
     * @scope private
     */
    this.layer = null;
    
    /**
     * Suggestion provider for the autosuggest feature.
     * @scope private.
     */
    this.provider /*:SuggestionProvider*/ = oProvider;
    
    /**
     * The textbox to capture.
     * @scope private
     */
    this.textbox /*:HTMLInputElement*/ = oTextbox;
    
    /**
     * Store the time out return value form setTimeout function.
     * @scope private
     */
    this.timeout = null;
    
    this.storeText = null;
    
    this.storeInputText = null;
    
    //initialize the control
    this.init();
    
}

/**
 * Creates the dropdown layer to display multiple suggestions.
 * @scope private
 */
UIAutoSuggestMultiValueControl.prototype.createDropDown = function () {

    var oThis = this;

    //create the layer and assign styles
    this.layer = document.createElement("div");
    this.layer.id = "UIAutoSuggestMultiValueControl";
    this.layer.className = "suggestions";
    this.layer.style.visibility = "hidden";
    this.layer.style.width = this.textbox.offsetWidth;
    
    //when the user clicks on the a suggestion, get the text (innerHTML)
    //and place it into a textbox
    this.layer.onkeydown =
    this.layer.onmousedown =
    this.layer.onkeyup =
    this.layer.onmouseup =
    this.layer.onfocus =
    this.layer.onmouseover = function (oEvent) {
        oEvent = oEvent || window.event;
        oTarget = oEvent.target || oEvent.srcElement;
        var currentVal = oThis.textbox.value;
        var inputUserName = currentVal.substr(0, currentVal.lastIndexOf(","));
        if (oEvent.type == "mousedown" || oEvent.type == "keydown") {
        	  if (currentVal.lastIndexOf(",") > 0) {
              oThis.textbox.value = inputUserName + ", " + oTarget.firstChild.nodeValue;
        	  } else {
        	  	oThis.textbox.value = oTarget.firstChild.nodeValue;
        	  }
            oThis.hideSuggestions();
//            oThis.provider.submitSearchForm(oThis.textbox);
        } else if (oEvent.type == "mouseover" || oEvent.type == "focus") {
            oThis.highlightSuggestion(oTarget);
        } else {
            oThis.textbox.focus();
        }
    };
    
    document.body.appendChild(this.layer);
};

/**
 * Gets the left coordinate of the textbox.
 * @scope private
 * @return The left coordinate of the textbox in pixels.
 */
UIAutoSuggestMultiValueControl.prototype.getLeft = function () /*:int*/ {

    var oNode = this.textbox;
    var iLeft = 0;
    var obj = null;
    while(oNode.tagName != "BODY") {
        iLeft += oNode.offsetLeft;
        if (!(oNode = oNode.offsetParent)) return iLeft;
    }
    
    return iLeft;
};

/**
 * Gets the top coordinate of the textbox.
 * @scope private
 * @return The top coordinate of the textbox in pixels.
 */
UIAutoSuggestMultiValueControl.prototype.getTop = function () /*:int*/ {
    var oNode = this.textbox;
    var iTop = 0;
    var obj1 = null;
    while(oNode.tagName != "BODY") {
        iTop += oNode.offsetTop;
        if (!(oNode = oNode.offsetParent)) return iTop;
    }
    
    return iTop;
};

/**
 * Handles three keydown events.
 * @scope private
 * @param oEvent The event object for the keydown event.
 */
UIAutoSuggestMultiValueControl.prototype.handleKeyDown = function (oEvent /*:Event*/) {
	if (this.layer.style.visibility == "visible") { //Applied on suggestion drop-down only.
	    switch(oEvent.keyCode || oEvent.which) {
	        case 38: //up arrow
	            this.previousSuggestion();
	            break;
	        case 40: //down arrow
	            this.nextSuggestion();
	            break;
	        case 13: //Enter
	            var currentValue = this.textbox.value;
	            this.textbox.value = currentValue + ", ";
	            this.textbox.onfocus();
	            return;
	        case 27: //Esc 
	        	  this.hideSuggestions();
	        	  break;
	    }
	}
};

/**
 * Handles keyup events.
 * @scope private
 * @param oEvent The event object for the keyup event.
 */
UIAutoSuggestMultiValueControl.prototype.handleKeyUp = function (oEvent /*:Event*/) {
    var iKeyCode = oEvent.keyCode || oEvent.which;
    var el = oEvent.target || oEvent.srcElement;
	  var isInputTag = (el.tagName.toLowerCase() == 'input');
	  var currentVal = el.value.trim();
	  var oThis = eXo.social.webui.UIAutoSuggestMultiValueControl;
	  
	  if (currentVal == '') {
	  	oThis.hideSuggestions();
	  	return;
	  }
	  
	  var currentValLength = (currentVal ? currentVal.length : 0);
	  
    //for backspace (8) and delete (46), shows suggestions without typeahead
    if (iKeyCode == 8 || iKeyCode == 46) {
    	if (isInputTag) {
    		this.storeInputText = currentVal;
    		this.storeText = currentVal.substr(currentVal.lastIndexOf(",") + 1, currentValLength - 1).trim();
    	}

      oThis.resetAutoSuggestList();
        
    } else if (iKeyCode == 32 || iKeyCode == 44) {
    	this.storeText = "";
    	this.storeInputText = currentVal;
    //make sure not to interfere with non-character keys
    } else if (iKeyCode < 32 || (iKeyCode >= 33 && iKeyCode < 46) || (iKeyCode >= 112 && iKeyCode <= 123)) {
        //ignore
    } else {
    	if (isInputTag) {
    		this.storeText = currentVal.substr(currentVal.lastIndexOf(",") + 1, currentValLength).trim();
	    	this.storeInputText = currentVal;
    	}
    	
    	oThis.resetAutoSuggestList();
    }
};


///////////////// Request for new data each time input by ajax ///////////////////

/**
 * Resets the autosuggest list.
 */
UIAutoSuggestMultiValueControl.prototype.resetAutoSuggestList = function() {
	  var oThis = eXo.social.webui.UIAutoSuggestMultiValueControl;
  	if (oThis.timeout) clearTimeout(oThis.timeout);
  	oThis.timeout = setTimeout(function(){
  	  oThis.requestDataForAutoSuggest();
  	  clearTimeout(oThis.timeout);
  	}, 100);
}

/**
 * Sends request to get data from server to add to auto suggest control.
 */
UIAutoSuggestMultiValueControl.prototype.requestDataForAutoSuggest = function() {
	var CONFIG = UIAutoSuggestMultiValueControl.config;
	var inputString = this.textbox.value.trim();
	var restContext = eXo.social.webui.restContextName;
	var currentUser = eXo.social.webui.currentUserName;
	var typeOfRelation = eXo.social.webui.typeOfRelation;
	var spaceURL = eXo.social.webui.spaceURL;
	var typeOfSuggest = eXo.social.webui.typeOfSuggest;
	var portalName = eXo.social.webui.portalName;
  var inputStringLen = (inputString ? inputString.length : 0);
  
  // TODO Need more check
  for (var idx = 0; idx < inputStringLen; idx++) {
  	var currentCharKeyCode = inputString.charCodeAt(idx);
  	if (currentCharKeyCode == 44) {
  		// ignore comma for putting as saperating of multi suggestion
  	} else if (currentCharKeyCode < 32 || (currentCharKeyCode >= 33 && currentCharKeyCode < 46) || 
  	          (currentCharKeyCode >= 123)) {
  		this.hideSuggestions();
  		return;
  	}
  }
  
  if ((typeOfSuggest == 'user_to_invite') && (inputString.indexOf(',') != -1)) {
    inputString = inputString.substr(inputString.lastIndexOf(',') + 1, inputStringLen).trim();
  }
  
	restContext = (restContext) ? restContext : CONFIG.DEFAULT_REST_CONTEXT_NAME;
	
	var restURL = "/" + restContext + CONFIG.PEOPLE_REST_PATH + inputString ;
	
	if (!isNull(currentUser)) {
		restURL += "&currentUser=" + currentUser;
	}
	
	if (!isNull(typeOfRelation)) {
		restURL += "&typeOfRelation=" + typeOfRelation;
	}
	
	if (!isNull(spaceURL)) {
		restURL += "&spaceURL=" + spaceURL;
	}
	
	if ((inputStringLen == 0) || (inputString == ' ')) {
  	this.hideSuggestions();
  	return;
	}
	
	this.makeRequest(restURL, true, this.resetList);
	
	function isNull(str) {
		var obj = null;
		try {
			obj = new Function( "return " + str )();
			return (obj == null);
		} catch(e) {
			return false;
		}
    return false;
	}
}

/**
 * Gets return data and resets the name list to suggest control.
 */
UIAutoSuggestMultiValueControl.prototype.resetList = function(resp) {
  var names = gj.parseJSON(resp.responseText).names;
  
  var namesLen = (names ? names.length : 0);
  var oThis = eXo.social.webui.UIAutoSuggestMultiValueControl;
  if (namesLen == 0) {
	  oThis.hideSuggestions();
	  return;
  }
  oThis.showSuggestions(names);
}

/**
 * Posts rest request to server.
 */
UIAutoSuggestMultiValueControl.prototype.makeRequest = function(url, async, callback) {
  gj.ajax({
    type: "get",
    url: url,
    async: async,
    cache: false,
    success: function(data, status, jqXHR) {
      if (callback) {
        callback(jqXHR);
      }
    }
  });
}

////////////////////////////End of request data for autosuggest/////////////////////////////


/**
 * Hides the suggestion dropdown.
 * @scope private
 */
UIAutoSuggestMultiValueControl.prototype.hideSuggestions = function () {
  	var uiAutosuggestion = document.getElementById("UIAutoSuggestMultiValueControl");
  	if (uiAutosuggestion != null) {
      uiAutosuggestion.style.visibility = "hidden";
      return;
  	}
};

/**
 * Highlights the given node in the suggestions dropdown.
 * @scope private
 * @param oSuggestionNode The node representing a suggestion in the dropdown.
 */
UIAutoSuggestMultiValueControl.prototype.highlightSuggestion = function (oSuggestionNode) {
	  var childNodes = this.layer.childNodes;
    var childNodesLen = (childNodes ? childNodes.length : 0);
    for (var i=0; i < childNodesLen; i++) {
        var oNode = childNodes[i];
        if (oNode == oSuggestionNode) {
            oNode.className = "current"
        } else if (oNode.className == "current") {
            oNode.className = "";
        }
    }
};

/**
 * Initializes the textbox with event handlers for
 * auto suggest functionality.
 * @scope private
 */
UIAutoSuggestMultiValueControl.prototype.init = function () {

    //save a reference to this object
    var oThis = this;
    
    //assign the onkeyup event handler
    this.textbox.onkeyup = function (oEvent) {
    
        //check for the proper location of the event object
        if (!oEvent) {
            oEvent = window.event;
        }    
        
        //call the handleKeyUp() method with the event object
        oThis.handleKeyUp(oEvent);
    };
    
    var uiAutosuggestion = document.getElementById("UIAutoSuggestMultiValueControl");
    if (uiAutosuggestion) {
    	if (this.timeout) clearTimeout(this.timeout);
    	this.layer = uiAutosuggestion;
    	uiAutosuggestion.style.visibility = "hidden";
    } else {
	    //create the suggestions dropdown
	    this.createDropDown();
    }
};

/**
 * Highlights the next suggestion in the dropdown and
 * places the suggestion into the textbox. (down arrow)
 * @scope private
 */
UIAutoSuggestMultiValueControl.prototype.nextSuggestion = function () {
    var cSuggestionNodes = this.layer.childNodes;
    var cSuggestionNodesLen = (cSuggestionNodes ? cSuggestionNodes.length : 0);
    if (this.cur == cSuggestionNodesLen) this.cur = -1;
    ++this.cur;
    var currentVal = this.textbox.value;
    if (cSuggestionNodesLen > 0 && this.cur < cSuggestionNodesLen) {
        var oNode = cSuggestionNodes[this.cur];
        this.highlightSuggestion(oNode);
        if (currentVal.lastIndexOf(",") > 0) {
          this.textbox.value = currentVal.substr(0, currentVal.lastIndexOf(",")) + ", " + oNode.firstChild.nodeValue; 
        } else {
        	this.textbox.value = oNode.firstChild.nodeValue; 
        }
    } 
    
    // If press next at the last element then return the start input value
    if (this.cur > cSuggestionNodesLen - 1) {
    	(cSuggestionNodes[cSuggestionNodesLen - 1]).className = "";
    	this.textbox.value = this.storeInputText;
    	this.textbox.focus();
    	this.cur = -1;
    }
    
};

/**
 * Highlights the previous suggestion in the dropdown and
 * places the suggestion into the textbox. (up arrow)
 * @scope private
 */
UIAutoSuggestMultiValueControl.prototype.previousSuggestion = function () {
    var cSuggestionNodes = this.layer.childNodes;
    var cSuggestionNodesLen = (cSuggestionNodes ? cSuggestionNodes.length : 0);
    if (this.cur < 0) this.cur = cSuggestionNodesLen;
    --this.cur;
    var currentVal = this.textbox.value;
    if (cSuggestionNodesLen > 0 && this.cur >= 0) {
        var oNode = cSuggestionNodes[this.cur];
        this.highlightSuggestion(oNode);
        if (currentVal.lastIndexOf(",") > 0) {     
          this.textbox.value = currentVal.substr(0, currentVal.lastIndexOf(",")) + ", " + oNode.firstChild.nodeValue;   
        } else {
        	this.textbox.value = oNode.firstChild.nodeValue;   
        }
    }
    
    if (this.cur < 0) {
    	(cSuggestionNodes[0]).className = "";
    	this.textbox.value = this.storeInputText;
    	this.textbox.focus();
    	this.cur = cSuggestionNodesLen;
    }
};

/** 
 * Builds the suggestion layer contents, moves it into position,
 * and displays the layer.
 * @scope private
 * @param aSuggestions An array of suggestions for the control.
 */
UIAutoSuggestMultiValueControl.prototype.showSuggestions = function (aSuggestions /*:Array*/) {
    
    var oDiv = null;
    this.layer.innerHTML = "";  //clear contents of the layer
    var aSuggestionsLen = (aSuggestions ? aSuggestions.length : 0);
    // Display 10 top suggestion only
    var numberSuggestShowed = (aSuggestionsLen >= 10) ? 10 : (aSuggestionsLen);
    for (var i=0; i < numberSuggestShowed; i++) {
        oDiv = document.createElement("div");
        oDiv.appendChild(document.createTextNode(aSuggestions[i]));
        this.layer.appendChild(oDiv);
    }
    
    this.layer.style.left = this.getLeft() + "px";
    this.layer.style.top = (this.getTop()+this.textbox.offsetHeight) + "px";
    var thisLayer = this.layer;
    if (this.timeout) clearTimeout(this.timeout);
    // haven't support delay, maybe later
    this.timeout = setTimeout(function(){thisLayer.style.visibility = "visible";}, 0);
};

/*===================================================================*/
eXo.social.webui.UIAutoSuggestMultiValueControl = new UIAutoSuggestMultiValueControl();
