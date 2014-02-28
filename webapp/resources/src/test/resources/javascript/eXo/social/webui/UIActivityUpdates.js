/**
 * UIActivityUpdates.js
 */
(function ($){
	var UIActivityUpdates = {
	  numberOfUpdatedActivities: 0,
	  cookieName: '',
	  updates: '',
	  noUpdates: '',
	  currentRemoteId : '',
	  lastUpdatedActivitiesNumKey : '',
	  clientTimerAtStart: 0,
	  currentServerTime : 0,
	  isOnMyActivities: false,
	  hasPaging: false,
	  totalNumKey: '',
	  ACTIVITIES_ON_PAGE_NUM: 20,
	  ALL : 'ALL_ACTIVITIES',
	  CONNECTIONS : 'CONNECTIONS',
	  MY_SPACES : 'MY_SPACE',
	  MY_ACTIVITIES : 'MY_ACTIVITIES',
    CURRENT_SELECTED_TAB_KEY : "exo_social_activity_stream_tab_selected_%remoteId%",
    LAST_VISTED_TIME_FROM: "exo_social_activity_stream_%tab%_visited_%remoteId%_from",
    LAST_VISTED_TIME_OLD_FROM: "exo_social_activity_stream_%tab%_visited_%remoteId%_old_from",
    LAST_VISTED_TIME_TO: "exo_social_activity_stream_%tab%_visited_%remoteId%_to",
    LAST_UPDATED_ACTIVITIES_NUM : "exo_social_last_updated_activities_num_on_%tab%_of_%remoteId%",
    TOTAL_UPDATED_ACTIVITIES_NUM : "exo_social_total_updated_activities_num_on_%tab%_of_%remoteId%",
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
			  
	  init: function (inputs) {
	    //
	    var form = UIActivityUpdates;
	    
	    //
	    form.currentServerTime = inputs.currentServerTime*1 || 0;
	    form.numberOfUpdatedActivities = inputs.numberOfUpdatedActivities*1 || 0;
	    form.cookieName = inputs.cookieName || "";
	    form.currentRemoteId = inputs.currentRemoteId || "";
	    form.updates = inputs.updates || "";
	    form.noUpdates = inputs.noUpdates || "";
	    form.selectedMode = inputs.selectedTab || "";
	    form.isOnMyActivities = inputs.isOnMyActivities || false;
	    
	    form.hasPaging = form.numberOfUpdatedActivities > form.ACTIVITIES_ON_PAGE_NUM;
	    
	    form.currentSelectedTabCookieKey =  form.CURRENT_SELECTED_TAB_KEY.replace(form.REMOTE_ID_PART, form.currentRemoteId);
	  
	    form.clientTimerAtStart = new Date().getTime();
	    
	    //
	    if ( !form.selectedMode ) {
	      form.selectedMode = form.ALL;
	    }
    
      //
			var totalNumKey = form.TOTAL_UPDATED_ACTIVITIES_NUM.replace(form.TAB_PART, form.selectedMode);
			form.totalNumKey = totalNumKey.replace(form.REMOTE_ID_PART, form.currentRemoteId);
			
			var totalNum = eXo.core.Browser.getCookie(form.totalNumKey);
			if ( !totalNum || totalNum*1 <= 0 ) {
			  form.resetCookie(form.totalNumKey, form.numberOfUpdatedActivities);  
			} else {
			  form.resetCookie(form.totalNumKey, 0);
			}
      
      //
      $('#numberInfo').html(form.updates.replace("{0}", form.numberOfUpdatedActivities));
      
      //
      if (form.numberOfUpdatedActivities > 0) {
        $('#UIUserActivitiesDisplay').addClass('notSeen');
      }
      
      $.each($('#UIActivitiesLoader').find('.activityStream'), function(i, item) {
        if(i < form.numberOfUpdatedActivities) {
          $(item).addClass('updatedActivity');
        }
      });
	
	    function isScrolledIntoView() {
	      var scrollTop = $(window).scrollTop();
	      var docViewBottom = scrollTop + $(window).height();
	      var elem = $('#UIActivitiesLoader').find('.updatedActivity:last');
	      if(elem.length > 0) {
	        var elemTop = elem.offset().top;
	        var elemBottom = elemTop + elem.height();
	        return ((elemBottom >= scrollTop) && (elemTop <= docViewBottom));
	      }
	      return false;
	    }
	    
	    function runOnScroll() {
	      if (isScrolledIntoView()) {
	        if ( !form.hasPaging ) {
	          form.unMarkedAsUpdate();
	          $(window).off('scroll', runOnScroll);
	        } else {
	          form.unMarkedPageAsUpdate();
	        }
	        
	        if (form.numberOfUpdatedActivities > 0) {
              form.resetCookiesOnTabs();
	        }
            
	        
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
	         // set last Updated Number onto cookie
	         //var lastUpdatedActivitiesNumKey = form.LAST_UPDATED_ACTIVITIES_NUM.replace(form.TAB_PART, form.selectedMode);
			 //    lastUpdatedActivitiesNumKey = lastUpdatedActivitiesNumKey.replace(form.REMOTE_ID_PART, form.currentRemoteId);
			 //    form.resetCookie(lastUpdatedActivitiesNumKey, form.numberOfUpdatedActivities);
			     
		     //  if (form.numberOfUpdatedActivities == 0) {
	         //  form.resetCookiesOnTabs();
			 //    }
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
		     //prepareForRefresh();
		   }); 
			           
			 $(window).load(function() {
			   checkRefresh();
			 });
	  
	  },
	  unMarkedAsUpdate : function() {
	    var form = UIActivityUpdates;
	    var updatedEls = $('#UIActivitiesLoader').find('.updatedActivity');
	    $('#UIUserActivitiesDisplay').removeClass('notSeen');
	    updatedEls.removeClass('updatedActivity');
	    
	    $('#numberInfo').html(form.noUpdates);
	    
	    
	  },
	  unMarkedPageAsUpdate : function() {
	    var form = UIActivityUpdates;
			var updatedEls = $('#UIActivitiesLoader').find('.updatedActivity');
			var limit = form.numberOfUpdatedActivities > form.ACTIVITIES_ON_PAGE_NUM ? form.ACTIVITIES_ON_PAGE_NUM : form.numberOfUpdatedActivities;
			
			for( var id=0; id < limit; id++ ) {
			  $(updatedEls[id]).removeClass('updatedActivity');
			  form.numberOfUpdatedActivities = form.numberOfUpdatedActivities - 1;
			}
			
			//
			if ( form.numberOfUpdatedActivities > 0 ) {
			  $('#numberInfo').html(form.updates.replace("{0}", form.numberOfUpdatedActivities));
			} else {
			  $('#numberInfo').html(form.noUpdates);
			}
			
			//
			form.hasPaging = form.numberOfUpdatedActivities*1 > form.ACTIVITIES_ON_PAGE_NUM;
	  },
	  markActivitiesOnPageLoad : function() {
		  var form = UIActivityUpdates;
		  
		  var totalNum = eXo.core.Browser.getCookie(form.totalNumKey);
		  
		  var seenActivitiesNum = totalNum - form.numberOfUpdatedActivities;
		  
		  var limit = form.numberOfUpdatedActivities > form.ACTIVITIES_ON_PAGE_NUM ? form.ACTIVITIES_ON_PAGE_NUM : form.numberOfUpdatedActivities;
		  
		  //
			$.each($('#UIActivitiesLoader').find('.activityStream'), function(i, item) {
			  if(i > seenActivitiesNum && i <= seenActivitiesNum + limit) {
			    $(item).addClass('updatedActivity');
			  }
			});
			
			if ( seenActivitiesNum == 0 ) {
			  form.resetCookie(form.totalNumKey, 0);
			}
		},
	  resetCookiesOnTabs : function() {
	    var form = UIActivityUpdates;
	    var userId = UIActivityUpdates.currentRemoteId;
	    var onSelectedTabCookieName = form.CURRENT_SELECTED_TAB_KEY.replace(form.REMOTE_ID_PART, userId);
      var selectedTab = eXo.core.Browser.getCookie(onSelectedTabCookieName);
      if ( selectedTab == null || $.trim(selectedTab).length === 0 ) {
        selectedTab = form.ALL;
        form.resetCookie(onSelectedTabCookieName, form.ALL);
      }
      
      form.applyChanges([selectedTab]);
      
	    // [All Activities] is current Selected tab then reset all other tabs on visited time
	    if ( selectedTab === form.ALL ) {
	      form.applyChanges([form.CONNECTIONS, form.MY_SPACES, form.MY_ACTIVITIES]);
	    }
	  },
	  initCookiesForFirstRun : function(userId, currentServerTime) {
      var form = UIActivityUpdates;
	    form.currentRemoteId = userId;
			var checked_tab_key_from = form.LAST_VISTED_TIME_FROM.replace(form.TAB_PART, form.ALL).replace(form.REMOTE_ID_PART, userId);
			var checked_tab_cookie_from = eXo.core.Browser.getCookie(checked_tab_key_from);
			
			// check if the first run or not, if not then quit
			if ( checked_tab_cookie_from && checked_tab_cookie_from.length > 0 ) {
			  return;
			}
			
			// init timer
			form.clientTimerAtStart = new Date().getTime();
      form.currentServerTime = currentServerTime*1;
        
			form.initValueOnTabs([form.ALL, form.CONNECTIONS, form.MY_SPACES, form.MY_ACTIVITIES]);
			
			//
			var currentSelectedTabCookieKey = form.CURRENT_SELECTED_TAB_KEY.replace(form.REMOTE_ID_PART, userId);
			
			form.resetCookie(currentSelectedTabCookieKey, form.ALL);
	  },
	  removeUpdateInfo : function() {
	    var updatedEls = $('#UIActivitiesLoader').find('.updatedActivity');
	    var updatedInfoBox = $('#UIActivitiesLoader').find('.UpdateInfo');
	    $('#UIUserActivitiesDisplay').removeClass('notSeen');
      updatedEls.removeClass('updatedActivity');
      updatedInfoBox.remove();
	  },
	  setFromCookie : function(from_key, to_key) {
	    var to_value = eXo.core.Browser.getCookie(to_key);
	    UIActivityUpdates.resetCookie(from_key, to_value);
	  },
	  setToCookie : function(to_key, value) {
	    UIActivityUpdates.resetCookie(to_key, value);
	  },
	  initValueOnTabs : function( affectedFields ) { // FIELDS
	    if ( affectedFields.length === 0 ) return;
	    
	    var form = UIActivityUpdates;
	    var userId = form.currentRemoteId;
	    $.each( affectedFields, function( index, field ) {
			  var checked_tab_key_from = form.LAST_VISTED_TIME_FROM.replace(form.TAB_PART, field).replace(form.REMOTE_ID_PART, userId);
			  var checked_tab_key_old_from = form.LAST_VISTED_TIME_OLD_FROM.replace(form.TAB_PART, field).replace(form.REMOTE_ID_PART, userId);
			  var checked_tab_key_to = form.LAST_VISTED_TIME_TO.replace(form.TAB_PART, field).replace(form.REMOTE_ID_PART, userId);
			  
			  form.setToCookie(checked_tab_key_to, form.calculateServerTime());
			  form.setFromCookie(checked_tab_key_from, checked_tab_key_to);
			  form.setFromCookie(checked_tab_key_old_from, checked_tab_key_from);
			  
			  //			  			
			  var lastUpdatedActivitiesNumKey = form.LAST_UPDATED_ACTIVITIES_NUM.replace(form.TAB_PART, field);
		    lastUpdatedActivitiesNumKey = lastUpdatedActivitiesNumKey.replace(form.REMOTE_ID_PART, userId);
		    form.resetCookie(lastUpdatedActivitiesNumKey, 0);
			});
	  },
	  applyChanges : function( affectedFields ) { // FIELDS
	    if ( affectedFields.length === 0 ) return;
	    var form = UIActivityUpdates;
	    var userId = form.currentRemoteId;
	    $.each( affectedFields, function( index, field ) {
	      var checked_tab_key_from = form.LAST_VISTED_TIME_FROM.replace(form.TAB_PART, field).replace(form.REMOTE_ID_PART, userId);
	      var checked_tab_key_old_from = form.LAST_VISTED_TIME_OLD_FROM.replace(form.TAB_PART, field).replace(form.REMOTE_ID_PART, userId);
	      var checked_tab_key_to = form.LAST_VISTED_TIME_TO.replace(form.TAB_PART, field).replace(form.REMOTE_ID_PART, userId);
	      var lastUpdatedNumKey = form.LAST_UPDATED_ACTIVITIES_NUM.replace(form.TAB_PART, field).replace(form.REMOTE_ID_PART, userId);
	      
	      //
	      form.resetCookie(lastUpdatedNumKey, 0);
	      
	      form.setFromCookie(checked_tab_key_old_from, checked_tab_key_from);
	      form.setFromCookie(checked_tab_key_from, checked_tab_key_to);
	      form.setToCookie(checked_tab_key_to, form.calculateServerTime());
	    });
	  },
	  calculateServerTime : function() {
	    var form = UIActivityUpdates;
	    var currentClientTime = new Date().getTime();
	    var duration = currentClientTime - form.clientTimerAtStart;
	    var currentServerTime = form.currentServerTime + duration;
	    return $.trim(currentServerTime);
	  }
	};

  return UIActivityUpdates;
})($);