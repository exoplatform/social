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
package org.exoplatform.social.webui.profile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.webui.Utils;
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
 * Some contact information such as: phone, ims, website ...
 * This is one part of profile management beside basic information and experience.<br>
 *
 * Modified : hanh.vi
 *          hanhvq@gmail.com
 * Aug 18, 2009
 */
@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template =  "classpath:groovy/social/webui/profile/UIContactSection.gtmpl",
  events = {
    @EventConfig(listeners = UIContactSection.EditActionListener.class, phase=Phase.DECODE),
    @EventConfig(listeners = UIContactSection.SaveActionListener.class),
    @EventConfig(listeners = UIProfileSection.CancelActionListener.class, phase=Phase.DECODE),
    @EventConfig(listeners = UIContactSection.AddActionListener.class),
    @EventConfig(listeners = UIContactSection.RemoveActionListener.class, phase=Phase.DECODE)
  }
)
public class UIContactSection extends UIProfileSection {
  /** GENDER */
  public static final String GENDER_CHILD = "gender";

  /** PHONE. */
  public static final String PHONE = "1phone";

  /** URL. */
  public static final String IM = "2im";

  /** URL. */
  public static final String URL = "3url";

  /** MALE. */
  public static final String VALUE_GENDER_MALE      = "male";

  /** FEMALE. */
  public static final String VALUE_GENDER_FEMALE    = "female";

  /** PHONE_TYPES. */
  public static final String[] PHONE_TYPES = new String[] {"Work","Home","Other"};

  /** IM_TYPES. */
  public static final String[] IM_TYPES = new String[] {"Gtalk","Msn","Skype","Yahoo","Other"};

  /** WEBSITE TITLE. */
  public static final String WEBSITE_TITLE = "Website Title";

  /** URL EXAMPLE. */
  public static final String URL_EXAMPLE = "http://";

  /** KEY. */
  public static final String KEY = "key";

  /** VALUE. */
  public static final String VALUE = "value";

//  /** EMAIL REGEX EXPRESSION. */
//  final public static String EMAIL_REGEX_EXPRESSION = "^([^@\\s]+)@((?:[-a-z0-9]+\\.)+[a-z]{2,})$";

//  /** INVALID EMAIl. */
//  final public static String INVALID_EMAIl = "UIContactSect.msg.Invalid-email";

  /** PHONE REGEX EXPRESSION. */
  public static final String PHONE_REGEX_EXPRESSION = "^[\\d\\s ().+-]+$";

  /** INVALID PHONE. */
  public static final String INVALID_PHONE = "UIContactSect.msg.Invalid-phone";

  /** URL REGEX EXPRESSION. */
  public static final String URL_REGEX_EXPRESSION = "^(http|https|ftp)\\:\\/\\/[a-z0-9\\-\\.]+\\.[a-z]{2,3}(:[a-z0-9]*)?\\/?([a-z0-9\\-\\._\\?\\,\\'\\/\\\\+&amp;%\\$#\\=~])*$";

  /** INVALID URL. */
  public static final String INVALID_URL = "UIContactSect.msg.Invalid-url";

  /** Number of phone. */
  private int phoneCount = 0;

  /** Number of ims. */
  private int imCount = 0;

  /** Number of url. */
  private int urlCount = 0;

  /** Get the number of phone. */
  /**
   * @return phoneCount
   */
  public final int getPhoneCount() {
    return phoneCount;
  }

  /** Get the number of ims. */
  /**
   * @return imCount
   */
  public final int getImsCount() {
    return imCount;
  }

  /** Get the number of url. */
  /**
   * @return urlCount
   */
  public final int getUrlCount() {
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
    options.add(new SelectItemOption<String>(VALUE_GENDER_MALE));
    options.add(new SelectItemOption<String>(VALUE_GENDER_FEMALE));
    addUIFormInput(new UIFormSelectBox(GENDER_CHILD, GENDER_CHILD, options));
  }

  /**
   * Gets and sort all uicomponents.<br>
   *
   * @return All children in order.
   */
  public final List<UIComponent> getChilds() {
    List<UIComponent> listChild = getChildren();
    return sort(listChild);
  }

  /**
   * Gets all children of Phone.<br>
   *
   * @return All Phone children in order.
   */
  public final List<UIComponent> getPhoneChilds() {
    return sortSubList(getSubList(0, phoneCount));
  }

  /**
   * Gets all children of ims.<br>
   *
   * @return All ims children in order.
   */
  public final List<UIComponent> getImsChilds() {
    return sortSubList(getSubList(phoneCount, phoneCount + imCount));
  }

  /**
   * Gets all children of URL.<br>
   *
   * @return All URL children in order.
   */
  public final List<UIComponent> getUrlChilds() {
    return sortSubList(getSubList( phoneCount + imCount, phoneCount + imCount + urlCount));
  }

  /**
   * Gets gender child<br>
   *
   * @return  gender child.
   */
  @SuppressWarnings("unchecked")
  public final UIFormInput<String> getGenderChild() {
    return (UIFormInput<String>)getChildById(GENDER_CHILD);
  }

  /**
   *  Stores profile information into database when form is submitted.<br>
   *
   */
  public static class SaveActionListener extends UIProfileSection.SaveActionListener {

    @Override
    public final void execute(final Event<UIProfileSection> event) throws Exception {
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

    @Override
    public final void execute(final Event<UIContactSection> event) throws Exception {
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

    @Override
    public final void execute(final Event<UIContactSection> event) throws Exception {
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

    @Override
    public final void execute(final Event<UIProfileSection> event) throws Exception {
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
    Profile p = getProfile();

    Profile toBeUpdatedProfile = new Profile(p.getIdentity());
    toBeUpdatedProfile.setId(p.getId());

    toBeUpdatedProfile.setProperty(Profile.GENDER, getGenderChild().getValue());
    toBeUpdatedProfile.setProperty(Profile.CONTACT_PHONES, getProfileForSave(phoneCount, getPhoneChilds(), PHONE));
    toBeUpdatedProfile.setProperty(Profile.CONTACT_IMS, getProfileForSave(imCount, getImsChilds(), IM));
    toBeUpdatedProfile.setProperty(Profile.CONTACT_URLS, getProfileForSave(urlCount, getUrlChilds(), URL));

    Utils.getIdentityManager().updateContactSection(toBeUpdatedProfile);
  }

  /**
   * Gets information input by user for saving profile.<br>
   *
   * @param count Number of children.
   *
   * @param listUIComp Contains children of each input type (phone,...).
   *
   * @param uiStringType Type of component.
   *
   * @return All profile information.
   */
  private ArrayList<HashMap<String, String>> getProfileForSave(final int count, final List<UIComponent> listUIComp, final String uiStringType) {
    ArrayList<HashMap<String, String>> profileMap = new ArrayList<HashMap<String, String>>();
    for (int i = 0; i < count; i+=2) {
      HashMap<String, String> uiMap = new HashMap<String, String>();
      String value = null;
      String key = null;
      if (uiStringType.equals(URL)) {
        UIFormStringInput uiStringInput = (UIFormStringInput) listUIComp.get(i+1);
        key = uiStringInput.getValue();
        value = uiStringInput.getValue();
      } else {
        key = ((UIFormSelectBox) listUIComp.get(i)).getValue();
        value = ((UIFormStringInput) listUIComp.get(i+1)).getValue();
      }

      uiMap.put(KEY,key);
      uiMap.put(VALUE, value);
      profileMap.add(uiMap);
    }
    return profileMap;
  }

  /**
   * Gets children of each type (phone, ...) from the all children list.<br>
   *
   * @param startIdx Start index of sublist.
   *
   * @param endIdx End index of sublist.
   *
   * @return A list contains children of each type.
   */
  private List<UIComponent> getSubList(final int startIdx, final int endIdx) {
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
    Profile profile = getProfile();
    String gender = (String) profile.getProperty(Profile.GENDER);
    if (gender != "") {
      getGenderChild().setValue(gender);
    }

    setValueByType(profile, getPhoneChilds(), Profile.CONTACT_PHONES);
    setValueByType(profile, getImsChilds(), Profile.CONTACT_IMS);
    setValueByType(profile, getUrlChilds(), Profile.CONTACT_URLS);
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
  private void setValueByType(final Profile profile, final List<UIComponent> listChilds, final String uiType) throws Exception {
    if (profile == null || listChilds == null || uiType == null || uiType.equals("")) {
      return;
    }
    List<Map<String, String>> profiles = (List<Map<String, String>>) profile.getProperty(uiType);
    int numberOfUI = listChilds.size();
    int newProfileSize = profiles == null ? 0 : profiles.size();

    while (numberOfUI/2 > newProfileSize) {
      String id1 = listChilds.get(--numberOfUI).getName();
      String id2 = listChilds.get(--numberOfUI).getName();
      removeFormInput(id1, id2);
    }

    if(newProfileSize == 0) {
      return;
    }

    while (newProfileSize > numberOfUI/2) {
      if (Profile.CONTACT_IMS.equals(uiType)) {
        addUIFormInput(IM);
      } else if (Profile.CONTACT_PHONES.equals(uiType)) {
        addUIFormInput(PHONE);
      } else if (Profile.CONTACT_URLS.equals(uiType)) {
        addUIFormInput(URL);
      }
      numberOfUI += 2;
    }

    int index = 0;
    if (Profile.CONTACT_IMS.equals(uiType)) {
      for (Map<String, String> map : profiles) {
        ((UIFormInput) getImsChilds().get(index++)).setValue(map.get(KEY));
        ((UIFormInput) getImsChilds().get(index++)).setValue(map.get(VALUE));
      }
    } else if (Profile.CONTACT_URLS.equals(uiType)) {
      for (Map<String, String> map : profiles) {
        ((UIFormInput) getUrlChilds().get(index++)).setValue(map.get(KEY));
        ((UIFormInput) getUrlChilds().get(index++)).setValue(map.get(VALUE));
      }
    } else if (Profile.CONTACT_PHONES.equals(uiType)) {
      for (Map<String, String> map : profiles) {
        ((UIFormInput) getPhoneChilds().get(index++)).setValue(map.get(KEY));
        ((UIFormInput) getPhoneChilds().get(index++)).setValue(map.get(VALUE));
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
  private void removeFormInput(final String id1, final String id2) {
    removeChildById(id1);
    removeChildById(id2);
    if (id1.startsWith(IM)) {
      imCount -= 2;
    } else if (id1.startsWith(URL)) {
      urlCount -= 2;
    } else if (id1.startsWith(PHONE)) {
      phoneCount -= 2;
    }
  }

  /**
   * Adds component with the input type.<br>
   *
   * @param type Type of component is added (email, phone, ...).
   *
   * @throws Exception
   */
  private void addUIFormInput(final String type) throws Exception {
    if (PHONE.equals(type)) {
      int phoneIdx = phoneCount;
      createUISelectBox(PHONE_TYPES, PHONE + StringUtils.leftPad(String.valueOf(phoneIdx++), 3, '0'));
      addUIFormInput(new UIFormStringInput(PHONE + StringUtils.leftPad(String.valueOf(phoneIdx++), 3, '0'),null,null)
      .addValidator(MandatoryValidator.class)
      .addValidator(StringLengthValidator.class, 3, 20)
      .addValidator(ExpressionValidator.class, PHONE_REGEX_EXPRESSION, INVALID_PHONE));
      phoneCount += 2;
    } else if (IM.equals(type)) {
      int imIdx = imCount;
      createUISelectBox(IM_TYPES, IM + StringUtils.leftPad(String.valueOf(imIdx++), 3, '0'));
      addUIFormInput(new UIFormStringInput(IM + StringUtils.leftPad(String.valueOf(imIdx++), 3, '0'), null, null)
      .addValidator(MandatoryValidator.class)
      .addValidator(StringLengthValidator.class, 3, 60));
      imCount += 2;
    } else if (URL.equals(type)) {
      int urlIdx = urlCount;
      addUIFormInput(new UIFormStringInput(URL + StringUtils.leftPad(String.valueOf(urlIdx++), 3, '0'), null, WEBSITE_TITLE));
      addUIFormInput(new UIFormStringInput(URL + StringUtils.leftPad(String.valueOf(urlIdx++), 3, '0'), null, URL_EXAMPLE)
      .addValidator(MandatoryValidator.class)
      .addValidator(ExpressionValidator.class, URL_REGEX_EXPRESSION, INVALID_URL));
      urlCount += 2;
    }
  }

  /**
   * Creates UISelectBox with name and values.
   *
   * @param values Array of value for setting.
   *
   * @param uiName Name of component.
   */
  private void createUISelectBox(final String[] values, final String uiName) {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>();
    for (String value : values) {
      options.add(new SelectItemOption<String>(value));
    }
    addUIFormInput(new UIFormSelectBox(uiName, null, options));
  }

  /**
   * Sorts a list in increase order of alphabet.<br>
   *
   * @param lstComps List for sorting.
   *
   * @return A sorted array in increase order.
   */
  private List<UIComponent> sort(final List<UIComponent> lstComps) {
    Collections.sort(lstComps, new UiComponentComparator());
    return lstComps;
  }

  /**
   *   Implement UiComponentComparator class for sorting in increase order of alphabet.<br>
   *
   */
  private static class UiComponentComparator implements Comparator<UIComponent> {
    /**
     * Compare 2 uicomponent by id
     */
    public int compare(final UIComponent uicomp1, final UIComponent uicomp2) {
      return uicomp1.getId().compareToIgnoreCase(uicomp2.getId());
    }
  }

  /**
   * Sort a list in increase order of length.<br>
   *
   * @param lstComps List for sorting.
   * @return A sorted array in increase order.
   */
  private List<UIComponent> sortSubList(final List<UIComponent> lstComps) {
    Collections.sort(lstComps, new UiComponentComparator());
    return lstComps;
  }
}