/**
 * UISpaceMenuPortlet class
 * - Show/ hide UISpaceMenuPortlet
 */
function UISpaceMenuPortlet() {
	
}

UISpaceMenuPortlet.init = function() {
	var Util = eXo.social.Util,
		shown = true,
		uiSpaceMenuPortlet = document.getElementById('UISpaceMenuPortlet'),
		uiContainerMenu = document.getElementById('UIContainer-Menu'),
		menuTdContainer = uiContainerMenu.parentNode,
		tdToggleElement = document.createElement('td');
		
	tdToggleElement.setAttribute('id', 'ToggleElement');
	tdToggleElement.setAttribute('class', 'ToggleElement');
	tdToggleElement.setAttribute('className', 'ToggleElement');
	
	eXo.social.Util.insertAfter(tdToggleElement, menuTdContainer);
	
	//set event handler
	Util.addEventListener(tdToggleElement, 'click', function() {
		if (shown) { //hide
			uiSpaceMenuPortlet.style.display='none';
			menuTdContainer.style.width='0px';
			shown = false;
		} else { //show
			uiSpaceMenuPortlet.style.display='block';
			menuTdContainer.style.width='162px';
			shown = true;
		}
	});
	
}

eXo.social = eXo.social || {};
eXo.social.webui = eXo.social.webui || {};
eXo.social.webui.UISpaceMenuPortlet = UISpaceMenuPortlet;