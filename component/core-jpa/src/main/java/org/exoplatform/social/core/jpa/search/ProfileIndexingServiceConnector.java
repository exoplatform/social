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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.commons.search.domain.Document;
import org.exoplatform.commons.search.index.impl.ElasticIndexingServiceConnector;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.jpa.storage.dao.ConnectionDAO;
import org.exoplatform.social.core.jpa.storage.dao.IdentityDAO;
import org.json.simple.JSONObject;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.relationship.model.Relationship;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Sep
 * 29, 2015
 */
  public class ProfileIndexingServiceConnector extends ElasticIndexingServiceConnector {
  private static final Log LOG = ExoLogger.getLogger(ProfileIndexingServiceConnector.class);
  public final static String TYPE = "profile";
  /** */
  private final IdentityManager identityManager;
  /** */
  private final ConnectionDAO connectionDAO;

  private final IdentityDAO identityDAO;

  public ProfileIndexingServiceConnector(InitParams initParams,
                                         IdentityManager identityManager,
                                         IdentityDAO identityDAO,
                                         ConnectionDAO connectionDAO) {
    super(initParams);
    this.identityManager = identityManager;
    this.identityDAO = identityDAO;
    this.connectionDAO = connectionDAO;
  }

  @Override
  public Document create(String id) {
    return getDocument(id);
  }

  @Override
  public Document update(String id) {
    return getDocument(id);
  }

  private String buildConnectionString(Identity identity, Relationship.Type type) {
    StringBuilder sb = new StringBuilder();

    final int limit = 200;
    List<Long> list = null;
    long id = Long.parseLong(identity.getId());

    boolean inSender = true, inReceiver = true;

    if (type == Relationship.Type.OUTGOING) {
      inSender = false;
      type = Relationship.Type.PENDING;

    } else if (type == Relationship.Type.INCOMING) {
      inReceiver = false;
      type = Relationship.Type.PENDING;
    }


    if (inSender) {
      int offset = 0;
      do {
        list = connectionDAO.getSenderIds(id, type, offset, limit);
        sb = append(sb, list);
        offset += limit;
      } while (list.size() >= limit);
    }

    if (inReceiver) {
      int offset = 0;
      do {
        list = connectionDAO.getReceiverIds(id, type, offset, limit);
        sb = append(sb, list);
        offset += limit;
      } while (list.size() >= limit);
    }

    //Remove the last ","
    if (sb.length() > 0) {
      sb.deleteCharAt(sb.length() - 1);
    }

    return sb.toString();
  }

  private StringBuilder append(StringBuilder sb, List<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return sb;
    }
    int len = ids.size()*10;

    if (sb.capacity() < sb.length() + len) {
      sb = new StringBuilder(sb.capacity() + len).append(sb);
    }
    for (Long id : ids) {
      sb.append(id).append(",");
    }
    return sb;
  }

  @Override
  public List<String> getAllIds(int offset, int limit) {
    List<Long> ids = identityDAO.getAllIdsByProvider(OrganizationIdentityProvider.NAME, offset, limit);

    if (ids == null || ids.isEmpty()) {
      return new ArrayList<>();
    } else {
      List<String> result = new ArrayList<>(ids.size());
      for (Long id : ids) {
        result.add(String.valueOf(id));
      }
      return result;
    }
  }
  
  @Override
  public String getMapping() {
    StringBuilder mapping = new StringBuilder()
            .append("{")
            .append("  \"properties\" : {\n")
            .append("    \"userName\" : {\"type\" : \"keyword\"},\n")
            .append("    \"name\" : {")
            .append("      \"type\" : \"text\",")
            .append("      \"index_options\": \"offsets\",")
            .append("      \"fields\": {")
            .append("        \"raw\": {")
            .append("          \"type\": \"keyword\"")
            .append("        },")
            .append("        \"whitespace\": {")
            .append("          \"type\": \"text\",")
            .append("          \"analyzer\": \"whitespace_lowercase_asciifolding\"")
            .append("        }")
            .append("      }")
            .append("    },\n")
            .append("    \"firstName\" : {")
            .append("      \"type\" : \"text\",")
            .append("      \"index_options\": \"offsets\",")
            .append("      \"fields\": {")
            .append("        \"raw\": {")
            .append("          \"type\": \"keyword\"")
            .append("        },")
            .append("        \"whitespace\": {")
            .append("          \"type\": \"text\",")
            .append("          \"analyzer\": \"whitespace_lowercase_asciifolding\"")
            .append("        }")
            .append("      }")
            .append("    },\n")
            .append("    \"lastName\" : {")
            .append("      \"type\" : \"text\",")
            .append("      \"index_options\": \"offsets\",")
            .append("      \"fields\": {")
            .append("        \"raw\": {")
            .append("          \"type\": \"keyword\"")
            .append("        },")
            .append("        \"whitespace\": {")
            .append("          \"type\": \"text\",")
            .append("          \"analyzer\": \"whitespace_lowercase_asciifolding\"")
            .append("        }")
            .append("      }")
            .append("    },\n")
            .append("    \"email\" : {\"type\" : \"keyword\"},\n")
            .append("    \"avatarUrl\" : {\"type\" : \"text\", \"index\": false},\n")
            .append("    \"position\" : {\"type\" : \"text\", \"index_options\": \"offsets\"},\n")
            .append("    \"skills\" : {\"type\" : \"text\", \"index_options\": \"offsets\"},\n")
            .append("    \"lastUpdatedDate\" : {\"type\" : \"date\", \"format\": \"epoch_millis\"}\n")
            .append("  }\n")
            .append("}");

    return mapping.toString();
  }

  private Document getDocument(String id) {
    if (StringUtils.isBlank(id)) {
      throw new IllegalArgumentException("id is mandatory");
    }

    long ts = System.currentTimeMillis();
    LOG.debug("get profile document for identity id={}", id);

    Identity identity = identityManager.getIdentity(id, true);
    Profile profile = identity.getProfile();

    Map<String, String> fields = new HashMap<String, String>();
    fields.put("name", removeAccents(profile.getFullName()));
    fields.put("firstName", removeAccents((String) profile.getProperty(Profile.FIRST_NAME)));
    fields.put("lastName", removeAccents((String) profile.getProperty(Profile.LAST_NAME)));
    fields.put("position", removeAccents(profile.getPosition()));
    fields.put("skills", removeAccents((String) profile.getProperty(Profile.EXPERIENCES_SKILLS)));
    fields.put("avatarUrl", profile.getAvatarUrl());
    fields.put("userName", identity.getRemoteId());
    fields.put("email", profile.getEmail());
    Date createdDate = new Date(profile.getCreatedTime());

    //confirmed connections
    String connectionsStr = buildConnectionString(identity, Relationship.Type.CONFIRMED);
    if (connectionsStr.length() > 0) {
      fields.put("connections", connectionsStr);
    }
    //outgoing connections
    connectionsStr = buildConnectionString(identity, Relationship.Type.OUTGOING);
    if (connectionsStr.length() > 0) {
      fields.put("outgoings", connectionsStr);
    }
    //incoming connections
    connectionsStr = buildConnectionString(identity, Relationship.Type.INCOMING);
    if (connectionsStr.length() > 0) {
      fields.put("incomings", connectionsStr);
    }

    Document document = new Document(TYPE, id, null, createdDate, (Set<String>)null, fields);
    LOG.info("profile document generated for identity id={} remote_id={} duration_ms={}", id, identity.getRemoteId(), System.currentTimeMillis() - ts);

    return document;
  }

  private static String removeAccents(String string) {
    if (StringUtils.isNotBlank(string)){
      string = Normalizer.normalize(string, Normalizer.Form.NFD);
      string = string.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
    }
    return string;
  }
}
