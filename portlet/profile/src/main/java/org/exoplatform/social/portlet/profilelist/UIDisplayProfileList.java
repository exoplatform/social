package org.exoplatform.social.portlet.profilelist;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.impl.organization.OrganizationIdentityProvider;
import org.exoplatform.social.core.relationship.RelationshipManager;
import org.exoplatform.social.core.relationship.Relationship;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;

import java.util.List;

@ComponentConfig(
    template =  "app:/groovy/portal/webui/component/UIDisplayProfileList.gtmpl",
    events = {
        @EventConfig(listeners = UIDisplayProfileList.AddContactActionListener.class),
        @EventConfig(listeners = UIDisplayProfileList.AcceptContactActionListener.class)
    }
)
public class UIDisplayProfileList  extends UIComponent {


 public List<Profile> getList() throws Exception {
   return ((UIProfileList)this.getParent()).getList();
 }

 public boolean isRelationshipList() {
   UIProfileList.Type type = ((UIProfileList)this.getParent()).getCurrentType();
   return type.equals(UIProfileList.Type.PENDING) || type.equals(UIProfileList.Type.CONTACTS);
 }

 public UIProfileList.Type getCurrentType() {
   return ((UIProfileList)this.getParent()).getCurrentType();
 }

 public Identity getCurrentIdentity() throws Exception {
   return ((UIProfileList)this.getParent()).getCurrentIdentity();
 }

  public UIProfileList.Status getContactStatus(Identity identity) throws Exception {
   return ((UIProfileList)this.getParent()).getContactStatus(identity);
 }

  public static class AddContactActionListener extends EventListener<UIDisplayProfileList> {

    public void execute(Event<UIDisplayProfileList> event) throws Exception {
      UIDisplayProfileList portlet = event.getSource();

      String userId = event.getRequestContext().getRequestParameter(OBJECTID);
      String currUserId = ((UIProfileList)portlet.getParent()).getCurrentUserName();

      ExoContainer container = ExoContainerContext.getCurrentContainer();
      IdentityManager im = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
      Identity currIdentity = im.getIdentityByRemoteId(OrganizationIdentityProvider.NAME, currUserId);

      Identity requestedIdentity = im.getIdentityById(userId);

      RelationshipManager rm = (RelationshipManager) container.getComponentInstanceOfType(RelationshipManager.class);

      Relationship rel = rm.getRelationship(currIdentity, requestedIdentity);

      if(rel == null) {
        rel = rm.create(currIdentity, requestedIdentity);
        rel.setStatus(Relationship.Type.PENDING);
        rm.save(rel);
      }
    }
  }



  public static class AcceptContactActionListener extends EventListener<UIDisplayProfileList> {

    public void execute(Event<UIDisplayProfileList> event) throws Exception {
      UIDisplayProfileList portlet = event.getSource();

      String userId = event.getRequestContext().getRequestParameter(OBJECTID);
      String currUserId = ((UIProfileList)portlet.getParent()).getCurrentUserName();

      ExoContainer container = ExoContainerContext.getCurrentContainer();
      IdentityManager im = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
      Identity currIdentity = im.getIdentityByRemoteId(OrganizationIdentityProvider.NAME, currUserId);

      Identity requestedIdentity = im.getIdentityById(userId);

      RelationshipManager rm = (RelationshipManager) container.getComponentInstanceOfType(RelationshipManager.class);

      Relationship rel = rm.getRelationship(currIdentity, requestedIdentity);

      rel.setStatus(Relationship.Type.CONFIRM);
      rm.save(rel);
    }
  }
}
