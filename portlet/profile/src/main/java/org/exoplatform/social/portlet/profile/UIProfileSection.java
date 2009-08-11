/**
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.portlet.profile;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.social.core.identity.ProfileMapper;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SARL
 * Modified : dang.tung
 *          tungcnw@gmail.com
 * Aug 11, 2009          
 */

@ComponentConfig(
  template = "system:/groovy/webui/form/UIForm.gtmpl",
  lifecycle = UIFormLifecycle.class,
  events = {
    @EventConfig(listeners = UIProfileSection.SaveActionListener.class),
    @EventConfig(listeners = UIProfileSection.CancelActionListener.class, phase = Phase.DECODE)
  }
)
public abstract class UIProfileSection extends UIForm {
  private boolean isEditMode;
  private boolean isMultipart = false;
  private String currentProperty;
  private ProfileMapper profilemapper;


  public Profile getProfile() throws Exception {
    UIProfile uiProfile = this.getAncestorOfType(UIProfile.class);
    return uiProfile.getProfile();
  }

  public boolean isEditMode() {
    return this.isEditMode;
  }

  public void setEditMode(boolean editMode) {
    this.isEditMode = editMode;
  }

  public boolean isEditable() {
    UIProfile pp = this.getAncestorOfType(UIProfile.class);
    return pp.isEditable();
  }

  public boolean isMultipart() {
    return isMultipart;
  }

  public void setMultipart(boolean multipart) {
    isMultipart = multipart;
  }

  public String getCurrentProperty() {
    return currentProperty;
  }

  public void setCurrentProperty(String currentProperty) {
    this.currentProperty = currentProperty;
  }

//  public void processDecode(WebuiRequestContext context) throws Exception {
//
//    Map params = ((PortletRequest) context.getRequest()).getParameterMap();
//
//    Iterator it1 = params.keySet().iterator();
//      while (it1.hasNext()) {
//        String paramkey = (String) it1.next();
//        String[] values = ((PortletRequest) context.getRequest()).getParameterValues(paramkey);
//        for(String value : values) {
//          System.out.println("profile: " + paramkey + "=" + value);
//        }
//      }
//
//    //if we are going to save, we need to read the parameters
//    if (params.get("op") != null && "Save".equals(((String[])params.get("op"))[0])) {
//      System.out.println("going to save");
//      Map profileInfo = new HashMap();
//
//      //need to be done only on save
//      Profile p = getProfile();
//
//      Iterator it = params.keySet().iterator();
//      while (it.hasNext()) {
//        String paramkey = (String) it.next();
//
//        if (paramkey.startsWith("profile.")) {
//          String name = paramkey.substring(8);
//          String[] values = ((PortletRequest) context.getRequest()).getParameterValues(paramkey);
//          profileInfo.put(name, values);
//          System.out.println("profile: " + name + "=" + profileInfo.get(name));
//        }
//      }
//      getProfileMapper().copy(profileInfo, p);
//
//      ExoContainer container = ExoContainerContext.getCurrentContainer();
//      IdentityManager im = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
//
//      im.saveProfile(p);
//    }
//    super.processDecode(context);
//  }

  public static class EditActionListener extends EventListener<UIProfileSection> {

    public void execute(Event<UIProfileSection> event) throws Exception {
      UIProfileSection sect = event.getSource();

      sect.setEditMode("true".equals(event.getRequestContext().getRequestParameter(OBJECTID)));

      event.getRequestContext().addUIComponentToUpdateByAjax(sect);
    }
  }

  public static class SaveActionListener extends EventListener<UIProfileSection> {

    public void execute(Event<UIProfileSection> event) throws Exception {
      UIProfileSection sect = event.getSource();

      sect.setEditMode(false);

      event.getRequestContext().addUIComponentToUpdateByAjax(sect);
    }
  }

  public static class CancelActionListener extends EventListener<UIProfileSection> {

    public void execute(Event<UIProfileSection> event) throws Exception {
      UIProfileSection sect = event.getSource();

      sect.setEditMode(false);

      event.getRequestContext().addUIComponentToUpdateByAjax(sect);
    }
  }

//  protected void beginEditMode(Writer writer) throws Exception {
//    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
//    String b = context.getURLBuilder().createURL(this, null, null);
//
//    writer.
//        append("<form class=\"UIForm\" name=\"form_").append(getId()).
//        append("\" id=\"form_").append(getId()).append("\" action=\"").
//        append(b).append("\" onSubmit=\"").append(eventSubmit("Save")).
//        append(";return false;\"");
//    if (isMultipart) {
//      writer.append(" enctype=\"multipart/form-data\"");
//    }
//    writer.append(" method=\"post\">");
//    writer.append("<input type=\"hidden\" name=\"op\" value=\"\"/>");
//
//  }
//
//  protected void endEditMode(Writer writer) throws IOException {
//    writer.write("</form>");
//    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
//    context.getJavascriptManager().addOnLoadJavascript("eXo.social.profile.UIProfileSection.initForm(\"" + getId() + "\")");
//  }
//
//  protected void begin(Writer writer) throws IOException {
//    writer.append("<div id=\"").append(getId()).append("\" class=\"UIProfileSection\">");
//  }
//
//  protected void end(Writer writer) throws IOException {
//    writer.write("</div>");
//  }

//  @Override
//  public void processRender(WebuiRequestContext context) throws Exception {
//
//    Writer writer = context.getWriter();
//
//    begin(writer);
//
//    if (isEditMode())
//      beginEditMode(writer);
//
//    super.processRender(context);
//
//    if (isEditMode())
//      endEditMode(writer);
//
//    end(writer);
//  }

//  public String eventSubmit(String name) throws Exception {
//
//    StringBuilder b = new StringBuilder();
//    b.append("javascript:eXo.social.profile.UIProfileSection.submitForm('")
//        .append(getId()).append("', '").append(name).append("')");
//    return b.toString();
//  }

  private ProfileMapper getProfileMapper() {
    if (this.profilemapper == null) {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      this.profilemapper = (ProfileMapper) container.getComponentInstanceOfType(ProfileMapper.class);
    }
    return profilemapper;  
  }

}
