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
package org.exoplatform.social.service.rest;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.parser.XMLDocumentFilter;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLParserConfiguration;
import org.cyberneko.html.HTMLConfiguration;
import org.cyberneko.html.filters.DefaultFilter;
import org.cyberneko.html.filters.ElementRemover;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.social.common.embedder.EmbedderFactory;
import org.exoplatform.social.common.embedder.ExoSocialMedia;

/**
 * LinkShare - gets preview information of a link including: 
 * - link
 * - title 
 * - description
 * - images 
 * 
 * - media (from popular sites: youtube, vimeo, flickr...) - low priority (NOT IMPLEMENTED YET)
 *    This should be implemented from the client side to display preview and media player.
 * <p>
 * In order to provide this preview, always looks for
 * the title of the page, a summary of the main content, and an image.
 * Looks for preview information by the following priority:
 * <p>
 * 1.
 * <pre> 
 * &lt;meta name="title" content="page_title" />
 * &lt;meta name="description" content="page_description" />
 * &lt;link rel="image_src" href="image_source" />
 * </pre>
 * <p>
 * 2.
 * If title not found -> find in <title> tag.
 * If description not found -> find first <p> tag. If no description -> return ""
 * If img_src not found -> find all images in page with max, min specified width + height
 * <p>
 * 3. 
 * To specify medium, use tag:
 * <pre>
 * &lt;meta name="medium" content="medium_type" />
 * </pre>
 * In which: medium_type can be "audio", "image", "video", "news", "blog" and "mult".
 * <p>
 * Created by The eXo Platform SEA
 * TODO: hoatle improvement:
 * + scans description with MIN_CHARACTER
 * + handles exception
 * + parser more faster with and without scanning image tag, stop right when things got.
 * 
 * @author <a href="mailto:hoatlevan@gmail.com">hoatle</a>
 * @since Oct 8, 2009
 * @see "http://activitystrea.ms/"
 * @see "http://www.facebook.com/share_partners.php"
 */
@XmlRootElement
public class LinkShare extends DefaultFilter {
  
  private final String MEDIUM_TYPE_NEWS = "news";
  private final String MEDIUM_TYPE_AUDIO = "audio";
  private final String MEDIUM_TYPE_IMAGE = "image";
  private final String MEDIUM_TYPE_VIDEO = "video";
  private final String MEDIUM_TYPE_BLOG = "blog";
  private final String MEDIUM_TYPE_MULT = "mult";
  
  private static final String IMAGE_MIME_TYPE = "image/";
  private static final String HTML_MIME_TYPE = "text/html";
  //default medium_type = "news"
  private String mediumType = MEDIUM_TYPE_NEWS;
  private String mediaSrc;
  private String mediaType;
  private String mediaTitle;
  private String mediaArtist;
  private String mediaAlbum;
  private String mediaHeight;
  private String mediaWidth;
  
  private static final String HTTP_PROTOCOL = "http://";
  private static final String HTTPS_PROTOCOL = "https://";
  
  //min with and height of images to get from img attributes in pixel.
  // With <img src="img_src" width="55px" height="55px" /> ~ <img src="img_src" width="55" height="55" />
  //if width="55pt" => with="55" ~ width="55px" (not correct but can be accepted) 
  private static final int MIN_WIDTH = 55;
  private static final int MIN_HEIGHT = 55;
  //maxium description length = 250 characters
  private static final int MAX_DESCRIPTION = 500;
  //default lang
  private static String lang = "en";
  private String   link;
  private String   title;
  private String   description;
  private String imageSrc;
  private List<String> images;
  private ExoSocialMedia mediaObject;
  //Collections of description with key as lang
  private HashMap<String, String> descriptions;
  //holds temporary string values from characters() method
  private String temp;
  //store all text from the first p tag
  private StringBuffer pText;
  //gets all the text from first p tag if no description meta and headEnded = true
  private boolean firstPTagParsed = false;
  //If on  p parsing, get all text from temp to pText
  private boolean onPParsing = false;
  
  // to mark the end of the head tag part ~ no more meta tag.
  // If no more meta tag (headEnded = true) and no description -> get description.
  private boolean headEnded = false;
  
  /**
   * Uses LinkShare.getInstance(String link) or 
   * LinkShare.getInstance(String  link, String lang)
   * for creating LinkShare object
   */
  private LinkShare() {

  }
  
  /**
   * gets provided link
   * @return provided link
   */
  public String getLink() {
    return this.escapeSpecialCharacters(this.link);
  }

  /**
   * gets title
   * @return title
   */
  public String getTitle() {
    return this.escapeSpecialCharacters(this.title);
  }
  
  /**
   * Set new value for title.
   * @param title
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * gets description
   * @return description
   */
  public String getDescription() {
    return this.escapeSpecialCharacters(this.description);
  }

  /**
   * Set new value for description.
   * @param description
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * gets images list
   * @return images
   */
  public List<String> getImages() {
    return images;
  }
  
  /**
   * gets mediumType
   * @return mediumType
   */
  public String getMediumType() {
    return mediumType;
  }
  
  
  /**
   * gets mediaSrc
   * @return mediaSrc
   */
  public String getMediaSrc() {
    return mediaSrc;
  }
  
  /**
   * gets mediaType if provided in:
   * <pre>
   *  &lt;meta name="audio_type" content="Content-Type header field" /&gt;
   * </pre>
   * or:
   * <pre>
   *  &lt;meta name="video_type" content="Content-Type header field" /&gt;
   * </pre>
   * @return mediaType
   */
  public String getMediaType() {
    return mediaType;
  }
  
  /**
   * gets mediaTitle if provided in:
   * <pre>
   *  &lt;meta name="audio_title" content="audio_title_name" /&gt;
   * </pre>
   * @return mediaTitle
   */
  public String getMediaTitle() {
    return mediaTitle;
  }
  
  
  /**
   * gets mediaArtist if provided in:
   * <pre>
   *  &lt;meta name="audio_artist" content="audio_artist_name" /&gt;
   * </pre>
   * @return mediaArtist
   */
  public String getMediaArtist() {
    return mediaArtist;
  }
  
  /**
   * gets mediaAlbum if provided in:
   * <pre>
   *  &lt;meta name="audio_album" content="audio_album_name" /&gt;
   * </pre>
   * @return mediaAlbum
   */
  public String getMediaAlbum() {
    return mediaAlbum;
  }
  
  /**
   * gets mediaHeight if provided in:
   * <pre>
   *  &lt;meta name="video_height" content="video_height_value" /&gt;
   * </pre>
   * @return mediaHeight;
   */
  public String getMediaHeight() {
    return mediaHeight;
  }
  
  /**
   * gets mediaWidth if provided in:
   * <pre>
   *  &lt;meta name="video_width" content="video_width_value" /&gt;
   * </pre>
   * @return mediaWidth
   */
  public String getMediaWidth() {
    return mediaWidth;
  }
  /**
   * get mediaObject
   * @return
   */
  public ExoSocialMedia getMediaObject() {
    return mediaObject;
  }
  
  /**
   * Gets information of the provided link by using remover filter,
   * using call back filter methods to get desired information.
 * @param encoding 
   */
  private void get(String encoding) throws Exception {
    //Creates element remover filter
    ElementRemover remover = new ElementRemover();
    remover.acceptElement("head", null);
    remover.acceptElement("meta", new String[] {"name", "content", "lang"});
    remover.acceptElement("link", new String[] {"rel", "href"});
    remover.acceptElement("title", null);
    remover.acceptElement("img", new String[] {"src", "width", "height"});
    remover.acceptElement("p", null);
    //accepts more tags to get text from a <p> tag
    remover.acceptElement("a", null);
    remover.acceptElement("b", null);
    remover.acceptElement("i", null);
    remover.acceptElement("strong", null);
    
    remover.removeElement("script");
    //Sets up filter chain
    XMLDocumentFilter[] filter = {
        remover
    };
    XMLParserConfiguration parser = new HTMLConfiguration();
    parser.setProperty("http://cyberneko.org/html/properties/default-encoding", "UTF-8");
    parser.setProperty("http://cyberneko.org/html/properties/filters", filter);
    parser.setDocumentHandler(this);
    XMLInputSource source = new XMLInputSource(null, link, null);
    source.setEncoding(encoding);
    try {
      parser.parse(source);
    } catch (NullPointerException ne) {
      ExoLogger.getLogger(LinkShare.class)
        .warn("Problem when parsing the link in LinkShare.getInstance(String) method");
    } catch (IOException e) {
      // Process as normal behavior in case the link is in the valid form
      // but have been blocked or some other same reasons.
      this.title = link;
    } catch (Exception e) {
      this.title = link;
    }
  }
  
  /**
   * Gets LinkShare instance with specified link. The default lang = "en"
   * @param link
   * @return LinkShare instance
   * @throws Exception 
   */
  public static LinkShare getInstance(String link) throws Exception {
    return getInstance(link, lang);
  }
  
  /**
   * Gets LinkShare instance with link and lang specified.
   * @param link
   * @param lang
   * @return LinkShare instance
   * @throws Exception 
   */
  public static LinkShare getInstance(String link, String lang) throws Exception {
    if (link == null)
      return null;
    if (!Util.isValidURL(link))
      return null;
    
    if (!(link.toLowerCase().startsWith(HTTP_PROTOCOL) || link.toLowerCase().startsWith(HTTPS_PROTOCOL))) {
        link = HTTP_PROTOCOL + link;
    }
    
    LinkShare linkShare = new LinkShare();
    linkShare.link = link;
    LinkShare.lang = lang;
    
    linkShare.mediaObject = EmbedderFactory.getInstance(link).getExoSocialMedia();  
    
    // if there is no media object, processes link to get page metadata
    if(linkShare.mediaObject == null) {
      String mimeType = org.exoplatform.social.service.rest.Util.getMimeTypeOfURL(link);
      if(mimeType.toLowerCase().startsWith(IMAGE_MIME_TYPE)){
        linkShare.images = new ArrayList<String>(0);
        linkShare.images.add(link);
        linkShare.description = "";
      } else if(mimeType.toLowerCase().startsWith(HTML_MIME_TYPE)){
        String encoding = (mimeType.contains("charset=")) ? mimeType.split("charset=")[1] : "UTF-8"; 
        linkShare.get(encoding);
      } else {
        linkShare.images = new ArrayList<String>(0);
        linkShare.description = "";
      }
      
      if ((linkShare.title == null) || (linkShare.title.trim().length() == 0)) linkShare.title = link;
      
      //If image_src detected from meta tag, sets this image_src to images
      if (linkShare.imageSrc != null) {
        List<String> images = new ArrayList<String>();
        images.add(linkShare.imageSrc);
        linkShare.images = images;
      }
      //gets desired description by lang when there are many description meta name with different lang
      HashMap<String, String> descriptions = linkShare.descriptions;
      if (descriptions != null) {
        String description = descriptions.get(LinkShare.lang);
        if (description == null) {
         Collection<String> values = descriptions.values();
         //get the first value in the collection
         description = values.iterator().next();
        }
        linkShare.description = description;
        //gets with maximum characters only
        String tail = "";
        if (description.length() > MAX_DESCRIPTION) {
          tail = "...";
          linkShare.description = description.substring(0, MAX_DESCRIPTION - 1) + tail;
        }
      }
      if (linkShare.description == null) linkShare.description = "";
      if (linkShare.images == null) {
        linkShare.images = new ArrayList<String>();
      }
    }
    return linkShare;
  }
  
  /**
   * filter method is called back when scanning meets start element tag
   */
  public void startElement(QName element, XMLAttributes attrs, Augmentations augs) {
    if (headEnded == true && descriptions == null) {
      if (firstPTagParsed == false) {
        if ("p".equalsIgnoreCase(element.rawname)) {
          firstPTagParsed = true;
          onPParsing = true;
        }
      }
    } else if ("title".equalsIgnoreCase(element.rawname)) {
      onPParsing = true;
    }
  }

  
  /**
   * filter method is called back when scanning meets end element tag
   */
  public void endElement(QName element, Augmentations augs) {
    //System.out.println("( " + element.rawname);
    //if end of title -> set temporary title;
    //if detect <meta name="title" content="meta_title" />, reset title
      if ("title".equalsIgnoreCase(element.rawname)) {
        if (title == null) {
          if (onPParsing) {
            title = pText.toString();
            onPParsing = false;
            pText = null;
          } else {
            title = temp;
          }
        }
      }
    //set headEnded
    if ("head".equalsIgnoreCase(element.rawname)) {
      headEnded = true;
    }
    //Set end of p tag
    if (onPParsing == true) {
      if ("p".equalsIgnoreCase(element.rawname)) {
        onPParsing = false;
        description = pText.toString();
      }
    }
  }
  
  /**
   * this filter method is called back when scanning meets empty element tag
   */
  public void emptyElement(QName element, XMLAttributes attributes, Augmentations augs) {
    if("link".equalsIgnoreCase(element.rawname)) { //process link tag
      String relValue;
      String hrefValue;
      relValue = attributes.getValue("rel");
      hrefValue = attributes.getValue("href");
      if (hrefValue != null) hrefValue = getAbsLink(hrefValue);
      if ("image_src".equalsIgnoreCase(relValue)) {
        imageSrc = hrefValue;
      } else if ("audio_src".equalsIgnoreCase(relValue)) {
        mediaSrc = hrefValue;
        mediumType = MEDIUM_TYPE_AUDIO;
      } else if ("video_src".equalsIgnoreCase(relValue)) {
        mediaSrc = hrefValue;
        mediumType = MEDIUM_TYPE_VIDEO;
      }
    } else if ("meta".equalsIgnoreCase(element.rawname)) { //process meta tag
      String nameValue;
      String contentValue;
      nameValue = attributes.getValue("name");
      if (nameValue == null) return;
      contentValue = attributes.getValue("content");
      if (contentValue == null) return;
      //Set mediumType
      if ("medium".equalsIgnoreCase(nameValue)) {
        if ("news".equalsIgnoreCase(contentValue)) {
          mediumType = MEDIUM_TYPE_NEWS;
        } else if ("audio".equalsIgnoreCase(contentValue)) {
          mediumType = MEDIUM_TYPE_AUDIO;
        } else if ("image".equalsIgnoreCase(contentValue)) {
          mediumType = MEDIUM_TYPE_IMAGE;
        } else if ("video".equalsIgnoreCase(contentValue)) {
          mediumType = MEDIUM_TYPE_VIDEO;
        } else if ("blog".equalsIgnoreCase(contentValue)) {
          mediumType = MEDIUM_TYPE_BLOG;
        } else if ("mult".equalsIgnoreCase(contentValue)) {
         mediumType = MEDIUM_TYPE_MULT; 
        }
      } else if ("title".equalsIgnoreCase(nameValue)) {
        title = contentValue;
      } else if ("description".equalsIgnoreCase(nameValue)) {
        String langValue = attributes.getValue("lang");
        if (langValue != null) {
          if (descriptions == null) descriptions = new HashMap<String, String>();
          descriptions.put(langValue, contentValue);
        } else {
          description = contentValue;
        }
      }
      
      if (mediumType.equals(MEDIUM_TYPE_AUDIO) || mediumType.equals(MEDIUM_TYPE_MULT)) {
        if ("audio_type".equalsIgnoreCase(nameValue)) {
          mediaType = contentValue;
        } else if ("audio_title".equalsIgnoreCase(nameValue)) {
          mediaTitle = contentValue;
        } else if ("audio_artist".equalsIgnoreCase(nameValue)) {
          mediaArtist = contentValue;
        } else if ("audio_album".equalsIgnoreCase(nameValue)) {
          mediaAlbum = contentValue;
        }
      } else if (mediumType.equals(MEDIUM_TYPE_VIDEO) || mediumType.equals(MEDIUM_TYPE_MULT)) {
        if ("video_type".equalsIgnoreCase(nameValue)) {
          mediaType = contentValue;
        } else if ("video_title".equalsIgnoreCase(nameValue)) {
          mediaTitle = contentValue;
        } else if ("video_height".equalsIgnoreCase(nameValue)) {
          mediaHeight = contentValue;
        } else if ("video_width".equalsIgnoreCase(nameValue)) {
          mediaWidth = contentValue;
        } else if ("video_artist".equalsIgnoreCase(nameValue)) {
          mediaArtist = contentValue;
        } else if ("video_album".equalsIgnoreCase(nameValue)) {
          mediaAlbum = contentValue;
        }
      }
    } else if ((imageSrc == null) && ("img".equalsIgnoreCase(element.rawname))) { //process img tag
      String src = attributes.getValue("src");
      if (src == null) return;
      
      if (isAcceptableImg(src)) {
        src = getAbsLink(src);
        if (images == null) images = new ArrayList<String>();
        images.add(src);
      }
    }
  }
  
  
  /**
   * filter method is called back when scanning meets the end of text in a tag
   */
  public void characters(XMLString text, Augmentations augs) {
    temp = text.toString();
    if (onPParsing == true) {
      if (pText == null) pText = new StringBuffer();
      pText.append(temp);
    }
  }
  
  /**
   * Gets absolute link from the provided link
   * @param base
   * @param link
   * @return absolute link
   */
  private String getAbsLink(String link) {
    if (link.startsWith("http://")) return link;
    URL url = null;
    try {
      url = new URL(this.link);
    } catch (MalformedURLException e) {
      //Do nothing, this exception will never occur here
    }
    String protocol = url.getProtocol();
    String host = url.getHost();
    String base = protocol + "://" + host;
    if (link.startsWith("/")) {
      //Absolute
      return base + link;
    } else if (link.startsWith("./")) {
      if (this.link.endsWith("/")) {
        this.link = this.link.substring(0, this.link.length() - 1);
      }
      link = link.substring(1, link.length());
      return this.link + link;        
    } else if (link.startsWith("../")) {
      String regex = "\\.\\./";
      Pattern partern = Pattern.compile(regex);
      Matcher matcher = partern.matcher(link);
      int level = 0;
      while (matcher.find()) {
        level++;
      }
      String secondPath = link.replace("(\\.\\./)+", "");
      String[] str = this.link.split("/");
      StringBuffer sb = new StringBuffer();
      level = (str.length - 1) - level;
      for (int i = 0; i < level; i++) {
        sb.append(str[i]);
      }
      sb.append(secondPath);
      return sb.toString();
    } else {
      if (this.link.endsWith("/")) {
        return this.link + link;
      } else {
        return this.link + "/" + link;
      }
    }
  }
  
  /**
   * Escapes the special characters.
   * 
   * @param str
   * @return
   * @since 1.2.7
   */
  private String escapeSpecialCharacters(String str) {
    if (str != null) {
      return str.replaceAll("\r\n|\n\r|\n|\r", "");
    } else {
      return "";
    }
  }
  
  private boolean isAcceptableImg(String src) {
    BufferedImage img = null;
    try {
       img = ImageIO.read(new URL(src));
       int width = img.getWidth();
       int height = img.getHeight();
       return (width > MIN_WIDTH && height > MIN_HEIGHT);
    } catch (MalformedURLException e) {
      return false;
    } catch (IOException e) {
      return false;
    }
  }
}
