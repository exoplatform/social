/**
 * UIActivity.js
 */

(function() {
	var window_ = this,
		Util = eXo.social.Util;
	
	function UIActivity() {
	}
	
	UIActivity.prototype.init = function(params) {
		this.activityId = params.activityId || null;
		this.inputWriteAComment = params.inputWriteAComment || "";
		this.commentFormDisplayed = params.commentFormDisplayed || false;
		this.allCommentsDisplayed = params.allCommentsDisplayed || false;
		if (this.activityId == null) {
			alert('err: activityId is null!')
		}
		this.commentLinkId = 'CommentLink' + this.activityId;
		//this.likeLinkId = 'LikeLink'  + this.activityId;
		this.commentFormBlockId = 'CommentFormBlock' + this.activityId;
		this.commentTextareId = 'CommentTextarea' + this.activityId;
		this.commentButtonId = 'CommentButton' + this.activityId;
		
		this.commentLinkEl = Util.getElementById(this.commentLinkId);
		//this.likeLinkEl = Util.getElementById(this.likeLinkId);
		this.commentFormBlockEl = Util.getElementById(this.commentFormBlockId);
		this.commentTextareaEl = Util.getElementById(this.commentTextareId);
		this.commentButtonEl = Util.getElementById(this.commentButtonId);
		
		if (!(this.commentLinkEl && this.commentFormBlockEl && this.commentTextareaEl && this.commentButtonEl)) {
			alert('err: init uiActivity!');
		}
		if (this.commentFormDisplayed) {
			Util.showElement(this.commentFormBlockId);
			this.commentTextareaEl.value = this.inputWriteAComment;
		} else {
			Util.hideElement(this.commentFormBlockId);
			this.commentTextareaEl.value = this.inputWriteAComment;
		}
		var uiActivity = this;
		//event handlers
		Util.addEventListener(this.commentLinkEl, 'click', function(evt) {
			if (uiActivity.commentFormBlockEl.style.display != 'block') {
				Util.showElement(uiActivity.commentFormBlockId);
				uiActivity.commentTextareaEl.focus();
			} else {
				Util.hideElement(uiActivity.commentFormBlockId);
			}
		}, false);
		
		Util.addEventListener(this.commentTextareaEl, 'focus', function(evt) {
			this.style.height = '50px';
			this.style.color = '#000000';
			if (this.value === uiActivity.inputWriteAComment) {
				this.value = '';
			}
			Util.showElement(uiActivity.commentButtonId);
		}, false);
		
		Util.addEventListener(this.commentTextareaEl, 'blur', function(evt) {
			if (this.value === '') {
				Util.hideElement(uiActivity.commentButtonId);
				this.value = uiActivity.inputWriteAComment;
				this.style.height = '20px';
				this.style.color = 'gray';
			}
		}, false);
		
		
	}
	//expose
	window_.eXo = window_.eXo || {};
	window_.eXo.social = window_.eXo.social || {};
	window_.eXo.social.webui = window_.eXo.social.webui || {};
	window_.eXo.social.webui.UIActivity = UIActivity;
})();