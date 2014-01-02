/**
 * Like.js
 * Implements like feature for activity
 * @author	<a href="mailto:hoatlevan@gmail.com">hoatle</a>
 * @since	Oct 22, 2009
 * @copyright	eXo Platform SEA
 */

//namespace
var eXo = eXo || {};
eXo.social = eXo.social || {};

/**
 * constructor
 */
eXo.social.Like = function() {
}

/**
 * reference object
 */
eXo.social.Like.ref = {
  statusUpdate : null
}

/**
 * config object
 * @static
 */
eXo.social.Like.config = {
  REST_LIKE: 'http://${HOST}/${REST_CONTEXT_NAME}/${portalName}/social/activities/{activityId}/likes'
};

 /**
  * get like ids
  * @param	activityId
  * @param	callback
  * @static
  */
eXo.social.Like.getLikeIds = function(activityId, callback) {
  if (!activityId) {
    debug.warn("activityId is null");
    return;
  }
  var url = eXo.social.Like.config.REST_LIKE.replace('{activityId}', activityId) + '/show.json';
  eXo.social.Util.makeRequest(url, callback);
}

/**
 * set like id
 * @param	activityId
 * @param	userId
 * @param	callback
 * @static
 */
eXo.social.Like.setLikeId = function(activityId, userId, callback) {
  //debug.info('aId: ' + activityId + '; userId: ' + userId);
  if (!activityId || !userId) {
    debug.warn("activityId or userId is null! activityId: " + activityId + "; userId: " + userId);
    return;
  }
  eXo.social.Like.currentActivityId = activityId;
  var url = eXo.social.Like.config.REST_LIKE.replace('{activityId}', activityId) + '/update.json';
  var like = {
    identityId: userId
  }
  eXo.social.Util.makeRequest(url, callback, null, gadgets.io.MethodType.POST, gadgets.io.ContentType.JSON, like);
}

/**
 * remove like id
 * @param	activityId
 * @param	userId
 * @param	callback
 * @static
 */
eXo.social.Like.removeLikeId = function(activityId, userId, callback) {
  if (!activityId || !userId) {
    debug.warn("activityId or userId is null! activityId: " + activityId + "; userId: " + userId);
    return;
  }
  eXo.social.Like.currentActivityId = activityId;
  var url = eXo.social.Like.config.REST_LIKE.replace('{activityId}', activityId) + '/destroy/' + userId + '.json';
  eXo.social.Util.makeRequest(url, callback, null, gadgets.io.MethodType.POST, gadgets.io.ContentType.JSON, null);
}

/**
 * callback handler for displaying likes of an activity
 * @static
 */
eXo.social.Like.displayLike = function(response) {
	var SocialUtil = eXo.social.SocialUtil;
  var Util = eXo.social.Util;
  var Locale = eXo.social.Locale;
  var Like = eXo.social.Like;
  var statusUpdate = Like.ref.statusUpdate;

  if (response.rc === 404) { //not found
    alert(Locale.getMsg('error_activity_not_found'));
    //delete that activity block
    Util.removeElementById('Activity' + Like.currentActivityId);
    //check if delete all => displays empty message
    var ownerRootEl = Util.getElementById(eXo.social.StatusUpdate.config.ui.UI_OWNER_APPENDABLE_ROOT);
    var friendsRootEl = Util.getElementById(eXo.social.StatusUpdate.config.ui.UI_FRIENDS_APPENDABLE_ROOT);
    if (!ownerRootEl.hasChildNodes()) {
      ownerRootEl.innerHTML = '<div class= "Empty">' + Locale.getMsg('displayName_does_not_have_update', [statusUpdate.owner.getDisplayName()]) + '</div>';
    }
    if (!friendsRootEl.hasChildNodes()) {
      friendsRootEl.innerHTML = '<div class="Empty">' + Locale.getMsg('displayName_do_not_have_update', [Locale.getMsg('owner_friends')]) + '</div>';
    }
    return;
  } else if (!response.data) {
    debug.warn('Like.displayLike: response data is null!!!');
    //alert(Locale.getMsg('internal_error'));
    return;
  }
  if (!statusUpdate) {
    debug.error('statusUpdate ref is not set!');
    return;
  }
  var activityId = response.data.activityId;
  var likes = response.data.likes;
  var viewerId = statusUpdate.viewer.getId();
  var html = [];
  var like = Util.getElementById('Like' + activityId);
  var listPeopleLike = Util.getElementById('ListPeopleLike' + activityId);
  var titleLike = Util.getElementById('TitleLike' + activityId);
  var ids = [];
  if (likes.length === 0) {
    like.innerHTML = Locale.getMsg('like');
    like.onclick = function() { Like.setLikeId(activityId, viewerId, Like.displayLike); };
    listPeopleLike.style.display = 'none';
    SocialUtil.adjustHeight(statusUpdate.contentForAdjustHeight);
    return;
  }
  for (var i = 0, l = likes.length; i < l; i++) {
    ids.push(likes[i].identityId);
  }
  if(statusUpdate.hasViewerId(ids)) {
    like.innerHTML = Locale.getMsg('unlike');
    like.onclick = function() { Like.removeLikeId(activityId, viewerId, Like.displayLike); };
    var persons = [];
    persons.push('<a id="PeopleLikes' + activityId + '" class="PeopleLikes" href="#Like.displayLikeDetails">');
    if(ids.length === 2) {
      persons.push(Locale.getMsg('one_person'));
    } else if (ids.length > 2) {
      persons.push(Locale.getMsg('num_persons', [ids.length-1]));
    }
    persons.push('</a>');
    if (ids.length === 1) {//only u
    html.push(Locale.getMsg('you_like_this'));
    } else {
    html.push(Locale.getMsg('you_and_persons_like_this', [persons.join('')]));
    }
  } else {
    like.innerHTML = Locale.getMsg('like');
    like.onclick = function() { Like.setLikeId(activityId, viewerId, Like.displayLike); };
    var persons = [];
    persons.push('<a id="PeopleLikes' + activityId + '" class="PeopleLikes" href="#Like.displayLikeDetails">');
    if(ids.length === 1) {
      persons.push(Locale.getMsg('one_person'));
    } else {
      persons.push(Locale.getMsg('num_people', [ids.length]))
    }
    persons.push('</a>')
    if (ids.length === 1) {
    html.push(Locale.getMsg('person_likes_this', [persons.join('')]));
    } else {
    html.push(Locale.getMsg('persons_like_this', [persons.join('')]));
    }
  }
  titleLike.innerHTML = html.join('');
  listPeopleLike.style.display = 'block';
  var peopleLikesBlock = Util.getElementById('PeopleLikes' + activityId);
  if (peopleLikesBlock) {
    peopleLikesBlock.onclick = function() {
      Like.toggleDisplayListPeople(activityId);
    };
  }
  Like.renderListPeople(activityId, likes);
}

/**
 * render like details
 */
eXo.social.Like.renderListPeople = function(activityId, likes) {
	var SocialUtil = eXo.social.SocialUtil;
  var Util = eXo.social.Util,
    Like = eXo.social.Like;
  if(!activityId) {
    debug.warn('activityId is null from Like.renderLikeDetail()');
    return;
  }
  var listPeople = Util.getElementById('ListPeople' + activityId);
  var html = [];

  if(likes !== null) {
    for(var i = 0, length = likes.length; i < length; i++) {
      if (likes[i].identityId === Like.ref.statusUpdate.viewer.getId()) continue;
      var likePerson = Like.ref.statusUpdate.getPerson(likes[i].identityId);
      if (!likePerson) {
        debug.warn('likePerson is null');
      }
      var userName = likePerson.getDisplayName();
      var profileUrl = likePerson.getField(opensocial.Person.Field.PROFILE_URL);
      var thumbnail = '/eXoSkin/skin/less/social/skin/ShareImages/activity/AvatarPeople.gif';
      if (likes[i].thumbnail !== null)	thumbnail = likes[i].thumbnail;
      html.push('<a href="' + profileUrl + '" target="_parent" title="' + profileUrl + '"  class="AvatarPeopleBG">');
        html.push('<img title="' + userName + '" alt="' + userName + '" height="47px" width="47px" src="' + thumbnail + '" />');
      html.push('</a>');
    }
  }
  listPeople.innerHTML = html.join('');
  SocialUtil.adjustHeight(Like.ref.statusUpdate.contentForAdjustHeight);
}

/**
 * toggles display LikeDetailsBlock
 * @param	activityId
 * @param	enabled - true to show, false to hide
 * @static
 */
eXo.social.Like.toggleDisplayListPeople = function(activityId) {
	var SocialUtil = eXo.social.SocialUtil;
  var Util = eXo.social.Util;
  var listPeople = Util.getElementById('ListPeople' + activityId);
  if (listPeople.style.display === 'none') {
    listPeople.style.display = 'block';
  } else {
    listPeople.style.display = 'none';
  }
  SocialUtil.adjustHeight(Like.ref.statusUpdate.contentForAdjustHeight);
}
