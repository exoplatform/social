(function () {
	var window_ = this;
	var Util = eXo.social.Util;
	
	function UIContactSection() {
	}
	
	/**
	 * Inits textbox to add focus and blur capability.
	 * @param elementIds Id of component which will be added focus and blur capability.
	 * @param defaultValue Default value when initing.
	 */
	UIContactSection.prototype.initInputTextBox = function(elementIds, defaultValue) {
		var idList = elementIds.split(',');
		
		for (var i = 0; i<idList.length; i++) {
			this.urlChildInput = document.getElementById(idList[i]);
			if (this.urlChildInput == null) {
		    continue;	
		  }
		  
			(function(idx) {
				 this.input = document.getElementById(idList[idx]);
		     var inputValue = this.input.value;
		     if (inputValue != defaultValue) {
		       this.input.style.color = "#000000";
		     } else {
		       this.input.style.color = "#545454";			                             
		     }
		     
		     var uiContactSection = this;
		     
		     Util.addEventListener(this.input, 'focus', function() {
			       if (this.value == defaultValue) {
			         this.value = "";
			         this.style.color = "#000000";
			       }
		     }, false);
		     
		     Util.addEventListener(this.input, 'blur', function() {
		       if (this.value == "") {
		         this.value = defaultValue;
		         this.style.color = "#545454";
		       }
		     }, false);
		     
		  })(i);
		}
	}
	  //expose
  window_.eXo = window_.eXo || {};
  window_.eXo.social = window_.eXo.social || {};
  window_.eXo.social.webui = window_.eXo.social.webui || {};
  window_.eXo.social.webui.UIContactSection = UIContactSection;
})();