(function($, webui) { 
var UICustomizeControl = {
  onLoad : function(uicomponentId) {
		$('#' + uicomponentId).click(function() {
		  $('input').each(function() {
	      var form = $(this).closest('.UIForm');
	      if (form != null ) webui.UIForm.submitForm(form.attr("id"), 'ChangeOption', true);
	    });
	  });
  }
};

return UICustomizeControl;
})($, webui);