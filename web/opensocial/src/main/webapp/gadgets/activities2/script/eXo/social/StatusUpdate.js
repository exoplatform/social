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
	this.refreshTime = 5 * 60 * 1000 //in mili seconds; should improve by detecting user's activity on app. Can be changed by user's pref
	this.viewer = null;
	this.owner = null;
	this.ownerFriends = null;
	this.activities = null;
	this.linkShare = null;
	this.uiComposer = null;
	this.currentView = gadgets.views.getCurrentView().getName();
	this.currentFirstActivity = null;
	this.currentLastActivity = null;
	this.olderFirstParam = null; //the FIRST param to get older activities
	this.storedActivities = null; //stores new activites to notify users and then displays
	this.continueAddHeight = true; //if true, adjustHeight with added height
}

/**
 * static config object 
 */
eXo.social.StatusUpdate.config = {
	path : {
		ROOT_PATH : "http://localhost:8080/social/gadgets/activities2",
		SCRIPT_PATH : "http://localhost:8080/social/gadgets/activities2/script"
	},
	ui : {//dom id reference
		UI_COMPOSER_TEXTAREA : "UIComposerTextArea",
		UI_COMPOSER_EXTENSION : "UIComposerExtension",
		UI_COMPOSER_SHARE_BUTTON: 'UIComposerShareButton',
		UI_APPENDABLE_ROOT :"UIAppendableRoot",
		UI_MORE : "UIMore",
		HEIGHT_ADDED : 80, //fix bug iframe not fully resized
		MAX_ACTIVITIES : 5 //default displays 5 item only
	}
};

/**
 * called instead of gadgets.window.adjustHeight();
 * @param	opt_heightAdded if not provided, will use default config.ui.HEIGHT_ADDED
 * @static
 */
eXo.social.StatusUpdate.adjustHeight = function(opt_heightAdded) {
	var dm = gadgets.window.getViewportDimensions();
	var height = dm.height;
	if (!opt_heightAdded) {
		height += eXo.social.StatusUpdate.config.ui.HEIGHT_ADDED
	} else {
			height += opt_heightAdded;
	}
	debug.info('height: ' + height);
	window.setTimeout(function(height) {
		debug.info(height);	
		gadgets.window.adjustHeight(height);
	}, 1000);
}

/**
 * main entry point for app
 * @static 
 */
eXo.social.StatusUpdate.main = function() {
	var statusUpdate = new eXo.social.StatusUpdate();
	statusUpdate.init();
	
	//create and use linkShare object
	var linkShare = eXo.social.linkShare = new eXo.social.LinkShare();
	//set ref
	statusUpdate.linkShare = linkShare;
	eXo.social.Like.ref.statusUpdate = statusUpdate;
	//linkShare.init();
}

/**
 * Init
 */
eXo.social.StatusUpdate.prototype.init = function() {
	//alias imports
	var Locale = eXo.social.Locale;
	var UIComposer = eXo.social.UIComposer;
	var Util = eXo.social.Util;
	var StatusUpdate = eXo.social.StatusUpdate;
	var config = StatusUpdate.config;
	var statusUpdate = this;
	
	var uiComposerTextArea = Util.getElementById(config.ui.UI_COMPOSER_TEXTAREA);
	var uiComposer = new UIComposer(uiComposerTextArea);
	this.uiComposer = uiComposer;
	uiComposer.statusUpdate = this;
	if (!uiComposerTextArea) {
		debug.warn("uiComposerTextArea is null!");
		return;
	}
	//event handler attach
	Util.addEventListener(uiComposerTextArea, 'focus', function() {
		uiComposer.focusInput(this);
	}, false);
	Util.addEventListener(uiComposerTextArea, 'blur', function() {
		uiComposer.blurInput(this);
	}, false);
	var uiComposerShareButton = Util.getElementById(config.ui.UI_COMPOSER_SHARE_BUTTON);
	if (!uiComposerShareButton) {
		debug.warn('uiComposerShareButton is null');
		return;
	}
	var statusUpdate = this;
	Util.addEventListener(uiComposerShareButton, 'click', function() {
		statusUpdate.share(this);
	}, false);
	//run to set viewer, owner, owner's friends
	(function() {
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
			if (response.hadError()) {
				debug.error('Can not get viewer, owner, ownerFriends!');
				debug.info(response);
				return;
			}
			statusUpdate.viewer = response.get('viewer').getData();
			statusUpdate.owner = response.get('owner').getData();
			statusUpdate.ownerFriends = response.get('ownerFriends').getData();
			//BUG: can not use person.isViewer() or person.isOwner()
			//debug.debug(this.viewer.isViewer());
			//debug.debug(this.owner.isOwner());
			statusUpdate.refresh();
		}
	})();
	//refresh after a specific of time
	//window.setInterval(function() {
	//	statusUpdate.refresh();
	//}, this.refreshTime);
}

///**
// * gets activities 
// */
//eXo.social.StatusUpdate.prototype.getActivities = function() {
//
//}

eXo.social.StatusUpdate.prototype.refresh = function() {
	var StatusUpdate = eXo.social.StatusUpdate;
	debug.info("Refresh!!!!");
  	//this.MAX = 2 * (this.more + 1); 
  	//Create request for getting owner's and ownerFriends' activities.
  	var req = opensocial.newDataRequest();	

	var opts_act = {};
 	opts_act[opensocial.DataRequest.ActivityRequestFields.FIRST] = 0;
  	opts_act[opensocial.DataRequest.ActivityRequestFields.MAX] = StatusUpdate.config.ui.MAX_ACTIVITIES;
  	req.add(req.newFetchActivitiesRequest(opensocial.DataRequest.PersonId.OWNER, opts_act), 'ownerActivities');
  	req.add(req.newFetchActivitiesRequest(opensocial.DataRequest.Group.OWNER_FRIENDS, opts_act), 'ownerFriendsActivities');
  	var statusUpdate = this;
  	req.send(function(res) {
  		statusUpdate.handleActivities(res);
  	});
}

/**
 * Automatically gets new activity,
 * if any newer activity than currentFirstActivity => insert activity before currentFirstActivity element
 * Notifies user the number of new activities.
 * After clicked on notification, updates
 */
eXo.social.StatusUpdate.prototype.update = function() {
	
}

/**
 * gets all activities, stores all older activity than currentLastActitivy
 * when user clicks on More, display activities with specified batch size (config)
 */
eXo.social.StatusUpdate.prototype.readMore = function() {
	
}

/**
 * callback hander for activities 
 */
eXo.social.StatusUpdate.prototype.handleActivities = function(dataResponse) {
	var Locale = eXo.social.Locale;
	var Util = eXo.social.Util;
	var Like = eXo.social.Like;
	
	//private methods
  	var  sortPostedTimeHandler = function(activity1, activity2) {	
		if (activity1.getField('postedTime') > activity2.getField('postedTime')) {
			return -1;
		} else if (activity1.getField('postedTime') < activity2.getField('postedTime')) {
			return 1;
		}
		return 0;
  	}
  	
 	if (dataResponse.hadError()) {
  		debug.warn('dataResponse had error, please refresh!!!');
  		debug.info(dataResponse);
  		//TODO informs user, use mini message
  		return;
  	}
  	
  	var ownerActivityTitle = Util.getElementById('OwnerActivityTitle');
  	if (!ownerActivityTitle) {
  		debug.warn('ownerActivityTitle is null!!!');
  		return;
  	}
  	ownerActivityTitle.innerHTML = Locale.getMsg('activities_of_ownerDisplayName', [this.owner.getDisplayName()]);
  	
  	var ownerActivities = dataResponse.get('ownerActivities').getData()['activities'].asArray();
 	this.activities = ownerActivities;
 	var totalActivities = dataResponse.get('ownerActivities').getData()['activities'].getTotalSize();
 	if (this.viewer.getId() === this.owner.getId()) {
 		var ownerFriendsActivities = dataResponse.get('ownerFriendsActivities').getData()['activities'].asArray();
 		this.activities.concat(ownerFriendsActivities);
 		totalActivities += ownerFriendsActivities.length;
 	}
  	this.activities.sort(sortPostedTimeHandler);
  	
  	var config = eXo.social.StatusUpdate.config;
  	if (!this.activities || this.activities.length === 0) {
    	Util.getElementById(config.ui.UI_APPENDABLE_ROOT).innerHTML = '<div id="NoActivity" class= "Empty">' + Locale.getMsg('ownerDisplayName_does_not_have_update', [this.owner.getDisplayName()]) + '</div>';
    	return;
  	}
  	Util.getElementById(config.ui.UI_APPENDABLE_ROOT).innerHTML = ''; //resets
  	var activitiesLength = this.activities.length;
  	var displayActivityNum = activitiesLength;
  	if (activitiesLength > config.ui.MAX_ACTIVITIES) {
		displayActivityNum = config.ui.MAX_ACTIVITIES;
		//TODO stores older activities
  	}
  
//  	if (displayActivityNum < totalActivities) {
//		Util.getElementById(config.ui.UI_MORE).style.display = 'block';
//  	} else {
//		Util.getElementById(config.ui.UI_MORE).style.display = 'none';
//  	}
  	var useLightBackground = true;
  	var UI_APPENDABLE_ROOT= eXo.social.StatusUpdate.config.ui.UI_APPENDABLE_ROOT;
  	for (var i = 0; i < displayActivityNum; i++) {
  		var html = [];
  		var aDecoratorContainerClass = 'ADecoratorContainer';
  		if (!useLightBackground) {
  			aDecoratorContainerClass = 'ADecoratorContainerGray';
  			useLightBackground = true;
  		} else {
  			aDecoratorContainerClass = 'ADecoratorContainer';
  			useLightBackground = false;
  		}
  		var userId = this.activities[i].getField(opensocial.Activity.Field.USER_ID);
  		var url = this.activities[i].getField(opensocial.Activity.Field.URL);
		var activityId =  this.activities[i].getField(opensocial.Activity.Field.ID);
		var viewerId = this.viewer.getId();
  		var title = this.activities[i].getField(opensocial.Activity.Field.TITLE);
  		var body = this.activities[i].getField(opensocial.Activity.Field.BODY);
  		var userName = this.getName(userId);
  		var avatarUrl = this.getAvatar(userId);
  		var prettyTime = Util.toPrettyTime(new Date(this.activities[i].getField('postedTime')));
  		var jsonBody = body.replace(/&#34;/g, '"');
  		jsonBody = jsonBody.replace(/&#92;/g, "\\");
  		jsonBody = gadgets.json.parse(jsonBody);
  		if (jsonBody.data) { //process with json body, link display
  			var data = jsonBody.data;
  			html.push('<div class="ActivitiesContent"');
  				html.push('<div class="MiniAvatarSpaceBG">');
	    			html.push('<img src="' + avatarUrl + '" width="60" height="60" />'); 
	   			html.push('</div>');
	    		html.push('<div class="UserName">');
	    			html.push('<a href="#">' + userName + '</a>');
	    		html.push('</div>');
	    		html.push('<div class="UserStatus">');
  				if (data.comment) {
  					html.push(data.comment);
  				}
  				html.push('</div>');
  				html.push('<div class="Thumbnail">');
  				if (data.noThumbnail === false) {
  					html.push('<img width="70px" src="' + data.images[data.selectedImageIndex] + '" title="' + data.title + '" />');
  				}
  				html.push('</div>');
  				html.push('<div class="Title">' + data.title + '</div>');
  				html.push('<div class="Description">' + data.description + '</div>');
  				html.push('<div class="Source">' + Locale.getMsg('source') + ' : ' + data.link + '</div>')
  			html.push('</div>')
  		} else {//normal display
  			//html.push('<div class="' + aDecoratorContainerClass + '">');
  				html.push('<div class="ActivitiesContent">');
  					html.push('<a href="#" class="AvatarPeopleBG">');
  						html.push('<img height="47px" width="47px" src="' + avatarUrl + '" />');
  					html.push('</a>');
  					html.push('<div class="Content">');
  						html.push('<div class="TitleItem" href="#">' + title + '</div>');
  						html.push('<div class="Content">' + body + '</div>');
  						html.push('<div class="NewsDate">' + prettyTime + '</div>');
  					if (this.currentView === 'canvas') {
  						html.push('<a href="#comment" style="color: #058ee6;">' + Locale.getMsg('comment') + '</a><span>|</span><a id="Like' + activityId + '" href="#like" style="color: #058ee6;">' + Locale.getMsg('like') + '</a>');
  					}
  					html.push('</div>')
  					html.push('<div class="ClearLeft"><span></span></div>');
  				html.push('</div>');
  				html.push('<div class="ListPeopleLikeBG">');
  					html.push('<div id="ListPeopleLike' + activityId + '" class="ListPeopleLike DisplayNone">');
  						html.push('<div class="ListPeopleContent"');
  							html.push('<div id="TitleLike' + activityId + '" class="Title"></div>');
  							html.push('<div class="DisplayNone" id="ListPeople' + activityId + '"></div>');
  							html.push('<div class="ClearLeft"><span></span></div>');
  						html.push('</div>');
  					html.push('</div>');
  				html.push('</div>');
  			//html.push('</div>');
  			var newEl = Util.addElement(UI_APPENDABLE_ROOT, 'div', null, html.join(''));
  			newEl.setAttribute('class', aDecoratorContainerClass);
  			Like.getLikeIds(activityId, Like.displayLike);
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
    var person = this.ownerContacts.getById(userId);
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
	 	person = this.ownerContacts.getById(id);
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
		if(ids[i] === this.owner.getId()) return true;
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
	debug.info(this.shareable);
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
		statusUpdate.refresh();}
	);
	restore();
}

gadgets.util.registerOnLoadHandler(eXo.social.StatusUpdate.main); 