/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Affero General Public License
* as published by the Free Software Foundation; either version 3
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.impl.StorageUtils;

public class StorageUtilsTest extends TestCase {

  public void testProcessUnifiedSearchCondition() throws Exception {
    List<String> result = new ArrayList<String>();
    result.add("%first%");
    result.add("%%two%");
    result.add("%three%%");
    result.add("%%four%%");
    result.add("%%five%%");
    assertEquals(result, StorageUtils.processUnifiedSearchCondition("first *two three% %four* *five*"));
  }
  
  public void testSortMapByValue() throws Exception {
    Map<String, Integer> map = new HashMap<String, Integer>();
    map.put("value1", 1);
    map.put("value12", 12);
    map.put("value10", 10);
    map.put("value9", 9);
    map.put("value3", 3);
    map.put("value5", 0);

    Map<String, Integer> ascMap = new LinkedHashMap<String, Integer>();
    ascMap.put("value5", 0);
    ascMap.put("value1", 1);
    ascMap.put("value3", 3);
    ascMap.put("value9", 9);
    ascMap.put("value10", 10);
    ascMap.put("value12", 12);

    Map<String, Integer> descMap = new LinkedHashMap<String, Integer>();
    descMap.put("value12", 12);
    descMap.put("value10", 10);
    descMap.put("value9", 9);
    descMap.put("value3", 3);
    descMap.put("value1", 1);
    descMap.put("value5", 0);

    assertEquals(ascMap, StorageUtils.sortMapByValue(map, true));
    assertEquals(descMap, StorageUtils.sortMapByValue(map, false));

  }
  
  public void testGetCommonItemNumber() throws Exception {
    List<String> list1 = new ArrayList<String>();
    list1.add("a");
    list1.add("b");
    list1.add("c");
    list1.add("d");
    list1.add("e");
    List<String> list2 = new ArrayList<String>();
    list2.add("b");
    list2.add("d");
    list2.add("x");
    list2.add("y");
    List<String> list3 = new ArrayList<String>();
    list3.add("m");
    list3.add("e");
    list3.add("p");
    
    assertEquals(2, StorageUtils.getCommonItemNumber(list1, list2));
    assertEquals(2, StorageUtils.getCommonItemNumber(list2, list1));
    
    assertEquals(1, StorageUtils.getCommonItemNumber(list1, list3));
    assertEquals(1, StorageUtils.getCommonItemNumber(list3, list1));
    
    assertEquals(0, StorageUtils.getCommonItemNumber(list2, list3));
    assertEquals(0, StorageUtils.getCommonItemNumber(list3, list2));
    
  }
  
  public void testSubList() throws Exception {
    List<String> list = new ArrayList<String>();
    
    for (int i = 0; i < 20; i++) {
      list.add(""+i);
    }
    
    List<String> loaded = StorageUtils.subList(list, 0, 10);
    
    assertEquals(10, loaded.size());
    
    loaded = StorageUtils.subList(list, 0, 25);
    
    assertEquals(20, loaded.size());
    
    loaded = StorageUtils.subList(list, 19 , 25);
    
    assertEquals(1, loaded.size());
    
    loaded = StorageUtils.subList(list, 10, 25);
    
    assertEquals(10, loaded.size());
    
    loaded = StorageUtils.subList(list, 15 , 15);
    
    assertEquals(0, loaded.size());
    
    loaded = StorageUtils.subList(list, 20 , 25);
    
    assertEquals(0, loaded.size());
    
    loaded = StorageUtils.subList(list, 25 , 10);
    
    assertEquals(0, loaded.size());
    
    loaded = StorageUtils.subList(list, 25 , 30);
    
    assertEquals(0, loaded.size());
    
  }
  
  public void testSortSpaceByName() {
    Space space1 = new Space();
    space1.setDisplayName("XYZ");
    Space space2 = new Space();
    space2.setDisplayName("ABC");
    Space space3 = new Space();
    space3.setDisplayName("GHE");
    
    List<Space> list = new LinkedList<Space>();
    list.add(space1);
    list.add(space2);
    list.add(space3);
    
    assertEquals("XYZ", list.get(0).getDisplayName());
    assertEquals("ABC", list.get(1).getDisplayName());
    assertEquals("GHE", list.get(2).getDisplayName());
    
    StorageUtils.sortSpaceByName(list, true);
    
    assertEquals("ABC", list.get(0).getDisplayName());
    assertEquals("GHE", list.get(1).getDisplayName());
    assertEquals("XYZ", list.get(2).getDisplayName());
    
  }
  
  public void testSortIdentitiesByFullName() {
    Identity id1 = new Identity("id1");
    Profile profile = id1.getProfile();
    profile.setProperty(Profile.FULL_NAME, "Xyz");
    
    Identity id2 = new Identity("id2");
    profile = id2.getProfile();
    profile.setProperty(Profile.FULL_NAME, "BCD");
    
    Identity id3 = new Identity("id3");
    profile = id3.getProfile();
    profile.setProperty(Profile.FULL_NAME, "Abc");
    
    //
    List<Identity> list = new LinkedList<Identity>();
    list.add(id1);
    list.add(id2);
    list.add(id3);
    
    //before sort
    assertEquals("Xyz", list.get(0).getProfile().getFullName());
    assertEquals("Abc", list.get(2).getProfile().getFullName());
    
    //
    StorageUtils.sortIdentitiesByFullName(list, true);
    
    //after sort
    assertEquals("Abc", list.get(0).getProfile().getFullName());
    assertEquals("Xyz", list.get(2).getProfile().getFullName());
  }
  
  public void testEscapeSpecialCharacter() {
    String s = "! . , : ; ( ) ^}{[] -, \" '% *";
    assertEquals("\\! . , \\: ; \\( \\) \\^\\}\\{\\[\\] \\-, \\\" ''% \\*", StorageUtils.escapeSpecialCharacter(s));
  }

}
