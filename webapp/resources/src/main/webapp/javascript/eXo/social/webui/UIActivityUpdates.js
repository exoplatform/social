/**
 * UIActivityUpdates.js
 */

var UIActivityUpdates = {
  numberOfUpdatedActivities: 0,
  cookieName: '',
  cookieValue: '',
  init: function (numberOfUpdatedActivities, cookieName, cookieValue) {
    //
    UIActivityUpdates.numberOfUpdatedActivities = numberOfUpdatedActivities;
    UIActivityUpdates.cookieName = cookieName;
    UIActivityUpdates.cookieValue = cookieValue;

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
        $('#numberInfo').html('No');
        document.cookie = UIActivityUpdates.cookieName + "=" + UIActivityUpdates.cookieValue;
       
        $(window).off('scroll', runOnScroll);
      }
    }

    $(window).on('scroll', runOnScroll );
  }
}

_module.UIActivityUpdates = UIActivityUpdates;
