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
 * Component is used for contact information managing.<br>
 * Some contact information such as: email, phone, ims, website ...
 * This is one part of profile management beside basic information and experience.<br>
 *  
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
  /** EMAILS. */
  final public static String EMAILS = "emails";
  
  /** PHONES. */
  final public static String PHONES = "phones";
  
  /** IMS. */
  final public static String IMS = "ims";
  
  /** URLS. */
  final public static String URLS = "urls";
  
  /** URL. */
  final public static String URL = "url";
  
  /** EMAIL. */
  final public static String EMAIL = "email";
  
  /** PHONE. */
  final public static String PHONE = "phone";
  
  /** FONE. */
  final public static String FONE = "fone";
  
  /** RLU. */
  final public static String RLU = "rlu";
  
  /** WORK. */
  final public static String WORK = "Work";
  
  /** HOME. */
  final public static String HOME = "Home";
  
  /** OTHER. */
  final public static String OTHER = "Other";
  
  /** GTALK. */
  final public static String GTALK = "Gtalk";
  
  /** MSN. */
  final public static String MSN = "Msn";
  
  /** SKYPE. */
  final public static String SKYPE = "Skype";
  
  /** YAHOO. */
  final public static String YAHOO = "Yahoo";
  
  /** WEBSITE TITLE. */
  final public static String WEBSITE_TITLE = "Website Title";
  
  /** URL EXAMPLE. */
  final public static String URL_EXAMPLE = "URL (ex: http://www.site.com)";
  
  /** KEY. */
  final public static String KEY = "key";
  
  /** VALUE. */
  final public static String VALUE = "value";
  
  /** EMAIL REGEX EXPRESSION. */
  final public static String EMAIL_REGEX_EXPRESSION = "^([^@\\s]+)@((?:[-a-z0-9]+\\.)+[a-z]{2,})$";
  
  /** INVALID EMAIl. */
  final public static String INVALID_EMAIl = "UIContactSect.msg.Invalid-email";
  
  /** PHONE REGEX EXPRESSION. */
  final public static String PHONE_REGEX_EXPRESSION = "^[\\d\\s ().-]+$";
  
  /** INVALID PHONE. */
  final public static String INVALID_PHONE = "UIContactSect.msg.Invalid-phone";
  
  /** URL REGEX EXPRESSION. */
  final public static String URL_REGEX_EXPRESSION = "^(http|https|ftp)\\:\\/\\/[a-z0-9\\-\\.]+\\.[a-z]{2,3}(:[a-z0-9]*)?\\/?([a-z0-9\\-\\._\\?\\,\\'\\/\\\\+&amp;%\\$#\\=~])*$";
  
  /** INVALID URL. */
  final public static String INVALID_URL = "UIContactSect.msg.Invalid-url";
  
  /** GENDER Child. */
  final public static String GENDER_CHILD = "UITgender";
  
  /** GENDER. */
  final public static String GENDER = "gender";
  
  /** DEFAULT GENDER. */
  final public static String GENDER_DEFAULT = "Gender";
  
  /** MALE. */
  final public static String MALE = "male";
  
  /** FEMALE. */
  final public static String FEMALE = "female";
  
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
   * Initializes contact form.<br>
   * 
   * @throws Exception
   */
  public UIContactSection() throws Exception {
    addChild(UITitleBar.class, null, null);

    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>();
    options.add(new SelectItemOption<String>(GENDER_DEFAULT));
    options.add(new SelectItemOption<String>(MALE));
    options.add(new SelectItemOption<String>(FEMALE));
    addUIFormInput(new UIFormSelectBox(GENDER_CHILD, GENDER_CHILD, options));
  }
  
  /**
   * Gets and sort all uicomponents.<br>
   *  
   * @return All children in order.
   */
  public List<UIComponent> getChilds() {
    List<UIComponent> listChild = getChildren();
    return sort(listChild);
  }
  
  /**
   * Gets all children of email.<br>
   * 
   * @return All email children in order.
   */
  public List<UIComponent> getEmailChilds() {
    return sortSubList(getSubList(0, emailCount));
  }
  
  /**
   * Gets all children of Phone.<br>
   * 
   * @return All Phone children in order.
   */
  public List<UIComponent> getPhoneChilds() {
    return sortSubList(getSubList(emailCount, emailCount + phoneCount));
  }
  
  /**
   * Gets all children of ims.<br>
   * 
   * @return All ims children in order.
   */
  public List<UIComponent> getImsChilds() {
    return sortSubList(getSubList(emailCount + phoneCount, emailCount + phoneCount + imsCount));
  }
  
  /**
   * Gets all children of URL.<br>
   * 
   * @return All URL children in order.
   */
  public List<UIComponent> getUrlChilds() {
    return sortSubList(getSubList( emailCount + phoneCount + imsCount, emailCount + phoneCount + imsCount + urlCount));
  }

  /**
   * Gets gender child<br>
   * 
   * @return  gender child.
   */
  public UIComponent getGenderChild() {
    return getChildById(GENDER_CHILD);
  }
  
  /**
   *  Stores profile information into database when form is submitted.<br>
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
   * Adds component when user click add button.<br>
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
   * Removes the component that user selected for removing.<br>
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
   * Changes form into edit mode when user click eddit button.<br>
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
   * Gets profile information from components and save into Profile.<br>
   * 
   * @throws Exception
   */
  private void saveProfileInfo() throws Exception {
//    ArrayList<HashMap<String, String>> emails = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String, String>> phones = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String, String>> ims = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String, String>> urls = new ArrayList<HashMap<String, String>>();
    
//    List<UIComponent> listEmailUIComp = getEmailChilds();
    List<UIComponent> listPhoneUIComp = getPhoneChilds();
    List<UIComponent> listIMSUIComp = getImsChilds();
    List<UIComponent> listURLUIComp = getUrlChilds();

    UIFormSelectBox uiGender = getChildById(GENDER_CHILD);
    String gender = uiGender.getValue();
    gender = ("Gender".equals(gender) ? "" : gender);
    
//    emails = getProfileForSave(emailCount, listEmailUIComp, null);
    phones = getProfileForSave(phoneCount, listPhoneUIComp, WORK);
    ims = getProfileForSave(imsCount, listIMSUIComp, null);
    urls = getProfileForSave(urlCount, listURLUIComp, URL);
    
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    IdentityManager im = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
    
    Profile p = getProfile(true);     
    
//    p.setProperty(EMAILS, emails);
    p.setProperty(PHONES, phones);
    p.setProperty(IMS, ims);
    p.setProperty(URLS, urls);
    p.setProperty(GENDER, gender);
    
    im.updateContactSection(p);
  }

  /**
   * Gets information input by user for saving profile.<br>
   * 
   * @param count Number of children.
   * 
   * @param listUIComp Contains children of each input type (email, phone,...).
   * 
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
    if (WORK.equals(uiStringType)) {
      for (int i = 0; i < count; i++) {       
        HashMap<String, String> uiMap = new HashMap<String, String>();
        key = uiStringType;
        uiStringInput = (UIFormStringInput) listUIComp.get(i); 
        value = uiStringInput.getValue();

        uiMap.put(KEY,key);
        uiMap.put(VALUE, value);
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
          //uiStringInput = (UIFormStringInput) listUIComp.get(i);
          //key = uiStringInput.getValue();
          uiStringInput = (UIFormStringInput) listUIComp.get(i+1); 
          key = uiStringInput.getValue();
          value = uiStringInput.getValue();
        }          
        
        uiMap.put(KEY,key);
        uiMap.put(VALUE, value);
        profileMap.add(uiMap);
      }
    }
    return profileMap;
  }
  
  /**
   * Gets children of each type (email, phone, ...) from the all children list.<br>
   * 
   * @param startIdx Start index of sublist.
   * 
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
   * Sets value for components.<br>
   * 
   * @throws Exception
   */
  private void setValue() throws Exception {
//    setValueByType(getEmailChilds(), EMAILS);
    setValueByType(getPhoneChilds(), PHONES);
    setValueByType(getImsChilds(), IMS);
    setValueByType(getUrlChilds(), URLS);
  }
  
  /**
   * Sets value for components with each type.<br>
   * 
   * @param listChilds List children for setting.
   * 
   * @param uiType Type for setting.
   * 
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  private void setValueByType(List<UIComponent> listChilds, String uiType) throws Exception {
    Profile profile = getProfile(false);
    ArrayList<HashMap<String, String>> profiles = (ArrayList<HashMap<String, String>>) profile.getProperty(uiType);
    List<String> listProfile = new ArrayList<String>();    
    int listChildSize = listChilds.size();
    String gender = (String) profile.getProperty(GENDER);
    if (gender != "") ((UIFormInput)getGenderChild()).setValue(gender);
    
    if ((profiles == null) || (profiles.size() == 0)) {
      if (listChildSize != 0) {
        if (PHONES.equals(uiType)) {
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
        listProfile.add(map.get(KEY));
        listProfile.add(map.get(VALUE));
      }
      
      int listProfileSize = listProfile.size();
      int totalProfile = 0;
      if (PHONES.equalsIgnoreCase(uiType)) {
        totalProfile = listProfileSize;
        while( totalProfile > 2*listChildSize) {
          addUIFormInput(PHONE);
          totalProfile -= 2;
        }
        totalProfile = listProfileSize;
        while( totalProfile < 2*listChildSize) {
          String id1 = listChilds.get(listChildSize-1).getName();
          removeFormInput(id1, null);
          totalProfile += 2;
        }
        
        for (int i = 0; i < listProfileSize/2; i++) {
          ((UIFormInput)getPhoneChilds().get(i)).setValue(listProfile.get(2*i + 1));
        }
      } else {
        totalProfile = listProfileSize;
        while (totalProfile > listChildSize) {
          if (EMAILS.equals(uiType)) {
            addUIFormInput(EMAIL);
          } else if (IMS.equals(uiType)) {
            addUIFormInput(IMS);
          } else if (URLS.equals(uiType)) {
            addUIFormInput(RLU);
          }
          totalProfile -= 2;
        }
        totalProfile = listProfileSize;
        while (totalProfile < listChildSize) {
          String id1 = listChilds.get(listChildSize-1).getName();
          String id2 = listChilds.get(listChildSize-2).getName();
          removeFormInput(id1, id2);
          
          totalProfile += 2;
        }
        
        if (EMAILS.equals(uiType)) {
          for (int i = 0; i <= listProfileSize - 1; i++) {
            ((UIFormInput)getEmailChilds().get(i)).setValue(listProfile.get(i));
          }
        } else if (IMS.equals(uiType)) {
          for (int i = 0; i <= listProfileSize - 1; i++) {
            ((UIFormInput)getImsChilds().get(i)).setValue(listProfile.get(i));
          }
        } else if (URLS.equals(uiType)) {
          for (int i = 0; i <= listProfileSize - 1; i++) {
            ((UIFormInput)getUrlChilds().get(i)).setValue(listProfile.get(i));
          }
        }
      }
    }
  }

  /**
   * Removes components by input id.<br>
   * 
   * @param id1 The id of first component.
   * 
   * @param id2 The id of next component.
   */
  private void removeFormInput(String id1, String id2) {
    if (id1.startsWith(FONE)) {
      removeChildById(id1);
      --phoneCount;
    } else {      
      removeChildById(id1);
      removeChildById(id2);
      if (id1.startsWith(EMAIL)) {
        emailCount -= 2;
      } else if (id1.startsWith(IMS)) {
        imsCount -= 2;
      } else if (id1.startsWith(RLU)) {
        urlCount -= 2;
      }
    }
  }

  /**
   * Adds component with the input type.<br>
   * 
   * @param type Type of component is added (email, phone, ...).
   * 
   * @throws Exception
   */
  private void addUIFormInput(String type) throws Exception {
    if (EMAIL.equals(type)) {
      createUISelectBox(new String[]{WORK, HOME, OTHER}, EMAIL);
      addUIFormInput(new UIFormStringInput(EMAIL + (++emailIdx), null, null)
      .addValidator(MandatoryValidator.class)
      .addValidator(StringLengthValidator.class, 3, 30).addValidator(ExpressionValidator
      .class, EMAIL_REGEX_EXPRESSION, INVALID_EMAIl));
    } else if (PHONE.equals(type)) {
      phoneCount += 1;
      addUIFormInput(new UIFormStringInput(FONE + (++phoneIdx),null,null)
      .addValidator(MandatoryValidator.class)
      .addValidator(StringLengthValidator.class, 3, 20)
      .addValidator(ExpressionValidator.class, PHONE_REGEX_EXPRESSION, INVALID_PHONE));
    } else if (IMS.equals(type)) {
      createUISelectBox(new String[]{GTALK, MSN, SKYPE, YAHOO, OTHER}, IMS);
      addUIFormInput(new UIFormStringInput(IMS + (++imsIdx),null,null)
      .addValidator(MandatoryValidator.class)
      .addValidator(StringLengthValidator.class, 3, 60));
    } else {
      urlCount += 2;
      addUIFormInput(new UIFormStringInput(RLU + (++urlIdx),null, WEBSITE_TITLE));
      addUIFormInput(new UIFormStringInput(RLU + (++urlIdx), null, URL_EXAMPLE)
      .addValidator(MandatoryValidator.class)
      .addValidator(ExpressionValidator
      .class, URL_REGEX_EXPRESSION, INVALID_URL));
    }
  }

  /**
   * Creates UISelectBox with name and values.
   * 
   * @param values Array of value for setting.
   * 
   * @param uiName Name of component.
   */
  private void createUISelectBox(String[] values, String uiName) {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>();
    int len = values.length;
    for (int idx = 0; idx < len; idx ++) {
      options.add(new SelectItemOption<String>(values[idx]));
    }
    
    if (EMAIL.equals(uiName)) {
      emailCount += 2;
      addUIFormInput(new UIFormSelectBox(uiName + (++emailIdx), null, options));
    } else if (IMS.equals(uiName)) {
      imsCount += 2;
      addUIFormInput(new UIFormSelectBox(uiName + (++imsIdx), null, options));
    } 
  }
  
  /**
   * Sorts a list in increase order of alphabet.<br>
   * 
   * @param lstComps List for sorting.
   * 
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