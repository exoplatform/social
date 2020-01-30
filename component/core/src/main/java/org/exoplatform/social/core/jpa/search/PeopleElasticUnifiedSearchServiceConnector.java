/* 
* Copyright (C) 2003-2016 eXo Platform SAS.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see http://www.gnu.org/licenses/ .
*/
package org.exoplatform.social.core.jpa.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.commons.search.es.ElasticSearchException;
import org.exoplatform.commons.search.es.ElasticSearchFilter;
import org.exoplatform.commons.search.es.ElasticSearchServiceConnector;
import org.exoplatform.commons.search.es.client.ElasticSearchingClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.exoplatform.commons.api.search.data.SearchContext;
import org.exoplatform.commons.api.search.data.SearchResult;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.service.LinkProvider;

public class PeopleElasticUnifiedSearchServiceConnector extends ElasticSearchServiceConnector {

  private static final Log LOG = ExoLogger.getLogger(PeopleElasticUnifiedSearchServiceConnector.class);

  private IdentityManager  identityManager;
  
  private Map<String, String> sortMapping;

  public PeopleElasticUnifiedSearchServiceConnector(InitParams initParams,
                                                     ElasticSearchingClient client,
                                                     IdentityManager identityManager) {
    super(initParams, client);
    this.identityManager = identityManager;
    
    sortMapping = new HashMap<>();
    sortMapping.put("date", "lastUpdatedDate");
    sortMapping.put("title", "name.raw");
  }

  @Override
  protected String getSourceFields() {
    List<String> fields = new ArrayList<>();
    fields.add("name");
    fields.add("firstName");
    fields.add("lastName");
    fields.add("position");
    fields.add("skills");

    List<String> sourceFields = new ArrayList<>();
    for (String sourceField : fields) {
      sourceFields.add("\"" + sourceField + "\"");
    }

    return StringUtils.join(sourceFields, ",");
  }
  
  protected String buildFilteredQuery(String query, Collection<String> sites, List<ElasticSearchFilter> filters, int offset, int limit, String sort, String order) {
    String escapedQuery = escapeReservedCharacters(query.trim());
    StringBuilder esQuery = new StringBuilder();
    esQuery.append("{\n");
    esQuery.append("     \"from\" : " + offset + ", \"size\" : " + limit + ",\n");

    //Score are always tracked, even with sort
    //https://www.impl.co/guide/en/elasticsearch/reference/current/search-request-sort.html#_track_scores
    esQuery.append("     \"track_scores\": true,\n");
    esQuery.append("     \"sort\" : [\n");
    esQuery.append("       { \"" + (StringUtils.isNotBlank(sortMapping.get(sort))?sortMapping.get(sort):"_score") + "\" : ");
    esQuery.append(             "{\"order\" : \"" + (StringUtils.isNotBlank(order)?order:"desc") + "\"}}\n");
    esQuery.append("     ],\n");
    esQuery.append("     \"_source\": [" + getSourceFields() + "],");
    esQuery.append("     \"query\": {\n");
    esQuery.append("        \"bool\" : {\n");
    esQuery.append("          \"must\" : {\n");
    esQuery.append("                \"query_string\" : {\n");
    esQuery.append("                    \"fields\" : [" + getFields() + "],\n");
    esQuery.append("                    \"query\" : \"" + escapedQuery + "\"\n");
    esQuery.append("                }\n");
    esQuery.append("          }\n");
    esQuery.append("        }\n");
    esQuery.append("     },\n");
    esQuery.append("     \"highlight\" : {\n");
    esQuery.append("       \"type\" : \"unified\",\n");
    esQuery.append("       \"pre_tags\" : [\"<strong>\"],\n");
    esQuery.append("       \"post_tags\" : [\"</strong>\"],\n");
    esQuery.append("       \"fields\" : {\n");
    for (int i=0; i<getSearchFields().size(); i++) {
      esQuery.append("         \""+getSearchFields().get(i)+"\" : {\"fragment_size\" : 150, \"number_of_fragments\" : 3}");
      if (i<this.getSearchFields().size()-1) {
        esQuery.append(",");
      }
      esQuery.append("\n");
    }
    esQuery.append("       }\n");
    esQuery.append("     }\n");
    esQuery.append("}");

    LOG.debug("Search Query request to ES : {} ", esQuery);

    return esQuery.toString();
  }

  protected Collection<SearchResult> buildResult(String jsonResponse, SearchContext context) {

    LOG.debug("Search Query response from ES : {} ", jsonResponse);

    Collection<SearchResult> results = new ArrayList<>();
    JSONParser parser = new JSONParser();

    Map json;
    try {
      json = (Map) parser.parse(jsonResponse);
    } catch (ParseException e) {
      throw new ElasticSearchException("Unable to parse JSON response", e);
    }

    JSONObject jsonResult = (JSONObject) json.get("hits");
    if (jsonResult == null) {
      return results;
    }

    JSONArray jsonHits = (JSONArray) jsonResult.get("hits");

    for (Object jsonHit : jsonHits) {
      Identity identity = identityManager.getIdentity(((JSONObject) jsonHit).get("_id").toString(), true);
      Profile profile = identity.getProfile();
      if (identity.isDeleted()) {
        continue;
      }

      Double score = (Double) ((JSONObject) jsonHit).get("_score");

      //
      StringBuilder sb = new StringBuilder();

      //
      if (profile.getEmail() != null) {
        sb.append(profile.getEmail());
      }

      //
      List<Map> phones = (List<Map>) profile.getProperty(Profile.CONTACT_PHONES);
      if (phones != null && phones.size() > 0) {
        sb.append(" - " + phones.get(0).get("value"));
      }

      //
      if (profile.getProperty(Profile.GENDER) != null) {
        sb.append(" - " + profile.getProperty(Profile.GENDER));
      }

      results.add(new SearchResult(profile.getUrl(),
                                   profile.getFullName(),
                                   profile.getPosition(),
                                   sb.toString(),
                                   profile.getAvatarUrl() != null ? profile.getAvatarUrl()
                                                                  : LinkProvider.PROFILE_DEFAULT_AVATAR_URL,
                                   profile.getCreatedTime(),
                                   // score must not be null as "track_scores"
                                   // is part of the query
                                   score.longValue()));
    }

    return results;

  }

}
