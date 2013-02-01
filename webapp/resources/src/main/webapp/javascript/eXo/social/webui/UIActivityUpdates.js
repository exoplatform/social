/**
 * UIActivityUpdates.js
 */
(function ($){
	var UIActivityUpdates = {
	  numberOfUpdatedActivities: 0,
	  cookieName: '',
	  noUpdates: '',
	  currentRemoteId : '',
	  seenCookieKey : '',
	  notSeenCookieKey : '',
	  lastUpdatedActivitiesNumKey : '',
	  hasUpdated : false,
	  ALL : 'ALL_ACTIVITIES',
	  CONNECTIONS : 'CONNECTIONS',
	  MY_SPACES : 'MY_SPACE',
	  MY_ACTIVITIES : 'MY_ACTIVITIES',
    NOT_SEEN_ACTIVITIES_KEY : "exo_social_not_seen_activities_%remoteId%",
    SEEN_ACTIVITIES_KEY : "exo_social_seen_activities_%remoteId%",
    CURRENT_SELECTED_TAB_KEY : "exo_social_activity_stream_tab_selected_%remoteId%",
    LAST_UPDATED_ACTIVITIES_NUM : "exo_social_last_updated_activities_num_on_%tab%_of_%remoteId%",
    REMOTE_ID_PART: "%remoteId%",
    TAB_PART: "%tab%",
    setCookies : function(name, value, expiredays) {
      var exdate = new Date();
      exdate.setDate(exdate.getDate() + expiredays);
      expiredays = ((expiredays == null) ? "" : ";expires=" + exdate.toGMTString());
      var path = ';path=/portal';
      document.cookie = name + "=" + escape(value) + expiredays + path;
    },
	  resetCookie: function(cookieKey, value) {
	    cookieKey = $.trim(cookieKey); 
	    UIActivityUpdates.setCookies(cookieKey, '', -365);
	    UIActivityUpdates.setCookies(cookieKey, value, 365);
		},
			  
	  init: function (numberOfUpdatedActivities, cookieName, noUpdates, currentRemoteId, selectedMode) {
	    //
	    var form = UIActivityUpdates;
	    
	    //
	    form.numberOfUpdatedActivities = numberOfUpdatedActivities;
	    form.cookieName = cookieName;
	    form.currentRemoteId = currentRemoteId;
	    form.noUpdates = noUpdates;
	    
	    form.seenCookieKey =  form.SEEN_ACTIVITIES_KEY.replace(form.REMOTE_ID_PART, currentRemoteId);
	    form.notSeenCookieKey =  form.NOT_SEEN_ACTIVITIES_KEY.replace(form.REMOTE_ID_PART, currentRemoteId);
      form.currentSelectedTabCookieKey =  form.CURRENT_SELECTED_TAB_KEY.replace(form.REMOTE_ID_PART, currentRemoteId);
      
      
      //
      if ( selectedMode ) {
        selectedMode = form.ALL;
      }
      
      var lastUpdatedActivitiesNumKey = form.LAST_UPDATED_ACTIVITIES_NUM.replace(form.TAB_PART, selectedMode);
      lastUpdatedActivitiesNumKey = lastUpdatedActivitiesNumKey.replace(form.REMOTE_ID_PART, currentRemoteId);
      
      //
      var lastUpdate= eXo.core.Browser.getCookie(lastUpdatedActivitiesNumKey);
      form.hasUpdated = (lastUpdate*1 !== numberOfUpdatedActivities*1);
      form.resetCookie(lastUpdatedActivitiesNumKey, numberOfUpdatedActivities);

	    //
	    $.each($('#UIActivitiesLoader').find('.UIActivity'), function(i, item) {
	      if(i < numberOfUpdatedActivities) {
	        $(item).addClass('UpdatedActivity');
	      }
	    });
	
	    function isScrolledIntoView() {
	      var docViewTop = $(window).scrollTop();
	      var docViewBottom = docViewTop + $(window).height();
	      var elem = $('#UIActivitiesLoader').find('.UpdatedActivity:last');
	      if(elem.length > 0) {
	        var elemTop = elem.offset().top;
	        var elemBottom = elemTop + $(elem).height();
	        return ((elemBottom >= docViewTop) && (elemTop <= docViewBottom));
	      }
	      return false;
	    }
	    
	    function runOnScroll() {
	      if (isScrolledIntoView()) {
	        form.unMarkedAsUpdate();
        
	        $(window).off('scroll', runOnScroll);
	      }
	    }
	
	    $(window).on('scroll', runOnScroll );
	    
	    //
			function checkRefresh()    {
	       var today = new Date();
	       var now = today.getUTCSeconds();
	   
	       var cookie = document.cookie;
	       var cookies = cookie.split('; ');
	   
	       
	       var result = {};
	       for (var i = 0; i < cookies.length; i++) {
	           var cur = cookies[i].split('=');
	           result[cur[0]] = cur[1];
	       }
	       
	       var cookieTime = parseInt( result.SHTS );
	       var cookieName = result.SHTSP;
	   
	       if( cookieName && cookieTime && cookieName == escape(location.href) &&  Math.abs(now - cookieTime) <= 5 ) {
			    //
			    if (form.hasUpdated === false ) {
			      form.unMarkedAsUpdate();
			    }
			    
			    //refresh_prepare = 0; 
	       }   
			 };
			           
       function prepareForRefresh() {
			   if( refresh_prepare > 0 ) {
					 var today = new Date();
					 var now = today.getUTCSeconds();
					 form.setCookies('SHTS', now);
					 form.setCookies('SHTSP', window.location.href);
				 } else {
           form.setCookies('SHTS', '0');
           form.setCookies('SHTSP', ' ');
				 }
       };
			           
			 var refresh_prepare = 1;
			
		   $(window).unload(function(){
		     prepareForRefresh();
		   }); 
			           
			 $(window).load(function() {
			   checkRefresh();
			 });
	  
	  },
	  unMarkedAsUpdate : function() {
	    var form = UIActivityUpdates;
	    var updatedEls = $('#UIActivitiesLoader').find('.UpdatedActivity');
	    
	    /* * ====== add ids to have been seen ============================================= */
	    
	    // reset to empty in case of action taken on [All Activities]
	    var currentSelectedTabOnCookies = eXo.core.Browser.getCookie(form.currentSelectedTabCookieKey);
	    if ( currentSelectedTabOnCookies == form.ALL ) {
	      form.resetCookie(form.seenCookieKey, '');
	      form.resetCookie(form.notSeenCookieKey, '');
	    } else {
	        // get seen activity list from cookies
	        var seenActivitiesOnCookies = eXo.core.Browser.getCookie(form.seenCookieKey);
	        var seenActivities = [];
	        if ( seenActivitiesOnCookies && seenActivitiesOnCookies.length > 0 ) {
	          seenActivities = seenActivitiesOnCookies.split('_');
	        }
	        
	        // get activity ids
	        var activityContextBoxes = $(updatedEls).find("[id^='ActivityContextBox']");
	        
	        $.each( activityContextBoxes, function(idx, el) {
	          var id = $(el).attr("id").replace('ActivityContextBox', '');
	          if ( $.inArray(id, seenActivities) ) {
	            (seenActivities.length == 0) ? seenActivities.push(id) : seenActivities.push(id);
	          } 
	        });
	        
	        // reset cookies value
	        form.resetCookie(form.seenCookieKey, seenActivities.join('_'));
	    }
	    /* * =============================================================================== */
	    
	    updatedEls.removeClass('UpdatedActivity');
	    
	    $('#numberInfo').html(form.noUpdates);
	    
	    form.resetCookiesOnTabs();
	  },
	  resetCookiesOnTabs : function() {
	    var form = UIActivityUpdates;
	    var slected_tab_tmpl = "exo_social_activity_stream_tab_selected_%REMOTE_ID%";
	    var replaced_id = "%REMOTE_ID%";
	    var userId = UIActivityUpdates.currentRemoteId;
	    var onSelectedTabCookieName = slected_tab_tmpl.replace(replaced_id, userId);
      var selectedTab = eXo.core.Browser.getCookie(onSelectedTabCookieName);
      if ( selectedTab == null || $.trim(selectedTab).length === 0 ) {
        selectedTab = form.ALL;
        form.resetCookie(onSelectedTabCookieName, form.ALL);
      }
      
      //
      form.applyChanges([selectedTab]);
      
	    // [All Activities] is current Selected tab then reset all other tabs on visited time
	    if ( selectedTab === form.ALL ) {
	      form.applyChanges([form.CONNECTIONS, form.MY_SPACES, form.MY_ACTIVITIES]);
	    }
	  },
	  initCookiesForFirstRun : function(userId) {
	    UIActivityUpdates.currentRemoteId = userId;
	    var form = UIActivityUpdates;
      var src_str = "exo_social_activity_stream_%FIELD%_visited_%REMOTE_ID%";
			var replaced_field = "%FIELD%";
			var replaced_id = "%REMOTE_ID%";
			var checked_tab_key = src_str.replace(replaced_field, form.ALL).replace(replaced_id, userId);
			var checked_tab_cookie = eXo.core.Browser.getCookie(checked_tab_key);
			
			if ( checked_tab_cookie && checked_tab_cookie.length > 0 ) {
			  return;
			}
			
			form.applyChanges([form.ALL, form.CONNECTIONS, form.MY_SPACES, form.MY_ACTIVITIES]);
			
			//
			var currentSelectedTabCookieKey = form.CURRENT_SELECTED_TAB_KEY.replace(form.REMOTE_ID_PART, userId);
			form.resetCookie(currentSelectedTabCookieKey, form.ALL);
	  },
	  applyChanges : function( affectedFields ) { // FIELDS
	    if ( affectedFields.length === 0 ) return;
	    
	    var form = UIActivityUpdates;
	    var src_str = "exo_social_activity_stream_%FIELD%_visited_%REMOTE_ID%";
	    var replaced_field = "%FIELD%";
	    var replaced_id = "%REMOTE_ID%";
	    var userId = form.currentRemoteId;
	    $.each( affectedFields, function( index, field ) {
			  var changed_field = src_str.replace(replaced_field, field).replace(replaced_id, userId);
			  form.resetCookie(changed_field, (new Date().getTime()));
			  
			  //
			  var lastUpdatedActivitiesNumKey = form.LAST_UPDATED_ACTIVITIES_NUM.replace(form.TAB_PART, field);
        lastUpdatedActivitiesNumKey = lastUpdatedActivitiesNumKey.replace(form.REMOTE_ID_PART,form.currentRemoteId);
        form.resetCookie(lastUpdatedActivitiesNumKey, 0);
			});
	  }
	};

  return UIActivityUpdates;
})($);