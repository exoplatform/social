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
	      var profile_url =  this.viewer.getField(opensocial.Person.Field.PROFILE_URL);
	      var userId = profile_url.substr(profile_url.lastIndexOf('/') + 1);
	      var hostName = this.viewer.getField('hostName');
	      var portalName = this.viewer.getField('portalName');
	      var restContext = this.viewer.getField('restContextName');
	      var address = window.top.location.href;
	      var moreSpaceEl = _gel("more_spaces");
	      var titleContent = '';
	      
	      if (address.indexOf("classic")) {
		      this.context = this.viewer.getField('hostName') + "/" + portalName + "/private/classic/";
	      } else {
	      	this.context = this.viewer.getField('hostName') + "/" + portalName + "/private/office/";
	      }
	      
	      this.moreSpaces = this.context + 'spaces';
	      
	      titleContent += '<div class="TitGad ClearFix">';
	      titleContent += '<a href="' + this.moreSpaces + '" target="_parent" class="IconDropDown">' + 'more</a>'
	      titleContent += '<div class="ContTit">My Spaces</div>';
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
	      var spaceDetail = this.context + space.url;
	      spaceItem.innerHTML = '<a href="' + spaceDetail + '" target="_parent" class="IconLink">' + space.name + '</a>';
	      mySpacesEl.appendChild(spaceItem);
	    }
	    
   	  gadgets.window.adjustHeight();
	  }
	}
	
	var mySpaces = new MySpaces();
	
	gadgets.util.registerOnLoadHandler(mySpaces.init);