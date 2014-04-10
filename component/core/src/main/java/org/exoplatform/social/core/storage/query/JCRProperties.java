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

package org.exoplatform.social.core.storage.query;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class JCRProperties {

  public static final PropertyLiteralExpression<String> path = new PropertyLiteralExpression<String>(String.class, "jcr:path");
  public static final PropertyLiteralExpression<String> id = new PropertyLiteralExpression<String>(String.class, "jcr:uuid");
  public static final PropertyLiteralExpression<String> name = new PropertyLiteralExpression<String>(String.class, "exo:name");
  
  public static final PropertyLiteralExpression<String> JCR_EXCERPT = new PropertyLiteralExpression<String>(String.class, "rep:excerpt()");
  public static final PropertyLiteralExpression<String> JCR_RELEVANCY = new PropertyLiteralExpression<String>(String.class, "jcr:score");
  public static final PropertyLiteralExpression<String> JCR_LAST_CREATED_DATE = new PropertyLiteralExpression<String>(String.class, "exo:dateCreated");
  public static final PropertyLiteralExpression<String> JCR_LAST_MODIFIED_DATE = new PropertyLiteralExpression<String>(String.class, "exo:lastModifiedDate");
  
  public static final String PROFILE_NODE_TYPE = "soc:profiledefinition";
  public static final String SPACE_NODE_TYPE = "soc:spacedefinition";
  public static final String SPACE_REF_NODE_TYPE = "soc:spaceref";
  public static final String ACTIVITY_NODE_TYPE = "soc:activity";
  public static final String RELATIONSHIP_NODE_TYPE = "soc:relationship";
  public static final String IS_DISABLED_NODE_TYPE = "soc:isDisabled";
}
