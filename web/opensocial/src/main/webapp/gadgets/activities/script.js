var eXo = eXo || {};
eXo.social = eXo.social || {};

function StatusUpdate() {
	this.more = 0;
	this.MAX = 2;
	this.viewer = null;
	this.owner = null;
	this.ownerContacts = null;
	this.activities = null;
	this.active = false;
}


StatusUpdate.prototype.init = function() {
  eXo.social.statusUpdate = new StatusUpdate();
  eXo.social.statusUpdate.refresh();	
}

StatusUpdate.prototype.refresh = function() {
  // Create request for getting totalsize of owner and owner-friend activities.
  var reqTotalSize = opensocial.newDataRequest();
  
  //For reducing the returning array size, set 1 for MAX 
  var params = {};
  params[opensocial.DataRequest.PeopleRequestFields.FIRST] =  0;
	params[opensocial.DataRequest.PeopleRequestFields.MAX] = 1;
	
  reqTotalSize.add(reqTotalSize.newFetchActivitiesRequest('OWNER', params), 'ownerActivities');   
  reqTotalSize.add(reqTotalSize.newFetchActivitiesRequest('OWNER_FRIENDS', params), 'activities');
  reqTotalSize.send(process);
  
 	function process(data) {
 	  var totalOwnerActs = data.get('ownerActivities').getData()['activities'].getTotalSize();;
  	var totalOwnerFriendActs = data.get('activities').getData()['activities'].getTotalSize();;

	  var totalSize = totalOwnerActs;
	  eXo.social.statusUpdate.MAX = 2 * (eXo.social.statusUpdate.more + 1);
	  
	  if (totalOwnerActs > totalOwnerFriendActs)  totalSize = totalOwnerFriendActs;
	  if (eXo.social.statusUpdate.MAX > totalSize) {
	  	eXo.social.statusUpdate.MAX = 2*eXo.social.statusUpdate.MAX - totalSize;
	  }
	  
	  //Create request for getting owner and owner-friend activities.
	  var req = opensocial.newDataRequest();	
	
	  if (!eXo.social.statusUpdate.viewer) {
	    req.add(req.newFetchPersonRequest('VIEWER'), 'viewer');
	    req.add(req.newFetchPersonRequest('OWNER'), 'owner');
	
	    var opts = {};
	    opts[opensocial.DataRequest.PeopleRequestFields.FIRST] =  0;
	    opts[opensocial.DataRequest.PeopleRequestFields.MAX] = 40;
	    opts[opensocial.DataRequest.PeopleRequestFields.PROFILE_DETAILS] =
	                   [opensocial.Person.Field.NAME,
	                   opensocial.Person.Field.THUMBNAIL_URL];
	    req.add(req.newFetchPeopleRequest('OWNER_FRIENDS', opts), 'ownerContacts');
	  }
	
	  var opts_act = {};
	  opts_act[opensocial.DataRequest.ActivityRequestFields.FIRST] = 0;
	  opts_act[opensocial.DataRequest.ActivityRequestFields.MAX] = eXo.social.statusUpdate.MAX;
	  req.add(req.newFetchActivitiesRequest('OWNER', opts_act), 'ownerActivities');   
	  req.add(req.newFetchActivitiesRequest('OWNER_FRIENDS', opts_act), 'activities');
	  req.send(eXo.social.statusUpdate.handleActivities);
	}
}

StatusUpdate.prototype.handleActivities = function(dataResponse) {
  var currentView = gadgets.views.getCurrentView().getName();
  if (!eXo.social.statusUpdate.viewer) {
    eXo.social.statusUpdate.viewer = dataResponse.get('viewer').getData();
    eXo.social.statusUpdate.owner = dataResponse.get('owner').getData();
    eXo.social.statusUpdate.ownerContacts = dataResponse.get('ownerContacts').getData();
  }  
  
  eXo.social.statusUpdate.activities = dataResponse.get('ownerActivities').getData()['activities'].asArray();
  eXo.social.statusUpdate.activities = eXo.social.statusUpdate.activities.concat(dataResponse.get('activities').getData()['activities'].asArray());
  
  //Total activities
  var totalAct = dataResponse.get('ownerActivities').getData()['activities'].getTotalSize();
  totalAct+= dataResponse.get('activities').getData()['activities'].getTotalSize();
  gadgets.window.setTitle("Activities from " + eXo.social.statusUpdate.owner.getDisplayName() + "'s contact");

  var html = '';
  if (!eXo.social.statusUpdate.activities || eXo.social.statusUpdate.activities.length == 0) {
    document.getElementById('ActivitiesContainer').innerHTML = '<div class= "Empty">' + eXo.social.statusUpdate.owner.getDisplayName() + ' do not have any updates yet' + '</div>';
    return;
  }
  var activitiesLength = eXo.social.statusUpdate.activities.length;
  
  if (activitiesLength < totalAct) {
		document.getElementById('more').style.display = 'block';
  } else {
		document.getElementById('more').style.display = 'none';
  }
  for (var i = 0; i < activitiesLength; i++) {
    html += '<div class="ActivitiesContent">';
    var image = eXo.social.statusUpdate.getAvatar(eXo.social.statusUpdate.activities[i].getField('userId'));
    html += '<div class="MiniAvatarSpaceBG">';
    html += '<img src="' + image + '" width="65" height="70"/>'; 
    html += '</div>';
    html += '<div class="Content">';
    html += '<a href="#" class="TitleItem">' + eXo.social.statusUpdate.getName(eXo.social.statusUpdate.activities[i].getField('userId')) + '</a>';
    
    html += '<div>';
	var mediaItems = eXo.social.statusUpdate.activities[i].getField('mediaItems');
	if (mediaItems) {
      for (var j = 0; j < mediaItems.length; j++) {
        if (mediaItems[j].getField('type') == 'image') {
					html += '<a href="#" class="ImageItem">';
					html += '<img src="' + mediaItems[j].getField('url') + '" width="25" height="25"/>';
					html += '</a>';
      	}
      }
	 } else {
				html += '<a href="#" class="ImageItem">';
				html += '<img src="http://localhost:8080/social/gadgets/activities/Backgrouds/Gadget.gif" width="25" height="25"/>';
				html += '</a>';		
	 }
		var body = eXo.social.statusUpdate.activities[i].getField('body') || '';
		var url = eXo.social.statusUpdate.activities[i].getField('url');
		html += '<div class="Content">' + body + '</div>';
		html += '<div class="ClearLeft"><span></span></div>';
		html += '</div>';
		html += '<br>';
		if (currentView == "canvas") {
			var timeAgo = eXo.social.statusUpdate.timeDetermineAgo(new Date(eXo.social.statusUpdate.activities[i].getField('postedTime')));
			html += '<div id="NewsComment" class="NewsComment">';
			html += timeAgo;
			html += '<a class="Links" id="comment'+ i +'" href="#">' + ' Comment ' + '</a>';
			html += ' | ';
			html += '<a class="Links"  id="like'+ i +'" href="#">' + ' Like ' + '</a>';
			html += '</div>';
		} else {
			var timeAgo = eXo.social.statusUpdate.timeDetermineAgo(new Date(eXo.social.statusUpdate.activities[i].getField('postedTime')));
			html += '<div class="NewsDate">';
			if (url) html += '<a href="' + url + '" target="_blank">link</a>' + ' | ';
			html += timeAgo + '</div>';
		}
		
		html += '</div>';
		html += '<div style="clear: left;"><span></span></div>';
		
		html += '</div>';
		if(i < activitiesLength -1 )html += '<hr/>';
  }
  
  document.getElementById('ActivitiesContainer').innerHTML = html;
  document.getElementById('more').innerHTML = '<a style="text-decoration:none;" href="#" onclick="eXo.social.statusUpdate.displayMore();">more</a>';
  	
  gadgets.window.adjustHeight();
}

StatusUpdate.prototype.displayMore = function() {
	eXo.social.statusUpdate.more += 1;
	if(eXo.social.statusUpdate.more == 2) {
		eXo.social.statusUpdate.more -= 1;
		alert("Please go to canvas view to see more details!");
		return;
	}
	eXo.social.statusUpdate.refresh();
}

StatusUpdate.prototype.getName = function(id) {
  if (id == null)
    return "";
  if (id == eXo.social.statusUpdate.owner.getId())
    return eXo.social.statusUpdate.owner.getDisplayName();
  var person = eXo.social.statusUpdate.ownerContacts.getById(id);
  if (person == null)
    return "";
  return person.getDisplayName();
}

StatusUpdate.prototype.getAvatar = function(id) {
  if (id == null)
    return "";
  var person = null;
  if (id == eXo.social.statusUpdate.owner.getId())
	  person = eXo.social.statusUpdate.owner;
  else
	  person = eXo.social.statusUpdate.ownerContacts.getById(id);
  if (person == null)
    return "";
  var avatarUrl = person.getField(opensocial.Person.Field.THUMBNAIL_URL);
  if(avatarUrl == undefined) {
	  return "http://localhost:8080/social/gadgets/activities/Backgrouds/AvartarIcon.gif";
  }
  return person.getField('thumbnailUrl');
}

StatusUpdate.prototype.timeToPrettyString = function(B) {
    if (isNaN(B)) {
        return "an indeterminate amount of time ago"
    }
    var date = new Date();
    date.setTime(B);
    return date;
}

StatusUpdate.prototype.timeDetermineAgo = function(B) {
    if (isNaN(B)) {
        return "an indeterminate amount of time ago"
    }
    time = (new Date().getTime() - B) / 1000;
    
    if (time < 60) {
        return "less than a minute ago"
    } else {
        if (time < 120) {
            return "about a minute ago"
        } else {
            if (time < 3600) {
                var A = Math.round(time / 60);
                return "about " + A + " minutes ago"
            } else {
                if (time < 7200) {
                    return "about an hour ago"
                } else {
                    if (time < 86400) {
                        var A = Math.round(time / 3600);
                        return "about " + A + " hours ago"
                    } else {
                    	return '' + eXo.social.statusUpdate.getPostedDate(B);
                    }
                }
            }
        }
    }
}

StatusUpdate.prototype.getPostedDate = function(D) {
	var d_names = new Array("Sunday", "Monday", "Tuesday",
	"Wednesday", "Thursday", "Friday", "Saturday");

	var m_names = new Array("January", "February", "March", 
	"April", "May", "June", "July", "August", "September", 
	"October", "November", "December");

	var curr_month = D.getMonth();
	var curr_year = D.getFullYear();
	var curr_day = D.getDay();
	var curr_date = D.getDate();
	var a_p = "";
	var curr_hour = D.getHours();
	
	if (curr_hour < 12) {
	   a_p = "AM";
	}
	else {
	   a_p = "PM";
	}
	if (curr_hour == 0) {
	   curr_hour = 12;
	}
	if (curr_hour > 12) {
	   curr_hour = curr_hour - 12;
	}

	var curr_min = D.getMinutes();

	curr_min = curr_min + "";

	if (curr_min.length == 1) {
	   curr_min = "0" + curr_min;
	}

	var time = (curr_hour + " : " + curr_min + " " + a_p);
	var date = (d_names[curr_day] + " " + curr_date + " " + m_names[curr_month] + " " + curr_year);
	
	return (date + " at " + time);
}

StatusUpdate.prototype.postNewActivity = function() {
  var currentView = gadgets.views.getCurrentView().getName();
  var activityElement = document.getElementById('newActivity');
  
  if ((currentView == 'canvas') && (this.active == false)) return;
  
  // Check for blank spaces, if yes return false without any update
  var reWhiteSpace = new RegExp(/^\s+$/);
  var text = activityElement.innerHTML;
  var content = text.replace(/<p>/gi, "<br>").replace(/<\/\p>/gi, "<br>");
  var activityContent = content.replace(/<br>/gi, " ");
  if (activityContent  == "" || reWhiteSpace.test(activityElement.innerHTML)) {
	return false;	
  }
  if ((activityContent == "What're you doing?") && (activityElement.style.minHeight == "12px")) return;
  var activity = opensocial.newActivity({ 'title' : activityContent, 'body' : activityContent});
  
  if (currentView == 'home') {
	activityElement.style.color="#777777";
	activityElement.style.minHeight="12px";
	activityElement.innerHTML = "What're you doing?";
  } else if (currentView == 'canvas') {
	this.active = false;
	activityElement.style.minHeight="20px";
	activityElement.style.color="#777777";
	activityElement.innerHTML = "What're you doing?";
  }
  opensocial.requestCreateActivity(activity, "HIGH", statusUpdates.refresh);
}

StatusUpdate.prototype.hideShowDiv = function(divToShow, divToHide) {
  document.getElementById(divToShow).style.display = 'block';
  document.getElementById(divToHide).style.display = 'none';
}

StatusUpdate.prototype.streamSubmit = function(e) { // Handle ENTER keypress
	var keyNum;
	var ENTER_KEY_NUM = 13;
	if(window.event) {// IE
  	keyNum = e.keyCode;
  }
	else if(e.which) { // Netscape/Firefox/Opera
  	keyNum = e.which;
  }
  if (ENTER_KEY_NUM == keyNum) {
  	eXo.social.statusUpdate.postNewActivity();
  	return false;
  }
}
