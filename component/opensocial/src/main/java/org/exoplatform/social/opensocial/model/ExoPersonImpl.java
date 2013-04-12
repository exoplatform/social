/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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

import java.util.List;
import java.util.Map;

import org.apache.shindig.social.core.model.PersonImpl;

import com.google.common.collect.Maps;

public class ExoPersonImpl extends PersonImpl {

  private List<Space> spaces;

  private String portalName;

  private String restContext;

  private String hostName;

  private String portalOwner;
  
  private String peopleUri;
  
  public static enum Field {
    /* the json field for spaces*/
    SPACES("spaces"),

    /* the json field for portal container*/
    PORTAL_CONTAINER("portalName"),

    /* the json field for rest container*/
    REST_CONTEXT("restContext"),

    /* the json field for host name*/
    HOST("host"),

    /* the json field for portal onwer name */
    PORTAL_OWNER("portalOwner"),
    /* the URI of people page */
    PEOPLE_URI("peopleUri");
    
    /**
     * The json field that the instance represents.
     */
    private final String urlString;
    /**
     * create a field base on the a json element.
     *
     * @param urlString the name of the element
     */
    private Field (String urlString) {
      this.urlString = urlString;
    }

    /**
     * emit the field as a json element.
     *
     * @return the field name
     */
    @Override
    public String toString() {
      return this.urlString;
    }

    /**
     * a Map to convert json string to Field representations.
     */
    private static Map<String, Field> URL_STRING_TO_FIELD_MAP;

    /**
     * Converts from a url string (usually passed in the fields= parameter) into the
     * corresponding field enum.
     * @param urlString The string to translate.
     * @return The corresponding person field.
     */
    public static ExoPersonImpl.Field fromUrlString(String urlString) {
      if (URL_STRING_TO_FIELD_MAP == null) {
        URL_STRING_TO_FIELD_MAP = Maps.newHashMap();
        for (ExoPersonImpl.Field field : ExoPersonImpl.Field.values()) {
          URL_STRING_TO_FIELD_MAP.put(field.toString(), field);
        }
      }

      return URL_STRING_TO_FIELD_MAP.get(urlString);
    }

  }

  public void setSpaces(List<Space> spaces) {
    this.spaces = spaces;
  }

  public List<Space> getSpaces() {
    return spaces;
  }

  public void setPortalName(String portalName) {
    this.portalName = portalName;
  }

  public String getPortalName() {
    return portalName;
  }
  public void setRestContextName(String restContext) {
    this.restContext = restContext;
  }

  public String getRestContextName() {
    return restContext;
  }

  public void setHostName(String hostName) {
    this.hostName = hostName;
  }

  public String getHostName() {
    return hostName;
  }

  public String getPortalOwner() {
    return portalOwner;
  }

  public void setPortalOwner(String portalOwner) {
    this.portalOwner = portalOwner;
  }
  
  public String getPeopleUri() {
    return peopleUri;
  }

  public void setPeopleUri(String peopleUri) {
    this.peopleUri = peopleUri;
  } 
}
