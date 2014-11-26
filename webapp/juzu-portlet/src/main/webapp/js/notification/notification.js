(function(sUtils, $) {

  var Notification = {
    formData: '',
    parentId: '#userNotification',
    saveSetting : function(e) {
      var jElm = $(this);
      if(jElm.is('button') && jElm.hasClass('disabled')) {
        return;
      }
      var id = jElm.attr('id');
      var msgOk = jElm.attr('data-ok');
      var msgNOk = jElm.attr('data-nok');
      
      Notification.formData = $(document.forms['uiNotificationSetting']).serialize();
      $(Notification.parentId).jzAjax({        
        url : "UserNotificationSetting.saveSetting()",
        data : {
          "params" : Notification.formData
        },
        success : function(data) {
          if(data.ok === 'true') {
            if(data.status === 'false') {
              $(Notification.parentId).find('div.form-horizontal:first').hide();
            } else {
              $(Notification.parentId).find('div.form-horizontal:first').show();
            }
          }
          if(jElm.is('button')) {
            jElm.addClass('disabled');
          }
        }
      }).fail(function(jqXHR, textStatus) {
        alert( "Request failed: " + textStatus + ". "+jqXHR);
      });
    },
    onload : function() {
      Notification.formData = $(document.forms['uiNotificationSetting']).serialize();
      var parent = $(Notification.parentId);
      var save = parent.find("button#Save").addClass('disabled');
      var reset = parent.find("button#Reset");
      //
      save.on('click', Notification.saveSetting) ;
      //
      reset.on('click', function(e) {
        var elm = $(this);
        var close = elm.parents('div:first').attr('data-close');
        var confTitle = elm.parents('div:first').attr('data-conf');
        var actions = {
          action: function() {
            $(Notification.parentId).jzAjax({        
              url : "UserNotificationSetting.resetSetting()",
              data : {},
              success : function(data) {
                var content = $('<div></div>').html(data).find('div.uiUserNotificationPortlet:first').html();
                $(Notification.parentId).html(content);
                Notification.onload();
              }
            }).fail(function(jqXHR, textStatus) {
              alert( "Request failed: " + textStatus + ". "+jqXHR);
            });
          }, 
          label: elm.attr('data-confirm-label')
        };
        sUtils.PopupConfirmation.confirm(elm.attr('id'), [actions], confTitle, elm.attr('data-confirm'), close);
      });
      //
      var horizontal = parent.find('div.form-horizontal');
      horizontal.find('tbody:first').find('input[type=checkbox]').on('click', Notification.checkActiveButton);
      horizontal.find('select').on('change', Notification.checkActiveButton);
      //
      horizontal.find('input.iphoneStyle').iphoneStyle({ 
        checkedLabel:'YES', 
        uncheckedLabel:'NO',
        onChange : function() {
          var input = $(this.elem);
          Notification.switchStatus(input.attr('name'), input.hasClass("staus-false"));
        }
      });
    },
    switchStatus : function(saveType, isEnable) {
      $(Notification.parentId).jzAjax({   
  	    url : "UserNotificationSetting.saveActiveStatus()",
  	    data : {
  	      "type" : saveType,
  	      "enable" : isEnable
  	    },
  	    success : function(data) {
  	      var parent = $(Notification.parentId);
  	      var action = parent.find('input[name=' + data.type + ']');
  	      var clazz = "enable", disabled = false;
  	      if((data.enable == 'true')) {
            action.attr('checked', 'checked');
          } else {
            action.removeAttr('checked');
            clazz = "disabled";
            disabled = true;
          }
  	      action.attr('class', 'iphoneStyle yesno staus-' + data.enable);
          var plugin = parent.find("td." + data.type)
                             .attr("class", data.type + ' center ' + clazz);
          plugin.find('input').prop('disabled', disabled);
          plugin.find('select').prop('disabled', disabled);
          //
  	    }
  	  }).fail(function(jqXHR, textStatus) {
  	    alert("Request failed: " + textStatus + ". " + jqXHR);
  	  });
  	},
    checkActiveButton : function(e) {
      var newData = $(document.forms['uiNotificationSetting']).serialize();
      var parent = $(Notification.parentId);
      if (Notification.formData !== newData) {
        parent.find("button#Save").removeClass('disabled');
        parent.find("button#Reset").removeClass('disabled');
      } else if (parent.find("button#Save").hasClass('disabled') === false) {
        parent.find("button#Save").addClass('disabled');
      }
    }
  };
  Notification.onload();
  return Notification;
})(socialUtil, jQuery);
