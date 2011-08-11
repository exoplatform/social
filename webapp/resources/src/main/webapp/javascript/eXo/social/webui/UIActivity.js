/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */

/**
 * UIActivity.js
 */

(function() {
  var window_ = this,
  Util = eXo.social.Util,
  COMMENT_BLOCK_BOUND_CLASS_NAME = "CommentBlockBound",
  COMMENT_BLOCK_BOUND_NONE_CLASS_NAME = "CommentBlockBoundNone",
  DEFAULT_COMMENT_TEXT_AREA_HEIGHT = "30px",
  FOCUS_COMMENT_TEXT_AREA_HEIGHT = "30px",
  FOCUS_COMMENT_TEXT_AREA_COLOR = "#000000",
  DEFAULT_COMMENT_TEXT_AREA_COLOR = "#808080";

  function UIActivity(params) {
    this.configure(params);
    this.init();
  }

  UIActivity.prototype.configure = function(params) {
    this.activityId = params.activityId || null;
    this.inputWriteAComment = params.inputWriteAComment || "";
    this.commentMinCharactersAllowed = params.commentMinCharactersAllowed || 0;
    this.commentMaxCharactersAllowed = params.commentMaxCharactersAllowed || 0;
    this.commentFormDisplayed = params.commentFormDisplayed || false;
    this.allCommentsDisplayed = params.allCommentsDisplayed || false;
    this.commentFormFocused = params.commentFormFocused || false;
    if (this.activityId == null) {
      alert('err: activityId is null!');
      return;
    }
    this.commentLinkId = 'CommentLink' + this.activityId;
    //this.likeLinkId = 'LikeLink'  + this.activityId;
    this.commentFormBlockId = 'CommentFormBlock' + this.activityId;
    this.commentTextareId = 'CommentTextarea' + this.activityId;
    this.commentButtonId = 'CommentButton' + this.activityId;
    this.contentBoxId = 'ContextBox' + this.activityId;
    this.deleteActivityButtonId = 'DeleteActivityButton' + this.activityId;
    this.allCommentSize = parseInt(params.allCommentSize);
    this.commentBlockBoundId = "CommentBlockBound" + this.activityId;
  }

  UIActivity.prototype.init = function() {
    this.commentLinkEl = Util.getElementById(this.commentLinkId);
    //this.likeLinkEl = Util.getElementById(this.likeLinkId);
    this.commentFormBlockEl = Util.getElementById(this.commentFormBlockId);
    this.commentTextareaEl = Util.getElementById(this.commentTextareId);
    this.commentButtonEl = Util.getElementById(this.commentButtonId);
    this.contentBoxEl = Util.getElementById(this.contentBoxId);
    this.deleteActivityButtonEl = Util.getElementById(this.deleteActivityButtonId);
    this.commentBlockBoundEl = Util.getElementById(this.commentBlockBoundId);
    
    if (!(this.commentFormBlockEl && this.commentTextareaEl && this.commentButtonEl)) {
      alert('err: init uiActivity!');
    }
    
    this.commentBlockBoundEl.className=COMMENT_BLOCK_BOUND_NONE_CLASS_NAME;
    
    if (this.commentFormDisplayed) {
      this.commentTextareaEl.style.height = DEFAULT_COMMENT_TEXT_AREA_HEIGHT;
      this.commentTextareaEl.style.color = DEFAULT_COMMENT_TEXT_AREA_COLOR;
      if (this.commentTextareaEl.value === this.inputWriteAComment) {
        this.commentTextareaEl.value = '';
      }
      this.commentBlockBoundEl.className = COMMENT_BLOCK_BOUND_CLASS_NAME;
    } else {
      if (this.allCommentSize == 0) {
        this.commentBlockBoundEl.className = COMMENT_BLOCK_BOUND_NONE_CLASS_NAME;
      } else {
        this.commentBlockBoundEl.className = COMMENT_BLOCK_BOUND_CLASS_NAME;
      }
    }

    var uiActivity = this;
    if (this.commentLinkEl) {
      //event handlers
      Util.addEventListener(this.commentLinkEl, 'click', function(evt) {
        if (uiActivity.commentFormBlockEl.style.display != 'block') {
          if (uiActivity.allCommentSize == 0) {
            uiActivity.commentBlockBoundEl.className = COMMENT_BLOCK_BOUND_CLASS_NAME;
          }
          Util.showElement(uiActivity.commentFormBlockId);
          uiActivity.commentTextareaEl.focus();
        } else {
          if (uiActivity.allCommentSize == 0) {
            uiActivity.commentBlockBoundEl.className = COMMENT_BLOCK_BOUND_NONE_CLASS_NAME;
          }
          Util.hideElement(uiActivity.commentFormBlockId);
        }
      }, false);

      Util.addEventListener(this.commentTextareaEl, 'focus', function(evt) {
        this.style.height = FOCUS_COMMENT_TEXT_AREA_HEIGHT;
        this.style.color = FOCUS_COMMENT_TEXT_AREA_COLOR;
        if (this.value === uiActivity.inputWriteAComment) {
          this.value = '';
        }
        Util.showElement(uiActivity.commentButtonId);
      }, false);

      Util.addEventListener(this.commentTextareaEl, 'blur', function(evt) {
        if (this.value === '') {
          Util.hideElement(uiActivity.commentButtonId);
          this.value = uiActivity.inputWriteAComment;
          this.style.height = DEFAULT_COMMENT_TEXT_AREA_HEIGHT;
          this.style.color = DEFAULT_COMMENT_TEXT_AREA_COLOR;
        }
      }, false);

      if (this.commentFormDisplayed) {
        Util.showElement(this.commentFormBlockId);
        this.commentTextareaEl.value = this.inputWriteAComment;
        if (this.commentFormFocused) {
          this.commentTextareaEl.focus();
        }
      } else {
        Util.hideElement(this.commentFormBlockId);
        this.commentTextareaEl.value = this.inputWriteAComment;
      }
    } else {
      Util.hideElement(this.commentFormBlockId);
    }

    if (this.deleteActivityButtonEl !== null) {
      Util.addEventListener(this.contentBoxEl, 'mouseover', function(evt) {
        uiActivity.deleteActivityButtonEl.className = 'CloseContentBoxHilight';
      }, false);

      Util.addEventListener(this.contentBoxEl, 'mouseout', function(evt) {
        uiActivity.deleteActivityButtonEl.className = 'CloseContentBoxNormal';
      }, false);
    }

  }
  //expose
  window_.eXo = window_.eXo || {};
  window_.eXo.social = window_.eXo.social || {};
  window_.eXo.social.webui = window_.eXo.social.webui || {};
  window_.eXo.social.webui.UIActivity = UIActivity;
})();