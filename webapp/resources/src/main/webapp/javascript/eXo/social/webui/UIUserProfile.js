(function($, socialUtils, uiProfile) {
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
              if($(this).find('#right-editprofile-container').length > 0) {
                if($(this).find('#right-editprofile-container').find('.UIRowContainer:last').find('div').length > 0) {
                  return '40%';
                }
                return '0px';
              } else {
                return '';
              }
            });
          }
          //
          UserProfile.changeInput();
          UserProfile.handlerEnterKeydown();
        }
      },
      leftBorder : function() {
        var leftRow = $('.LeftColumnContainerTDContainer:first');
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
          var checkProcess = function(input) {
            if(input.is(':checked')) {
              var existingMandatory = parent.find('.control-group').eq(5).find('.controls').find('>span').length > 0;
              if (!existingMandatory) {
                parent.find('.control-group').eq(5)
                  .find('.controls').append($("<span> *  </span>"));
              }
              parent.find('.control-group').eq(6).hide();
            } else {
              parent.find('.control-group').eq(6).show();
              parent.find('.control-group').eq(5)
              		.find('.controls').find('span').remove();
            }
          }
          //
          checkProcess(checkbox);
          checkbox.change(function () {
        	  checkProcess($(this));
          });
        }
      },
      changeInput : function() {
        var status = UserProfile.portlet.data('btnStatus') || false;
        UserProfile.updateBtnSaveStatus(status);
        //
        var form = UserProfile.portlet.find('form');
        form.find('input, textarea, select').on('change input', function(evt) {
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
      },
      handlerEnterKeydown : function(status) {
        var form = UserProfile.portlet.find('form');
        form.find('input').on('keydown', function(evt) {
          if(evt.which == 13) {
            form.find('button.btn-save:first').trigger('click');
          }
        });
      },
      loadingProfileSize : function(componentId) {
        var portlet = $('#' + componentId);
        var loadSizeUrl = portlet.find('div.loadingSizeLink:first').text();
        if(loadSizeUrl && loadSizeUrl.length > 0) {
          $.ajax(loadSizeUrl).done(function(data) {
            if(data && data.showAll == true) {
              //
              var textA = portlet.find('div.viewAllConnection:first').show().find('a:first');
              textA.html(textA.data('text') + '&nbsp;(' + data.size + ')' );
            }
          });
        }
      },
      loadingProfile : function(componentId) {
        var portlet = $('#' + componentId);
        var loadProfileUrl = portlet.find('div.loadingProfilesLink:first').text();
        var container = portlet.find('div.profileContainer:first');
        if(loadProfileUrl && loadProfileUrl.length > 0) {
          $.ajax(loadProfileUrl).done(function(html) {
            container.find('.uiLoadingIconMedium').remove();
            if(html && html.length > 0) {
              var items = $(html);
              container.prepend(items);
              //
              UserProfile.loadingProfileSize(componentId);
              //
              uiProfile.initUserProfilePopup(componentId, {});
            } else {
              container.hide();
              portlet.find('div.borderContainer.empty:first').show();
            }
          });
        }
      }
  };
  //
  return UserProfile;
})(jQuery, socialUtil, socialUIProfile);
