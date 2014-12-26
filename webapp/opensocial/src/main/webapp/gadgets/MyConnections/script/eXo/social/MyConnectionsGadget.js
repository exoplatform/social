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
  	var settingStored = Util.getSetting();
  	
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
  		
  		$(uiComponent.GadgetUIIconList).css('display', 'block');
  		$(uiComponent.GadgetUITextList).css('display', 'none');
  		$(uiComponent.UISearchContent).css('display', 'block');
  		
  		var settingStored = Util.getSetting();
  		
  		ActivityStream.initProfiles({offset: 0, limit: settingStored.itemPerViewNum, 
  																viewType: "ICON_LIST", updateTime: settingStored.updateTime,
  																orderBy: settingStored.orderBy});
  		
  		if ($(uiComponent.GadgetConnectionSetting).length > 0) {
  			$(uiComponent.GadgetConnectionSetting).empty();
  		}
  		gadgets.window.adjustHeight();
  	});
  	
  	$(uiComponent.ModeTextList).click(function() {
  		var settingStored = Util.getSetting();
  		
  		$(uiComponent.UITextListListContent).empty();
  		
  		$(uiComponent.GadgetUIIconList).css('display', 'none');
  		$(uiComponent.GadgetUITextList).css('display', 'block');
  		$(uiComponent.UISearchContent).css('display', 'block');
  		
  		UISearch.setNameToSearch(null);
  		
  		$(uiComponent.SearchTextBox).val('Quick Search');
  		
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