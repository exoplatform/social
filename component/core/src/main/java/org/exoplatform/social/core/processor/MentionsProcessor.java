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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.exoplatform.container.xml.InitParams;
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

  public MentionsProcessor(InitParams params, LinkProvider linkProvider) {
    super(params);
    this.linkProvider = linkProvider;
  }


  public void processActivity(Activity activity) {
    if (activity != null) {
    activity.setTitle(substituteUsernames(activity.getTitle()));
    activity.setBody(substituteUsernames(activity.getBody()));
    }
  }

  /*
   * Substitute @username expressions by full user profile link
   */
  private String substituteUsernames(String message) {
    if (message == null) {
      return null;
    }

    Pattern pattern = Pattern.compile("@([^\\s]+)|@([^\\s]+)$");
    Matcher matcher = pattern.matcher(message);

    // Replace all occurrences of pattern in input
    StringBuffer buf = new StringBuffer();
    while (matcher.find()) {
        // Get the match result
        String replaceStr = matcher.group().substring(1);

        // Convert to uppercase
        replaceStr = linkProvider.getProfileLink(replaceStr, null);

        // Insert replacement
        matcher.appendReplacement(buf, replaceStr);
    }
    matcher.appendTail(buf);
    return buf.toString();

  }


}
