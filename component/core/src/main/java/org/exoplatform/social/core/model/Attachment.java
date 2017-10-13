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

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Session;
import java.io.InputStream;

public abstract  class Attachment {

  private static final Log LOG = ExoLogger.getLogger(Attachment.class);

  /**
   * The id.
   */
  private String id;

  /**
   * The file name.
   */
  private String fileName;

  /**
   * The mime type.
   */
  private String mimeType;

  /**
   * The workspace.
   */
  private String workspace;

  /**
   * The image bytes.
   */
  private byte[] imageBytes;

  /**
   * The last modified.
   */
  private long lastModified;

  public Attachment() {
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
  public Attachment(String id,
                    String fileName,
                    String mimeType,
                    InputStream inputStream,
                    String workspace,
                    long lastModified) throws Exception {
    this.id = id;
    this.fileName = fileName;
    setInputStream(inputStream);
    this.mimeType = mimeType;
    this.workspace = workspace;
    this.lastModified = lastModified;
  }

  public abstract String getAttachmentType();

  /**
   * Gets the data path by specifying a PortalContainer instance
   *
   * @return the data path
   * @throws Exception the exception
   * @deprecated
   */
  public String getDataPath(PortalContainer portalContainer) throws Exception {
    return getDataPath();
  }

  /**
   * Gets the data path.
   *
   * @return the data path
   * @throws Exception the exception
   */
  public String getDataPath() throws Exception {
    try {
      return getSession().getItem(getId()).getPath();
    } catch (ItemNotFoundException e) {
      LOG.warn("Failed to get data path", e);
      return null;
    }
  }

  /**
   * Gets the id.
   *
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the id.
   *
   * @param s the new id
   */
  public void setId(String s) {
    id = s;
  }

  /**
   * Gets the workspace.
   *
   * @return the workspace
   */
  public String getWorkspace() {
    return workspace;
  }

  /**
   * Sets the workspace.
   *
   * @param ws the new workspace
   */
  public void setWorkspace(String ws) {
    workspace = ws;
  }

  /**
   * Gets the file name.
   *
   * @return the file name
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * Sets the file name.
   *
   * @param s the new file name
   */
  public void setFileName(String s) {
    fileName = s;
  }

  /**
   * Gets the mime type.
   *
   * @return the mime type
   */
  public String getMimeType() {
    return mimeType;
  }

  /**
   * Sets the mime type.
   *
   * @param s the new mime type
   */
  public void setMimeType(String s) {
    mimeType = s;
  }

  /**
   * Gets the last modified.
   *
   * @return the last modified
   */
  public long getLastModified() {
    return lastModified;
  }

  /**
   * Sets the last modified.
   *
   * @param lastModified the new last modified
   */
  public void setLastModified(long lastModified) {
    this.lastModified = lastModified;
  }

  /**
   * Gets images size in MB/ KB/ Bytes.
   *
   * @return image size string
   */
  public String getSize() {
    int KB_SIZE = 1024;
    int MB_SIZE = 1024 * KB_SIZE;
    int length = imageBytes.length;
    double size;
    if (length >= MB_SIZE) {
      size = length / MB_SIZE;
      return size + " MB";
    } else if (length >= KB_SIZE) {
      size = length / KB_SIZE;
      return size + " KB";
    } else { //Bytes size
      return length + " Bytes";
    }
  }

  /**
   * Gets imageBytes.
   *
   * @return
   */
  public byte[] getImageBytes() {
    return imageBytes;
  }

  /**
   * Sets the input stream.
   *
   * @param input the new input stream
   * @throws Exception the exception
   */
  public void setInputStream(InputStream input) throws Exception {
    if (input != null) {
      imageBytes = new byte[input.available()];
      input.read(imageBytes);
    } else {
      imageBytes = null;
    }
  }

  /**
   * Gets the session.
   *
   * @return the session
   * @throws Exception the exception
   */
  private Session getSession() throws Exception {
    return CommonsUtils.getSystemSessionProvider().getSession(workspace, CommonsUtils.getRepository());
  }

}