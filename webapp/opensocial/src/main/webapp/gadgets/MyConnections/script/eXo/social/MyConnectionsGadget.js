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
   * Main point of gadget
   */
  MyConnectionsGadget.main = function() {
  	var settings = prefs.getArray("SETTINGS");
  	var viewType = "ICON_LIST",
  			updateTime,
  			orderBy,
  			itemPerViewNum = 10;
  	
  	debug.info('settings');
  	debug.debug(settings);
  	
  	debug.info('viewType:');
  	debug.debug(settings[0]);
  	
  	if (settings !== null) {
  		if (settings[0] !== undefined)	viewType = settings[0];
  		if (settings[1] !== undefined) updateTime = settings[1];
  		if (settings[2] !== undefined) orderBy = settings[2];
  		if (settings[3] !== undefined) itemPerViewNum = settings[3];
  	}
    
  	ActivityStream.initProfiles({offset: 0, limit: itemPerViewNum, viewType: viewType});
  	
  	if (viewType === 'TEXT_LIST') {
  		
  		if ($('.ListIcon').length > 0) {
  			$(uiComponent.ModeTextList).removeClass('ListIcon');
  			$(uiComponent.ModeTextList).addClass('ListSelected');
  		}
  		
  		if ($(uiComponent.UITextListListContent).length > 0) {
  			$(uiComponent.UITextListListContent).empty();
  		}
  	}
  	
  	if (viewType === 'ICON_LIST') {
  		if ($('.NumberListIcon').length > 0) {
  			$(uiComponent.ModeIconList).removeClass('NumberListIcon');
  			$(uiComponent.ModeIconList).addClass('NumberListSelected');
  		}
  		
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
  		ActivityStream.initProfiles({offset: 0, limit: 10, viewType: "ICON_LIST"});
  		if ($('.NumberListIcon').length > 0) {
  			$(uiComponent.ModeIconList).removeClass('NumberListIcon');
  			$(uiComponent.ModeIconList).addClass('NumberListSelected');
  		}
  		
  		if ($('.ListSelected').length > 0) {
  			$(uiComponent.ModeTextList).removeClass('ListSelected');
  			$(uiComponent.ModeTextList).addClass('ListIcon');
  		}
  		
  		if ($('.SettingSelected').length > 0) {
  			$(uiComponent.ModeSetting).removeClass('SettingSelected');
  			$(uiComponent.ModeSetting).addClass('SettingIcon');
  		}
  		
  		if ($(uiComponent.GadgetConnectionSetting).length > 0) {
  			$(uiComponent.GadgetConnectionSetting).empty();
  		}
  		gadgets.window.adjustHeight();
  	});
  	
  	$(uiComponent.ModeTextList).click(function() {
  		$(uiComponent.UITextListListContent).empty();
  		
  		$(uiComponent.GadgetUIIconList).css('display', 'none');
  		$(uiComponent.GadgetUITextList).css('display', 'block');
  		$(uiComponent.UISearchContent).css('display', 'block');
  		
  		UISearch.setNameToSearch(null);
  		
  		$(uiComponent.SearchTextBox).val('Quick Search');
  		$(uiComponent.SearchTextBox).css('color', '#D5D5D5');
  		
  		ActivityStream.initProfiles({offset: 0, limit: 10, viewType: "TEXT_LIST"});
  		
  		if ($('.ListIcon').length > 0) {
  			$(uiComponent.ModeTextList).removeClass('ListIcon');
  			$(uiComponent.ModeTextList).addClass('ListSelected');
  		}
  		
  		if ($('.ListSelected').length > 0) {
  			$(uiComponent.ModeIconList).removeClass('NumberListSelected');
  			$(uiComponent.ModeIconList).addClass('NumberListIcon');
  		}
  		
  		if ($('.SettingSelected').length > 0) {
  			$(uiComponent.ModeSetting).removeClass('SettingSelected');
  			$(uiComponent.ModeSetting).addClass('SettingIcon');
  		}
  		
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
  		
  		UISetting.initSettingForm();
  		$(uiComponent.GadgetConnectionSetting).css('display', 'block');
  		
  		gadgets.window.adjustHeight();
  	});
  });
  
  //exposes
  window_.eXo = window_.eXo || {};
  window_.eXo.social = window_.eXo.social || {};
  window_.eXo.social.MyConnectionsGadget = MyConnectionsGadget;
})(jQuery);
gadgets.util.registerOnLoadHandler(eXo.social.MyConnectionsGadget.main);