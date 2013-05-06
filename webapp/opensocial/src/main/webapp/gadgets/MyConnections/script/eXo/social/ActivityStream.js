(function() {
	var window_ = this,
			viewer,
			viewType;
			
	Util = exo.social.Util;
  Configuration = exo.social.Configuration;
  UITextList = exo.social.UITextList;
  UIIconList = exo.social.UIIconList;
  UISetting = exo.social.UISetting;
  UISearch = exo.social.UISearch;
  Locale = exo.social.Locale;
  
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
             "host",
             "peopleUri"
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
      
      var configPeopleRestUrl = viewer.getField('hostName') + "/" + 
      													viewer.getField('restContextName') +
      													"/social/people/" + 
      													viewer.getField('portalName') +
      													"/getConnections.json";
      
       var configActivitiesRestUrl = viewer.getField('hostName') + "/" + 
       															 viewer.getField('restContextName') + "/" +
       															 viewer.getField('portalName') + "/" +
       															 "social/activities/";
      
      var peopleDirectory = viewer.getField('hostName') + parent.eXo.env.portal.context + "/" + parent.eXo.env.portal.portalName + '/people'; 
       
      Configuration.portalEnvironment = {
        'portalName': viewer.getField('portalName'),
        'restContextName': viewer.getField('restContextName'),
        'host': viewer.getField('hostName'),
        'peopleRestUrl': configPeopleRestUrl,
        'activitiesRestUrl': configActivitiesRestUrl,
        'peopleDirectory': peopleDirectory
      };
      
      var lang = Locale.getLang();
      
      //get activities of connections
      var peopleRestUrl = Configuration.portalEnvironment.peopleRestUrl + 
      										"?offset=" + params.offset + 
      										"&limit=" + params.limit +
      										"&lang=" + lang;

			//get user with latest activities by jquery
			Util.makeRequest(peopleRestUrl, function(response) {
				var userConnections = [];
				
				if (response.rc === 200 && response.data !== null) {
					userConnections = Util.parseUserConnectionActivities(response);
				}
				
				if ($(uiComponent.GadgetConnectionSetting).length > 0) {
	  			$(uiComponent.GadgetConnectionSetting).empty();
	  		}
				
				UISearch.setNameToSearch(null);
				
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
				var settingStored = Util.getSetting();
		  	
				initActivityStream({offset: 0, 
														limit: settingStored.itemPerViewNum,
		  											viewType: settingStored.viewType,
		  											updateTime: settingStored.updateTime,
		  											orderBy: settingStored.orderBy});
			}, params.updateTime);
		}
  };
	
	//name space
	window_.exo = window_.exo || {};
	window_.exo.social = window_.exo.social || {};
	window_.exo.social.ActivityStream = ActivityStream;
})();