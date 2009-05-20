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

import org.exoplatform.gadgets.WebFileEditor.client.WebFileEditor;
import org.exoplatform.gadgets.WebFileEditor.client.view.editor.ContentEditor;
import org.exoplatform.gadgets.WebFileEditor.client.view.editor.codemirror.GroovyEditor;
import org.exoplatform.gadgets.WebFileEditor.client.view.editor.codemirror.XMLEditor;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:dmitry.ndp@exoplatform.com.ua">Dmytro Nochevnov</a>
 * @version $Id: $
*/
public class FileModel {

  /**
   * The constant denotes document with unknown type .
   */
  public static final int UNKNOWN = 0;

  /**
   * The constant denotes Text-document.
   */
  public static final int TEXT = 1;

  /**
   * The constant denotes XML-document.
   */
  public static final int XML = 2;    
  
  /**
   * The constant denotes Groovy-script.
   */
  public static final int GROOVY = 3;

  public static ContentEditor getContentEditor( int fileType, WebFileEditor mainPanel ) {
    switch ( fileType ) {
      case XML: return new XMLEditor(mainPanel); 
      case GROOVY: return new GroovyEditor(mainPanel);
    }

    return null;
  }
  
  public static int getFileTypeOnFileExtension(String fileExtension) {
    if ( fileExtension.equals("txt") ) return TEXT;
    if ( fileExtension.equals("xml") ) return XML;
    if ( fileExtension.equals("groovy") ) return GROOVY;      
    
    return UNKNOWN;
  }

  public static String getFileExtensionOnFileType(int fileType) {
    switch ( fileType ) {
      case TEXT: return "txt";
      case XML: return "xml"; 
      case GROOVY: return "groovy";
    }
  
    return "text/plain";
  }  
  
  public static int getFileTypeOnMIMEType(String MIME) {
    if ( MIME.equals("text/plain") ) return TEXT;
    if ( MIME.equals("text/xml") ) return XML;     
    if ( MIME.equals("script/groovy") ) return GROOVY;

    return UNKNOWN;
  }

  public static String getMIMETypeOnFileType(int fileType) {
    switch ( fileType ) {
      case TEXT: return "text/plain";
      case XML: return "text/xml";
      case GROOVY: return "script/groovy";
      default: return "text/plain";
    }
  }    
  
  public static String getSampleContent( int fileType ) {
    switch ( fileType ) {
      case XML:
        return "<?xml version='1.0' encoding='UTF-8'?>\n"
          + "<D:response>\n"
          + "<D:href>\n"
          + "http://localhost:8080/rest/private/jcr/repository/collaboration/test.txt\n"
          + "</D:href>\n"
          + "<D:propstat>\n"
          + "<D:prop>\n"
          + "<D:resourcetype />\n"
          + "<D:getlastmodified b:dt='dateTime.rfc1123'>Tue, 28 Apr 2009\n"
          + "16:12:54 GMT</D:getlastmodified>\n"
          + "<D:displayname>test.txt</D:displayname>\n"
          + "<D:creationdate b:dt='dateTime.tz'>2009-04-28T16:12:54Z\n"
          + "</D:creationdate>\n"
          + "<D:getcontenttype>text/plain</D:getcontenttype>\n"
          + "<D:getcontentlength>8</D:getcontentlength>\n"
          + "</D:prop>\n"
          + "<D:status>HTTP/1.1 200 OK</D:status>\n"
          + "</D:propstat>\n"
          + "</D:response>\n";

      case GROOVY:
        return "// simple groovy script\n"
          + "import javax.ws.rs.Path\n"
          + "import javax.ws.rs.GET\n"
          + "import javax.ws.rs.PathParam\n\n"
          + "@Path(\"/\")\n"
          + "public class HelloWorld {\n"
          + "  @GET\n"
          + "  @Path(\"helloworld/{name}\")\n"
          + "  public String hello(@PathParam(\"name\") String name) {\n"
          + "    return \"Hello \" + name\n"
          + "  }\n"
          + "}\n";

      default:
        return "";
    }     
  }

  // getEncodingCharsetOnContentType("text/plain; charset=UTF-8") returns "UTF-8"
  public static native String getEncodingCharsetOnContentType(String HTTPContentType) /*-{
    if ( HTTPContentType.match(/charset=([^;]*)/i) != null ) {
      return HTTPContentType.match(/charset=([^;]*)/i)[1];
    } else {
      return null;
    }    
  }-*/;
  
  // getEncodingCharsetOnContentType("text/plain; charset=UTF-8") returns "text/plain"  
  public static native String getMIMETypeOnContentType(String HTTPContentType) /*-{
    if ( HTTPContentType.match(/([^;]*)/) != null ) {
      return HTTPContentType.match(/([^;]*)/)[0];
    } else {
      return null;
    }    
  }-*/;

  // getNameOnUrl("http://test/") returns "test"; getNameOnUrl("http://test/file.txt") returns "file.txt"; getNameOnUrl("file.txt") returns "file.txt" 
  public static native String getNameOnUrl(String url) /*-{
    if ( url.match(/^(.*)[\/]+([^\/]+)[\/]*$/) != null ) {
      return url.match(/^(.*)[\/]+([^\/]+)[\/]*$/)[2]
    } else {
      return url;
    }
  }-*/;

  // getPathOnUrl("http://test/folder/") returns "http://test/folder"; getPathOnUrl("http://test/file.txt") returns "http://test"; getPathOnUrl("file.txt") returns ""
  public static native String getPathOnUrl(String url) /*-{
    if ( url.match(/^(.*)[\/]+([^\/]*)$/) != null ) {
      return url.match(/^(.*)[\/]+([^\/]*)$/)[1]
    } else {
      return "";
    }
  }-*/;
  
  public static String getContentTypeOnMIMEAndCharset(String currentFileMIMEType, String currentFileEncodingCharset) {
    return  currentFileMIMEType + "; charset=" + currentFileEncodingCharset;
  }
  
  // isFolder("http://test/folder/") returns true; isFolder("http://test/file") returns false
  public static boolean isFolder(String url) {
    return (url.lastIndexOf("/") == (url.length() - 1)); // test if url is ended with "/"
  }
  
  // checkFileName(null), or checkFileName(""), or checkFileName("file/") returns "false". Otherwise checkFileName() returns "true". 
  public static boolean checkFileName(String fileName) {
    if (fileName == null || fileName.length() < 1)
      return false;

    if (fileName.lastIndexOf("/") == fileName.length() - 1 ) // test if last symbol is "/"
      return false;

    return true;
  }  
}