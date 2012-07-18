/**
 * A class to manage vertical tabs
*/

if(!eXo.social) eXo.social = {};
if(!eXo.social.webui) eXo.social.webui = {};

function UIVerticalTabs() {
  this.backupNavigationTabStyle;
  this.backupItemStyle;
};

UIVerticalTabs.prototype.init = function() {
};


/**
 * Calls changeTabForUITabPane to display tab content
 */
UIVerticalTabs.prototype.displayTabContent = function(clickedEle) {
	this.changeTabForUITabPane(clickedEle, null, null) ;
};
/**
 * Gets the tab element and the tab content associated and displays them
 *  . changes the style of the tab
 *  . displays the tab content of the selected tab (display: block)
 * if tabId are provided, can get the tab content by Ajax
 */
UIVerticalTabs.prototype.changeTabForUITabPane = function(clickedEle, tabId, url) {
  var uiSelectTab = gj(clickedEle).closest('.UIVerticalTab');
  var uiVerticalTabs = gj(clickedEle).closest('.UIVerticalTabs');
  var uiTabs = gj(uiVerticalTabs).find('div.UIVerticalTab');
  var parentdVerticalTab = uiVerticalTabs.parentNode ;
  var contentTabContainer = (gj(parentdVerticalTab).find('div.UIVerticalTabContentContainer:first-child'))[0]; 
  var uiTabContents = gj(contentTabContainer).find('div.UIVerticalTabContent');
  var form = gj(contentTabContainer).children("form");
 	if(form.length > 0) {
 	  var tmp = gj(form[0]).find('div.UIVerticalTabContent');
  	  for(var i = 0; i < tmp.length; i++) {
  		uiTabContents.push(tmp[i]) ;
  	  }
 	}
  var index = 0 ;
  for(var i = 0; i < uiTabs.length; i++) {
    var styleTabDiv = (gj(uiTabs[i]).children("div"))[0] ;
    if(styleTabDiv.className == "DisabledTab") continue ;
    if(uiSelectTab == uiTabs[i]) {
      styleTabDiv.className = "SelectedTab" ;
      index = i ;
	  continue ;
    }
    styleTabDiv.className = "NormalTab" ;
    uiTabContents[i].style.display = "none" ;
  }
  uiTabContents[index].style.display = "block" ;
	if (eXo.ecm.UIJCRExplorer) {
		try {
				eXo.ecm.UIJCRExplorer.initViewNodeScroll();
		} catch(e) {void(0);}
	}
};

eXo.social.webui.UIVerticalTabs = new UIVerticalTabs();
