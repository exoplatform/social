/**
 * UIActivityLoader.js
 */
(function ($){
	var UIActivityLoader = {
	  delta : 65,    
	  hasMore: false,
	  scrollBottom : function() {
	  return $(document).height() - $(window).scrollTop() - $(window).height();  
	  },
	  init: function (parentId, hasMore) {
	    UIActivityLoader.hasMore = (hasMore === true || hasMore === 'true') ? true : false;
	    UIActivityLoader.initIndicator();
	
	    $(document).ready(function() {
	      // check onLoad page.
	      if(UIActivityLoader.scrollBottom() <= UIActivityLoader.delta) {
	        $(window).scrollTop($(document).height() - $(window).height() - (UIActivityLoader.delta+1));
	      }
	      $(window).scroll(function(e) {
	        var distanceToBottom = UIActivityLoader.scrollBottom();
	        var loadAnimation = $('#UIActivitiesLoader').find('div.ActivityIndicator'); 
	        var isLoading = loadAnimation.css("display") != "none";
	        if (distanceToBottom <= UIActivityLoader.delta && !isLoading) {
	          if (UIActivityLoader.hasMore === true) {
	            $(loadAnimation).stop(true, true).fadeIn(
                    500, function() {
	                  $(this).show();
	                  $('div.bottomContainer:last')[0].scrollIntoView(true);
	                  $('#ActivitiesLoader').click();
	                });
	          }
	        }
	      });
	      
	      UIActivityLoader.processBottomTimeLine();
	    });
	
	  },
	  setStatus : function(hasMore) {
	    if(UIActivityLoader.scrollBottom() <= UIActivityLoader.delta) {
	      $(window).scrollTop($(window).scrollTop()-5);
	    }
      UIActivityLoader.hasMore = (hasMore === true || hasMore === 'true') ? true : false;
	    UIActivityLoader.initIndicator();
	    UIActivityLoader.processBottomTimeLine();
	  },
	  initIndicator : function() {
	    $('#UIActivitiesLoader').find('div.ActivityIndicator').remove();
	    var activityIndicator = $('<div class="ActivityIndicator" id="ActivityIndicator" style="display:none"></div>');
	    for (var i=1; i < 9; i++) {
	      activityIndicator.append($('<div id="rotateG_0' + i + '" class="blockG"></div>'));
	    }
	    activityIndicator.appendTo('#UIActivitiesLoader');
	  },
	  processBottomTimeLine : function() {
	    //
      if ( UIActivityLoader.hasMore ) {
        $('div.activityBottom').hide();
        $('#ActivitiesLoader').parent().show();
      } else {
        $('div.activityBottom').show();
        $('#ActivitiesLoader').parent().hide();
      }
	  }
	};
  return UIActivityLoader;
})($);


