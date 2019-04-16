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
  COMMENT_BLOCK_BOUND_CLASS_NAME : "commentBox commentBlockBound ",
  COMMENT_BLOCK_BOUND_NONE_CLASS_NAME : " commentBox commentBlockBoundNone",
  DEFAULT_COMMENT_TEXT_AREA_HEIGHT : "28px",
  FOCUS_COMMENT_TEXT_AREA_HEIGHT : "28px",
  FOCUS_COMMENT_TEXT_AREA_COLOR : "#999999",
  DEFAULT_COMMENT_TEXT_AREA_COLOR : "#808080",
  isLoadLike : false,
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
    UIActivity.inputContainer = $("#InputContainer" + UIActivity.activityId);
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
    
    var commentLinkEl = $("#" + UIActivity.commentLinkId);
    if (commentLinkEl.length > 0) {
      commentLinkEl.off('click').on('click', function (evt) {
        if (UIActivity.checkDevice().isMobile === true) {
          $('.footComment').find('div.replaceTextArea:first').focus();
          return true;
        }
        //
        var currentActivityId = $(this).attr('id').replace('CommentLink', '');
        var inputContainer = $('#InputContainer' + currentActivityId).fadeToggle('fast', function () {
          var thiz = $(this);
          if(thiz.css('display') === 'block') {
            var blockInput = thiz.parents('.uiActivityStream:first').find('.inputContainerShow');
            if(blockInput.length > 0) {
              blockInput.removeClass('inputContainerShow').hide();
            }
            thiz.addClass('inputContainerShow');
            thiz.find('div.replaceTextArea:first').focus();
            var ctTop = ($(window).height()- thiz.height())/2;
            var nTop = thiz.offset().top - ctTop - 20;
            nTop = (nTop > 0) ? nTop : 0;
            
            $('html, body').animate({scrollTop:nTop}, 'slow');
          } else {
            thiz.removeClass('inputContainerShow')
          }
        });
      });
    }
    
    //
    $('textarea#CommentTextarea' + UIActivity.activityId).exoMentions({
        onDataRequest:function (mode, query, callback) {
          var url = window.location.protocol + '//' + window.location.host + '/' + eXo.social.portal.rest + '/social/people/getprofile/data.json?search='+query;
          $.getJSON(url, function(responseData) {
            callback.call(this, responseData);
          });
        },
        idAction : ('CommentButton'+UIActivity.activityId),
        elasticStyle : {
          maxHeight : '52px',
          minHeight : '22px',
          marginButton: '4px',
          enableMargin: false
        },
        messages : window.eXo.social.I18n.mentions
    });
  
    var actionDeletes = $('a.controllDelete');
    if (actionDeletes.length > 0) {
      actionDeletes.off('click').on('click',
        function(e) {
          $('.currentDeleteActivity:first').removeClass('currentDeleteActivity');
          var jElm = $(this);
          jElm.addClass('currentDeleteActivity');
          var id = jElm.attr('id');
          if(id == null || id.length == 0) {
            $('#SocialCurrentConfirm').removeAttr('id');
            id = "SocialCurrentConfirm";
            jElm.attr('id', id)
          }
          var confirmText = jElm.attr('data-confirm');
          var captionText = jElm.attr('data-caption');
          var confirmButton = jElm.attr('data-ok');
          var cancelButton = jElm.attr('data-close');
          eXo.social.PopupConfirmation.confirm(id, [{action: UIActivity.removeActivity, label : confirmButton}], captionText, confirmText, cancelButton);
        }
      );
    }
  },
  displayLikes : function() {
    if(UIActivity.isLoadLike === true) {
      return;
    }
    //
    var rootLoader = $('div#UIActivitiesLoader');
    var activities = rootLoader.find('.activityStream');
    //
    $.each(activities, function(idx, activity) {
      UIActivity.displayLike(activity);
    }) ;
    //
    var t = setTimeout(function(){
      UIActivity.isLoadLike = false; clearTimeout(t);
    }, 400);
  },
  displayLike : function(activity) {
    UIActivity.isLoadLike = true;
    var likeBox = $(activity).find('.listLikedBox:first');
    if(likeBox.length > 0 && likeBox.attr('data-load') !== 'false') {
      var likeContainer = likeBox.find('.listLiked:first');
      var moreBtn = likeContainer.find('button.btn').hide();
      var mWith = likeContainer.width();
      var items = likeContainer.find('a');
      items.show();
      var allMumber = items.length;
      if(allMumber > 0) {
        var maxItemDisplay = Math.floor(mWith/(items.eq(0).outerWidth() + 12))*1;
        for(var i = maxItemDisplay; i < allMumber; ++i) {
          items.eq(i).hide();
          if (i === (allMumber - 1)) {
            moreBtn.show();
            items.eq(maxItemDisplay-1).hide();
          }
        }
      }
    }
  },
  loadLikes : function () {
      var contentBoxEl = $('#'+UIActivity.contentBoxId);
      var listLiked = $(contentBoxEl).find('.listLiked').find('a').show();
      UIActivity.isLoadLike = true;
  },
  removeActivity : function () {
    var jElm = $('.currentDeleteActivity:first');
    var idElm = jElm.attr('id');
    jElm.removeClass('currentDeleteActivity');
    if (idElm.indexOf('Activity') > 0) { // remove activity
      var idActivty = idElm.replace('DeleteActivityButton', '')
      $('#activityContainer' + idActivty).css('overflow', 'hidden').animate(
        {
          height : '1px',
          opacity : '0'
        }, 500,
        function() {
          $(this).removeClass('activityStream');
          window.eval(jElm.attr('data-delete').replace('javascript:', ''));
          $('.iconReturn').trigger('click');
        });
    } else if (idElm.indexOf('Comment') > 0) { // remove comment
      var idComment = idElm.replace('DeleteCommentButton', '')
      $('#commentContainer' + idComment).css('overflow', 'hidden')
      .animate(
        {
          height : '1px',
          opacity : '0.1'
        }, 300,
        function() {
          $(this).hide();
          window.eval(jElm.attr('data-delete').replace('javascript:', ''));
        }
      );
    }
  },
  
  hightlightComment : function(activityId) {
    var isReply = window.location.href.indexOf("comment=1") > 0;
    var anchor = window.location.hash;
    var anchor_pattern = "#comment-([\\w|/]+|)";
    var result = anchor.match(anchor_pattern);
    if (result != null) {
      var jcomment = $('#commentContainer' + result[1]).addClass('focus');
      if (isReply) {
        this.replyByURL(activityId);
      } else if (jcomment.length > 0) {
        jcomment[0].scrollIntoView(true);
      }
    }
  },

  focusToComment : function() {  
    var comment = $('#commentContainer' + $.getQuery('commentId'));
    if(comment.length > 0) {
      comment.addClass('focus')[0].scrollIntoView(true);
    }
  },

  replyByURL : function(activityId) {
    $(document).ready(function() {
      var actionComment = '#CommentLink' + activityId;
      var cmAction = $(actionComment);
      if (cmAction.length > 0) {
        setTimeout(function() {
          cmAction.trigger('click');
        }, 500);
      }
    });
  },

  setPageTitle : function(activityTitle) {
    $(document).attr('title', 'Activity: ' + $('<div></div>').html(window.decodeURIComponent(activityTitle)).text());
  },
  
  loadLikersByURL : function() {
    $(document).ready(function() {
      var contentBoxEl = $('#' + UIActivity.contentBoxId);
      var listLiked = $(contentBoxEl).find('.listLiked');
      setTimeout(function() {
        listLiked.find('.btn').trigger('click');
      }, 500);
    });
  },
  checkDevice : function() {
    var body = $('body:first').removeClass('phoneDisplay').removeClass('tabletDisplay').removeClass('tabletLDisplay');
    var isMobile = body.find('.visible-phone:first').css('display') !== 'none';
    var isTablet = body.find('.visible-tablet:first').css('display') !== 'none';
	  var isTabletL = body.find('.visible-tabletL:first').css('display') !== 'none';
    if (isMobile) {
      body.addClass('phoneDisplay');
    }
    if (isTablet) {
      body.addClass('tabletDisplay');
    }
    if (isTabletL) {
      body.addClass('tabletLDisplay');
    }
    return {'isMobile' : isMobile, 'isTablet' : isTablet, 'isTabletL' : isTabletL};
  },
  hasClass : function(target, cssClass) {
    return (target.hasClass(cssClass) || target.find('>.' + cssClass).length > 0)
  },
  resetRightHeight : function() {
    var wHeight = $(window).height();
    var leftBody = $('td.LeftNavigationTDContainer.TDContainer:first').height('auto');
    var rightTD = $('td.RightBodyTDContainer.TDContainer:first').attr('style', '');
    $('#UIUserActivityStreamPortlet').height('auto');
    var T = setTimeout(function(){
      rightTD.css('min-height', Math.max(wHeight, leftBody.height()) + 'px');
      clearTimeout(T);
    },1200);
  },
  activityOnClick : function(evt) {
    if(UIActivity.checkDevice().isMobile === true) {
      var activity = $(this);
      if(activity.hasClass('uiActivityLoader')) {
        activity = activity.find('.activityStream:first');
      }
      var target = $(evt.target);
      if(activity.length === 0 ||
          activity.hasClass('block-activity') ||
          UIActivity.hasClass(target, 'uiIconWatch') ||
          UIActivity.hasClass(target, 'uiIconDownload') ||
          UIActivity.hasClass(target, 'uiIconThumbUp') ||
          UIActivity.hasClass(target, 'uiIconLink') ||
          UIActivity.hasClass(target, 'uiIconClose')) {
        return true;
      }
      //$('html, body').animate({scrollTop: 16}, 'slow');
      //
      var parent = $('#' + UIActivity.responsiveId);
      parent.find('.activityStream').addClass('hidden-phone');
      //
      UIActivity.resetRightHeight();
      //
      var activityDisplay = parent.find('div.uiActivitiesDisplay:first').addClass('activityDisplay');
      activityDisplay.find('.activityTop').addClass('hidden-phone');

      if(activityDisplay.find('.iconReturn').length === 0) {
        activityDisplay.prepend($('<div class="visible-phone" style="cursor:pointer"><i class="uiIconEcmsDarkGray uiIconEcmsReturn iconReturn"></i></div>').click(function() {
          var parent = $('#' + UIActivity.responsiveId);
          parent.find('div.uiActivitiesDisplay:first').removeClass('activityDisplay');
          parent.find('.activityStream').removeClass('hidden-phone');
          parent.find('.activityTop').removeClass('hidden-phone');
          var activity = parent.find('.block-activity').removeClass('block-activity');
          if (activity.length > 0) {
            $('html, body').animate({scrollTop:activity.offset().top - activity.height() + 62}, 'slow');
          }
          //
          $('.footComment').html('');
          $(this).remove();
        }));
      }
      //
      activity.removeClass('hidden-phone').addClass('block-activity');
      //
      var listLiker = activity.find('.listLiked:first');
      if(listLiker.find('a').length > 0) {
        listLiker.find('button:first').trigger('click');
      }
      //
      var footComment = $('.footComment');
      if(footComment.length === 0) {
        footComment = $('<div class="footComment visible-phone" style="z-index:1000"></div>');
        $('body').append(footComment);
      }
      var input = activity.find('.inputContainer:first').clone().removeClass('hidden-phone');
      window.inputId = input.attr('id');
      input.attr('id', 'CurrentCommentInput');
      input.find('.exo-mentions').remove();
      input.find('button.btn:first').attr('id', 'CurrentCommentButton');
      footComment.html('').append(input);
      footComment.find('textarea.textarea:first').attr('id', 'CurrentCommentTextare').exoMentions({
        onDataRequest:function (mode, query, callback) {
          var url = window.location.protocol + '//' + window.location.host + '/' + eXo.social.portal.rest + '/social/people/getprofile/data.json?search='+query;
          $.getJSON(url, function(responseData) {
            callback.call(this, responseData);
          });
        },
        idAction : ('CurrentCommentButton'),
        elasticStyle : {
          maxHeight : '42px',
          minHeight : '32px',
          marginButton: '4px',
          enableMargin: false
        },
        messages : window.eXo.social.I18n.mentions
      });
      //
      var widthBtn = footComment.find('#CurrentCommentButton').on('click keyup', function(evt) {
        if(evt.type === 'keyup' && evt.keyCode !== 13) {
          return false;
        }
        var value = $(this).parents('.footComment').find('textarea.textarea:first').val();
        $('#' + window.inputId).find('textarea.textarea:first').val(value);
        var t = setTimeout(function() {
          clearTimeout(t);
          $.globalEval($('#' + window.inputId).find('button.btn:first').data('action-link'));
        }, 100);
      }).outerWidth();
      footComment.find('.commentInput:first').css('margin-right', widthBtn + 18 + 'px');
      //
      var commentList = activity.find('.commentListInfo:first');
      if(commentList.length > 0 && commentList.find('a:first').length > 0) {
        var action = commentList.find('a:first').attr('onclick');
        if(action && action.length > 0) {
          $.globalEval(action.replace('objectId=none', 'objectId=all'));
        }
      }
    }
  },
  responsiveMobile : function(id) {
    //
    if (typeof id === 'undefined' && UIActivity.responsiveId) {
      id = UIActivity.responsiveId
    } else {
      UIActivity.responsiveId = id;
    }
    var deviceInfo = UIActivity.checkDevice();
    if(deviceInfo.isMobile === true || deviceInfo.isTablet === true ||  deviceInfo.isTabletL === true  ) {
      UIActivity.resetRightHeight();
    }
    var root = $('#'+id);
    if(root.length > 0 && deviceInfo.isMobile === true) {
      var hidenComposer = function(elm) {
        $('#' + UIActivity.responsiveId).find('.uiActivitiesDisplay:first').removeClass('hidden-phone');
        return elm.parents('.uiComposer:first').addClass('hidden-phone');
      };
      //
      root.find('.changeStatus').click(function(evt) {
        UIActivity.resetRightHeight();
        //
        var parent = $(this).parents('#' + UIActivity.responsiveId);
        parent.find('.uiActivitiesDisplay:first').addClass('hidden-phone');
        if(parent.find('div.uiComposer.hidden-phone').length > 0) {
          parent.find('div.uiComposer.hidden-phone').removeClass('hidden-phone');
        }
        //
        var composer = parent.find('.uiComposer:first');
        composer.find('.replaceTextArea').focus();
        composer.find('.button-group').find('.btn-cancel').off('click').click(function() {
          hidenComposer($(this));
        });
        composer.find('.button-group').find('.btn-submit').prop('disabled', true).off('click').click(function() {
          hidenComposer($(this)).find('#ShareButton').trigger('mousedown');
        });
        //
        composer.find('textarea#composerInput').exoMentions('registerControlButton', function(status) {
          var btnSubmit = $('#' + UIActivity.responsiveId).find('.uiComposer:first').find('.btn-submit');
          if(status === true) {
            btnSubmit.removeAttr('disabled');
          } else {
            btnSubmit.prop('disabled', true);
          }
        });
      });
      //
      if ($('.activityDisplay').length === 0) {
        $('.inputContainer').addClass('hidden-phone');
      }
      //activityStream
      var activities = root.find('.activityStream');
      if(activities.length === 0) {
        activities = root.find('.uiActivityLoader');
      }
      //
      activities.off('click').click(UIActivity.activityOnClick);
    }
  }
};
//
eXo.social.SocialUtil.addOnResizeWidth(function(evt){UIActivity.responsiveMobile()});
return UIActivity;
})($, mentions._);
