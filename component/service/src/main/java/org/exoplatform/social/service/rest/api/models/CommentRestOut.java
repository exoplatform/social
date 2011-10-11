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
package org.exoplatform.social.service.rest.api.models;

import java.util.HashMap;

import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.service.rest.Util;

/**
 * The comment model for Social Rest APIs.
 * 
 * @author <a href="http://phuonglm.net">phuonglm</a>
 * @since 1.2.3
 */
public class CommentRestOut extends HashMap<String, Object>{
  public static enum Field {
    /**
     * Id of comment Activity
     */
    ID("id"),
    /**
     * Comment's test
     */
    TEXT("text"),
    /**
     * Identity of comment creator
     */
    POSTER_IDENTITY("posterIdentity"),
    /**
     * Long value present activity created time.
     */
    POSTED_TIME("postedTime"),
    /**
     * The human readable of posted time
     */
    CREATE_AT("createdAt");
    
   /**
    * String type.
    */
    private final String fieldName;

   /**
    * private constructor.
    *
    * @param string string type
    */
    private Field(final String string) {
      fieldName = string;
    }
    
    public String toString() {
      return fieldName;
    }
  }

  /**
   * Default constructor
   */
  public CommentRestOut(){
    initialize();
  }
  
  /**
   * Construct Comments model ExoSocialActivity
   * @param commentActivity
   */
  public CommentRestOut(ExoSocialActivity commentActivity, String portalContainerName){
    initialize();
    this.setId(commentActivity.getId());
    this.setPosterIdentity(new IdentityRestOut(commentActivity.getUserId(),portalContainerName));
    this.setText(commentActivity.getTitle());
    this.setPostedTime(commentActivity.getPostedTime());
    this.setCreatedAt(Util.convertTimestampToTimeString(commentActivity.getPostedTime()));  
  }
  
  public String getId() {
    return (String) this.get(Field.ID.toString());
  }
  public void setId(String id) {
    if(id != null){
      this.put(Field.ID.toString(), id);
    } else {
      this.put(Field.ID.toString(), "");
    }
  }
  
  public IdentityRestOut getPosterIdentity(){
    return (IdentityRestOut) this.get(Field.POSTER_IDENTITY.toString());
  }
  
  public void setPosterIdentity(IdentityRestOut posterIdentity) {
    if(posterIdentity != null){
      this.put(Field.POSTER_IDENTITY.toString(), posterIdentity);
    } else {
      this.put(Field.POSTER_IDENTITY.toString(), new HashMap<String, Object>());
    }
  }  
  
  public void setPosterIdentity(ExoSocialActivity commentActivity,String portalContainerName) {
    if(commentActivity != null){
      this.put(Field.POSTER_IDENTITY.toString(), new IdentityRestOut(commentActivity.getUserId(), portalContainerName));
    } else {
      this.put(Field.POSTER_IDENTITY.toString(), new HashMap<String, Object>());
    }
  }  
  
  public String getText() {
    return (String) this.get(Field.TEXT.toString());
  }
  public void setText(String text) {
    if(text != null){
      this.put(Field.TEXT.toString(), text);
    } else {
      this.put(Field.TEXT.toString(), "");
    }
  }
  
  public Long getPostedTime() {
    return (Long) this.get(Field.POSTED_TIME.toString());
  }
  public void setPostedTime(Long postedTime) {
    if(postedTime != null){
      this.put(Field.POSTED_TIME.toString(), postedTime);
    } else {
      this.put(Field.POSTED_TIME.toString(), new Long(0));
    }
  }
  
  public String getCreatedAt() {
    return (String) this.get(Field.CREATE_AT.toString());
  }
  public void setCreatedAt(String createdAt) {
    if(createdAt != null){
      this.put(Field.CREATE_AT.toString(), createdAt);
    } else {
      this.put(Field.CREATE_AT.toString(), "");
    }
  }
  
  private void initialize(){
    this.setId("");
    this.setPostedTime(new Long(0));
    this.setPosterIdentity(null);
    this.setText("");
    this.setCreatedAt("");
  }
}
