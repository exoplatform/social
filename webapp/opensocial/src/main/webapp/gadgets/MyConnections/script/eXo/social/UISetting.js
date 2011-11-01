/**
 * UISetting class.
 * 
 * @since 1.2.4
 */

(function() {

	var window_ = this,
	    offset = 0,
			limit = 0,
	    viewType;
  
	Locale = eXo.social.Locale;
  Util = exo.social.Util;
  Configuration = exo.social.Configuration;
  SocialUtil = eXo.social.SocialUtil;
  UISearch = exo.social.UISearch;
  ActivityStream = exo.social.ActivityStream;
  
  	    
  var prefs = new gadgets.Prefs();

	  /**
   * UI component.
   */
  var uiComponent = {
    GadgetUIIconList: '#GadgetUIIconList',
  	GadgetUITextList: '#GadgetUITextList',
  	GadgetConnectionSetting: '#GadgetConnectionSetting',
  	ModeSetting: '#ModeSetting',
    ModeIconList: '#ModeIconList',
    ModeTextList: '#ModeTextList'
  }
  
	/**
	 * View types.
	 */
	UISetting.VIEW_TYPE = {
	  TEXT_LIST: "TEXT_LIST",
	  ICON_LIST: "ICON_LIST"
	};
	
  /**
   * Class definition
   */
  function UISetting () {
  	
  }

  UISetting.display = function() {
  	reset();
  };
  
  UISetting.initSettingForm = function() {
  	
  	if ($('.ListSelected').length > 0) {
			$(uiComponent.ModeIconList).removeClass('ListSelected');
			$(uiComponent.ModeIconList).addClass('NumberListIcon');
		}
		
		if ($('.NumberListSelected').length > 0) {
			$(uiComponent.ModeIconList).removeClass('NumberListSelected');
			$(uiComponent.ModeIconList).addClass('NumberListIcon');
		}
		
		if ($('.SettingIcon').length > 0) {
			$(uiComponent.ModeSetting).removeClass('SettingIcon');
			$(uiComponent.ModeSetting).addClass('SettingSelected');
		}
  	
    var settingContentBlock = 
    '<div class="SettingContent">' +
      '<div class="Row ClearFix">' +
			  '<div class="LabelIL">View Type</div>' +
			  '<select class="SelectboxIL" id="ViewType">' +
				  '<option value="ICON_LIST">Icon list</option>' +
          '<option value="TEXT_LIST">Text list</option>' +
			  '</select>' +
		  '</div>' +
		  
		  '<div class="Row ClearFix">' +
			  '<div class="LabelUT">Update Time</div>' +
			  '<select class="SelectboxUT" id="UpdateTime">' +
				  '<option value="5">Every 5 mins</option>' +
          '<option value="30">Every 30 mins</option>' +
          '<option value="60">Every 60 mins</option>' +
          '<option value="0">No update</option>' +
			  '</select>' +
		  '</div>' +
		  
		  '<div class="Row ClearFix">' +
			  '<div class="LabelISB">Item Sort by</div>' +
			  '<select id="OrderBy" class="SelectboxISB">' +
          '<option value="RAND">Randomize</option>' +
          '<option value="AZ_UN">User Name (A-Z)</option>' +
          '<option value="ZA_UN">User Name (Z-A)</option>' +
          '<option value="A_U_T">Activity Update</option>' +
			  '</select>' +
		  '</div>' +
		  
		  '<div class="Row ClearFix">' +
		    '<div class="LabelNIV">Number Item per view</div>' +
			  '<input id="ItemPerViewNum" class="InputNIV" type="text" value="10"/>' +
		  '</div>' +
		  
		  '<a href="#" id="SaveButton" class="MCSettingButton">Save</a>' +
	    '<a href="#" id="CancelButton" class="Link">Cancel</a>' +
	  '</div>';
	  
	  $(uiComponent.GadgetConnectionSetting).empty();
		$(uiComponent.GadgetConnectionSetting).append(settingContentBlock);
		
		$(document).ready(function(){
		  reset();
		  
		  $('#SaveButton').click(function() {
		    save();
		  });
		  
		  $('#CancelButton').click(function() {
		    cancel();
		  });
	  });
  };
  
  function save() {
  
		var values = [];
		
		var viewType = "ICON_LIST",
		updateTime,
		orderBy,
		itemPerViewNum = 10;

    values.push($("#ViewType :selected").val());
    values.push($("#UpdateTime :selected").val());
    values.push($("#OrderBy :selected").val());
    values.push($("#ItemPerViewNum").val());
		
    prefs.setArray("SETTINGS", values);
    
    viewType = values[0];
    updateTime = values[1];
    orderBy = values[2];
    itemPerViewNum = parseInt(values[3]);
    
    ActivityStream.initProfiles({offset: 0, limit: itemPerViewNum, viewType: viewType});
    
    debug.info('limit in ui setting:');
    debug.debug(itemPerViewNum);
    
    debug.info('viewType');
    debug.debug(viewType);
    
    $(uiComponent.GadgetConnectionSetting).hide();
  };
  
  	
  /**
   * private method
   */
  function cancel() {
  	reset();
  };
  
  /**
   * private method
   */
  function reset() {
  	var settings = prefs.getArray("SETTINGS");
  	if (settings != null) {
	  	$("#ViewType").val(settings[0]);
	  	setSelected("ViewType", settings[0]);
	    $("#UpdateTime").val(settings[1]);
	    setSelected("UpdateTime", settings[1]);
	    $("#OrderBy").val(settings[2]);
	    setSelected("OrderBy", settings[2]);
	    $("#ItemPerViewNum").val(settings[3]);
  	}
  };
  
  function setSelected(optId, val) {
    var text = '';
    var id = '#' + optId + ' option';
    $(id).each(function(i, option) {
	    text = $(id + ':eq('+i+')').text();
	    if(text.toLowerCase() == val){
	      $(id + ':eq('+i+')').attr('selected', true);
	    }
    });
  }
  
  window_.exo = window_.exo || {};
  window_.exo.social = window_.exo.social || {};
  window_.exo.social.UISetting = UISetting;
})();