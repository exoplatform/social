var social = {
	webui : { }
}

function UIManageSpace() {};

UIManageSpace.prototype.requestJoin = function(selectedElement) {
	alert("request to join");
}

social.webui.UIManageSpace = new UIManageSpace();