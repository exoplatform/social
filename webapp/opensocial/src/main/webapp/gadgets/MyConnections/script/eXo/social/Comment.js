/**
 * Class comment for getting comments, displaying comments of an latest activity.
 * 
 * @since 1.2.4
 */
(function() {
	var window_ = this,
			offset = 0,
			limit = 0;
	
	Util = exo.social.Util;
  Configuration = exo.social.Configuration;
	
	/**
	 * The constructor of comment.
	 */
	function Comment() {
		
	}
	
	/**
	 * Set the offset.
	 * 
	 * @param off
	 */
	Comment.setOffset = function(off) {
		offset = off;
	};
	
	/**
	 * Get the offset.
	 */
	Comment.getOffset = function() {
		return offset;
	};
	
	/**
	 * Set the limit.
	 * 
	 * @param lim
	 */
	Comment.setLimit = function(lim) {
		limit = lim;
	};
	
	/**
	 * Get the limit.
	 */
	Comment.getLimit = function() {
		return limit;
	};
	
	/**
	 * Comment refer to an latest activity.
	 */
	Comment.refer = {
		connectionActivity: null
	};

	/**
	 * Get comments of current latest activity.
	 * 
	 * @param activityId
	 * @param callback
	 */
	Comment.getComments = function(activityId, callback) {
	  if (!activityId) {
	    debug.warn('[Comment.getComments]: activityId is null!');
	    return;
	  }
	  
	  var url = Configuration.portalEnvironment.activitiesRestUrl + 
	  					activityId + 
	  					"/comments.json?offset=" + Comment.getOffset() + "&limit=" + Comment.getLimit();
	  
	  debug.info("url get comments:");
	  debug.debug(url);
	  
	  Util.makeRequest(url, callback);
	}
	
	/**
	 * Load more comments of the current latest activity.
	 * 
	 * @param callback
	 */
	Comment.loadMore = function(callback) {
		var url = Configuration.portalEnvironment.activitiesRestUrl + 
							Comment.refer.connectionActivity.activityId + 
							"/comments.json?offset=" + Comment.getOffset() + "&limit=" + Comment.getLimit();
		
		debug.info("url Comment.loadMore:");
		debug.debug(url);
		
		Util.makeRequest(url, callback);
	};
	
	window_.exo = window_.exo || {};
	window_.exo.social = window_.exo.social || {};
	window_.exo.social.Comment = Comment;
})();