(function($, socialUtils) {
  var UserProfile = {
      init : function(id) {
        var portlet = $('#' + id);
        if(portlet.length > 0) {
          portlet.find('.multiValueContainer').find('.uiIconTrash').attr('class', 'uiIconClose uiIconLightGray');
          portlet.find('.uiExperien').each(function(i) {
            UserProfile.chechboxUtil($(this).attr('id'));
          });
          //
          $('#socialMainLayout').find('.right-column-containerTDContainer:first')
            .css('width', function(){
                if($(this).find('.UIRowContainer:last').find('div').length > 0) {
                  return '40%';
                }
                return '0px';
             });
        }
      },
      chechboxUtil : function(parentId) {
        var parent = $('#' + parentId);
        if(parent.length > 0) {
          var checkbox = parent.find('input[type=checkbox]');
          if(checkbox.is(':checked')) {
            parent.find('.control-group').eq(5).hide();
          }
          checkbox.change(function () {
            if($(this).is(':checked')) {
              parent.find('.control-group').eq(5).hide();
            } else {
              parent.find('.control-group').eq(5).show();
            }
          });
        }
      }
  };
  //
  UserProfile.init();
  return UserProfile;
})(jQuery, socialUtil);
