(function() {
	var window_ = this,
			viewer,
			viewType;
			
	Util = exo.social.Util;
  Configuration = exo.social.Configuration;
  UITextList = exo.social.UITextList;
  UIIconList = exo.social.UIIconList;
  UISetting = exo.social.UISetting;
  
  var uiComponent = {
		GadgetUIIconList: '#GadgetUIIconList',
		GadgetUITextList: '#GadgetUITextList',
		GadgetConnectionSetting: '#GadgetConnectionSetting',
		ModeSetting: '#ModeSetting',
    ModeIconList: '#ModeIconList',
    ModeTextList: '#ModeTextList',
    GadgetMemberMore: '#GadgetMemberMore',
    UIIconListListContent: '#UIIconListListContent',
    UITextListListContent: '#UITextListListContent',
    UISearchContent: '#UISearchContent',
    SearchTextBox: '#SearchTextBox'
  };
  
	/**
	 * The constructor.
	 */
	function ActivityStream() {
		
	}
	
	/**
	 * The views type of activity stream.
	 */
	ActivityStream.VIEW_TYPE = {
	  TEXT_LIST: "TEXT_LIST",
	  ICON_LIST: "ICON_LIST",
	  SETTING: "SETTING"
	};

	/**
	 * Init activity stream
	 * 
	 * @param params
	 */
	function initActivityStream(params) {
		var viewerOpts = {};
    viewerOpts[opensocial.DataRequest.PeopleRequestFields.PROFILE_DETAILS] =
            [opensocial.Person.Field.ID,
             opensocial.Person.Field.NAME,
             opensocial.Person.Field.PROFILE_URL,
             opensocial.Person.Field.THUMBNAIL_URL,
             "portalName",
             "restContext",
             "host"
            ];

    Util.getViewer(viewerOpts, function(res) {
      if (res.hadError()) {
        debug.error('Failed to get viewer');
        debug.info(res);
        return;
      }
      
      viewer = res.get('viewer').getData();
      
      // set viewer
      Configuration.setViewer(viewer);
      
      debug.info("viewer:");
      debug.debug(viewer);

      var configPeopleRestUrl = viewer.getField('hostName') + "/" + 
      													viewer.getField('restContextName') + "/" + 
      													"social/people/" + 
      													viewer.getField('portalName') + "/" + 
      													"getConnections.json";
      
       var configActivitiesRestUrl = viewer.getField('hostName') + "/" + 
       															 viewer.getField('restContextName') + "/" +
       															 viewer.getField('portalName') + "/" +
       															 "social/activities/";
      
      Configuration.portalEnvironment = {
        'portalName': viewer.getField('portalName'),
        'restContextName': viewer.getField('restContextName'),
        'host': viewer.getField('hostName'),
        'peopleRestUrl': configPeopleRestUrl,
        'activitiesRestUrl': configActivitiesRestUrl
      };
      
      debug.info('Configuration.portalEnvironment:');
      debug.debug(Configuration.portalEnvironment);
      
      //get activities of connections
      var peopleRestUrl = Configuration.portalEnvironment.peopleRestUrl + 
      										"?offset=" + params.offset + 
      										"&limit=" + params.limit;

			debug.info("peopleRestUrl:");
			debug.debug(peopleRestUrl);
			
			
			//get user with latest activities by jquery
			Util.makeRequest(peopleRestUrl, function(response) {
				var userConnections = [];
				
				debug.info('response');
				debug.debug(response);
				
				if (response.rc === 200 && response.data !== null) {
					userConnections = Util.parseUserConnectionActivities(response);
				}
				
				if ($(uiComponent.GadgetConnectionSetting).length > 0) {
	  			$(uiComponent.GadgetConnectionSetting).empty();
	  		}
				
				if (params.viewType === ActivityStream.VIEW_TYPE.TEXT_LIST) {
					$(uiComponent.UITextListListContent).empty();
					
					UITextList.setOffset(params.offset);
					UITextList.setLimit(params.limit);
					UITextList.setUserConnection(userConnections);
					UITextList.setSearchMode(false);
					UITextList.display();
				} else if (params.viewType === ActivityStream.VIEW_TYPE.ICON_LIST) {
					$(uiComponent.UIIconListListContent).empty();
					
					UIIconList.setOffset(params.offset);
					UIIconList.setLimit(params.limit);
					UIIconList.setUserConnectionList(userConnections);
					UIIconList.setSearchMode(false);
					UIIconList.display();
				} else if (params.viewType === ActivityStream.VIEW_TYPE.SETTING) {
					UISetting.display();
				}
				
				debug.info("userConnections:");
				debug.debug(userConnections);
			});
    });
	}
	
	/**
	 * Loads viewer and init connection's activities.
	 * 
	 * @param params
	 */
  ActivityStream.initProfiles = function(params) {
    initActivityStream(params);
    
    // Set the update time.
		if (params.updateTime > 0) {
			setInterval(function() {
				initActivityStream(params);
			}, params.updateTime);
		}
  };
	
	//name space
	window_.exo = window_.exo || {};
	window_.exo.social = window_.exo.social || {};
	window_.exo.social.ActivityStream = ActivityStream;
})();