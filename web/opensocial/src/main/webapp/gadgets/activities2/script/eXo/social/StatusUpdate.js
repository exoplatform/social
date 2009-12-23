/**
 * StatusUpdate.js - the main entry class.
 * used as main entry application for status update.
 * calls and intialize all instances.
 * @author	<a href="mailto:hoatlevan@gmail.com">hoatle</a>
 * @since	Oct 19, 2009
 * @copyright	eXo Platform SEA
 */

var eXo = eXo || {};
eXo.social = eXo.social || {};

/**
 * StatusUpdate constructor
 *
 */
eXo.social.StatusUpdate = function() {
	this.shareable = false; //if false, click on share do nothing
	this.viewer = null;
	this.owner = null;
	this.ownerFriends = null;
	this.ownerActivities = null; //stores all owner activites
	this.ownerFriendsActivities = null; //stores all owner's friends' activities
	this.linkShare = null;
	this.uiComposer = null;
	this.currentView = gadgets.views.getCurrentView().getName();
	this.newOwnerActivities = null; //stores new activites to notify users and then displays
	this.newOwnerFriendsActivities = null;
	this.ownerMoreClickedTimes = 0; //click num
	this.friendsMoreClickedTimes = 0;
	this.miniMessage = new gadgets.MiniMessage();
}

/**
 * static variables
 */
eXo.social.StatusUpdate.DataMode_BOTH = 'DataMode_BOTH';
eXo.social.StatusUpdate.DataMode_OWNER_ONLY = 'DataMode_OWNER_ONLY';
eXo.social.StatusUpdate.DataMode_FRIENDS_ONLY = 'DataMode_FRIENDS_ONLY';

eXo.social.StatusUpdate.MessageType_ERROR = 'MessageType_ERROR';
eXo.social.StatusUpdate.MessageType_INFO  = 'MessageType_INFO';
eXo.social.StatusUpdate.MessageType_WARN = 'MessageType_WARN';

/**
 * static config object
 */
eXo.social.StatusUpdate.config = {
	ACTIVITIES_REST_URL: "http://localhost:8080/rest/social/activities",
	IDENTITY_REST_URL : "http://localhost:8080/rest/social/activities/identity",
	path : {
		ROOT_PATH : "http://localhost:8080/social/gadgets/activities2",
		SCRIPT_PATH : "http://localhost:8080/social/gadgets/activities2/script"
	},
	ui : {//dom id reference
		UI_MY_STATUS_INPUT: "UIMyStatusInput",
		UI_FRIENDS_ACTIVITIES: "UIFriendsActivities",
		IMG_OWNER_AVATAR: "ImgOwnerAvatar",
		UI_COMPOSER_TEXTAREA : "UIComposerTextArea",
		UI_COMPOSER_EXTENSION : "UIComposerExtension",
		UI_COMPOSER_SHARE_BUTTON: "UIComposerShareButton",
		UI_OWNER_APPENDABLE_ROOT :"UIOwnerAppendableRoot",
		UI_OWNER_MORE : "UIOwnerMore",
		UI_FRIENDS_APPENDABLE_ROOT: "UIFriendsAppendableRoot",
		UI_FRIENDS_MORE: "UIFriendsMore"
	},
	BATCH_SIZE: 10, //batch size to get activities
	MAX_ACTIVITIES : 5, //default displays 5 item only
	REFRESH_TIME : 3 * 60 * 1000 //in miliseconds; should improve by detecting user's activity on app. Can be changed by user's pref
};

/**
 * main entry point for app
 * @static
 */
eXo.social.StatusUpdate.main = function() {
	//get full render from start BUG #SOC-375
	gadgets.window.adjustHeight();
	var statusUpdate = new eXo.social.StatusUpdate();
	statusUpdate.init();
	//create and use linkShare object
	var linkShare = eXo.social.linkShare = new eXo.social.LinkShare();
	//set ref
	statusUpdate.linkShare = linkShare;
	eXo.social.Like.ref.statusUpdate = statusUpdate;
	linkShare.init();
}

/**
 * Inits
 */
eXo.social.StatusUpdate.prototype.init = function() {
	//alias imports
	var Locale = eXo.social.Locale;
	var UIComposer = eXo.social.UIComposer;
	var Util = eXo.social.Util;
	var StatusUpdate = eXo.social.StatusUpdate;
	var config = StatusUpdate.config;
	var statusUpdate = this;
	var miniMessage = statusUpdate.miniMessage;
	var uiComposerTextArea = Util.getElementById(config.ui.UI_COMPOSER_TEXTAREA);
	var uiComposer = new UIComposer(uiComposerTextArea);
	this.uiComposer = uiComposer;
	uiComposer.statusUpdate = this;
	if (!uiComposerTextArea) {
		debug.error("uiComposerTextArea is null!");
		return;
	}
	//event handler attach
	Util.addEventListener(uiComposerTextArea, 'focus', function() {
		uiComposer.focusInput(this);
	}, false);
	Util.addEventListener(uiComposerTextArea, 'blur', function() {
		uiComposer.blurInput(this);
	}, false);
	Util.addEventListener(uiComposerTextArea, 'keypress', function(evt) {
		uiComposer.onKeyPress(evt);
	}, false);
	var uiComposerShareButton = Util.getElementById(config.ui.UI_COMPOSER_SHARE_BUTTON);
	if (!uiComposerShareButton) {
		debug.error('uiComposerShareButton is null');
		return;
	}
	Util.addEventListener(uiComposerShareButton, 'click', function() {
		statusUpdate.share(this);
	}, false);


	//run to set viewer, owner, owner's friends
	(function() {

		/**
		 * hard-coded get username by url of top address
		 * /activitites/root ; /activities/demo
		 * @return	username by url or null
		 */
		var getUsernameFromUrl = function() {
			var address;
			try {
				address = window.top.location.href;
			} catch(e) {
				//cross-domain exception
				miniMessage.createDismissibleMessage(Locale.getMsg('internal_error'));
			}
			if (address !== null) {
				var firstIndex = address.indexOf('/activities/');
				if (firstIndex < 0) {
					return null;
				}
				firstIndex += '/activities/'.length;
				//when url is appended with ?
				var questionIndex = address.indexOf("?");
				var lastIndex = 0;
				if (questionIndex > 0) {
					lastIndex = (address.substr(0, questionIndex)).length;
				} else {
					lastIndex = address.length;
				}
				if (firstIndex >= lastIndex) {
					return null;
				}
				return address.substring(firstIndex, lastIndex);
			}
			return null;
		}

		/**
		 * fix ownerHandler
		 * To detect the ownerId by url (hard-coded), eXo Social container has to improve this.
		 */
		var fixOwnerHandler = function(res) {
			var data = res.data;
			if (data !== null) {
				var ownerId = data.id;
				//regets ownerId if different id
				if (ownerId !== statusUpdate.owner.getId()) {
					var req = opensocial.newDataRequest();
					req.add(req.newFetchPersonRequest(ownerId), 'owner');
					req.send(function(res) {
						if (res.hadError()) {
							debug.warn("Can not reget owner!");
							debug.info(res);
							miniMessage.createDismissibleMessage(Locale.getMsg('internal_error'));
							return;
						}
						statusUpdate.owner = res.get('owner').getData();
						//hides text input + friends's activitites
						var uiMyStatusInput = Util.getElementById(config.ui.UI_MY_STATUS_INPUT);
						var uiFriendsActivities = Util.getElementById(config.ui.UI_FRIENDS_ACTIVITIES);
						uiMyStatusInput.style.display = 'none';
						uiFriendsActivities.style.display = 'none';
						statusUpdate.refresh();
					});
				} else {
					statusUpdate.refresh();
				}
			}
		}

		var req = opensocial.newDataRequest();
		req.add(req.newFetchPersonRequest(opensocial.DataRequest.PersonId.VIEWER), 'viewer');
		req.add(req.newFetchPersonRequest(opensocial.DataRequest.PersonId.OWNER), 'owner');
		var opts = {};
    	opts[opensocial.DataRequest.PeopleRequestFields.FIRST] =  0;
    	opts[opensocial.DataRequest.PeopleRequestFields.MAX] = 40; //why 40?
    	opts[opensocial.DataRequest.PeopleRequestFields.PROFILE_DETAILS] =
    		[opensocial.Person.Field.NAME,
             opensocial.Person.Field.THUMBNAIL_URL];
    	req.add(req.newFetchPeopleRequest(opensocial.DataRequest.Group.OWNER_FRIENDS, opts), 'ownerFriends');
		req.send(handler);
		function handler(response) {
			var miniMessage = statusUpdate.miniMessage;
			if (response.hadError()) {
				debug.warn('Can not get viewer, owner, ownerFriends!');
				debug.info(response);
				miniMessage.createDismissibleMessage(Locale.getMsg('internal_error'));
				gadgets.window.adjustHeight();
				return;
			}
			statusUpdate.viewer = response.get('viewer').getData();
			statusUpdate.owner = response.get('owner').getData();
			statusUpdate.ownerFriends = response.get('ownerFriends').getData();
			var username = getUsernameFromUrl();
			debug.info('username: ' + username);
			if (username !== null) {
				var url = config.IDENTITY_REST_URL + "/" + username + "/id";
				debug.info(url);
				Util.makeRequest(url, fixOwnerHandler);
			} else {
				statusUpdate.refresh();
			}
			//BUG: can not use person.isViewer() or person.isOwner()
			//debug.debug(this.viewer.isViewer());
			//debug.debug(this.owner.isOwner());
		}
	})();
	//refresh after a specific of time
	//window.setInterval(function() {
	//	statusUpdate.refresh();
	//}, this.refreshTime);
}

eXo.social.StatusUpdate.prototype.refresh = function() {
	var Util = eXo.social.Util;
	var Locale = eXo.social.Locale;
	var StatusUpdate = eXo.social.StatusUpdate;
	var config = StatusUpdate.config;
	debug.info("Refresh!!!!");
	var statusUpdate = this;
	var miniMessage = statusUpdate.miniMessage;
	//updates owner avatar
	var imgOwnerAvatar = Util.getElementById(config.ui.IMG_OWNER_AVATAR);
	if (!imgOwnerAvatar) {
		debug.error('imgOwnerAvatar is null!');
		miniMessage.createDismissibleMessage(Locale.getMsg('internal_error'));
		return;
	}
	imgOwnerAvatar.src = statusUpdate.getAvatar(statusUpdate.owner.getId());
	var ownerActivityTitle = Util.getElementById('OwnerActivityTitle');
  	if (!ownerActivityTitle) {
  		debug.error('ownerActivityTitle is null!!!');
  		miniMessage.createDismissibleMessage(Locale.getMsg('internal_error'));
  		return;
  	}
  	ownerActivityTitle.innerHTML = Locale.getMsg('activities_of_displayName', [statusUpdate.owner.getDisplayName()]);

  	//Create request for getting owner's and ownerFriends' activities.
  	var req = opensocial.newDataRequest();
	var opts_act = {};
 	opts_act[opensocial.DataRequest.ActivityRequestFields.FIRST] = 0;
  	opts_act[opensocial.DataRequest.ActivityRequestFields.MAX] = StatusUpdate.config.BATCH_SIZE;
  	req.add(req.newFetchActivitiesRequest(statusUpdate.owner.getId(), opts_act), 'ownerActivities');
  	if (this.viewer.getId() === this.owner.getId()) {
  		req.add(req.newFetchActivitiesRequest(opensocial.DataRequest.Group.OWNER_FRIENDS, opts_act), 'ownerFriendsActivities');
  		req.send(function(res) {
  			statusUpdate.handleActivities(res);
  		});
  	} else {
  	  	req.send(function(res) {
  			statusUpdate.handleActivities(res, StatusUpdate.DataMode_OWNER_ONLY);
  		});
  	}


}

/**
 * set onclick handler for deleting
 */
eXo.social.StatusUpdate.prototype.setDeleteActivity = function(activityId) {
  var Util = eXo.social.Util;
  var Locale = eXo.social.Locale;
  var el = Util.getElementById('Delete'+activityId);
  var statusUpdate = this;
  el.onclick = function() {
	if (confirm(Locale.getMsg('are_you_sure_to_delete_this_activity'))) {
		statusUpdate.deleteActivity(activityId);
	}
  }
}

/**
 * sets action container
 */
eXo.social.StatusUpdate.prototype.setActionContentButton = function(activityId) {
  var Util = eXo.social.Util;

    function display(activityId) {
      var el = Util.getElementById('MenuItemContainer' + activityId);
      el.style.visibility = 'visible';
    }

    function hide(activityId) {
      var el = Util.getElementById('MenuItemContainer' + activityId);
      el.style.visibility = 'hidden';
    }

    function toggle(activityId) {
      var el = Util.getElementById('ActionContentButton' + activityId);
      var menuItemContainer = Util.getElementById('MenuItemContainer' + activityId);
      el.onclick = function() {
        display(activityId);
      }
      el.onmouseover = function() {
        display(activityId);
      }
      el.onmouseout = function() {
        hide(activityId);
      }
      menuItemContainer.onmouseover = function() {
        display(activityId);
      }
      menuItemContainer.onmouseout = function() {
        hide(activityId);
      }
    }
    toggle(activityId);
}

/**
 * delete activity by id
 */
eXo.social.StatusUpdate.prototype.deleteActivity = function(activityId) {
	var Util = eXo.social.Util,
      url = eXo.social.StatusUpdate.config.ACTIVITIES_REST_URL + '/delete/'+activityId,
      statusUpdate = this;
	eXo.social.Util.makeRequest(url, function(res) {
	   if (res.data.id) {
         Util.removeElementById('Activity' + res.data.id);
         //check if delete all => refresh
         var rootEl = Util.getElementById('UIOwnerAppendableRoot');
         if (!rootEl.hasChildNodes()) {
           statusUpdate.refresh();
         }
	   } else {
	       //TODO: informs problem
	   }
	}, null, gadgets.io.MethodType.GET, gadgets.io.ContentType.JSON, null);
}


/**
 * set onclick handler for deleting
 */
eXo.social.StatusUpdate.prototype.setCommentActivity = function(activityId) {
  var Util = eXo.social.Util;
  var Locale = eXo.social.Locale;
  var el = Util.getElementById('Comment'+activityId);
  var statusUpdate = this;
  el.onclick = function() {
	statusUpdate.commentActivity(activityId);
  }
}

/**
 * delete activity by id
 */
eXo.social.StatusUpdate.prototype.commentActivity = function(activityId) {
	//alert("tungcnw");
	/*var activity = opensocial.newActivity({ 'title' : this.viewer.getDisplayName(), 'body' : 'reply to this activity', "EXTERNAL_ID" : activityId});
	var statusUpdate = this;
	opensocial.requestCreateActivity(activity, "HIGH", function() {
		statusUpdate.refresh();}
	);
	restore();
	return;*/

}

/**
 * gets new activities of owner's
 * informs user about new activities to display
 */
eXo.social.StatusUpdate.prototype.getNewOwnerActivities = function() {
	var config = eXo.social.StatusUpdate.config;
	var statusUpdate = this;
	var getEnough = false;
	var newActivities = null;
	var times = 0;
	function getNewActivities() {
		var req = opensocial.newDataRequest();
		var params = {};
		params[opensocial.DataRequest.ActivityRequestFields.FIRST] = 0;
		params[opensocial.DataRequest.ActivityRequestFields.MAX] = config.BATCH_SIZE + (config.BATCH_SIZE * times)
		req.add(req.newFetchActivitiesRequest(statusUpdate.owner.getId(), params), 'ownerActivities');
		req.send(ownerActivitiesHandler);
	}

	function ownerActivitiesHandler(res) {
		if (res.hadError()) {
			debug.warn('statusUpdate.getNewOwnerActivities: Error get activitites!!');
			return;
		}
		debug.info(res);
	}
}

/**
 * gets new activities of owner's friends'
 * informs user about new activities to display
 */
eXo.social.StatusUpdate.prototype.getNewOwnerFriendsActivities = function() {

}

/**
 * Automatically gets new activity,
 * if any newer activity than currentFirstActivity => insert activity before currentFirstActivity element
 * Notifies user the number of new activities.
 * After clicked on notification, updates
 */
eXo.social.StatusUpdate.prototype.updateOwnerActivities = function() {
	var StatusUpdate = eXo.social.StatusUpdate;
	var statusUpdate = this;
	var req = opensocial.newDataRequest();
	var params = {};
	params[opensocial.DataRequest.ActivityRequestFields.FIRST] = 0;
	params[opensocial.DataRequest.ActivityRequestFields.MAX] = StatusUpdate.config.BATCH_SIZE + (StatusUpdate.config.BATCH_SIZE * this.ownerMoreClickedTimes);
	req.add(req.newFetchActivitiesRequest(statusUpdate.owner.getId(), params), 'ownerActivities');
	req.send(function(res) {
		statusUpdate.handleActivities(res, StatusUpdate.DataMode_OWNER_ONLY);
	});
}

eXo.social.StatusUpdate.prototype.updateFriendsActivities = function() {
	var StatusUpdate = eXo.social.StatusUpdate;
	var statusUpdate = this;
	var req = opensocial.newDataRequest();
	var params = {};
	params[opensocial.DataRequest.ActivityRequestFields.FIRST] = 0;
	params[opensocial.DataRequest.ActivityRequestFields.MAx] = StatusUpdate.config.BATCH_SIZE + (StatusUpdate.config.BATCH_SIZE * this.friendsMoreClickedTimes);
	req.add(req.newFetchActivitiesRequest(opensocial.DataRequest.Group.OWNER_FRIENDS, params), 'ownerFriendsActivities');
	req.send(function(res) {
		statusUpdate.handleActivities(res, StatusUpdate.DataMode_FRIENDS_ONLY);
	});
}


/**
 * callback hander for activities
 * @param	dataResponse
 * @param	opt_dataMode - can be StatusUpdate.DataMode_BOTH
 *								  StatusUpdate.DataMode_OWNER_ONLY
 *                                StatusUpdate.DataMode_FRIENDS_ONLY
 */
eXo.social.StatusUpdate.prototype.handleActivities = function(dataResponse, dataMode) {
	var Locale = eXo.social.Locale;
	var Util = eXo.social.Util;
	var Like = eXo.social.Like;
	var StatusUpdate = eXo.social.StatusUpdate;
	var config = StatusUpdate.config;
	var statusUpdate = this;
	var miniMessage = statusUpdate.miniMessage;
	var dataMode = dataMode || StatusUpdate.DataMode_BOTH;
	//private methods
	/**
	 * sort activites by time
	 */
  	var  sortPostedTimeHandler = function(activity1, activity2) {
		if (activity1.getField('postedTime') > activity2.getField('postedTime')) {
			return -1;
		} else if (activity1.getField('postedTime') < activity2.getField('postedTime')) {
			return 1;
		}
		return 0;
  	}

  	/**
  	 * gets html block of normal activity
  	 * @param	activity object
     * @return html
  	 */
  	var getNormalActivityBlock = function(activity) {
  		if (!activity) {
  			debug.error('getNormalActivityBlock: activity is null.');
  			debug.info(activity);
  			miniMessage.createDismissibleMessage(Locale.getMsg('internal_error'));
  			return '';
  		}
  		var userId = activity.getField(opensocial.Activity.Field.USER_ID),
          url = activity.getField(opensocial.Activity.Field.URL),
          activityId =  activity.getField(opensocial.Activity.Field.ID),
          viewerId = statusUpdate.viewer.getId(),
          ownerId = statusUpdate.owner.getId(),
          title = activity.getField(opensocial.Activity.Field.TITLE),
          body = activity.getField(opensocial.Activity.Field.BODY),
          userName = statusUpdate.getName(userId),
          avatarUrl = statusUpdate.getAvatar(userId),
          prettyTime = Util.toPrettyTime(new Date(activity.getField('postedTime'))),
          html = [];

  		html.push('<div class="ActivitiesContent">');
  			html.push('<a href="#" class="AvatarPeopleBG">');
  				html.push('<img height="47px" width="47px" src="' + avatarUrl + '" />');
  			html.push('</a>');
  			html.push('<div class="Content">');
  			html.push('<div class="Titlecontent" style="height: 24px;">');
  				html.push('<div class="TitleItem">' + title + '</div>');
        if ((statusUpdate.currentView === 'canvas') && (viewerId === ownerId)) {
          html.push('<div class="ActionContent">');
          html.push('<div id="ActionContentButton' + activityId + '" class="ActionContenButton" href="#action"><span></span></div>');
          html.push('<div id="MenuItemContainer' + activityId + '" style="position: absolute; display: block; min-width: 98px; top: 19px; right: 5px; visibility: hidden;" class="MenuItemContainer">');
            html.push('<div class="SubBlock">');
            if (ownerId === userId) {
              html.push('<div class="MenuItem">');
                html.push('<a id="Delete'+ activityId +'" class="ItemIcon DeleteIcon DefaultPageIcon" title="'+ Locale.getMsg('delete') +'" href="#delete">' + Locale.getMsg('delete') + '</a>');
              html.push('</div>');
            }
            html.push('</div>');
          html.push('</div>');
        html.push('</div>');
        }
				html.push('<div style="clear: both; height: 0px;"><span></span></div>');
				html.push('</div>');
  				html.push('<div class="Content">' + body + '</div>');
  				html.push('<div class="NewsDate">' + prettyTime + '</div>');
  			if (statusUpdate.currentView === 'canvas') {
  				html.push('<a id="Comment' + activityId + '" href="#comment" style="color: #058ee6;">' + Locale.getMsg('comment') + '</a><span>|</span><a id="Like' + activityId + '" href="#like" style="color: #058ee6;">' + Locale.getMsg('like') + '</a>');
  			}
  			html.push('</div>')
  			html.push('<div class="ClearLeft"><span></span></div>');
  		html.push('</div>');
  		html.push('<div class="ListPeopleLikeBG">');
  			html.push('<div id="ListPeopleLike' + activityId + '" class="ListPeopleLike DisplayNone">');
  				html.push('<div class="ListPeopleContent"');
  					html.push('<div id="TitleLike' + activityId + '" class="Title"></div>');
  					html.push('<div style="display:none;" id="ListPeople' + activityId + '"></div>');
  					html.push('<div class="ClearLeft"><span></span></div>');
  				html.push('</div>');
  			html.push('</div>');
  		html.push('</div>');
  		return html.join('');
  	}

  	/**
  	 * get html block of link share activity
  	 * @param	activity
  	 * @param	data json body object from activity.body
  	 */
  	var getLinkShareActivityBlock = function(activity, jsonBody) {
  	  	if (!activity || !jsonBody) {
  	  		debug.error('getLinkShareActivityBlock: activity or data is null.');
  	  		debug.info(activity);
  	  		debug.info(jsonBody);
  	  		miniMessage.createDismissibleMessage(Locale.getMsg('internal_error'));
  	  		return '';
  	  	}

	  		var userId = activity.getField(opensocial.Activity.Field.USER_ID);
	  		var url = activity.getField(opensocial.Activity.Field.URL);
			var activityId =  activity.getField(opensocial.Activity.Field.ID);
			var viewerId = statusUpdate.viewer.getId(),
			ownerId = statusUpdate.owner.getId();
	  		var title = activity.getField(opensocial.Activity.Field.TITLE);
	  		var userName = statusUpdate.getName(userId);
	  		var avatarUrl = statusUpdate.getAvatar(userId);
	  		var prettyTime = Util.toPrettyTime(new Date(activity.getField('postedTime')));
	  		var html = [];
	  		html.push('<div class="ActivitiesContent"');
	  			html.push('<div class="MiniAvatarSpaceBG">');
	    			html.push('<img src="' + avatarUrl + '" width="60" height="60" />');
	   			html.push('</div>');
	    		html.push('<div class="UserName">');
	    			html.push('<a href="#">' + userName + '</a>');
	    		html.push('</div>');
	    		html.push('<div class="UserStatus">');
  				if (jsonBody.comment) {
  					html.push(jsonBody.comment);
  				}
  				html.push('</div>');
  				html.push('<div class="Thumbnail">');
  				if (jsonBody.data.noThumbnail === false) {
  					html.push('<img width="50px" src="' + jsonBody.data.images[jsonBody.data.selectedImageIndex] + '" title="' + jsonBody.data.title + '" />');
  				}
  				html.push('</div>');
  				html.push('<div class="Title">' + jsonBody.data.title + '</div>');
  				html.push('<div class="Description">' + jsonBody.data.description + '</div>');
  				html.push('<div class="Source">' + Locale.getMsg('source') + ' : ' + jsonBody.data.link + '</div>');
  		        if ((statusUpdate.currentView === 'canvas') && (viewerId === ownerId)) {
  		        html.push('<div class="ActionContent">');
  		          html.push('<div id="ActionContentButton' + activityId + '" class="ActionContenButton" href="#action"><span></span></div>');
  		          html.push('<div id="MenuItemContainer' + activityId + '" style="position: absolute; display: block; min-width: 98px; top: 19px; right: 5px; visibility: hidden;" class="MenuItemContainer">');
  		            html.push('<div class="SubBlock">');
  		            if (ownerId === userId) {
  		              html.push('<div class="MenuItem">');
  		                html.push('<a id="Delete'+ activityId +'" class="ItemIcon DeleteIcon DefaultPageIcon" title="'+ Locale.getMsg('delete') +'" href="#delete">' + Locale.getMsg('delete') + '</a>');
  		              html.push('</div>');
  		            }
  		            html.push('</div>');
  		          html.push('</div>');
  		        html.push('</div>');
  		        }
  						html.push('<div style="clear: both; height: 0px;"><span></span></div>');
  						html.push('</div>');
  		  				html.push('<div class="NewsDate">' + prettyTime + '</div>');
  		  			if (statusUpdate.currentView === 'canvas') {
  		  				html.push('<a id="Comment' + activityId + '" href="#comment" style="color: #058ee6;">' + Locale.getMsg('comment') + '</a><span>|</span><a id="Like' + activityId + '" href="#like" style="color: #058ee6;">' + Locale.getMsg('like') + '</a>');
  		  			}
  		  			html.push('</div>')
  		  			html.push('<div class="ClearLeft"><span></span></div>');
  		  		html.push('</div>');
  		  		html.push('<div class="ListPeopleLikeBG">');
  		  			html.push('<div id="ListPeopleLike' + activityId + '" class="ListPeopleLike DisplayNone">');
  		  				html.push('<div class="ListPeopleContent"');
  		  					html.push('<div id="TitleLike' + activityId + '" class="Title"></div>');
  		  					html.push('<div style="display:none;" id="ListPeople' + activityId + '"></div>');
  		  					html.push('<div class="ClearLeft"><span></span></div>');
  		  				html.push('</div>');
  		  			html.push('</div>');
  		  		html.push('</div>');
  			html.push('</div>');
  			return html.join('');
  	}

  	var displayActivities = function(appendableRootId, moreId, activities, isOwner, displayName) {
  		if (!activities || activities.length === 0) {
  			if (isOwner) {
    			Util.getElementById(appendableRootId).innerHTML = '<div class= "Empty">' + Locale.getMsg('displayName_does_not_have_update', [displayName]) + '</div>';
    		} else {
    			Util.getElementById(appendableRootId).innerHTML = '<div class="Empty">' + Locale.getMsg('displayName_do_not_have_update', [displayName]) + '</div>';
    		}
    		return;
  		}
  		Util.getElementById(appendableRootId).innerHTML = ''; //resets
  		var activitiesLength = activities.length;
  		var displayActivityNum = activitiesLength;
  		var maxDisplayedActivity = 0;
  		if (dataMode === StatusUpdate.DataMode_OWNER_ONLY) {
  			maxDisplayedActivity = config.MAX_ACTIVITIES + config.MAX_ACTIVITIES * statusUpdate.ownerMoreClickedTimes;
  		} else if (dataMode == StatusUpdate.DataMode_FRIENDS_ONLY) {
  			maxDisplayedActivity = config.MAX_ACTIVITIES + config.MAX_ACTIVITIES * statusUpdate.friendsMoreClickedTimes;
  		} else {
  			maxDisplayedActivity = config.MAX_ACTIVITIES;
  		}
  		if (activitiesLength > maxDisplayedActivity) {
			displayActivityNum = maxDisplayedActivity;
			//TODO stores older activities
  		}
  		var uiMore = Util.getElementById(moreId);
	  	if (displayActivityNum < activitiesLength) {
			uiMore.style.display = 'block';
			if (isOwner) {
				uiMore.onclick = function() {
					statusUpdate.ownerMoreClickedTimes += 1;
					statusUpdate.updateOwnerActivities();
				}
			} else {
				uiMore.onclick = function() {
					statusUpdate.friendsMoreClickedTimes += 1;
					statusUpdate.updateFriendsActivities();
				}
			}
	  	} else {
			Util.getElementById(moreId).style.display = 'none';
	  	}
  		var useLightBackground = true;

	  	for (var i = 0; i < displayActivityNum; i++) {
	  		var html = '';
	  		var aDecoratorContainerClass = '';
	  		if (!useLightBackground) {
	  			aDecoratorContainerClass = 'ADecoratorContainerGray';
	  			useLightBackground = true;
	  		} else {
	  			aDecoratorContainerClass = 'ADecoratorContainer';
	  			useLightBackground = false;
	  		}
			var activityId =  activities[i].getField(opensocial.Activity.Field.ID);
	  		var body = activities[i].getField(opensocial.Activity.Field.BODY);
	  		var jsonBody = body.replace(/&#34;/g, '"');
	  		jsonBody = jsonBody.replace(/&#92;/g, "\\");
	  		jsonBody = gadgets.json.parse(jsonBody);
	  		if (jsonBody.data) { //process with json body, link display
				html = getLinkShareActivityBlock(activities[i], jsonBody);
	  		} else {//normal display
				html = getNormalActivityBlock(activities[i]);
			}
			if (html === null || html === '') {
				debug.error('html is null!!!');
				return;
			}
			var newEl = Util.addElement(appendableRootId, 'div', null, html);
	  		newEl.setAttribute('class', aDecoratorContainerClass);
        newEl.setAttribute('id', 'Activity' + activityId);
	  		if (statusUpdate.currentView === 'canvas') {
	  			Like.getLikeIds(activityId, Like.displayLike);
          statusUpdate.setActionContentButton(activityId);
	  			statusUpdate.setDeleteActivity(activityId);
          statusUpdate.setCommentActivity(activityId);
	  		}
	  	}
	 }

 	if (dataResponse.hadError()) {
  		debug.error('dataResponse had error, please refresh!!!');
  		debug.info(dataResponse);
  		miniMessage.createDismissibleMessage(Locale.getMsg('internal_error'));
  		return;
  	}
  	if (dataMode === StatusUpdate.DataMode_BOTH || dataMode === StatusUpdate.DataMode_OWNER_ONLY) {
  		this.ownerActivities = dataResponse.get('ownerActivities').getData()['activities'].asArray();
  		this.ownerActivities.sort(sortPostedTimeHandler);
  		displayActivities(config.ui.UI_OWNER_APPENDABLE_ROOT, config.ui.UI_OWNER_MORE, this.ownerActivities, true, this.owner.getDisplayName());
  	}

 	if (this.viewer.getId() === this.owner.getId()) {
 		if (dataMode === StatusUpdate.DataMode_BOTH || dataMode === StatusUpdate.DataMode_FRIENDS_ONLY) {
 			this.ownerFriendsActivities = dataResponse.get('ownerFriendsActivities').getData()['activities'].asArray();
 			this.ownerFriendsActivities.sort(sortPostedTimeHandler);
 			displayActivities(config.ui.UI_FRIENDS_APPENDABLE_ROOT, config.ui.UI_FRIENDS_MORE, this.ownerFriendsActivities, false, Locale.getMsg('owner_friends'));
 		}
 	}
	gadgets.window.adjustHeight();
}

/**
 * gets name by userId
 * if not found, return "" and warning
 * @param	userId
 */
eXo.social.StatusUpdate.prototype.getName = function(userId) {
  	if (userId === this.owner.getId()) {
  		return this.owner.getDisplayName();
  	}
    var person = this.ownerFriends.getById(userId);
  	if (person !== null) {
  		return person.getDisplayName();
  	}
  	debug.warn("Can not get name with userId: " + userId);
  	return "";
}

/**
 * gets avatar by userId
 * if not found, return default avatar url
 * @param	userId
 */
eXo.social.StatusUpdate.prototype.getAvatar = function(userId) {
	var avatarUrl = avatarUrl = eXo.social.StatusUpdate.config.path.ROOT_PATH + '/' + 'style/images/AvatarPeople.gif';
  	var person = null;
  	if (userId === this.owner.getId()) {
		person = this.owner;
	} else {
	 	person = this.ownerFriends.getById(userId);
	}
  	if (person !== null) {
  		newAvatarUrl = person.getField(opensocial.Person.Field.THUMBNAIL_URL);
  		if (newAvatarUrl) avatarUrl = newAvatarUrl;
  	}
  	return avatarUrl;
}
/**
 * checks if in a id list has viewerId
 * @param	id list
 * @return	boolean
 */
eXo.social.StatusUpdate.prototype.hasViewerId = function(ids) {
	for(var i=0, length=ids.length; i< length; i++) {
		if(ids[i] === this.viewer.getId()) return true;
	}
	return false;
}

/**
 * event handler for clicking on share
 * @param	el element triggered by this event
 */
eXo.social.StatusUpdate.prototype.share = function(el) {
	var Util = eXo.social.Util;
	var config = eXo.social.StatusUpdate.config;
	var currentView = this.currentView;
	debug.info('Share!!!');
	//debug.info(this.shareable);
	if (!this.shareable) {
		return;
	}
	var activityElement = Util.getElementById(config.ui.UI_COMPOSER_TEXTAREA);
	var restore = function() {
		this.shareable = false;
		if (currentView === 'home') {
			activityElement.style.color="#777777";
			activityElement.style.minHeight="12px";
			activityElement.value = statusUpdate.uiComposer.DEFAULT_INPUT;
		} else if (currentView === 'canvas') {
			activityElement.style.minHeight="20px";
			activityElement.style.color="#777777";
			activityElement.innerHTML = statusUpdate.uiComposer.DEFAULT_INPUT;
		}
	}

	if (this.currentView === 'home') {
		var activity = opensocial.newActivity({ 'title' : this.viewer.getDisplayName(), 'body' : activityElement.value});
		var statusUpdate = this;
		opensocial.requestCreateActivity(activity, "HIGH", function() {
			statusUpdate.refresh();}
		);
		restore();
		return;
	}
	// replace tag
	var reWhiteSpace = new RegExp(/^\s+$/);
	var text = activityElement.innerHTML;
	var content = text.replace(/<p>/gi, "<br>").replace(/<\/\p>/gi, "<br>");
	var activityContent = content.replace(/<br>/gi, " ");
	//for linkShare
	var linkShare = this.linkShare;
	var statusUpdate = this;
	if (linkShare.content !== null) {
		if (activityContent === this.uiComposer.DEFAULT_INPUT) {
	  		activityContent = '';
	  	}
	  	linkShare.save(activityContent, function() {
	  		statusUpdate.refresh();
	  	});
	  	restore();
	  	linkShare.displayAttach(eXo.social.LinkShare.config.ATTACH_OPTION_ID);
	  	return;
	} else {
		if (activityContent  === "" || reWhiteSpace.test(activityElement.innerHTML)) {
			return false;
		}
		if ((activityContent === this.uiComposer.DEFAULT_INPUT) && (activityElement.style.minHeight === "20px" || activityElement.style.minHeight === "12px")) return;
	}

	if ((currentView === 'canvas') && (this.shareable === false)) return;

	var activity = opensocial.newActivity({ 'title' : this.viewer.getDisplayName(), 'body' : activityContent});
	var statusUpdate = this;
	opensocial.requestCreateActivity(activity, "HIGH", function() {
		statusUpdate.updateOwnerActivities();}
	);
	restore();
}

eXo.social.StatusUpdate.sendPrivateMessage = function(userId) {
//	var sendMsg = this.sendPrivateMsg;
	var messageTitle = 'msg title';
	var messageBody = 'msg body';
	var id = opensocial.DataRequest.PersonId.OWNER;
	var recipient = [];
	recipient[0] = userId;
	var params = [];
	params[opensocial.Message.Field.TITLE] = messageTitle;
	params[opensocial.Message.Field.TYPE] = opensocial.Message.Type.EMAIL;
	var message = opensocial.newMessage(messageBody, params);
	
	opensocial.requestSendMessage(recipient, message);

//	function callback(data) {
//	  if (data.hadError()) {
//	    alert("There was a problem:" + data.getErrorCode());
//	  } else {
//	    output("Ok");
//	  }
//	};
}

gadgets.util.registerOnLoadHandler(eXo.social.StatusUpdate.main);