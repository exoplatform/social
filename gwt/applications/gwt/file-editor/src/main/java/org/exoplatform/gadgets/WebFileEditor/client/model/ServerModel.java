/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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

package org.exoplatform.gadgets.WebFileEditor.client.model;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;

/**
 * Created by The eXo Platform SAS        .
 * 
 * methods for client-server conversation
 * 
 * @version $Id: $
 */

public class ServerModel {
  
  
  /**
   * retrieves a file content
   * @param url of file
   * @param handler
   * @throws RequestException
   */
  public static void getFile(final String url, final FileDataHandler handler) throws RequestException {
    final RequestBuilder builder = new RequestBuilder( RequestBuilder.GET, url );   

    builder.sendRequest(null, new FileRequestCallback(handler));
  }
  
  /**
   * retrieves all of resource properties (infinity depth)
   * @param url
   * @param handler
   * @throws RequestException
   */
  public static void getAllProperties(final String url, final FileDataHandler handler) throws RequestException {
    final RequestBuilder builder = new RequestBuilder( RequestBuilder.GET, url );
    builder.setHeader("X-HTTP-Method-Override", "PROPFIND");
    builder.setHeader("Depth", "Infinity");
    
    builder.sendRequest(null, new FileRequestCallback(handler));
      
  }
  
  /**
   * retrieves child properties (depth=1)
   * @param url
   * @param handler
   * @throws RequestException
   */
  public static void getChildrenProperties(final String url, final FileDataHandler handler) throws RequestException {
    final RequestBuilder builder = new RequestBuilder( RequestBuilder.GET, url );
    builder.setHeader("X-HTTP-Method-Override", "PROPFIND");
    builder.setHeader("Depth", "1");
    
    builder.sendRequest(null, new FileRequestCallback(handler));  
  }
  
  /**
   * sets a content of file
   * @param url
   * @param handler
   * @param data
   * @param mimeType
   * @throws RequestException
   */
  public static void saveFile(final String url, final String content, final String MIMEtype, final FileDataHandler handler) throws RequestException {
    final RequestBuilder builder = new RequestBuilder( RequestBuilder.POST, url );
    builder.setHeader("X-HTTP-Method-Override", "PUT");
    builder.setHeader("Content-Type", MIMEtype);
    
    builder.sendRequest(content, new FileRequestCallback(handler));
  }

  
  /**
   * creates a folder
   * @param url
   * @param handler
   * @throws RequestException
   */
  public static void createFolder(final String url, final FileDataHandler handler) throws RequestException {
    final RequestBuilder builder = new RequestBuilder( RequestBuilder.POST, url );
    builder.setHeader("X-HTTP-Method-Override", "MKCOL");
    
    builder.sendRequest(null, new FileRequestCallback(handler));
  }

  /**
   * delete a folder or file
   * @param url
   * @param handler
   * @throws RequestException
   */
  public static void delete(final String url, final FileDataHandler handler) throws RequestException {
    final RequestBuilder builder = new RequestBuilder( RequestBuilder.POST, url );
    builder.setHeader("X-HTTP-Method-Override", "DELETE");
    
    builder.sendRequest(null, new FileRequestCallback(handler));
  }  
  
  private static class FileRequestCallback implements RequestCallback {
   
    private final FileDataHandler handler;
    
    public FileRequestCallback(FileDataHandler handler) {
      this.handler = handler;
    }
    
    public void onResponseReceived(Request request, final Response response) {
      if(success(response.getStatusCode())) {
        handler.onSuccess(request, response);
      } else {
        handler.onError(request, new RequestException(response.getStatusCode() + " " + response.getStatusText()));
      }
    }

    public void onError(Request request, Throwable e) {
      handler.onError(request, e);
    }
  }
  
  public interface FileDataHandler {
    void onSuccess(Request request, Response response);
    void onError(Request request, Throwable e);
  }
  
  public static boolean success(int status) {
    return (status >= 200 && status < 300 ) || status == 304 || status == 1223;
  }
}