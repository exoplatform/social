/**
 * Comment.js
 * Implements comment feature for activity
 * @author <a href="mailto:hoatlevan@gmail.com">hoatle</a>
 * @since Dec 24, 2009
 * @copyright eXo Platform SEA
 */
//namespace
var eXo = eXo || {};
eXo.social = eXo.social || {};
/**
 * constructor
 */
eXo.social.Comment = function() {
	
}

/**
 * reference object
 */
eXo.social.Comment.ref = {
	statusUpdate: null
}

/**
 * config object
 */
eXo.social.Comment.config = {
	URL_COMMENTS: 'http://localhost:8080/rest/social/activities/${activityId}/comments'
}

/**
 * get list of comments
 * @param	activityId
 * @param	callback
 */
eXo.social.Comment.get = function(activityId, callback) {
	if (!activityId) {
		debug.warn('[Comment.get]: activityId is null!');
		return;
	}
	var url = eXo.social.Comment.config.URL_COMMENTS.replace('${activityId}', activityId) + '/show.json';
	eXo.social.Util.makeRequest(url, callback);
}

/**
 * actually comment is an activity with externalId the id of activity commented
 * @param	activityId
 * @param	comment
 */
eXo.social.Comment.create = function(activityId, comment, callback) {
	if (!activityId) {
		debug.warn('[Comment.create]: activityId is null!');
		return;
	}
	var url = eXo.social.Comment.config.URL_COMMENTS.replace('${activityId}', activityId) + '/update.json';
	eXo.social.Util.makeRequest(url, callback, null, gadgets.io.MethodType.POST, gadgets.io.ContentType.JSON, comment);
}

/**
 * delete comment by id
 * @param	commentId
 */
eXo.social.Comment.del = function(activityId, commentId, callback) {
	if (!activityId) {
		debug.warn('[Comment.del]: activityId is null!');
		return;
	}
	var url = eXo.social.Comment.config.URL_COMMENTS.replace('${activityId}', activityId) + '/destroy/' + commentId + '.json';
	eXo.social.Util.makeRequest(url, callback, null, gadgets.io.MethodType.POST, gadgets.io.ContentType.JSON, null);
}

eXo.social.Comment.setComment = function(activityId) {
	var Util = eXo.social.Util,
		Locale = eXo.social.Locale,
		statusUpdate = eXo.social.Comment.ref.statusUpdate,
		comments, comment,
		commentListBlockId = 'CommentListBlock' + activityId,
		commentListBlockEl = Util.getElementById(commentListBlockId),
		commentListInfoId = 'CommentListInfo' + activityId,
		commentListInfoEl = Util.getElementById(commentListInfoId);
	
	function display(cms) {
		commentListBlockEl.innerHTML = ''; //resets
		for (var i = 0, l = cms.length; i < l; i++) {
			comment = cms[i];
			if (comment) {
				var newEl = Util.addElement(commentListBlockId, 'div', 'CommentBlock' + comment.id, getCommentBlock());
				newEl.setAttribute('class', 'CommentBlock');
			}
		}	
	}
	
	function renderCommentList() {
		display(comments);
	}
	
	(function() {
		eXo.social.Comment.get(activityId, function(res) {
			if (res.data !== null) {
				comments = res.data.comments;
				renderCommentList();
				gadgets.window.adjustHeight();
			}
		})
	})();
	
  	var getCommentBlock = function() {
  		var html = [],
  			inlineStyle = 'style="background:none; padding-top:2px;"';
  		//if the first or no like => keep it
  		if (!commentListBlockEl.hasChildNodes()) {
  			inlineStyle = '';
  		}
		html.push('<div class="CommentContent"');
									html.push(inlineStyle);
										html.push('>');
			html.push('<div class="CommentBorder">')
  				html.push('<div class="CommentActivitiesContent">');
  					html.push('<a href="#" class="AvatarPeopleBG">');
  						html.push('<img height="47px" width="47px" src="' + statusUpdate.getAvatar(comment.userId) + '" />');
  					html.push('</a>');
  					html.push('<div class="Content">');
  						html.push('<div class="Titlecontent" style="height: 24px;">');
  							html.push('<div class="TitleItem">' + statusUpdate.getName(comment.userId) + '</div>');
  							html.push('<div style="clear: both; height: 0px;"><span></span></div>');
  						html.push('</div>');
  						html.push('<div class="Content">' + comment.body + '</div>');
  						html.push('<div class="NewsDate">' + Util.toPrettyTime(new Date(comment.postedTime)) + '</div>');
  					html.push('</div>')
  				html.push('<div class="ClearLeft"><span></span></div>');
  			html.push('</div>');
  		html.push('</div>');
		return html.join('');
  	}
  	
	var commentEl = Util.getElementById('Comment' + activityId),
		commentFormId = 'CommentForm' + activityId,
		commentTextareaId = 'CommentTextarea' + activityId,
		commentButtonId = 'CommentButton' + activityId,
		commentFormEl = Util.getElementById(commentFormId),
		commentTextareaEl = Util.getElementById(commentTextareaId),
		commentButtonEl = Util.getElementById(commentButtonId);
	
	if (!commentEl) {
		debug.warn('commentEl is null!');
		return;
	}
	Util.addEventListener(commentEl, 'click', function(evt) {
		if (commentFormEl.style.display !== 'block') {
			Util.showElement(commentFormId);
			commentTextareaEl.focus();
			gadgets.window.adjustHeight();
		} else {
			Util.hideElement(commentFormId);
		}
	}, false);
	
	Util.addEventListener(commentTextareaEl, 'focus', function(evt) {
		this.style.height = '50px';
		this.style.color = '#000000';
		if (this.value === Locale.getMsg('write_a_comment')) {
			this.value = '';
		}
		Util.showElement(commentButtonId);
		gadgets.window.adjustHeight();
	}, false);
	
	Util.addEventListener(commentTextareaEl, 'blur', function(evt) {
		if (this.value === '') {
			this.value = Locale.getMsg('write_a_comment');
			this.style.height = '20px';
			this.style.color = 'gray';
			Util.hideElement(commentButtonId);
		}
	}, false);
	
	Util.addEventListener(commentButtonEl, 'click', function() {
		commentTextareaEl.disabled = true;
		this.disabled = true;
		var userName = statusUpdate.viewer.getDisplayName();
		//TODO hoatle check XSS security?
		var userId = statusUpdate.viewer.getId(),
		    title = Locale.getMsg('user_commented_on_an_activity', [userName]),
		    body = commentTextareaEl.value;
		var activity = {
			'userId': userId,
			'title': title,
			'body': body
		}
		
		eXo.social.Comment.create(activityId, activity, function(res) {
			commentTextareaEl.disabled = false;
			commentButtonEl.disabled = false;
			if (res.data != null) { //succeeded
				commentTextareaEl.value = '';
				commentTextareaEl.focus();
				activityId = res.data.activityId;
				comment = res.data.comments[0];
				var newEl = Util.addElement(commentListBlockId, 'div', 'CommentBlock' + comment.id, getCommentBlock());
				newEl.setAttribute('class', 'CommentBlock');
				gadgets.window.adjustHeight();
			} else { //failed
				//TODO hoatle informs users
				alert('Can not post comment, please retry!');
				debug.warn('post comment failed!');
			}
		});
	}, false);
}