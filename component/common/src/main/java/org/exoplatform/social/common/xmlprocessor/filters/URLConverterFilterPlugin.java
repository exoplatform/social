/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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

package org.exoplatform.social.common.xmlprocessor.filters;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.xmlprocessor.BaseXMLFilterPlugin;
import org.exoplatform.social.common.xmlprocessor.DOMParser;
import org.exoplatform.social.common.xmlprocessor.Tokenizer;
import org.exoplatform.social.common.xmlprocessor.model.Attributes;
import org.exoplatform.social.common.xmlprocessor.model.Node;

/**
 * This URLConverterFilterPlugin is a plugin for XMLProcessor which will auto convert any detected text link into real links,
 * for example:
 * http://abc.com => <a href="http://abc.com" title="http://abc.com">http://abc.com</a>
 * Moreover, this plugin can trim any link to a fix size when it's too long by specifying the urlMaxLength via init params.
 * the urlMaxLength = url.length() + "...".length(), if urlMaxLength less than 3 the filter will no trim the url for display.
 *   
 * @author Ly Minh Phuong - http://phuonglm.net
 * @since 1.2.2
 */
public class URLConverterFilterPlugin extends BaseXMLFilterPlugin {
  private int urlMaxLength = -1;
  private static final Log LOG = ExoLogger.getLogger(URLConverterFilterPlugin.class);
  
  private static final Pattern pattern = Pattern
      .compile("(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
  
  /**
   * Constructor with max length of URL, if url.length > UrlMaxLength then the url display will be shorten.
   * @param urlMaxLength
   */
  public URLConverterFilterPlugin(int urlMaxLength){
    this.urlMaxLength = urlMaxLength;
  }
  
  /**
   * Constructor with InitParams from container, if url.length > UrlMaxLength then the url display will be shorten.
   * @param urlMaxLength
   */
  public URLConverterFilterPlugin(InitParams params){
    try {
      urlMaxLength = Integer.valueOf(params.getValueParam("urlMaxLength").getValue());
    } catch (Exception e) {
      LOG.warn("an <value-param> 'urlMaxLength' of type int is needed for this component " + getClass());
    }
  }
 
  /**
   * {@inheritDoc}
   */
  @Override
  public Object doFilter(Object input) {
    if(input instanceof String){
      return convertURL((String) input);
    } else {
      return input;
    }
  }
  /**
   * Gets max length of URL, if url.length > urlMaxLength and urlMaxLength >= 3 then the url display will be shorten
   * @return
   */
  public int getUrlMaxLength() {
    return urlMaxLength;
  }
  
  /**
   * Sets max length of URL, if url.length > urlMaxLength and urlMaxLength >= 3 then the url display will be shorten.
   * @param urlMaxLength
   */
  public void setUrlMaxLength(int urlMaxLength) {
    this.urlMaxLength = urlMaxLength;
  }

  private String convertURL(String xmlString){
    List<String> xmlTokens = Tokenizer.tokenize(xmlString);
    Node rootNode = DOMParser.createDOMTree(xmlTokens);
    nodeFilter(rootNode);
    return rootNode.toString();
  }

  private int nodeFilter(Node currentNode) {
    LinkedList<Node> currentChildNode = currentNode.getChildNodes();
    if (currentNode.getTitle().isEmpty()) {
      int insertedCount = convertNode(currentNode);
      if(insertedCount > 0){
        return insertedCount;
      }
    }
    
    for (int i = 0; i < currentChildNode.size(); i++) {
      if(!currentChildNode.get(i).getTitle().equals("a")){
        int insertedCount = nodeFilter(currentChildNode.get(i));
        if( insertedCount > 0){
          i = i + insertedCount;
        }
      }
    }
    return 0;
  }
  
  private int convertNode(Node currentNode){
    String content = StringEscapeUtils.unescapeHtml(currentNode.getContent());
    int insertedCount = 0; // if found for delete the original after parsing

    if(!currentNode.isRootNode()){
      Node currentProccessNode = currentNode;
      Node parrentNode = currentNode.getParentNode();
      
      int lastMatch = 0;
      
      Matcher m = pattern.matcher(content);
      
      while (m.find()) {
        String url = content.substring(m.start(), m.end());
        
        // case when string like this: "abc http://xyz" so we must create text node abc
        if(m.start() > lastMatch){
          Node textNode = new Node();
          textNode.setContent(StringEscapeUtils.escapeHtml(content.substring(lastMatch, m.start())));
          
          parrentNode.insertAfter(currentProccessNode, textNode);
          currentProccessNode = textNode;
          insertedCount++;
        }
        
        // create <a> node
        Node aHrefNode = new Node();
        aHrefNode.setTitle("a");
        aHrefNode.setParentNode(parrentNode); // set parent of <a>
        
        // set attribute for <a> 
        Attributes aHrefNodeAttributes = new Attributes();
        aHrefNodeAttributes.put("href", url);
        aHrefNodeAttributes.put("target", "_blank");
        aHrefNode.setAttributes(aHrefNodeAttributes);
        
        // Create text node for <a>, text node inside <a> and position is 0
        Node aHrefContentNode = new Node();
        aHrefContentNode.setContent(StringEscapeUtils.escapeHtml(shortenURL(url)));
        aHrefContentNode.setParentNode(aHrefNode);
        aHrefNode.addChildNode(aHrefContentNode);
        
        // insert <a> to the child list of parrentNode
        parrentNode.insertAfter(currentProccessNode, aHrefNode);
        currentProccessNode = aHrefNode;
        
        lastMatch = m.end(); // update the lastMatch of 
        insertedCount++;
      }
      if((lastMatch + 1) < content.length()){
        Node textNode = new Node();
        textNode.setContent(StringEscapeUtils.escapeHtml(content.substring(lastMatch, content.length())));
        
        parrentNode.insertAfter(currentProccessNode, textNode);
        currentProccessNode = textNode;
        insertedCount++;        
      }
      if(insertedCount > 0){
        parrentNode.getChildNodes().remove(currentNode);
      }
    }
    return insertedCount;
  }
  
  private String shortenURL(String url) {
    if (urlMaxLength < 3 || url.length() < urlMaxLength)
      return url;
    else {
      return url.substring(0, urlMaxLength - 3) + "...";
    }
  }
}
