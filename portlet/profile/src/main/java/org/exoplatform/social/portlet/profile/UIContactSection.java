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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.ExpressionValidator;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.StringLengthValidator;

/**
 * Created by The eXo Platform SARL
 * Modified : hanh.vi
 *          hanhvq@gmail.com
 * Aug 18, 2009          
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/groovy/portal/webui/component/UIContactSection.gtmpl",
    events = {
        @EventConfig(listeners = UIContactSection.EditActionListener.class, phase=Phase.DECODE),
        @EventConfig(listeners = UIContactSection.SaveActionListener.class),
        @EventConfig(listeners = UIProfileSection.CancelActionListener.class, phase=Phase.DECODE),
        @EventConfig(listeners = UIContactSection.AddActionListener.class),
        @EventConfig(listeners = UIContactSection.RemoveActionListener.class, phase=Phase.DECODE)
    }
)
public class UIContactSection extends UIProfileSection {
  /** Number of email. */
  private int emailCount = 0;
  /** Number of phone. */
  private int phoneCount = 0;
  /** Number of ims. */
  private int imsCount = 0;
  /** Number of url. */
  private int urlCount = 0;
  /** Index of email. */
  private int emailIdx = 0;
  /** Index of phone. */
  private int phoneIdx = 0;
  /** Index of ims. */
  private int imsIdx = 0;
  /** Index of url. */
  private int urlIdx = 0;
  
  /** Get the number of email. */
  public int getEmailCount() {
    return emailCount;
  }

  /** Get the number of phone. */
  public int getPhoneCount() {
    return phoneCount;
  }

  /** Get the number of ims. */
  public int getImsCount() {
    return imsCount;
  }

  /** Get the number of url. */
  public int getUrlCount() {
    return urlCount;
  }

  /**
   * Constructor.<br>
   * 
   * @throws Exception
   */
  public UIContactSection() throws Exception {
    addChild(UITitleBar.class, null, null);
  }
  
  /**
   * Get and sort all uicomponents.<br>
   *  
   * @return All children in order.
   */
  public List<UIComponent> getChilds() {
    List<UIComponent> listChild = getChildren();
    return sort(listChild);
  }
  
  /**
   * Get all children of email.<br>
   * 
   * @return All email children in order.
   */
  public List<UIComponent> getEmailChilds() {
    return sortSubList(getSubList(0, emailCount));
  }
  
  /**
   * Get all children of Phone.<br>
   * 
   * @return All Phone children in order.
   */
  public List<UIComponent> getPhoneChilds() {
    return sortSubList(getSubList(emailCount, emailCount + phoneCount));
  }
  
  /**
   * Get all children of ims.<br>
   * 
   * @return All ims children in order.
   */
  public List<UIComponent> getImsChilds() {
    return sortSubList(getSubList(emailCount + phoneCount, emailCount + phoneCount + imsCount));
  }
  
  /**
   * Get all children of URL.<br>
   * 
   * @return All URL children in order.
   */
  public List<UIComponent> getUrlChilds() {
    return sortSubList(getSubList( emailCount + phoneCount + imsCount, emailCount + phoneCount + imsCount + urlCount));
  }
  
  /**
   *  Store profile information into database when form is submitted.<br>
   *
   */
  public static class SaveActionListener extends UIProfileSection.SaveActionListener {

    public void execute(Event<UIProfileSection> event) throws Exception {
      super.execute(event);
      
      UIProfileSection sect = event.getSource();
      UIContactSection uiContactSectionSect = (UIContactSection)sect;
      
      uiContactSectionSect.saveProfileInfo();   
    }
  }
  
  /**
   * Add component when user click add button.<br>
   *
   */
  public static class AddActionListener extends EventListener<UIContactSection> {

    public void execute(Event<UIContactSection> event) throws Exception {
      UIContactSection sect = event.getSource();
      String typeOfComp = event.getRequestContext().getRequestParameter(OBJECTID);
      sect.addUIFormInput(typeOfComp);
    }    
  }

  /**
   * Remove the component that user selected for removing.<br>
   *
   */
  public static class RemoveActionListener extends EventListener<UIContactSection> {

    public void execute(Event<UIContactSection> event) throws Exception {
      UIContactSection sect = event.getSource();
      String comps = event.getRequestContext().getRequestParameter(OBJECTID);
      String uiComp1 = comps.substring(0, comps.indexOf("."));
      String uiComp2 = comps.substring(comps.indexOf(".") + 1, comps.length());
      sect.removeFormInput(uiComp1, uiComp2);
    }    
  }
  
  /**
   * Change form into edit mode when user click eddit button.<br>
   *
   */
  public static class EditActionListener extends UIProfileSection.EditActionListener {

    public void execute(Event<UIProfileSection> event) throws Exception {
      super.execute(event);
      UIProfileSection sect = event.getSource();
      UIContactSection uiContactSectionSect = (UIContactSection)sect;
      uiContactSectionSect.setValue();
      event.getRequestContext().addUIComponentToUpdateByAjax(sect);
    }
  }
  
  /**
   * Get profile information from components and save into Profile.<br>
   * 
   * @throws Exception
   */
  private void saveProfileInfo() throws Exception {
    ArrayList<HashMap<String, String>> emails = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String, String>> phones = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String, String>> ims = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String, String>> urls = new ArrayList<HashMap<String, String>>();
    
    List<UIComponent> listEmailUIComp = getEmailChilds();
    List<UIComponent> listPhoneUIComp = getPhoneChilds();
    List<UIComponent> listIMSUIComp = getImsChilds();
    List<UIComponent> listURLUIComp = getUrlChilds();

    emails = getProfileForSave(emailCount, listEmailUIComp, null);
    phones = getProfileForSave(phoneCount, listPhoneUIComp, "work");
    ims = getProfileForSave(imsCount, listIMSUIComp, null);
    urls = getProfileForSave(urlCount, listURLUIComp, "url");
    
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    IdentityManager im = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
    
    Profile p = getProfile();     
    
    p.setProperty("emails", emails);
    p.setProperty("phones", phones);
    p.setProperty("ims", ims);
    p.setProperty("urls", urls);
    
    im.saveProfile(p);
  }

  /**
   * Get information input by user for saving profile.<br>
   * 
   * @param count Number of children.
   * @param listUIComp Contains children of each input type (email, phone,...).
   * @param uiStringType Type of component.
   * 
   * @return All profile information.
   */
  private ArrayList<HashMap<String, String>> getProfileForSave(int count, List<UIComponent> listUIComp, String uiStringType) {
    ArrayList<HashMap<String, String>> profileMap = new ArrayList<HashMap<String, String>>();
    String value = null;
    String key = null;
    UIFormStringInput uiStringInput = null;
    UIFormSelectBox uiSelectBox = null;
    if ("work".equals(uiStringType)) {
      for (int i = 0; i < count; i++) {       
        HashMap<String, String> uiMap = new HashMap<String, String>();
        key = uiStringType;
        uiStringInput = (UIFormStringInput) listUIComp.get(i); 
        value = uiStringInput.getValue();

        uiMap.put("key",key);
        uiMap.put("value", value);
        profileMap.add(uiMap);
      }
    } else {
      for (int i = 0; i < count; i+=2) {       
        HashMap<String, String> uiMap = new HashMap<String, String>();
        if (uiStringType == null) {            
          uiSelectBox = (UIFormSelectBox) listUIComp.get(i);
          key = uiSelectBox.getValue();
          uiStringInput = (UIFormStringInput) listUIComp.get(i+1); 
          value = uiStringInput.getValue();
        } else {
          uiStringInput = (UIFormStringInput) listUIComp.get(i);
          key = uiStringInput.getValue();
          uiStringInput = (UIFormStringInput) listUIComp.get(i+1); 
          value = uiStringInput.getValue();
        }          
        
        uiMap.put("key",key);
        uiMap.put("value", value);
        profileMap.add(uiMap);
      }
    }
    return profileMap;
  }
  
  /**
   * Get children of each type (email, phone, ...) from the all children list.<br>
   * 
   * @param startIdx Start index of sublist.
   * @param endIdx End index of sublist.
   * 
   * @return A list contains children of each type.
   */
  private List<UIComponent> getSubList(int startIdx, int endIdx) {
    List<UIComponent> rtnList = new ArrayList<UIComponent>();
    for (int idx = startIdx; idx < endIdx; idx++) {
      rtnList.add(getChilds().get(idx));
    }
    
    return rtnList;
  }
  
  /**
   * Set value for components.<br>
   * 
   * @throws Exception
   */
  private void setValue() throws Exception {
    setValueByType(getEmailChilds(), "emails");
    setValueByType(getPhoneChilds(), "phones");
    setValueByType(getImsChilds(), "ims");
    setValueByType(getUrlChilds(), "urls");
  }
  
  /**
   * Set value for components with each type.<br>
   * 
   * @param listChilds List children for setting.
   * @param uiType Type for setting.
   * 
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  private void setValueByType(List<UIComponent> listChilds, String uiType) throws Exception {
    Profile profile = getProfile();  
    ArrayList<HashMap<String, String>> profiles = (ArrayList<HashMap<String, String>>) profile.getProperty(uiType);
    List<String> listProfile = new ArrayList<String>();    
    int listChildSize = listChilds.size();
    
    if ((profiles == null) || (profiles.size() == 0)) {
      if (listChildSize != 0) {
        if ("phones".equals(uiType)) {
          String id1 = listChilds.get(listChildSize-1).getName();
          removeFormInput(id1, null);
          return;
        } else {
          String id1 = listChilds.get(listChildSize-1).getName();
          String id2 = listChilds.get(listChildSize-2).getName();
          removeFormInput(id1, id2);
          return;
        }
      } else {
        return;
      }
    } else {
      for (HashMap<String, String> map : profiles) {
        listProfile.add(map.get("key"));
        listProfile.add(map.get("value"));
      }
      
      int listProfileSize = listProfile.size();
      
      if ("phones".equalsIgnoreCase(uiType)) {
        int run = listProfileSize;
        while( run > 2*listChildSize) {
          addUIFormInput("phone");
          run -= 2;
        }
        int run1 = listProfileSize;
        while( run1 < 2*listChildSize) {
          String id1 = listChilds.get(listChildSize-1).getName();
          removeFormInput(id1, null);
          run1 += 2;
        }
        
        for (int i = 0; i < listProfileSize/2; i++) {
          ((UIFormInput)getPhoneChilds().get(i)).setValue(listProfile.get(2*i + 1));
        }
      } else {
        int run2 = listProfileSize;
        while (run2 > listChildSize) {
          if ("emails".equals(uiType)) {
            addUIFormInput("email");
          } else if ("ims".equals(uiType)) {
            addUIFormInput("ims");
          } else if ("urls".equals(uiType)) {
            addUIFormInput("rlu");
          }
          run2 -= 2;
        }
        int run3 = listProfileSize;
        while (run3 < listChildSize) {
          String id1 = listChilds.get(listChildSize-1).getName();
          String id2 = listChilds.get(listChildSize-2).getName();
          removeFormInput(id1, id2);
          
          run3 += 2;
        }
        
        if ("emails".equals(uiType)) {
          for (int i = 0; i <= listProfileSize - 1; i++) {
            ((UIFormInput)getEmailChilds().get(i)).setValue(listProfile.get(i));
          }
        } else if ("ims".equals(uiType)) {
          for (int i = 0; i <= listProfileSize - 1; i++) {
            ((UIFormInput)getImsChilds().get(i)).setValue(listProfile.get(i));
          }
        } else if ("urls".equals(uiType)) {
          for (int i = 0; i <= listProfileSize - 1; i++) {
            ((UIFormInput)getUrlChilds().get(i)).setValue(listProfile.get(i));
          }
        }
      }
    }
  }

  /**
   * Remove components by input id.<br>
   * 
   * @param id1 The id of first component.
   * @param id2 The id of next component.
   */
  private void removeFormInput(String id1, String id2) {
    if (id1.startsWith("fone")) {
      removeChildById(id1);
      --phoneCount;
    } else {      
      removeChildById(id1);
      removeChildById(id2);
      if (id1.startsWith("email")) {
        emailCount -= 2;
      } else if (id1.startsWith("ims")) {
        imsCount -= 2;
      } else if (id1.startsWith("rlu")) {
        urlCount -= 2;
      }
    }
  }
  
  /**
   * Add component with the input type.<br>
   * 
   * @param type Type of component is added (email, phone, ...).
   * 
   * @throws Exception
   */
  private void addUIFormInput(String type) throws Exception {
    if ("email".equals(type)) {
      createUISelectBox(new String[]{"Work", "Home", "Other"}, "email");
      addUIFormInput(new UIFormStringInput("email" + (++emailIdx), null, null)
      .addValidator(MandatoryValidator.class)
      .addValidator(StringLengthValidator.class, 3, 30).addValidator(ExpressionValidator
      .class, "^([^@\\s]+)@((?:[-a-z0-9]+\\.)+[a-z]{2,})$", "UIContactSect.msg.Invalid-email"));
    } else if ("phone".equals(type)) {
      phoneCount += 1;
      addUIFormInput(new UIFormStringInput("fone" + (++phoneIdx),null,null)
      .addValidator(MandatoryValidator.class)
      .addValidator(StringLengthValidator.class, 3, 20)
      .addValidator(ExpressionValidator.class, "^[\\d\\s ().-]+$", "UIContactSect.msg.Invalid-phone"));
    } else if ("ims".equals(type)) {
      createUISelectBox(new String[]{"Gtalk", "Msn", "Skype", "Yahoo", "Other"}, "ims");
      addUIFormInput(new UIFormStringInput("ims" + (++imsIdx),null,null)
      .addValidator(MandatoryValidator.class)
      .addValidator(StringLengthValidator.class, 3, 60));
    } else {
      urlCount += 2;
      addUIFormInput(new UIFormStringInput("rlu" + (++urlIdx),null,"Website Title"));
      addUIFormInput(new UIFormStringInput("rlu" + (++urlIdx), null,"URL (ex: http://www.site.com)")
      .addValidator(MandatoryValidator.class)
      .addValidator(ExpressionValidator
      .class, "^(http|https|ftp)\\:\\/\\/[a-z0-9\\-\\.]+\\.[a-z]{2,3}(:[a-z0-9]*)?\\/?([a-z0-9\\-\\._\\?\\,\\'\\/\\\\+&amp;%\\$#\\=~])*$", "UIContactSect.msg.Invalid-url"));
    }
  }

  /**
   * Create UISelectBox with name and values.
   * 
   * @param values Array of value for setting.
   * @param uiName Name of component.
   */
  private void createUISelectBox(String[] values, String uiName) {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>();
    int len = values.length;
    for (int idx = 0; idx < len; idx ++) {
      options.add(new SelectItemOption<String>(values[idx]));
    }
    
    if ("email".equals(uiName)) {
      emailCount += 2;
      addUIFormInput(new UIFormSelectBox(uiName + (++emailIdx), null, options));
    } else if ("ims".equals(uiName)) {
      imsCount += 2;
      addUIFormInput(new UIFormSelectBox(uiName + (++imsIdx), null, options));
    } 
  }
  
  /**
   * Sort a list in increase order of alphabet.<br>
   * 
   * @param lstComps List for sorting.
   * @return A sorted array in increase order.
   */
  private List<UIComponent> sort(List<UIComponent> lstComps) {    
    Collections.sort(lstComps, new UiComponentComparator());       
    return lstComps;
  }

  /**
   *   Implement UiComponentComparator class for sorting in increase order of alphabet.<br>
   *
   */
  private class UiComponentComparator implements Comparator<UIComponent> {
    /**
     * Compare 2 uicomponent by name
     */
    public int compare(UIComponent uicomp1, UIComponent uicomp2) {
      return uicomp1.getName().compareToIgnoreCase(uicomp2.getName());
    }
  }
  
  /**
   * Sort a list in increase order of length.<br>
   * 
   * @param lstComps List for sorting.
   * @return A sorted array in increase order.
   */
  private List<UIComponent> sortSubList(List<UIComponent> lstComps) {    
    Collections.sort(lstComps, new UIComparator());       
    return lstComps;
  }

  /**
   * Implement UiComponentComparator class for sorting in increase order of length.<br>
   * 
   */
  private class UIComparator implements Comparator<UIComponent> {
    /**
     * Compare 2 uicomponent by name
     */
    public int compare(UIComponent uicomp1, UIComponent uicomp2) {
      return (uicomp1.getName().length() - uicomp2.getName().length());
    }
  }
}