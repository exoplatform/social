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
package org.exoplatform.social.core.space.model;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.Session;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.RepositoryService;

// TODO: Auto-generated Javadoc
/**
 * Created by The eXo Platform SARL
 * Author : dang.tung
 *          tungcnw@gmail.com
 * Nov 10, 2009  
 * 
 */

/*
 * This class using for attachment profile of identity, such as image.
 */
public class SpaceAttachment {
  
  /** The id. */
  private String id;
  
  /** The file name. */
  private String fileName;
  
  /** The mime type. */
  private String mimeType;
  
  /** The workspace. */
  private String workspace;
  
  /** The image bytes. */
  private byte[] imageBytes;
  
  /** The last modified. */
  private long lastModified;
  
  /** The Constant KB_SIZE. */
  private static final int KB_SIZE = 1024;
  
  /** The Constant MB_SIZE. */
  private static final int MB_SIZE = 1024 * KB_SIZE;
  
  /**
   * Gets the data path.
   * 
   * @return the data path
   * @throws Exception the exception
   */
  public String getDataPath() throws Exception {
    Node attachmentData;
    try{
      attachmentData = (Node)getSession().getItem(getId());      
    }catch (ItemNotFoundException e) {
      e.printStackTrace();
      return null;
    }
    return attachmentData.getPath();
  }
  
  /**
   * Gets the id.
   * 
   * @return the id
   */
  public String getId() { return id; }
  
  /**
   * Sets the id.
   * 
   * @param s the new id
   */
  public void   setId(String s) { id = s; }
  
  /**
   * Gets the workspace.
   * 
   * @return the workspace
   */
  public String getWorkspace() { return workspace; }
  
  /**
   * Sets the workspace.
   * 
   * @param ws the new workspace
   */
  public void setWorkspace(String ws) { workspace = ws; }
  
  /**
   * Gets the file name.
   * 
   * @return the file name
   */
  public String getFileName()  { return fileName; }
  
  /**
   * Sets the file name.
   * 
   * @param s the new file name
   */
  public void   setFileName(String s) { fileName = s; }
  
  /**
   * Gets the mime type.
   * 
   * @return the mime type
   */
  public String getMimeType() { return mimeType; }
  
  /**
   * Sets the mime type.
   * 
   * @param s the new mime type
   */
  public void setMimeType(String s) { mimeType = s;}
  
  /**
   * Gets the image bytes.
   * 
   * @return the image bytes
   */
  public byte[] getImageBytes() { return imageBytes;}
  
  /**
   * Gets the last modified.
   * 
   * @return the last modified
   */
  public long getLastModified() { return lastModified;}
  
  /**
   * Sets the last modified.
   * 
   * @param lastModified the new last modified
   */
  public void setLastModified(long lastModified) { this.lastModified = lastModified;}

/**
 * get images size in MB/ KB/ Bytes.
 * 
 * @return image size string
 */
  public String getImageSize() {
    int length = imageBytes.length;
    double size;
    if (length >= MB_SIZE) {
      size = length / MB_SIZE;
      return size + " MB";
    } else if (length >= KB_SIZE) {
      size = length / KB_SIZE;
      return size + " KB";
    } else { //Bytes size
      return  length + " Bytes";
    }
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
    }
    else imageBytes = null;
  }
  
  /**
   * Gets the input stream.
   * 
   * @param session the session
   * @return the input stream
   * @throws Exception the exception
   */
  public InputStream getInputStream(Session session) throws Exception { 
    if(imageBytes != null) return new ByteArrayInputStream(imageBytes);  
    Node attachment;
    try{
      attachment = (Node)session.getItem(getId());  
    }catch (ItemNotFoundException e) {  
      return null;
    } catch (PathNotFoundException ex) {
      return  null;
    }
    Property property = attachment.getNode("jcr:content").getProperty("jcr:data");
    InputStream inputStream = property.getValue().getStream();
    return inputStream ;
  }
  
  /**
   * Gets the session.
   * 
   * @return the session
   * @throws Exception the exception
   */
  private Session getSession()throws Exception {
    RepositoryService repoService = (RepositoryService)PortalContainer
      .getInstance().getComponentInstanceOfType(RepositoryService.class);
    return repoService.getDefaultRepository().getSystemSession(workspace);
  }
}