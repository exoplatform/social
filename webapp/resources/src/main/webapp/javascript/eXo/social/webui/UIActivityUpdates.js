/**
 * UIActivityUpdates.js
 */
(function ($){
	var UIActivityUpdates = {
	  numberOfUpdatedActivities: 0,
	  cookieName: '',
	  noUpdates: '',
	  currentRemoteId : '',
	  ALL : 'ALL_ACTIVITIES',
	  CONNECTIONS : 'CONNECTIONS',
	  MY_SPACES : 'MY_SPACE',
	  MY_ACTIVITIES : 'MY_ACTIVITIES',
	  resetCookie: function(cookieKey, value) {
	    cookieKey = $.trim(cookieKey); 
	    eraseCookies(cookieKey)
		  eXo.core.Browser.setCookie(cookieKey, value, 365);
		  
		  function eraseCookies(key) {
		   var cookies = document.cookie.split(";");
		
	     for (var i = 0; i < cookies.length; i++) {
		     var cookie = cookies[i];
		     var eqPos = cookie.indexOf("=");
		     var name = eqPos > -1 ? cookie.substr(0, eqPos) : cookie;
		     name = $.trim(name);
		     key = $.trim(key);
		     
		     if (name == key) {
		       document.cookie = name + "=;expires=Thu, 01 Jan 1970 00:00:00 GMT";
		     }
	     }
		  };
		},
			  
	  init: function (numberOfUpdatedActivities, cookieName, noUpdates,currentRemoteId) {
	    //
	    var form = UIActivityUpdates;
	    
	    //
	    UIActivityUpdates.numberOfUpdatedActivities = numberOfUpdatedActivities;
	    UIActivityUpdates.cookieName = cookieName;
	    UIActivityUpdates.currentRemoteId = currentRemoteId;
	
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
	        $('#UIActivitiesLoader').find('.UpdatedActivity').removeClass('UpdatedActivity');
	        $('#numberInfo').html(noUpdates);
	        
	        form.resetCookiesOnTabs();
        
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
	   
	       for(var loop=0; loop < cookies.length; loop++) {
				    var name = cookies[loop].split('=');
				    if( name[0].toString() == 'SHTS' ) {
				      var cookieTime = parseInt( name[1] );
				    }
				    else if( name[0].toString() == 'SHTSP' ) {
				      var cookieName = name[1];
				    }
	       }
	   
	       if( cookieName && cookieTime && cookieName == escape(location.href) &&  Math.abs(now - cookieTime) <= 5 ) {
			    //
			    $('#UIActivitiesLoader').find('.UpdatedActivity').removeClass('UpdatedActivity');
			    $('#numberInfo').html(noUpdates);
			    form.resetCookiesOnTabs();
			    //refresh_prepare = 0; 
	       }   
			 };
			           
       function prepareForRefresh() {
			   if( refresh_prepare > 0 ) {
					 var today = new Date();
					 var now = today.getUTCSeconds();
					 document.cookie = 'SHTS=' + now + ';';
				   document.cookie = 'SHTSP=' + escape(location.href) + ';';
				 } else {
				   document.cookie = 'SHTS=;';
				   document.cookie = 'SHTSP=;';
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
	  resetCookiesOnTabs : function() {
	    var form = UIActivityUpdates;
	    var slected_tab_tmpl = "exo_social_activity_stream_tab_selected_%REMOTE_ID%";
	    var replaced_id = "%REMOTE_ID%";
	    var userId = UIActivityUpdates.currentRemoteId;
	    var onSelectedTabCookieName = slected_tab_tmpl.replace(replaced_id, userId);
      var selectedTab = form.getCookie(onSelectedTabCookieName);
      if ( selectedTab == null || $.trim(selectedTab).length === 0 ) {
        selectedTab = form.ALL;
        form.resetCookie(onSelectedTabCookieName, form.ALL);
      }
      
      //
      form.applyChanges([selectedTab]);
      
	    // [All Activities] is current Selected tab then reset all other tabs on visited time
	    if ( selectedTab === form.ALL ) {
	      form.applyChanges([form.CONNECTIONS, form.MY_SPACES, form.MY_ACTIVITIES]);
	    } else { // others
	      // [Connections]
	      
	      // [Space Activities]
	      
	      // [My Activities]
	    }
	  },
	  applyChanges : function( affectedFields ) { // FIELDS
	    if ( affectedFields.length === 0 ) return;
	    
	    var src_str = "exo_social_activity_stream_%FIELD%_visited_%REMOTE_ID%";
	    var replaced_field = "%FIELD%";
	    var replaced_id = "%REMOTE_ID%";
	    var userId = UIActivityUpdates.currentRemoteId;
	    $.each( affectedFields, function( index, field ) {
			  var changed_field = src_str.replace(replaced_field, field).replace(replaced_id, userId);
			  UIActivityUpdates.resetCookie(changed_field, (new Date().getTime()));
			});
	  },
	  getCookie : function(name) {
      if (document.cookie.length > 0) {
        var start = document.cookie.indexOf(name + "=")
        if (start != -1) {
          start = start + name.length + 1 ;
          var end = document.cookie.indexOf(";",start) ;
          if (end == -1) end = document.cookie.length ;
            return unescape(document.cookie.substring(start,end)) ;
        } 
      }
      return "" ;
    }
	};

  return UIActivityUpdates;
})($);