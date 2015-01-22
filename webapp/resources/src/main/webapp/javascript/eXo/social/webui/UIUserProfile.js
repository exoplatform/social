(function($, socialUtils) {
  var UserProfile = {
      init : function(id) {
        var portlet = $('#' + id);
        if(portlet.length > 0) {
          if(portlet.parents('#left-column-container').length > 0) {
            UserProfile.leftBorder();
          } else {
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
        }
      },
      leftBorder : function() {
        var leftRow = $('.left-column-containerTDContainer:first');
        if(leftRow.length > 0) {
          leftRow.css('position', 'relative');
          leftRow.append($('<div class="left-border-row"></div>'))
          //RightBodyTDContainer
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
