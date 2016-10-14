/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
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
package org.exoplatform.social.core.jpa.search;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.exoplatform.addons.es.client.ElasticSearchingClient;
import org.exoplatform.addons.es.search.ElasticSearchException;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.relationship.model.Relationship.Type;
import org.exoplatform.social.core.storage.impl.StorageUtils;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Sep 29, 2015  
 */
public class ProfileSearchConnector {
  private static final Log LOG = ExoLogger.getLogger(ProfileSearchConnector.class);
  private final ElasticSearchingClient client;
  private String index;
  private String searchType;
  
  public ProfileSearchConnector(InitParams initParams, ElasticSearchingClient client) {
    PropertiesParam param = initParams.getPropertiesParam("constructor.params");
    this.index = param.getProperty("index");
    this.searchType = param.getProperty("searchType");
    this.client = client;
  }

  public List<Identity> search(Identity identity,
                                     ProfileFilter filter,
                                     Type type,
                                     long offset,
                                     long limit) {
    String esQuery = buildQueryStatement(identity, filter, type, offset, limit);
    String jsonResponse = this.client.sendRequest(esQuery, this.index, this.searchType);
    return buildResult(jsonResponse);
  }
  /**
   * TODO it will be remove to use "_count" query
   * 
   * @param identity the Identity
   * @param filter the filter
   * @param type type type
   * @return number of identities
   */
  public int count(Identity identity,
                               ProfileFilter filter,
                               Type type) {
    String esQuery = buildQueryStatement(identity, filter, type, 0, 1);
    String jsonResponse = this.client.sendRequest(esQuery, this.index, this.searchType);
    return getCount(jsonResponse);
  }
  
  private int getCount(String jsonResponse) {
    
    LOG.debug("Search Query response from ES : {} ", jsonResponse);
    JSONParser parser = new JSONParser();

    Map<?, ?> json = null;
    try {
      json = (Map<?, ?>)parser.parse(jsonResponse);
    } catch (ParseException e) {
      throw new ElasticSearchException("Unable to parse JSON response", e);
    }

    JSONObject jsonResult = (JSONObject) json.get("hits");
    if (jsonResult == null) return 0;

    int count = Integer.parseInt(jsonResult.get("total").toString());
    return count;
  }
  
  private List<Identity> buildResult(String jsonResponse) {

    LOG.debug("Search Query response from ES : {} ", jsonResponse);

    List<Identity> results = new ArrayList<Identity>();
    JSONParser parser = new JSONParser();

    Map json = null;
    try {
      json = (Map)parser.parse(jsonResponse);
    } catch (ParseException e) {
      throw new ElasticSearchException("Unable to parse JSON response", e);
    }

    JSONObject jsonResult = (JSONObject) json.get("hits");
    if (jsonResult == null) return results;

    //
    JSONArray jsonHits = (JSONArray) jsonResult.get("hits");
    Identity identity = null;
    Profile p;
    for(Object jsonHit : jsonHits) {
      JSONObject hitSource = (JSONObject) ((JSONObject) jsonHit).get("_source");
      String position = (String) hitSource.get("position");
      String name = (String) hitSource.get("name");
      String userName = (String) hitSource.get("userName");
      String firstName = (String) hitSource.get("firstName");
      String lastName = (String) hitSource.get("lastName");
      String avatarUrl = (String) hitSource.get("avatarUrl");
      String email = (String) hitSource.get("email");
      String identityId = (String) ((JSONObject) jsonHit).get("_id");
      identity = new Identity(OrganizationIdentityProvider.NAME, userName);
      identity.setId(identityId);
      p = new Profile(identity);
      p.setAvatarUrl(avatarUrl);
      p.setProperty(Profile.FULL_NAME, name);
      p.setProperty(Profile.FIRST_NAME, firstName);
      p.setProperty(Profile.LAST_NAME, lastName);
      p.setProperty(Profile.POSITION, position);
      p.setProperty(Profile.EMAIL, email);
      p.setProperty(Profile.USERNAME, userName);
      identity.setProfile(p);
      results.add(identity);
    }
    return results;
  }
  
  
  private String buildQueryStatement(Identity identity, ProfileFilter filter, Type type, long offset, long limit) {
    String expEs = buildExpression(filter);
    StringBuilder esQuery = new StringBuilder();
    esQuery.append("{\n");
    esQuery.append("   \"from\" : " + offset + ", \"size\" : " + limit + ",\n");
    esQuery.append("   \"sort\": [\n");
    esQuery.append("             {\"lastName\": {\"order\": \"asc\"}},\n");
    esQuery.append("             {\"firstName\": {\"order\": \"asc\"}}\n");
    esQuery.append("             ]\n");
    if (identity != null && type != null) {
      esQuery.append("       ,\n");
      esQuery.append("\"query\" : {\n");
      esQuery.append("    \"bool\" :{\n");
      esQuery.append("      \"must\" : {\n");
      esQuery.append("        \"query_string\" : {\n");
      esQuery.append("          \"query\" : \"*"+ identity.getId() +"*\",\n");
      esQuery.append("          \"fields\" : [\"" + buildTypeEx(type) + "\"]\n");
      esQuery.append("        }\n");
      esQuery.append("      }\n");
    } else if (filter.getExcludedIdentityList() != null && filter.getExcludedIdentityList().size() > 0) {
      esQuery.append("       ,\n");
      esQuery.append("\"query\" : {\n");
      esQuery.append("\"filtered\" :{\n");
      esQuery.append("  \"query\" : {\n");
        esQuery.append("    \"bool\" : {\n");
      esQuery.append("\"must_not\": [\n");
      esQuery.append("        {\n");
      esQuery.append("          \"ids\" : {\n");
      esQuery.append("             \"values\" : [" + buildExcludedIdentities(filter) + "]\n");
      esQuery.append("          }\n");
      esQuery.append("        }\n");
      esQuery.append("      ]\n");
      esQuery.append("    }\n");
      esQuery.append("  }\n");
      esQuery.append("  }\n");
      esQuery.append("}\n");
    }
    //if the search fields are existing.
    if (expEs != null && expEs.length() > 0) {
      esQuery.append("      ,\n");
      esQuery.append("\"filter\" : {\n");
      esQuery.append("  \"bool\" : {\n");
      esQuery.append("    \"must\": [\n");
      esQuery.append("      {");
      esQuery.append("        \"query\": {\n");
      esQuery.append("          \"query_string\": {\n");
      esQuery.append("            \"query\": \"" + expEs + "\"\n");
      esQuery.append("          }\n");
      esQuery.append("         }\n");
      esQuery.append("      }\n");
      esQuery.append("    ]\n");
      esQuery.append("  }\n");
      esQuery.append("}\n");
    } //end if
    
    //don't need add in the case search ALL
    if (identity != null && type != null) {
      esQuery.append("     }\n");      
      esQuery.append("   }\n");
    }
    
    esQuery.append("}\n");
    LOG.debug("Search Query request to ES : {} ", esQuery);

    return esQuery.toString();
  }
  
  /**
   * 
   * @param filter
   * @return
   */
  private String buildExcludedIdentities(ProfileFilter filter) {
    StringBuilder typeExp = new StringBuilder();
    if (filter.getExcludedIdentityList() != null && filter.getExcludedIdentityList().size() > 0) {
      
      Iterator<Identity> iter = filter.getExcludedIdentityList().iterator();
      Identity first = iter.next();
      typeExp.append("\"").append(first.getId()).append("\"");
      
      if (!iter.hasNext()) {
        return typeExp.toString();
      }
      Identity next;
      while (iter.hasNext()) {
        next = iter.next();
        typeExp.append(",\"").append(next.getId()).append("\"");
      }
    }
    return typeExp.toString();
  }
  
  /**
   * 
   * @param type
   * @return
   */
  private String buildTypeEx(Type type) {
    String result;
    switch(type) {
      case CONFIRMED:
        result = "connections";
        break;
      case INCOMING:
        result = "incomings";
        break;
      case OUTGOING:
        result = "outgoings";
        break;
      default:
        throw new IllegalArgumentException("Type ["+type+"] not supported");
    }
    return result;
  }

  private String buildExpression(ProfileFilter filter) {
    StringBuilder esExp = new StringBuilder();
    char firstChar = filter.getFirstCharacterOfName();
    //
    if (firstChar != '\u0000') {
      esExp.append("lastName:").append(firstChar).append(StorageUtils.ASTERISK_STR);
      return esExp.toString();
    }
    //
    String inputName = filter.getName().replace(StorageUtils.ASTERISK_STR, StorageUtils.EMPTY_STR);
    if (inputName != null && inputName.length() > 0) {
      esExp.append("name:").append(StorageUtils.ASTERISK_STR).append(inputName).append(StorageUtils.ASTERISK_STR);
    }

    //skills
    String skills = filter.getSkills().replace(StorageUtils.ASTERISK_STR, StorageUtils.EMPTY_STR);
    if (skills != null && skills.length() > 0) {
      if (esExp.length() > 0) {
        esExp.append(" AND ");
      }
      //
      esExp.append("skills:").append(StorageUtils.ASTERISK_STR).append(skills).append(StorageUtils.ASTERISK_STR);
    }
    
    //position
    String position = filter.getPosition().replace(StorageUtils.ASTERISK_STR, StorageUtils.EMPTY_STR);
    if (position != null && position.length() > 0) {
      if (esExp.length() > 0) {
        esExp.append(" AND ");
      }
      esExp.append("position:").append(StorageUtils.ASTERISK_STR).append(position).append(StorageUtils.ASTERISK_STR);
    }
    return esExp.toString();
  }
}
