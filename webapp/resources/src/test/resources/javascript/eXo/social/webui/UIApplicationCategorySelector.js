(function($, Selectors) { 
var UIApplicationCategorySelector = {
  init: function(params) {
	  var applicationCategoryIds = (params.applicationCategoryIds.replace('[','').replace(']','')).split(',');
	  var allApplicationCategorySize = parseInt(params.allApplicationCategorySize);
	
    if(applicationCategoryIds != null) {
	    for ( var i = 0; i < allApplicationCategorySize; i++ ) {
        $('#' + applicationCategoryIds[i]).on('mouseover', function(evt){
          Selectors.UIItemSelector.onOver(this, true);
        });

        $("#" + applicationCategoryIds[i]).on('mouseout', function(evt){
          Selectors.UIUserSelector.onOver(this, false);
        });
	    }
    }
  }
};

return UIApplicationCategorySelector;
})($, selector);