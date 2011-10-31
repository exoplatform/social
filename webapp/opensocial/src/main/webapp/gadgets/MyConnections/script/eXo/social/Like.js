(function() {
	var window_ = this;
	
	Configuration = exo.social.Configuration;
	Util = exo.social.Util;
	
	
	/**
	 * The constructor.
	 */
	function Like() {
		
	}
	
	/**
	 * The current activity id.
	 */
	Like.ref = {
			currentActivity: null
	};
	
	/**
	  * Get like ids.
	  * 
	  * @param	activityId
	  * @param	callback
	  * @static
	  */
	Like.getLikeIds = function(activityId, callback) {
	  if (!activityId) {
	    debug.warn("activityId is null");
	    return;
	  }
	  
	  Like.currentActivityId = activityId;
	  
	  var url = Configuration.portalEnvironment.activitiesRestUrl +  
	  					activityId + "/" + 
	  					"likes/show.json";
	  Util.makeRequest(url, callback);
	}
	
	/**
	 * Set like id with current activity.
	 * 
	 * @param	activityId
	 * @param	userId
	 * @param	callback
	 * @static
	 */
	Like.setLikeId = function(activityId, userId, callback) {
	  if (!activityId || !userId) {
	    debug.warn("activityId or userId is null! activityId: " + activityId + "; userId: " + userId);
	    return;
	  }
	 
	  Like.currentActivityId = activityId;
	  
	  var url = Configuration.portalEnvironment.activitiesRestUrl +
	  					activityId + "/" + 
	  					"likes/update.json";
	  
	  var like = {
	    identityId: userId
	  }
	 Util.makeRequest(url, callback, null, gadgets.io.MethodType.POST, gadgets.io.ContentType.JSON, like);
	}
	
	//Expose
	window_.exo = window_.exo || {};
	window_.exo.social = window_.exo.social || {};
	window_.exo.social.Like = Like;
})();