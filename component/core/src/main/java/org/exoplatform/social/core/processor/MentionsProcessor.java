/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.social.core.processor;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.social.core.BaseActivityProcessorPlugin;
import org.exoplatform.social.core.activity.model.Activity;
import org.exoplatform.social.core.service.LinkProvider;

/**
 * A processor that substitute @username expressions by a link on the user profile
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class MentionsProcessor extends BaseActivityProcessorPlugin {


  private LinkProvider linkProvider;
  private static final Pattern USERNAME_MENTION_PATTERN = Pattern.compile("(^|\\s)(@([\\p{L}\\p{Digit}\\_\\.\\-]+))");

  public MentionsProcessor(InitParams params, LinkProvider linkProvider) {
    super(params);
    this.linkProvider = linkProvider;
  }


  public void processActivity(Activity activity) {
    if (activity != null) {
      activity.setTitle(substituteUsernames(activity.getTitle()));
      activity.setBody(substituteUsernames(activity.getBody()));
      Map<String,String> templateParams = activity.getTemplateParams();
      if (templateParams != null) {
        Iterator<Entry<String,String>> iterator = templateParams.entrySet().iterator();
        while (iterator.hasNext()) {
          Entry<String, String> entry = iterator.next();
          templateParams.put(entry.getKey(), substituteUsernames(entry.getValue()));
        }
      }
    }
  }

  /*
   * Substitute @username expressions by full user profile link
   */
  private String substituteUsernames(String message) {
    if (message == null) {
      return null;
    }

    Matcher matcher = USERNAME_MENTION_PATTERN.matcher(message);

    // Replace all occurrences of pattern in input
    StringBuffer buf = new StringBuffer();
    while (matcher.find()) {
      // Get the match result
      String portalOwner;
      try{
        portalOwner = Util.getPortalRequestContext().getPortalOwner();
      } catch (Exception e){
        //default value for testing and social
        portalOwner = LinkProvider.DEFAULT_PORTAL_OWNER;
      }

      //create replace String.
      String profileURL = linkProvider.getProfileLink(matcher.group(3), portalOwner);
      if(profileURL != null){
        String replaceStr = matcher.group().replace(matcher.group(2), profileURL);
        // Insert replacement
        matcher.appendReplacement(buf, replaceStr);
      }

    }
    matcher.appendTail(buf);
    return buf.toString();

  }
}
