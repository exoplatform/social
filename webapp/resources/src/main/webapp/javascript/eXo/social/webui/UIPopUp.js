/**
 * 
 * TO USE UIPopUp:
 * Add class name 'UserName' to achor tag
 * 
 */
 
/*************************************************************************/

(function() { 
	
	var DEFAULT_REST_INFO = {
	  PATH: '/social/people',
    CONTEXT_NAME: 'rest-socialdemo'
	},
	DEFAULT_AVATAR_URL = "/social-resources/skin/ShareImages/activity/MyStatusAvatar.gif";
	
	var personId = null;
	var timeOutId = null;
	var actionType = null;
	
  eXo.social.UIPopUp = {
		
			reBuildPopup: function(isUpdated) {
				var UIPopUp = window.eXo.social.webui.UIPopUp;
				var portalName = eXo.social.UIPopUp.portalName;
			  var restCtxName = eXo.social.UIPopUp.restContextName;
			  var currentUserName = eXo.social.UIPopUp.currentUserName;
				var restContext = (restCtxName) ? restCtxName : eXo.social.UIPopUp.DEFAULT_REST_CONTEXT_NAME;
				var restURL = "/" + restContext;
				
				if (!portalName) {
					portalName = eXo.env.server.context;
				} else {
					portalName = '/' + portalName;
				}
				
				restURL +=  eXo.social.UIPopUp.REST_PATH + portalName + "/" + currentUserName + "/getPeopleInfo/" + eXo.social.UIPopUp.personId + ".json"
				
				if (isUpdated) {
					restURL += "?updatedType=" + eXo.social.UIPopUp.actionType;
				}
				
				eXo.social.UIPopUp.makeRequest(restURL, true, eXo.social.UIPopUp.reRenderPopUp);
			},
			reRenderPopUp: function(resp) {
				var UIPopUp = window.eXo.social.webui.UIPopUp;
				var avatarURL = (gj.parseJSON(resp.responseText)).avatarURL;
				var activityTitle = (gj.parseJSON(resp.responseText)).activityTitle;
				var relationStatus = (gj.parseJSON(resp.responseText)).relationshipType;
				var connectionAvatar = document.getElementById("UserAvatar");
				var imgTag = connectionAvatar.getElementsByTagName("img")[0];
				var connectionTitle = document.getElementById("UserTitle");
				var connectionAction = document.getElementById("UserAction");
				if (avatarURL) {
					imgTag.src = avatarURL;
				} else {
					imgTag.src = eXo.social.UIPopUp.DEFAULT_AVATAR_URL;
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
				      eXo.social.UIPopUp.actionType = tag.id;
				      eXo.social.UIPopUp.reBuildPopup(true);
			      });
					})(idx);
				}
			},
			
			submitAction: function(actionType) {
				eXo.social.UIPopUp.actionType = actionType;
				eXo.social.UIPopUp.reBuildPopup(eXo.social.UIPopUp.personId, true);
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
				eXo.social.UIPopUp.boundPopup = document.createElement("div");
				eXo.social.UIPopUp.boundPopup.id = 'UIPopup';
				eXo.social.UIPopUp.boundPopup.className = 'UIPopup';

				var uiPopup = this;
				
				/********************************************************
			  *** CODE RENDER UIPOPUP WILL BE PLACED HERE *************
			  ********************************************************/
			  var html = [];
			  html.push('<div style="float: right; cursor:pointer;">');
			  html.push('<div id="ClosePopup" class="ClosePopup" title="Close">[x]</div>');
			  html.push('</div>');
			  html.push('<div id="UserAvatar" class="UserAvatar">');
			  html.push('<img title="Avatar" alt="Avatar"></img>');	
			  html.push('</div>');
			  html.push('<div id="UserTitle" class="UserTitle">');
			  html.push('<span></span>');
			  html.push('</div>');
			  html.push('<div id="UserAction" class="UserAction">');
			  html.push('<span></span>');
			  html.push('</div>');
			  eXo.social.UIPopUp.boundPopup.innerHTML = html.join('');
			  element.parentNode.appendChild(eXo.social.UIPopUp.boundPopup);
			  
			  var closePopupEl = document.getElementById("ClosePopup");
			  Util.addEventListener(closePopupEl, 'click', eXo.social.UIPopUp.hidePopup);
			}
		
	};
	
	function buildURL() {
	  var portalName = eXo.social.webui.portalName;
	  var restCtxName = eXo.social.webui.restContextName;
	  var currentUserName = eXo.social.webui.currentUserName;
    var restCtx = restCtxName ? restCtxName : DEFAULT_REST_INFO.CONTEXT_NAME;
    
    var restURL = "/" + restCtx;
    
    if (!portalName) {
      portalName = eXo.env.server.context;
    } else {
      portalName = '/' + portalName;
    }
    
    restURL +=  DEFAULT_REST_INFO.PATH + portalName + "/" + currentUserName + "/getPeopleInfo/person_Id.json"
    
    //if (isUpdated) {
//      restURL += "?updatedType=" + eXo.social.UIPopUp.actionType;
//    }
    return restURL;
	};
	
	
	function reBuildPopup(el) {
	  el.val();
	  
	};
	
	function getContent() {
	  var html = [];
	  html.push('<div style="float: right; cursor:pointer;">');
	  html.push('<div id="ClosePopup" class="ClosePopup" title="Close">[x]</div>');
	  html.push('</div>');
	  html.push('<div id="UserAvatar" class="UserAvatar">');
	  html.push('<img title="Avatar" alt="Avatar"></img>'); 
	  html.push('</div>');
	  html.push('<div id="UserTitle" class="UserTitle">');
	  html.push('<span></span>');
	  html.push('</div>');
	  html.push('<div id="UserAction" class="UserAction">');
	  html.push('<span></span>');
	  html.push('</div>');
	  return html.join('');
	};
	
	/* Adding a tooltip to any tag has specified class name. */    
	gj(document).ready(function(){
	  gj('.UserName').toolTip(buildURL(), {color:'yellow'});
	});
})(); 
