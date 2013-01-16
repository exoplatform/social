(function (eXo) {
  
  if(!String.prototype.trim) {
    String.prototype.trim = function () {
      return this.replace(/^\s+|\s+$/g,'');
    };
  }
  
  var portal = eXo.env.portal
  eXo.social = eXo.social || {};
  eXo.social.portal = {
    rest : (portal.rest) ? portal.rest : 'rest-socialdemo',
    portalName : (portal.portalName) ? portal.portalName : 'classic',
    context : (portal.context) ? portal.context : '/socialdemo',
    accessMode : (portal.accessMode) ? portal.accessMode : 'public',
    userName : (portal.userName) ? portal.userName : ''
  };
  eXo.social.I18n = eXo.social.I18n || {};
  eXo.social.I18n.mentions = eXo.social.I18n.mentions || {
    helpSearch: 'Type to start searching for users.',
    searching: 'Searching for ',
    foundNoMatch : 'Found no matching users for '
  };

  
  var SocialUtils = {
    /**
     * Constants
     */
    ADDED_MARGIN_BOTTOM : 10,
     
    /**
     * Browsers for checking
     */
    dataBrowser : [
    		{
    			string: navigator.userAgent,
    			subString: "Chrome",
    			identity: "Chrome"
    		},
    		{ string: navigator.userAgent,
    			subString: "OmniWeb",
    			versionSearch: "OmniWeb/",
    			identity: "OmniWeb"
    		},
    		{
    			string: navigator.vendor,
    			subString: "Apple",
    			identity: "Safari",
    			versionSearch: "Version"
    		},
    		{
    			prop: window.opera,
    			identity: "Opera"
    		},
    		{
    			string: navigator.vendor,
    			subString: "iCab",
    			identity: "iCab"
    		},
    		{
    			string: navigator.vendor,
    			subString: "KDE",
    			identity: "Konqueror"
    		},
    		{
    			string: navigator.userAgent,
    			subString: "Firefox",
    			identity: "Firefox"
    		},
    		{
    			string: navigator.vendor,
    			subString: "Camino",
    			identity: "Camino"
    		},
    		{		// for newer Netscapes (6+)
    			string: navigator.userAgent,
    			subString: "Netscape",
    			identity: "Netscape"
    		},
    		{
    			string: navigator.userAgent,
    			subString: "MSIE",
    			identity: "Explorer",
    			versionSearch: "MSIE"
    		},
    		{
    			string: navigator.userAgent,
    			subString: "Gecko",
    			identity: "Mozilla",
    			versionSearch: "rv"
    		},
    		{ 		// for older Netscapes (4-)
    			string: navigator.userAgent,
    			subString: "Mozilla",
    			identity: "Netscape",
    			versionSearch: "Mozilla"
    		}
    ],
    
    /**
     * Adjust height belong to browser
     */
    adjustHeight : function(contentContainer) {
      var browser = this.getCurrentBrowser();
      
      if (browser != null) {
        if ((browser == "Safari")	|| (browser == "Chrome")) {
        	gadgets.window.adjustHeight(contentContainer.offsetHeight + this.ADDED_MARGIN_BOTTOM);
        } else {
        	gadgets.window.adjustHeight();
        }
      } else {
      	gadgets.window.adjustHeight();
      }
    },
    
    /**
     * Get current Browser
     */
    getCurrentBrowser : function() {
    	function searchString(data) {
    		for (var i=0;i<data.length;i++)	{
    			var dataString = data[i].string;
    			var dataProp = data[i].prop;
    			this.versionSearchString = data[i].versionSearch || data[i].identity;
    			if (dataString) {
    				if (dataString.indexOf(data[i].subString) != -1)
    					return data[i].identity;
    			}
    			else if (dataProp)
    				return data[i].identity;
    		}
    	}
    	
    	var browser = searchString(this.dataBrowser) || null; 
    	
    	return browser;
    }
  };

  eXo.social.SocialUtils = SocialUtils;
  return SocialUtils;

})(window.eXo);