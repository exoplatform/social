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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringEscapeUtils;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormDateTimeInput;
import org.exoplatform.webui.form.UIFormInput;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.input.UICheckBoxInput;
import org.exoplatform.webui.form.validator.ExpressionValidator;
import org.exoplatform.webui.form.validator.UserConfigurableValidator;


/**
 * Component manages all experience informations
 * (ex: company, position, professional...) of profile user.
 * This is one part of profile management beside contact, basic information.<br>
 *
 */

@ComponentConfigs({
  @ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "war:/groovy/social/webui/profile/UIExperienceSection.gtmpl",
    events = {
      @EventConfig(listeners = UIExperienceSection.EditActionListener.class, phase=Phase.DECODE),
      @EventConfig(listeners = UIExperienceSection.SaveActionListener.class),
      @EventConfig(listeners = UIExperienceSection.AddActionListener.class),
      @EventConfig(listeners = UIExperienceSection.RemoveActionListener.class),
      @EventConfig(listeners = UIProfileSection.CancelActionListener.class, phase=Phase.DECODE)
    }
  ),
  @ComponentConfig(
    type = UICheckBoxInput.class,
    id = "UIFormCheckBoxEndDate",
    events = @EventConfig(phase = Phase.DECODE, listeners = UIExperienceSection.ShowHideEndDateActionListener.class)
  )
})
public class UIExperienceSection extends UIProfileSection {

  /**
   * START DATE AFTER TODAY.
   */
  final public static String START_DATE_AFTER_TODAY = "UIExperienceSection.msg.StartDateAfterToday";

  /**
   * END DATE AFTER TODAY.
   */
  final public static String END_DATE_AFTER_TODAY = "UIExperienceSection.msg.EndDateAfterToday";

  /**
   * START DATE BEFORE END DATE.
   */
  final public static String STARTDATE_BEFORE_ENDDATE = "UIExperienceSection.msg.startDateBeforeEndDate";

  /**
   * DATE FORMAT.
   */
  final public static String DATE_FORMAT_MMDDYYYY = "MM/dd/yyyy";

  /** DATE TIME VALIDATE EXPRESSION. */
  private static final String DATETIME_REGEX =
     "^(\\d{1,2}\\/\\d{1,2}\\/\\d{1,4})\\s*(\\s+\\d{1,2}:\\d{1,2}:\\d{1,2})?$";
      
  /** MANDATORY START DATE MESSAGE. */
  private static final String INVALID_START_DATE_MANDATORY = "UIExperienceSection.msg.Invalid-startdate-mandatory";
  
  /** MANDATORY END DATE MESSAGE. */
  private static final String INVALID_END_DATE_MANDATORY = "UIExperienceSection.msg.Invalid-enddate-mandatory";
  
  /** INVALID FORMAT DATE TIME MESSAGE. */
  private static final String INVALID_START_DATE_FORMAT = "UIExperienceSection.msg.Invalid-startdate-format";
  
  /** INVALID FORMAT DATE TIME MESSAGE. */
  private static final String INVALID_END_DATE_FORMAT = "UIExperienceSection.msg.Invalid-enddate-format";
    
  /** INVALID COMPANY LENGTH MESSAGE. */
  private static final String INVALID_COMPANY_STRINGLENGTH = "UIExperienceSection.msg.Invalid-company-stringlength";
  
  /** MANDATORY COMPANY MESSAGE. */
  private static final String INVALID_COMPANY_MANDATORY = "UIExperienceSection.msg.Invalid-company-mandatory";
    
  /** INVALID POSITION LENGTH MESSAGE. */
  private static final String INVALID_POSITION_STRINGLENGTH = "UIExperienceSection.msg.Invalid-position-stringlength";
    
  /** MANDATORY POSITION MESSAGE. */
  private static final String INVALID_POSITION_MANDATORY = "UIExperienceSection.msg.Invalid-position-mandatory";

  /** Html attribute title. */
  private static final String HTML_ATTRIBUTE_TITLE   = "title";
  
  /**
   * Number of components.
   */
  int expIdx = 0;

  /**
   * Gets number of component.<br>
   *
   * @return Number of components.
   */
  public int getExpCount() {
    return expIdx;
  }

  /**
   * Gets all the children.<br>
   */
  public List<UIComponent> getChilds() {
    return getChildren();
  }

  /**
   * Constructor.<br>
   *
   * @throws Exception
   */
  public UIExperienceSection() throws Exception {
    addChild(UITitleBar.class, null, null);
  }

  /**
   * Gets month of date time.<br>
   *
   * @param inDate Input date
   * @return Month of input date.
   * @throws ParseException
   */
  public int getMonth(String inDate) throws ParseException {
    if ((inDate == null) || (inDate.length() == 0)) {
      return 0;
    }
    Calendar calendar = getCalendar(inDate);

    return (calendar.get(Calendar.MONTH) + 1); // Month start from 0
  }

  /**
   * Gets date of date time.<br>
   *
   * @param inDate Input date
   * @return Date of input date.
   * @throws ParseException
   */
  public int getDate(String inDate) throws ParseException {
    if ((inDate == null) || (inDate.length() == 0)) {
      return 0;
    }
    Calendar calendar = getCalendar(inDate);

    return calendar.get(Calendar.DATE);
  }

  /**
   * Convert calendar to string.
   * 
   * @param cal
   * @return
   * @since 1.2.5
   */
  protected String calendarToString(Calendar cal) {
    String sDate = "" ;
    if (cal != null) {
      SimpleDateFormat sd = new SimpleDateFormat(DATE_FORMAT_MMDDYYYY, Locale.ENGLISH);
      sDate = sd.format(cal.getTime());
    }
    return sDate ;
  }

  /**
   * Convert string to calendar.
   * 
   * @param sDate
   * @return
   * @since 1.2.5
   */
  protected Calendar stringToCalendar(String sDate) {
    try {
      SimpleDateFormat sd = new SimpleDateFormat(DATE_FORMAT_MMDDYYYY, Locale.ENGLISH);
      Calendar calendar = Calendar.getInstance() ;
      calendar.setTime(sd.parse(sDate));
      return calendar ;
    } catch (ParseException e) {
      return null ;
    }
  }

  /**
   * Display date time.
   * 
   * @param d
   * @return
   * @since 1.2.5
   * @throws ParseException
   */
  protected String displayDateTime(Object d) throws ParseException{
    Date date = this.stringToCalendar(d.toString()).getTime(); 
    Locale l = WebuiRequestContext.getCurrentInstance().getLocale();
    DateFormat sf = SimpleDateFormat.getDateInstance(DateFormat.LONG, l);
    return sf.format(date) ;
  }

  /**
   * Gets year of date time.<br>
   *
   * @param inDate Input date
   * @return Year of input date.
   * @throws ParseException
   */
  public int getYear(String inDate) throws ParseException {
    if ((inDate == null) || (inDate.length() == 0)) {
      return 0;
    }
    Calendar calendar = getCalendar(inDate);

    return calendar.get(Calendar.YEAR);
  }

  /**
   * Adds component when Add button is clicked. <br>
   */
  public static class AddActionListener extends EventListener<UIExperienceSection> {
    @Override
    public void execute(Event<UIExperienceSection> event) throws Exception {
      UIExperienceSection uiForm = event.getSource();
      uiForm.addUIFormInput();
    }
  }

  /**
   * Adds component when Add button is clicked. <br>
   */
  public static class RemoveActionListener extends EventListener<UIExperienceSection> {
    @Override
    public void execute(Event<UIExperienceSection> event) throws Exception {
      UIExperienceSection uiForm = event.getSource();
      String block = event.getRequestContext().getRequestParameter(OBJECTID);
      int blockIdx = Integer.parseInt(block);
      String companyId = null;
      String positionId = null;
      String descriptionId = null;
      String skillsId = null;
      String startDateId = null;
      String endDateId = null;
      String isCurrentId = null;

      List<UIComponent> listChild = uiForm.getChilds();

      companyId = listChild.get(blockIdx).getId();
      positionId = listChild.get(blockIdx + 1).getId();
      descriptionId = listChild.get(blockIdx + 2).getId();
      skillsId = listChild.get(blockIdx + 3).getId();
      startDateId = listChild.get(blockIdx + 4).getId();
      endDateId = listChild.get(blockIdx + 5).getId();
      isCurrentId = listChild.get(blockIdx + 6).getId();

      uiForm.removeChildById(companyId);
      uiForm.removeChildById(positionId);
      uiForm.removeChildById(descriptionId);
      uiForm.removeChildById(skillsId);
      uiForm.removeChildById(startDateId);
      uiForm.removeChildById(endDateId);
      uiForm.removeChildById(isCurrentId);
      
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
      Utils.resizeHomePage();
    }
  }

  /**
   * Saves experience informations to profile.<br>
   */
  public static class SaveActionListener extends UIProfileSection.SaveActionListener {

    @Override
    public void execute(Event<UIProfileSection> event) throws Exception {
      UIProfileSection sect = event.getSource();
      UIExperienceSection uiExperienceSectionSect = (UIExperienceSection) sect;
      if (uiExperienceSectionSect.saveProfileInfo() == 0) {
        super.execute(event);
      }
    }
  }

  /**
   * Changes to edit mode when Edit button is clicked.<br>
   */
  public static class EditActionListener extends UIProfileSection.EditActionListener {

    @Override
    @SuppressWarnings("unchecked")
    public void execute(Event<UIProfileSection> event) throws Exception {
      UIProfileSection sect = event.getSource();
      UIExperienceSection uiExpSection = (UIExperienceSection) sect;
      ArrayList<HashMap<String, Object>> experiences;
      Profile p = sect.getProfile();
      List<UIComponent> listChild = uiExpSection.getChilds();
      List<Object> listProfile = new ArrayList<Object>();
      int childSize = listChild.size() - 1; // List of children include UITitleBar child.
      String companyId = null;
      String positionId = null;
      String skillsId = null;
      String startDateId = null;
      String isCurrentId = null;
      String endDateId = null;
      String descriptionId = null;

      experiences = (ArrayList<HashMap<String, Object>>) p.getProperty(Profile.EXPERIENCES);
      if (experiences == null) {
        for (int idx = 1; idx <= childSize; idx += 7) {
          sect.removeChild(UIFormStringInput.class);
          sect.removeChild(UIFormStringInput.class);
          sect.removeChild(UIFormTextAreaInput.class);
          sect.removeChild(UIFormTextAreaInput.class);
          sect.removeChild(UIFormDateTimeInput.class);
          sect.removeChild(UIFormDateTimeInput.class);
          sect.removeChild(UICheckBoxInput.class);
        }
      } else {
        for (HashMap<String, Object> map : experiences) {
          listProfile.add(StringEscapeUtils.unescapeHtml((String)map.get(Profile.EXPERIENCES_COMPANY)));
          listProfile.add(StringEscapeUtils.unescapeHtml((String)map.get(Profile.EXPERIENCES_POSITION)));
          listProfile.add(StringEscapeUtils.unescapeHtml((String)map.get(Profile.EXPERIENCES_DESCRIPTION)));
          listProfile.add(StringEscapeUtils.unescapeHtml((String)map.get(Profile.EXPERIENCES_SKILLS)));
          listProfile.add(map.get(Profile.EXPERIENCES_START_DATE));
          listProfile.add(map.get(Profile.EXPERIENCES_END_DATE));
          listProfile.add(map.get(Profile.EXPERIENCES_IS_CURRENT));
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
            companyId = listChild.get(childSize - 6).getName();
            positionId = listChild.get(childSize - 5).getName();
            descriptionId = listChild.get(childSize - 4).getName();
            skillsId = listChild.get(childSize - 3).getName();
            startDateId = listChild.get(childSize - 2).getName();
            endDateId = listChild.get(childSize - 1).getName();
            isCurrentId = listChild.get(childSize).getName();

            sect.removeChildById(companyId);
            sect.removeChildById(positionId);
            sect.removeChildById(descriptionId);
            sect.removeChildById(skillsId);
            sect.removeChildById(startDateId);
            sect.removeChildById(endDateId);
            sect.removeChildById(isCurrentId);

            numberOfChildren -= 7;
          }
        }

        List<UIComponent> listChildForSetValue = uiExpSection.getChilds();
        for (int idx = 0; idx < totalProfiles; idx += 7) {
          ((UIFormInput) listChildForSetValue.get(idx + 1)).setValue(listProfile.get(idx));
          ((UIFormInput) listChildForSetValue.get(idx + 2)).setValue(listProfile.get(idx + 1));
          ((UIFormInput) listChildForSetValue.get(idx + 3)).setValue(listProfile.get(idx + 2)); // des
          ((UIFormInput) listChildForSetValue.get(idx + 4)).setValue(listProfile.get(idx + 3)); // skills

          if (listProfile.get(idx + 4) != null) {
            // start date
            ((UIFormDateTimeInput) listChildForSetValue.get(idx + 5)).setCalendar(uiExpSection.stringToCalendar(
                                                                                  listProfile.get(idx + 4).toString()));
          }
          
          if (listProfile.get(idx + 5) != null) {
            // end date
            ((UIFormDateTimeInput) listChildForSetValue.get(idx + 6)).setCalendar(uiExpSection.stringToCalendar(
                                                                                  listProfile.get(idx + 5).toString()));
          } else {
            // end date
            ((UIFormInput) listChildForSetValue.get(idx + 6)).setValue(listProfile.get(idx + 5));
          }

          ((UICheckBoxInput) listChildForSetValue.get(idx + 7)).setValue((Boolean) listProfile.get(idx + 6));
          ((UIFormDateTimeInput)listChildForSetValue.get(idx + 6)).setRendered(!(
                                (UICheckBoxInput)listChildForSetValue.get(idx + 7)).getValue());
        }
      }

      super.execute(event);
    }
  }

  /**
   * Shows and hides end date component depending on isCurrent variable.<br>
   */
  static public class ShowHideEndDateActionListener extends EventListener<UICheckBoxInput> {

    @Override
    public void execute(Event<UICheckBoxInput> event) throws Exception {
      UICheckBoxInput sect = event.getSource();
      UIExperienceSection uiForm = sect.getAncestorOfType(UIExperienceSection.class);
      UIFormDateTimeInput uiDateTime = uiForm.getChildById(Profile.EXPERIENCES_END_DATE + sect.getId());
      boolean isCheck = sect.isChecked();
      uiDateTime.setRendered(!isCheck);

      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
      Utils.resizeHomePage();
    }
  }

  /**
   * Gets all the past experience.<br>
   *
   * @return all the experience that has isCurrent is false.
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public List<HashMap<String, Object>> getPastExperience() throws Exception {
    ArrayList<HashMap<String, Object>> experiences;
    ArrayList<HashMap<String, Object>> pastExperiences = new ArrayList<HashMap<String, Object>>();
    Profile p = getProfile();
    experiences = (ArrayList<HashMap<String, Object>>) p.getProperty(Profile.EXPERIENCES);
    if (experiences != null) {
      for (HashMap<String, Object> map : experiences) {
        if (!(Boolean) map.get(Profile.EXPERIENCES_IS_CURRENT)) {
          pastExperiences.add(map);
        }
      }
    }

    return pastExperiences;
  }

  /**
   * Gets all the current experience.<br>
   *
   * @return all the experience that has isCurrent is true.
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public List<HashMap<String, Object>> getCurrentExperience() throws Exception {
    ArrayList<HashMap<String, Object>> experiences;
    ArrayList<HashMap<String, Object>> currentExperiences = new ArrayList<HashMap<String, Object>>();
    Profile p = getProfile();
    experiences = (ArrayList<HashMap<String, Object>>) p.getProperty(Profile.EXPERIENCES);
    if (experiences != null) {
      for (HashMap<String, Object> map : experiences) {
        if ((Boolean) map.get(Profile.EXPERIENCES_IS_CURRENT)) {
          currentExperiences.add(map);
        }
      }
    }

    return currentExperiences;
  }

  /**
   * Saves input information into profile.<br>
   *
   * @return Integer value (0: save complete; 1,2: an error has occurs)
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  private int saveProfileInfo() throws Exception {
    ArrayList<HashMap<String, Object>> experiences = new ArrayList<HashMap<String, Object>>();
    UIFormStringInput uiStringInput = null;
    UIFormTextAreaInput uiFormTextAreaInput = null;
    UICheckBoxInput uiCheckBox = null;
    UIFormDateTimeInput uiDateTimeInput = null;
    String company = null;
    String position = null;
    String description = null;
    String skills = null;
    String startDate = null;
    String endDate = null;
    Boolean isCurrent = null;
    // Check if there is any exception during parsing Date Time field
    boolean isDateTimeException = false;
    Date sDate = null;
    Date eDate = null;
    Date today = new Date();

    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    UIApplication uiApplication = context.getUIApplication();
    Profile p = getProfile();
    List<UIComponent> listUIComp = getChilds();
    int totalUIComponent = listUIComp.size() - 1; // List of children not include UITitleBar child.
    
    int errorCode = 0;
    if (totalUIComponent == 0) {
      if (p != null) {
        Profile updateProfile = new Profile(p.getIdentity());
        updateProfile.setId(p.getId());
        updateProfile.setProperty(Profile.EXPERIENCES, experiences);
        Utils.getIdentityManager().updateExperienceSection(updateProfile);
      }

      return errorCode;
    }

    for (int i = 1; i <= totalUIComponent; i += 7) {
      HashMap<String, Object> uiMap = new HashMap<String, Object>();

      uiStringInput = (UIFormStringInput) listUIComp.get(i);
      company = uiStringInput.getValue();

      if ((company == null) || (company.length() == 0)) {
        uiApplication.addMessage(new ApplicationMessage(INVALID_COMPANY_MANDATORY, null, 1));
        errorCode = 1;
      }
      
      uiStringInput = (UIFormStringInput) listUIComp.get(i + 1);
      position = uiStringInput.getValue();

      if ((position == null) || (position.length() == 0)) {
        uiApplication.addMessage(new ApplicationMessage(INVALID_POSITION_MANDATORY, null, 1));
        errorCode = 1;
      }
      
      uiFormTextAreaInput = (UIFormTextAreaInput) listUIComp.get(i + 2);
      description = uiFormTextAreaInput.getValue();

      uiFormTextAreaInput = (UIFormTextAreaInput) listUIComp.get(i + 3);
      skills = uiFormTextAreaInput.getValue();

      isDateTimeException = false;
      uiDateTimeInput = (UIFormDateTimeInput) listUIComp.get(i + 4);
      Locale locale = context.getParentAppRequestContext().getLocale();
      String currentPattern = uiDateTimeInput.getDatePattern_();
      
      SimpleDateFormat sf = new SimpleDateFormat(currentPattern, locale);
      // Specify whether or not date/time parsing is to be lenient.
      sf.setLenient(false);
      Calendar cal = Calendar.getInstance();

      String startDateInput = uiDateTimeInput.getValue();      
      if (startDateInput != null && startDateInput.length() > 0) {
        try {
          cal.setTime(sf.parse(startDateInput));
          startDate = calendarToString(cal);
        } catch (Exception e) {
          uiApplication.addMessage(new ApplicationMessage(INVALID_START_DATE_FORMAT, new String[] {currentPattern}, 1));
          errorCode = 1;
          isDateTimeException = true;
        }
      } else {
        startDate = null;
      }
      
      uiDateTimeInput = (UIFormDateTimeInput) listUIComp.get(i + 5);
      String endDateInput = uiDateTimeInput.getValue();
      if (endDateInput != null && endDateInput.length() > 0) {
    	 try {
    	   cal.setTime(sf.parse(endDateInput)) ;
    	 } catch (Exception e) {
    	   uiApplication.addMessage(new ApplicationMessage(INVALID_END_DATE_FORMAT, new String[] {currentPattern}, 1));
    	   errorCode = 1;
    	   isDateTimeException = true;
    	 }
      } else {
        endDate = null;
      }

      //Only check other conditions if there is not any Date Time exception 
      if (!isDateTimeException) {
        uiCheckBox = (UICheckBoxInput) listUIComp.get(i + 6);
        isCurrent = uiCheckBox.getValue();
      
        if (startDate == null && (endDate != null || isCurrent.booleanValue()) ||
            "".equals(startDate) && ("".equals(endDate)|| isCurrent.booleanValue())) {
          uiApplication.addMessage(new ApplicationMessage(INVALID_START_DATE_MANDATORY, null, 1));
          errorCode = 1;
        }
      
        if (startDate != null && !"".equals(startDate)) {
          endDate = calendarToString(cal);

          sDate = stringToDate(startDate);
          eDate = stringToDate(endDate);
          if (endDate != null && !"".equals(endDate) && sDate.after(today)) {
            uiApplication.addMessage(new ApplicationMessage(START_DATE_AFTER_TODAY, null, 1));
            errorCode = 1;
          }

          if (!isCurrent) {
            if ((endDate == null) || (endDate.length() == 0)) {
              uiApplication.addMessage(new ApplicationMessage(INVALID_END_DATE_MANDATORY, null, 1));
              errorCode = 1;
            }
          
            if ((eDate != null) && eDate.after(today)) {
              uiApplication.addMessage(new ApplicationMessage(END_DATE_AFTER_TODAY, null, 1));
              errorCode = 1;
            }
            if ((sDate != null) && sDate.after(eDate)) {
              uiApplication.addMessage(new ApplicationMessage(STARTDATE_BEFORE_ENDDATE, null, 1));
              errorCode = 1;
            }
          } else {
            endDate = null;
          }
        }
      }

      uiMap.put(Profile.EXPERIENCES_COMPANY, escapeHtml(company));
      uiMap.put(Profile.EXPERIENCES_POSITION, escapeHtml(position));
      uiMap.put(Profile.EXPERIENCES_DESCRIPTION, escapeHtml(description));
      uiMap.put(Profile.EXPERIENCES_SKILLS, escapeHtml(skills));
      
      uiMap.put(Profile.EXPERIENCES_START_DATE, startDate);
      uiMap.put(Profile.EXPERIENCES_END_DATE, endDate);
      uiMap.put(Profile.EXPERIENCES_IS_CURRENT, isCurrent);

      experiences.add(uiMap);
    }
    
    if (errorCode == 1) {
      return errorCode;
    }
    
    p.setProperty(Profile.EXPERIENCES, experiences);
    Utils.getIdentityManager().updateProfile(p);

    return errorCode;
  }

  /**
   * Adds component when add buttons clicked.<br>
   *
   * @throws Exception
   */
  private void addUIFormInput() throws Exception {
    final String JOB_TITLE = "jobtitle";
    WebuiRequestContext requestContext = WebuiRequestContext.getCurrentInstance();
    ResourceBundle resourceBundle = requestContext.getApplicationResourceBundle();
    expIdx += 1;
    addUIFormInput(new UIFormStringInput(Profile.EXPERIENCES_COMPANY + expIdx, null, null));
    addUIFormInput(new UIFormStringInput(Profile.EXPERIENCES_POSITION + expIdx, null, null)
      .addValidator(UserConfigurableValidator.class, JOB_TITLE, UserConfigurableValidator.KEY_PREFIX + JOB_TITLE, false));

    UIFormTextAreaInput description = new UIFormTextAreaInput(Profile.EXPERIENCES_DESCRIPTION + expIdx, null, null);
    description.setHTMLAttribute(HTML_ATTRIBUTE_TITLE, resourceBundle.getString("UIExperienceSection.label.description"));
    addUIFormInput(description);
    UIFormTextAreaInput uiDespcription = getChildById(Profile.EXPERIENCES_DESCRIPTION + expIdx);
    uiDespcription.setColumns(28);
    uiDespcription.setRows(3);

    UIFormTextAreaInput skills = new UIFormTextAreaInput(Profile.EXPERIENCES_SKILLS + expIdx, null, null);
    skills.setHTMLAttribute(HTML_ATTRIBUTE_TITLE, resourceBundle.getString("UIExperienceSection.label.skills"));
    addUIFormInput(skills);
    UIFormTextAreaInput uiFormTextAreaInput = getChildById(Profile.EXPERIENCES_SKILLS + expIdx);
    uiFormTextAreaInput.setColumns(28);
    uiFormTextAreaInput.setRows(3);

    UIFormDateTimeInput startDate = new UIFormDateTimeInput(Profile.EXPERIENCES_START_DATE + expIdx, null, null, false);
    startDate.setHTMLAttribute(HTML_ATTRIBUTE_TITLE, resourceBundle.getString("UIExperienceSection.label.startDate"));
    addUIFormInput(startDate);

    UIFormDateTimeInput endDate = new UIFormDateTimeInput(Profile.EXPERIENCES_END_DATE + expIdx, null, null, false);
    endDate.setHTMLAttribute(HTML_ATTRIBUTE_TITLE, resourceBundle.getString("UIExperienceSection.label.endDate"));
    addUIFormInput(endDate);

    UICheckBoxInput uiDateInputCheck = new UICheckBoxInput(Integer.toString(expIdx), null, false);
    uiDateInputCheck.setComponentConfig(UICheckBoxInput.class, "UIFormCheckBoxEndDate");
    uiDateInputCheck.setOnChange("ShowHideEndDate", uiDateInputCheck.getId());
    addUIFormInput(uiDateInputCheck);

    //    addUIFormInput(new UIFormStringInput(DESCRIPTION + expIdx, null, null));
  }

  /**
   * Formats String type to Date type.<br>
   *
   * @param dateStr Input String
   * @return Converted date.
   * @throws ParseException
   */
  private Date stringToDate(String dateStr) throws ParseException {
    SimpleDateFormat formatDate = new SimpleDateFormat(DATE_FORMAT_MMDDYYYY, Locale.ENGLISH);
    if ((dateStr == null) || (dateStr.trim().length() == 0)) {
      return null;
    }
    return formatDate.parse(dateStr);
  }

  /**
   * Gets calendar with string date input.<br>
   *
   * @param inDate Date input.
   * @return calendar.
   * @throws ParseException
   */
  private Calendar getCalendar(String inDate) throws ParseException {
    SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT_MMDDYYYY, Locale.ENGLISH);
    Date date = format.parse(inDate);
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    return calendar;
  }  
}
