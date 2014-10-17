package org.exoplatform.social.service.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.exoplatform.services.rest.Filter;
import org.exoplatform.services.rest.GenericContainerResponse;
import org.exoplatform.services.rest.ResponseFilter;
import org.exoplatform.services.rest.impl.ApplicationContextImpl;
import org.exoplatform.social.service.rest.api.models.ResourceCollections;

@Filter
public class FieldsResponseFilter implements ResponseFilter {
  private static final String FIELDS_QUERY_PARAM = "fields";

  @Override
  public void doFilter(GenericContainerResponse response) {
    if (response.getEntity() == null) return;
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

  @SuppressWarnings("unchecked")
  private Object filterProperties(Object entity, String outputFields) {
    List<String> returnedProperties = new ArrayList<String>();
    returnedProperties.addAll(Arrays.asList(outputFields.split(",")));
    
    if (ResourceCollections.class.isAssignableFrom(entity.getClass())) {
      ResourceCollections rc = (ResourceCollections)entity;
      return rc.getCollectionByFields(returnedProperties);
    } else if (entity instanceof Map) {
      Map<String, Object> elementInfo = (Map<String, Object>)entity;
      Map<String, Object> map = new LinkedHashMap<String, Object>();
      for (Map.Entry<String, Object> entry : elementInfo.entrySet()) {
        if (returnedProperties.contains(entry.getKey())) {
          map.put(entry.getKey(), entry.getValue());
        }
      }
      return map;
    }
    
    return new Object();
  }
}
