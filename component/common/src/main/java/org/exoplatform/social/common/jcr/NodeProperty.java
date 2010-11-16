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
package org.exoplatform.social.common.jcr;

/**
 * Node properties for JCR and Social.
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since Nov 10, 2010
 * @since 1.2.0-GA
 */
public final class NodeProperty {

  /* eXo Social Activity Node Node Properties */

  public static final String ACTIVITY_ID = "exo:id";
  public static final String ACTIVITY_TITLE = "exo:title";
  public static final String ACTIVITY_TITLE_TEMPLATE = "exo:titleTemplate";
  public static final String ACTIVITY_USER_ID = "exo:userId";
  public static final String ACTIVITY_TYPE = "exo:type";
  public static final String ACTIVITY_REPLY_TO_ID = "exo:replyToId";
  public static final String ACTIVITY_HIDDEN = "exo:hidden";
  public static final String ACTIVITY_LIKE_IDENTITY_IDS = "exo:likeIdentityIds";
  //TODO use "exo:templateParams" instead, need change in node configuration
  public static final String ACTIVITY_TEMPLATE_PARAMS = "exo:params";
  public static final String ACTIVITY_BODY = "exo:body";
  public static final String ACTIVITY_BODY_TEMPLATE = "exo:bodyTemplate";
  public static final String ACTIVITY_EXTERNAL_ID = "exo:externalId";
  //TODO use "exo:updated" instead, need change in node configuration
  public static final String ACTIVITY_UPDATED = "exo:updatedTimestamp";
  public static final String ACTIVITY_POSTED_TIME = "exo:postedTime";
  public static final String ACTIVITY_PRIORITY = "exo:priority";
  public static final String ACTIVITY_URL = "exo:url";


  /* JCR Node Property */

  public static final String JCR_PRIMARY_TYPE = "jcr:primaryType";

  public static final String JCR_MIXIN_TYPES = "jcr:mixinTypes";

  public static final String JCR_CREATED = "jcr:created";

  public static final String JCR_CONTENT = "jcr:content";

  public static final String JCR_ENCODING = "jcr:encoding";

  public static final String JCR_MIME_TYPE = "jcr:mimeType";

  public static final String JCR_LAST_MODIFIED = "jcr:lastModified";

  public static final String JCR_NODE_TYPE_NAME = "jcr:nodeTypeName";

  public static final String JCR_SUPER_TYPES = "jcr:superTypes";

  public static final String JCR_IS_MIXIN = "jcr:isMixin";

  public static final String JCR_HAS_ORDERABLE_CHILD_NODES = "jcr:hasOrderableChildNodes";

  public static final String JCR_PRIMARY_ITEM_NAME = "jcr:primaryItemName";

  public static final String JCR_PROPERTY_DEFINITION = "jcr:propertyDefinition";

  public static final String JCR_CHILD_NODE_DEFINITION = "jcr:childNodeDefinition";

  public static final String JCR_NAME = "jcr:name";

  public static final String JCR_AUTO_CREATED = "jcr:autoCreated";

  public static final String JCR_MANDATORY = "jcr:mandatory";

  public static final String JCR_ON_PARENT_VERSION = "jcr:onParentVersion";

  public static final String JCR_PROTECTED = "jcr:protected";

  public static final String JCR_REQUIRED_TYPE = "jcr:requiredType";

  public static final String JCR_REQUIRED_PRIMARY_TYPES = "jcr:requiredPrimaryTypes";

  public static final String JCR_DEFAULT_PRIMARY_TYPE = "jcr:defaultPrimaryType";

  public static final String JCR_SAME_NAME_SIBLINGS = "jcr:sameNameSiblings";

  public static final String JCR_VERSIONABLE_UUID = "jcr:versionableUuid";

  public static final String JCR_ROOT_VERSION = "jcr:rootVersion";

  public static final String JCR_VERSION_LABELS = "jcr:versionLabels";

  public static final String JCR_VALUE_CONSTRAINTS = "jcr:valueConstraints";

  public static final String JCR_DEFAULT_VALUES = "jcr:defaultValues";

  public static final String JCR_MULTIPLE = "jcr:multiple";

  public static final String JCR_VERSION_HISTORY = "jcr:versionHistory";

  public static final String JCR_BASE_VERSION = "jcr:baseVersion";

  public static final String JCR_IS_CHECKED_OUT = "jcr:isCheckedOut";

  public static final String JCR_PREDECESSORS = "jcr:predecessors";

  public static final String JCR_SUCCESSORS = "jcr:successors";

  public static final String JCR_MERGE_FAILED = "jcr:mergeFailed";

  public static final String JCR_FROZEN_NODE = "jcr:frozenNode";

  public static final String JCR_FROZEN_PRIMARY_TYPE = "jcr:prozenPrimaryType";

  public static final String JCR_FROZEN_MIXIN_TYPES = "jcr:prozenMixinTypes";

  public static final String JCR_FROZEN_UUID = "jcr:frozenUuid";

  public static final String JCR_CHILD_VERSION_HISTORY = "jcr:childVersionHistory";

  public static final String JCR_STATEMENT = "jcr:statement";

  public static final String JCR_LANGUAGE = "jcr:language";

}
