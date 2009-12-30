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
package org.exoplatform.social.space;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.Session;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.RepositoryService;

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
  private String id;
  private String fileName;
  private String mimeType;
  private String workspace;
  private byte[] imageBytes;
  private long lastModified;
  private static final int KB_SIZE = 1024;
  private static final int MB_SIZE = 1024 * KB_SIZE;
  
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
  public String getId() { return id; }
  public void   setId(String s) { id = s; }
  
  public String getWorkspace() { return workspace; }
  public void setWorkspace(String ws) { workspace = ws; }
  
  public String getFileName()  { return fileName; }
  public void   setFileName(String s) { fileName = s; }
  
  public String getMimeType() { return mimeType; }
  public void setMimeType(String s) { mimeType = s;}
  public byte[] getImageBytes() { return imageBytes;}
  public long getLastModified() { return lastModified;}
  public void setLastModified(long lastModified) { this.lastModified = lastModified;}
/**
   * get images size in MB/ KB/ Bytes
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

  public void setInputStream(InputStream input) throws Exception {
    if (input != null) {
      imageBytes = new byte[input.available()]; 
      input.read(imageBytes);
    }
    else imageBytes = null;
  }
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
  
  private Session getSession()throws Exception {
    RepositoryService repoService = (RepositoryService)PortalContainer
      .getInstance().getComponentInstanceOfType(RepositoryService.class);
    return repoService.getDefaultRepository().getSystemSession(workspace);
  }
}