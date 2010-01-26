if(!eXo.social) eXo.social = {};
if(!eXo.social.webui) eXo.social.webui = {};

/**
 * An autosuggest textbox control.
 * @class
 * @scope public
 */
function UIAutoSuggestControl() {
};

/**
 * Load the initialization for textbox control.
 * @scope private
 */
UIAutoSuggestControl.prototype.load = function(oTextbox /*:HTMLInputElement*/, 
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
    
    //initialize the control
    this.init();
    
}

/**
 * Autosuggests one or more suggestions for what the user has typed.
 * If no suggestions are passed in, then no autosuggest occurs.
 * @scope private
 * @param aSuggestions An array of suggestion strings.
 * @param bTypeAhead If the control should provide a type ahead suggestion.
 */
UIAutoSuggestControl.prototype.autosuggest = function (aSuggestions /*:Array*/, currTextBoxValue) {
    //make sure there's at least one suggestion
    if (aSuggestions.length > 0) {
        this.showSuggestions(aSuggestions);
    } else {
        this.hideSuggestions();
    }
};

/**
 * Creates the dropdown layer to display multiple suggestions.
 * @scope private
 */
UIAutoSuggestControl.prototype.createDropDown = function () {

    var oThis = this;

    //create the layer and assign styles
    this.layer = document.createElement("div");
    this.layer.className = "suggestions";
    this.layer.style.visibility = "hidden";
    this.layer.style.width = this.textbox.offsetWidth;
    
    //when the user clicks on the a suggestion, get the text (innerHTML)
    //and place it into a textbox
    this.layer.onmousedown = 
    this.layer.onmouseup = 
    this.layer.onmouseover = function (oEvent) {
        oEvent = oEvent || window.event;
        oTarget = oEvent.target || oEvent.srcElement;

        if (oEvent.type == "mousedown") {
            oThis.textbox.value = oTarget.firstChild.nodeValue;
            oThis.hideSuggestions();
            oThis.provider.submitSearchForm(oThis.textbox);
        } else if (oEvent.type == "mouseover") {
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
UIAutoSuggestControl.prototype.getLeft = function () /*:int*/ {

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
UIAutoSuggestControl.prototype.getTop = function () /*:int*/ {
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
UIAutoSuggestControl.prototype.handleKeyDown = function (oEvent /*:Event*/) {
	if (this.layer.style.visibility == "visible") { //Applied on suggestion drop-down only.
	    switch(oEvent.keyCode || oEvent.which) {
	        case 38: //up arrow
	            this.previousSuggestion();
	            break;
	        case 40: //down arrow
	            this.nextSuggestion();
	            break;
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
UIAutoSuggestControl.prototype.handleKeyUp = function (oEvent /*:Event*/) {
    var iKeyCode = oEvent.keyCode || oEvent.which;
    var el = oEvent.target || oEvent.srcElement;
	var isInputTag = (el.tagName.toLowerCase() == 'input');
	
    //for backspace (8) and delete (46), shows suggestions without typeahead
    if (iKeyCode == 8 || iKeyCode == 46) {
    	if (isInputTag) {
    		this.storeText = el.value;
    	}
        this.provider.requestSuggestions(this);
        
    //make sure not to interfere with non-character keys
    } else if (iKeyCode < 32 || (iKeyCode >= 33 && iKeyCode < 46) || (iKeyCode >= 112 && iKeyCode <= 123)) {
        //ignore
    } else {
    	if (isInputTag) {
    		this.storeText = el.value;
    	}
    	this.provider.requestSuggestions(this);
    }
};

/**
 * Hides the suggestion dropdown.
 * @scope private
 */
UIAutoSuggestControl.prototype.hideSuggestions = function () {
    this.layer.style.visibility = "hidden";
};

/**
 * Highlights the given node in the suggestions dropdown.
 * @scope private
 * @param oSuggestionNode The node representing a suggestion in the dropdown.
 */
UIAutoSuggestControl.prototype.highlightSuggestion = function (oSuggestionNode) {
    
    for (var i=0; i < this.layer.childNodes.length; i++) {
        var oNode = this.layer.childNodes[i];
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
UIAutoSuggestControl.prototype.init = function () {

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
    
    //create the suggestions dropdown
    this.createDropDown();
};

/**
 * Highlights the next suggestion in the dropdown and
 * places the suggestion into the textbox. (down arrow)
 * @scope private
 */
UIAutoSuggestControl.prototype.nextSuggestion = function () {
    var cSuggestionNodes = this.layer.childNodes;
    if (this.cur == cSuggestionNodes.length) this.cur = -1;
    ++this.cur;
    if (cSuggestionNodes.length > 0 && this.cur < cSuggestionNodes.length) {
        var oNode = cSuggestionNodes[this.cur];
        this.highlightSuggestion(oNode);
        this.textbox.value = oNode.firstChild.nodeValue; 
    } 
    
    // If press next at the last element then return the start input value
    if (this.cur > cSuggestionNodes.length - 1) {
    	(cSuggestionNodes[cSuggestionNodes.length - 1]).className = "";
    	this.textbox.value = this.storeText;
    	this.textbox.focus();
    	this.cur = -1;
    }
    
};

/**
 * Highlights the previous suggestion in the dropdown and
 * places the suggestion into the textbox. (up arrow)
 * @scope private
 */
UIAutoSuggestControl.prototype.previousSuggestion = function () {
    var cSuggestionNodes = this.layer.childNodes;
    if (this.cur < 0) this.cur = cSuggestionNodes.length;
    --this.cur;
    if (cSuggestionNodes.length > 0 && this.cur >= 0) {
        var oNode = cSuggestionNodes[this.cur];
        this.highlightSuggestion(oNode);
        this.textbox.value = oNode.firstChild.nodeValue;   
    }
    
    if (this.cur < 0) {
    	(cSuggestionNodes[0]).className = "";
    	this.textbox.value = this.storeText;
    	this.textbox.focus();
    	this.cur = cSuggestionNodes.length;
    }
};

/** 
 * Builds the suggestion layer contents, moves it into position,
 * and displays the layer.
 * @scope private
 * @param aSuggestions An array of suggestions for the control.
 */
UIAutoSuggestControl.prototype.showSuggestions = function (aSuggestions /*:Array*/) {
    
    var oDiv = null;
    this.layer.innerHTML = "";  //clear contents of the layer
    // Display 10 top suggestion only
    var numberSuggestShowed = (aSuggestions.length >= 10) ? 10 : (aSuggestions.length);
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
eXo.social.webui.UIAutoSuggestControl = new UIAutoSuggestControl();