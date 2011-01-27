/**
 * 
 * TO USE UIPopUp?
 * To use this popup for displaying detail information of one user we must:
 * - Construct your document has 'a' tag with class name is 'IconLink' and href ends 
 *   with user's name; Ex:
 *     <a class="PoupUserName" href="linkToProfile/demo"> ... </a>
 * - Import this js file into your document;
 * - Import eXo.social.Util into your document;
 * - Call addPopupToLink()function to init UI Popup;
 * - Add variables: 
 *     + eXo.social.webui.UIPopUp.restContextName;
 *     + eXo.social.webui.UIPopUp.currentUserName;
 * - Import css of UI Popup into your stylesheet:
 *     @import url('/social-resources/skin/social/webui/UIPopUp/DefaultSkin.css');
 * 
 */
 
/*************************************************************************/

(function() { 
	
	var personId = null;
	var timeOutId = null;
	var actionType = null;
	var Util = eXo.social.Util;
	
	var UIPopUp = {
		REST_PATH: '/social/people',
		DEFAULT_REST_CONTEXT_NAME: 'rest-socialdemo',
		DEFAULT_AVATAR_URL: "/social-resources/skin/ShareImages/activity/MyStatusAvatar.gif",
		addPopupToLink: function() {
			  var listEls = eXo.social.Util.getElementsByClassName("PoupUserName", "a", document);
			  var numOfEls = listEls.length;
			 	var uiPopup = this;
			 	// Adds to each link mouse over action
			  for (var i = 0; i < numOfEls; i++) {
				  (function(idx) { 
						 this.targetEl = listEls[idx];
				     Util.addEventListener(this.targetEl, 'mouseover', function() {
						  	var hrefValue = this.href;
						  	uiPopup.personId = hrefValue.substr(hrefValue.lastIndexOf("/") + 1);
						  	
						  	if (uiPopup.timeOutId) clearTimeout(uiPopup.timeOutId);
					
					      var popUp = document.getElementById('UIPopup');
					      if (!popUp) {
					      	uiPopup.buildPopup(this);	  	
					      }
					 	    
					 	  	// Upadates user information
						  	uiPopup.reBuildPopup(false);
					
						  	// Displays
						  	uiPopup.displayPopup(this.parentNode);
		         }, false);
		         
		         Util.addEventListener(this.targetEl, 'mouseout', function() {
  			       var popUp = document.getElementById('UIPopup');
		         	 if (uiPopup.timeOutId) clearTimeout(uiPopup.timeOutId);
		           Util.hideElement(popUp.id);
		         }, false);
			    })(i);
			  }
			},
			reBuildPopup: function(isUpdated) {
				var UIPopUp = window.eXo.social.webui.UIPopUp;
				var portalName = UIPopUp.portalName;
			  var restCtxName = UIPopUp.restContextName;
			  var currentUserName = UIPopUp.currentUserName;
				var restContext = (restCtxName) ? restCtxName : UIPopUp.DEFAULT_REST_CONTEXT_NAME;
				var restURL = "/" + restContext;
				
				if (!portalName) {
					portalName = eXo.env.server.context;
				} else {
					portalName = '/' + portalName;
				}
				
				restURL +=  UIPopUp.REST_PATH + portalName + "/" + currentUserName + "/getPeopleInfo/" + this.personId + ".json"
				
				if (isUpdated) {
					restURL += "?updatedType=" + this.actionType;
				}
				
				this.makeRequest(restURL, true, this.reRenderPopUp);
			},
			reRenderPopUp: function(resp) {
				var UIPopUp = window.eXo.social.webui.UIPopUp;
				var avatarURL = (JSON.parse(resp.responseText)).avatarURL;
				var activityTitle = (JSON.parse(resp.responseText)).activityTitle;
				var relationStatus = (JSON.parse(resp.responseText)).relationshipType;
				var connectionAvatar = document.getElementById("UserAvatar");
				var imgTag = connectionAvatar.getElementsByTagName("img")[0];
				var connectionTitle = document.getElementById("UserTitle");
				var connectionAction = document.getElementById("UserAction");
				if (avatarURL) {
					imgTag.src = avatarURL;
				} else {
					imgTag.src = UIPopUp.DEFAULT_AVATAR_URL;
				}
				
				imgTag.style.height = "26px";
			  imgTag.style.width = "26px";
			
			  //TODO. Processes content to fit the space of popup: too long -> cut
			  
			  // TODO. Need check with the relation condition to make sure the activity is newest or default 
			  // cause the activity is kept after removing the relation.
				connectionTitle.innerHTML = activityTitle;
				
				var action = '<div id="Action">';
				if (relationStatus == "pending") { // Viewing is not owner
					action += 'Invited to make connection \n';
			    action += '<div id="Accept" class="AcceptAction" title="Accept">Accept</div>';
			    action += '|';
			    action += '<div id="Deny" class="DenyAction" title="Deny">Deny</div>';
				} else if (relationStatus == "waiting") { // Viewing is owner
					action += '<div id="Revoke" class="WaitingAction" title="Revoke">Revoke</div>';
				} else if (relationStatus == "confirmed") { // Had Connection 
					action += '<div id="Remove" class="RemoveAction" title="Disconnect">Disconnect</div>';
				} else if (relationStatus == "ignored") { // Connection is removed
					action += '<div id="Invite" class="InviteAction" title="Invite">Invite</div>';
				} else {
					action += '<div id="Invite" class="InviteAction" title="Invite">Invite</div>';
				}
    
        action += '</div>';
        
				connectionAction.innerHTML = action;
				
				var connectioActionEls = document.getElementById("UserAction");
				var tags = connectioActionEls.getElementsByTagName('div');
				for (idx in tags) {
					(function(i) {
						var tag = tags[i];
						Util.addEventListener(tag, 'click', function() {
				      UIPopUp.actionType = tag.id;
				      UIPopUp.reBuildPopup(true);
			      });
					})(idx);
				}
			},
			
			submitAction: function(actionType) {
				this.actionType = actionType;
				this.reBuildPopup(this.personId, true);
			},
			
			makeRequest: function(url, async, callback) {
			  if (async !== false) async = true;
			  var request = new XMLHttpRequest;
			  request.open('GET', url, async);
			  request.setRequestHeader("Cache-Control", "max-age=86400") ;
			  request.onreadystatechange = function() {
			    if((request.readyState === 4) && (request.status === 200)) {
			      if (callback) {
			        callback(request);
			      }
			    }
			  }
			  request.send(null);
			},
			buildPopup: function(element) {
				this.boundPopup = document.createElement("div");
				this.boundPopup.id = 'UIPopup';
				this.boundPopup.className = 'UIPopup';

				var uiPopup = this;
				
				Util.addEventListener(this.boundPopup, 'mouseover', function() {
       	  if (uiPopup.timeOutId) clearTimeout(uiPopup.timeOutId);
  				Util.showElement(this.id);
        }, false);
		         
				Util.addEventListener(this.boundPopup, 'mouseout', function() {
					Util.hideElement(this.id);
        }, false);
		         
				/********************************************************
			  *** CODE RENDER UIPOPUP WILL BE PLACED HERE *************
			  ********************************************************/
			  var html = [];
			  html.push('<div style="float: right; cursor:pointer;">');
			  html.push('<div id="ClosePopup" class="ClosePopup" title="Close">[x]</div>');
			  html.push('</div>');
			  html.push('<div id="UserAvatar" class="UserAvatar">');
			  html.push('<img></img>');	
			  html.push('</div>');
			  html.push('<div id="UserTitle" class="UserTitle">');
			  html.push('<span></span>');
			  html.push('</div>');
			  html.push('<div id="UserAction" class="UserAction">');
			  html.push('<span></span>');
			  html.push('</div>');
			  this.boundPopup.innerHTML = html.join('');
			  element.parentNode.appendChild(this.boundPopup);
			  
			  var closePopupEl = document.getElementById("ClosePopup");
			  Util.addEventListener(closePopupEl, 'click', this.hidePopup);
			},
			findPos: function(currentElement) {
				var curleft = curtop = 0;
				if (currentElement.offsetParent) {
					curleft = currentElement.offsetLeft;
					curtop = currentElement.offsetTop;
					while (currentElement == currentElement.offsetParent) {
						curleft += currentElement.offsetLeft;
						curtop += currentElement.offsetTop;
					}
				}
				return [curleft + 10, curtop - 80];
			},
			displayPopup: function(parentEl) {
				var popup_element = document.getElementById('UIPopup');
				var placement = this.findPos(parentEl);
			 	var delayPeriodTimer = 700;
				popup_element.style.left = placement[0] + "px";
				popup_element.style.top = placement[1] + "px";
				this.timeOutId = setTimeout("eXo.social.webui.UIPopUp.showPopup()", delayPeriodTimer);
			},
			showPopup: function() {
				var popup_element = document.getElementById('UIPopup');
				Util.showElement(popup_element.id);
			},
			hidePopup: function() {
				var popup_element = document.getElementById('UIPopup');
				Util.hideElement(popup_element.id);
			}
		
	};
	
	window.eXo = window.eXo || {}; 
	window.eXo.social = window.eXo.social || {}; 
	window.eXo.social.webui = window.eXo.social.webui || {}; 
	window.eXo.social.webui.UIPopUp = UIPopUp; 
})(); 