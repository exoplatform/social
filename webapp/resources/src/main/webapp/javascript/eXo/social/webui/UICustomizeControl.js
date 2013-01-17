(function($, uiForm) { 
var UICustomizeControl = {
  onLoad : function(uicomponentId) {
		$('#' + uicomponentId).click(function() {
		  $('input').each(function() {
	      var form = $(this).closest('.UIForm');
	      if (form != null ) uiForm.submitForm(form.attr("id"), 'ChangeOption', true);
	    });
	  });
  }
};

return UICustomizeControl;
})($, uiForm);