/**
 * The class defines configurations of gadget.
 * 
 * @since 1.2.4
 */
(function() {
	//refer to window
	var window_ = this,
			numberOfActivitiesEachFetch = 10,
			viewer = null;
	
	/**
	 * The constructor.
	 */
	function Configuration() {
	
	}
	
	/**
	 * Get the viewer.
	 */
	Configuration.getViewer = function() {
		return viewer;
	};
	
	/**
	 * Set the viewer.
	 * 
	 * @param view
	 */
	Configuration.setViewer = function(view) {
		viewer = view;
	};
	
	/**
	 * Gets the number of activities with each fetch request.
	 * 
	 * @return
	 */
	Configuration.getNumberOfActivitiesEachFetch = function() {
		return numberOfActivitiesEachFetch;
	};
	
	/**
	 * Sets the number of activities with each fetch request.
	 * 
	 * @param numberOfActivities
	 */
	Configuration.setNumberOfActivitiesEachFetch = function(numberOfActivities) {
		numberOfActivitiesEachFetch = numberOfActivities;
	};
	
	/**
	 * Define portal environment.
	 */
	Configuration.portalEnvironment = {
		host: null,
		portalName: null,
		restContextName: null,
		peopleRestUrl: null,
		activitiesRestUrl: null,
		peopleDirectory: null
	};
	
	/**
	 * Some div tags default.
	 */
	Configuration.divDefault = {
		search: '<div class="SearchContent ClearFix"><input type="text" id="SearchTextBox" value="Quick Search" class="Search"><a href="#" id="SearchButton" class="IconSearch">&nbsp;</a></div>',
		loadMore: '<div class="MoreContent"><a href="#" class="ReadMore" id="UITextListLoadMore"> Load more ... </a></div>',
		peopleDirectory: '<a href="#" class="Link">People Directory</a>'
	};
	
	//name space
	window_.exo = window_.exo || {};
	window_.exo.social = window_.exo.social || {};
	window_.exo.social.Configuration = Configuration;
})();