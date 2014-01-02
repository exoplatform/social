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
  this.viewerFriends = null;
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
  this.isOwnerActivityShown = false;
  this.contentForAdjustHeight;
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
  HOST: null,
  HOST_SCHEMA: null,
  HOST_NAME: null,
  HOST_PORT: null,
  PORTAL_NAME: null,
  REST_CONTEXT_NAME: null,
  ACTIVITIES_REST_URL: "${HOST_SCHEMA}://${HOST}/${REST_CONTEXT_NAME}/${portalName}/social/activities",
  IDENTITY_REST_URL : "${HOST_SCHEMA}://${HOST}/${REST_CONTEXT_NAME}/${portalName}/social/identity",
  path : {
    ROOT_PATH : "${HOST_SCHEMA}://${HOST}/social/gadgets/activities2",
    SCRIPT_PATH : "${HOST_SCHEMA}://${HOST}/social/gadgets/activities2/script"
  },
  ui : {//dom id reference
    UI_STATUS_LOADING: "StatusLoading",
    UI_STATUS_UPDATE: "StatusUpdate",
    UI_MY_STATUS_INPUT: "UIMyStatusInput",
    UI_FRIENDS_ACTIVITIES: "UIFriendsActivities",
    UI_OWNER_AVATAR: "UIOwnerAvatar",
    IMG_OWNER_AVATAR: "ImgOwnerAvatar",
    UI_COMPOSER_TEXTAREA : "UIComposerTextArea",
    UI_COMPOSER_EXTENSION : "UIComposerExtension",
    UI_COMPOSER_SHARE_BUTTON: "UIComposerShareButton",
    UI_OWNER_APPENDABLE_ROOT :"UIOwnerAppendableRoot",
    UI_OWNER_MORE : "UIOwnerMore",
    UI_FRIENDS_APPENDABLE_ROOT: "UIFriendsAppendableRoot",
    UI_FRIENDS_MORE: "UIFriendsMore",
    UI_OWNER_ACTIVITIES_SHOW_HIDE: "UIOwnerActivitiesShowHide"
  },
  BATCH_SIZE: 10, //batch size to get activities
  MAX_ACTIVITIES : 5, //default displays 5 item only
  REFRESH_TIME : 3 * 60 * 1000 //in miliseconds; should improve by detecting user's activity on app. Can be changed by user's pref
};
//see SOC-654
eXo.social.StatusUpdate.allowedTags = ['b', 'i', 'a', 'span', 'em', 'strong', 'p', 'ol', 'ul', 'li', 'br'];


eXo.social.StatusUpdate.configEnvironment = function() {
  var StatusUpdate = eXo.social.StatusUpdate;
  var config = eXo.social.StatusUpdate.config;
  //currently HOST = 'http://localhost:8080' = HOST_NAME + HOST_PORT
  var spliter = config.HOST.split(":");
  config.HOST_SCHEMA = spliter[0];
  config.HOST_NAME = spliter[1].replace('//','');
  config.HOST_PORT = spliter[2];
  var host;
  if (config.HOST_PORT) {
    host = config.HOST_NAME + ":" + config.HOST_PORT;
  } else {
    host = config.HOST_NAME;
  }
  if (!(config.HOST_SCHEMA && host && config.PORTAL_NAME && config.REST_CONTEXT_NAME)) {
    //problem
    //alert('error: can not get right environment!');
  }

  config.ACTIVITIES_REST_URL = config.ACTIVITIES_REST_URL.replace('${HOST_SCHEMA}', config.HOST_SCHEMA).replace('${HOST}', host).replace('${REST_CONTEXT_NAME}', config.REST_CONTEXT_NAME).replace('${portalName}', config.PORTAL_NAME);
  config.IDENTITY_REST_URL = config.IDENTITY_REST_URL.replace('${HOST_SCHEMA}', config.HOST_SCHEMA).replace('${HOST}', host).replace('${REST_CONTEXT_NAME}', config.REST_CONTEXT_NAME).replace('${portalName}', config.PORTAL_NAME);
  config.path.ROOT_PATH = config.path.ROOT_PATH.replace('${HOST_SCHEMA}', config.HOST_SCHEMA).replace('${HOST}', host);
  config.path.SCRIPT_PATH = config.path.SCRIPT_PATH.replace('${HOST_SCHEMA}', config.HOST_SCHEMA).replace('${HOST}', host);

  eXo.social.Comment.config.URL_COMMENTS = eXo.social.Comment.config.URL_COMMENTS.replace('${HOST_SCHEMA}', config.HOST_SCHEMA).replace('${HOST}', host).replace('${REST_CONTEXT_NAME}', config.REST_CONTEXT_NAME).replace('${portalName}', config.PORTAL_NAME);
  eXo.social.Like.config.REST_LIKE = eXo.social.Like.config.REST_LIKE.replace('${HOST_SCHEMA}', config.HOST_SCHEMA).replace('${HOST}', host).replace('${REST_CONTEXT_NAME}', config.REST_CONTEXT_NAME).replace('${portalName}', config.PORTAL_NAME);
  eXo.social.LinkShare.config.LINKSHARE_REST_URL = eXo.social.LinkShare.config.LINKSHARE_REST_URL.replace('${HOST_SCHEMA}', config.HOST_SCHEMA).replace('${HOST}', host).replace('${REST_CONTEXT_NAME}', config.REST_CONTEXT_NAME);
}

/**
 * main entry point for app
 * @static
 */
eXo.social.StatusUpdate.main = function() {
  var Util = eXo.social.Util;
  var SocialUtil = eXo.social.SocialUtil;
  Util.hideElement(eXo.social.StatusUpdate.config.ui.UI_STATUS_UPDATE);
  var statusUpdate = new eXo.social.StatusUpdate();
  
  //get full render from start BUG #SOC-375
  statusUpdate.contentForAdjustHeight = document.getElementById("ActivitiesContainer");
  SocialUtil.adjustHeight(statusUpdate.contentForAdjustHeight);
  
  statusUpdate.init();
  //create and use linkShare object
  var linkShare = eXo.social.linkShare = new eXo.social.LinkShare();
  //set ref
  statusUpdate.linkShare = linkShare;
  eXo.social.LinkShare.ref.statusUpdate = statusUpdate;
  eXo.social.Like.ref.statusUpdate = statusUpdate;
  eXo.social.Comment.ref.statusUpdate = statusUpdate;
  if (statusUpdate.currentView === 'canvas') {
    linkShare.init();
  }
}

/**
 * Inits
 */
eXo.social.StatusUpdate.prototype.init = function() {
  //alias imports
  var Locale = eXo.social.Locale;
  var UIComposer = eXo.social.UIComposer;
  var Util = eXo.social.Util;
  var SocialUtil = eXo.social.SocialUtil;
  var StatusUpdate = eXo.social.StatusUpdate;
  var config = StatusUpdate.config;
  var statusUpdate = this;
  var miniMessage = statusUpdate.miniMessage;
  var uiComposerTextArea = Util.getElementById(config.ui.UI_COMPOSER_TEXTAREA);
  var uiComposer = new UIComposer(uiComposerTextArea);
  var uiOwnerActivitiesShowHide = Util.getElementById(config.ui.UI_OWNER_ACTIVITIES_SHOW_HIDE);

  this.uiComposer = uiComposer;
  uiComposer.statusUpdate = this;
  if (!uiComposerTextArea) {
    debug.error("uiComposerTextArea is null!");
    return;
  }

  Util.showElement(config.ui.UI_OWNER_APPENDABLE_ROOT);

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

  Util.addEventListener(uiOwnerActivitiesShowHide, 'click', function(evt) {
    if (statusUpdate.isOwnerActivityShown) {
      statusUpdate.isOwnerActivityShown = false;
      uiOwnerActivitiesShowHide.innerHTML='<div class="ExpandAllActivities">' + Locale.getMsg('expand_all_activities') + '</div>';
      Util.showElement(config.ui.UI_OWNER_ACTIVITIES_SHOW_HIDE);
      Util.hideElement(config.ui.UI_OWNER_APPENDABLE_ROOT);
    } else {
    	statusUpdate.refresh();
      statusUpdate.isOwnerActivityShown = true;
      uiOwnerActivitiesShowHide.innerHTML='<div class="CollapseAllActivities">' + Locale.getMsg('collapse_all_activities') + '</div>';
      Util.showElement(config.ui.UI_OWNER_ACTIVITIES_SHOW_HIDE);
      Util.showElement(config.ui.UI_OWNER_APPENDABLE_ROOT);
    }

    SocialUtil.adjustHeight(statusUpdate.contentForAdjustHeight);
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
          statusUpdate.viewerFriends = statusUpdate.ownerFriends;
          var req = opensocial.newDataRequest();
          var opts = {};
          opts[opensocial.DataRequest.PeopleRequestFields.PROFILE_DETAILS] =
              [opensocial.Person.Field.ID,
               opensocial.Person.Field.NAME,
               opensocial.Person.Field.PROFILE_URL,
               opensocial.Person.Field.THUMBNAIL_URL];
          req.add(req.newFetchPersonRequest(ownerId, opts), 'owner');
          var ownerFriendsSpec = opensocial.newIdSpec({"userId":ownerId, "groupId":"FRIENDS"});
          req.add(req.newFetchPeopleRequest(ownerFriendsSpec, opts), 'ownerFriends');
          req.send(function(res) {
            if (res.hadError()) {
              debug.warn("Can not re-get owner!");
              debug.info(res);
              miniMessage.createDismissibleMessage(Locale.getMsg('internal_error'));
              return;
            }
            statusUpdate.owner = res.get('owner').getData();
            statusUpdate.ownerFriends = res.get('ownerFriends').getData();
            debug.info(statusUpdate.ownerFriends);
            //hides text input + friends's activities
            Util.hideElement(config.ui.UI_MY_STATUS_INPUT);
            Util.hideElement(config.ui.UI_FRIENDS_ACTIVITIES);
            Util.hideElement(config.ui.UI_STATUS_LOADING);
            Util.showElement(config.ui.UI_STATUS_UPDATE);
            statusUpdate.refresh();
          });
        } else {
          Util.hideElement(config.ui.UI_STATUS_LOADING);
          Util.showElement(config.ui.UI_STATUS_UPDATE);
          statusUpdate.refresh();
        }
      }
    }

    var req = opensocial.newDataRequest();
    var opts = {};
    opts[opensocial.DataRequest.PeopleRequestFields.FIRST] =  0;
    //opts[opensocial.DataRequest.PeopleRequestFields.MAX] = 40; //why 40?
    opts[opensocial.DataRequest.PeopleRequestFields.PROFILE_DETAILS] =
        [opensocial.Person.Field.ID,
         opensocial.Person.Field.NAME,
         opensocial.Person.Field.PROFILE_URL,
         opensocial.Person.Field.THUMBNAIL_URL,
         "portalName", //TODO hoatle tricky, need exo-environment feature
         "restContext",
         "host"];
    req.add(req.newFetchPersonRequest(opensocial.IdSpec.PersonId.VIEWER, opts), 'viewer');
    req.add(req.newFetchPersonRequest(opensocial.IdSpec.PersonId.OWNER, opts), 'owner');
    var ownerFriendsSpec = opensocial.newIdSpec({"userId":"OWNER", "groupId":"FRIENDS"});
    req.add(req.newFetchPeopleRequest(ownerFriendsSpec, opts), 'ownerFriends');
    req.send(handler);
    function handler(response) {
      var miniMessage = statusUpdate.miniMessage;
      if (response.hadError()) {
        debug.warn('Can not get viewer, owner, ownerFriends!');
        debug.info(response);
        miniMessage.createDismissibleMessage(Locale.getMsg('internal_error'));
        SocialUtil.adjustHeight(statusUpdate.contentForAdjustHeight);
        return;
      }
      statusUpdate.viewer = response.get('viewer').getData();
      statusUpdate.owner = response.get('owner').getData();
      //update context environment
      StatusUpdate.config.HOST = statusUpdate.owner.getField('hostName');
      StatusUpdate.config.PORTAL_NAME = statusUpdate.owner.getField('portalName');
      StatusUpdate.config.REST_CONTEXT_NAME = statusUpdate.owner.getField('restContextName');
      StatusUpdate.configEnvironment();
      statusUpdate.ownerFriends = response.get('ownerFriends').getData();

      var username = getUsernameFromUrl();
      if (username !== null) {
        var url = config.IDENTITY_REST_URL + "/" + username + "/id/show.json";
        debug.info(url);
        Util.makeRequest(url, fixOwnerHandler);
      } else {
        Util.hideElement(config.ui.UI_STATUS_LOADING);
        Util.showElement(config.ui.UI_STATUS_UPDATE);
        statusUpdate.refresh();
      }
      //BUG: can not use person.isViewer() or person.isOwner()
      //debug.debug(this.viewer.isViewer());
      //debug.debug(this.owner.isOwner());
    }
  })();
  //refresh after a specific of time
  //window.setInterval(function() {
  // statusUpdate.refresh();
  //}, this.refreshTime);
}

eXo.social.StatusUpdate.prototype.refresh = function() {
  debug.info("Refresh!!!!");
  var Util = eXo.social.Util;
  var Locale = eXo.social.Locale;
  var StatusUpdate = eXo.social.StatusUpdate;
  var config = StatusUpdate.config;
  var statusUpdate = this;
  var miniMessage = statusUpdate.miniMessage;
  //updates owner avatar
  var imgOwnerAvatar = Util.getElementById(config.ui.IMG_OWNER_AVATAR);
  if (!imgOwnerAvatar) {
    debug.error('imgOwnerAvatar is null!');
    miniMessage.createDismissibleMessage(Locale.getMsg('internal_error'));
    return;
  }
  imgOwnerAvatar.src = statusUpdate.getAvatar(statusUpdate.owner.getId(), true);
  imgOwnerAvatar.alt = statusUpdate.getName(statusUpdate.owner.getId());
  //set owner href
  var ownerProfileUrl = statusUpdate.owner.getField(opensocial.Person.Field.PROFILE_URL);
  var ownerAvatar = Util.getElementById(config.ui.UI_OWNER_AVATAR);
  ownerAvatar.setAttribute('href', ownerProfileUrl);
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
  var ownerActivitiesSpec = opensocial.newIdSpec({"userId":statusUpdate.owner.getId()});
  req.add(req.newFetchActivitiesRequest(ownerActivitiesSpec, opts_act), 'ownerActivities');
  if (this.viewer.getId() === this.owner.getId()) {
    var ownerFriendsActivitiesSpec = opensocial.newIdSpec({"userId":"OWNER", "groupId":"FRIENDS"});
    req.add(req.newFetchActivitiesRequest(ownerFriendsActivitiesSpec, opts_act), 'ownerFriendsActivities');
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

  Util.addEventListener(el, 'click', function(evt) {
    if (confirm(Locale.getMsg('are_you_sure_to_delete_this_activity'))) {
      statusUpdate.deleteActivity(activityId);
    }
  }, false);
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
    Util.addEventListener(el, ['click', 'mouseover', 'focus'], function() {
      display(activityId);
    }, false);

    Util.addEventListener(el, ['mouseout', 'blur'], function() {
      hide(activityId);
    }, false);

    Util.addEventListener(menuItemContainer, ["mouseover", "focus"], function() {
      display(activityId);
    }, false);

    Util.addEventListener(menuItemContainer, ["mouseout", "blur"], function() {
      hide(activityId);
    }, false);
  }
  toggle(activityId);
}

/**
 * delete activity by id
 */
eXo.social.StatusUpdate.prototype.deleteActivity = function(activityId) {
  var Util = eXo.social.Util,
      Locale = eXo.social.Locale;
      url = eXo.social.StatusUpdate.config.ACTIVITIES_REST_URL + '/destroy/' + activityId + '.json',
      statusUpdate = this;
  eXo.social.Util.makeRequest(url, function(res) {
    if (res.rc === 404) {
      Util.removeElementById('Activity' + activityId);
      //check if delete all => displays empty message
      var rootEl = Util.getElementById(eXo.social.StatusUpdate.config.ui.UI_OWNER_APPENDABLE_ROOT);
      if (!rootEl.hasChildNodes()) {
        eXo.social.StatusUpdate.isOwnerActivityShown = false;
        if (statusUpdate.owner.getId() === statusUpdate.viewer.getId()) {
          Util.hideElement(eXo.social.StatusUpdate.config.ui.UI_OWNER_ACTIVITIES_SHOW_HIDE);
        }
        rootEl.innerHTML = '<div class= "Empty">' + Locale.getMsg('displayName_does_not_have_update', [statusUpdate.owner.getDisplayName()]) + '</div>';
      }
      return;
    }
    if (res.data) {
      Util.removeElementById('Activity' + res.data.id);
      //check if delete all => displays empty message
      var rootEl = Util.getElementById(eXo.social.StatusUpdate.config.ui.UI_OWNER_APPENDABLE_ROOT);
      if (!rootEl.hasChildNodes()) {
        eXo.social.StatusUpdate.isOwnerActivityShown = false;
        Util.hideElement(eXo.social.StatusUpdate.config.ui.UI_OWNER_ACTIVITIES_SHOW_HIDE);
        rootEl.innerHTML = '<div class= "Empty">' + Locale.getMsg('displayName_does_not_have_update', [statusUpdate.owner.getDisplayName()]) + '</div>';
      }
    } else {
      //TODO informs about the error
      //alert('Problem when deleting the activity!');
    }
  }, null, gadgets.io.MethodType.POST, gadgets.io.ContentType.JSON, null);
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
  var ownerActivitiesSpec = opensocial.newIdSpec({"userId":statusUpdate.owner.getId()});
  req.add(req.newFetchActivitiesRequest(ownerActivitiesSpec, params), 'ownerActivities');
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
  var ownerFriendsSpec = opensocial.newIdSpec({"userId":"OWNER", "groupId":"FRIENDS"});
  req.add(req.newFetchActivitiesRequest(ownerFriendsSpec, params), 'ownerFriendsActivities');
  req.send(function(res) {
    statusUpdate.handleActivities(res, StatusUpdate.DataMode_FRIENDS_ONLY);
  });
}


/**
 * callback hander for activities
 * @param	dataResponse
 * @param	opt_dataMode - can be StatusUpdate.DataMode_BOTH
 *								  StatusUpdate.DataMode_OWNER_ONLY
 *                  StatusUpdate.DataMode_FRIENDS_ONLY
 */
eXo.social.StatusUpdate.prototype.handleActivities = function(dataResponse, dataMode) {
  var Locale = eXo.social.Locale;
  var Util = eXo.social.Util;
  var SocialUtil = eXo.social.SocialUtil;
  var Like = eXo.social.Like;
  var Comment = eXo.social.Comment;
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
    var profileUrl, userId, url, activityId, viewerId, ownerId, title, body, userName, avatarUrl, prettyTime;
    var displayActivitiesNext = false;
    /**
     * Check if the current viewer and the owner of activity is friend or not.
     * Using this result for allowing one person comment or like the activity.
     *  - If is friend then can comment or like;
     *  - If is not friend can view activity only.
     *
     * @param viewerId
     * @return
     */
    var isFriend = function(viewerId) {
      statusUpdate.ownerFriends.each(function(person) {
        if (person.getId() === viewerId) {
          return true;
        }
      });
      return false;
    }
    /**
     * Checks if the current viewer is the application owner
     *
     * @param viewerId
     * @return boolean
     */
    var isOwner = function(viewerId) {
      return (statusUpdate.owner.getId() === viewerId);
    }

    /**
     * gets html block of normal activity
     * @param	activity object
     * @param	isOwnerActivity to decide some actions displayed
     * @return html
     */
    var getNormalActivityBlock = function(activity, isOwnerActivity) {
    	var templateParams = activity.getField(opensocial.Activity.Field.TEMPLATE_PARAMS);

      if (!activity) {
        debug.error('getNormalActivityBlock: activity is null.');
        miniMessage.createDismissibleMessage(Locale.getMsg('internal_error'));
        return '';
      }
      url = activity.getField(opensocial.Activity.Field.URL);
      activityId =  activity.getField(opensocial.Activity.Field.ID);
      viewerId = statusUpdate.viewer.getId();
      ownerId = statusUpdate.owner.getId();
      title = activity.getField(opensocial.Activity.Field.TITLE);
      body = activity.getField(opensocial.Activity.Field.BODY);
      if (title === null) {
        title = '';
      }
      var stripedTitle = Util.stripHtml(StatusUpdate.allowedTags, title);
      if (stripedTitle !== '') {
        title = stripedTitle;
      }
      avatarUrl = statusUpdate.getAvatar(userId);
      prettyTime = Util.toPrettyTime(new Date(activity.getField('postedTime')));
      var html = [];
      html.push('<div class="ActivitiesContent">');
        html.push('<a href="' + profileUrl + '" target="_parent" title="' + userName + '" class="AvatarPeopleBG">');
          html.push('<img height="47px" width="47px" src="' + avatarUrl + '" title="' + userName + '" alt="' + userName + '" />');
        html.push('</a>');
		    if (templateParams == undefined) {
		      html.push('<div class="Content">');
		    } else {
		    	html.push('<div class="LinkShareContent">');
		    }
          html.push('<div class="TitleContent" style="height: 24px;">');
            html.push('<div class="UserName"><a class="Link" href="' + profileUrl + '" target="_parent" title="' + userName + '">' + userName + '</a></div>');
          if (isOwnerActivity) {
            html.push(getActionContentBlock());
          }
            html.push('<div style="clear: both; height: 0px;"><span></span></div>');
          html.push('</div>');

          if ((templateParams !== undefined) && (templateParams.title !== undefined)) {
          	var title = templateParams.title;
          	var link = templateParams.link;
            var descripts = templateParams.description;
            html.push('<div><a class="ColorLinkShared" href="' + link + '" target="_blank" title="' + title + '">' + title + '</a></div>');          	
            html.push('<div class="Description">' + descripts + '</div>');
           	html.push('<div>' + Locale.getMsg('source') + ' : <a href="' + link + '" target="_blank">' + link + '</a></div>');
          } else {
            html.push('<div class="ContentArea">' + title + '</div>');            	
          }
          
          html.push('<div class="NewsDate">' + prettyTime + '  ' + getCommentLikeBlock() + '</div>');
//          html.push(getCommentLikeBlock());
        html.push('</div>');
        html.push('<div class="ClearLeft"><span></span></div>');
      html.push('</div>');
      html.push(getPeopleLikeBlock());
      html.push(getCommentListBlock());
      html.push(getCommentFormBlock());
      return html.join('');
    }

    /**
     * Gets html block of link share activity.
     *
     * @param  activity
     * @param  jsonObject
     * @param  isOwnerActivity to decide some actions displayed
     */
    var getLinkShareActivityBlock = function(activity, jsonObject, isOwnerActivity) {
      if (!activity || !jsonObject) {
        debug.error('getLinkShareActivityBlock: activity or data is null.');
        debug.info(activity);
        debug.info(jsonObject);
        miniMessage.createDismissibleMessage(Locale.getMsg('internal_error'));
        return '';
      }
      url = activity.getField(opensocial.Activity.Field.URL);
      activityId =  activity.getField(opensocial.Activity.Field.ID);
      viewerId = statusUpdate.viewer.getId();
      ownerId = statusUpdate.owner.getId();
      title = activity.getField(opensocial.Activity.Field.TITLE);
      avatarUrl = statusUpdate.getAvatar(userId);
      prettyTime = Util.toPrettyTime(new Date(activity.getField('postedTime')));
      var html = [];
      html.push('<div class="ActivitiesContent">');
        html.push('<a href="#" class="AvatarPeopleBG">');
          html.push('<img height="47px" width="47px" src="' + avatarUrl + '" title="' + userName + '" alt="' + userName + '" />');
        html.push('</a>');
        html.push('<div class="LinkShareContent">');
          html.push('<div class="TitleContent" style="height: 24px;">');
            html.push('<div class="UserName">');
              html.push('<a class="Link" href="' + profileUrl + '" target="_parent" title="' + userName + '">' + userName + '</a>');
            html.push('</div>');
          if (isOwnerActivity) {
            html.push(getActionContentBlock());
          }
            html.push('<div style="clear: both; height: 0px;"><span></span></div>');
          html.push('</div>');
        html.push('<div class="ContentArea">');
        if (jsonObject.comment) {
          var comment = Util.stripHtml(StatusUpdate.allowedTags, jsonObject.comment);
          if (comment) {
            jsonObject.comment = comment;
          }
          html.push(jsonObject.comment);
        } 
        if (!jsonObject.title) {
          jsonObject.title = jsonObject.link;
        }
        var description;
        if (jsonObject.description) {
          description = jsonObject.description;
        } 
        html.push('</div>');
        html.push('<div class="LinkShare">')
          html.push('<div class="Thumbnail">');
          if (jsonObject.image) {
            html.push('<img width="100px" src="' + jsonObject.image + '" title="' + jsonObject.title + '" alt="' + jsonObject.title + '" />');
          }
          html.push('</div>');
        if (jsonObject.image) {
          html.push('<div class="Content">'); //margin-left is set
        } else {
          html.push('<div>'); //no margin-left is set
        }
            html.push('<div class="Title"><a class="ColorLinkShared" href="'+ jsonObject.link +'" target="_blank">' + jsonObject.title + '</a></div>');
              html.push('<div class="Description">' + description + '</div>');
              html.push('<div class="Source">' + Locale.getMsg('source') + ' : <a href="' + jsonObject.link + '" target="_blank">' + jsonObject.link + '</a></div>');
            html.push('</div>');
            html.push('<div style="clear: both; height: 0px;"><span></span></div>');
          html.push('</div>');
          html.push('<div class="NewsDate">' + prettyTime + '  ' + getCommentLikeBlock() + '</div>');
//          html.push(getCommentLikeBlock());
        html.push('</div>')
        html.push('<div class="ClearLeft"><span></span></div>');
      html.push('</div>');
      html.push(getPeopleLikeBlock());
      html.push(getCommentListBlock());
      html.push(getCommentFormBlock());
      return html.join('');
    }

    var getActionContentBlock = function() {
      var html = [];
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
      return html.join('');
    }

    var getCommentLikeBlock = function() {
      var html = [];
      if (statusUpdate.currentView === 'canvas') {
        if (isOwner(viewerId) || isFriend(viewerId)) {
          html.push('<a class="Link" id="Comment' + activityId + '" href="#comment">' + Locale.getMsg('comment') + '</a><span>  |  </span><a class="Link" id="Like' + activityId + '" href="#like">' + Locale.getMsg('like') + '</a>');
        }
      }
      return html.join('');
    }

    var getPeopleLikeBlock = function() {
      var html = [];
      if (statusUpdate.currentView === 'canvas') {
        html.push('<div class="ListPeopleLikeBG">');
          html.push('<div id="ListPeopleLike' + activityId + '" class="ListPeopleLike DisplayNone">');
            html.push('<div class="ListPeopleContent">');
              html.push('<div id="TitleLike' + activityId + '" class="Title"></div>');
              html.push('<div style="display:none;" id="ListPeople' + activityId + '"></div>');
              html.push('<div class="ClearLeft"><span></span></div>');
            html.push('</div>');
          html.push('</div>');
        html.push('</div>');
      }
      return html.join('');
    }

    var getCommentListBlock = function() {
      var html = [];
      html.push('<div id="CommentListInfo' + activityId + '" class="CommentListInfo"></div>');
      html.push('<div id="CommentListBlock' + activityId + '"></div>');
      return html.join('');
    }

    var getCommentFormBlock = function() {
      var html = [];
      if (statusUpdate.currentView === 'canvas') {
        html.push('<div id="CommentForm' + activityId + '" class="CommentFormBlock DisplayNone">')
          html.push('<div class="CommentContent">');
            html.push('<div class="CommentBorder">');
              html.push('<textarea title="' + Locale.getMsg('write_a_comment') + '" id="CommentTextarea' + activityId + '" class="CommentTextarea">' + Locale.getMsg('write_a_comment') + '</textarea>');
              html.push('<input id="CommentButton' + activityId + '" class="CommentButton DisplayNone" type="button" value="' + Locale.getMsg('comment') + '" />');
              html.push('<div class="ClearBoth"></div>');
            html.push('</div>');
          html.push('</div>');
        html.push('</div>');
      }
      return html.join('');
    }

    var displayActivities = function(appendableRootId, moreId, activities, isOwnerActivity, displayName) {
      var uiOwnerActivitiesShowHide = Util.getElementById(config.ui.UI_OWNER_ACTIVITIES_SHOW_HIDE);

      if (!activities || activities.length === 0) {
        if (isOwnerActivity) {
          displayActivitiesNext = true;
          if (statusUpdate.owner.getId() === statusUpdate.viewer.getId()) {
            Util.hideElement(config.ui.UI_OWNER_ACTIVITIES_SHOW_HIDE);
          }
          Util.getElementById(appendableRootId).innerHTML = '<div class= "Empty">' + Locale.getMsg('displayName_does_not_have_update', [displayName]) + '</div>';
        } else {
          Util.getElementById(appendableRootId).innerHTML = '<div class="Empty">' + Locale.getMsg('displayName_do_not_have_update', [displayName]) + '</div>';
        }
        SocialUtil.adjustHeight(statusUpdate.contentForAdjustHeight);
        return;
      }

      if (statusUpdate.owner.getId() === statusUpdate.viewer.getId()) {
        if (activities != null) {
          if(activities.length > 0) {
            if (statusUpdate.isOwnerActivityShown) {
              Util.showElement(config.ui.UI_OWNER_APPENDABLE_ROOT);
            } else {
              Util.hideElement(config.ui.UI_OWNER_APPENDABLE_ROOT);
            }
          }
        }

        if (statusUpdate.isOwnerActivityShown) {
          uiOwnerActivitiesShowHide.innerHTML='<div class="CollapseAllActivities">' + Locale.getMsg('collapse_all_activities') + '</div>';
        } else {
          uiOwnerActivitiesShowHide.innerHTML='<div class="ExpandAllActivities">' + Locale.getMsg('expand_all_activities') + '</div>';
        }

        if (!activities || activities.length === 0) {
          Util.hideElement(config.ui.UI_OWNER_ACTIVITIES_SHOW_HIDE);
        } else {
          Util.showElement(config.ui.UI_OWNER_ACTIVITIES_SHOW_HIDE);
        }
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
        if (isOwnerActivity) {
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
      //var useLightBackground = true;
      var index = 0;
      ajaxQueue();
      function ajaxQueue() {
      if (index === displayActivityNum) {
        SocialUtil.adjustHeight(statusUpdate.contentForAdjustHeight);
        displayActivitiesNext = true;
        return
      }
      var html = '';
      var aDecoratorContainerClass = 'ADecoratorContainer';
      /*
      if (!useLightBackground) {
        aDecoratorContainerClass = 'ADecoratorContainerGray';
        useLightBackground = true;
      } else {
        aDecoratorContainerClass = 'ADecoratorContainer';
        useLightBackground = false;
      }
      */
      var activity = activities[index];
      var activityId =  activity.getField(opensocial.Activity.Field.ID);
      userId = activity.getField(opensocial.Activity.Field.USER_ID);
      var activityOwner = statusUpdate.getPerson(userId);
      index++;
      //if not found, fetching
      if (!activityOwner) {
        var req = opensocial.newDataRequest();
        var opts = {};
        opts[opensocial.DataRequest.PeopleRequestFields.PROFILE_DETAILS] =
            [opensocial.Person.Field.ID,
             opensocial.Person.Field.NAME,
             opensocial.Person.Field.PROFILE_URL,
             opensocial.Person.Field.THUMBNAIL_URL];
        req.add(req.newFetchPersonRequest(userId, opts), 'activityOwner');
        req.send(function(res) {
          if (res.hadError()) {
            debug.error('error getting activityOwner!');
            statusUpdate.miniMessage.createDismissibleMessage(Locale.getMsg('internal_error'));
            return;
          }
          activityOwner = res.get('activityOwner').getData();
          //setting profileUrl; userName;
          userName = activityOwner.getDisplayName();
          profileUrl = activityOwner.getField(opensocial.Person.Field.PROFILE_URL);
          setDisplay();
          ajaxQueue();
        });
      } else {
        userName = activityOwner.getDisplayName();
        profileUrl = activityOwner.getField(opensocial.Person.Field.PROFILE_URL);
        setDisplay();
        ajaxQueue();
      }

      function setDisplay() {
        var title = activity.getField(opensocial.Activity.Field.TITLE);
        var url = activity.getField(opensocial.Activity.Field.URL);
        var externalId = activity.getField(opensocial.Activity.Field.EXTERNAL_ID);
        var templateParams = activity.getField(opensocial.Activity.Field.TEMPLATE_PARAMS);
        var jsonObject = {
          "comment": templateParams.comment,
          "title": title,
          "link": url,
          "description": templateParams.description,
          "image": templateParams.image
        };
        if (!title) title = '';
        if (externalId && (externalId == 'LINK_ACTIVITY')) {
          html = getLinkShareActivityBlock(activity, jsonObject, isOwnerActivity);
        } else {//normal display
          html = getNormalActivityBlock(activity, isOwnerActivity);
        }
        if (html === null || html === '') {
          debug.error('html is null!!!');
          miniMessage.createDismissibleMessage(Locale.getMsg('internal_error'));
          return;
        }
        var newEl = Util.addElement(appendableRootId, 'div', null, html);
        newEl.setAttribute('class', aDecoratorContainerClass);
        newEl.setAttribute('className', aDecoratorContainerClass);
        newEl.setAttribute('id', 'Activity' + activityId);
        if (statusUpdate.currentView === 'canvas') {
          if (isOwner(viewerId) || isFriend(viewerId)) {
            Like.getLikeIds(activityId, Like.displayLike);
          }
          if (isOwnerActivity && (viewerId === ownerId)) {
            statusUpdate.setActionContentButton(activityId);
            statusUpdate.setDeleteActivity(activityId);
          }
          if (isOwner(viewerId) || isFriend(viewerId)) {
            Comment.setComment(activityId, userId);
          }
        }
      }
    }
  }

  if (dataResponse.hadError()) {
    debug.error('dataResponse had error, please refresh!!!');
    debug.info(dataResponse);
    miniMessage.createDismissibleMessage(Locale.getMsg('internal_error'));
    return;
  }
  //should be synchronized when displayActivities is called because inside displayAcvitities
  // contains AJAX code runs asynchronizedly
  if (dataMode === StatusUpdate.DataMode_BOTH || dataMode === StatusUpdate.DataMode_OWNER_ONLY) {
    this.ownerActivities = dataResponse.get('ownerActivities').getData().asArray();
    this.ownerActivities.sort(sortPostedTimeHandler);
    displayActivities(config.ui.UI_OWNER_APPENDABLE_ROOT, config.ui.UI_OWNER_MORE, this.ownerActivities, true, this.owner.getDisplayName());
  }
  var intervalId = window.setInterval(function() {
    if (displayActivitiesNext) {
      window.clearInterval(intervalId);
      if (statusUpdate.viewer.getId() === statusUpdate.owner.getId()) {
        if (dataMode === StatusUpdate.DataMode_BOTH || dataMode === StatusUpdate.DataMode_FRIENDS_ONLY) {
          statusUpdate.ownerFriendsActivities = dataResponse.get('ownerFriendsActivities').getData().asArray();
          statusUpdate.ownerFriendsActivities.sort(sortPostedTimeHandler);
          displayActivities(config.ui.UI_FRIENDS_APPENDABLE_ROOT, config.ui.UI_FRIENDS_MORE, statusUpdate.ownerFriendsActivities, false, Locale.getMsg('owner_friends'));
        }
      }
    }
  }, 100);
}

/**
 * gets name by userId
 * if not found, return "" and warning
 * @param	userId
 */
eXo.social.StatusUpdate.prototype.getName = function(userId) {
  if (userId === this.owner.getId()) {
    return this.owner.getDisplayName();
  } else if (userId === this.viewer.getId()) {
    return this.viewer.getDisplayName();
  }
  var person = this.ownerFriends.getById(userId);
  if (person !== null) {
    return person.getDisplayName();
  }
  debug.warn("Can not get name with userId: " + userId);
  return "";
}

eXo.social.StatusUpdate.prototype.getPerson = function(userId) {
  if (userId === this.owner.getId()) {
    return this.owner;
  } else if (userId === this.viewer.getId()) {
    return this.viewer;
  }
  var person = this.ownerFriends.getById(userId);
  if (!person) {
    debug.warn("can not get person with userId: " + userId);
  }
  return person;
}

/**
 * gets avatar by userId
 * if not found, return default avatar url
 * @param	userId
 */
eXo.social.StatusUpdate.prototype.getAvatar = function(userId, isMyAvartar) {
  var avatarUrl = "/eXoSkin/skin/less/social/skin/ShareImages/activity/AvatarPeople.gif";
  if (isMyAvartar) {
    avatarUrl = "/eXoSkin/skin/less/social/skin/ShareImages/activity/MyStatusAvatar.gif";
  }
  var person = null;
  if (userId === this.owner.getId()) {
    person = this.owner;
  } else {
    person = this.ownerFriends.getById(userId);
  }

  if (person !== null) {
    return person.getField(opensocial.Person.Field.THUMBNAIL_URL);
  }
  
  return eXo.social.StatusUpdate.config.HOST + avatarUrl;
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
  if (this.linkShare.content) {
    this.shareable = true;
  }
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
      activityElement.style.height="20px";
      activityElement.style.color="#777777";
      activityElement.value = statusUpdate.uiComposer.DEFAULT_INPUT;
    }
  }

  if (this.currentView === 'home') {
    var activity = opensocial.newActivity({ 'title': activityElement.value});
    var statusUpdate = this;
    opensocial.requestCreateActivity(activity, "HIGH", function() {
      statusUpdate.refresh();}
    );
    restore();
    return;
  }
  // replace tag
  var reWhiteSpace = new RegExp(/^\s+$/);
  var activityContent = activityElement.value;
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
    if (activityContent  === "" || reWhiteSpace.test(activityElement.value)) {
      return false;
    }
    if ((activityContent === this.uiComposer.DEFAULT_INPUT) && (activityElement.style.height === "20px" || activityElement.style.minHeight === "12px")) return;
  }

  if ((currentView === 'canvas') && (this.shareable === false)) return;

  var activity = opensocial.newActivity({ 'title': activityContent});
  var statusUpdate = this;
  opensocial.requestCreateActivity(activity, "HIGH", function() {
    statusUpdate.updateOwnerActivities();}
  );
  restore();
}

eXo.social.StatusUpdate.sendPrivateMessage = function(userId) {
//var sendMsg = this.sendPrivateMsg;
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
