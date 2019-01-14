package org.exoplatform.social.service.test;

import org.apache.commons.lang3.StringUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.commons.utils.ListAccessImpl;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.impl.GroupImpl;
import org.exoplatform.services.organization.search.GroupSearchService;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class MockGroupSearchService implements GroupSearchService {

  @Override
  public ListAccess<Group> searchGroups(String keyword) throws Exception {
    List<Group> allGroups = Arrays.asList(new GroupImpl("/users"), new GroupImpl("/administrators"));
    
    List<Group> groups = new LinkedList<>();
    if(StringUtils.isBlank(keyword)) {
      return new ListAccessImpl<>(Group.class, allGroups);
    } else {
        String lowerCaseKeyword = keyword.toLowerCase();
        for (Group group : allGroups) {
          if (group.getGroupName().toLowerCase().contains(lowerCaseKeyword)) {
            groups.add(group);
          }
        }
    }
    return new ListAccessImpl<>(Group.class, groups);
  }
}
