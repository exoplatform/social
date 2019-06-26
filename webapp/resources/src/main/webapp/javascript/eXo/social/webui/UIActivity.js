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
      UIActivity.commentPlaceholder = params.placeholderComment || null;
      UIActivity.spaceURL = params.spaceURL;
      UIActivity.spaceGroupId = params.spaceGroupId;
      UIActivity.labels = params.labels;

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
      UIActivity.permaLinkActivityButtonId = 'Permalink' + UIActivity.activityId;
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
      var commentButton = $("#" + UIActivity.commentButtonId);
      commentButton.click(function(event) {
        var commentId = commentButton.data("comment-id");
        var clickAction = commentButton.data("click").replace("COMMENTID", (commentId ? commentId : ""));
        eval(clickAction);
      });
    },

    initCKEditor: function (activityId, spaceURL, commentPlaceholder, spaceGroupId) {
      var extraPlugins = 'simpleLink,selectImage,suggester,hideBottomToolbar';
      var windowWidth = $(window).width();
      var windowHeight = $(window).height();
      if (windowWidth > windowHeight && windowWidth < 768) {
        // Disable suggester on smart-phone landscape
        extraPlugins = 'simpleLink,selectImage';
      }

      var MAX_LENGTH = 2000;
      // TODO this line is mandatory when a custom skin is defined, it should not be mandatory
      CKEDITOR.basePath = '/commons-extension/ckeditor/';

      $('textarea#CommentTextarea' + activityId).ckeditor({
        customConfig: '/commons-extension/ckeditorCustom/config.js',
        extraPlugins: extraPlugins,
        removePlugins: 'image',
        placeholder: commentPlaceholder != null ? commentPlaceholder : window.eXo.social.I18n.mentions.defaultMessage,
        activityId : activityId,
        extraAllowedContent: 'img[style,class,src,referrerpolicy,alt,width,height]',
        spaceURL: spaceURL,
        spaceGroupId: spaceGroupId,
        typeOfRelation: 'mention_comment',
        on : {
          instanceReady : function ( evt ) {
            var data = this.getData();
            if(data && data.trim()) {
              this.setData("");
            }
            // Hide the editor toolbar
            var elId = this.element.$.id.replace('CommentTextarea','');
            $('#CommentButton' + elId).prop("disabled", true);
          },
          change: function( evt) {
            var newData = evt.editor.getData();
            var pureText = newData? newData.replace(/<[^>]*>/g, "").replace(/&nbsp;/g,"").trim() : "";
            var elId = this.element.$.id.replace('CommentTextarea','');
            var disabled = pureText.length == 0 || pureText.length > MAX_LENGTH;
            disabled = disabled ? (newData.indexOf("<img ") < 0) : false;

            if (disabled) {
              $('#CommentButton' + elId).prop("disabled", true);
            } else {
              $('#CommentButton' + elId).removeAttr("disabled");
            }

            if (pureText.length <= MAX_LENGTH) {
              evt.editor.getCommand('selectImage').enable();
            } else {
              evt.editor.getCommand('selectImage').disable();
            }
          },
          key: function( evt) {
            var newData = evt.editor.getData();
            var pureText = newData? newData.replace(/<[^>]*>/g, "").replace(/&nbsp;/g,"").trim() : "";
            if (pureText.length > MAX_LENGTH) {
              if ([8, 46, 33, 34, 35, 36, 37,38,39,40].indexOf(evt.data.keyCode) < 0) {
                evt.cancel();
              }
            }
          }
        }
      });
    },

    initCKEditorForActivityEditing: function(currentComposerEditInput, $contentBox, $editButton) {
      var MAX_LENGTH = 2000;
      var extraPlugins = 'simpleLink,selectImage,suggester,hideBottomToolbar';
      var windowWidth = $(window).width();
      var windowHeight = $(window).height();
      if (windowWidth > windowHeight && windowWidth < 768) {
        // Disable suggester on smart-phone landscape
        extraPlugins = 'simpleLink,selectImage';
      }
      var tempdiv = $("<div class='temp'/>").append($contentBox.html());
      tempdiv.find('a[href*="/profile"]').each(function( index ) {
        $(this).replaceWith(function() { return $('<span/>', {
         class:'atwho-inserted',
         html: '<span class="exo-mention">'+$(this).text()+'</span>'}).attr("data-atwho-at-query","@"+$(this).attr("href").substring($(this).attr("href").lastIndexOf("/")+1)).attr("data-atwho-at-value",$(this).attr("href").substring($(this).attr("href").lastIndexOf("/")+1)).attr(
                                   "contenteditable","true");
        });
      });
      var spaceURL = UIActivity.spaceURL;
      var spaceGroupId = UIActivity.spaceGroupId;
      var commentPlaceholder = UIActivity.commentPlaceholder;
      var activityId = UIActivity.activityId;
      $('#' + currentComposerEditInput).ckeditor({
        customConfig: '/commons-extension/ckeditorCustom/config.js',
        extraPlugins: extraPlugins,
        removePlugins: 'image',
        placeholder: commentPlaceholder != null ? commentPlaceholder : window.eXo.social.I18n.mentions.defaultMessage,
        activityId : activityId,
        extraAllowedContent: 'img[style,class,src,referrerpolicy,alt,width,height]; span(*)[*]{*}; span[data-atwho-at-query,data-atwho-at-value,contenteditable]; a[*];i[*]',
        spaceURL: spaceURL,
        spaceGroupId: spaceGroupId,
        typeOfRelation: 'mention_comment',
        on : {
          instanceReady : function ( evt ) {
            $editButton.prop("disabled", true);
          },
          change: function( evt) {
            var newData = evt.editor.getData();
            var originalComment = $contentBox.html();
            var pureText = newData? newData.replace(/<[^>]*>/g, "").replace(/&nbsp;/g,"").trim() : "";
            var disabled = (originalComment == newData) || pureText.length > MAX_LENGTH || pureText.length == 0;
            disabled = disabled ? (newData.indexOf("<img ") < 0) : false;

            if (disabled) {
               $editButton.prop("disabled", true);
            } else {
               $editButton.removeAttr("disabled");
            }
          },
          key: function( evt) {}
        }
      });

      CKEDITOR.instances[currentComposerEditInput].setData(tempdiv.html());
    },

    init: function() {
      var self = this;
      UIActivity.commentLinkEl = $("#"+UIActivity.commentLinkId);
      UIActivity.commentFormBlockEl = $("#" + UIActivity.commentFormBlockId);
      UIActivity.commentTextareaEl = $("#" + UIActivity.commentTextareId);
      UIActivity.commentButtonEl = $("#" + UIActivity.commentButtonId).show();
      UIActivity.deleteCommentButtonEls = [];
      UIActivity.contentBoxEl = $("#" + UIActivity.contentBoxId);
      UIActivity.deleteActivityButtonEl = $("#" + UIActivity.deleteActivityButtonId);
      UIActivity.permaLinkActivityButtonEl = $("#" + UIActivity.permaLinkActivityButtonId);
      UIActivity.commentBlockBoundEl = $("#" + UIActivity.commentBlockBoundId);
      UIActivity.inputContainer = $("#InputContainer" + UIActivity.activityId);
      UIActivity.editInputContainer = $("#EditInputContainer" + UIActivity.activityId);
      UIActivity.commentBlockEls = [];
      UIActivity.activityContextBoxEl = $("#" + UIActivity.activityContextBoxId);
      if(UIActivity.allCommentSize > 0) {
        for(var i=0; i < UIActivity.allCommentSize; i++) {
          UIActivity.deleteCommentButtonEls[i] = $("#" + UIActivity.deleteCommentButtonIds[i]);
          UIActivity.commentBlockEls[i] = $("#" + UIActivity.commentBlockIds[i]);
        }
      }

      var contentBoxEl = this.contentBoxEl;
      var activityId = UIActivity.activityId;
      UIActivity.contentBoxEl.find(".subCommentShowAllLink").on("click", function() {
        var parentCommentId = $(this).attr('data-parent-comment');

        $('#SubCommentShowAll_' + parentCommentId).hide();
        contentBoxEl.find('[data-parent-comment=' + parentCommentId + ']').removeClass('hidden');

        UIActivity.addLastCommentParamToLikeLink(activityId, parentCommentId);
      });
      if(window.lastExpandedComment && window.lastExpandedComment[UIActivity.activityId]) {
        var parentCommentId = window.lastExpandedComment[UIActivity.activityId];
        this.addLastCommentParamToLikeLink(UIActivity.activityId, parentCommentId);
      }

      window.takeActionFromLikeComment = UIActivity.takeActionFromLikeComment;

      if (!(UIActivity.commentFormBlockEl && UIActivity.commentTextareaEl && UIActivity.commentButtonEl)) {
        alert('err: init UIActivity!');
      }

      UIActivity.permaLinkActivityButtonEl.off('click').on('click', function(evt) {
        evt.stopPropagation();
      });

      var activityId = UIActivity.activityId;
      var spaceURL = UIActivity.spaceURL;
      var spaceGroupId = UIActivity.spaceGroupId;
      var commentPlaceholder = UIActivity.commentPlaceholder;

      var commentLinkEl = $("[data-activity='" + activityId + "']");
      if (commentLinkEl.length > 0) {
        commentLinkEl.off('click').on('click', function (evt) {
          var currentActivityId = $(this).attr('data-activity');
          var currentCommentId = $(this).attr('data-comment');
          var currentSubCommentId = $(this).attr('data-sub-comment');
          var commentButton = $("[data-comment-button='" + activityId + "']");
          var inputContainer = $('#InputContainer' + currentActivityId);

          if(currentCommentId) {
            commentButton.data("comment-id", currentCommentId);

            // Move CKEditor instance to comment block
            inputContainer.addClass("subCommentBlock");
            inputContainer.insertAfter($("[data-comment-id=" + currentCommentId + "]").last());
          } else {
            commentButton.data("comment-id", null);

            // Move CKEditor instance to activity comment block
            inputContainer.removeClass("subCommentBlock");
            inputContainer.appendTo("#CommentBlockBound" + activityId);
          }

          inputContainer = $('#InputContainer' + currentActivityId);
          var inputContainerCommentId = inputContainer.data("comment-id");
          var inputContainerSubCommentId = inputContainer.data("comment-sub-id");
          inputContainerCommentId = inputContainerCommentId ? inputContainerCommentId : undefined;
          inputContainerSubCommentId = inputContainerSubCommentId ? inputContainerSubCommentId : undefined;
          var instanciateCKEditor = !inputContainer.is(":visible") || inputContainerCommentId !== currentCommentId || inputContainerSubCommentId !== currentSubCommentId;
          if (instanciateCKEditor) {
            try {
              if(CKEDITOR.instances['CommentTextarea' + activityId]) {
                  CKEDITOR.instances['CommentTextarea' + activityId].destroy();
              }
            } catch(e){
              console.log(e);
            }

            inputContainer.data("comment-id", currentCommentId ? currentCommentId : "");
            inputContainer.data("comment-sub-id", currentSubCommentId ? currentSubCommentId : "");

            self.initCKEditor(activityId, spaceURL, commentPlaceholder, spaceGroupId);

            inputContainer.show('fast', function () {
              var thiz = $(this);
              thiz.addClass('inputContainerShow');
              thiz.removeClass('hidden-phone');
              thiz.find('div.replaceTextArea:first').focus();

              if($('#cke_CommentTextarea' + currentActivityId + ' .cke_contents').length) {
                $('#cke_CommentTextarea' + currentActivityId + ' .cke_contents')[0].style.height = "110px";
              }

              var selectedComment = currentSubCommentId ? currentSubCommentId : currentCommentId;
              self.focusToComment(selectedComment, thiz);
            });
          } else {
            if (eXo.social.SocialUtil.checkDevice().isMobile !== true) {
              inputContainer.hide('fast', function() {
                var thiz = $(this);
                thiz.removeClass('inputContainerShow');
              });
          }
          else {
              var thiz = $(this);
              var blockInput = thiz.parents('.uiActivityStream:first').find('.inputContainerShow');
              if(blockInput.length > 0) {
                blockInput.removeClass('inputContainerShow').hide();
              }
            }
		}
          commentLinkEl.closest('.activityStream').click();
        });
        }

        var editActivityEl = $("[data-edit-activity='" + activityId + "']");
        if (editActivityEl.length > 0) {
          editActivityEl.off('click').on('click', function (evt) {
            evt.stopPropagation();
            var currentComposerEditInput = 'composerEditInput' + activityId;
            $('#dropDownEditActivity'+ activityId + '.actLink').removeClass("open");
            $('#dropDownEditActivity'+ activityId + '.actionLink').removeClass("open");

            if (!$('#ActivityContextBox'+activityId+' .description').is(":visible") &&
                $('#ActivityContextBox'+activityId+' .blastInputPeople').first().is(":visible")) {
              try {
               if(CKEDITOR.instances[currentComposerEditInput]) {
                   CKEDITOR.instances[currentComposerEditInput].destroy();
               }
              } catch(e){
               console.log(e);
              }
              $('textarea#composerEditInput' + activityId).hide();
              $('#ActivityContextBox'+activityId+' .blastInputPeople').first().hide();
              $('#ActivityContextBox'+activityId+' .description').show();

            } else {
              $('#ActivityContextBox'+activityId+' .blastInputPeople').first().show();
              $('#ActivityContextBox'+activityId+' .description').hide();
              $('textarea#composerEditInput' + activityId).show();
              $('.dropdown-backdrop').remove();

              self.initCKEditorForActivityEditing(currentComposerEditInput, $('#ActivityContextBox'+activityId+' .description'), $('#EditActivityButton' + activityId));
            }
          });

        }

       $("[data-edit-comment]").each(function(){
        var editCommentEl = $(this);
       if (editCommentEl.length > 0) {
              var commentId = $(this).data('edit-comment');
              $('textarea#composerEditComment' + commentId).hide();
              editCommentEl.off('click').on('click', function (evt) {
              evt.stopPropagation();
              var currentComposerEditComment = 'composerEditComment' + commentId;
              var smallScreen = $(window).outerWidth(true) <= 768;
              if (($('#commentContainer'+commentId+' .contentComment').is(":visible"))){
                if (smallScreen) {
                  $('#commentContainer' + commentId + " .desktop-input").append('<div class="backdrop"></div>');
                } else {
                  $('#commentContainer'+commentId+' .contentComment').hide();
                }
                $('textarea#composerEditComment' + commentId).show();
                $('#dropDownEditComment'+ commentId).hide();
                $('#commentContainer'+commentId+' .blastInputPeople').first().show();

                self.initCKEditorForActivityEditing(currentComposerEditComment, $('#commentContainer'+commentId+' .contentComment'), $('#EditCommentButton' + commentId));
             }
             else {
             try {
                           if(CKEDITOR.instances[currentComposerEditComment]) {
                               CKEDITOR.instances[currentComposerEditComment].destroy();
                           }
                         } catch(e){
                           console.log(e);
                         }
             $('#commentContainer'+commentId+' .blastInputPeople').first().hide();
             $('textarea#composerEditComment' + commentId).hide();
             $('#commentContainer'+commentId+' .contentComment').show();
             $('.commentBox .backdrop').remove();
             $('.dropdown-backdrop').remove();
             }
        });


        }


       });

       $("[data-edit-comment-id]").each(function(){
               var editCommentButton = $(this);
                     editCommentButton.off('click').on('click',function(event) {
                       event.stopPropagation();
                       var commentId = editCommentButton.data("edit-comment-id");
                       var clickAction = editCommentButton.data("click").replace("COMMENTID", (commentId ? commentId : ""));
                       eval(clickAction);
                     });


       });
       $("[data-cancel-edit-comment-id]").each(function(){
                      var cancelEditCommentButton = $(this);
                      cancelEditCommentButton.click(function(event) {
                        event.stopPropagation();
                        var commentId = cancelEditCommentButton.data("cancel-edit-comment-id");
                        $('.commentBox .backdrop').remove();
                        $('.dropdown-backdrop').remove();
                        if (commentId.toString().indexOf('comment') >= 0){
                        var currentComposerEditComment = 'composerEditComment' + commentId;
                        try {
                              if(CKEDITOR.instances[currentComposerEditComment]) {
                              CKEDITOR.instances[currentComposerEditComment].destroy();
                              }
                            } catch(e){
                              console.log(e);
                        }
                        $('textarea#composerEditComment' + commentId).hide();
                        $('#commentContainer'+commentId+' .contentComment').show();
                        $('#dropDownEditComment'+ commentId).show();
                        $('#dropDownEditComment'+ commentId).removeClass("open");
                        $('#commentContainer'+commentId+' .blastInputPeople').hide();
                        }
                        else{
                           var currentComposerEditInput = 'composerEditInput' + commentId;
                            try {
                                if(CKEDITOR.instances[currentComposerEditInput]) {
                                    CKEDITOR.instances[currentComposerEditInput].destroy();
                                }
                                } catch(e){
                                   console.log(e);
                                }
                            $('textarea#composerEditInput' + commentId).hide();
                            $('#ActivityContextBox'+commentId+' .description').show();
                            $('#dropDownEditActivity'+ commentId).show();
                            $('#dropDownEditActivity'+ commentId).removeClass("open");
                            $('#ActivityContextBox'+commentId+' .blastInputPeople').hide();
                        }
                        });
                       });

        $("#CancelButton" + UIActivity.activityId).on('click', function (evt) {
          var currentActivityId = evt.target.id.replace('CancelButton', '');
          var inputContainer = $('#InputContainer' + currentActivityId);
          var thiz = $(this);
          if(thiz.css('display') === 'block') {
            var blockInput = thiz.parents('.uiActivityStream:first').find('.inputContainerShow');
            if(blockInput.length > 0) {
              blockInput.removeClass('inputContainerShow').hide();
            }
            thiz.addClass('inputContainerShow');
            thiz.find('div.replaceTextArea:first').focus();
          } else {
            thiz.removeClass('inputContainerShow');
          }

        });


      var actionDeletes = $('a.controllDelete');
      if (actionDeletes.length > 0) {
        actionDeletes.off('click').on('click', function(e) {
              e.stopPropagation();
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

      this.adaptFileBreadCrumb(UIActivity.activityId);

      // click on "like comments" buttons
      $('#ContextBox'+UIActivity.activityId+' a[id*="LikeCommentLink_"]').each(function (idx, el) {
        var id = $(el).attr('id');
        var commentId = id.substring(id.indexOf('_') + 1);
        $(el).click(function(){
          UIActivity.showLikersPopup(commentId);
        });
      });
    },

    addLastCommentParamToLikeLink: function (activityId, parentCommentId){
      var linkElement = $("#LikeLink"+activityId);
      if(linkElement.length == 0) {
        linkElement = $("#UnLikeLink"+activityId);
      }
      var clickAction = linkElement.attr("onclick");
      if(clickAction && linkElement.length > 0) {
        clickAction = clickAction.replace("&objectId=", "&commentId=" + parentCommentId + "&objectId=");
        linkElement.attr("onclick", clickAction);
        window.lastExpandedComment = {};
        window.lastExpandedComment[activityId] = parentCommentId;
      }
      linkElement = $("#LikeCommentLink"+parentCommentId);
      clickAction = linkElement.attr("onclick");
      if(clickAction && linkElement.length > 0) {
        clickAction = clickAction.replace("&objectId=", "&commentId=" + parentCommentId + "&objectId=");
        linkElement.attr("onclick", clickAction);
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
        if (isReply) {
          this.replyByURL(activityId);
        } else {
          this.focusToComment(result[1]);
        }
      }
    },

    focusToComment : function(commentId, elementToScrollTo, delayToDisableFocus) {
      var comment = $('#commentContainer' + commentId);
      var ele = elementToScrollTo;
      var subCommentParentId = comment.data('parent-comment');
      var subCommentShowAll = $('#SubCommentShowAll_' + subCommentParentId);

      // show subComments if exist before calculate scroll value
      if(subCommentShowAll.length > 0) {
        subCommentShowAll.find('.subCommentShowAllLink').trigger('click');
      }

      if(comment.length > 0) {
        $("div[id^='commentContainer']").removeClass("focus");

        var nTop = 0;
        if (!elementToScrollTo || eXo.social.SocialUtil.checkDevice().isMobile === true) {
          if(commentId) {
            ele = $("#commentContainer" + commentId);
          }
        }

        if(commentId) {
          var eleToFocus = $("#commentContainer" + commentId);
          if(eleToFocus.length > 0 && eleToFocus.offset()) {
            eleToFocus.addClass('focus');
            if(delayToDisableFocus && delayToDisableFocus > 0) {
              setTimeout(function() {
                eleToFocus.removeClass('focus', delayToDisableFocus);
              }, 2000);
            }
          }
        }
      }
      if(ele) {
        var nTop = ele.offset().top - $(window).height() / 2 + ele.height() / 2;
        $('html, body').animate({scrollTop:nTop}, 'slow');
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
    hideLink : function() {
      //$('textarea#composerInput').exoMentions('reset');
      // This is work around, if we call method setData('') of ckeditor,
      // it will cancel the suggester plugin due to it replace all content in iframe
      var editor = CKEDITOR.instances["composerInput"];
      if (editor.mode != 'source') {
        if (editor.document) $(editor.document.getBody().$).html('');
      } else {
        $(editor.container.$).find(".cke_source").html('');
      }
      $('.composerLimited').addClass('hide');

      var container = $('#ComposerContainer');
      var link = container.find('#LinkExtensionContainer');
      if (link.length > 0) {
        if (link.css('display') !== 'none') {
          link.hide();
        }
      }

      var cmp = container.find('.uiLinkShareDisplay');
      if (cmp.length > 0) {
        var closeLink = cmp.find('a.uiIconClose');
        if (closeLink) {
          closeLink.trigger('click');
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
      var deviceInfo = eXo.social.SocialUtil.checkDevice();
      var root = $('#'+id);
      if(root.length > 0 && deviceInfo.isMobile === true) {
        var hideComposer = function(elm) {
          UIActivity.hideLink();
          root.find('.uiActivitiesDisplay:first').removeClass('hidden-phone');
          return elm.parents('.uiComposer:first').addClass('hidden-phone');
        };
        //
        root.find('.changeStatus').click(function(evt) {
          UIActivity.resetRightHeight();
          //
          var parent = root;
          parent.find('.uiActivitiesDisplay:first').addClass('hidden-phone');
          if(parent.find('div.uiComposer.hidden-phone').length > 0) {
            parent.find('div.uiComposer.hidden-phone').removeClass('hidden-phone');
          }

          var composer = parent.find('.uiComposer:first');
          composer.find('.replaceTextArea').focus();
          composer.find('.button-group').find('.btn-cancel').off('click').click(function() {
            hideComposer($(this));
          });
          composer.find('.button-group').find('.btn-submit').prop('disabled', true);
          composer.find('.share-buttons-top').find('.btn-submit').off('click').click(function() {
            parent.find('#ShareButton').trigger('onclick');
            hideComposer($(this));
          });
          composer.find('#ShareButton').off('click').click(function() {
            hideComposer($(this));
          });
          //
          CKEDITOR.instances["composerInput"].focus();

          /*composer.find('textarea#composerInput').exoMentions('registerControlButton', function(status) {
           var btnSubmit = $('#' + UIActivity.responsiveId).find('.uiComposer:first').find('.btn-submit');
           if(status === true) {
           btnSubmit.removeAttr('disabled');
           } else {
           btnSubmit.prop('disabled', true);
           }
           });*/
        });
        //
        var btnGroup = root.find('.button-group');
        if (btnGroup) {
          btnGroup.find('.btn-cancel').off('click').click(function() {
            hideComposer($(this));
          });
          btnGroup.find('.btn-submit').off('click').click(function() {
            root.find('#ShareButton').trigger('mousedown');
            hideComposer($(this));
          });
        }
      }
    },

    /**
     * show/hide the ellipsis on the left of file breadcrumb is it is overflowed on window resizing
     */
    adaptFileBreadCrumb : function(activityId) {
      var ellipsisReverseContent = null;
      var breadcrumbs =  null;
      if(activityId) {
        ellipsisReverseContent = $('#ContextBox' + activityId + " .ellipsis-reverse");
        breadcrumbs = $('#ContextBox'+activityId + " .fileBreadCrumb");
      }
      if (!ellipsisReverseContent || !ellipsisReverseContent.length) {
        ellipsisReverseContent = $('.ellipsis-reverse');
        breadcrumbs = $('.fileBreadCrumb');
      }

      // for each selected old breadcrumb preview
      breadcrumbs.each(function() {
        var breadcrumbContent = $(this).find('.fileBreadCrumbContent');
        // do not process empty breadcrumbs
        if(breadcrumbContent.length > 0) {
          var breadcrumbSpan = breadcrumbContent.find('span');
          if(breadcrumbContent.width() < breadcrumbSpan.width()) {
            // if the breadcrumb is overflowed, display the ellipsis
            var ellipsis = $(this).find('.fixedBreadCrumb');
            ellipsis.addClass('active');
          } else {
            // otherwise, hide it
            var ellipsis = $(this).find('.fixedBreadCrumb');
            ellipsis.removeClass('active');
          }
        }
      })

      // for each selected multiupload preview breadcrumb
      ellipsisReverseContent.each(function() {
        var $ellipsisContent = $(this).find(".ellipsis-reverse-content");
        var $ellipsisApplyContent = $(this).find(".ellipsis-reverse-apply-content");
        var $ellipsisFirstChild = $ellipsisApplyContent.find(' :first-child');
        if(!$ellipsisApplyContent.length
            || !$ellipsisContent.length
            || !$(this).offset()
            || !$ellipsisFirstChild
            || !$ellipsisFirstChild.offset()
            || !$ellipsisFirstChild.length) {
          return;
        }
        $ellipsisContent.addClass("hidden");
        $ellipsisApplyContent.css("position", "static");
        var applyEllipsisReverse = $ellipsisFirstChild.offset().left - $(this).offset().left - 15;
        if(applyEllipsisReverse < 0) {
          $ellipsisContent.removeClass("hidden");
          $ellipsisApplyContent.css("position", "absolute");
        }
      })
    },

    /**
     * prepare Popup of commennt Likers
    */
    buildLikersPopupSkeleton: function() {
      var likersPopupSkeleton = "<div id=\"likersPopupMask\">" +
        "  <div id=\"likersPopup\" class=\"UIPopupWindow uiPopup UIDragObject NormalStyle\">" +
        "    <div class=\"popupHeader ClearFix\">" +
        "      <a class=\"uiIconClose pull-right\" aria-hidden=\"true\" data-dismiss=\"modal\" ></a>" +
        "      <span class=\"PopupTitle popupTitle\">" + UIActivity.labels.LikePopupTitle + "</span>" +
        "    </div>" +
        "    <div class=\"PopupContent popupContent\">" +
        "      <ul id=\"likersDetail\">" +
        "      </ul>" +
        "    </div>" +
        "  </div>" +
        "</div>";
      $("body").append(likersPopupSkeleton);

      $("#likersPopup .uiIconClose").click(function(){
        $("#likersPopupMask").hide();
        $("#likersPopup .PopupContent #likersDetail").empty();
      });
    },

    showLikersPopup: function (commentId) {
      var likersPopup = $("#likersPopup");
      if(likersPopup.length == 0) {
        UIActivity.buildLikersPopupSkeleton();
      }

      $("#likersPopupMask").show();

      var env = eXo.social.portal;
      var restUrl = env.context + '/' + env.rest + '/v1/social/comments/' + commentId + '/likes';
      $.ajax({
        type: "GET",
        cache: false,
        url: restUrl,
        complete: function (jqXHR) {
          if (jqXHR.readyState === 4) {
            var dataLikers = $.parseJSON(jqXHR.responseText);

            if (dataLikers) {
              var likers = dataLikers.likes.map(function(like) {
                return like.username;
              });

              // fetch relationships with likers
              var relationshipsRestUrl = env.context + '/' + env.rest + '/v1/social/usersRelationships?others=' + likers.join(',') + '&expand=sender,receiver&fields=sender,receiver,status';
              $.ajax({
                type: "GET",
                cache: false,
                url: relationshipsRestUrl,
                complete: function (jqXHR) {
                  if (jqXHR.readyState === 4) {
                    var dataRelationships = $.parseJSON(jqXHR.responseText);
                    dataLikers.likes.reverse();
                    UIActivity.buildLikersPopup(dataLikers.likes, dataRelationships.usersRelationships);
                  }
                }
              });
            }
          }
        }
      });
    },

    buildRelationshipButton: function (likerUsername, relationshipSender, relationshipStatus) {
      var actionButton = $('<div/>', {
        "class": "connectConnection btn btn-primary",
        "data-action": "Invite:" + likerUsername,
        "onclick": "takeActionFromLikeComment(this)"
      });
      actionButton.append($('<span/>', {
        "text": "" + UIActivity.labels.Connect
      }));
      actionButton.append($('<i/>', {
        "class": "uiIconSocConnectUser"
      }));

      var relationStatus = relationshipStatus ? relationshipStatus.toLowerCase() : relationshipStatus;
      if (relationStatus == "pending") {
        if(relationshipSender == likerUsername) { // Viewer is not owner
          actionButton = $('<div/>', {
            "class": "confirmConnection btn btn-primary",
            "data-action": "Accept:" + likerUsername,
            "onclick": "takeActionFromLikeComment(this)"
          });
          actionButton.append($('<span/>', {
            "text": "" + UIActivity.labels.Confirm
          }));
          actionButton.append($('<i/>', {
            "class": "uiIconSocAcceptConnectUser"
          }));
        } else { // Viewer is owner
          actionButton = $('<div/>', {
            "class": "cancelConnection btn",
            "data-action": "Revoke:" + likerUsername,
            "onclick": "takeActionFromLikeComment(this)"
          });
          actionButton.append($('<span/>', {
            "text": "" + UIActivity.labels.CancelRequest
          }));
          actionButton.append($('<i/>', {
            "class": "uiIconSocCancelConnectUser"
          }));
        }
      } else if (relationStatus == "confirmed") { // Has Connection
        actionButton = $('<div/>', {
          "class": "removeConnection btn",
          "data-action": "Disconnect:" + likerUsername,
          "onclick": "takeActionFromLikeComment(this)"
        });
        actionButton.append($('<span/>', {
          "text": "" + UIActivity.labels.RemoveConnection
        }));
        actionButton.append($('<i/>', {
          "class": "uiIconSocCancelConnectUser"
        }));
      } else if (relationStatus == "ignored") { // Connection is removed
        actionButton = $('<div/>', {
          "class": "ignoreConnection btn",
          "data-action": "Deny:" + likerUsername,
          "onclick": "takeActionFromLikeComment(this)"
        });
        actionButton.append($('<span/>', {
          "text": "" + UIActivity.labels.Ignore
        }));
        actionButton.append($('<i/>', {
          "class": "uiIconSocCancelConnectUser"
        }));
      }

      return actionButton;
    },

    buildLikersPopup: function(likers, usersRelationships){
      var env = eXo.social.portal;

      var likersList = $("#likersPopup .PopupContent #likersDetail");
      likersList.empty();
      for (i = 0; i < likers.length; i++) {
        var likerUsername = likers[i].username;
        var likerFullname = likers[i].fullname;
        var likerAvatarUrl = env.context + "/" + env.rest + "/v1/social/users/" + likerUsername + "/avatar";
        var likerProfileUrl = env.context + "/" + env.portalName + "/profile/" + likerUsername;

        var likerItem = $('<li/>', {"class":"liker"});
        var likerAvatar = $('<div/>', {"class":"likerAvatar"});
        var imgAvatar = $("<img/>", {
          "src": likerAvatarUrl
        });

        var aAvatar = $("<a/>", {
          "target": "_self",
          "href": likerProfileUrl
        });

        aAvatar.append(imgAvatar);
        likerAvatar.append(aAvatar);
        likerItem.append(likerAvatar);

        var likerProfile = $("<div/>",{
          "class": "likerName"
        });
        var aProfile = $("<a/>", {
          "target": "_self",
          "href": likerProfileUrl,
          "text": likerFullname
        });

        likerItem.append(likerProfile.append(aProfile));

        var likerAction = $("<div/>",{
          "class": "likeClick"
        });
        var divActionContainer = $("<div/>",{
          "class": "uiActionLike"
        });

        //Add Connection Action
        var action = null;
        var currentViewerId = env.userName;
        if (currentViewerId != likerUsername) {
          var likerRelationship = null;
          for(j = 0; j < usersRelationships.length; j++) {
            if(usersRelationships[j].receiver.username == likerUsername || usersRelationships[j].sender.username == likerUsername) {
              likerRelationship = usersRelationships[j];
              break;
            }
          }
          var sender = null, status = null;
          if(likerRelationship) {
            sender = likerRelationship.sender.username;
            status = likerRelationship.status;
          }
          action = UIActivity.buildRelationshipButton(likerUsername, sender, status);
        }

        if (action){
          divActionContainer.append(action);
        }

        likerItem.append(likerAction.append(divActionContainer));
        likersList.append(likerItem);
      }
    },

    takeActionFromLikeComment: function(el) {
      var env = eXo.social.portal;

      var button = $(el);
      var dataAction = button.attr('data-action');
      var updatedType = dataAction.split(":")[0];
      var ownerUserId = dataAction.split(":")[1];

      button.hide();
      var loading = $('<div/>', {
        "class": "uiLoadingIconMini"
      });
      button.after(loading);
      $.ajax({
        type: "GET",
        url: env.context + "/" + env.rest + "/social/people/getPeopleInfo/" + ownerUserId + ".json?updatedType=" + updatedType
      }).done(function () {
        $.ajax({
          type: 'GET',
          url: env.context + '/' + env.rest + '/v1/social/usersRelationships?others=' + ownerUserId + '&expand=sender,receiver&fields=sender,receiver,status'
        }).done(function (data) {
          if (data && data.usersRelationships && data.usersRelationships.length <= 1) {
            loading.hide();

            var buttonParent = button.parent();
            buttonParent.empty();
            var sender = null, status = null;
            if(data.usersRelationships.length == 1) {
              sender = data.usersRelationships[0].sender.username;
              status = data.usersRelationships[0].status;
            }
            var action = UIActivity.buildRelationshipButton(ownerUserId, sender, status);
            buttonParent.append(action);
          } else {
            loading.hide();
            button.show();
          }
        }).fail(function() {
          loading.hide();
          button.show();
        });;
      }).fail(function() {
        loading.hide();
        button.show();
      });
    }

};
//
  eXo.social.SocialUtil.addOnResizeWidth(function(evt){UIActivity.responsiveMobile()});
  eXo.social.SocialUtil.addOnResizeWidth(function(evt){UIActivity.adaptFileBreadCrumb()});
  return UIActivity;
})($, mentions._);
