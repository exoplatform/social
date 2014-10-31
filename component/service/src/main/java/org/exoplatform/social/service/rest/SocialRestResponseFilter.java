package org.exoplatform.social.service.rest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.ApplicationContext;
import org.exoplatform.services.rest.Filter;
import org.exoplatform.services.rest.GenericContainerResponse;
import org.exoplatform.services.rest.ResponseFilter;
import org.exoplatform.services.rest.impl.ApplicationContextImpl;
import org.exoplatform.social.service.rest.api.models.ResourceCollections;
import org.json.JSONObject;

@Filter
public class SocialRestResponseFilter implements ResponseFilter {
  private static final String FIELDS_QUERY_PARAM = "fields";
  private static final String PATTERN_RFC1123    = "EEE, dd MMM yyyy HH:mm:ss zzz";
  private static final String DOUBLE_QUOTE       = "^\"|\"$";
  private static final Log    LOG                = ExoLogger.getExoLogger(SocialRestResponseFilter.class);
  private static final String JSONP              = "jsonp";
  
  @Override
  public void doFilter(GenericContainerResponse response) {
    Object entity = response.getEntity();
    if (entity == null) return;
    
    int status = response.getStatus();
    
    ApplicationContext acx = ApplicationContextImpl.getCurrent();
    ResponseBuilder responseBuilder = Response.fromResponse(response.getResponse());

    MultivaluedMap<String, String> reqHeaders = acx.getContainerRequest().getRequestHeaders();
    List<String> ifModifiedSince = reqHeaders.get(HttpHeaders.IF_MODIFIED_SINCE);
    List<String> ifNoneMatch = reqHeaders.get(HttpHeaders.IF_NONE_MATCH);
    
    String etag = acx.getProperty(RestProperties.ETAG);
    String updateDate = acx.getProperty(RestProperties.UPDATE_DATE);
    
    Date ifModifiedSinceDate = null;
    if (updateDate != null) { // Time base
      long lastModified = Long.parseLong(updateDate); // time in millisecond
      
      SimpleDateFormat df = new SimpleDateFormat(PATTERN_RFC1123);
      
      try {
        if (ifModifiedSince != null) {
          ifModifiedSinceDate = df.parse(ifModifiedSince.get(0));
          if (lastModified == ifModifiedSinceDate.getTime()) {
            status = Response.Status.NOT_MODIFIED.getStatusCode();
          }
        }
      } catch (ParseException e) {
        LOG.debug("Could not parse date time input: " + e.getMessage());
      }
      
      responseBuilder.lastModified(new Date(lastModified));
    } else if (etag != null) { // Content base
      if (ifNoneMatch != null) {
        String inmVal = ifNoneMatch.get(0).replaceAll(DOUBLE_QUOTE, StringUtils.EMPTY);
        if (inmVal.equals(etag)) {
          status = Response.Status.NOT_MODIFIED.getStatusCode();
        }
      }
      
      responseBuilder.tag(new EntityTag(etag));
    }

    String outputFields = ApplicationContextImpl.getCurrent().getQueryParameters().getFirst(FIELDS_QUERY_PARAM);
    boolean hasOutputFieldsFilter = outputFields != null && outputFields.length() > 0;

    if (hasOutputFieldsFilter) {
      entity = filterProperties(response.getEntity(), outputFields);
    }
    
    String callbackFunction = getQueryParam(JSONP);
     if (callbackFunction != null) {
      StringBuffer theStringBuffer = new StringBuffer();
      
      // add callback function name
      theStringBuffer.append(callbackFunction);
      
      // open 
      theStringBuffer.append("(");

      // serialize the POJO to JSON
      String theResponseString = serializeToJson(entity); 

      // add the JSON string
      theStringBuffer.append(theResponseString);
      
      // close 
      theStringBuffer.append(")");
      
      responseBuilder.entity(theStringBuffer.toString());  
    } else {
      responseBuilder.entity(entity);
    }
    
    CacheControl cc = new CacheControl();
    cc.setMaxAge(86400);
    cc.setPrivate(true);
    cc.setNoCache(false);
    
    response.setResponse(responseBuilder.cacheControl(cc)
                                        .status(status)
                                        .build());
  }

  private String serializeToJson(Object entity) {
    if (ResourceCollections.class.isAssignableFrom(entity.getClass())) {
      return new JSONObject((ResourceCollections)entity).toString();
    } else if (entity instanceof Map) {
      return new JSONObject((Map<String, Object>)entity).toString();
    }
    
    return StringUtils.EMPTY;
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
  
  private String getQueryParam(String queryParamName) {
    ApplicationContext acx = ApplicationContextImpl.getCurrent();
    return acx.getQueryParameters().getFirst(queryParamName);
  }
}
