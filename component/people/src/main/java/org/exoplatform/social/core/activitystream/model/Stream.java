package org.exoplatform.social.core.activitystream.model;

/**
 * Created by IntelliJ IDEA.
 * User: jeremi
 * Date: May 23, 2008
 * Time: 11:55:38 AM
 * To change this template use File | Settings | File Templates.
 */
public class Stream {
  private String faviconUrl;
  private String sourceUrl;
  private String title;
  private String url;

  public String getFaviconUrl() {
    return faviconUrl;
  }

  public void setFaviconUrl(String faviconUrl) {
    this.faviconUrl = faviconUrl;
  }

  public String getSourceUrl() {
    return sourceUrl;
  }

  public void setSourceUrl(String sourceUrl) {
    this.sourceUrl = sourceUrl;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }
}
