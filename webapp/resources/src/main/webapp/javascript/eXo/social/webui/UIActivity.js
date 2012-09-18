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

var UIActivity = {
  COMMENT_BLOCK_BOUND_CLASS_NAME : "CommentBlockBound",
  COMMENT_BLOCK_BOUND_NONE_CLASS_NAME : "CommentBlockBoundNone",
  DEFAULT_COMMENT_TEXT_AREA_HEIGHT : "30px",
  FOCUS_COMMENT_TEXT_AREA_HEIGHT : "30px",
  FOCUS_COMMENT_TEXT_AREA_COLOR : "#000000",
  DEFAULT_COMMENT_TEXT_AREA_COLOR : "#808080",
  onLoad: function (params) {
    UIActivity.configure(params);
    UIActivity.init();
  },
  configure: function(params) {
    UIActivity.activityId = params.activityId || null;
    UIActivity.inputWriteAComment = params.inputWriteAComment || "";
    UIActivity.commentMinCharactersAllowed = params.commentMinCharactersAllowed || 0;
    UIActivity.commentMaxCharactersAllowed = params.commentMaxCharactersAllowed || 0;
    UIActivity.commentFormDisplayed = params.commentFormDisplayed == "true" ? true : false || false;
    UIActivity.allCommentsDisplayed = params.allCommentsDisplayed = "true" ? true : false || false;
    UIActivity.commentFormFocused = params.commentFormFocused = "true" ? true : false  || false;
    if (UIActivity.activityId == null) {
      alert('err: activityId is null!');
      return;
    }
    UIActivity.commentLinkId = 'CommentLink' + UIActivity.activityId;
    //UIActivity.likeLinkId = 'LikeLink'  + UIActivity.activityId;
    UIActivity.commentFormBlockId = 'CommentFormBlock' + UIActivity.activityId;
    UIActivity.commentTextareId = 'CommentTextarea' + UIActivity.activityId;
    UIActivity.commentButtonId = 'CommentButton' + UIActivity.activityId;
    UIActivity.deleteCommentButtonIds = [];
    UIActivity.contentBoxId = 'ContextBox' + UIActivity.activityId;
    UIActivity.deleteActivityButtonId = 'DeleteActivityButton' + UIActivity.activityId;
    UIActivity.allCommentSize = parseInt(params.allCommentSize);
    UIActivity.commentBlockBoundId = "CommentBlockBound" + UIActivity.activityId;
    UIActivity.commentBlockIds = [];
    UIActivity.activityContextBoxId = "ActivityContextBox" + UIActivity.activityId;
    if (UIActivity.allCommentSize > 0) {
      for (var i = 1; i <= UIActivity.allCommentSize; i++) {
        UIActivity.deleteCommentButtonIds[i - 1] = 'DeleteCommentButton' + UIActivity.activityId + i;
        UIActivity.commentBlockIds[i - 1] = "CommentBlock" + UIActivity.activityId + i;
      }
    }
  },
  init: function() {
    UIActivity.commentLinkEl = $("#"+UIActivity.commentLinkId);
    UIActivity.commentFormBlockEl = $("#" + UIActivity.commentFormBlockId);
    UIActivity.commentTextareaEl = $("#" + UIActivity.commentTextareId);
    UIActivity.commentButtonEl = $("#" + UIActivity.commentButtonId);
    UIActivity.deleteCommentButtonEls = [];
    UIActivity.contentBoxEl = $(UIActivity.contentBoxId);
    UIActivity.deleteActivityButtonEl = $("#" + UIActivity.deleteActivityButtonId);
    UIActivity.commentBlockBoundEl = $("#" + UIActivity.commentBlockBoundId);
    UIActivity.commentBlockEls = [];
    UIActivity.activityContextBoxEl = $("#" + UIActivity.activityContextBoxId);
    if(UIActivity.allCommentSize > 0) {
      for(var i=0; i < UIActivity.allCommentSize; i++) {
        UIActivity.deleteCommentButtonEls[i] = $("#" + UIActivity.deleteCommentButtonIds[i]);
        UIActivity.commentBlockEls[i] = $("#" + UIActivity.commentBlockIds[i]);
      }
    }
    
    if (!(UIActivity.commentFormBlockEl && UIActivity.commentTextareaEl && UIActivity.commentButtonEl)) {
      alert('err: init uiActivity!');
    }
    
    UIActivity.commentBlockBoundEl.attr('class', UIActivity.COMMENT_BLOCK_BOUND_NONE_CLASS_NAME);
    
    if (UIActivity.commentFormDisplayed) {
      UIActivity.commentTextareaEl.css('height', UIActivity.DEFAULT_COMMENT_TEXT_AREA_HEIGHT);
      UIActivity.commentTextareaEl.css('color', UIActivity.DEFAULT_COMMENT_TEXT_AREA_COLOR);
      if (UIActivity.commentTextareaEl.val() === UIActivity.inputWriteAComment) {
        UIActivity.commentTextareaEl.val('');
      }
      UIActivity.commentBlockBoundEl.attr('class', UIActivity.COMMENT_BLOCK_BOUND_CLASS_NAME);
    } else {
      if (UIActivity.allCommentSize == 0) {
        UIActivity.commentBlockBoundEl.attr('class', UIActivity.COMMENT_BLOCK_BOUND_NONE_CLASS_NAME);
      } else {
        UIActivity.commentBlockBoundEl.attr('class', UIActivity.COMMENT_BLOCK_BOUND_CLASS_NAME);
      }
    }

    var uiActivity = this;
    if (UIActivity.commentLinkEl) {
      //event handlers
      UIActivity.commentLinkEl.on( 'click', function(evt) {
        if (uiActivity.commentFormBlockEl.css('display') != 'block') {
          if (uiActivity.allCommentSize == 0) {
            uiActivity.commentBlockBoundEl.attr('class', UIActivity.COMMENT_BLOCK_BOUND_CLASS_NAME);
          }
          $("#" + uiActivity.commentFormBlockId).show();
          uiActivity.commentTextareaEl.focus();
        } else {
          if (uiActivity.allCommentSize == 0) {
            uiActivity.commentBlockBoundEl.attr('class', UIActivity.COMMENT_BLOCK_BOUND_NONE_CLASS_NAME);
          }
          $("#" + uiActivity.commentFormBlockId).hide();
        }
      });

      UIActivity.commentTextareaEl.on('focus', function(evt) {
        $(this).css('height', UIActivity.FOCUS_COMMENT_TEXT_AREA_HEIGHT);
        $(this).css('color', UIActivity.FOCUS_COMMENT_TEXT_AREA_COLOR);
          if ($(this).val() === uiActivity.inputWriteAComment) {
          $(this).val('');
        }
        $("#" + uiActivity.commentButtonId).show();
      });

      UIActivity.commentTextareaEl.on('blur', function(evt) {
        if ($(this).val() === '') {
          $("#" + uiActivity.commentButtonId).hide();
          $(this).val(uiActivity.inputWriteAComment);

          $(this).css('height', UIActivity.DEFAULT_COMMENT_TEXT_AREA_HEIGHT);
          $(this).css('color', UIActivity.DEFAULT_COMMENT_TEXT_AREA_COLOR);
        }
      });

      if (UIActivity.commentFormDisplayed) {
        $("#" + UIActivity.commentFormBlockId).show();
        UIActivity.commentTextareaEl.val(UIActivity.inputWriteAComment);
        if (UIActivity.commentFormFocused) {
          UIActivity.commentTextareaEl.focus();
        }
      } else {
        $("#" + UIActivity.commentFormBlockId).hide();
        UIActivity.commentTextareaEl.val(UIActivity.inputWriteAComment);
      }
    } else {
      $("#" + UIActivity.commentFormBlockId).hide();
    }

    if (UIActivity.deleteActivityButtonEl.length !== 0) {
      UIActivity.activityContextBoxEl.on('mouseover focus', function(evt) {
        uiActivity.deleteActivityButtonEl.attr('class', 'CloseContentBoxHilight');
      });

      UIActivity.activityContextBoxEl.on('mouseout blur', function(evt) {
        uiActivity.deleteActivityButtonEl.attr('class', 'CloseContentBoxNormal');
      });

    }

    if (UIActivity.allCommentSize > 0) {

      for (var i = 0; i < UIActivity.allCommentSize; i++) {
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
}

 _module.UIActivity = UIActivity;
