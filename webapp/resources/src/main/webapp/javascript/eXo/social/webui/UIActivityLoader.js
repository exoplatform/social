/**
 * UIActivityLoader.js
 */

var UIActivityLoader = {
  hasMore: false,
  init: function (hasMore) { 
    UIActivityLoader.hasMore = hasMore;
    $('#ActivityIndicator').hide();

    $(window).scroll(function(e) {
      var distanceToBottom = $(document).height() - $(window).scrollTop() - $(window).height();
      var isLoading = $('#ActivityIndicator').css('display') != 'none';
      if (distanceToBottom <= 0 && !isLoading) {
        if (UIActivityLoader.hasMore) {
          $('#ActivityIndicator').animate({'display': 'block'}, 2000, function() {
                                                                    $('#ActivityIndicator').show();
                                                                    $('#ActivitiesLoader').click();
                                                                  });
        }
      }
    });
  },
  setStatus : function(hasMore) {
    UIActivityLoader.hasMore = hasMore;
    UIActivityLoader.initIndicator();
  },
  initIndicator : function() {
    $('#ActivityIndicator').remove();
    var activityIndicator = $('<div>').attr('id', 'ActivityIndicator');
    for (var i=1; i < 9; i++) {
      activityIndicator.append($("<div>").attr("id", "rotateG_0" + i).addClass("blockG"));
    }
    activityIndicator.insertAfter('#UIActivitiesContainer');
  }
}
window.jq = $;
_module.UIActivityLoader = UIActivityLoader;
