package org.exoplatform.social.service.rest.api.models;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * PeopleInfo class
 * 
 * Contains people's information that relate to specific user.
 *
 */
@XmlRootElement
public class PeopleInfo {
  private String id;
  private String profileUrl;
  private String avatarURL;
  private String activityTitle;
  private String relationshipType;
  private String fullName;
  private String position;
  private Boolean isDeleted;
  private Boolean isEnable;

  
  public PeopleInfo() {
  }
  
  public PeopleInfo(String relationshipType) {
    this.relationshipType = relationshipType;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getActivityTitle() {
    return activityTitle;
  }
  
  public void setActivityTitle(String activityTitle) {
    this.activityTitle = activityTitle;
  }
  
  public String getAvatarURL() {
    return avatarURL;
  }
  
  public void setAvatarURL(String avatarURL) {
    this.avatarURL = avatarURL;
  }

  public String getRelationshipType() {
    return relationshipType;
  }

  public void setRelationshipType(String relationshipType) {
    this.relationshipType = relationshipType;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getProfileUrl() {
    return profileUrl;
  }

  public void setProfileUrl(String profileUrl) {
    this.profileUrl = profileUrl;
  }

  public String getPosition() {
    return position;
  }

  public void setPosition(String position) {
    this.position = position;
  }

  public Boolean getDeleted() {
        return isDeleted;
  }

  public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
  }

  public Boolean getEnable() {
    return isEnable;
  }

  public void setEnable(Boolean enable) {
    isEnable = enable;
  }
}
