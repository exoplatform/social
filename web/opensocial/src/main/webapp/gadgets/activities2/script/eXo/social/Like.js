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
 	URL_GET_LIKE_IDS: 'http://localhost:8080/rest/social/activities/getLikeIds',
 	URL_SET_LIKE_ID: 'http://localhost:8080/rest/social/activities/setLikeId',
 	URL_REMOVE_LIKE_ID: 'http://localhost:8080/rest/social/activities/removeLikeId'
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
 	var url = eXo.social.Like.config.URL_GET_LIKE_IDS + "/" + activityId;
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
 	var url = eXo.social.Like.config.URL_SET_LIKE_ID + '/' + activityId + '/' + userId;
 	eXo.social.Util.makeRequest(url, callback);
 }
 
 /**
  * remove like id
  * @param	activityId
  * @param	userId
  * @param	callback
  * @static 
  */
 eXo.social.Like.removeLikeId = function(activityId, userId, callback) {
 	debug.info('aId: ' + activityId + '; userId: ' + userId);
 	if (!activityId || !userId) {
 		debug.warn("activityId or userId is null! activityId: " + activityId + "; userId: " + userId);
 		return;
 	}
 	var url = eXo.social.Like.config.URL_REMOVE_LIKE_ID + '/' + activityId + '/' + userId;
 	eXo.social.Util.makeRequest(url, callback);
 }
 
/**
 * callback hanlder for displaying likes of an activity
 * @static
 */
 eXo.social.Like.displayLike = function(response) {
 	var Util = eXo.social.Util;
 	var Locale = eXo.social.Locale;
 	var Like = eXo.social.Like;
 	var statusUpdate = Like.ref.statusUpdate;
 	
 	if(!response.data) {
		debug.warn('response data is null!!!');
		return;
	}
	if (!statusUpdate) {
		debug.warn('statusUpdate ref is not set!');
		return;
	}
	var activityId = response.data.activityId;
	var ids = response.data.ids;
	var likeInfos = response.data.likeInfos;
	var viewerId = statusUpdate.viewer.getId();
	var html = [];
	var like = Util.getElementById('Like' + activityId);
	var listPeopleLike = Util.getElementById('ListPeopleLike' + activityId);
	var titleLike = Util.getElementById('TitleLike' + activityId);
	
	if (ids.length === 0) {
		like.innerHTML = Locale.getMsg('like');
		like.onclick = function() { Like.setLikeId(activityId, viewerId, Like.displayLike); };
		listPeopleLike.style.display = 'none';
		gadgets.window.adjustHeight();
		return;
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
	Like.renderListPeople(activityId, likeInfos);
 }
 
 /**
  * render like details 
  */
 eXo.social.Like.renderListPeople = function(activityId, likeInfos) {
 	var Util = eXo.social.Util;
	if(!activityId) {
		debug.warn('activityId is null from Like.renderLikeDetail()');
		return;
	}
	var listPeople = Util.getElementById('ListPeople' + activityId);
	var html = [];
	var thumbnail = eXo.social.StatusUpdate.config.path.ROOT_PATH + '/style/images/AvatarPeople.gif';
	if (likeInfos !== null) {
		for(var i = 0, length = likeInfos.length; i < length; i++) {
			if (likeInfos[i].thumbnail !== null)	thumbnail = likeInfos[i].thumbnail;
			html.push('<a href="#UserId"  class="AvatarPeopleBG">');
				html.push('<img title="' + likeInfos[i].fullName + '" alt="" height="47px" width="47px" src="' + thumbnail + '" />');
			html.push('</a>');
		}
	}
	listPeople.innerHTML = html.join('');
	gadgets.window.adjustHeight();
}

/**
 * toggles display LikeDetailsBlock
 * @param	activityId
 * @param	enabled - true to show, false to hide
 * @static
 */
eXo.social.Like.toggleDisplayListPeople = function(activityId) {
	var Util = eXo.social.Util;
	var listPeople = Util.getElementById('ListPeople' + activityId);
	if (listPeople.style.display === 'none') {
		listPeople.style.display = 'block';
	} else {
		listPeople.style.display = 'none';
	}
	gadgets.window.adjustHeight();
}