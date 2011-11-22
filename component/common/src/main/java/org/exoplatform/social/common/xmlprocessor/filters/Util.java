/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.common.xmlprocessor.filters;

import java.util.LinkedHashMap;
import java.util.Set;

import org.exoplatform.social.common.xmlprocessor.model.Attributes;
import org.exoplatform.social.common.xmlprocessor.model.XMLTagFilterPolicy;
import org.exoplatform.social.common.xmlprocessor.model.XMLTagFilterPolicy.AllowedTag;

/**
 * Utility class for filter processing.
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since Aug 3, 2011
 * @since  1.2.1
 */
class Util {

  /**
   * Gets white list from a tag filter policy.
   *
   * @param tagFilterPolicy the tag filter policy
   * @return the white list
   */
  public static LinkedHashMap<String, Attributes> getAllowedTagsFromTagFilterPolicy(XMLTagFilterPolicy tagFilterPolicy) {
    LinkedHashMap<String, Attributes> allowedTags = new LinkedHashMap<String, Attributes>();
    Set<AllowedTag> allowedTagList = tagFilterPolicy.getAllowedTagSet();
    for (AllowedTag allowedTag: allowedTagList) {
      Set<String> tagAttributes = allowedTag.getTagAttributes();
      Attributes attributes = new Attributes();
      for (String attributeKey : tagAttributes) {
        attributes.put(attributeKey, "");
      }
      allowedTags.put(allowedTag.getTagName(), attributes);
    }
    return allowedTags;
  }

}
