(function($, socialUtils) {
  var UserProfile = {
      init : function() {
        
        UserProfile.leftBorder();
      },
      leftBorder : function() {
        var leftRow = $('.left-column-containerTDContainer:first');
        if(leftRow.length > 0) {
          leftRow.css('position', 'relative');
          leftRow.append($('<div class="left-border-row"></div>'))
        }
      } 
  };
  //
  UserProfile.init();
  return UserProfile;
})(jQuery, socialUtil);
