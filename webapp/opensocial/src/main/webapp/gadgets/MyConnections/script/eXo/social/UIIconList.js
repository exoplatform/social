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
  	MoreActivityDetail: '#MoreActivityDetail',
  	UILoading: '#UILoading',
  	BackToUIIconListFromSearch: '#BackToUIIconListFromSearch',
  	UIIconListPeopleDirectory: '#UIIconListPeopleDirectory',
  	BackToListAndPeopleDirectory: '#BackToListAndPeopleDirectory',
  	UIIconListLoadMoreContent: '#UIIconListLoadMoreContent',
  	ModeSetting: '#ModeSetting',
    ModeIconList: '#ModeIconList',
    ModeTextList: '#ModeTextList'
  };
	
  //Locale = eXo.social.Locale;
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
  	
  	debug.info("userConnectionActivities:");
  	debug.debug(userConnectionActivities);
  	
  	debug.info(UIIconList.getSearchMode());
  	
  	if (UIIconList.getSearchMode() === true) {
  		$(uiComponent.UIIconListListContent).empty();
  	}
  	
  	if (userConnectionActivities === null || userConnectionActivities.length === 0) {
  		debug.info("size:");
  		debug.debug($(uiComponent.UIIconListListContent).children().size());
  		if ($(uiComponent.UIIconListListContent).children().size() === 0) {
  			$(uiComponent.UIIconListListContent).append('No user connection activities update');
  		}
  		
  		if ($(uiComponent.UIIconListLoadMoreContent).length > 0) {
  			$(uiComponent.UIIconListLoadMoreContent).hide();
  		}
  		
  		if (UIIconList.getSearchMode() === true) {
  			var addBlock = '<div id="BackToListAndPeopleDirectory">' + 
													'<a href="#" class="Link" id="BackToUIIconListFromSearch">Back to List</a> | <a href="#" class="Link">People Directory</a>' +
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
  			$(uiComponent.UIIconListListContent).after('<div class="MoreContent" id="UIIconListLoadMoreContent"><a href="#" class="ReadMore" id="UIIconListMoreContent"> Load more ... </a></div>');
  		}
  		if (UIIconList.getSearchMode() === true) {
  			var addBlock = '<div id="BackToListAndPeopleDirectory">' + 
  												'<a href="#" class="Link" id="BackToUIIconListFromSearch">Back to List</a> | <a href="#" class="Link">People Directory</a>' +
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
  	
  	$(uiComponent.More).click(function() {
  		var id = $(this).attr('id');
  		debug.info("id activity:");
  		debug.debug(id);
  		
  		if (UIIconList.getSearchMode() === true && userConnectionActivities !== null && userConnectionActivities.length > 0) {
  			setCurrentActivity(userConnectionActivities, id);
  		} else {
  			setCurrentActivity(userConnectionList, id);
  		}
  		UIIconList.moreDetail();
  	});
  	$(uiComponent.GadgetMemberMore).hide();
  	$(uiComponent.GadgetUIIconList).show();
  	gadgets.window.adjustHeight();
  }
  
  $(uiComponent.BackToUIIconListFromSearch).live("click", function() {
		UIIconList.setSearchMode(false);
		UIIconList.setUserConnectionSearch(null);
		
		UISearch.setNameToSearch(null);
		
		$(uiComponent.UIIconListListContent).empty();
		$(uiComponent.BackToListAndPeopleDirectory).remove();
		$(uiComponent.UIIconListPeopleDirectory).show();
		
		var isMore = UIIconList.getUserConnectionList().length % UIIconList.getLimit();
		if (isMore === 0) {
			UIIconList.setOffset(UIIconList.getOffset() - UIIconList.getLimit());
		}
		
		display(UIIconList.getUserConnectionList());
		
		$("#SearchTextBox").val('Quick Search');
	});
  
  $(uiComponent.UIIconListMoreContent).live("click", function() {
  	
  	debug.info('$(uiComponent.BackToListAndPeopleDirectory).length:');
  	debug.debug($(uiComponent.BackToListAndPeopleDirectory).length);
  	
  	if ($(uiComponent.BackToListAndPeopleDirectory).length === 0) {
  		UIIconList.loadMore();
  		
  		debug.info("load more offset:");
  		debug.debug(UIIconList.getOffset());
  		
  		debug.info("load more limit:");
  		debug.debug(UIIconList.getLimit());
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
  	var peopleRestUrl = Configuration.portalEnvironment.peopleRestUrl + 
  											"?offset=" + UIIconList.getOffset() + 
												"&limit=" + UIIconList.getLimit();

  	debug.info('peopleRestUrl in loadMore: ');
  	debug.debug(peopleRestUrl);
  	
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
  	debug.info("displayComments:");
		debug.debug(response);
		
		if (response.data !== null && response.data.comments !== null) {
			var commentsBlock = [];
			$.each(response.data.comments, function(index, comment) {
				commentsBlock.push('<li class="ClearFix">');
					commentsBlock.push('<div class="Commnent">');
						commentsBlock.push('<div class="Comm">' + comment.posterIdentity.profile.fullName + ": " + comment.text + '</div>');
						commentsBlock.push('<div class="TimeComm">20 minutes ago</div>');
					commentsBlock.push('</div>');
					commentsBlock.push('<div class="Action" id="LikeCurrentLatestActivity"><a href="#" class="CommIcon">&nbsp;</a><a href="#" class="LikeIcon">&nbsp;</a></div>')
				commentsBlock.push('</li>');
			});
			
			debug.info("comment block:");
			debug.debug(commentsBlock.join(""));
			
			$(uiComponent.ListContentMoreDetail).append(commentsBlock.join(""));
			
			$(uiComponent.LikeCurrentLatestActivity).click(function() {
				var currentIdentityId = Configuration.getViewer().getField(opensocial.Person.Field.ID);
				var currentActivityId = Like.ref.currentActivity.activityId;
				
				Like.setLikeId(currentActivityId, currentIdentityId, function(response) {
					if (response.data !== null && response.data.likes !== null) {
						$(uiComponent.LikeCurrentLatestActivity).append(" ( " + response.data.likes.length + " )");
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
		
  	///social-resources/skin/DefaultSkin/portal/background/UserlistAvatar.png
  	
		userBlock.push('<div class="MemberProptile ClearFix" id="MemberProptileDetail">');
			userBlock.push('<a href="' + Comment.refer.connectionActivity.profileUrl + '" class="Avatar"><img alt="" width="44px" height="44px" src="' + Comment.refer.connectionActivity.avatarUrl + '"/></a>');
			userBlock.push('<div class="Content">');
				userBlock.push('<a href="' + Comment.refer.connectionActivity.profileUrl + '" class="User">' + Comment.refer.connectionActivity.displayName + '</a>');
				userBlock.push('<div class="Member"> Member</div>');
				userBlock.push('<a href="' + Comment.refer.connectionActivity.profileUrl + '" class="Work" id="MoreActivityDetail">' + Comment.refer.connectionActivity.activityTitle + '</a>');
			userBlock.push('</div>');
		userBlock.push('</div>');
		userBlock.push('<ul class="ListContent" id="ListContentMoreDetail">');
		userBlock.push('</ul>');
		userBlock.push('<div class="MoreContent" id="LoadMoreComments">');
			userBlock.push('<a href="#" class="ReadMore"> Load more ... </a>');
		userBlock.push('</div>');
		userBlock.push('<a href="#" class="Link" id="BackToUIIconList">Back to List</a> | <a href="#" class="Link">People Directory</a>');
		
		return userBlock.join('');
  }
  
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
	
	  $(uiComponent.BackToUIIconList).click(function() {
	  	$(uiComponent.GadgetUIIconList).show();
  		$(uiComponent.GadgetMemberMore).hide();
  		gadgets.window.adjustHeight();
	  });
	  
	  Comment.setOffset(0);
	  Comment.setLimit(10);
	  
	  Util.getActivity({activityId: id, limit: 10}, function(response) {
	  	
	  	debug.info("get activity:");
	  	debug.debug(response);
	  	
	  	if (response.data !== null && response.data.totalNumberOfLikes !== null) {
	  		$(uiComponent.MoreActivityDetail).append(' (' + response.data.totalNumberOfLikes + ' )');
	  	}
	  	displayComments(response);
	  });
  	
  	gadgets.window.adjustHeight();
  }
  
  /**
   * Click more to view detail.
   */
  UIIconList.moreDetail = function() {
  	debug.info("current activity:");
  	debug.debug(Comment.refer.connectionActivity);
  	
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
  	        $(this).attr('src', '/social-resources/skin/DefaultSkin/portal/background/UserlistAvatar.png');
  	    }
  	}); 
  })
  
  window_.exo = window_.exo || {};
  window_.exo.social = window_.exo.social || {};
  window_.exo.social.UIIconList = UIIconList;
})();