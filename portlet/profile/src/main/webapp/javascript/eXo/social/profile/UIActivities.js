/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */

/** 
 * @author <a href="mailto:tungcnw@gmail.com">dang.tung</a>
 */
eXo = window.eXo || {};
eXo.social = eXo.social || {};
eXo.social.activities = eXo.social.activities || {};

function UIActivities() {};


UIActivities.prototype.changeTimeZone = function(btn) {	
	var uiActivities = btn.parentNode.parentNode;
	var DOMUtil = eXo.core.DOMUtil;
	var portletFragment = DOMUtil.findAncestorByClass(btn, "PORTLET-FRAGMENT");
	var compId = portletFragment.parentNode.id;
	var uicomp = uiActivities.id;
	
	// Get client's timezone offset in minutes, multiple with -1 for the time zone.
	var timezone = new Date().getTimezoneOffset() * (-1);
	var href = eXo.env.server.portalBaseURL + "?portal:componentId=" + compId;
	href += "&portal:type=action&uicomponent=" + uicomp;
	href += "&op=ChangeTimeZone";
	href += "&objectId=" + timezone + "&ajaxRequest=true";
	ajaxGet(href);
};

eXo.social.activities.UIActivities = new UIActivities();