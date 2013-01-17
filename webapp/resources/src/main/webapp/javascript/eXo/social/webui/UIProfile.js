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
  }
};

return UIProfile;
})($);