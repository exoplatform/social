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
package org.exoplatform.social.core.model;

import java.io.InputStream;

/*
 * This class using for attachment profile of identity or of space, such as
 * image.
 *
 * @author  <a href="mailto:tungcnw@gmail.com">dang.tung</a>
 * @since   Sep 11, 2009
 */
public class AvatarAttachment extends  Attachment {

  public static final String TYPE = "avatar";

  public AvatarAttachment() {
    super();
  }

  /**
   * Constructor.
   *
   * @param id
   * @param fileName
   * @param mimeType
   * @param inputStream
   * @param workspace
   * @param lastModified
   * @throws Exception
   */
  public AvatarAttachment(String id,
                          String fileName,
                          String mimeType,
                          InputStream inputStream,
                          String workspace,
                          long lastModified) throws Exception {
    super(id, fileName, mimeType, inputStream, workspace, lastModified);
  }

  @Override
  public String getAttachmentType() {
    return TYPE;
  }

}