package org.exoplatform.social.opensocial.model.impl;

import org.exoplatform.social.opensocial.model.Space;

/**
 * Created by The eXo Platform SARL
 * Author : dang.tung
 *          tungcnw@gmail.com
 * Jan 08, 2009          
 */


public class SpaceImpl implements Space {

  private String displayName;
  private String id;
  
  public SpaceImpl() {
    
  }
  
  public String getDisplayName() {
    return displayName;
  }

  public String getId() {
    return id;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public void setId(String id) {
    this.id = id;
  }
  
}
