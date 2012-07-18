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
    this.deleteCommentButtonIds = [];
    this.contentBoxId = 'ContextBox' + this.activityId;
    this.deleteActivityButtonId = 'DeleteActivityButton' + this.activityId;
    this.allCommentSize = parseInt(params.allCommentSize);
    this.commentBlockBoundId = "CommentBlockBound" + this.activityId;
    this.commentBlockIds = [];
    this.activityContextBoxId = "ActivityContextBox" + this.activityId;
    if (this.allCommentSize > 0) {
      for (var i = 1; i <= this.allCommentSize; i++) {
        this.deleteCommentButtonIds[i - 1] = 'DeleteCommentButton' + this.activityId + i;
        this.commentBlockIds[i - 1] = "CommentBlock" + this.activityId + i;
      }
    }
  }

  UIActivity.prototype.init = function() {
    this.commentLinkEl = gj("#"+this.commentLinkId);
    this.commentFormBlockEl = gj("#" + this.commentFormBlockId);
    this.commentTextareaEl = gj("#" + this.commentTextareId);
    this.commentButtonEl = gj("#" + this.commentButtonId);
    this.deleteCommentButtonEls = [];
    this.contentBoxEl = gj(this.contentBoxId);
    this.deleteActivityButtonEl = gj("#" + this.deleteActivityButtonId);
    this.commentBlockBoundEl = gj("#" + this.commentBlockBoundId);
    this.commentBlockEls = [];
    this.activityContextBoxEl = gj("#" + this.activityContextBoxId);
    if(this.allCommentSize > 0) {
      for(var i=0; i < this.allCommentSize; i++) {
        this.deleteCommentButtonEls[i] = gj("#" + this.deleteCommentButtonIds[i]);
        this.commentBlockEls[i] = gj("#" + this.commentBlockIds[i]);
      }
    }
    
    if (!(this.commentFormBlockEl && this.commentTextareaEl && this.commentButtonEl)) {
      alert('err: init uiActivity!');
    }
    
    this.commentBlockBoundEl.attr('class',COMMENT_BLOCK_BOUND_NONE_CLASS_NAME);
    
    if (this.commentFormDisplayed) {
      this.commentTextareaEl.css('height',DEFAULT_COMMENT_TEXT_AREA_HEIGHT);
      this.commentTextareaEl.css('color', DEFAULT_COMMENT_TEXT_AREA_COLOR);
      if (this.commentTextareaEl.val() === this.inputWriteAComment) {
        this.commentTextareaEl.val('');
      }
      this.commentBlockBoundEl.attr('class',COMMENT_BLOCK_BOUND_CLASS_NAME);
    } else {
      if (this.allCommentSize == 0) {
        this.commentBlockBoundEl.attr('class',COMMENT_BLOCK_BOUND_NONE_CLASS_NAME);
      } else {
        this.commentBlockBoundEl.attr('class', COMMENT_BLOCK_BOUND_CLASS_NAME);
      }
    }

    var uiActivity = this;
    if (this.commentLinkEl) {
      //event handlers
      this.commentLinkEl.on( 'click', function(evt) {
        if (uiActivity.commentFormBlockEl.css('display') != 'block') {
          if (uiActivity.allCommentSize == 0) {
            uiActivity.commentBlockBoundEl.attr('class',COMMENT_BLOCK_BOUND_CLASS_NAME);
          }
          gj("#" + uiActivity.commentFormBlockId).show();
          uiActivity.commentTextareaEl.focus();
        } else {
          if (uiActivity.allCommentSize == 0) {
            uiActivity.commentBlockBoundEl.attr('class',COMMENT_BLOCK_BOUND_NONE_CLASS_NAME);
          }
          gj("#" + uiActivity.commentFormBlockId).hide();
        }
      });

      this.commentTextareaEl.on('focus', function(evt) {
        gj(this).css('height', FOCUS_COMMENT_TEXT_AREA_HEIGHT);
        gj(this).css('color',FOCUS_COMMENT_TEXT_AREA_COLOR);
          if (gj(this).val() === uiActivity.inputWriteAComment) {
          gj(this).val('');
        }
        gj("#" + uiActivity.commentButtonId).show();
      });

      this.commentTextareaEl.on('blur', function(evt) {
        if (gj(this).val() === '') {
          gj("#" + uiActivity.commentButtonId).hide();
          gj(this).val(uiActivity.inputWriteAComment);

          gj(this).css('height', DEFAULT_COMMENT_TEXT_AREA_HEIGHT);
          gj(this).css('color',DEFAULT_COMMENT_TEXT_AREA_COLOR);
        }
      });

      if (this.commentFormDisplayed) {
        gj("#" + this.commentFormBlockId).show();
        this.commentTextareaEl.val(this.inputWriteAComment);
        if (this.commentFormFocused) {
          this.commentTextareaEl.focus();
        }
      } else {
        gj("#" + this.commentFormBlockId).hide();
        this.commentTextareaEl.val(this.inputWriteAComment);
      }
    } else {
      gj("#" + this.commentFormBlockId).hide();
    }

    if (this.deleteActivityButtonEl !== null) {
      this.activityContextBoxEl.on('mouseover focus', function(evt) {
        uiActivity.deleteActivityButtonEl.attr('class', 'CloseContentBoxHilight');
      });

      this.activityContextBoxEl.on('mouseout blur', function(evt) {
        uiActivity.deleteActivityButtonEl.attr('class', 'CloseContentBoxNormal');
      });

    }

    if (this.allCommentSize > 0) {

      for (var i = 0; i < this.allCommentSize; i++) {
        (function(i) {
          //when this element is displayed
          if (uiActivity.commentBlockEls[i]) {
            uiActivity.commentBlockEls[i].on('mouseover focus', function(evt) {
              uiActivity.deleteCommentButtonEls[i].attr('class','CloseContentBoxHilight');
            });

            uiActivity.commentBlockEls[i].on('mouseout blur', function(evt) {
              uiActivity.deleteCommentButtonEls[i].attr('class','CloseContentBoxNormal');
            });
          }
        })(i);
      }
    }
  }

  window_.eXo.social.webui.UIActivity = UIActivity;
})();
