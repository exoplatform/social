/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
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
package org.exoplatform.commons.search.service.search;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.exoplatform.commons.api.search.SearchServiceConnector;
import org.exoplatform.commons.api.search.data.SearchContext;
import org.exoplatform.commons.api.search.data.SearchResult;
import org.exoplatform.container.xml.InitParams;

/**
 * Created by The eXo Platform SAS
 * Author : Canh Pham Van
 *          canhpv@exoplatform.com
 * Apr 1, 2013  
 */
public class EventSearchConnector extends SearchServiceConnector {

  public EventSearchConnector(InitParams initParams) {
    super(initParams);
    // TODO Auto-generated constructor stub
  }

  @Override
  public Collection<SearchResult> search(SearchContext context,
                                         String query,
                                         Collection<String> sites,
                                         int offset,
                                         int limit,
                                         String sort,
                                         String order) {
    List<SearchResult> results = new ArrayList<SearchResult>();
    
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/mm/dd");
    Date createDate = null;
    try {
      createDate = sdf.parse("2013/03/29");
    } catch (ParseException e) {
      // TODO Auto-generated catch block
    }    
    
    SearchResult result1 = new SearchResult("calendar/details/Eventc4b1f07e7f0001010101c2cdd3cc2f8c"
                                           , "Unitest seminar"                                           
                                           , "Unitest seminar more detail"
                                           , "Root Root - Monday, April 1, 2013 8:30 PM", null, createDate.getTime(), 587);
        
    Date createDate1 = null;
    try {
      createDate1 = sdf.parse("2013/04/01");
    } catch (ParseException e) {
      // TODO Auto-generated catch block
    }     
    
    SearchResult result2 = new SearchResult("calendar/details/Eventc4b1f07e7f0001010101c2cdd3cc2f8d"
                                           , "Unitest seminar 1"                                           
                                           , "Unitest seminar more detail 1"
                                           , "Root Root - Monday, April 1, 2013 8:30 PM", null, createDate1.getTime(), 587);
    
    results.add(result1);
    results.add(result2);
    
    return results;
  }

}
