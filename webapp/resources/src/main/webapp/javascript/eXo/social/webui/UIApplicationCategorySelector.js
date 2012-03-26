(function() {
	var window_ = this,
	Util = eXo.social.Util,
	UIItemSelector = eXo.webui.UIItemSelector,
	applicationCategoryIds = null,
	applicationCategoryEls = null,
	allApplicationCategorySize = 0;
	
	/**
	 * UIApplicationCategorySelector constructor.
	 */
	function UIApplicationCategorySelector(params) {
	  UIApplicationCategorySelector.configure(params);
	  UIApplicationCategorySelector.init();
	};
	
	/**
	 * Configure parameters.
	 */
	UIApplicationCategorySelector.configure = function(params) {
		applicationCategoryIds = UIApplicationCategorySelector.toArray(params.applicationCategoryIds) || null;
		allApplicationCategorySize = parseInt(params.allApplicationCategorySize);
	}
	
	/**
	 * Initialize UIApplicationCategorySelector.
	 */
	UIApplicationCategorySelector.init = function() {
		applicationCategoryEls = [];
		if(applicationCategoryIds != null) {
			for (var i = 0; i < allApplicationCategorySize; i++) {
				applicationCategoryEls[i] = Util.getElementById(applicationCategoryIds[i]);
			}
		}
		
		if (applicationCategoryEls != null) {
      for (var i = 0; i < allApplicationCategorySize; i++) {
        (function(i) {
          //when this element is displayed
          if (applicationCategoryEls[i]) {
            Util.addEventListener(applicationCategoryEls[i], ['mouseover', 'focus'], function(evt) {
            	UIItemSelector.onOver(this,true);
            }, false);

            Util.addEventListener(applicationCategoryEls[i], ['mouseout', 'blur'], function(evt) {
            	UIItemSelector.onOver(this,false);
            }, false);
          }
        })(i);
      }
    }
	}
	
	/**
	 * Convert string to array.
	 * @param {string} str String to convert.
	 */
	UIApplicationCategorySelector.toArray = function(str) {
		var returnArray = str.split(",");
		for(var i = 0; i < returnArray.length; i++) {
			returnArray[i] = returnArray[i].replace(/[\[\]\s]/g, "");
		}
		return returnArray;
	}
	
  //expose
  window_.eXo = window_.eXo || {};
  window_.eXo.social = window_.eXo.social || {};
  window_.eXo.social.webui = window_.eXo.social.webui || {};
  window_.eXo.social.webui.UIApplicationCategorySelector = UIApplicationCategorySelector;
})();
