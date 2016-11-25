package org.exoplatform.social.service.rest.api.models;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.exoplatform.social.core.identity.model.Identity;

/**
 * UserNameList class
 * 
 * Contains list of user's name that match the input string.
 *
 */
@XmlRootElement
public class IdentityNameList {
  private List<Option> options;

  public List<Option> getOptions() {
    return options;
  }

  public void setOptions(List<Option> options) {
    this.options = options;
  }

  public void addOption(Option opt) {
    if (opt == null) {
      throw new IllegalArgumentException("Option can not be NULL");
    }

    if (options == null) {
      options = new ArrayList<Option>();
    }
    options.add(opt);
  }

  public void addToNameList(Identity ...identities) {
    for (Identity identity : identities) {
      String fullName = identity.getProfile().getFullName();
      Option opt = new Option();
      opt.setType("user");
      opt.setText(fullName);
      opt.setValue(identity.getRemoteId());
      opt.setAvatarUrl(identity.getProfile() == null ? null : identity.getProfile().getAvatarUrl());
      this.addOption(opt);
    }
  }

  static public class Option {
    private String type;
    private String value;
    private String text;
    private String avatarUrl;
    private int order;
    private boolean isInvalid;

    public boolean isInvalid() {
      return isInvalid;
    }

    public void setInvalid(boolean isInvalid) {
      this.isInvalid = isInvalid;
    }

    public int getOrder() {
      return order;
    }

    public void setOrder(int order) {
      this.order = order;
    }

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }

    public String getText() {
      return text;
    }

    public void setText(String text) {
      this.text = text;
    }

    public String getAvatarUrl() {
      return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
      this.avatarUrl = avatarUrl;
    }
  }
}
