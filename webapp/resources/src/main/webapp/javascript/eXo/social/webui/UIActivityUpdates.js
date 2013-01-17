/**
 * UIActivityUpdates.js
 */
(function ($){
	var UIActivityUpdates = {
	  numberOfUpdatedActivities: 0,
	  cookieName: '',
	  noUpdates: '',
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
	        eXo.core.Browser.setCookie(cookieKey, (new Date().getTime()), 365);
	        $(window).off('scroll', runOnScroll);
	      }
	    }
	
	    $(window).on('scroll', runOnScroll );
	  }
	};

  return UIActivityUpdates;
})($);