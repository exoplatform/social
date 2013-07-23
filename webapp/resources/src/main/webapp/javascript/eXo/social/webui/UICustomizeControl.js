(function($, uiForm) { 
	var UICustomizeControl = {
	  onLoad : function(uicomponentId) {
			$('#' + uicomponentId).find('input[type=radio]').
				on('click', function() {
					var uiForm = $(this).parents('.UIForm:first');
					if (uiForm.length > 0) {
						var action = uiForm.find('.uiAction:first').find('.btn:first');
						var link = String(action.attr('onclick')).replace('Create', 'ChangeOption');
						eval(link);			
					}				
				});
			
	
	  }
	};

	return UICustomizeControl;
})($, uiForm);