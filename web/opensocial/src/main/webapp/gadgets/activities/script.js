var eXo = eXo || {};
eXo.social = eXo.social || {};

function StatusUpdate() {
	this.more = 0;
	this.MAX = 3;
	this.viewer = null;
	this.owner = null;
	this.ownerContacts = null;
	this.activities = null;
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
	  eXo.social.statusUpdate.MAX = 3*(eXo.social.statusUpdate.more + 1);
	  
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
  eXo.social.statusUpdate.activities = eXo.social.statusUpdate.activities.sort(eXo.social.statusUpdate.sortUpdates);
  var activitiesLength = eXo.social.statusUpdate.activities.length;
  
  if (activitiesLength < totalAct) {
		document.getElementById('more').style.display = 'block';
  } else {
		document.getElementById('more').style.display = 'none';
  }
  for (var i = 0; i < activitiesLength; i++) {
    html += '<div class="ActivitiesContent">';
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
		html += '<div class="NewsDate">';
		if (url) html += '<a href="' + url + '" target="_blank">link</a>' + ' | ';
		html += 'posted on ' + ("" + (new Date(eXo.social.statusUpdate.activities[i].getField('postedTime')))).substring(0, 24) + '</div>';
		html += '</div>';
		if(i < activitiesLength -1 )html += '<hr/>';
  }
  
  document.getElementById('ActivitiesContainer').innerHTML = html;
  document.getElementById('more').innerHTML = '<a style="text-decoration:none;" href="#" onclick="eXo.social.statusUpdate.displayMore();">more</a>';
  	
  gadgets.window.adjustHeight();
}

StatusUpdate.prototype.displayMore = function() {
		eXo.social.statusUpdate.more += 1;
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

StatusUpdate.prototype.sortUpdates = function(a, b) {
  return b.getField('postedTime') - a.getField('postedTime');
}

StatusUpdate.prototype.timeToPrettyString = function(B) {
    if (isNaN(B)) {
        return "an indeterminate amount of time ago"
    }
    var date = new Date();
    date.setTime(B);
    return date;
}

StatusUpdate.prototype.postNewActivity = function(){
  var activityElement = document.getElementById('newActivity');
	// Check for blank spaces, if yes return false without any update
	var reWhiteSpace = new RegExp(/^\s+$/);
	if (activityElement.value == "" || reWhiteSpace.test(activityElement.value)) {
		return false;
	}
  var activity = opensocial.newActivity({ 'title' : activityElement.value,
    'body' : activityElement.value});

  activityElement.value = '';
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