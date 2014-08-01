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

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.social.core.BaseActivityProcessorPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.service.LinkProvider;

/**
 * A processor that substitute @username expressions by a link on the user profile
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class MentionsProcessor extends BaseActivityProcessorPlugin {

  private static final Pattern USERNAME_PATTERN = Pattern.compile("(^|\\s)(@([\\p{L}\\p{Digit}\\_\\.\\-]+)([\\p{L}\\p{Digit}]+))");

  public MentionsProcessor(InitParams params) {
    super(params);
  }


  public void processActivity(ExoSocialActivity activity) {
    if (activity != null) {
      activity.setTitle(substituteUsernames(activity.getTitle()));
      activity.setBody(substituteUsernames(activity.getBody()));
      Map<String, String> templateParams = activity.getTemplateParams();
      
      List<String> templateParamKeys = getTemplateParamKeysToFilter(activity);
      for(String key : templateParamKeys){
        templateParams.put(key, (String) substituteUsernames(templateParams.get(key)));
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
    
    Matcher matcher = USERNAME_PATTERN.matcher(message);

    // Replace all occurrences of pattern in input
    StringBuffer buf = new StringBuffer();
    String mention, replaceStr, portalOwner;
    while (matcher.find()) {
      // Get the match result
      mention = matcher.group();
      replaceStr = mention.substring(mention.indexOf("@") + 1);
      try {
        portalOwner = Util.getPortalRequestContext().getPortalOwner();
      } catch (Exception e) {
        //default value for testing and social
        portalOwner = LinkProvider.DEFAULT_PORTAL_OWNER;
      }
      replaceStr = LinkProvider.getProfileLink(replaceStr, portalOwner);
      if (mention.indexOf("@") > 0) {
        replaceStr = " " + replaceStr;
      }

      // Insert replacement
      if (replaceStr != null) {
        matcher.appendReplacement(buf, replaceStr);
      }
    }
    matcher.appendTail(buf);
    return buf.toString();
  }
}
