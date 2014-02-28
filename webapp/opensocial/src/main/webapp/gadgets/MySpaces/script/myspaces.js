  var eXo = eXo || {};
  eXo.social = eXo.social || {};
  
  var SocialGadgetsUtil = eXo.social.SocialGadgetsUtil;
  
  //When run in socialdemo, need to change this config to /classsic/all-spaces
  var domain = top.location.protocol + "//" + top.location.host; 
  function getAllSpacesURL() {
   var portalEnv = (eXo.env) ? eXo.env.portal : parent.eXo.env.portal;
   return (domain + portalEnv.context + "/" + portalEnv.portalName) + ((portalEnv.context.indexOf('social') > 0) ? "/all-spaces" : "/spaces");
  } 

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
	      var moreSpaceEl = _gel("more_spaces");
	      var titleContent = '';
	      
	      this.context = hostName + "/" + portalName;
	      
	      this.moreSpaces = getAllSpacesURL();
	      
	      titleContent += '<h6 class="title left">';
	      titleContent += Locale.getMsg('my_spaces');
	      titleContent += '<a id="MoreAllSpaces" href="' + this.moreSpaces + '" target="_parent" class="btn btn-primary btn-mini pull-right">' + Locale.getMsg('more_link_label') + '</a>'
	      titleContent += '</h6>';
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
	  mySpacesEl.className = "uiContentBox";
	  while (mySpacesEl.hasChildNodes()) {
	    mySpacesEl.removeChild(mySpacesEl.firstChild);
	  }
	
	  if (!resp) {
	    //alert('response is invalid');
	  } else {
	  	this.moreSpaces = getAllSpacesURL(); 
	  	document.getElementById("MoreAllSpaces").href = this.moreSpaces;
	  	
	    var spaceData = resp.data.spaces;
	    if ((spaceData == null) || (spaceData.length == 0)) {
	      var emptyItem = document.createElement('div');
	      emptyItem.className = 'light_message';
	      emptyItem.innerHTML = eXo.social.Locale.getMsg('has_no_space');
	      mySpacesEl.appendChild(emptyItem);
	      SocialGadgetsUtil.adjustHeight(mySpacesEl);
	      return;
	    }
	
	    for (var i = 0; i < spaceData.length; i++) {
	      var space = spaceData[i];
	      var spaceItem = document.createElement('div');
	      var spaceDetail = this.context + space.spaceUrl;
	      spaceItem.innerHTML = '<a href="' + spaceDetail + '" target="_parent" class="IconLink">' + space.name + '</a>';
	      mySpacesEl.appendChild(spaceItem);
	    }
	    
	    SocialGadgetsUtil.adjustHeight(mySpacesEl);
	  }
	}
	
	var mySpaces = new MySpaces();
	
	gadgets.util.registerOnLoadHandler(mySpaces.init);
