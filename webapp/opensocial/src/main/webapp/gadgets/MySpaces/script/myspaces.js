  var eXo = eXo || {};
  eXo.social = eXo.social || {};

	function MySpaces() {
	  this.viewer = null;
	  this.context = null;
	  this.moreSpaces = null;
	}
	
	MySpaces.prototype.init = function() {
	  var req = opensocial.newDataRequest();
	  var opts = {};
	  
	  opts[opensocial.DataRequest.PeopleRequestFields.PROFILE_DETAILS] = [
	      opensocial.Person.Field.PROFILE_URL,
	      "portalName",
	      "restContext",
	      "host"];
	
	  req.add(req.newFetchPersonRequest("VIEWER", opts), 'viewer');
	  req.send(onLoad);
	
	  function onLoad(data) {
	    if (!data.hadError()) {
	      this.viewer = data.get('viewer').getData();
	      var Locale = eXo.social.Locale;
	      var profile_url =  this.viewer.getField(opensocial.Person.Field.PROFILE_URL);
	      var userId = profile_url.substr(profile_url.lastIndexOf('/') + 1);
	      var hostName = this.viewer.getField('hostName');
	      var portalName = this.viewer.getField('portalName');
	      var restContext = this.viewer.getField('restContextName');
	      var address = window.top.location.href;
	      var moreSpaceEl = _gel("more_spaces");
	      var titleContent = '';
	      var baseContext = hostName + "/" + portalName + "/";
        var extensionContext = address.replace(baseContext, "");
        var extensionParts = extensionContext.split("/");
        this.context = baseContext + extensionParts[0] + "/" + extensionParts[1];
	      
	      this.moreSpaces = this.context + '/spaces';
	      
	      titleContent += '<div class="TitGad ClearFix">';
	      titleContent += '<a href="' + this.moreSpaces + '" target="_parent" class="IconDropDown">' + Locale.getMsg('more_link_label') + '</a>'
	      titleContent += '<div class="ContTit">' + Locale.getMsg('my_spaces') + '</div>';
	      titleContent += '</div>';
	      moreSpaceEl.innerHTML = titleContent;
	      var siteUrl = hostName + "/" + restContext + "/" + portalName + "/social/spaces/mySpaces/show.json";
	      mySpaces.makeRequest(siteUrl, mySpaces.displayValue);
	    }
	  }
	}
	
	MySpaces.prototype.makeRequest = function(url, callback) {
	  var params = {};
	  params[gadgets.io.RequestParameters.AUTHORIZATION] = gadgets.io.AuthorizationType.SIGNED;
	  params[gadgets.io.RequestParameters.METHOD] = gadgets.io.MethodType.GET;
	  params[gadgets.io.RequestParameters.CONTENT_TYPE] = gadgets.io.ContentType.JSON;
	  mySpaces.makeCachedRequest(url, callback, params, 0);
	}
	
	MySpaces.prototype.makeCachedRequest = function(url, callback, params, refreshInterval) {
		var ts = new Date().getTime();
    var sep = "?";
    if (refreshInterval && refreshInterval > 0) {
      ts = Math.floor(ts / (refreshInterval * 1000));
    }
    if (url.indexOf("?") > -1) {
     sep = "&";
    }

    url = [ url, sep, "nocache=", ts ].join("");
	  gadgets.io.makeRequest(url, callback, params);
	}
	
	MySpaces.prototype.displayValue = function(resp) {
	  var mySpacesEl = _gel("myspaces_id");
	  mySpacesEl.className = "GadCont MySpace";
	  while (mySpacesEl.hasChildNodes()) {
	    mySpacesEl.removeChild(mySpacesEl.firstChild);
	  }
	
	  if (!resp) {
	    //alert('response is invalid');
	  } else {
	    var spaceData = resp.data.spaces;
	    if ((spaceData == null) || (spaceData.length == 0)) {
	      var emptyItem = document.createElement('div');
	      emptyItem.innerHTML = eXo.social.Locale.getMsg('has_no_space');
	      mySpacesEl.appendChild(emptyItem);
	      gadgets.window.adjustHeight();
	      return;
	    }
	
	    for (var i = 0; i < spaceData.length; i++) {
	      var space = spaceData[i];
	      var spaceItem = document.createElement('div');
	      var spaceDetail = this.context + "/" + space.url;
	      spaceItem.innerHTML = '<a href="' + spaceDetail + '" target="_parent" class="IconLink">' + space.name + '</a>';
	      mySpacesEl.appendChild(spaceItem);
	    }
	    
   	  gadgets.window.adjustHeight();
	  }
	}
	
	var mySpaces = new MySpaces();
	
	gadgets.util.registerOnLoadHandler(mySpaces.init);