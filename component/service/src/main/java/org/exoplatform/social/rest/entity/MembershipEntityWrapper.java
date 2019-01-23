package org.exoplatform.social.rest.entity;

public class MembershipEntityWrapper {
  private String group;
  private String membershipType;

  public String getGroup() {
    return group;
  }

  public void setGroup(String group) {
    this.group = group;
  }

  public String getMembershipType() {
    return membershipType;
  }

  public void setMembershipType(String membershipType) {
    this.membershipType = membershipType;
  }
}
