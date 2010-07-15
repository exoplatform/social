/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.social.opensocial.model;

/**
 * An OAuth service provider is a web site that allows eXo to access its data,
 * provided that eXo identifies itself using the OAuth protocol.
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class ServiceProviderData {

  /**
   * name of the service provider, I.e the application whose data eXo will be accessing
   */
  private String name;

  /**
   *
   */
  private String description;

  /**
   * The key assigned to eXo by this service provider.
   * The format of this key is determined by the service provider
   */
  private String consumerKey;

  /**
   * The consumer secret assigned to eXo by the service provider.
   * his secret is used to digitally sign all the requests from your application to the service provider.
   */
  private String sharedSecret;

  /**
   * The callback url
   */
  private String callbackUrl;

  public ServiceProviderData(String name, String description, String consumerKey, String sharedSecret, String callbackUrl) {
    this.name = name;
    this.description = description;
    this.consumerKey = consumerKey;
    this.sharedSecret = sharedSecret;
    this.callbackUrl = callbackUrl;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getConsumerKey() {
    return consumerKey;
  }

  public void setConsumerKey(String consumerKey) {
    this.consumerKey = consumerKey;
  }

  public String getSharedSecret() {
    return sharedSecret;
  }

  public void setSharedSecret(String sharedSecret) {
    this.sharedSecret = sharedSecret;
  }

  public String getCallbackUrl() {
    return callbackUrl;
  }

  public void setCallbackUrl(String callbackUrl) {
    this.callbackUrl = callbackUrl;
  }

}
