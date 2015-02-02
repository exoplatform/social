(function($, socialUtils) {
  var UserProfile = {
      portlet : null,
      init : function(id) {
        var portlet = $('#' + id);
        UserProfile.portlet = portlet;
        if(portlet.length > 0) {
          if(portlet.parents('#BasicProfilePortlet').length > 0) {
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
          //
          UserProfile.changeInput();
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
      },
      changeInput : function() {
        var status = UserProfile.portlet.data('btnStatus') || false;
        UserProfile.updateBtnSaveStatus(status);
        //
        var form = UserProfile.portlet.find('form');
        form.find('input, textarea, select').change(function(evt) {
          UserProfile.updateBtnSaveStatus(true);
        });
        form.find('i.uiIconClose').click(function(evt) {
          UserProfile.updateBtnSaveStatus(true);
        });
      },
      updateBtnSaveStatus : function(status) {
        UserProfile.portlet.data('btnStatus', status);
        var btn = UserProfile.portlet.find('button.btn-save');
        if (status) {
          btn.removeAttr('disabled');
        } else {
          btn.attr('disabled', 'disabled');
        }
      }
  };
  //
  return UserProfile;
})(jQuery, socialUtil);
