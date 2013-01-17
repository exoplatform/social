/**
 * UIActivityUpdates.js
 */
(function ($){
	var UIActivityUpdates = {
	  numberOfUpdatedActivities: 0,
	  cookieName: '',
	  noUpdates: '',
	  resetCookie: function(cookieKey) {
	    eraseCookies(cookieKey)
		  eXo.core.Browser.setCookie(cookieKey, (new Date().getTime()), 365);
		  
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
			  
	  init: function (numberOfUpdatedActivities, cookieName, noUpdates) {
	    //
	    UIActivityUpdates.numberOfUpdatedActivities = numberOfUpdatedActivities;
	    UIActivityUpdates.cookieName = cookieName;
	
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
	        UIActivityUpdates.resetCookie(UIActivityUpdates.cookieName);
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
			    UIActivityUpdates.resetCookie(UIActivityUpdates.cookieName);
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
	  
	  }
	};

  return UIActivityUpdates;
})($);