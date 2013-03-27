(function($, uiForm) { 
	var UICustomizeControl = {
	  onLoad : function(uicomponentId) {
			$('#' + uicomponentId).find('input[type=radio]').
				on('click', function() {
					var uiForm = $(this).parents('.UIForm:first');
					if (uiForm.length > 0) {
						uiForm.submitForm(form.attr("id"), 'ChangeOption', true);					
					}				
				});
			
	
	  }
	};

	return UICustomizeControl;
})($, uiForm);