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

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.exoplatform.commons.search.es.ElasticSearchException;
import org.exoplatform.commons.search.es.client.ElasticSearchingClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.apache.commons.lang.StringUtils;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.relationship.model.Relationship.Type;
import org.exoplatform.social.core.search.Sorting;
import org.exoplatform.social.core.search.Sorting.SortBy;
import org.exoplatform.social.core.service.LinkProvider;
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
    if(identity == null && filter.getViewerIdentity() != null) {
      identity = filter.getViewerIdentity();
    }
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
      p.setId(identityId);
      p.setAvatarUrl(avatarUrl);
      p.setUrl(LinkProvider.getProfileUri(userName));
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
    Sorting sorting = filter.getSorting();
    if(sorting != null && sorting.sortBy != null) {
      esQuery.append("   \"sort\": {");
      switch (sorting.sortBy) {
      case DATE:
        esQuery.append("\"lastUpdatedDate\"");
        break;
      case FIRSTNAME:
        esQuery.append("\"firstName.raw\"");
        break;
      case LASTNAME:
        esQuery.append("\"lastName.raw\"");
        break;
      default:
        // Title, Fullname, any other condition, we will use the fullname
        esQuery.append("\"name.raw\"");
        break;
      }
      esQuery.append(": {\"order\": \"")
             .append(sorting.orderBy == null ? "asc" : sorting.orderBy.name())
             .append("\"}}\n");
    } else {
      esQuery.append("   \"sort\": {\"name.raw\": {\"order\": \"asc\"}}\n");
    }
    StringBuilder esSubQuery = new StringBuilder();
    esSubQuery.append("       ,\n");
    esSubQuery.append("\"query\" : {\n");
    esSubQuery.append("      \"constant_score\" : {\n");
    esSubQuery.append("        \"filter\" : {\n");
    esSubQuery.append("          \"bool\" :{\n");
    boolean subQueryEmpty = true;
    if (filter.getRemoteIds() != null && !filter.getRemoteIds().isEmpty()) {
      subQueryEmpty = false;
      StringBuilder remoteIds = new StringBuilder();
      for (String remoteId : filter.getRemoteIds()) {
        if (remoteIds.length() > 0) {
          remoteIds.append(",");
        }
        remoteIds.append("\"").append(remoteId).append("\"");
      }
      esSubQuery.append("      \"must\" : {\n");
      esSubQuery.append("        \"terms\" :{\n");
      esSubQuery.append("          \"userName\" : [" + remoteIds.toString() + "]\n");
      esSubQuery.append("        } \n");
      esSubQuery.append("      },\n");
    }
    if (identity != null && type != null) {
      subQueryEmpty = false;
      esSubQuery.append("      \"must\" : {\n");
      esSubQuery.append("        \"query_string\" : {\n");
      esSubQuery.append("          \"query\" : \""+ identity.getId() +"\",\n");
      esSubQuery.append("          \"fields\" : [\"" + buildTypeEx(type) + "\"]\n");
      esSubQuery.append("        }\n");
      esSubQuery.append("      }\n");
    } else if (filter.getExcludedIdentityList() != null && filter.getExcludedIdentityList().size() > 0) {
      subQueryEmpty = false;
      esSubQuery.append("      \"must_not\": [\n");
      esSubQuery.append("        {\n");
      esSubQuery.append("          \"ids\" : {\n");
      esSubQuery.append("             \"values\" : [" + buildExcludedIdentities(filter) + "]\n");
      esSubQuery.append("          }\n");
      esSubQuery.append("        }\n");
      esSubQuery.append("      ]\n");
    }
    //if the search fields are existing.
    if (expEs != null && expEs.length() > 0) {
      if(!subQueryEmpty) {
        esSubQuery.append("      ,\n");
      }
      subQueryEmpty = false;
      esSubQuery.append("    \"filter\": [\n");
      esSubQuery.append("      {");
      esSubQuery.append("          \"query_string\": {\n");
      esSubQuery.append("            \"query\": \"" + expEs + "\"\n");
      esSubQuery.append("          }\n");
      esSubQuery.append("      }\n");
      esSubQuery.append("    ]\n");
    } //end if
    esSubQuery.append("     } \n");
    esSubQuery.append("   } \n");
    esSubQuery.append("  }\n");
    esSubQuery.append(" }\n");
    if(!subQueryEmpty) {
      esQuery.append(esSubQuery);
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
        // Search for identity of current user viewer
        // in the outgoings relationships field of other identities
        result = "outgoings";
        break;
      case OUTGOING:
        // Search for identity of current user viewer
        // in the incomings relationships field of other identities
        result = "incomings";
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
      String filterField = "name";
      if (filter.getFirstCharFieldName() != null) {
        switch (SortBy.valueOf(filter.getFirstCharFieldName().toUpperCase())) {
        case FIRSTNAME:
          filterField = "firstName";
          break;
        case LASTNAME:
          filterField = "lastName";
          break;
        default:
          // Filter by first character on full name
          filterField = "name";
          break;
        }
      }

      esExp.append(filterField)
           .append(".whitespace:")
           .append(firstChar)
           .append(StorageUtils.ASTERISK_STR);
      return esExp.toString();
    }

    //
    String inputName = StringUtils.isBlank(filter.getName()) ? null : filter.getName().replace(StorageUtils.ASTERISK_STR, StorageUtils.EMPTY_STR);
    if (StringUtils.isNotBlank(inputName)) {
     //
      String[] keys = inputName.split(" ");
      if (keys.length > 1) {
        // We will not search on username because it doesn't contain a space character
        if (filter.isSearchEmail()) {
          esExp.append("(");
        }
        esExp.append("(");
        for (int i = 0; i < keys.length; i++) {
          if (i != 0 ) {
            esExp.append(" AND ") ;
          }
          esExp.append(" name.whitespace:").append(StorageUtils.ASTERISK_STR).append(removeAccents(keys[i])).append(StorageUtils.ASTERISK_STR);
        }
        esExp.append(")");
        if (filter.isSearchEmail()) {
          esExp.append(" OR ( ");
          for (int i = 0; i < keys.length; i++) {
            if (i != 0 ) {
              esExp.append(" AND ") ;
            }
            esExp.append(" email:").append(StorageUtils.ASTERISK_STR).append(removeAccents(keys[i])).append(StorageUtils.ASTERISK_STR);
          }
          esExp.append(") )");
        }
      } else {
        esExp.append("( name.whitespace:").append(StorageUtils.ASTERISK_STR).append(removeAccents(inputName)).append(StorageUtils.ASTERISK_STR);
        if (filter.isSearchEmail()) {
          esExp.append(" OR email:").append(StorageUtils.ASTERISK_STR).append(removeAccents(inputName)).append(StorageUtils.ASTERISK_STR);
        }
        esExp.append(" OR userName:").append(StorageUtils.ASTERISK_STR).append(removeAccents(inputName)).append(StorageUtils.ASTERISK_STR).append(")");
      }
    }

    //skills
    String skills = StringUtils.isBlank(filter.getSkills()) ? null : filter.getSkills().replace(StorageUtils.ASTERISK_STR, StorageUtils.EMPTY_STR);
    if (StringUtils.isNotBlank(skills)) {
      if (esExp.length() > 0) {
        esExp.append(" AND ");
      }
      //
      esExp.append("skills:").append(StorageUtils.ASTERISK_STR).append(removeAccents(skills)).append(StorageUtils.ASTERISK_STR);
    }

    //position
    String position = StringUtils.isBlank(filter.getPosition()) ? null : filter.getPosition().replace(StorageUtils.ASTERISK_STR, StorageUtils.EMPTY_STR);
    if (StringUtils.isNotBlank(position)) {
      if (esExp.length() > 0) {
        esExp.append(" AND ");
      }
      esExp.append("position:").append(StorageUtils.ASTERISK_STR).append(removeAccents(position)).append(StorageUtils.ASTERISK_STR);
    }
    return esExp.toString();
  }

  private static String removeAccents(String string) {
    string = Normalizer.normalize(string, Normalizer.Form.NFD);
    string = string.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
    return string;
  }
}
