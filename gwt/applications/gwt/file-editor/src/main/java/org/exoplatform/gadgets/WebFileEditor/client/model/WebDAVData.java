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

package org.exoplatform.gadgets.WebFileEditor.client.model;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.RequestBuilder.Method;

/**
 * Created by The eXo Platform SAS.
 * 
 * Handlers of PropertiesPanel events
 * 
 * @author <a href="mailto:dmitry.ndp@exoplatform.com.ua">Dmytro Nochevnov</a>
 * @version $Id: $
*/
public class WebDAVData{

  public String webDAVURL;
  
  public WebDAVData(String webDAVURL) { 
    this.webDAVURL = webDAVURL;
  }

  private RequestBuilder getRequestBuilder(Method method, String url) {
    return new RequestBuilder(method, url);
  }  

  public void PROPFIND(String url, final PROPFINDHandler handler) {
    RequestBuilder builder = getRequestBuilder(RequestBuilder.POST, url);
    builder.setHeader("Authorization", "Basic cm9vdDpleG8=");
    builder.setHeader("X-HTTP-Method-Override", "PROPFIND");
    builder.setHeader("Depth", "1");
    
    try {
      builder.sendRequest(null, new RequestCallback() {
        public void onResponseReceived(Request request, Response response) {        
          handler.onSuccess(response.getStatusCode(), response.getText());
        }

        public void onError(Request request, Throwable e) {
          handler.onError(e);
        }
      });
    } catch (RequestException e) {
      handler.onError(e);
    }
  }
  
  public static boolean successStatus(int status) {
    return (status >= 200 && status < 300 ) || status == 304 || status == 1223;
  }

  public interface WebDAVHandler { }

  public interface PROPFINDHandler extends WebDAVHandler {
    void onSuccess(int status, String text);
    void onError(Throwable e);
  }

  public static String renderStatus(int status) {
    String msg = "";
    switch (status) {
      case 100: msg = "Continue"; break;
      case 101: msg = "Switching Protocols"; break;
      case 102: msg = "Processing"; break;
      case 200: msg = "OK"; break;
      case 201: msg = "Created"; break;
      case 202: msg = "Accepted"; break;
      case 203: msg = "None-Authoritive Information"; break;
      case 204: msg = "No Content"; break;
      case 1223: msg = "No Content"; break;
      case 205: msg = "Reset Content"; break;
      case 206: msg = "Partial Content"; break;
      case 207: msg = "Multi-Status"; break;
      case 300: msg = "Multiple Choices"; break;
      case 301: msg = "Moved Permanently"; break;
      case 302: msg = "Found"; break;
      case 303: msg = "See Other"; break;
      case 304: msg = "Not Modified"; break;
      case 305: msg = "Use Proxy"; break;
      case 307: msg = "Redirect"; break;
      case 400: msg = "Bad Request"; break;
      case 401: msg = "Unauthorized"; break;
      case 402: msg = "Payment Required"; break;
      case 403: msg = "Forbidden"; break;
      case 404: msg = "Not Found"; break;
      case 405: msg = "Method Not Allowed"; break;
      case 406: msg = "Not Acceptable"; break;
      case 407: msg = "Proxy Authentication Required"; break;
      case 408: msg = "Request Time-out"; break;
      case 409: msg = "Conflict"; break;
      case 410: msg = "Gone"; break;
      case 411: msg = "Length Required"; break;
      case 412: msg = "Precondition Failed"; break;
      case 413: msg = "Request Entity Too Large"; break;
      case 414: msg = "Request-URI Too Large"; break;
      case 415: msg = "Unsupported Media Type"; break;
      case 416: msg = "Requested range not satisfiable"; break;
      case 417: msg = "Expectation Failed"; break;
      case 422: msg = "Unprocessable Entity"; break;
      case 423: msg = "Locked"; break;
      case 424: msg = "Failed Dependency"; break;
      case 500: msg = "Internal Server Error"; break;
      case 501: msg = "Not Implemented"; break;
      case 502: msg = "Bad Gateway"; break;
      case 503: msg = "Service Unavailable"; break;
      case 504: msg = "Gateway Time-out"; break;
      case 505: msg = "HTTP Version not supported"; break;
      case 507: msg = "Insufficient Storage"; break;
    }
    return " with status: " + status + " (" + msg + ")";
  }
}