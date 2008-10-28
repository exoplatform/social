package org.exoplatform.social.core.activitystream.model;

/**
 * Created by IntelliJ IDEA.
 * User: jeremi
 * Date: May 23, 2008
 * Time: 11:45:17 AM
 * To change this template use File | Settings | File Templates.
 */
public class MediaItem {
  private String mimeType;
  private Type type;
  private String url;

  public enum Type {
    AUDIO,
    IMAGE,
    VIDEO
  }

  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }
}
