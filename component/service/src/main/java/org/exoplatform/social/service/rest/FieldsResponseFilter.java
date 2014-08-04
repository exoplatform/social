package org.exoplatform.social.service.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.exoplatform.services.rest.Filter;
import org.exoplatform.services.rest.GenericContainerResponse;
import org.exoplatform.services.rest.ResponseFilter;
import org.exoplatform.services.rest.impl.ApplicationContextImpl;
import org.exoplatform.social.service.rest.api.models.ActivitiesCollections;
import org.exoplatform.social.service.rest.api.models.IdentitiesCollections;
import org.exoplatform.social.service.rest.api.models.RelationshipsCollections;
import org.exoplatform.social.service.rest.api.models.SpaceMembershipsCollections;
import org.exoplatform.social.service.rest.api.models.SpacesCollections;
import org.exoplatform.social.service.rest.api.models.UsersCollections;

@Filter
public class FieldsResponseFilter implements ResponseFilter {
  private static final String FIELDS_QUERY_PARAM = "fields";

  @Override
  public void doFilter(GenericContainerResponse response) {
    String outputFields = ApplicationContextImpl.getCurrent().getQueryParameters().getFirst(FIELDS_QUERY_PARAM);
    
    boolean hasOutputFieldsFilter = outputFields != null && outputFields.length() > 0;
    
    if (hasOutputFieldsFilter) {
      Object entity = filterProperties(response.getEntity(), outputFields);
      response.setResponse(Response
          .status(response.getStatus())
          .entity(entity)
          .type(response.getContentType())
          .build());
    }
  }

  private Object filterProperties(Object entity, String outputFields) {
    List<String> returnedProperties = new ArrayList<String>();
    returnedProperties.addAll(Arrays.asList(outputFields.split(",")));
    List<Map<String, Object>> returnedInfos = new ArrayList<Map<String,Object>>();
    
    if (entity instanceof ActivitiesCollections) {
      ActivitiesCollections ids = (ActivitiesCollections)entity;
      extractInfo(returnedProperties, returnedInfos, ids.getActivities());
      ActivitiesCollections identitiesColl = new ActivitiesCollections(ids.getSize(), ids.getOffset(), ids.getLimit());
      identitiesColl.setActivities(returnedInfos);
      return identitiesColl;
    } else if (entity instanceof IdentitiesCollections) {
      IdentitiesCollections ids = (IdentitiesCollections)entity;
      extractInfo(returnedProperties, returnedInfos, ids.getIdentities());
      IdentitiesCollections identitiesColl = new IdentitiesCollections(ids.getSize(), ids.getOffset(), ids.getLimit());
      identitiesColl.setIdentities(returnedInfos);
      return identitiesColl;
    } else if (entity instanceof SpacesCollections) {
      SpacesCollections ids = (SpacesCollections)entity;
      extractInfo(returnedProperties, returnedInfos, ids.getSpaces());
      SpacesCollections identitiesColl = new SpacesCollections(ids.getSize(), ids.getOffset(), ids.getLimit());
      identitiesColl.setSpaces(returnedInfos);
      return identitiesColl;
    } else if (entity instanceof SpaceMembershipsCollections) {
      SpaceMembershipsCollections ids = (SpaceMembershipsCollections)entity;
      extractInfo(returnedProperties, returnedInfos, new ArrayList<Map<String, Object>>(ids.getSpaceMemberships()));
      SpaceMembershipsCollections identitiesColl = new SpaceMembershipsCollections(ids.getSize(), ids.getOffset(), ids.getLimit());
      identitiesColl.setSpaceMemberships(returnedInfos);
      return identitiesColl;
    } else if (entity instanceof UsersCollections) {
      UsersCollections ids = (UsersCollections)entity;
      extractInfo(returnedProperties, returnedInfos, ids.getUsers());
      UsersCollections identitiesColl = new UsersCollections(ids.getSize(), ids.getOffset(), ids.getLimit());
      identitiesColl.setUsers(returnedInfos);
      return identitiesColl;
    } else if (entity instanceof RelationshipsCollections) {
      RelationshipsCollections ids = (RelationshipsCollections)entity;
      extractInfo(returnedProperties, returnedInfos, ids.getRelationships());
      RelationshipsCollections identitiesColl = new RelationshipsCollections(ids.getSize(), ids.getOffset(), ids.getLimit());
      identitiesColl.setRelationships(returnedInfos);
      return identitiesColl;
    }
    
    return returnedInfos;
  }

  private void extractInfo(List<String> returnedProperties, List<Map<String, Object>> returnedInfos, 
      List<Map<String, Object>> elementInfos) {
    
    for (Map<String, Object> elementInfo : elementInfos) {
      Map<String, Object> map = new LinkedHashMap<String, Object>();
      for (Map.Entry<String, Object> entry : elementInfo.entrySet()) {
        if (returnedProperties.contains(entry.getKey())) {
          map.put(entry.getKey(), entry.getValue());
        }
      }
      returnedInfos.add(map);
    }
  }
}
