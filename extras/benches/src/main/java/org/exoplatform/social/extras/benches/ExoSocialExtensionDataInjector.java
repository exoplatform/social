/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.social.extras.benches;


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.exoplatform.services.bench.DataInjector;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.extras.benches.util.Range;
import org.exoplatform.social.extras.benches.util.RangeCalculator;
/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvucong.78@gmail.com
 * Aug 4, 2011  
 */
public class ExoSocialExtensionDataInjector extends DataInjector {

  private static Log LOG = ExoLogger.getLogger(ExoSocialExtensionDataInjector.class);
  private long       numberOfUser;
  private long[]     relationRanks;
  private long[]     activityRanks;
  private long        numberOfSpace;
  private long[]     activitySpaceRanks;
  private ExoSocialDataInjectionExecutor injector;
  
  private Collection<Identity> identities = null;
  private Map<Space, Identity> identitySpacesMap = null;
  
  public ExoSocialExtensionDataInjector(ExoSocialDataInjectionExecutor injector) {
    this.injector = injector;
    
  }

  @Override
  public Object execute(HashMap<String, String> arg0) throws Exception {
    return null;
  }

  @Override
  public Log getLog() {
    return LOG;
  }

  @Override
  public void inject(HashMap<String, String> paramsMap) throws Exception {
    initParams(paramsMap);
    executeInject();
  }

  /**
   * Executes the DataInjector into database.
   */
  private void executeInject() {
    LOG.info("starting...");
    boolean nothingWasDone = true;
    nothingWasDone = executePeopleInjector(nothingWasDone);
    nothingWasDone = executeRelationInjector(nothingWasDone);
    nothingWasDone = executeActivityInjector(nothingWasDone);
    /*
     * Currently the UserPortal can not support to create UserNavigation in the RestService
     * so this code can not execute */
    nothingWasDone = executeSpaceInjector(nothingWasDone);
    nothingWasDone = executeAcivitySpaceInjector(nothingWasDone);
  
    if (nothingWasDone) {
      LOG.info("nothing to inject.");
    }
  }

  private boolean executeAcivitySpaceInjector(boolean nothingWasDone) {
    if (activitySpaceRanks.length > 0) {
      nothingWasDone = false;
      //Eache space have numberOfSpace spaces.
      numberOfSpace = numberOfSpace * numberOfUser;
      Range[] activitySpaceRanges = RangeCalculator.calculateRange(numberOfSpace, activitySpaceRanks);
      Space[] spaceArray = identitySpacesMap.keySet().toArray(new Space[0]);
      if (numberOfSpace != spaceArray.length) {
        throw new IllegalArgumentException("Wrong::the space array's length is not equals numberOfSpace.");
      }
      for(int i = 0; i < activitySpaceRanges.length; i++) {
        long low = activitySpaceRanges[i].getLow();
        long high = activitySpaceRanges[i].getHigh();
        long amount = activitySpaceRanges[i].getAmount();
        for (long j = low; j< high; j++) {
          Space space = spaceArray[(int)j];
          Identity identity = identitySpacesMap.get(space);
          LOG.info("\t> about to inject " + amount + " activity spaces.");
          injector.generateActivitySpace(space, identity, amount);
        }
      }
    }
    return nothingWasDone;
  }

  private boolean executeSpaceInjector(boolean nothingWasDone) {
    if (numberOfSpace > 0) {
      nothingWasDone = false;
      LOG.info("\t> about to inject " + numberOfSpace + " spaces.");
      identitySpacesMap = injector.generateSpaces(identities, numberOfSpace);
    }
    return nothingWasDone;
  }

  private boolean executeActivityInjector(boolean nothingWasDone) {
    if (activityRanks.length > 0) {
      nothingWasDone = false;
      Range[] activityRanges = RangeCalculator.calculateRange(numberOfUser, activityRanks);
      Identity[] identityArr = identities.toArray(new Identity[0]);
      //verifying the identity array.
      if (numberOfUser != identityArr.length) {
        throw new IllegalArgumentException("Wrong::the identity array's length is not equals numberOfUser.");
      }
      
      for(int i = 0; i < activityRanges.length; i++) {
        long low = activityRanges[i].getLow();
        long high = activityRanges[i].getHigh();
        long amount = activityRanges[i].getAmount();
        
        for (long j = low; j< high; j++) {
          Identity identity = identityArr[(int)j];
          LOG.info("\t> about to inject " + amount + " activities for identity = " + identity.getRemoteId());
          //can keep the Map<identity, activities> here.
          injector.generateActivities(identity, amount);
        }
      }
    }
    return nothingWasDone;
  }

  private boolean executePeopleInjector(boolean nothingWasDone) {
    if (numberOfUser > 0) {
      nothingWasDone = false;
      LOG.info("\t> about to inject " + numberOfUser + " people.");
      identities = injector.generatePeople(numberOfUser);
    }
    return nothingWasDone;
  }

  private boolean executeRelationInjector(boolean nothingWasDone) {
    if (relationRanks.length > 0) {
      nothingWasDone = false;
      Range[] relationRanges = RangeCalculator.calculateRange(numberOfUser, relationRanks);
      
      for(int i = 0; i < relationRanges.length; i++) {
        long low = relationRanges[i].getLow();
        long high = relationRanges[i].getHigh();
        long amount = relationRanges[i].getAmount();
        
        for (long j = low; j< high; j++) {
          LOG.info("\t> about to inject " + amount + " connections.");
          injector.generateRelations(amount);
        }
      }
    }
    return nothingWasDone;
  }
  
  /**
   * Parsing the Init parameters which is passed via request parameters.
   * @param paramsMap
   */
  private void initParams(HashMap<String, String> paramsMap) {
    if (paramsMap != null) {
      //Gets the maximum the User using for creating Users
      String value = paramsMap.get("mU");
      numberOfUser = longValue("mU", value);
      
      //Gets the maximum the Relationship using for creating Relationships
      value = paramsMap.get("mRpU");
      String[] values = value.split(",");
      relationRanks = longValues("mRpU", values);
      
      //Gets the maximum the Activity using for creating Activities
      value = paramsMap.get("mA");
      values = value.split(",");
      activityRanks = longValues("mA", values);
      
     //Gets the maximum the Space using for creating Spaces
      value = paramsMap.get("mS");
      numberOfSpace = IntValue("mS", value);
      
      //Gets the maximum the Space Activity using for each Space
      value = paramsMap.get("mSA");
      values = value.split(",");
      activitySpaceRanks = longValues("mSA", values);
    }
  }
  @Override
  public void reject(HashMap<String, String> arg0) throws Exception {
    
  }
  
  /**
   * Gets Long Value from param value.
   * @param property
   * @param value
   * @return
   */
  private long longValue(String property, String value) {
    try {
      if (value != null) {
        return Long.valueOf(value);
      }
    } catch (NumberFormatException e) {
      LOG.warn("Long number expected for property " + property);
    }
    return 0;
  }
  
  /**
   * Gets Long Value from param value.
   * @param property
   * @param value
   * @return
   */
  private long[] longValues(String property, String[] values) {
    long[] result = new long[values.length];
    try {
      if (values != null) {
        for(int i =0; i < values.length; i++)
          result[i] = Long.valueOf(values[i]);
      }
      return result;
    } catch (NumberFormatException e) {
      LOG.warn("Long number expected for property " + property);
    }
    return new long[0];
  }
  /**
   * Gets Long Value from param value.
   * @param property
   * @param value
   * @return
   */
  private int IntValue(String property, String value) {
    try {
      if (value != null) {
        return Integer.valueOf(value);
      }
    } catch (NumberFormatException e) {
      LOG.warn("Integer number expected for property " + property);
    }
    return 0;
  }
}
