/**
 * UIComposer.js
 * 
 * Requires: eXo.social.Util
 *
 */


(function () {
	var Util = eXo.social.Util;
	//configuration params
	var composer,
		composerId,
		defaultInput,
		focusColor,
		blurColor,
		minHeight,
		focusHeight,
		maxHeight,
		focusCallback,
		blurCallback,
		keypressCallback,
		postMessageCallback;
	
	function UIComposer(params) {
		this.configure(params);
		this.init();
	}
	
	UIComposer.prototype.configure = function(params) {
		composerId = params.composerId || 'composerInput';
		defaultInput = params.defaultInput || "";
		focusColor = params.focusColor || '#000000';
		blurColor = params.blurColor || '#777777';
		minHeight = params.minHeight || '20px';
		focusHeight = params.focusHeight || '35px';
		maxHeight = params.maxHeight || '50px';
		focusCallback = params.focusCallback;
		blurCallback = params.blurCallback;
		keypressCallback = params.keypressCallback;
		postMessageCallback = params.postMessageCallback;
	}
	
	
	UIComposer.prototype.init = function() {
		composer = Util.getElementById(composerId);
		if (!composer) {
			alert('error: can not find composer!');
		}
		
		composer.value = defaultInput;
		composer.style.height = minHeight;
		composer.style.color = blurColor;
		Util.addEventListener(composer, 'focus', function() {
			if (composer.value === defaultInput) {
				composer.value = '';
				composer.style.height = maxHeight;
				composer.style.color = focusColor;
			}
			if (focusCallback) {
				focusCallback();
			}
		}, false);
		
		Util.addEventListener(composer, 'blur', function() {
			if (composer.value === '') {
				composer.value = defaultInput;
				composer.style.height = minHeight;
				composer.style.color = blurColor;
			}
			if (blurCallback) {
				blurCallback();
			}
		}, false);
		
//		Util.addEventListener(composer, 'keypress', function() {
//			if (keypressCallback) {
//				keypressCallback();
//			}
//		}, false)
	}
	
	UIComposer.prototype.getValue = function() {
		return composer.value;
	}
	
	//expose
	window.eXo = window.eXo || {};
	window.eXo.social = window.eXo.social || {};
	window.eXo.social.webui = window.eXo.social.webui || {};
	window.eXo.social.webui.UIComposer = UIComposer;
})();