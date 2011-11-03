/**
 * MyConnectionsGadget class, this is the main class.
 *
 * How to use:
 *
 * var MyConnectionsGadget = eXo.social.MyConnectionsGadget;
 * MyConnectionsGadget.init();
 * MyConnectionsGadget.display(MyConnectionsGadget.VIEW.TEXT_LIST_MODE);
 * 
 * @since 1.2.4
 */

  
(function($) {
	var window_ = this;
	var prefs = new gadgets.Prefs();

	Locale = exo.social.Locale;
	Comment = exo.social.Comment;
	Like = exo.social.Like;
	UISearch = exo.social.UISearch;
	UISetting = exo.social.UISetting;
	UITextList = exo.social.UITextList;
	UIIconList = exo.social.UIIconList;
	ActivityStream = exo.social.ActivityStream;
	Configuration = exo.social.Configuration;
	SocialUtil = eXo.social.SocialUtil;
	
	debug.info('Locale');
	debug.debug(Locale);
	
	/**
	 * UI component.
	 */
	var uiComponent = {
		GadgetUIIconList: '#GadgetUIIconList',
		GadgetUITextList: '#GadgetUITextList',
		GadgetConnectionSetting: '#GadgetConnectionSetting',
		ModeSetting: '#ModeSetting',
    ModeIconList: '#ModeIconList',
    ModeTextList: '#ModeTextList',
    GadgetMemberMore: '#GadgetMemberMore',
    UIIconListListContent: '#UIIconListListContent',
    UITextListListContent: '#UITextListListContent',
    UISearchContent: '#UISearchContent',
    SearchTextBox: '#SearchTextBox'
	};
	
	/**
	 * The constructor.
	 */
  function MyConnectionsGadget() {
  	
  }
  
  /**
   * Get the setting.
   * 
   * @return
   */
  function getSetting() {
  	var settings = prefs.getArray("SETTINGS");
  	
  	var settingStored = {
  		viewType: 'ICON_LIST',
  		updateTime: 5 * 60 * 1000,
  		orderBy: 'RAND',
  		itemPerViewNum: 10
  	};
  	
  	debug.info('settings');
  	debug.debug(settings);
  	
  	if (settings !== null) {
  		if (settings[0] !== undefined)	{
  			settingStored.viewType = settings[0];
  		}
  		if (settings[1] !== undefined) {
  			settingStored.updateTime = parseInt(settings[1]) * 60 * 1000;
  		}
  		if (settings[2] !== undefined) {
  			settingStored.orderBy = settings[2]; 
  		}
  		if (settings[3] !== undefined) {
  			settingStored.itemPerViewNum = parseInt(settings[3]); 
  		}
  	}
  	
  	debug.info('setting stored:');
  	debug.debug(settingStored);
  	
  	return settingStored;
  }
  
  /**
   * The view type of gadget.
   */
  MyConnectionsGadget.VIEW_TYPE = {
    TEXT_LIST: "TEXT_LIST",
    ICON_LIST: "ICON_LIST",
    SETTING: "SETTING"
  };
  
  /**
   * Main point of gadget.
   */
  MyConnectionsGadget.main = function() {
  	var settingStored = getSetting();
  	
  	ActivityStream.initProfiles({offset: 0, limit: settingStored.itemPerViewNum,
  															viewType: settingStored.viewType, updateTime: settingStored.updateTime,
  															orderBy: settingStored.orderBy});
  	
  	if (settingStored.viewType === 'TEXT_LIST') {
  		if ($(uiComponent.UITextListListContent).length > 0) {
  			$(uiComponent.UITextListListContent).empty();
  		}
  		
  		if ($(uiComponent.GadgetConnectionSetting).length > 0) {
  			$(uiComponent.GadgetConnectionSetting).css('display', 'none');
  		}
  	}
  	
  	if (settingStored.viewType === 'ICON_LIST') {
  		if ($(uiComponent.UIIconListListContent).length > 0) {
  			$(uiComponent.UIIconListListContent).empty();
  		}
  		
  		if ($(uiComponent.GadgetConnectionSetting).length > 0) {
  			$(uiComponent.GadgetConnectionSetting).css('display', 'none');
  		}
  	}
  };
  
  /**
   * Event and animation with jquery 
   */
  $(document).ready(function() {
  	
  	$(uiComponent.ModeIconList).click(function() {
  		$(uiComponent.UIIconListListContent).empty();
  		
  		UISearch.setNameToSearch(null);
  		
  		$(uiComponent.SearchTextBox).val('Quick Search');
  		$(uiComponent.SearchTextBox).css('color', '#D5D5D5');
  		
  		$(uiComponent.GadgetUIIconList).css('display', 'block');
  		$(uiComponent.GadgetUITextList).css('display', 'none');
  		$(uiComponent.UISearchContent).css('display', 'block');
  		
  		var settingStored = getSetting();
  		
  		debug.info('settingStored click icon list:');
  		debug.debug(settingStored);
  		
  		ActivityStream.initProfiles({offset: 0, limit: settingStored.itemPerViewNum, 
  																viewType: "ICON_LIST", updateTime: settingStored.updateTime,
  																orderBy: settingStored.orderBy});
  		
  		if ($(uiComponent.GadgetConnectionSetting).length > 0) {
  			$(uiComponent.GadgetConnectionSetting).empty();
  		}
  		gadgets.window.adjustHeight();
  	});
  	
  	$(uiComponent.ModeTextList).click(function() {
  		var settingStored = getSetting();
  		
  		debug.info('settingStored click text list:');
  		debug.debug(settingStored);
  		
  		$(uiComponent.UITextListListContent).empty();
  		
  		$(uiComponent.GadgetUIIconList).css('display', 'none');
  		$(uiComponent.GadgetUITextList).css('display', 'block');
  		$(uiComponent.UISearchContent).css('display', 'block');
  		
  		UISearch.setNameToSearch(null);
  		
  		$(uiComponent.SearchTextBox).val('Quick Search');
  		$(uiComponent.SearchTextBox).css('color', '#D5D5D5');
  		
  		ActivityStream.initProfiles({offset: 0, limit: settingStored.itemPerViewNum,
  																viewType: "TEXT_LIST", updateTime: settingStored.updateTime,
  																orderBy: settingStored.orderBy});
  		
  		if ($(uiComponent.GadgetConnectionSetting).length > 0) {
  			$(uiComponent.GadgetConnectionSetting).empty();
  		}
  		gadgets.window.adjustHeight();
  	});
  	
  	$(uiComponent.ModeSetting).click(function() {
  		$(uiComponent.GadgetUIIconList).css('display', 'none');
  		$(uiComponent.GadgetUITextList).css('display', 'none');
  		$(uiComponent.GadgetMemberMore).css('display', 'none');
  		$(uiComponent.UISearchContent).css('display', 'none');
  		$(uiComponent.GadgetConnectionSetting).css('display', 'block');
  		
  		UISetting.initSettingForm();
  		
  		gadgets.window.adjustHeight();
  	});
  });
  
  //exposes
  window_.eXo = window_.eXo || {};
  window_.eXo.social = window_.eXo.social || {};
  window_.eXo.social.MyConnectionsGadget = MyConnectionsGadget;
})(jQuery);
gadgets.util.registerOnLoadHandler(eXo.social.MyConnectionsGadget.main);