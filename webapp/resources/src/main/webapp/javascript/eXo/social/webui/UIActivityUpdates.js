/**
 * UIActivityUpdates.js
 */
(function ($){
	var UIActivityUpdates = {
	  numberOfUpdatedActivities: 0,
	  cookieName: '',
	  noUpdates: '',
	  currentRemoteId : '',
	  lastUpdatedActivitiesNumKey : '',
	  clientTimerAtStart: 0,
	  currentServerTime : 0,
	  ALL : 'ALL_ACTIVITIES',
	  CONNECTIONS : 'CONNECTIONS',
	  MY_SPACES : 'MY_SPACE',
	  MY_ACTIVITIES : 'MY_ACTIVITIES',
    CURRENT_SELECTED_TAB_KEY : "exo_social_activity_stream_tab_selected_%remoteId%",
    LAST_VISTED_TIME_FROM: "exo_social_activity_stream_%tab%_visited_%remoteId%_from",
    LAST_VISTED_TIME_TO: "exo_social_activity_stream_%tab%_visited_%remoteId%_to",
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
	    form.noUpdates = inputs.noUpdates || "";
	    form.selectedMode = inputs.selectedMode || "";
	    
      form.currentSelectedTabCookieKey =  form.CURRENT_SELECTED_TAB_KEY.replace(form.REMOTE_ID_PART, form.currentRemoteId);
      
      form.clientTimerAtStart = new Date().getTime();
      
      //
      if ( !form.selectedMode ) {
        form.selectedMode = form.ALL;
      }
      
	    //
	    $.each($('#UIActivitiesLoader').find('.UIActivity'), function(i, item) {
	      if(i < form.numberOfUpdatedActivities) {
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
           // reset cookies
           form.resetCookiesOnTabs();
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
	    
	    updatedEls.removeClass('UpdatedActivity');
	    
	    $('#numberInfo').html(form.noUpdates);
	    
	    form.resetCookiesOnTabs();
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
      
	    // [All Activities] is current Selected tab then reset all other tabs on visited time
	    if ( selectedTab === form.ALL ) {
	      form.applyChanges([form.ALL, form.CONNECTIONS, form.MY_SPACES, form.MY_ACTIVITIES]);
	    } else {
	      //
        form.applyChanges([selectedTab]);
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
			  var checked_tab_key_to = form.LAST_VISTED_TIME_TO.replace(form.TAB_PART, field).replace(form.REMOTE_ID_PART, userId);
			  
			  form.setToCookie(checked_tab_key_to, form.calculateServerTime());
			  form.setFromCookie(checked_tab_key_from, checked_tab_key_to);
			});
	  },
	  applyChanges : function( affectedFields ) { // FIELDS
	    if ( affectedFields.length === 0 ) return;
	    
	    var form = UIActivityUpdates;
	    var userId = form.currentRemoteId;
	    $.each( affectedFields, function( index, field ) {
	      var checked_tab_key_from = form.LAST_VISTED_TIME_FROM.replace(form.TAB_PART, field).replace(form.REMOTE_ID_PART, userId);
	      var checked_tab_key_to = form.LAST_VISTED_TIME_TO.replace(form.TAB_PART, field).replace(form.REMOTE_ID_PART, userId);
	      
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