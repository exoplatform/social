package org.exoplatform.social.core.binding.listener;

import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;
import org.exoplatform.social.core.binding.model.UserSpaceBinding;
import org.exoplatform.social.core.binding.spi.GroupSpaceBindingService;

public class SpaceBindingUserEventListener extends UserEventListener {

  private GroupSpaceBindingService groupSpaceBindingService;

  public SpaceBindingUserEventListener(GroupSpaceBindingService groupSpaceBindingService) {
    this.groupSpaceBindingService = groupSpaceBindingService;
  }

  @Override
  public void postDelete(User user) throws Exception {
    for (UserSpaceBinding userSpaceBinding : groupSpaceBindingService.findUserBindingsByUser(user.getUserName())) {
      groupSpaceBindingService.deleteUserBinding(userSpaceBinding);
    }
  }
}
