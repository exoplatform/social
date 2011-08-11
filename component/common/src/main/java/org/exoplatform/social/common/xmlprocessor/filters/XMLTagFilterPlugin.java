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
import java.util.List;
import java.util.regex.Matcher;

import org.apache.commons.lang.StringEscapeUtils;
import org.exoplatform.social.common.xmlprocessor.BaseXMLFilterPlugin;
import org.exoplatform.social.common.xmlprocessor.DOMParser;
import org.exoplatform.social.common.xmlprocessor.Tokenizer;
import org.exoplatform.social.common.xmlprocessor.model.Attributes;
import org.exoplatform.social.common.xmlprocessor.model.XMLTagFilterPolicy;

/**
 * This is XML Tags and Attributes filter from String input base on whitelist. You must define XMLTagFilterPolicy and
 * add XMLTagFilterPolicy to this constructor.
 *
 * @author Ly Minh Phuong - http://phuonglm.net
 */
public class XMLTagFilterPlugin extends BaseXMLFilterPlugin {
  private LinkedHashMap<String, Attributes> allowedTags;

  /**
   * Gets allowed tags list
   *
   * @return the allowed tags list
   */
  public LinkedHashMap<String, Attributes> getAllowedTags() {
    return allowedTags;
  }

  /**
   * Sets allowed tags list
   *
   * @param allowedTags
   */
  public void setAllowedTags(LinkedHashMap<String, Attributes> allowedTags) {
    this.allowedTags = allowedTags;
  }

  /**
   * Constructor, the policy must be set from constructor.
   *
   * @param tagFilterPolicy
   */
  public XMLTagFilterPlugin(XMLTagFilterPolicy tagFilterPolicy) {
    allowedTags = Util.getAllowedTagsFromTagFilterPolicy(tagFilterPolicy);
  }

  /**
   *
   */
  public Object doFilter(Object input) {
    if (input instanceof String) {
      input = nodeFilter((String) input);
    }
    return input;
  }

  private String nodeFilter(String xmlInput) {
    List<String> xmlTokens = Tokenizer.tokenize(xmlInput);

    for (int i = 0; i < xmlTokens.size(); i++) {
      String token = xmlTokens.get(i);


      Matcher startMatcher = DOMParser.TAGSTARTPATTERN.matcher(token);
      Matcher endMatcher = DOMParser.TAGCLOSEPATTERN.matcher(token);

      if (startMatcher.find()) {
        String tag = startMatcher.group(1).toLowerCase();

        if (!allowedTags.containsKey(tag)) {
          xmlTokens.set(i, StringEscapeUtils.escapeHtml(token));
        } else {
          StringBuilder tagStringBuilder = new StringBuilder("<" + tag);

          String tokenBody = startMatcher.group(2);
          Matcher attributes = DOMParser.ATTRIBUTESPATTERN.matcher(tokenBody);

          Attributes attributesWhiteList = allowedTags.get(tag);

          while (attributes.find()) {
            String attr = attributes.group(1).toLowerCase();
            String val = attributes.group(4) == null ? attributes.group(3) : attributes.group(4);
            if (attributesWhiteList.hasKey(attr)) {
              tagStringBuilder.append(" " + attr + "=" + "\"" + val + "\"");
            }
          }
          if (DOMParser.SELFTCLOSETAGPATTERN.matcher(token).find()) {
            tagStringBuilder.append(" />");
          } else {
            tagStringBuilder.append(">");
          }
          xmlTokens.set(i, tagStringBuilder.toString());
        }
      } else if (endMatcher.find()) {
        String tag = endMatcher.group(1).toLowerCase();
        if (!allowedTags.containsKey(tag)) {
          xmlTokens.set(i, StringEscapeUtils.escapeHtml(token));
        }
      }
    }
    StringBuilder xmlStringBuilder = new StringBuilder();

    for (String xmlToken : xmlTokens) {
      xmlStringBuilder.append(xmlToken);
    }
    return xmlStringBuilder.toString();
  }

}
