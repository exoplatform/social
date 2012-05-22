/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.social.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.IDN;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.Validate;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Author : hanhvq@exolpatform.com
 * May 10, 2011 
 * @since 1.2.0-GA 
 */
public class Util {
  public static final String YOUTUBE_URL_OEMBED = "http://www.youtube.com/oembed?url=%s&format=json";
  public static final String YOUTUBE_REGEX = "^http://\\w{0,3}.?youtube+\\.\\w{2,3}/watch\\?v=[\\w-]{11}";
  public static final String JSON_URL = "url";
  
  private static final String MSG_LINK_MUST_BE_NOT_NULL = "Link must be not null";
  private static final String MSG_INVALID_VIDEO_LINK = "Invalid video link";
  
  private static final String SPACE_STRING = " ";
  private static final String HTTP = "http";
  private static final String HTTP_PRTOCOL = "http://";
  private static final Pattern URL_PATTERN = Pattern.compile("^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)"
    + "(\\?([^#]*))?(#(.*))?");
  
  private static final int DOMAIN = 4;
  
  /**
   * Checks a url is in a valid form or not.
   * 
   * @param link
   * @return
   */
  public static boolean isValidURL(String link) {
    try {
      if ((link == null) || (link.length() == 0)) return false;
      
      // Check the case that url has the domain name in the right form. Exg: .com, .org... or not.
      if (link.indexOf('.') == -1) return false;
      
      if (!URL_PATTERN.matcher(link).matches()) return false;
      
      if (hasValidDomain(link)) return true;
      
      URI uri = null;
      uri = new URI(IDN.toUnicode(link));

      String scheme = uri.getScheme();
      if (scheme == null) {
        link = HTTP_PRTOCOL + link;
        uri = new URI(IDN.toUnicode(link));
      }
      
      String host = uri.getHost();
      if ((host != null) && (host.contains(SPACE_STRING))) return false;
      
      uri.toURL();
    } catch (URISyntaxException e) {
      return false;
    } catch (MalformedURLException e) {
      return false;
    } catch (IllegalArgumentException e) {
      return false;
    }
    return true;
  }
  
  /**
   * Checks if a provided link is an youtube video link.
   * 
   * @param link the provided link
   * @return true if the provided link is an youtube video link, otherwise return false.
   * @since 4.0.0
   */
  public static boolean isYoutubeLink(String link) throws Exception {
    Validate.notNull(link, MSG_LINK_MUST_BE_NOT_NULL);
    Pattern pattern = Pattern.compile(YOUTUBE_REGEX);
    return pattern.matcher(link).find();
  }
  
  /**
   * Get Oembed data in JSON format from URL.
   * 
   * @param url
   * @return Oembed data in JSON format.
   * @throws Exception
   * @since 4.0.0.
   */
  public static JSONObject getOembedData(String url) throws Exception {
    url = url.replaceAll(":", "%3A").replaceAll("=", "%3D");
    JSONObject jsonOEmbed = null;
    try {
      String oembedURL = getOembedURL(url);
      jsonOEmbed = jsonRequest(new URL(oembedURL));
      jsonOEmbed.put(JSON_URL, url);
    } catch (IOException e) {
      throw new Exception(MSG_INVALID_VIDEO_LINK);
    } catch (JSONException e) {
      throw new Exception(MSG_INVALID_VIDEO_LINK);
    }
    return jsonOEmbed;
  }
  
  /**
   * Request URL to get return JSONObject.
   * 
   * @param url
   * @return Response data in JSON format from requested URL.
   * @throws IOException
   * @throws JSONException
   */
  private static JSONObject jsonRequest(URL url) throws IOException, JSONException {
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));
    StringBuffer stringBuffer = new StringBuffer();
    String temp = null;
    
    while ((temp = bufferedReader.readLine()) != null) {
      stringBuffer.append(temp);
    }
    
    bufferedReader.close();
    return new JSONObject(stringBuffer.toString());
  }
  
  /**
   * Get OembedURL from input URL.
   * 
   * @param url
   * @return OembedURL
   * @throws Exception
   */
  private static String getOembedURL(String url) throws Exception {
    if(isYoutubeLink(url.replaceAll("%3A", ":").replaceAll("%3D", "="))) {
      return String.format(YOUTUBE_URL_OEMBED,url);
    }
    return null;
  }
  
  private static boolean hasValidDomain(String link) {
    
    if (!link.startsWith(HTTP)) link = HTTP_PRTOCOL + link;
    
    Matcher m = URL_PATTERN.matcher(link);
    
    if (m.matches()) {
        String domain = m.group(DOMAIN);
        if ((domain != null) && (!domain.contains(SPACE_STRING))) return true;
    }
    return false;
  }
}
