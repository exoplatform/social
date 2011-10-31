/**
 * UISearch class
 * 
 * @since 1.2.4
 */
(function() {
	var window_ = this,
			usersConnection = [],
			offset = 0,
			limit = 0,
			nameToSearch = null,
			viewMode;
	
	var OFFSET_DEFAULT = 0,
      LIMIT_DEFAULT = 10,
      DEFAULT_INPUT_VALUE="Quick Search", // changed resource bundle
      FOCUS_COLOR="black",
      BLUR_COLOR="#D5D5D5";
	
	var uiComponent = {
		SearchTextBox: '#SearchTextBox',
		SearchButton: '#SearchButton'
	};
	
  Util = exo.social.Util;
  Configuration = exo.social.Configuration;
  SocialUtil = eXo.social.SocialUtil;
  UITextList = exo.social.UITextList;
  UIIconList = exo.social.UIIconList;
  
  /**
   * Class definition
   */
  function UISearch() {
  	
  }

  /**
	 * View types.
	 */
	UISearch.VIEW_TYPE = {
	  TEXT_LIST: "TEXT_LIST",
	  ICON_LIST: "ICON_LIST"
	};
  
	/**
	 * Get the view mode.
	 * 
	 * @return
	 */
	UISearch.getViewType = function() {
		return viewMode;
	};
	
	/**
	 * Set the view mode.
	 * 
	 * @param mode
	 */
	UISearch.setViewType = function(mode) {
		viewMode = mode;
	};
	
  /**
   * Get the offset.
   * 
   * @return
   */
  UISearch.getOffset = function() {
  	return offset;
  };
  
  /**
   * Set the offset
   * 
   * @param off
   */
  UISearch.setOffset = function(off) {
  	offset = off;
  };
  
  /**
   * Get the limit.
   * 
   * @return
   */
  UISearch.getLimit = function() {
  	return limit;
  };
  
  /**
   * Set the limit.
   * 
   * @param lim
   */
  UISearch.setLimit = function(lim) {
  	limit = lim;
  };
  
  /**
   * Set the name to search.
   * 
   * @param name
   */
  UISearch.setNameToSearch = function(name) {
  	nameToSearch = name;
  };
  
  /**
   * Get the name to search.
   * 
   * @return
   */
  UISearch.getNameToSearch = function() {
  	return nameToSearch;
  };
  
  /**
   * Display search result.
   * 
   * @param userConnectionsActivities
   */
  function display(userConnectionsActivities) {
  	if (UISearch.getViewType() === UISearch.VIEW_TYPE.TEXT_LIST) {
  		UITextList.setSearchMode(true);
  		var currentConnectionSearch = UITextList.getUserConnectionSearch();
  		if (currentConnectionSearch !== null) {
  			UITextList.setUserConnectionSearch($.merge(currentConnectionSearch, userConnectionsActivities));
  		} else {
  			UITextList.setUserConnectionSearch(userConnectionsActivities);
  		}
			UITextList.display();
		} else if (UISearch.getViewType() === UISearch.VIEW_TYPE.ICON_LIST) {
			UIIconList.setSearchMode(true);
			var currentConnectionSearch = UIIconList.getUserConnectionSearch();
			if (currentConnectionSearch !== null) {
				UIIconList.setUserConnectionSearch($.merge(currentConnectionSearch, userConnectionsActivities));
			} else {
				UIIconList.setUserConnectionSearch(userConnectionsActivities);
			}
			UIIconList.display();
		}
  }
  
  /**
   * Search user connection activities.
   */
  function searchUserConnectionActivities() {
  	var searchInputEl = document.getElementById("SearchTextBox");
    var searchButton = document.getElementById("SearchButton");
  	
    var nameToSearch = ((searchInputEl.value != "") && (searchInputEl.value != DEFAULT_INPUT_VALUE)) ? searchInputEl.value : "";
    
    debug.info("name to search:");
    debug.debug(nameToSearch);
    
    debug.info('UISearch.getNameToSearch:');
    debug.debug(UISearch.getNameToSearch());
    
    if (nameToSearch === '') {
    	return;
    }
    
    if (nameToSearch === UISearch.getNameToSearch()) {
    	return;
    }
    
    UISearch.setNameToSearch(nameToSearch);
    
    debug.info('UISearch.getViewType():');
    debug.debug(UISearch.getViewType());
    
    if (UISearch.getViewType() === UISearch.VIEW_TYPE.TEXT_LIST) {
    	UITextList.setUserConnectionSearch(null);
    } else if (UISearch.getViewType() === UISearch.VIEW_TYPE.ICON_LIST) {
    	UIIconList.setUserConnectionSearch(null);
    }
    
  	var peopleRestUrl = Configuration.portalEnvironment.peopleRestUrl + 
  											"?offset=" + UISearch.getOffset() + 
  										  "&limit=" + UISearch.getLimit() +
  											"&nameToSearch=" + UISearch.getNameToSearch();
										
  	debug.info("search url:");
  	debug.debug(peopleRestUrl);
  	
  	Util.makeRequest(peopleRestUrl, function(response) {
  		var userConnectionsActivities = Util.parseUserConnectionActivities(response);
  		debug.info("search result:");
  		debug.debug(userConnectionsActivities);
  	  display(userConnectionsActivities);
		});
  }
  
  /**
   * Init UI for searching.
   * 
   * @param params
   */
  UISearch.initSearchInput = function(params) {
  	UISearch.setOffset(params.offset);
  	UISearch.setLimit(params.limit);
  	UISearch.setViewType(params.viewType);
  	
  	var searchInputEl = document.getElementById("SearchTextBox");
    var searchButton = document.getElementById("SearchButton");
    
    if (searchInputEl) {
      
      $('#SearchTextBox').focus(function() {
        if (searchInputEl.value === DEFAULT_INPUT_VALUE) {
          searchInputEl.value = "";
        }        
        searchInputEl.style.color=FOCUS_COLOR;
      });
      
      $('#SearchTextBox').blur(function() {
        if (searchInputEl.value === "") {
          searchInputEl.value = DEFAULT_INPUT_VALUE;
          searchInputEl.style.color = BLUR_COLOR;
        }   
      });
      
      $('#SearchTextBox').keypress(function(event) {
        var e = event || window.event;
        var textBox = e.srcElement || e.target;
        var keynum = e.keyCode || e.which;  
        
        if((keynum === 13) && (textBox.value !== "")) {
          //searchUserConnectionActivities();
        } 
      });
    }
    
    if (searchButton) {
    	$('#SearchButton').click(function() {
    		searchUserConnectionActivities();
    		UISearch.setOffset(0);
      });
    }		
	};
	
	/**
	 * Load more result with search.
	 */
	UISearch.loadMore = function() {
		var peopleRestUrl = Configuration.portalEnvironment.peopleRestUrl + 
												"?offset=" + UISearch.getOffset() + 
											  "&limit=" + UISearch.getLimit() +
												"&nameToSearch=" + UISearch.getNameToSearch();

		debug.info("search url:");
		debug.debug(peopleRestUrl);
		
		Util.makeRequest(peopleRestUrl, function(response) {
			var userConnectionsActivities = Util.parseUserConnectionActivities(response);
			debug.info("search result:");
			debug.debug(userConnectionsActivities);
			display(userConnectionsActivities);
		});
	}
	
	// expose
  window_.exo = window_.exo || {};
  window_.exo.social = window_.exo.social || {};
  window_.exo.social.UISearch = UISearch;
})();