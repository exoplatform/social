(function($) { 
var UIProfile = {
  KEYS : {
    ENTER : 13
  },
  init: function(params) {
    var positionId = params.positionId || null;
    var saveButtonId = params.saveButtonId || null;

    var positionEl = $("#" + positionId);
    var saveButtonEl = $("#" + saveButtonId);

    if (positionEl.length > 0 && saveButtonEl.length > 0) {
      positionEl.on('keydown', function(event) {
        if ((event.keyCode || event.which) == UIProfile.KEYS.ENTER) {
          saveButtonEl.click();
          event.stopPropagation()
          return;
        }
      });
    }
  },
  initUserProfilePopup : function(actionLabels) {
  console.log(actionLabels);
    // User Profile Popup initialize
	  var portal = eXo.social.portal;
	  var restUrl = 'http://' + window.location.host + portal.context + '/' + portal.rest + '/social/people' + '/getPeopleInfo/{0}.json';
    
    var userLinks = $('a:[href*="/profile/"]');

    $.each(userLinks, function (idx, el) {
        var userUrl = $(el).attr('href');
        var userId = userUrl.substring(userUrl.lastIndexOf('/') + 1);
        
        $(el).userPopup({
          restURL: restUrl,
          actionLabels: actionLabels,
          content: false,
          defaultPosition: "left",
          keepAlive: true,
          maxWidth: "240px"
        });
    });
  }
};

return UIProfile;
})($);