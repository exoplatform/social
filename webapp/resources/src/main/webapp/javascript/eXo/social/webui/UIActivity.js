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
(function ($, _) {  
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
        UIActivity.deleteCommentButtonIds[i - 1] = "DeleteCommentButton" + UIActivity.activityId + i;
        UIActivity.commentBlockIds[i - 1] = "CommentBlock" + UIActivity.activityId + i;
      }
    }
  },
  init: function() {
    UIActivity.commentLinkEl = $("#"+UIActivity.commentLinkId);
    UIActivity.commentFormBlockEl = $("#" + UIActivity.commentFormBlockId);
    UIActivity.commentTextareaEl = $("#" + UIActivity.commentTextareId);
    UIActivity.commentButtonEl = $("#" + UIActivity.commentButtonId).show();
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
      alert('err: init UIActivity!');
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
    };

    var commentLinkEl = $("#"+UIActivity.commentLinkId);
    if (commentLinkEl.length > 0) {
      
      //event handlers
      commentLinkEl.on( 'click', function(evt) {
        var commentForms = $("[id^='CommentFormBlock']");
        var currentActivityId = $(this).attr('id').replace('CommentLink', '');
        var thiscommentBlockId = 'CommentFormBlock' + currentActivityId;
        $.each(commentForms, function(idx, el) {
          if ( $(el).attr('id') !== thiscommentBlockId ) {
            if (UIActivity.allCommentSize == 0) {
              $("#" + UIActivity.commentBlockBoundId).attr('class', UIActivity.COMMENT_BLOCK_BOUND_NONE_CLASS_NAME);
            } else {
              $("#" + UIActivity.commentBlockBoundId).attr('class', UIActivity.COMMENT_BLOCK_BOUND_CLASS_NAME);
            }
            
            $(el).hide();
          } else {
            $(el).show();
          }
        });
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
     // $("#" + UIActivity.commentFormBlockId).hide();
    }

    if (UIActivity.deleteActivityButtonEl.length !== 0) {
      var deleteActivityButtonEl = $("#" + UIActivity.deleteActivityButtonId);
      UIActivity.activityContextBoxEl.on('mouseover focus', function(evt) {
        deleteActivityButtonEl.attr('class', 'CloseContentBoxHilight');
      });

      UIActivity.activityContextBoxEl.on('mouseout blur', function(evt) {
        deleteActivityButtonEl.attr('class', 'CloseContentBoxNormal');
      });

    }

    if (UIActivity.allCommentSize > 0) {
      $.each(UIActivity.deleteCommentButtonEls, function(idx, el) {
        UIActivity.commentBlockEls[idx].on('mouseover focus', function(evt) {
          el.attr('class','CloseContentBoxHilight');
        });
        
        UIActivity.commentBlockEls[idx].on('mouseout blur', function(evt) {
          el.attr('class','CloseContentBoxNormal');
        });
        
      });

    }
    
	//
    $('textarea#CommentTextarea' + UIActivity.activityId).exoMentions({
        onDataRequest:function (mode, query, callback) {
          var url = window.location.protocol + '//' + window.location.host + '/' + eXo.social.portal.rest + '/social/people/getprofile/data.json?search='+query;
          $.getJSON(url, function(responseData) {
            responseData = _.filter(responseData, function(item) { 
              return item.name.toLowerCase().indexOf(query.toLowerCase()) > -1;
            });
            callback.call(this, responseData);
          });
        },
        idAction : ('CommentButton'+UIActivity.activityId),
        elasticStyle : {
          maxHeight : '35px',
          minHeight : '22px'
        },
        messages : window.eXo.social.I18n.mentions
      });
  }
};

return UIActivity;
})($, mentions._);
