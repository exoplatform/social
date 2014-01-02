/**
 * UIIconList class.
 *
 * How to use:
 * var UIIconList = eXo.social.UIIconList;
 * UIIconList.setParams(params); //as dom ids
 * UIIconList.setUsers(users); //set lists of users to be displayed
 * UIIconList.display();
 * UIIconList.loadMore();
 * 
 * @since 1.2.4
 */

(function() {
	var window_ = this,
			offset = 0,
			limit = 0,
			userConnectionList = [],
			userConnectionSearch = null;
	
	/**
   * ui component.
   */
  var uiComponent = {
  	GadgetUITextList: '#GadgetUITextList',
  	UIIconListListContent: '#UIIconListListContent',
  	GadgetConnectionSetting: '#GadgetConnectionSetting',
  	UIIconListMoreContent: '#UIIconListMoreContent',
  	More: '.More',
  	GadgetMemberMore: '#GadgetMemberMore',
  	GadgetUIIconList: '#GadgetUIIconList',
  	MemberProptileDetail: '#MemberProptileDetail',
  	BackToUIIconList: '#BackToUIIconList',
  	ListContentMoreDetail: '#ListContentMoreDetail',
  	LoadMoreComments: '#LoadMoreComments',
  	LikeCurrentLatestActivity: '#LikeCurrentLatestActivity',
  	CommentCurrentLatestActivity: '#CommentCurrentLatestActivity',
  	MoreActivityDetail: '#MoreActivityDetail',
  	UILoading: '#UILoading',
  	BackToUIIconListFromSearch: '#BackToUIIconListFromSearch',
  	UIIconListPeopleDirectory: '#UIIconListPeopleDirectory',
  	BackToListAndPeopleDirectory: '#BackToListAndPeopleDirectory',
  	UIIconListLoadMoreContent: '#UIIconListLoadMoreContent',
  	ModeSetting: '#ModeSetting',
    ModeIconList: '#ModeIconList',
    ModeTextList: '#ModeTextList',
    UISearchContent: '#UISearchContent',
    NumberOfLike: '#NumberOfLike',
    SearchTextBox: '#SearchTextBox',
    QuickCommentDiv: '#QuickCommentDiv',
    QuickCommentInput: '#QuickCommentInput',
    ShareComment: '#ShareComment'
  };
	
  Locale = exo.social.Locale;
	Util = exo.social.Util;
  Configuration = exo.social.Configuration;
  SocialUtil = eXo.social.SocialUtil;
	Comment = exo.social.Comment;
	Like = exo.social.Like;
	UISearch = exo.social.UISearch;
	UISetting = exo.social.UISetting;
  
	/**
   * UI mode.
   */
  var isSearchMode = false;
	
  /**
   * Class definition
   */
  function UIIconList() {
  	
  }
  
  /**
   * Set the search mode.
   * 
   * @param mode
   */
  UIIconList.setSearchMode = function(mode) {
  	isSearchMode = mode;
  };
  
  /**
   * Get the search mode.
   * 
   * @return
   */
  UIIconList.getSearchMode = function() {
  	return isSearchMode;
  };
  
  /**
   * Get the user connection search
   * 
   * @return
   */
  UIIconList.getUserConnectionSearch = function() {
  	return userConnectionSearch;
  };
  
  /**
   * Set the user connection search.
   * 
   * @param list
   */
  UIIconList.setUserConnectionSearch = function(list) {
  	userConnectionSearch = list;
  };
  
  /**
   * Set user connection list.
   * 
   * @param list
   */
  UIIconList.setUserConnectionList = function(list) {
  	userConnectionList = list;
  };
  
  /**
   * Get user connection list.
   * 
   * @return
   */
  UIIconList.getUserConnectionList = function() {
  	return userConnectionList;
  };
  
  /**
   * Get the offset.
   * 
   * @return
   */
  UIIconList.getOffset = function() {
  	return offset;
  };
  
  /**
   * Set the offset.
   * 
   * @param off
   */
  UIIconList.setOffset = function(off) {
  	offset = off;
  };
  
  /**
   * Get the limit.
   * 
   * @return
   */
  UIIconList.getLimit = function() {
  	return limit;
  };
  
  /**
   * Set the limit.
   * 
   * @param lim
   */
  UIIconList.setLimit = function(lim) {
  	limit = lim;
  };
  
  /**
   * Set current activity of comment and like.
   * 
   * @param userConnectionList
   * @param id
   */
  function setCurrentActivity(userConnectionList, id) {
  	$.each(userConnectionList, function(index, user) {
  		if (user.activityId === id) {
  			Comment.refer.connectionActivity = user;
  			Like.ref.currentActivity = user;
  			return;
  		}
  	});
  }
  
  /**
   * Display the activities connection and comments
   * 
   * @param userConnectionActivities
   */
  function display(userConnectionActivities) {
  	$(uiComponent.UILoading).hide();
  	
  	if (UIIconList.getSearchMode() === true) {
  		$(uiComponent.UIIconListListContent).empty();
  	}
  	
  	if (userConnectionActivities === null || userConnectionActivities.length === 0) {
  		if ($(uiComponent.UIIconListListContent).children().size() === 0) {
  			$(uiComponent.UIIconListListContent).append(Locale.getMsg("no_user_connection_activities_update"));
  		}
  		
  		if ($(uiComponent.UIIconListLoadMoreContent).length > 0) {
  			$(uiComponent.UIIconListLoadMoreContent).hide();
  		}
  		
  		if (UIIconList.getSearchMode() === true) {
  			var addBlock = '<div id="BackToListAndPeopleDirectory">' + 
													'<a href="javascript:void(0)" class="Link" id="BackToUIIconListFromSearch">' + Locale.getMsg('back_to_list') + '</a> | <a target="_blank" href="' + Configuration.portalEnvironment.peopleDirectory + '" class="Link">' + Locale.getMsg('people_directory') + '</a>' +
												'</div>';
  			if ($(uiComponent.BackToListAndPeopleDirectory).length === 0) {
  				$(uiComponent.UIIconListListContent).append(addBlock);
  			}
  			$(uiComponent.UIIconListPeopleDirectory).hide();
  		}
  	} else {
  		var userBlock = Util.getUserConnectionActivitiesBlock(userConnectionActivities);
  		$(uiComponent.UIIconListListContent).append(userBlock);
  		
  		if ($(uiComponent.UIIconListLoadMoreContent).length === 0) {
  			$(uiComponent.UIIconListListContent).after('<div class="MoreContent" id="UIIconListLoadMoreContent"><a href="javascript:void(0)" class="ReadMore" id="UIIconListMoreContent">' + Locale.getMsg('load_more') + '</a></div>');
  		}
  		if (UIIconList.getSearchMode() === true) {
  			var addBlock = '<div id="BackToListAndPeopleDirectory">' + 
  												'<a href="javascript:void(0)" class="Link" id="BackToUIIconListFromSearch">' + Locale.getMsg('back_to_list') + '</a> | <a target="_blank" href="' + Configuration.portalEnvironment.peopleDirectory + '" class="Link">' + Locale.getMsg('people_directory') + '</a>' +
  											'</div>';
  			
  			if ($(uiComponent.BackToListAndPeopleDirectory).length === 0) {
  				$(uiComponent.UIIconListLoadMoreContent).after(addBlock);
  			}
  			$(uiComponent.UIIconListPeopleDirectory).hide();
  		}
  		
  		var isLoadMore = userConnectionActivities.length % UIIconList.getLimit();
    	if (userConnectionActivities !== null && userConnectionActivities.length > 0 && isLoadMore === 0) {
    		$(uiComponent.UIIconListLoadMoreContent).show();
    		if ($(uiComponent.BackToListAndPeopleDirectory).length === 0) {
    			UIIconList.setOffset(UIIconList.getOffset() + UIIconList.getLimit());
      	} else {
      		UISearch.setOffset(UISearch.getOffset() + UISearch.getLimit());
      	}
    	} else {
    		if ($(uiComponent.UIIconListLoadMoreContent).length > 0) {
    			$(uiComponent.UIIconListLoadMoreContent).hide();
    		}
    	}
  	}
  	
  	if ($(uiComponent.UIIconListPeopleDirectory).length === 0) {
  		$(uiComponent.GadgetUIIconList).append('<a target="_blank" href="' + Configuration.portalEnvironment.peopleDirectory + '" class="Link" id="UIIconListPeopleDirectory" target="_blank">' + Locale.getMsg('people_directory') + '</a>');
  	}
  	
  	$(uiComponent.GadgetMemberMore).hide();
  	$(uiComponent.GadgetUIIconList).show();
  	gadgets.window.adjustHeight();
  }
  
  /**
   * When click to view more detail.
   */
  $(uiComponent.More).live("click", function() {
		var id = $(this).attr('id');
		
		if (UIIconList.getSearchMode() === true) {
			setCurrentActivity(userConnectionSearch, id);
		} else {
			setCurrentActivity(userConnectionList, id);
		}
		UIIconList.moreDetail();
	});
  
  /**
   * Click Back To List from search.
   */
  $(uiComponent.BackToUIIconListFromSearch).live("click", function() {
		UIIconList.setSearchMode(false);
		UIIconList.setUserConnectionSearch(null);
		
		UISearch.setNameToSearch(null);
		
		$(uiComponent.UIIconListListContent).empty();
		$(uiComponent.BackToListAndPeopleDirectory).remove();
		$(uiComponent.UIIconListPeopleDirectory).show();
		$(uiComponent.UISearchContent).css('display', 'block');
		
		var isMore = UIIconList.getUserConnectionList().length % UIIconList.getLimit();
		if (isMore === 0) {
			UIIconList.setOffset(UIIconList.getOffset() - UIIconList.getLimit());
		}
		
		display(UIIconList.getUserConnectionList());
		
		$(uiComponent.SearchTextBox).val(Locale.getMsg('quick_search'));
	});
  
  $(uiComponent.UIIconListMoreContent).live("click", function() {
  	if ($(uiComponent.BackToListAndPeopleDirectory).length === 0) {
  		UIIconList.loadMore();
  	} else {
  		UISearch.loadMore();
  	}
  });
  
  /**
   * Display user connection activities.
   */
  UIIconList.display = function() {
  	//init search component
  	UISearch.initSearchInput({offset: 0, limit: 10, viewType: "ICON_LIST"});
  	
  	$(uiComponent.UISearchContent).css('display', 'block');
  	
  	$(uiComponent.GadgetUIIconList).css('display', 'block');
  	
  	if ($('.NumberListIcon').length > 0) {
			$(uiComponent.ModeIconList).removeClass('NumberListIcon');
			$(uiComponent.ModeIconList).addClass('NumberListSelected');
		}
		
		if ($('.ListSelected').length > 0) {
			$(uiComponent.ModeTextList).removeClass('ListSelected');
			$(uiComponent.ModeTextList).addClass('ListIcon');
		}
		
		if ($('.SettingSelected').length > 0) {
			$(uiComponent.ModeSetting).removeClass('SettingSelected');
			$(uiComponent.ModeSetting).addClass('SettingIcon');
		}
  	
  	$(uiComponent.GadgetUITextList).hide();
  	
  	if (UIIconList.getSearchMode() === true) {
  		display(UIIconList.getUserConnectionSearch());
  	} else {
  		display(UIIconList.getUserConnectionList());
  	}
  };

  /**
   * Load more user connection activities.
   */
  UIIconList.loadMore = function() {
  	$(uiComponent.UISearchContent).css('display', 'block');
  	
  	var lang = Locale.getLang();
  	
  	var peopleRestUrl = Configuration.portalEnvironment.peopleRestUrl + 
  											"?offset=" + UIIconList.getOffset() + 
												"&limit=" + UIIconList.getLimit() + 
												"&lang=" + lang;
  	
		//Get user connection activities when click load more.
		Util.makeRequest(peopleRestUrl, function(response) {
			var userConnectionsActivities = Util.parseUserConnectionActivities(response);
			if (userConnectionsActivities !== null && userConnectionsActivities.length > 0) {
				UIIconList.setUserConnectionList($.merge(userConnectionList, userConnectionsActivities));
			}
			display(userConnectionsActivities);
		});
  };
  
  /**
   * Display the comments of current activity.
   * 
   * @param response
   */
  function displayComments(response) {
		if (response.data !== null && response.data.comments !== null) {
			var commentsBlock = [];
			$.each(response.data.comments, function(index, comment) {
		   	commentsBlock.push('<li class="clearfix">');
		   		commentsBlock.push('<a href="javascript:void(0)" class="User">' + comment.posterIdentity.profile.fullName + ': </a>');
		   		commentsBlock.push('<span>' + comment.text + '</span>');
		   	commentsBlock.push('</li>');
			});
			
			$(uiComponent.ListContentMoreDetail).append(commentsBlock.join(""));
			
			$(uiComponent.LikeCurrentLatestActivity).click(function() {
				var currentIdentityId = Configuration.getViewer().getField(opensocial.Person.Field.ID);
				var currentActivityId = Like.ref.currentActivity.activityId;
				
				Like.setLikeId(currentActivityId, currentIdentityId, function(response) {
					if (response.data !== null && response.data.likes !== null) {
						if ($(uiComponent.NumberOfLike).length > 0) {
							$(uiComponent.NumberOfLike).empty();
						}
						$(uiComponent.NumberOfLike).append(" ( " + response.data.likes.length + " )");
					} else {
						debug.warn('Like.displayLike: response data is null!!!');
				    return;
					}
				});
			});
			
			if (response.data.comments.length === Comment.getLimit()) {
				$(uiComponent.LoadMoreComments).show();
				$(uiComponent.LoadMoreComments).click(UIIconList.moreComments); 
				Comment.setOffset(Comment.getOffset() + Comment.getLimit());
			} else {
				$(uiComponent.LoadMoreComments).hide();
			}
			
			$(uiComponent.ListContentMoreDetail).show();
		} else {
			if ($(uiComponent.ListContentMoreDetail).children().size() === 0) {
				$(uiComponent.ListContentMoreDetail).hide();
			}
			$(uiComponent.LoadMoreComments).hide();
			
			debug.warn("No comments of this activity.");
			return;
		}
		
		gadgets.window.adjustHeight();
  }
  
  /**
   * Get my connection detail block.
   * 
   * @return my connection detail block.
   */
  function getMyConnectionDetailBlock() {
  	var userBlock = [];
		
		userBlock.push('<div class="MemberProptile clearfix" id="MemberProptileDetail">');
			userBlock.push('<a target="_blank" href="' + Comment.refer.connectionActivity.profileUrl + '" class="Avatar"><img title="' + Comment.refer.connectionActivity.displayName + '" alt="' + Comment.refer.connectionActivity.displayName + '" width="44px" height="44px" src="' + Comment.refer.connectionActivity.avatarUrl + '"/></a>');
			userBlock.push('<div class="Content">');
				userBlock.push('<a target="_blank" href="' + Comment.refer.connectionActivity.profileUrl + '" class="User">' + Comment.refer.connectionActivity.displayName + '</a>');
				userBlock.push('<div class="Member"> Member</div>');
				userBlock.push('<a target="_blank" href="' + Comment.refer.connectionActivity.profileUrl + '" class="Work" id="MoreActivityDetail">' + Locale.getMsg('profile_page') + '</a>');
			userBlock.push('</div>');
		userBlock.push('</div>');
		
		userBlock.push('<div class="CurentActivity clearfix">');
			userBlock.push('<div class="Action">');
				userBlock.push('<a id="CommentCurrentLatestActivity" href="javascript:void(0)" class="CommIcon">&nbsp;</a><a id="LikeCurrentLatestActivity" href="javascript:void(0)" class="LikeIcon">&nbsp;</a>');
			userBlock.push('</div>');
		
			userBlock.push('<div class="Commnent">');
				userBlock.push('<span class="Comm">' + Comment.refer.connectionActivity.activityTitle + '</span>');
				userBlock.push('<span class="NumberLike" id="NumberOfLike"></span>');
				userBlock.push('<div class="TimeComm">' + Comment.refer.connectionActivity.prettyPostedTime + '</div>');
			userBlock.push('</div>')
		userBlock.push('</div>');
	  
	  userBlock.push('<div class="QuickCommentBox" style="display: none;" id="QuickCommentDiv">');
	  	userBlock.push('<input id="QuickCommentInput" type="text" title="' + Locale.getMsg('comment_here') + '" value="' + Locale.getMsg('comment_here') + '"/>');
	  	userBlock.push('<div class="ShareBT"><a id="ShareComment" href="javascript:void(0)">' + Locale.getMsg('share') + '</a></div>');
	  userBlock.push('</div>');
		
		userBlock.push('<ul id="ListContentMoreDetail">');
		userBlock.push('</ul>');
		
		userBlock.push('<div class="MoreContent" id="LoadMoreComments">');
			userBlock.push('<a href="javascript:void(0)" class="ReadMore">' + Locale.getMsg('load_more') + '</a>');
		userBlock.push('</div>');
		
		userBlock.push('<a href="javascript:void(0)" class="Link" id="BackToUIIconList">' + Locale.getMsg('back_to_list') + '</a> | <a target="_blank" href="' + Configuration.portalEnvironment.peopleDirectory + '" class="Link">' + Locale.getMsg('people_directory') + '</a>');
		
		return userBlock.join('');
  }
  
  /**
   * Click button comment.
   */
  $(uiComponent.CommentCurrentLatestActivity).live("click", function() {
  	$(uiComponent.QuickCommentDiv).css('display', 'block');
  	
  	//hide load more
  	$(uiComponent.LoadMoreComments).hide();
  	
  	gadgets.window.adjustHeight();
  });
  
  /**
   * Click share comment.
   */
  $(uiComponent.ShareComment).live("click", function() {
  	if ($(uiComponent.QuickCommentInput).val() === '' || 
  			$(uiComponent.QuickCommentInput).val() === Locale.getMsg('comment_here')) {
  		return;
  	}
  	
  	var commentContent = $(uiComponent.QuickCommentInput).val().replace(/&nbsp;/g,' ')
  																														 .replace(/<br>/gi, " ")
  																														 .replace(/<p>/gi, " ")
  																														 .replace(/<\/\p>/gi, " ");
  	
  	Comment.create(Comment.refer.connectionActivity.activityId, commentContent, function(response) {
  		if (response.rc === 200) {
  			$(uiComponent.QuickCommentInput).val(Locale.getMsg('comment_here'));
  			
  			var commentBack = [];
  			commentBack.push('<li class="clearfix">');
  				commentBack.push('<a href="javascript:void(0)" class="User">' + response.data.posterIdentity.profile.fullName + ': </a>');
  				commentBack.push('<span>' + response.data.text + '</span>');
  			commentBack.push('</li>');
  			$(uiComponent.ListContentMoreDetail).append(commentBack.join(''));
  			
  			gadgets.window.adjustHeight();
  		} else if (response.rc === 404) {
  			
  		}
  	});
  	
  	gadgets.window.adjustHeight();
  });
  
  /**
   * Init my connection detail.
   * 
   * @param id
   */
  function initMyConnectionDetail(id) {
		$(uiComponent.GadgetUIIconList).hide();
		$(uiComponent.GadgetMemberMore).show();
		
		if ($(uiComponent.MemberProptileDetail) !== null) {
			$(uiComponent.GadgetMemberMore).empty();
		}
		
		var userBlock = getMyConnectionDetailBlock();
	  
	  $(uiComponent.GadgetMemberMore).append(userBlock);
	
	  /**
	   * Blur event.
	   */
	  $(uiComponent.QuickCommentInput).blur(function() {
	  	$(uiComponent.QuickCommentInput).css('color', 'black');
	  });
	  
	  /**
	   * Focus event.
	   */
	  $(uiComponent.QuickCommentInput).focus(function() {
	  	$(uiComponent.QuickCommentInput).css('color', 'black');
	  });
	  
	  Comment.setOffset(0);
	  Comment.setLimit(10);
	  
	  Util.getActivity({activityId: id, limit: 10}, function(response) {
	  	if (response.data !== null && response.data.totalNumberOfLikes !== null) {
	  		if ($(uiComponent.NumberOfLike).length > 0) {
	  			$(uiComponent.NumberOfLike).empty();
	  		}
	  		$(uiComponent.NumberOfLike).append(' ( ' + response.data.totalNumberOfLikes + ' )');
	  	}
	  	displayComments(response);
	  });
  	
  	gadgets.window.adjustHeight();
  }
  
  /**
   * When click back to list.
   */
  $(uiComponent.BackToUIIconList).live("click", function() {
  	$(uiComponent.GadgetUIIconList).show();
		$(uiComponent.GadgetMemberMore).hide();
		$(uiComponent.UISearchContent).css('display', 'block');
		gadgets.window.adjustHeight();
  });
  
  /**
   * Click more to view detail.
   */
  UIIconList.moreDetail = function() {
  	$(uiComponent.UISearchContent).css('display', 'none');
  	initMyConnectionDetail(Comment.refer.connectionActivity.activityId);
  };

  /**
   * Load more comments in view detail.
   */
  UIIconList.moreComments = function() {
  	Comment.loadMore(function(response) {
  		displayComments(response);
  	});
  };
  
  /**
   * When load image error, change to avatar default.
   */
  $(window).bind('load', function() {
  	$('img').each(function() {
  	    if((typeof this.naturalWidth != "undefined" &&
  	        this.naturalWidth == 0 ) 
  	        || this.readyState == 'uninitialized' ) {
  	        $(this).attr('src', '/eXoSkin/skin/less/social/skin/DefaultSkin/portal/background/UserlistAvatar.png');
  	    }
  	}); 
  })
  
  window_.exo = window_.exo || {};
  window_.exo.social = window_.exo.social || {};
  window_.exo.social.UIIconList = UIIconList;
})();