/**
 * Utils for getting data.  
 * 
 * @since 1.2.4
 */
(function () {
	var window_ = this;
	
	Locale = exo.social.Locale;
	Configuration = exo.social.Configuration;
	
	/**
	 * The constructor.
	 */
	function Util() {
		
	}
	
	/**
	 * Gets the current viewer.
	 * 
	 * @param requestOptions
	 * @param callback
	 */
	Util.getViewer = function(requestOptions, callback) {
		var request = opensocial.newDataRequest();
		request.add(request.newFetchPersonRequest(opensocial.IdSpec.PersonId.VIEWER, requestOptions), 'viewer');
		request.send(callback);
	};
	
	/**
	 * Gets the friends of the current viewer.
	 * 
	 * @param requestOptions
	 * @param callback
	 */
	Util.getViewerFriends = function(requestOptions, callback) {
		var request = opensocial.newDataRequest();
		var viewerFriendsIdSpec = opensocial.newIdSpec({"userId":"VIEWER", "groupId":"FRIENDS"});
    request.add(request.newFetchPeopleRequest(viewerFriendsIdSpec, requestOptions), 'viewerFriends');
		request.send(callback);
	};
	
	/**
	 * Get the activity by id.
	 * 
	 * @param params
	 * @param callback
	 */
	Util.getActivity = function(params, callback) {
		if (!params.activityId) {
	    debug.warn('[Comment.getComments]: activityId is null!');
	    return;
	  }
	  var url = Configuration.portalEnvironment.activitiesRestUrl + 
	  					params.activityId + ".json?" + 
							"poster_identity=t" +
							"&number_of_comments=" + params.limit + 
							"&number_of_likes=100" +
							"&activity_stream=t";
	  
	  Util.makeRequest(url, callback);
	};
	
	/**
	 * Parse response to user connection activities.
	 * 
	 * @param response
	 * @return userConnectionsActivities
	 */
	Util.parseUserConnectionActivities = function(response) {
		var userConnectionsActivities = [];
		if (response.rc === 200) {
			if (response.data !== null && response.data !== null) {
				$.each(response.data, function(index, userConnection) {
					var paramsUser = {
						displayName: userConnection.displayName,
						profileUrl: userConnection.profileURL,
						avatarUrl: userConnection.avatarURL,
						position: userConnection.position,
						activityId: userConnection.activityId,
						activityTitle: userConnection.activityTitle,
						prettyPostedTime: userConnection.prettyPostedTime
					};
					var connectionActivity = new exo.social.User(paramsUser);
					userConnectionsActivities.push(connectionActivity);
				});
			}
		}
		
		return userConnectionsActivities;
	};
	
	/**
	 * Get user connectiona activities block.
	 * 
	 * @param userConnectionActivities
	 * @return
	 */
	Util.getUserConnectionActivitiesBlock = function(userConnectionActivities) {
		var userBlock = [];
		$.each(userConnectionActivities, function(index, userConnection) {
			userBlock.push('<li class="clearfix ListListContent">');
				userBlock.push('<a target="_blank" href="' + userConnection.profileUrl + '" class="Avatar"><img title="' + userConnection.displayName + '" alt="' + userConnection.displayName + '" width="30px" height="30px" src="' + userConnection.avatarUrl + '"/></a>');
				userBlock.push('<div class="Content">');
					userBlock.push('<a target="_blank" href="' + userConnection.profileUrl + '" class="User">' + userConnection.displayName +'</a><span class="Member">' + userConnection.position +'</span>');
					userBlock.push('<div class="Work">' + userConnection.activityTitle + '</div>');
				userBlock.push('</div>');
				userBlock.push('<a href="javascript:void(0)" class="More" id="' + userConnection.activityId + '">' + Locale.getMsg('more') + '</a>');
  		userBlock.push('</li>');
  	});
		
		return userBlock.join('');
	};
	
	/**
	 * makes remote request
	 * @param	url
	 * @param	callback
	 * @param	opt_refreshInterval
	 * @param	opt_method
	 * @param	opt_contentType
	 * @static
	 */
	Util.makeRequest = function(url, callback, opt_refreshInterval, opt_method, opt_contentType, opt_postData) {
	  //TODO handles method + contentType
	  var refreshInterval = opt_refreshInterval || 0;
	  var method = gadgets.io.MethodType.GET;
	  var contentType = gadgets.io.ContentType.JSON;
	  var postData = null,
	  headers = {};
	  switch(opt_method) {
	    case gadgets.io.MethodType.POST:
	      method = gadgets.io.MethodType.POST;
	      break;
	    case gadgets.io.MethodType.PUT:
	      method = gadgets.io.MethodType.PUT;
	      break;
	    case gadgets.io.MethodType.HEAD:
	      method = gadgets.io.MethodType.HEAD;
	      break;
	    case gadgets.io.MethodType.DELETE:
	      method = gadgets.io.MethodType.DELETE;
	      break;
	    default:
	      method = gadgets.io.MethodType.GET;
	      break;
	  }
	
	  switch (opt_contentType) {
	    case gadgets.io.ContentType.TEXT:
	      contentType = gadgets.io.ContentType.TEXT;
	      break;
	    case gadgets.io.ContentType.DOM:
	      contentType = gadgets.io.ContentType.DOM;
	      break;
	    case gadgets.io.ContentType.JSON:
	      contentType = gadgets.io.ContentType.JSON;
	      break;
	    case gadgets.io.ContentType.FEED:
	      contentType = gadgets.io.ContentType.FEED;
	      break;
	    default:
	      contentType = gadgets.io.ContentType.JSON;
	      break;
	  }
	
	  //TODO fined-check
	  if (opt_postData) {
	    switch(contentType) {
	      case gadgets.io.ContentType.TEXT:
	        postData = gadgets.io.encodeValues(opt_postData);
	        break;
	      case gadgets.io.ContentType.JSON:
	        postData = gadgets.json.stringify(opt_postData);
	        headers = {"Content-Type":"application/json; charset=UTF-8"};
	        break;
	       //TODO handles more
	      default:
	        postData = gadgets.io.encodeValues(opt_postData);
	        break;
	    }
	  }
	
	  var ts = new Date().getTime();
	  var sep = "?";
	  if (refreshInterval && refreshInterval > 0) {
	      ts = Math.floor(ts / (refreshInterval * 1000));
	  }
	  
	  if (url.indexOf("?") > -1) {
	     sep = "&";
	  }
	
	  url = [ url, sep, "nocache=", ts ].join("");
	  var params = {};
	  params[gadgets.io.RequestParameters.AUTHORIZATION] = gadgets.io.AuthorizationType.SIGNED;
	  params[gadgets.io.RequestParameters.METHOD] = method;
	  params[gadgets.io.RequestParameters.CONTENT_TYPE] = contentType;
	  if (postData) {
	    params[gadgets.io.RequestParameters.POST_DATA] = postData;
	  }
	  params[gadgets.io.RequestParameters.HEADERS] = headers;
	  gadgets.io.makeRequest(url, callback, params);
	}
	
	/**
	 * Get the setting.
	 * 
	 * @return
	 */
  Util.getSetting = function() {
  	var prefs = new gadgets.Prefs();
  	var settings = prefs.getArray("SETTINGS");
  	
  	var settingStored = {
  		viewType: 'ICON_LIST',
  		updateTime: 5 * 60 * 1000,
  		orderBy: 'RAND',
  		itemPerViewNum: 10
  	};
  	
  	if (settings !== null) {
  		if (settings[0] !== undefined)	{
  			settingStored.viewType = settings[0];
  		}
  		if (settings[1] !== undefined) {
  			settingStored.updateTime = parseInt(settings[1]) * 60 * 1000;
  		}
  		if (settings[2] !== undefined) {
  			settingStored.orderBy = settings[2]; 
  		}
  		if (settings[3] !== undefined) {
  			settingStored.itemPerViewNum = parseInt(settings[3]); 
  		}
  	}
  	
  	return settingStored;
  };
	 
	//name space
	window_.exo = window_.exo || {};
	window_.exo.social = window_.exo.social || {};
	window_.exo.social.Util = Util;
})();