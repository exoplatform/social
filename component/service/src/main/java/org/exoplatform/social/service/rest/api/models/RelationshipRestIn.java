package org.exoplatform.social.service.rest.api.models;

public class RelationshipRestIn {
  
  private String sender;

  private String receiver;

  private String status;
  
  public String getSender() {
    return sender;
  }
  public void setSender(String sender) {
    this.sender = sender;
  }
  public String getReceiver() {
    return receiver;
  }
  public void setReceiver(String receiver) {
    this.receiver = receiver;
  }
  public String getStatus() {
    return status;
  }
  public void setStatus(String status) {
    this.status = status;
  }

}
