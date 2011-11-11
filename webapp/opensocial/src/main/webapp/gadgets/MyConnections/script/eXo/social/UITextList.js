/**
 * UITextList class.
 * 
 * @since 1.2.4
 */

(function() {
	var window_ = this,
			usersConnection = [],
			userConnectionSearch = null,
			offset = 0,
			limit = 0;
	
	Locale = exo.social.Locale;
	Util = exo.social.Util;
  Configuration = exo.social.Configuration;
  SocialUtil = eXo.social.SocialUtil;
  UISearch = exo.social.UISearch;
  UISetting = exo.social.UISetting;
  
  /**
   * UI component.
   */
  var uiComponent = {
  	UILoading: '#UILoading',
  	GadgetUIIconList: '#GadgetUIIconList',
  	GadgetUITextList: '#GadgetUITextList',
  	GadgetConnectionSetting: '#GadgetConnectionSetting',
  	GadgetMemberMore: '#GadgetMemberMore',
  	UITextListListContent: '#UITextListListContent',
  	UITextListBackToListAndPeopleDirectory: '#UITextListBackToListAndPeopleDirectory',
  	BackToUITextListFromSearch: '#BackToUITextListFromSearch',
  	UITextListPeopleDirectory: '#UITextListPeopleDirectory',
  	UITextListLoadMore: '#UITextListLoadMore',
  	UITextListMoreContent: '#UITextListMoreContent',
  	ModeSetting: '#ModeSetting',
    ModeIconList: '#ModeIconList',
    ModeTextList: '#ModeTextList',
    UISearchContent: '#UISearchContent',
    SearchTextBox: '#SearchTextBox'
  };
  
  /**
   * The UI Mode.
   */
  var isSearchMode = false;
  
  /**
   * The constructor.
   */
  function UITextList() {
  	
  }
  
  /**
   * Set the search mode.
   * 
   * @param mode
   */
  UITextList.setSearchMode = function(mode) {
  	isSearchMode = mode;
  };
  
  /**
   * Get the search mode.
   * 
   * @return
   */
  UITextList.getSearchMode = function() {
  	return isSearchMode;
  };
  
  /**
   * Get the user connection.
   * 
   * @return
   */
  UITextList.getUserConnection = function() {
  	return usersConnection;
  };
  
  /**
   * Set the user connection.
   * 
   * @param list
   */
  UITextList.setUserConnection = function(list) {
  	usersConnection = list;
  };
  
  /**
   * Get the user connection search.
   * 
   * @return
   */
  UITextList.getUserConnectionSearch = function() {
  	return userConnectionSearch;
  };
  
  /**
   * Set the user connection search.
   * 
   * @param list
   */
  UITextList.setUserConnectionSearch = function(list) {
  	userConnectionSearch = list;
  };
  
  /**
   * Set the offset.
   * 
   * @param off
   */
  UITextList.setOffset = function(off) {
  	offset = off;
  };
  
  /**
   * Get the offset.
   * 
   * @return
   */
  UITextList.getOffset = function() {
  	return offset;
  };
  
  /**
   * Set the limit.
   * 
   * @param lim
   */
  UITextList.setLimit = function(lim) {
  	limit = lim;
  };
  
  /**
   * Get the limit.
   * 
   * @return
   */
  UITextList.getLimit = function() {
  	return limit;
  };
  
  /**
   * Get user text list block.
   * 
   * @param userConnectionList
   * @return
   */
  function getUserTextListBlock(userConnectionList) {
  	var userBlock = [];
  	$.each(userConnectionList, function(index, value) {
  		userBlock.push('<li><a target="_blank" href="' + value.profileUrl + '" class="Icon"> ' + value.displayName + ' </a><span>' + value.activityTitle + '</span></li>'); 
  	});
  	return userBlock.join('');
  }
  
  /**
   * Display the user connection activities.
   * 
   * @param userConnectionList
   */
  function display(userConnectionList) {
  	if (UITextList.getUserConnectionSearch() !== null) {
  		$(uiComponent.UITextListListContent).empty();
  	}
  	
  	if (userConnectionList === null || userConnectionList.length === 0) {
  		
  		if ($(uiComponent.UITextListListContent).children().size() === 0) {
  			$(uiComponent.UITextListListContent).append(Locale.getMsg("no_user_connection_activities_update"));
  		}
  		
  		if ($(uiComponent.UITextListMoreContent).length > 0) {
  			$(uiComponent.UITextListMoreContent).hide();
  		}
  		if (UITextList.getSearchMode() === true) {
  			var addBlock = '<div id="UITextListBackToListAndPeopleDirectory">' + 
		 											'<a href="javascript:void(0)" class="Link" id="BackToUITextListFromSearch">' + Locale.getMsg('back_to_list') + '</a> | <a target="_blank" href="' + Configuration.portalEnvironment.peopleDirectory + '" class="Link">' + Locale.getMsg('people_directory') + '</a>'  + 
		 										'</div>';
  			if ($(uiComponent.UITextListBackToListAndPeopleDirectory).length === 0) {
  				$(uiComponent.UITextListMoreContent).after(addBlock);
  			}
  			$(uiComponent.UITextListPeopleDirectory).hide();
  		}
  	} else {
  		$(uiComponent.UITextListListContent).append(getUserTextListBlock(userConnectionList));
  		if ($(uiComponent.UITextListMoreContent).length === 0) {
  			$(uiComponent.UITextListListContent).after('<div class="MoreContent" id="UITextListMoreContent"><a href="javascript:void(0)" class="ReadMore" id="UITextListLoadMore">' + Locale.getMsg('load_more') + '</a></div>');
  		}
  		//search mode
  		if (UITextList.getUserConnectionSearch() !== null) {
  			var addBlock = '<div id="UITextListBackToListAndPeopleDirectory">' + 
  										 		'<a href="javascript:void(0)" class="Link" id="BackToUITextListFromSearch">' + Locale.getMsg('back_to_list') + '</a> | <a target="_blank" href="' + Configuration.portalEnvironment.peopleDirectory + '" class="Link">' + Locale.getMsg('people_directory') + '</a>'  + 
  										 	'</div>';
  			
  			if ($(uiComponent.UITextListBackToListAndPeopleDirectory).length === 0) {
  				$(uiComponent.UITextListMoreContent).after(addBlock);
  			}
  			$(uiComponent.UITextListPeopleDirectory).hide();
  		}
  		
  		var isLoadMore = userConnectionList.length % UITextList.getLimit();
    	if (userConnectionList !== null && userConnectionList.length > 0 && isLoadMore === 0) {
    		$(uiComponent.UITextListMoreContent).show();
    		if ($(uiComponent.UITextListBackToListAndPeopleDirectory).length === 0) {
    			UITextList.setOffset(UITextList.getOffset() + UITextList.getLimit());
    		} else {
    			UISearch.setOffset(UISearch.getOffset() + UISearch.getLimit());
    		}
    	} else {
    		if ($(uiComponent.UITextListMoreContent).length > 0) {
    			$(uiComponent.UITextListMoreContent).hide();
    		}
    	}
  	}
  	
  	if ($(uiComponent.UITextListPeopleDirectory).length === 0) {
  		$(uiComponent.GadgetUITextList).append('<a target="_blank" href="' + Configuration.portalEnvironment.peopleDirectory + '" class="Link" id="UITextListPeopleDirectory" target="_blank">' + Locale.getMsg('people_directory') + '</a>');
  	}
  	
  	gadgets.window.adjustHeight();
  }
  
  $(uiComponent.BackToUITextListFromSearch).live("click", function() {
		UITextList.setUserConnectionSearch(null);
		UISearch.setNameToSearch(null);
		$(uiComponent.UITextListListContent).empty();
		$(uiComponent.UITextListBackToListAndPeopleDirectory).remove();
		$(uiComponent.UITextListPeopleDirectory).show();
		
		var isMore = UITextList.getUserConnection().length % UITextList.getLimit();
		if (isMore === 0) {
			UITextList.setOffset(UITextList.getOffset() - UITextList.getLimit());
		}
		
		UITextList.display(UITextList.getUserConnection());
		$(uiComponent.SearchTextBox).val(Locale.getMsg('quick_search'));
	});
  
	$(uiComponent.UITextListLoadMore).live("click", function() {
		if ($(uiComponent.UITextListBackToListAndPeopleDirectory).length === 0) {
			UITextList.loadMore();
		} else {
			UISearch.loadMore();
		}
	});
  
  /**
   * Displays the user connection activities.
   */
  UITextList.display = function() {
  	UISearch.initSearchInput({offset: 0, limit: 10, viewType: "TEXT_LIST"});
  	
  	$(uiComponent.UISearchContent).css('display', 'block');
  	
  	$(uiComponent.GadgetUITextList).css('display', 'block');
  	
  	if ($('.ListIcon').length > 0) {
			$(uiComponent.ModeTextList).removeClass('ListIcon');
			$(uiComponent.ModeTextList).addClass('ListSelected');
		}
		
		if ($('.ListSelected').length > 0) {
			$(uiComponent.ModeIconList).removeClass('NumberListSelected');
			$(uiComponent.ModeIconList).addClass('NumberListIcon');
		}
		
		if ($('.SettingSelected').length > 0) {
			$(uiComponent.ModeSetting).removeClass('SettingSelected');
			$(uiComponent.ModeSetting).addClass('SettingIcon');
		}
  	
  	$(uiComponent.UILoading).hide();
  	$(uiComponent.GadgetMemberMore).hide();
  	$(uiComponent.GadgetUIIconList).hide();
  	
  	if (UITextList.getUserConnectionSearch() === null) {
  		display(UITextList.getUserConnection());
  	} else {
  		display(UITextList.getUserConnectionSearch());
  	}
  };

  /**
   * Load more user connection activities.
   */
  UITextList.loadMore = function() {
  	var lang = Locale.getLang();
  	var peopleRestUrl = Configuration.portalEnvironment.peopleRestUrl +
  											"?offset=" + UITextList.getOffset() + 
												"&limit=" + UITextList.getLimit() + 
												"&lang=" + lang;
  	
  	//Get user connection activities when click load more.
  	Util.makeRequest(peopleRestUrl, function(response) {
			var userConnectionsActivities = Util.parseUserConnectionActivities(response);
			
			if (userConnectionsActivities !== null && userConnectionsActivities.length > 0) {
				$.merge(usersConnection, userConnectionsActivities);
			}
			
			display(userConnectionsActivities);
		});
  };
  
  window_.exo = window_.exo || {};
  window_.exo.social = window_.exo.social || {};
  window_.exo.social.UITextList = UITextList;
})();