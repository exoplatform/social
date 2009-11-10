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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormInput;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormDateTimeInput;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.validator.DateTimeValidator;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.StringLengthValidator;

@ComponentConfigs ({ 
  @ComponentConfig (
      lifecycle = UIFormLifecycle.class,
      template =  "app:/groovy/portal/webui/component/UIExperienceSection.gtmpl",
      events = {
        @EventConfig(listeners = UIExperienceSection.EditActionListener.class, phase=Phase.DECODE),
        @EventConfig(listeners = UIExperienceSection.SaveActionListener.class),
        @EventConfig(listeners = UIExperienceSection.AddActionListener.class),
        @EventConfig(listeners = UIExperienceSection.RemoveActionListener.class,  phase=Phase.DECODE),
        @EventConfig(listeners = UIProfileSection.CancelActionListener.class, phase=Phase.DECODE)
      }
  ),
  @ComponentConfig(
     type = UIFormCheckBoxInput.class,
     id = "UIFormCheckBoxEndDate",
     events = @EventConfig(phase = Phase.DECODE, listeners = UIExperienceSection.ShowHideEndDateActionListener.class)
 )
})
public class UIExperienceSection extends UIProfileSection {
  /** COMPANY. */
  final public static String COMPANY = "company";
  /** POSITION. */
  final public static String POSITION = "position";
  /** POSITION. */
  final public static String PROFESSIONAL = "professional";
  /** START DATE OF EXPERIENCE. */
  final public static String START_DATE = "startDate";
  /** END DATE OF EXPERIENCE. */
  final public static String END_DATE = "endDate";
  /** CURRENT OR PAST EXPERIENCE. */
  final public static String IS_CURRENT = "isCurrent";
  /** DESCRIPTION OF EXPERIENCE. */
  final public static String DESCRIPTION = "description";
  /** EXPERIENCE. */
  final public static String EXPERIENCE = "experiences";
  /** DATE AFTER TODAY. */
  final public static String DATE_AFTER_TODAY = "UIExperienceSection.msg.DateAfterToday";
  /** START DATE BEFORE END DATE. */
  final public static String STARTDATE_BEFORE_ENDDATE = "UIExperienceSection.msg.startDateBeforeEndDate";
  /** DATE FORMAT. */
  final public static String DATE_FORMAT_MMDDYYYY = "MM/dd/yyyy";
  
  /** Number of components. */
  int expIdx = 0;
  
  /**
   * Get number of component.
   * 
   * @return Number of components.
   */
  public int getExpCount() {
    return expIdx;
  }
  
  /**
   * Get all the children.
   */
  public List<UIComponent> getChilds() {
    return getChildren();
  }
  
  /**
   * Constructor.
   * 
   * @throws Exception
   */
  public UIExperienceSection() throws Exception {
    addChild(UITitleBar.class, null, null);
  }

  /**
   * Get month of date time.
   * 
   * @param inDate Input date
   * @return Month of input date.
   * @throws ParseException
   */
  public int getMonth(String inDate) throws ParseException {
    if ((inDate == null) || (inDate.length() == 0)) return 0; 
    Calendar calendar = getCalendar(inDate);
    
    return (calendar.get(Calendar.MONTH) + 1); // Month start from 0
  }
  
  /**
   * Get date of date time.
   * 
   * @param inDate Input date
   * @return Date of input date.
   * @throws ParseException
   */
  public int getDate(String inDate) throws ParseException {
    if ((inDate == null) || (inDate.length() == 0)) return 0;
    Calendar calendar = getCalendar(inDate);
    
    return calendar.get(Calendar.DATE);
  }
  
  /**
   * Get year of date time.
   * 
   * @param inDate Input date
   * @return Year of input date.
   * @throws ParseException
   */
  public int getYear(String inDate) throws ParseException {
    if ((inDate == null) || (inDate.length() == 0)) return 0;
    Calendar calendar = getCalendar(inDate);
    
    return calendar.get(Calendar.YEAR);
  }

  /**
   *  Add component when Add button is clicked. 
   *
   */
  public static class AddActionListener extends EventListener<UIExperienceSection> {
    public void execute(Event<UIExperienceSection> event) throws Exception {
      UIExperienceSection uiForm = event.getSource();
      uiForm.addUIFormInput();
    }
  }
  
  /**
   *  Add component when Add button is clicked. 
   *
   */
  public static class RemoveActionListener extends EventListener<UIExperienceSection> {
    public void execute(Event<UIExperienceSection> event) throws Exception {
      UIExperienceSection uiForm = event.getSource();
      String block = event.getRequestContext().getRequestParameter(OBJECTID);
      int blockIdx = Integer.parseInt(block);
      String companyId = null;
      String positionId = null;
      String professionalId = null;
      String startDateId = null;
      String isCurrentId = null;
      String endDateId = null;
      String descriptionId = null;
      List<UIComponent> listChild = uiForm.getChilds();

      companyId = listChild.get(blockIdx).getId();
      positionId = listChild.get(blockIdx+1).getId();
      professionalId = listChild.get(blockIdx+2).getId();
      startDateId = listChild.get(blockIdx+3).getId();
      isCurrentId = listChild.get(blockIdx+4).getId();
      endDateId = listChild.get(blockIdx+5).getId();
      descriptionId = listChild.get(blockIdx+6).getId();

      uiForm.removeChildById(companyId);
      uiForm.removeChildById(positionId);
      uiForm.removeChildById(professionalId);
      uiForm.removeChildById(startDateId);
      uiForm.removeChildById(isCurrentId);
      uiForm.removeChildById(endDateId);
      uiForm.removeChildById(descriptionId);
    }
  }
  
  /**
   *  Save experience informations to profile. 
   *
   */
  public static class SaveActionListener extends UIProfileSection.SaveActionListener {

    public void execute(Event<UIProfileSection> event) throws Exception {
      UIProfileSection sect = event.getSource();    
      UIExperienceSection uiExperienceSectionSect = (UIExperienceSection)sect;
      if (uiExperienceSectionSect.saveProfileInfo() == 0) {
        super.execute(event);
      }
    }       
  }
  
  /**
   * Change to edit mode when Edit button is clicked.
   *
   */
  public static class EditActionListener extends UIProfileSection.EditActionListener {
    
    @SuppressWarnings("unchecked")
    public void execute(Event<UIProfileSection> event) throws Exception {
      UIProfileSection sect = event.getSource();
      UIExperienceSection uiExpSection = (UIExperienceSection)sect;
      ArrayList<HashMap<String, Object>> experiences = new ArrayList<HashMap<String, Object>>();
      Profile p = sect.getProfile(); 
      List<UIComponent> listChild = uiExpSection.getChilds();
      List<Object> listProfile = new ArrayList<Object>();
      int childSize = listChild.size() - 1; // List of children include UITitleBar child.
      String companyId = null;
      String positionId = null;
      String professionalId = null;
      String startDateId = null;
      String isCurrentId = null;
      String endDateId = null;
      String descriptionId = null;
      
      experiences = (ArrayList<HashMap<String, Object>>) p.getProperty(EXPERIENCE);
      if (experiences == null) {
        for (int idx = 1; idx <= childSize; idx += 7) {
          sect.removeChild(UIFormStringInput.class);
          sect.removeChild(UIFormStringInput.class);
          sect.removeChild(UIFormTextAreaInput.class);
          sect.removeChild(UIFormDateTimeInput.class);
          sect.removeChild(UIFormCheckBoxInput.class);
          sect.removeChild(UIFormDateTimeInput.class);
          sect.removeChild(UIFormStringInput.class);
        }
      } else {
        for (HashMap<String, Object> map : experiences) {
          listProfile.add(map.get(COMPANY));
          listProfile.add(map.get(POSITION));
          listProfile.add(map.get(PROFESSIONAL));
          listProfile.add(map.get(START_DATE));
          listProfile.add(map.get(IS_CURRENT));
          listProfile.add(map.get(END_DATE));
          listProfile.add(map.get(DESCRIPTION));
        }
        
        int totalProfiles = listProfile.size();
        int numberOfChildren = 0;
        if (totalProfiles > childSize) {
          numberOfChildren = childSize;
          while (totalProfiles > numberOfChildren) {
            uiExpSection.addUIFormInput();
            numberOfChildren += 7;
          }
        } else if (totalProfiles < childSize) {
          numberOfChildren = childSize;
          while (totalProfiles < numberOfChildren) {
            companyId = listChild.get(childSize-6).getName();
            positionId = listChild.get(childSize-5).getName();
            professionalId = listChild.get(childSize-4).getName();
            startDateId = listChild.get(childSize-3).getName();
            isCurrentId = listChild.get(childSize-2).getName();
            endDateId = listChild.get(childSize-1).getName();
            descriptionId = listChild.get(childSize).getName();

            sect.removeChildById(companyId);
            sect.removeChildById(positionId);
            sect.removeChildById(professionalId);
            sect.removeChildById(startDateId);
            sect.removeChildById(isCurrentId);
            sect.removeChildById(endDateId);
            sect.removeChildById(descriptionId);
            
            numberOfChildren -= 7;
          }
        }
        
        List<UIComponent> listChildForSetValue = uiExpSection.getChilds();
        for (int idx = 0; idx < totalProfiles; idx += 7) {
          ((UIFormInput)listChildForSetValue.get(idx + 1)).setValue(listProfile.get(idx));
          ((UIFormInput)listChildForSetValue.get(idx + 2)).setValue(listProfile.get(idx+1));
          ((UIFormInput)listChildForSetValue.get(idx + 3)).setValue(listProfile.get(idx+2));
          ((UIFormInput)listChildForSetValue.get(idx + 4)).setValue(listProfile.get(idx+3));
          ((UIFormCheckBoxInput<Boolean>)listChildForSetValue.get(idx + 5)).setValue((Boolean)listProfile.get(idx+4));
          ((UIFormDateTimeInput)listChildForSetValue.get(idx + 6)).setRendered(!((UIFormCheckBoxInput<Boolean>)listChildForSetValue.get(idx + 5)).getValue());
          ((UIFormInput)listChildForSetValue.get(idx + 6)).setValue(listProfile.get(idx+5));
          ((UIFormInput)listChildForSetValue.get(idx + 7)).setValue(listProfile.get(idx+6));
        }
      }
      
      super.execute(event);
    }
  }

  /**
   * Show and hide end date component depending on isCurrent variable.
   *
   */
  static public class ShowHideEndDateActionListener extends EventListener<UIFormCheckBoxInput<Boolean>> {
    
    public void execute(Event<UIFormCheckBoxInput<Boolean>> event) throws Exception {
      UIFormCheckBoxInput<Boolean> sect = event.getSource();
      UIExperienceSection uiForm = sect.getAncestorOfType(UIExperienceSection.class);
      UIFormDateTimeInput uiDateTime = uiForm.getChildById(END_DATE + sect.getId());
      boolean isCheck = sect.isChecked();
      uiDateTime.setRendered(!isCheck);
      
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
    }
  }

  /**
   * Get all the past experience.
   * 
   * @return all the experience that has isCurrent is false.
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public List<HashMap<String, Object>> getPastExperience() throws Exception {
    ArrayList<HashMap<String, Object>> experiences = new ArrayList<HashMap<String, Object>>();
    ArrayList<HashMap<String, Object>> pastExperiences = new ArrayList<HashMap<String, Object>>();
    Profile p = getProfile();
    experiences = (ArrayList<HashMap<String, Object>>) p.getProperty(EXPERIENCE);
    if (experiences != null) {
      for (HashMap<String, Object> map : experiences) {
        if (!(Boolean)map.get(IS_CURRENT)) {
          pastExperiences.add(map);
        }
      }
    }
    
    return pastExperiences;
  }
  
  /**
   * Get all the current experience.
   * 
   * @return all the experience that has isCurrent is true.
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public List<HashMap<String, Object>> getCurrentExperience() throws Exception {
    ArrayList<HashMap<String, Object>> experiences = new ArrayList<HashMap<String, Object>>();
    ArrayList<HashMap<String, Object>> currentExperiences = new ArrayList<HashMap<String, Object>>();
    Profile p = getProfile();
    experiences = (ArrayList<HashMap<String, Object>>) p.getProperty(EXPERIENCE);
    if (experiences != null) {
      for (HashMap<String, Object> map : experiences) {
        if ((Boolean)map.get(IS_CURRENT)) {
          currentExperiences.add(map);
        }
      }
    }
    
    return currentExperiences;
  }
  
  /**
   * Save input information into profile.
   * 
   * @return Integer value (0: save complete; 1,2: an error has occurs)
   * 
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  private int saveProfileInfo() throws Exception {
    ArrayList<HashMap<String, Object>> experiences = new ArrayList<HashMap<String, Object>>();
    UIFormStringInput uiStringInput = null;
    UIFormTextAreaInput uiFormTextAreaInput = null;
    UIFormCheckBoxInput<Boolean> uiCheckBox = null;
    UIFormDateTimeInput uiDateTimeInput = null;
    String company = null;
    String position = null;
    String professional = null;
    String startDate = null;
    String endDate = null;
    Boolean isCurrent = null;
    String description = null;
    Date sDate = null;
    Date eDate = null;
    Date today = new Date();
    
    UIPortalApplication uiPortalApplication = Util.getUIPortalApplication();
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    IdentityManager im = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
    Profile p = getProfile(); 
    List<UIComponent> listUIComp = getChilds();
    int totalUIComponent = listUIComp.size() - 1; // List of children not include UITitleBar child.
    
    if (totalUIComponent == 0) {
      if (p != null) {
        p.setProperty(EXPERIENCE, experiences);
        im.saveProfile(p);
      }
      
      return 0;
    }
    
    for (int i = 1; i <= totalUIComponent; i+=7) {   
      HashMap<String, Object> uiMap = new HashMap<String, Object>();
      
      uiStringInput = (UIFormStringInput)listUIComp.get(i);
      company = uiStringInput.getValue();
      
      uiStringInput = (UIFormStringInput)listUIComp.get(i + 1);
      position = uiStringInput.getValue();
      
      uiFormTextAreaInput = (UIFormTextAreaInput)listUIComp.get(i + 2);
      professional = uiFormTextAreaInput.getValue();
      
      uiDateTimeInput = (UIFormDateTimeInput)listUIComp.get(i + 3);
      startDate = uiDateTimeInput.getValue();
      
      uiCheckBox = (UIFormCheckBoxInput<Boolean>)listUIComp.get(i + 4);
      isCurrent = uiCheckBox.getValue();
      
      uiDateTimeInput = (UIFormDateTimeInput)listUIComp.get(i + 5);
      endDate = uiDateTimeInput.getValue();
      
      sDate = stringToDate(startDate);
      eDate = stringToDate(endDate);
      if (sDate.after(today)) {
        uiPortalApplication.addMessage(new ApplicationMessage(DATE_AFTER_TODAY, null));
        return 2;
      }
      if (!isCurrent) {
        if (eDate.after(today)) {
          uiPortalApplication.addMessage(new ApplicationMessage(DATE_AFTER_TODAY, null));
          return 2;
        }
        if (sDate.after(eDate)) {
          uiPortalApplication.addMessage(new ApplicationMessage(STARTDATE_BEFORE_ENDDATE, null));
          return 1;
        }
      } else {
        endDate = null;
      }
      
      uiStringInput = (UIFormStringInput)listUIComp.get(i + 6);
      description = uiStringInput.getValue();
      
      uiMap.put(COMPANY, company);
      uiMap.put(POSITION,position); 
      uiMap.put(PROFESSIONAL,professional); 
      uiMap.put(START_DATE, startDate);
      uiMap.put(END_DATE, endDate);
      uiMap.put(IS_CURRENT, isCurrent);
      uiMap.put(DESCRIPTION, description);
      
      experiences.add(uiMap);
    }
      
    p.setProperty(EXPERIENCE, experiences);
    
    im.saveProfile(p);
    
    return 0;
  }
  
  /**
   * Add component when add buttons clicked.
   * 
   * @throws Exception
   */
  private void addUIFormInput() throws Exception {
    expIdx += 1;
    addUIFormInput(new UIFormStringInput(COMPANY + expIdx, null, null).addValidator(MandatoryValidator.class)
                   .addValidator(StringLengthValidator.class, 3, 90));
    addUIFormInput(new UIFormStringInput(POSITION + expIdx, null, null).addValidator(MandatoryValidator.class)
                   .addValidator(StringLengthValidator.class, 3, 90));
    addUIFormInput(new UIFormTextAreaInput(PROFESSIONAL + expIdx, null, null));
    
    addUIFormInput(new UIFormDateTimeInput(START_DATE + expIdx, null, null, false).
                   addValidator(DateTimeValidator.class).addValidator(MandatoryValidator.class)) ;
    
    UIFormCheckBoxInput<Boolean> uiDateInputCheck = new UIFormCheckBoxInput<Boolean>(Integer.toString(expIdx), null, false) ;
    uiDateInputCheck.setComponentConfig(UIFormCheckBoxInput.class, "UIFormCheckBoxEndDate");
    uiDateInputCheck.setOnChange("ShowHideEndDate", uiDateInputCheck.getId()) ;
    addUIFormInput(uiDateInputCheck);
    
    addUIFormInput(new UIFormDateTimeInput(END_DATE + expIdx, null, null, false).addValidator(MandatoryValidator.class)
      .addValidator(DateTimeValidator.class)) ;
    
    addUIFormInput(new UIFormStringInput(DESCRIPTION + expIdx, null, null));
  }
  
  /**
   * Format String type to Date type.
   * 
   * @param dateStr Input String
   * @return Converted date.
   * @throws ParseException
   */
  private Date stringToDate(String dateStr) throws ParseException {
     SimpleDateFormat formatDate = new SimpleDateFormat(DATE_FORMAT_MMDDYYYY);
     if ((dateStr == null) || (dateStr.length() == 0)) return null;
     return formatDate.parse(dateStr);
  }
  
  /**
   * Get calendar with string date input.
   * 
   * @param inDate Date input.
   * @return
   * @throws ParseException
   */
  private Calendar getCalendar(String inDate) throws ParseException {
    SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT_MMDDYYYY);
    Date date = format.parse(inDate);
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    return calendar;
  }
}