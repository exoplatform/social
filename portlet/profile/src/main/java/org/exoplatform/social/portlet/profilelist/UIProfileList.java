package org.exoplatform.social.portlet.profilelist;

import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.impl.organization.OrganizationIdentityProvider;
import org.exoplatform.social.core.relationship.RelationshipManager;
import org.exoplatform.social.core.relationship.Relationship;
import org.exoplatform.social.portlet.profile.UIProfileSection;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.web.application.RequestContext;

import java.util.List;
import java.util.ArrayList;

@ComponentConfig(
    template =  "app:/groovy/portal/webui/component/UIProfileList.gtmpl",
    events = {
        @EventConfig(listeners = UIProfileList.ChangeViewActionListener.class)
    }
)
public class UIProfileList extends UIContainer {
  List tempList = null;
  Identity currIdentity = null;
  List<Relationship> tempRelList = null;
  Type currType = null;
  Type displayType = null;

    public enum Type {
      ALL,
      CONTACTS,
      PENDING;
    }

    public enum Status {
      ALIEN,
      CONTACTS,
      PENDING,
      REQUIRE_VALIDATION,
      SELF;
    }


  public UIProfileList() throws Exception {
     addChild(UIDisplayProfileList.class, null, null);
  }


    public List getList() {
      return tempList;
    }

    public Type getCurrentType(){
      return currType;
    }

  public List load(Type type) throws Exception {
    if(type.equals(Type.ALL)) {
      tempList = loadAllProfiles();
    } else if(type.equals(Type.PENDING)) {
      tempList = loadPendingList();
    } else if(type.equals(Type.CONTACTS)) {
      tempList = loadContactList();
    }

    currType = type;
    return tempList;
  }


  public void unloadTemporaryVar() {
    tempList = null;
    currType = null;
    tempRelList = null;
  }

  public Status getContactStatus(Identity identity) throws Exception {
    if (tempRelList == null)
      tempRelList = loadRelationList();
    if(identity.getId().equals(getCurrentIdentity().getId()))
      return Status.SELF;
    for (Relationship rel : tempRelList) {
      if (rel.getIdentity1().getId().equals(identity.getId()) || rel.getIdentity2().getId().equals(identity.getId())) {
        if (rel.getStatus().equals(Relationship.Type.CONFIRM))
          return Status.CONTACTS;
        else if (rel.getStatus().equals(Relationship.Type.PENDING)) {
          if(rel.getIdentity2().getId().equals(identity.getId()))
            return Status.PENDING;
          else
            return Status.REQUIRE_VALIDATION;
        }
        else if (rel.getStatus().equals(Relationship.Type.IGNORE)) {
            //TODO to change
          return Status.PENDING;
        }
      }
    }
    return Status.ALIEN;
  }

  public void processRender(WebuiRequestContext context) throws Exception {
    try {
      super.processRender(context);
    } finally {
      //make sure we don't keep in memory the list of profile anbd relations
      unloadTemporaryVar();
    }
  }


 private List<Identity> loadAllProfiles() throws Exception {
   ExoContainer container = ExoContainerContext.getCurrentContainer();
   IdentityManager im = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
   List<Identity> ids = im.getIdentities("organization");

   return ids;
 }

  private List<Relationship> loadPendingList() throws Exception {
   ExoContainer container = ExoContainerContext.getCurrentContainer();
   RelationshipManager rm = (RelationshipManager) container.getComponentInstanceOfType(RelationshipManager.class);
      
   Identity currId = getCurrentIdentity();

   return rm.getPending(currId, true);

 }

 private List<Relationship> loadContactList() throws Exception {
   ExoContainer container = ExoContainerContext.getCurrentContainer();
   RelationshipManager rm = (RelationshipManager) container.getComponentInstanceOfType(RelationshipManager.class);

   Identity currId = getCurrentIdentity();

   return rm.getContacts(currId);
 }

  private List<Relationship> loadRelationList() throws Exception {
   ExoContainer container = ExoContainerContext.getCurrentContainer();
   RelationshipManager rm = (RelationshipManager) container.getComponentInstanceOfType(RelationshipManager.class);

   Identity currId = getCurrentIdentity();

   return rm.get(currId);
 }

    public String getCurrentUserName() {
      // if we are not on the page of a user, we display the profile of the current user
      RequestContext context = RequestContext.getCurrentInstance();
      return context.getRemoteUser();
    }

  public Identity getCurrentIdentity() throws Exception {
    if(currIdentity == null) {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      IdentityManager im = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
      currIdentity = im.getIdentityByRemoteId("organization", getCurrentUserName());
    }
    return currIdentity;
  }


  public static class ChangeViewActionListener extends EventListener<UIProfileList> {

    public void execute(Event<UIProfileList> event) throws Exception {
      UIProfileList pl = event.getSource();
      String type = event.getRequestContext().getRequestParameter(OBJECTID);
      pl.setDisplayType(UIProfileList.Type.valueOf(type));

      event.getRequestContext().addUIComponentToUpdateByAjax(pl);
    }
  }

  private void setDisplayType(Type type) {
    this.displayType = type;
  }

  public Type getDisplayType() {
    return displayType;
  }
}
