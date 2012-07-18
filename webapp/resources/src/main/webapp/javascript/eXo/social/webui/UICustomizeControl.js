function UICustomizeControl() {
}

UICustomizeControl.prototype.onLoad = function(uicomponentId) {
	var UIForm = eXo.webui.UIForm;
	var element = document.getElementById(uicomponentId);
	
	var radioEls = element.getElementsByTagName('input');
	var numberOfEls = radioEls.length;
  for (var k = 0; k < numberOfEls; k++) {
  	var el = radioEls[k];
  	el.onclick = function(event) {
  		var e = event || window.event;
		  var radio = e.srcElement || e.target;
		  var form = gj(radio).closest('.UIForm');
			if (form != null ) UIForm.submitForm(form.attr("id"), 'ChangeOption', true);
  	}
  }
}

/*===================================================================*/
if(!eXo.social) eXo.social = {};
if(!eXo.social.webui) eXo.social.webui = {};
eXo.social.webui.UICustomizeControl = new UICustomizeControl();