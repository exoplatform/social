function UICustomizeControl() {
}

UICustomizeControl.prototype.onLoad = function(uicomponentId) {
	gj('#' + uicomponentId).click(function() {
	  gj('input').each(function() {
      var form = gj(this).closest('.UIForm');
      if (form != null ) eXo.webui.UIForm.submitForm(form.attr("id"), 'ChangeOption', true);
    });
  });
}

/*===================================================================*/
eXo.social.webui.UICustomizeControl = new UICustomizeControl();