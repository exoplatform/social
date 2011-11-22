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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.exoplatform.social.common.xmlprocessor.BaseXMLFilterPlugin;
import org.exoplatform.social.common.xmlprocessor.model.Attributes;
import org.exoplatform.social.common.xmlprocessor.model.Node;
import org.exoplatform.social.common.xmlprocessor.model.XMLTagFilterPolicy;


/**
 * This Filter travel through DOM tree and find if any TAG is not satisfied the rules specified by a list of allowed
 * tags.
 * With wrong TAG, it change itself to content Type.
 *
 * @author Ly Minh Phuong - http://phuonglm.net
 * @since  1.2.1
 */
public class DOMXMLTagFilterPlugin extends BaseXMLFilterPlugin {
  private LinkedHashMap<String, Attributes> allowedTags = new LinkedHashMap<String, Attributes>();

  /**
   * Gets the policy List.
   *
   * @return the allowedTags
   */
  public LinkedHashMap<String, Attributes> getAllowedTags() {
    return allowedTags;
  }

  /**
   * Sets allowed tag policy to DOMXMLagFilter.
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
  public DOMXMLTagFilterPlugin(XMLTagFilterPolicy tagFilterPolicy) {
    allowedTags = Util.getAllowedTagsFromTagFilterPolicy(tagFilterPolicy);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object doFilter(Object input) {
    if (input instanceof Node) {
      nodeFilter((Node) input);
    }
    return input;
  }

  private Node nodeFilter(Node currentNode) {
    LinkedList<Node> currentChildNode = currentNode.getChildNodes();
    if (!currentNode.getTitle().isEmpty()) {
      String tag = currentNode.getTitle();
      if (allowedTags.containsKey(tag)) {

        Attributes currentAttributes = currentNode.getAttributes();
        Attributes validatedAttributes = new Attributes();

        for (Iterator<String> iterator = currentAttributes.getKeyIterator(); iterator.hasNext(); ) {
          String key = iterator.next();
          if (allowedTags.get(tag).hasKey(key)) {
            validatedAttributes.put(key, currentAttributes.get(key));
          }
        }
        currentNode.setAttributes(validatedAttributes);
      } else {
        currentNode.convertToContent();
      }
    }
    for (int i = 0; i < currentChildNode.size(); i++) {
      nodeFilter(currentChildNode.get(i));
    }
    return currentNode;
  }
}
