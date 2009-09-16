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
package org.exoplatform.social.portlet.activities;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.social.core.activitystream.ActivityManager;
import org.exoplatform.social.core.activitystream.model.Activity;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.portlet.URLUtils;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

@ComponentConfig(
    template =  "app:/groovy/portal/webui/component/UIActivities.gtmpl",
    events = {
        @EventConfig(listeners = UIActivities.ChangeTimeZoneActionListener.class)
    }
)
public class UIActivities  extends UIContainer {
  private int timeZoneOffset = -1;
  
  public void setTimeZoneOffset(int tzo) {
    timeZoneOffset = tzo;
  }
  
  public int getTimeZoneOffset() {
    return timeZoneOffset;
  }
  
  public List<Activity> getActivities() throws Exception {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    ActivityManager am = (ActivityManager) container.getComponentInstanceOfType(ActivityManager.class);
    IdentityManager im = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);

    Identity id = im.getIdentityByRemoteId("organization", URLUtils.getCurrentUser());
    List<Activity> listActivity = am.getActivities(id);
    // Reverse order of activity so that newer activity is placed on the top stack
    java.util.Collections.reverse(listActivity);
    return listActivity;
  }
  
  public Identity getCurrentIdentity() throws Exception {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    IdentityManager im = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);

    Identity currentId = im.getIdentityByRemoteId("organization", URLUtils.getCurrentUser());
    
    return currentId;
  }
  
  public String getPortalName() {
    PortalContainer pcontainer =  PortalContainer.getInstance();
    return pcontainer.getPortalContainerInfo().getContainerName();  
  }
  
  public String getRepository() throws Exception {
    RepositoryService rService = getApplicationComponent(RepositoryService.class) ;    
    return rService.getCurrentRepository().getConfiguration().getName() ;
  }
  
  public String timeToPrettyString(Long postedTime) {
    String pattern = "EEE MMM d yyyy HH:mm:ss z";
    SimpleDateFormat sdf = new SimpleDateFormat(pattern);
    if (timeZoneOffset == -1) {
      // Set system default timezone
      TimeZone tz = TimeZone.getDefault();
      timeZoneOffset = tz.getRawOffset()/(1000 * 60); // convert offset from miliseconds to mins
    }
    
    int minutes = timeZoneOffset % 60;
    int hours = (timeZoneOffset - minutes) / 60;
    String timezoneId = "GMT";
    timezoneId += (timeZoneOffset > 0 ? "+" : "-") + (hours < 10 || hours > -9 ? "0": "") + Math.abs(hours) + ":" + (minutes < 10 ? "0" : "") + minutes;
    sdf.setTimeZone(TimeZone.getTimeZone(timezoneId));
    
    return sdf.format(new Date(postedTime));
    
    /*
    long time = (new Date().getTime() - postedTime)/1000;
    long value = 0;
    if (time < 60) {
      return "less than a minute ago";
    } else {
      if (time < 120) {
        return "about a minute ago";
      } else {
        if (time < 3600) {
          value = Math.round(time / 60);
          return "about " + value + " minutes ago";
        } else {
          if (time < 7200) {
            return "about an hour ago";
          } else {
            if (time < 86400) {
              value = Math.round(time / 3600);
              return "about " + value + " hours ago";
            } else {
              if (time < 172800) {
                return "about a day ago";
              } else {
                if (time < 2592000) {
                  value = Math.round(time / 86400);
                  return "about " +value + " days ago";
                } else {
                  if (time < 5184000) {
                    return "about a month ago";
                  } else {
                    value = Math.round(time / 2592000);
                    return "about " + value + " months ago";
                  }
                }
              }
            }
          }
        }
      }
    }
    */
  }
  
  static public class ChangeTimeZoneActionListener extends EventListener<UIActivities> {
    public void execute(Event<UIActivities> event) throws Exception {
      String offset = event.getRequestContext().getRequestParameter(OBJECTID);
     UIActivities uiActivities = event.getSource();
     try {
       uiActivities.timeZoneOffset = Integer.parseInt(offset);
     } catch (Exception ex) {
       uiActivities.timeZoneOffset = -1;
     }
     event.getRequestContext().addUIComponentToUpdateByAjax(uiActivities);
    }
  }
}
