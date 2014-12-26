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
  
	Locale = exo.social.Locale;
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

  /**
   * Display the setting form.
   */
  UISetting.display = function() {
  	reset();
  };
  
  /**
   * Init the setting form.
   */
  UISetting.initSettingForm = function() {
  	
  	if ($('.ListSelected').length > 0) {
			$(uiComponent.ModeTextList).removeClass('ListSelected');
			$(uiComponent.ModeTextList).addClass('ListIcon');
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
      '<div class="Row clearfix">' +
			  '<div class="LabelIL">' + Locale.getMsg('view_type') + '</div>' +
			  '<select class="SelectboxIL" id="ViewType">' +
				  '<option value="ICON_LIST">' + Locale.getMsg('icon_list') + '</option>' +
          '<option value="TEXT_LIST">' + Locale.getMsg('text_list') + '</option>' +
			  '</select>' +
		  '</div>' +
		  
		  '<div class="Row clearfix">' +
			  '<div class="LabelUT">' + Locale.getMsg('update_time') + '</div>' +
			  '<select class="SelectboxUT" id="UpdateTime">' +
				  '<option value="5">' + Locale.getMsg('every_5_mins') + '</option>' +
          '<option value="30">' + Locale.getMsg('every_30_mins') + '</option>' +
          '<option value="60">' + Locale.getMsg('every_60_mins') + '</option>' +
          '<option value="0">' + Locale.getMsg('no_update') + '</option>' +
			  '</select>' +
		  '</div>' +
		  
		  '<div class="Row clearfix">' +
			  '<div class="LabelISB">' + Locale.getMsg('item_sort_by') + '</div>' +
			  '<select id="OrderBy" class="SelectboxISB">' +
          '<option value="RAND">' + Locale.getMsg('randomize') + '</option>' +
          '<option value="AZ_UN" disabled="disabled">' + Locale.getMsg('user_name_a_z') + '</option>' +
          '<option value="ZA_UN" disabled="disabled">' + Locale.getMsg('user_name_z_a') + '</option>' +
          '<option value="A_U_T">' + Locale.getMsg('activity_update') + '</option>' +
			  '</select>' +
		  '</div>' +
		  
		  '<div class="Row clearfix">' +
		    '<label class="LabelNIV" for="ItemPerViewNum">' + Locale.getMsg('number_item_per_view') + '</label>' +
			  '<input title="' + Locale.getMsg('number_item_per_view') + '" id="ItemPerViewNum" class="InputNIV" type="text" value="10"/>' +
		  '</div>' +
		  
		  '<a href="#" id="SaveButton" class="MCSettingButton">' + Locale.getMsg('save') + '</a>' +
	    '<a href="#" id="CancelButton" class="Link">' + Locale.getMsg('cancel') + '</a>' +
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
		  
		  // Disabled options fix for Internet Explorer
		  $('#OrderBy').each(function(){
		    this.rejectDisabled = function(){
		      if (this.options[this.selectedIndex].disabled){
		        if (this.lastSelectedIndex) {
		          this.selectedIndex = this.lastSelectedIndex;
		        } else {
		          var first_enabled = $(this).children('option:not(:disabled)').get(0);
		          this.selectedIndex = first_enabled ? first_enabled.index : 0;
		        }
		      } else {
		        this.lastSelectedIndex = this.selectedIndex;
		      }
		    };
		    this.rejectDisabled();
		    this.lastSelectedIndex = this.selectedIndex;
		    $(this).change(function() {
		      this.rejectDisabled();
		    });
		  });
	  });
  };
  
  /**
   * Save the setting.
   */
  function save() {
  
		var values = [];
		
		var viewType = "ICON_LIST",
		updateTime = 5 * 60 * 1000,
		orderBy = 'RAND',
		itemPerViewNum = 10;

    values.push($("#ViewType :selected").val());
    values.push($("#UpdateTime :selected").val());
    values.push($("#OrderBy :selected").val());
    values.push($("#ItemPerViewNum").val());
		
    prefs.setArray("SETTINGS", values);
    
    viewType = values[0];
    updateTime = parseInt(values[1]) * 60 * 1000;
    orderBy = values[2];
    itemPerViewNum = (values[3] !== '') ? parseInt(values[3]) : 10;
    
    ActivityStream.initProfiles({offset: 0, limit: itemPerViewNum,
    														viewType: viewType, updateTime: updateTime,
    														orderBy: orderBy});
    
    $(uiComponent.GadgetConnectionSetting).hide();
  };
  
  	
  /**
   * Cancel the current setting.
   */
  function cancel() {
  	reset();
  };
  
  /**
   * Reset the form.
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
  
  /**
   * Set option selected.
   */
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