var eXo = eXo || {};
eXo.social = eXo.social || {};

function RssFetch() {
  this.feed = {};
  this.startIndex = 0;
  this.endIndex = 0;
  this.totalItems = 0;
  this.totalPages = 1;
  this.currentPage = 1;
  this.itemList = null;
}

RssFetch.prototype.toggleDescription = function(elmnt_id) {
  	var blockForAdjustHeight = _gel("rssFeed");
    if (_gel('more_'+elmnt_id).style.display == 'none') {
        _gel('more_'+elmnt_id).style.display = '';
        _gel('item_'+elmnt_id).className = 'item descriptionHighlight';
    } else {
        _gel('more_'+elmnt_id).style.display = 'none';
        _gel('item_'+elmnt_id).className = 'item';
    }
    this.adjustHeight();
}

RssFetch.prototype.shared = function() {
  var miniMsg = new gadgets.MiniMessage();
  miniMsg.createTimerMessage('Successfully shared.', 7);
}

RssFetch.prototype.share = function(i, link) {
  var nodeList = eXo.social.thisRssFetch.itemList.item(i).childNodes;
  for (var j = 0; j < nodeList.length ; j++) {
    var node = nodeList.item(j);
    if (node.nodeName == "title") {
      var title = node.firstChild.nodeValue;
    } else if (node.nodeName == "link") {
      var link = node.firstChild.nodeValue;
    } else if (node.nodeName == "description") {
      var description = node.firstChild.nodeValue;
    } 
  }

  var sharedContent = "Shared a link: <a href=\"" + link + "\" target=\"_blank\">" + title + " </a>";
  
  var activity = opensocial.newActivity({ 'title' : sharedContent,
	                                'templateParams': {"title" : title, "link" : link, "description": description}});


   opensocial.requestCreateActivity(activity, "HIGH", this.shared);
}

RssFetch.prototype.generateLinkContent = function(i, link) {
  return "<a href='javascript:rssFetch.share(" + i +")'>Share</a> | <a href='" + link + "' target='_blank'>view link</a>";
}

RssFetch.prototype.refreshFeed = function() {
	_IG_FetchXmlContent(prefs.getString("rssurl"), function(feed) {rssFetch.renderFeed(feed);}, { refreshInterval: (60 * 30) });
}

RssFetch.prototype.adjustHeight = function() {
	var rss = document.getElementById('rssFeed');
	rss.style.height = 'auto';
	gadgets.window.adjustHeight();
	rss.style.height = '100%';
}

RssFetch.prototype.loadPage = function() {
  var feedEl = _gel("rssFeed");
  var currentView = gadgets.views.getCurrentView().getName();

  var bullet = "<img src='/social/gadgets/SocialRssReader/images/icon-rss.png' tilte='rssIcon' alt='rssIcon' border=0 align='absmiddle' style='height:16;width:16;' onerror='this.style.visibility=\"hidden\";'>&nbsp;&nbsp;";

  while ( feedEl.hasChildNodes() )
  {
    feedEl.removeChild( feedEl.firstChild );
  }

  eXo.social.thisRssFetch.startIndex = (eXo.social.thisRssFetch.currentPage - 1)*rssOnPage;

  if (eXo.social.thisRssFetch.currentPage == eXo.social.thisRssFetch.totalPages) {
    var totalPreviuosPages = (eXo.social.thisRssFetch.totalPages-1)*rssOnPage -1;
    var rssAtLastPage = eXo.social.thisRssFetch.totalItems - totalPreviuosPages;
    eXo.social.thisRssFetch.endIndex = 	eXo.social.thisRssFetch.startIndex + rssAtLastPage - 1;
  } else {
    eXo.social.thisRssFetch.endIndex = 	eXo.social.thisRssFetch.startIndex + rssOnPage;
  }

  for (var i = eXo.social.thisRssFetch.startIndex; i < eXo.social.thisRssFetch.endIndex; i++) {
    var itemEl = document.createElement('div');
        var item_title = document.createElement('div');
        var item_more = document.createElement('div');
        var item_desc = document.createElement('div');
        var item_date = document.createElement('div');
        var item_link = document.createElement('div');
        var item_link_date = document.createElement('div');
        item_link_date.appendChild(item_date);
        item_link_date.appendChild(item_link);

        itemEl.id = 'item_'+i;
        item_title.id = 'title_'+i;
        item_more.id = 'more_'+i;
        item_desc.id = 'desc_'+i;
        item_date.id = 'date_'+i;
        item_link.id = 'link_'+i;
        if (currentView == "home") {
          item_more.style.display='none';
        }

        if (currentView == "canvas") {
          item_date.style.fontSize="12";
        item_desc.style.fontSize="14";
        item_link.style.fontSize="14";
        }

        itemEl.className = 'item';
        item_title.className = 'title';
        item_more.className = 'more';
        item_desc.className = 'desc';
        item_date.className = 'date';
        item_link.className = 'link';
        
        item_link_date.className = 'subInfo';

    var nodeList = eXo.social.thisRssFetch.itemList.item(i).childNodes;
    for (var j = 0; j < nodeList.length ; j++) {
      var node = nodeList.item(j);
      if (node.nodeName == "title") {
        var title = node.firstChild.nodeValue;
      } else if (node.nodeName == "link") {
        var link = node.firstChild.nodeValue;
      } else if (node.nodeName == "description") {
        var description = node.firstChild.nodeValue;
      } else if (node.nodeName == "pubDate") {
        var date = node.firstChild.nodeValue;
      }
    }

    if (currentView == "home") {
      item_title.innerHTML = bullet + "<a style='text-decoration:underline;' id='link_title_"+i+"' class='titlelink' href='" + link + "' onclick='eXo.social.thisRssFetch.toggleDescription("+i+");return false;'>" + title + "</a>";
    } else {
      item_title.innerHTML = bullet + "<a style='text-decoration:underline; font-size:14px;' id='link_title_"+i+"' class='titlelink' href='" + link + "' onclick='eXo.social.thisRssFetch.toggleDescription("+i+");return false;'>" + title + "</a>";
    }
    item_date.innerHTML = date;
    item_desc.innerHTML = description;
    item_link.innerHTML = this.generateLinkContent(i, link);

    item_more.appendChild(item_link_date);
    item_more.appendChild(item_desc);

    itemEl.appendChild(item_title);
    itemEl.appendChild(item_more);

    feedEl.appendChild(itemEl);
  }

  if (eXo.social.thisRssFetch.totalPages > 1) {
    var outer_div = document.createElement('div');
    var item_paging = document.createElement('div');
    var float_div = document.createElement('div');
    var span_tag = document.createElement('span');
    item_paging.id = "paging";
    //item_paging.className = "UIPageIterator";
    item_paging.className = "pagination uiPageIterator";
    float_div.className = "ClearRight";

    item_paging.innerHTML = this.setDisplayPaging(eXo.social.thisRssFetch.currentPage);
    float_div.appendChild(span_tag);
    item_paging.appendChild(float_div);

    outer_div.appendChild(item_paging);
    feedEl.appendChild(outer_div);

    var totalpagesEl = _gel("totalPages");
    totalpagesEl.innerHTML = eXo.social.thisRssFetch.totalPages;

    var allPages = [];
    var allPagesEl = this.getEl("pages");
    
    var totalPage = eXo.social.thisRssFetch.totalPages;
    var nextPageTag = "<li><a  id='next' class='Icon NextPageIcon' onclick='eXo.social.thisRssFetch.nextPage()' data-placement='left' rel='tooltip' data-original-title='Next Page'>" +
    				  "<i class='uiIconNextArrow'></i></a></li>";
    
    var previousPageTag = "<li><a  id='previous' class='Icon LastPageIcon' onclick='eXo.social.thisRssFetch.previousPage()' data-placement='left' rel='tooltip' data-original-title='Previous Page'>" +
	  				  "<i class='uiIconPrevArrow'></i></a></li>";
    
    var nextDisTag = "<li class='disabled'><a  id='next' onclick='eXo.social.thisRssFetch.nextPage()' data-placement='left' rel='tooltip' data-original-title='Next Page'>" +
	  				 "<i class='uiIconNextArrow'></i></a></li>";
    
    var previousDisTag = "<li class='disabled'><a  id='previous' onclick='eXo.social.thisRssFetch.previousPage()' data-placement='left' rel='tooltip' data-original-title='Previous Page'>" +
	  					 "<i class='uiIconPrevArrow'></i></a></li>";
    
    var i = 1;
    while(i <= totalPage) {
    	var str="";

    	if (i == eXo.social.thisRssFetch.currentPage) {
    		str = "<li class ='active'><a onclick='eXo.social.thisRssFetch.toPage("+i+")'>" + i + "</a></li>";
    	} else {
    		str = "<li><a onclick='eXo.social.thisRssFetch.toPage("+i+")'>" + i + "</a></li>";
    	}

    	allPages.push(str);
    	i++;
    }
    
    var currentPage = eXo.social.thisRssFetch.currentPage;
    if (totalPage == 1) {
    	allPagesEl.innerHTML  = this.createHTML(previousDisTag, allPages.join(" "), nextDisTag);
    } else if ((currentPage > 1) && (currentPage < totalPage)) {
    	allPagesEl.innerHTML = this.createHTML(previousPageTag, allPages.join(" "), nextPageTag);
    } else if (currentPage == 1) {
    	allPagesEl.innerHTML = this.createHTML(previousDisTag, allPages.join(" "), nextPageTag);
    } else if (currentPage == totalPage) {
    	allPagesEl.innerHTML = this.createHTML(previousPageTag, allPages.join(" "), nextDisTag);
    }

  }
  
  //show Tool-tip for paging
  $('#next').tooltip();
  $('#previous').tooltip();

  this.adjustHeight();
}

RssFetch.prototype.createHTML = function(previous, pages, next) {
	var HTML="";
	HTML+= previous;
	HTML+= pages;
	HTML+= next;
	return HTML;
}

RssFetch.prototype.setDisplayPaging = function(currentPage) {
  var rtnHTML = "<ul id='pages'></ul>";
  rtnHTML+="<a class='PagesTotalNumber' id='totalPages' title='Total pages'></a>";
  rtnHTML+= "<a class='TotalPages'>Total pages:</a>";
  return rtnHTML;
}

RssFetch.prototype.createTag = function(id, cls, action, title) {
  return "<a  id='" +id+"' class='"+cls+"' onclick='"+action+"' title='" + title + "'></a>";
}

RssFetch.prototype.getEl = function (elId) {
    var el = document.getElementById(elId);
    if (el) {
        return el;
    } else {
        alert("element: " + elId + " not found!");
    }
}

RssFetch.prototype.renderFeed = function(feed) {

  if (!feed) {
    var feedEl = _gel("rssFeed");
    feedEl.className="err";
    feedEl.innerHTML = "Feed URL is invalid. Please check!";
    return;
  }

  var itemList = feed.getElementsByTagName("item");
  var titleList = feed.getElementsByTagName("title");
  var titleNodeList = titleList.item(0).childNodes;
  var titleValue = titleNodeList.item(0).nodeValue;

  eXo.social.thisRssFetch = new RssFetch();
  gadgets.window.setTitle(titleValue);
  eXo.social.thisRssFetch.feed = feed;
  eXo.social.thisRssFetch.itemList = itemList;
  eXo.social.thisRssFetch.totalItems = itemList.length;
  eXo.social.thisRssFetch.totalPages = Math.ceil(eXo.social.thisRssFetch.totalItems/rssOnPage);
  eXo.social.thisRssFetch.currentPage = 1;

  this.loadPage();
}

RssFetch.prototype.firstPage = function() {
    if (eXo.social.thisRssFetch.currentPage != 1) {
      eXo.social.thisRssFetch.currentPage = 1;
      this.toPage(eXo.social.thisRssFetch.currentPage);
    }
}

RssFetch.prototype.lastPage = function() {
    if (eXo.social.thisRssFetch.currentPage != eXo.social.thisRssFetch.totalPages) {
        eXo.social.thisRssFetch.currentPage = eXo.social.thisRssFetch.totalPages;
        this.toPage(eXo.social.thisRssFetch.currentPage);
    }

}

RssFetch.prototype.nextPage = function() {
    if (eXo.social.thisRssFetch.currentPage < eXo.social.thisRssFetch.totalPages) {
      eXo.social.thisRssFetch.currentPage += 1;
      this.toPage(eXo.social.thisRssFetch.currentPage);
    }
}

RssFetch.prototype.previousPage = function() {
    if (eXo.social.thisRssFetch.currentPage > 1) {
        eXo.social.thisRssFetch.currentPage -= 1;
        this.toPage(eXo.social.thisRssFetch.currentPage);
    }
}

RssFetch.prototype.toPage = function(pageNum) {
    if (pageNum > 0 && pageNum <= eXo.social.thisRssFetch.totalPages) {
        eXo.social.thisRssFetch.currentPage = pageNum;
        eXo.social.thisRssFetch.loadPage();
    }
}

