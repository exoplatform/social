(function(sUtils, $) {

  var Notification = {
    saveSetting : function(e) {
      var jElm = $(this);
      var id = jElm.attr('id');
      var msgOk = jElm.attr('data-ok');
      var msgNOk = jElm.attr('data-nok');
      
      var close = jElm.parents('div:first').attr('data-close');
      var ok = jElm.parents('div:first').attr('data-ok');
      var infoTitle = jElm.parents('div:first').attr('data-info');
      $("#userNotification").jzAjax({        
        url : "UserNotificationSetting.saveSetting()",
        data : {
          "params" : $(document.forms['uiNotificationSetting']).serialize()
        },
        success : function(data) {
          if(data.ok === 'true') {
            if(data.status === 'false') {
              $("#userNotification").find('div.form-horizontal:first').hide();
            } else {
              $("#userNotification").find('div.form-horizontal:first').show();
            }
            if(id !== 'checkBoxDeactivate') {
              sUtils.PopupConfirmation.confirm(id, {}, infoTitle, msgOk, ok);
            }
          } else if(id !== 'checkBoxDeactivate') {
            sUtils.PopupConfirmation.confirm(id, {}, infoTitle, msgNOk, close);
          }
        }
      }).fail(function(jqXHR, textStatus) {
        alert( "Request failed: " + textStatus + ". "+jqXHR);
      });
    },
    onload : function() {
      var activeNotification = $("input#checkBoxDeactivate"); 
      var save = $("button#Save");
      var reset = $("button#Reset");
      
      activeNotification.on('click', Notification.saveSetting) ;
      save.on('click', Notification.saveSetting) ;
      
      
      reset.on('click', function(e) {
        var elm = $(this);
        var close = elm.parents('div:first').attr('data-close');
        var confTitle = elm.parents('div:first').attr('data-conf');
        var actions = {
          action: function() {
            $("#userNotification").jzAjax({        
              url : "UserNotificationSetting.resetSetting()",
              data : {},
              success : function(data) {
                var content = $('<div></div>').html(data).find('div.uiUserNotificationPortlet:first').html();
                $("#userNotification").html(content);
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
    }
  };
  Notification.onload();
  return Notification;
})(socialUtil, jq);
