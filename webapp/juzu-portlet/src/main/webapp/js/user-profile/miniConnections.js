(function($) {
  var MiniConnections = {
      labels: {},
      initUserProfilePopup : function(labels) {
        //
        MiniConnections.labels = settings = $.extend(true, {}, MiniConnections.labels, labels);
        $.each(MiniConnections.labels, function(key) {
          MiniConnections.labels[key] =  window.decodeURIComponent(MiniConnections.labels[key]);
        });
        
        // User Profile Popup initialize
        var portal = eXo.social.portal;
        var port = (window.location.port) ? window.location.port : '';
        var restUrl = window.location.protocol + '//' + window.location.hostname + port + portal.context + '/' + portal.rest + '/social/people' + '/getPeopleInfo/{0}.json';
        
        var userLinks = $("#UIMiniConnectionsPortlet").find('a:[href*="/profile/"]');
        $.each(userLinks, function (idx, el) {
            var userUrl = $(el).attr('href');
            var userId = userUrl.substring(userUrl.lastIndexOf('/') + 1);
            
            $(el).userPopup({
              restURL: restUrl,
              labels: MiniConnections.labels,
              content: false,
              defaultPosition: "left",
              keepAlive: true,
              maxWidth: "240px"
            });
        });
      },
      clearUserProfilePopup : function() {
        $('div#socialUsersData').stop().animate({
            'cursor':'none'
        }, 1000, function () {
            $(this).data("CacheSearch", {});
        });
      }
  }
  
  return MiniConnections;
})(jq);
