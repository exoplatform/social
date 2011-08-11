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
package org.exoplatform.social.common.xmlprocessor.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;

/**
 * Contains the set of {@link AllowedTag}.
 *
 * @author Ly Minh Phuong - http://phuonglm.net
 * @since 1.2.1
 */
public class XMLTagFilterPolicy {

  private Set<AllowedTag> allowedTagSet;

  /**
   * Default constructor
   */
  public XMLTagFilterPolicy() {
    allowedTagSet = new HashSet<AllowedTag>();
  }

  /**
   * Gets allowed tag set.
   *
   * @return the allowed tag set
   */
  public Set<AllowedTag> getAllowedTagSet() {
    return allowedTagSet;
  }

  /**
   * Sets allowed tag set.
   *
   * @param allowedTagSet the allowed tag set
   */
  public void setAllowedTagSet(Set<AllowedTag> allowedTagSet) {
    this.allowedTagSet = allowedTagSet;
  }

  /**
   * Adds many tags at once with empty attributes.
   *
   * @param tagNames
   */
  public void addAllowedTags(String... tagNames) {
    for (String tagName : tagNames) {
      if (allowedTagSet.contains(tagName)) {
        return;
      }
      AllowedTag allowedTag = new AllowedTag(tagName);
      allowedTagSet.add(allowedTag);
    }
  }

  /**
   * Adds allowed tagName and its allowed attributes.
   *
   * @param tagName the tag name
   * @param tagAttributes the set of attributes
   */
  public void addAllowedTag(String tagName, Set<String> tagAttributes) {
    AllowedTag allowedTag = new AllowedTag(tagName, tagAttributes);
    addAllowedTag(allowedTag);
  }

  /**
   * Adds allowed tag.
   *
   * @param allowedTag the allowed tag
   */
  public void addAllowedTag(AllowedTag allowedTag) {
    allowedTagSet.add(allowedTag);
  }

  /**
   * Removed allowed tag.
   *
   * @param allowedTag the allowed tag
   */
  public void removeAllowedTag(AllowedTag allowedTag) {
    allowedTagSet.remove(allowedTag);
  }


  /**
   * Sets {@link AllowedTagPlugin}.
   *
   * @param allowedTagPlugin
   */
  public void setAllowedTagPlugin(AllowedTagPlugin allowedTagPlugin) {
    List<AllowedTag> allowedTagList = allowedTagPlugin.getAllowedTagList();
    for (AllowedTag allowedTag : allowedTagList) {
      addAllowedTag(allowedTag);
    }
  }

  /**
   * The plugin component for configuring the list of {@link AllowedTag}.
   */
  public static class AllowedTagPlugin extends BaseComponentPlugin {
    private List<AllowedTag> allowedTagList;

    /**
     * Constructor.
     *
     * @param initParams
     */
    public AllowedTagPlugin(InitParams initParams) {
      allowedTagList = initParams.getObjectParamValues(AllowedTag.class);
    }

    /**
     * Gets the allowed tag list.
     *
     * @return the allowed tag list
     */
    public List<AllowedTag> getAllowedTagList() {
      return allowedTagList;
    }

  }

  /**
   * The allowed tags: tagName and tagAttributes to be configured for allowed tags.
   */
  public static class AllowedTag {
    private String tagName;
    private Set<String> tagAttributes = new HashSet<String>();

    /**
     * Default constructor.
     */
    public AllowedTag() {

    }

    /**
     * Constructor with tagName.
     *
     * @param tagName
     */
    public AllowedTag(String tagName) {
      this.tagName = tagName;
    }

    /**
     * Constructor with tagName and tagAttributes.
     *
     * @param tagName the tag name
     * @param tagAttributes the tag attributes
     */
    public AllowedTag(String tagName, Set<String> tagAttributes) {
      this.tagName = tagName;
      this.tagAttributes = tagAttributes;
    }

    /**
     * Gets the tag name.
     *
     * @return the tag name
     */
    public String getTagName() {
      return tagName;
    }

    /**
     * Sets the tag name.
     *
     * @param setTagName the tag name
     */
    public void setTagName(String setTagName) {
      tagName = setTagName;
    }

    /**
     * Sets the tag attributes.
     *
     * @param tagAttributes the tag attributes
     */
    public void setTagAttributes(Set<String> tagAttributes) {
      this.tagAttributes = tagAttributes;
    }

    /**
     * Gets the tag attributes.
     *
     * @return the tag attributes
     */
    public Set<String> getTagAttributes() {
      return tagAttributes;
    }

  }


}
