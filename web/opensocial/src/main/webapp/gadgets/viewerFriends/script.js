/**
	Copyright (C) 2003-2007 eXo Platform SAS. This program is free
	software; you can redistribute it and/or modify it under the terms of
	the GNU Affero General Public License as published by the Free
	Software Foundation; either version 3 of the License, or (at your
	option) any later version. This program is distributed in the hope
	that it will be useful, but WITHOUT ANY WARRANTY; without even the
	implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
	PURPOSE. See the GNU General Public License for more details. You
	should have received a copy of the GNU General Public License along
	with this program; if not, see<http://www.gnu.org/licenses/>.
*/
/**
	* Created by The eXo Platform SARL 
	* Author : hoat_le 
	*          hoatlevan@gmail.com 
	* Jun 17, 2009 
	*/

var eXo = eXo || {};
eXo.social = eXo.social || {}; 

function ViewerFriend() {
	this.startIndex = 0;
	this.itemsPerPage = 5;
	this.viewer = null;
	this.viewerFriends = null;
	this.prefs = null;
	this.totalItems = 0;
	this.displayedItems = 0;
	this.totalPages = 1;
	this.currentPage = 1;
	
}

ViewerFriend.init = function() {
    eXo.social.viewerFriend = new ViewerFriend();
    eXo.social.viewerFriend.start();
}

//Constants
ViewerFriend.FIRST = "first";
ViewerFriend.PREVIOUS = "previous";
ViewerFriend.NEXT = "next";
ViewerFriend.LAST = "last";

ViewerFriend.prototype.start = function() {
	this.setPrefs();
	this.loadFriends();
	this.registerPagingAction();
}

ViewerFriend.prototype.setPrefs = function() {
	this.prefs = new gadgets.Prefs();
	this.itemsPerPage = this.prefs.getInt("itemsPerPage");
}

ViewerFriend.prototype.loadFriends = function() {
	var req = opensocial.newDataRequest();
	var opts = {};
	opts[opensocial.DataRequest.PeopleRequestFields.FIRST] = this.startIndex;
	opts[opensocial.DataRequest.PeopleRequestFields.MAX] = this.itemsPerPage;
	opts[opensocial.DataRequest.PeopleRequestFields.PROFILE_DETAILS] = [
			opensocial.Person.Field.AGE, opensocial.Person.Field.NAME,
			opensocial.Person.Field.GENDER,
			opensocial.Person.Field.PROFILE_URL,
			opensocial.Person.Field.THUMBNAIL_URL,
			opensocial.Person.Field.STATUS ];

	req.add(req.newFetchPersonRequest("VIEWER", opts), 'viewer');
	req.add(req.newFetchPeopleRequest("VIEWER_FRIENDS", opts), 'viewerFriends');
	req.send(onLoadFriends);

	function onLoadFriends(data) {
		if (!data.hadError()) {
			eXo.social.viewerFriend.viewer = data.get('viewer').getData();
			eXo.social.viewerFriend.viewerFriends = data.get('viewerFriends').getData();
			eXo.social.viewerFriend.totalItems = eXo.social.viewerFriend.viewerFriends.getTotalSize();
			eXo.social.viewerFriend.totalPages = Math.ceil(eXo.social.viewerFriend.totalItems/eXo.social.viewerFriend.itemsPerPage);
			eXo.social.viewerFriend.display();
		}
	}
}

ViewerFriend.prototype.display = function() {
	var viewerDisplay = "";
	var friendsDisplay = [];
	if (this.viewer != null) {
		viewerDisplay = this.viewer.getDisplayName();
	} else {
	   alert("ERROR!!!")
	}
	
	var viewerEl = document.getElementById("viewer");
  var friendsEl = document.getElementById("friends");
	if (this.totalItems > 0) {
		this.viewerFriends
				.each( function(person) {
					if (person.getId()) {
						friendsDisplay.push('<li>' + person.getDisplayName()
								+ "</li>");
					}
				});
		friendsEl.innerHTML = "<ul>" + friendsDisplay.join() + "</ul>";
	} else {
		friendDisplay = viewerDisplay + " has no friends yet.";
		friendsEl.innerHTML = friendDisplay;
	}
	
	viewerEl.innerHTML = viewerDisplay;
}

ViewerFriend.prototype.registerPagingAction = function() {
    var allPages = [];
    for (i = 1; i <= this.totalPages; i++) {
        //TODO: find better solution instead of vf. Should create DOM element on the fly
        var str = "<a href='#' onclick='eXo.social.viewerFriend.toPage("+i+")'>" + i + " " + "</a>";
        allPages.push(str);
    }
    var firstEl = getEl("first");
    var lastEl  = getEl("last");
    var nextEl = getEl("next");
    var previousEl = getEl("previous");
    var allPagesEl = getEl("pages");
    allPagesEl.innerHTML = allPages.join(); 
    
    binding(firstEl, "onclick", this.firstPage);
    binding(lastEl, "onclick", this.lastPage);
    binding(nextEl, "onclick", this.nextPage);
    binding(previousEl, "onclick", this.previousPage);
    
    
    function binding(el, action, func) {
        el.action = func;
    }
    
    
    function getEl(elId) {
        var el = document.getElementById(elId);
        if (el) {
            return el; 
        } else {
            alert("element: " + elId + " not found!");
        }
    }
}

//ViewerFriend.prototype.pageActionListener(elId) {
//    if (elId === ViewerFriend.FIRST) {
//    
//    } else if (elId === ViewerFriend.PREVIOUS) {
//    
//    } else if (elId == ViewerFriend.NEXT) {
//    
//    } else if (elId == ViewerFriend.LAST) {
//    
//    }
//}

ViewerFriend.prototype.firstPage = function() {
    if (eXo.social.viewerFriend.currentPage != 1) {
		  eXo.social.viewerFriend.curentPage = 1;
		  eXo.social.viewerFriend.start();
	  }
}

ViewerFriend.prototype.lastPage = function() {
    if (eXo.social.viewerFriend.currentPage != eXo.social.viewerFriend.totalPages) {
        eXo.social.viewerFriend.currentPage = eXo.social.viewerFriend.totalPages;
        eXo.social.viewerFriend.start();
    }
    
}

ViewerFriend.prototype.nextPage = function() {
    if (eXo.social.viewerFriend.currentPage < eXo.social.viewerFriend.totalPages) {
        eXo.social.viewerFriend.currentPage += 1;
        eXo.social.viewerFriend.start();
    }
}

ViewerFriend.prototype.previousPage = function() {
    if (eXo.social.viewerFriend.currentPage > 1) {
        eXo.social.viewerFriend.currentPage -= 1;
        eXo.social.viewerFriend.start();
    }
}

ViewerFriend.prototype.toPage = function(pageNum) {
    if (pageNum > 0 && pageNum < eXo.social.viewerFriend.totalPages) {
        eXo.social.viewerFriend.currentPage = pageNum;
        eXo.social.viewerFriend.start();
    }
}