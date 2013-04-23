/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
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
package org.exoplatform.social.common.embedder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * @since 4.0.0-GA  
 */

public class YoutubeEmbedder implements Embedder {
  private static final Log LOG = ExoLogger.getLogger(YoutubeEmbedder.class); 
  
  private static final Pattern YOUTUBE_ID_PATTERN = Pattern
      .compile("(youtu\\.be\\/|youtube\\.com\\/(watch\\?(.*&)?v=|(embed|v)\\/))([^\\?&\"'>]+)");
  private static final Pattern CONTENT_URL_PATTERN = Pattern
      .compile(".*youtube\\.com\\/v\\/([^\\&\\?\\/]+)");

  private Map<Pattern,String> schemeEndpointMap;
  private Pattern youTubeURLPattern;
  private String url;
  
  /**
   * constructor
   * @param initParams
   */
  public YoutubeEmbedder(InitParams initParams) {
    schemeEndpointMap = new HashMap<Pattern,String>();
    Iterator<ValueParam> it = initParams.getValueParamIterator();
    ValueParam valueParam = null;
    while(it.hasNext()) {
      valueParam = it.next();
      String youTubeReg = valueParam.getName().replaceAll("&amp;", "&");
      String youTubeFeedsURL = valueParam.getValue().replaceAll("&amp;", "&");
      youTubeURLPattern = Pattern.compile(youTubeReg);
      schemeEndpointMap.put(youTubeURLPattern, youTubeFeedsURL);
    }
  }
  
  public Pattern getYouTubeURLPattern() {
    return youTubeURLPattern;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * Processes input link and returns data wrapped into a model called ExoSocialMedia.
   * 
   * @return ExoSocialMedia object that corresponds to the link.
   */
  public ExoSocialMedia getExoSocialMedia() {
    String feedsURL = null;
    for(Pattern pattern : schemeEndpointMap.keySet()) {
      Matcher matcher = pattern.matcher(url);
      if(matcher.find()) {
        feedsURL = schemeEndpointMap.get(pattern);
      } else {
        return null;
      }
    }
    
    //
    BufferedReader bufferedReader;
    JSONObject jsonObject = null;
    
    try {
      Matcher matcher = YOUTUBE_ID_PATTERN.matcher(url);
      
      String youtubeId = null;
      while (matcher.find()) {
        youtubeId = matcher.group(5);
      }
       
      String youTubeFeedURL = String.format(feedsURL, youtubeId);
      URL reqURL = new URL(youTubeFeedURL);
      bufferedReader = new BufferedReader(new InputStreamReader(reqURL.openStream()));
      StringBuffer stringBuffer = new StringBuffer();
      String eachLine = null;
      
      while ((eachLine = bufferedReader.readLine()) != null) {
        stringBuffer.append(eachLine);
      }
      
      bufferedReader.close();
      jsonObject = new JSONObject(stringBuffer.toString());
      JSONObject entryObject = jsonObject.getJSONObject("entry");
      
      //
      String html = buildHtmlInfo(entryObject.getJSONObject("content"));
      
      if (html == null) return null;
    
      ExoSocialMedia mediaObject = new ExoSocialMedia();
    
      
      JSONObject mediaGroupObject = entryObject.getJSONObject("media$group"); 
      mediaObject.setTitle(entryObject.getJSONObject("title").getString("$t"));
      mediaObject.setHtml(html);
      mediaObject.setDescription(mediaGroupObject.has("media$description") ? mediaGroupObject
                 .getJSONObject("media$description").getString("$t") : "");
      
      return mediaObject;    
    } catch (JSONException e) {
      LOG.debug("Any syntax error cause to JSON exception.", e);
      return null;
    } catch (IOException e) {
      LOG.debug("Problem with IO when open url.", e);
      return null;
    } catch (Exception e) {
      LOG.debug("Problem occurred when get data from youtube link.", e);
      return null;
    }
  }
  
  private String buildHtmlInfo(JSONObject contentObject) throws JSONException {
    String videoContentURL = contentObject.getString("src");
    
    //
    Matcher matcher = CONTENT_URL_PATTERN.matcher(videoContentURL);
    
    String contentSrc = null;
    while (matcher.find()) {
      contentSrc = matcher.group(0);
    }

    if (contentSrc == null) {
      LOG.info("Returned content url not match the pattern to get content source.");
      return null;  
    }
    
    String videoPlayerType = contentObject.getString("type");
    StringBuilder contentURL = new StringBuilder();
    
    contentURL.append("<embed ")
              .append("width=\"420\" height=\"345\" ")
              .append("src=\"")
              .append(contentSrc)
              .append("\" type=\"")
              .append(videoPlayerType)
              .append("\">")
              .append("</embed>");
    
    return contentURL.toString();
  }
}
