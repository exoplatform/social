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
	URL_COMMENTS: 'http://${HOST}/${REST_CONTEXT_NAME}/${portalName}/social/activities/${activityId}/comments'
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
	eXo.social.Comment.currentActivityId = activityId;
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
	eXo.social.Comment.currentActivityId = activityId;
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
	eXo.social.Comment.currentActivityId = activityId;
	var url = eXo.social.Comment.config.URL_COMMENTS.replace('${activityId}', activityId) + '/destroy/' + commentId + '.json';
	eXo.social.Util.makeRequest(url, callback, null, gadgets.io.MethodType.POST, gadgets.io.ContentType.JSON, null);
}

eXo.social.Comment.setComment = function(activityId, activityUserId) {
	var Util = eXo.social.Util,
		Locale = eXo.social.Locale,
		statusUpdate = eXo.social.Comment.ref.statusUpdate,
		comment,
		commentListBlockId = 'CommentListBlock' + activityId,
		commentListBlockEl = Util.getElementById(commentListBlockId),
		commentListInfoId = 'CommentListInfo' + activityId,
		commentListInfoEl = Util.getElementById(commentListInfoId),
		commentLinkId = 'CommentLink' + activityId,
		deleteCommentButtonId = null,
		updateNumComment = false,
		activityUserId = activityUserId,
		commentId = null;
		commentUserId = null,
		comments = {};
		comments[activityId] = [];
	
	function display(cms) {
		commentListBlockEl.innerHTML = ''; //resets
		for (var i = 0, l = cms.length; i < l; i++) {
			comment = cms[i];
			commentId = comment.id;
			commentUserId = comment.userId;
			if (comment) {
				var newEl = Util.addElement(commentListBlockId, 'div', 'CommentBlock' + commentId, getCommentBlock());
				newEl.setAttribute('class', 'CommentBlock');
				//ie + firefox
				newEl.setAttribute('className', 'CommentBlock');
				newEl.className = 'CommentBlock';
			}
			setDeleteComment(commentId);
		}	
	}
	
	function setDeleteComment(cmId) {
		var commentId = cmId,
		deleteCommentButtonId = 'DeleteCommentButton' + commentId,
		deleteCommentButton = Util.getElementById(deleteCommentButtonId);
		if (deleteCommentButton) {
			Util.addEventListener(deleteCommentButton, 'click', function(evt) {
				if (confirm(Locale.getMsg('are_you_sure_to_delete_this_comment'))) {
					eXo.social.Comment.del(activityId, commentId, function(res) {
						if (res.rc === 200) {
							//remove commentBlock
							comment = res.data.comments[0];
							comments[activityId].splice(comments[activityId].indexOf(comment), 1);
							var commentBlockId = 'CommentBlock' + commentId;
							Util.removeElementById(commentBlockId);
							if (updateNumComment) {
								if (comments[activityId].length > 2) {
									var html = [];
									html.push('<div class="CommentBlock">');
										html.push('<div class="CommentContent">');
											html.push('<div class="CommentBorder">');
												html.push('<a id="' + commentLinkId + '" href="#show-all-comments">' + Locale.getMsg('show_all_num_comments', [comments[activityId].length]) + '</a>');
											html.push('</div>');
										html.push('</div>');
									html.push('</div>');
									commentListInfoEl.innerHTML = html.join('');
									var commentLinkEl = Util.getElementById(commentLinkId);
									commentLinkEl.onclick = function() {
										eXo.social.Comment.get(activityId, function(res) {
											if (res.data !== null) {
												hideAll = false;
												comments[activityId] = res.data.comments;
												renderCommentList(comments[activityId], false);
												gadgets.window.adjustHeight();
											}
										})
									}
								} else {
									Util.removeElementById(commentListInfoId);
									display(comments[activityId]);
								}
								gadgets.window.adjustHeight();
							} else if (comments[activityId].length < 3) {
								Util.removeElementById(commentListInfoId);
							}
						} else if (res.rc === 404) { //activity not found
						  alert(Locale.getMsg('error_activity_not_found'));
						  //remove activity
						  Util.removeElementById('Activity' + eXo.social.Comment.currentActivityId);
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
						} else {
							//alert(Locale.getMsg('internal_error'));
							debug.warn('problem deleting comment');
						}
					})
				}
			}, false);
		} else {
			debug.warn('deleteCommentButton is null!');
		}
	}
	
	function renderCommentList(cms, shortDisplay) {
		comments[activityId] = cms || [];
		if (comments[activityId].length > 2) {
			if (shortDisplay) {
				updateNumComment = true;
				var html = [];
				html.push('<div class="CommentBlock">');
					html.push('<div class="CommentContent">');
						html.push('<div class="CommentBorder">');
							html.push('<a id="'+ commentLinkId +'" href="#show-all-comments">' + Locale.getMsg('show_all_num_comments', [comments[activityId].length]) + '</a>');
						html.push('</div>');
					html.push('</div>');
				html.push('</div>');
				commentListInfoEl.innerHTML = html.join('');
				var commentLinkEl = Util.getElementById(commentLinkId);
				commentLinkEl.onclick = function() {
					eXo.social.Comment.get(activityId, function(res) {
						if (res.data !== null) {
							updateNumComment = false;
							comments[activityId] = res.data.comments;
							renderCommentList(comments[activityId], false);
							gadgets.window.adjustHeight();
						}
					})
				}
				display(comments[activityId].slice(-2));
			} else {
				updateNumComment = false;
				var html = [];
				html.push('<div class="CommentBlock">');
					html.push('<div class="CommentContent">');
						html.push('<div class="CommentBorder">');
							html.push('<a id="' + commentLinkId + '" href="#hide-all-comments">' + Locale.getMsg('hide_all_comments') + '</a>');
						html.push('</div>');
					html.push('</div>');
				html.push('</div>');
				commentListInfoEl.innerHTML = html.join('');
				var commentLinkEl = Util.getElementById(commentLinkId);
				commentLinkEl.onclick = function() {
					hideAll = true;
					commentListBlockEl.style.display = 'none';
					renderCommentList(comments[activityId], true);
					gadgets.window.adjustHeight();
				}
				commentListBlockEl.style.display = 'block';
				display(comments[activityId]);
			}
		} else {
			display(comments[activityId]);
		}
		
	}
	
	(function() {
		eXo.social.Comment.get(activityId, function(res) {
			if (res.data !== null) {
				comments[activityId] = [];
				if (res.data.comments) {
					comments[activityId] = res.data.comments;
				}
				renderCommentList(comments[activityId], true);
			} else {
			  debug.warn('Comment.get: res.data is null!!!');
			}
			gadgets.window.adjustHeight();
		})
	})();
	
  	var getDeleteContentBlock = function() {
  		var html = [];
		html.push('<div class="ActionContent">');
			html.push('<a id="DeleteCommentButton' + commentId + '" class="ActionDeleteButton" href="#action-delete">&nbsp;</a>');
		html.push('</div>');
  		return html.join('');
  	}
	
  	var getCommentBlock = function() {
  		var html = [],
  			inlineStyle = 'style="background:none; padding-top:2px;"';
  		//if the first or no like => keep it
  		if (!commentListBlockEl.hasChildNodes()) {
  			inlineStyle = '';
  		}
  		var activityOwner = statusUpdate.getPerson(comment.userId);
  		if (activityOwner) {
  			var userName = activityOwner.getDisplayName();
  			var profileUrl = activityOwner.getField(opensocial.Person.Field.PROFILE_URL);
  		} else {
  			debug.warn('activityOwner is null!');
  		}
		html.push('<div class="CommentContent"');
			html.push(inlineStyle);
		html.push('>');
			html.push('<div class="CommentBorder">')
  				html.push('<div class="CommentActivitiesContent">');
  					html.push('<a href="' + profileUrl + '" target="_parent" title="' + userName + '" class="AvatarPeopleBG">');
  						html.push('<img height="47px" width="47px" src="' + statusUpdate.getAvatar(comment.userId) + '" />');
  					html.push('</a>');
  					html.push('<div class="Content">');
  						html.push('<div class="Titlecontent" style="height: 24px;">');
  							html.push('<div class="UserName"><a href="' + profileUrl + '" target="_parent">' + userName + '</a></div>');
  						var viewerId = statusUpdate.viewer.getId();
  						if ((viewerId === activityUserId) || (viewerId === commentUserId)) {
  							html.push(getDeleteContentBlock());
  						}
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
		debug.error('commentEl is null!');
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
			Util.hideElement(commentButtonId);
			this.value = Locale.getMsg('write_a_comment');
			this.style.height = '20px';
			this.style.color = 'gray';
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
		if (body === '' || body === Locale.getMsg('write_a_comment')) return;
		body = body.replace(/</g, '&#60;').replace(/>/g, '&#62;').replace(/"/g, '&#34;');
		var activity = {
			'userId': userId,
			'title': title,
			'body': body
		}
		
		eXo.social.Comment.create(activityId, activity, function(res) {
			commentTextareaEl.disabled = false;
			commentButtonEl.disabled = false;
			if (res.rc === 404) { //activity deleted
			   //alert(Locale.getMsg('error_activity_not_found'));
			   Util.removeElementById('Activity' + eXo.social.Comment.currentActivityId);
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
			}
			if (res.data != null) { //succeeded
				commentTextareaEl.value = '';
				commentTextareaEl.focus();
				activityId = res.data.activityId;
				comment = res.data.comments[0];
				commentId = comment.id;
				commentUserId = comment.userId;
				if (!comments[activityId]) {
					comments[activityId] = [];
				}
				var stripedCommentBody = Util.stripHtml(eXo.social.StatusUpdate.allowedTags, comment.body);
				if (stripedCommentBody !== '') {
					comment.body = stripedCommentBody;
				}
				comments[activityId].push(comment);
				commentListBlockEl.style.display = 'block';
				var newEl = Util.addElement(commentListBlockId, 'div', 'CommentBlock' + commentId, getCommentBlock());
				if (updateNumComment) {
					var html = [];
					html.push('<div class="CommentBlock">');
						html.push('<div class="CommentContent">');
							html.push('<div class="CommentBorder">');
								html.push('<a id="' + commentLinkId + '" href="#show-all-comments">' + Locale.getMsg('show_all_num_comments', [comments[activityId].length]) + '</a>');
							html.push('</div>');
						html.push('</div>');
					html.push('</div>');
					commentListInfoEl.innerHTML = html.join('');
					var commentLinkEl = Util.getElementById(commentLinkId);
					commentLinkEl.onclick = function() {
						eXo.social.Comment.get(activityId, function(res) {
							if (res.data !== null) {
								hideAll = false;
								comments[activityId] = res.data.comments;
								renderCommentList(comments[activityId], false);
								gadgets.window.adjustHeight();
							}
						})
					}
				}
				newEl.setAttribute('class', 'CommentBlock');
				newEl.setAttribute('className', 'CommentBlock');
				newEl.className = 'CommentBlock';
				setDeleteComment(commentId);
				gadgets.window.adjustHeight();
			} else { //failed
				//alert(Locale.getMsg('internal_error'));
				debug.warn('post comment failed!');
			}
		});
	}, false);
}