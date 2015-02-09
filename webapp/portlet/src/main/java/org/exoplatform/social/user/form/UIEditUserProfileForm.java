package org.exoplatform.social.user.form;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.user.form.UIInputSection.ActionData;
import org.exoplatform.social.user.portlet.UserProfileHelper;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.application.RequireJS;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormDateTimeInput;
import org.exoplatform.webui.form.UIFormInputBase;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormMultiValueInputSet;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.input.UICheckBoxInput;
import org.exoplatform.webui.form.validator.EmailAddressValidator;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.PersonalNameValidator;
import org.exoplatform.webui.form.validator.StringLengthValidator;
import org.exoplatform.webui.form.validator.URLValidator;

@ComponentConfig(
   lifecycle = UIFormLifecycle.class,
   template = "app:/groovy/social/portlet/user/UIEditUserProfileForm.gtmpl",
   events = {
     @EventConfig(listeners = UIEditUserProfileForm.AddExperienceActionListener.class, phase = Phase.DECODE),
     @EventConfig(listeners = UIEditUserProfileForm.RemoveExperienceActionListener.class, phase = Phase.DECODE),
     @EventConfig(listeners = UIEditUserProfileForm.CancelActionListener.class, phase = Phase.DECODE),
     @EventConfig(listeners = UIEditUserProfileForm.SaveActionListener.class)
   }
)
public class UIEditUserProfileForm extends UIForm {
  public static final String PLACEHOLDER_KEY = "placeholder";

  public static final String FIELD_ABOUT_SECTION = "AboutSection";
  public static final String FIELD_BASE_SECTION = "BaseSection";
  public static final String FIELD_EXPERIENCE_SECTION = "ExperienceSection";
  public static final String OPTION_MALE = "Male";
  public static final String OPTION_FEMALE = "Female";
  public static final String FIELD_EMPTY_EXPERIENCE_SECTION = "emprySelection";
  public static final String FIELD_INFO_EMPTY_EXPERIENCE = "infoExperien";
  public static final String DATE_FORMAT_MMDDYYYY = "MM/dd/yyyy";
  /** PHONE_TYPES. */
  public static final String[] PHONE_TYPES = new String[] {"work","home","other"};
  /** IM_TYPES. */
  public static final String[] IM_TYPES = new String[] {"gtalk","msn","skype","yahoo","other"};
  private Profile currentProfile;
  private List<String> experiens = new LinkedList<String>();
  private int index = 0;
  
  public UIEditUserProfileForm() throws Exception {
    if (getId() == null) {
      setId("UIEditUserProfileForm");
    }
    UIInputSection aboutSection = new UIInputSection(FIELD_ABOUT_SECTION, "AboutMe");
    aboutSection.useGroupControl(false)
                .addUIFormInput(new UIFormTextAreaInput(Profile.ABOUT_ME, Profile.ABOUT_ME, null));
    //
    UIInputSection baseSection = new UIInputSection(FIELD_BASE_SECTION, "ContactInfomation");
    baseSection.addUIFormInput(createUIFormStringInput(Profile.FIRST_NAME, true)
                               .addValidator(PersonalNameValidator.class).addValidator(StringLengthValidator.class, 1, 45));
    //
    baseSection.addUIFormInput(createUIFormStringInput(Profile.LAST_NAME, true)
                               .addValidator(PersonalNameValidator.class).addValidator(StringLengthValidator.class, 1, 45));
    //
    baseSection.addUIFormInput(createUIFormStringInput(Profile.EMAIL, true).addValidator(EmailAddressValidator.class));
    //
    UIChangeAvatarContainer avatarContainer = createUIComponent(UIChangeAvatarContainer.class, null, "Avatar");
    baseSection.addUIFormInput(avatarContainer);
    //
    baseSection.addUIFormInput(createUIFormStringInput(Profile.POSITION, false));
    //
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>();
    options.add(new SelectItemOption<String>(getLabel(OPTION_MALE), OPTION_MALE));
    options.add(new SelectItemOption<String>(getLabel(OPTION_FEMALE), OPTION_FEMALE));
    UIFormSelectBox genderSelectBox = new UIFormSelectBox(Profile.GENDER, Profile.GENDER, options);
    genderSelectBox.setLabel(Profile.GENDER);
    baseSection.addUIFormInput(genderSelectBox);
    //
    UIMultiValueSelection phoneSelection = new UIMultiValueSelection(Profile.CONTACT_PHONES, getId(), Arrays.asList(PHONE_TYPES));
    baseSection.addUIFormInput(phoneSelection.addValidator(StringLengthValidator.class, 1, 20));
    //
    UIMultiValueSelection imsSelection = new UIMultiValueSelection(Profile.CONTACT_IMS, getId(), Arrays.asList(IM_TYPES));
    baseSection.addUIFormInput(imsSelection.addValidator(StringLengthValidator.class, 1, 60));
    //
    UIFormMultiValueInputSet urlMultiValueInput = new UIFormMultiValueInputSet(Profile.CONTACT_URLS, Profile.CONTACT_URLS);
    urlMultiValueInput.setType(UIFormStringInput.class);
    urlMultiValueInput.setValue(Arrays.asList(""));
    urlMultiValueInput.setLabel(Profile.CONTACT_URLS);
    baseSection.addUIFormInput(urlMultiValueInput);
    //
    addUIFormInput(aboutSection);
    addUIFormInput(baseSection);
  }
  
  private List<ActionData> createExperienceActions(String experienId, boolean hasAdd) {
    List<ActionData> actions = new ArrayList<UIInputSection.ActionData>();
    ActionData removeAction = new ActionData();
    removeAction.setAction("RemoveExperience").setIcon("uiIconClose")
                .setTooltip(UserProfileHelper.getLabel(null, "UIEditUserProfileForm.title.RemoveExperience")).setObjectId(experienId);
    actions.add(removeAction);
    if(hasAdd) {
      ActionData addAction = new ActionData();
      addAction.setAction("AddExperience").setIcon("uiIconPlus")
               .setTooltip(UserProfileHelper.getLabel(null, "UIEditUserProfileForm.title.AddExperience")).setObjectId(experienId);
      actions.add(addAction);
    }
    return actions;
  }

  private UIInputSection getOrCreateExperienceSection(String id) throws Exception {
    UIInputSection experienceSection = getChildById(id);
    if(experienceSection != null) {
      return experienceSection;
    }
    String label = (experiens.size() == 0) ? "Experience" : "";
    experienceSection = new UIInputSection(id, label, "uiExperien");

    //
    UIFormStringInput company = createUIFormStringInput(Profile.EXPERIENCES_COMPANY + id, true);
    company.setLabel(Profile.EXPERIENCES_COMPANY);
    experienceSection.addUIFormInput(company);
    //
    experienceSection.addUIFormInput(createUIFormStringInput(Profile.EXPERIENCES_POSITION + id, true), Profile.EXPERIENCES_POSITION);
    //
    experienceSection.addUIFormInput(new UIFormTextAreaInput(Profile.EXPERIENCES_DESCRIPTION + id,
                                                             Profile.EXPERIENCES_DESCRIPTION + id, ""), Profile.EXPERIENCES_DESCRIPTION);
    //
    experienceSection.addUIFormInput(new UIFormTextAreaInput(Profile.EXPERIENCES_SKILLS + id,
                                                             Profile.EXPERIENCES_SKILLS + id, ""), Profile.EXPERIENCES_SKILLS);
    //
    experienceSection.addUIFormInput(new UIFormDateTimeInput(Profile.EXPERIENCES_START_DATE + id,
                                                             Profile.EXPERIENCES_START_DATE + id, null, false), Profile.EXPERIENCES_START_DATE);
    //
    experienceSection.addUIFormInput(new UIFormDateTimeInput(Profile.EXPERIENCES_END_DATE + id,
                                                             Profile.EXPERIENCES_END_DATE + id, null, false), Profile.EXPERIENCES_END_DATE);
    //
    experienceSection.addUIFormInput(new UICheckBoxInput(Profile.EXPERIENCES_IS_CURRENT + id,
                                                         Profile.EXPERIENCES_IS_CURRENT + id, false), "CurrentPosition");
    //
    addUIFormInput(experienceSection);
    //
    ++index;
    experiens.add(id);
    return experienceSection;
  }

  protected UIInputSection setValueExperienceSection(String id, Map<String, String> experiance) throws Exception {
    UIInputSection experienceSection = getOrCreateExperienceSection(id);
    experienceSection.getUIStringInput(Profile.EXPERIENCES_COMPANY + id).setValue(experiance.get(Profile.EXPERIENCES_COMPANY));
    experienceSection.getUIStringInput(Profile.EXPERIENCES_POSITION + id).setValue(experiance.get(Profile.EXPERIENCES_POSITION));
    experienceSection.getUIFormTextAreaInput(Profile.EXPERIENCES_DESCRIPTION + id).setValue(experiance.get(Profile.EXPERIENCES_DESCRIPTION));
    experienceSection.getUIFormTextAreaInput(Profile.EXPERIENCES_SKILLS + id).setValue(experiance.get(Profile.EXPERIENCES_SKILLS));
    experienceSection.getUIFormDateTimeInput(Profile.EXPERIENCES_START_DATE + id).setCalendar(stringToCalendar(experiance.get(Profile.EXPERIENCES_START_DATE)));
    experienceSection.getUIFormDateTimeInput(Profile.EXPERIENCES_END_DATE + id).setCalendar(stringToCalendar(experiance.get(Profile.EXPERIENCES_END_DATE)));
    experienceSection.getUICheckBoxInput(Profile.EXPERIENCES_IS_CURRENT + id).setChecked(Boolean.valueOf(experiance.get(Profile.EXPERIENCES_IS_CURRENT)));
    //
    return experienceSection;
  }

  private String getStringValueProfile(String key) {
    return (String) currentProfile.getProperty(key);
  }

  protected void setValueBasicInfo() throws Exception {
    //about me
    getUIInputSection(FIELD_ABOUT_SECTION).getUIFormTextAreaInput(Profile.ABOUT_ME)
                                          .setValue(getStringValueProfile(Profile.ABOUT_ME));
    //Basic information
    UIInputSection baseSection = getUIInputSection(FIELD_BASE_SECTION);
    baseSection.getUIStringInput(Profile.FIRST_NAME).setValue(getStringValueProfile(Profile.FIRST_NAME));
    baseSection.getUIStringInput(Profile.LAST_NAME).setValue(getStringValueProfile(Profile.LAST_NAME));
    baseSection.getUIStringInput(Profile.EMAIL).setValue(getStringValueProfile(Profile.EMAIL));
    baseSection.getUIStringInput(Profile.POSITION).setValue(getStringValueProfile(Profile.POSITION));
    //
    baseSection.getUIFormSelectBox(Profile.GENDER).setValue(getStringValueProfile(Profile.GENDER));
    //
    List<Map<String, String>> phones = UserProfileHelper.getMultiValues(currentProfile, Profile.CONTACT_PHONES);
    baseSection.getUIMultiValueSelection(Profile.CONTACT_PHONES).setValues(phones);
    //
    List<Map<String, String>> ims = UserProfileHelper.getMultiValues(currentProfile, Profile.CONTACT_IMS);
    baseSection.getUIMultiValueSelection(Profile.CONTACT_IMS).setValues(ims);
    
    baseSection.getUIFormMultiValueInputSet(Profile.CONTACT_URLS).setValue(UserProfileHelper.getURLValues(currentProfile));
    //Experience
    List<Map<String, String>> experiences = UserProfileHelper.getDisplayExperience(currentProfile);
    if(!experiences.isEmpty()) {
      int i = 0;
      String experienId;
      removeChildById(FIELD_EMPTY_EXPERIENCE_SECTION);
      int experienCurrentSize = experiens.size();
      for (Map<String, String> experience : experiences) {
        if (i < experienCurrentSize) {
          experienId = experiens.get(i);
          ++i;
        } else {
          experienId = FIELD_EXPERIENCE_SECTION + index;
        }
        setValueExperienceSection(experienId, experience);
      }
      resetActionFileds();
    } else if (experiens.isEmpty()) {
      addEmpryExperienceSection();
    }
  }
  
  private void addEmpryExperienceSection() throws Exception {
    //
    UIInputSection emprySelection = getChildById(FIELD_EMPTY_EXPERIENCE_SECTION);
    if(emprySelection == null) {
      emprySelection = new UIInputSection(FIELD_EMPTY_EXPERIENCE_SECTION, "Experience", "uiExperien").useGroupControl(false);
      ActionData addAction = new ActionData();
      addAction.setAction("AddExperience").setIcon("uiIconPlus")
               .setTooltip(UserProfileHelper.getLabel(null, "UIEditUserProfileForm.title.AddExperience"))
               .setObjectId(FIELD_EXPERIENCE_SECTION + "empty");
      emprySelection.addUIFormInput(new UIFormInputInfo(FIELD_INFO_EMPTY_EXPERIENCE, FIELD_INFO_EMPTY_EXPERIENCE,
                                                        getLabel("AddExperience")), Arrays.asList(addAction));
      //
      addUIFormInput(emprySelection);
    }
  }
  
  protected Calendar stringToCalendar(String sDate) {
    try {
      SimpleDateFormat sd = new SimpleDateFormat(DATE_FORMAT_MMDDYYYY, Locale.ENGLISH);
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(sd.parse(sDate));
      return calendar;
    } catch (Exception e) {
      return null;
    }
  }

  private UIFormStringInput createUIFormStringInput(String name, boolean isMandatory) throws Exception {
    UIFormStringInput firstName = new UIFormStringInput(name, name, "");
    if (isMandatory) {
      firstName.addValidator(MandatoryValidator.class);
    }
    firstName.setLabel(name);
    return firstName;
  }
  /**
   * @throws Exception
   */
  private void initPlaceholder() throws Exception {
    //
    getUIInputSection(FIELD_ABOUT_SECTION).getUIFormTextAreaInput(Profile.ABOUT_ME)
                   .setHTMLAttribute(PLACEHOLDER_KEY, "Introduce yourself to others");
    //
    UIInputSection baseSection = getUIInputSection(FIELD_BASE_SECTION);
    UIFormMultiValueInputSet urlMulti = baseSection.getChildById(Profile.CONTACT_URLS);
    List<UIComponent> children = urlMulti.getChildren();
    for (UIComponent uiComponent : children) {
      if(uiComponent instanceof UIFormInputBase) {
        ((UIFormInputBase<?>)uiComponent).setHTMLAttribute(PLACEHOLDER_KEY, getLabel("sampleUrl"));
      }
    }
    //
    List<UIInputSection> experienceSections = getExperienceSections();
    for (UIInputSection uiInputSection : experienceSections) {
      List<UIFormDateTimeInput> dateInputs = new ArrayList<UIFormDateTimeInput>();
      uiInputSection.findComponentOfType(dateInputs, UIFormDateTimeInput.class);
      for (UIFormDateTimeInput uiFormDateTimeInput : dateInputs) {
        uiFormDateTimeInput.setHTMLAttribute(PLACEHOLDER_KEY, DATE_FORMAT_MMDDYYYY);
      }
    }
  }
  /**
   * @return
   */
  private List<UIInputSection> getExperienceSections() {
    List<UIInputSection> experienceSections = new ArrayList<UIInputSection>();
    List<UIComponent> children = this.getChildren();
    for (UIComponent uiComponent : children) {
      if (uiComponent.getId().startsWith(FIELD_EXPERIENCE_SECTION)) {
        experienceSections.add((UIInputSection) uiComponent);
      }
    }
    return experienceSections;
  }
  
  private void clearExperiences() {
    for (String string : experiens) {
      removeChildById(string);
    }
    //
    experiens.clear();
    index = 0;
  }
  
  /**
   * @param id
   * @return
   */
  private UIInputSection getUIInputSection(String id) {
    return (UIInputSection) getChildById(id);
  }

  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
    //
    if (!Util.getPortalRequestContext().useAjax()) {
      resetForm();
      //
      initPlaceholder();
    }
    //
    if(this.currentProfile == null) {
      this.currentProfile = Utils.getViewerIdentity(true).getProfile();
      this.setValueBasicInfo();
    }
    //
    super.processRender(context);
  }
  /**
   * Reset actions on experience
   */
  private void resetActionFileds() {
    List<UIInputSection> experienceSections = getExperienceSections();
    int i = 1;
    int experienceSize = experienceSections.size();
    for (UIInputSection uiInputSection : experienceSections) {
      boolean hasAdd = (i ==  experienceSize);
      String experienId = uiInputSection.getName();
      uiInputSection.setActionField(Profile.EXPERIENCES_COMPANY + experienId, createExperienceActions(uiInputSection.getName(), hasAdd));
      ++i;
    }
  }
  /**
   * Reset form
   */
  private void resetForm() {
    currentProfile = null;
    clearExperiences();
  }
  
  /**
   * @param messageKey
   * @param args
   */
  private void warning(String messageKey, String... args) {
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    context.getUIApplication().addMessage(new ApplicationMessage(messageKey, args, ApplicationMessage.WARNING));
    ((PortalRequestContext) context.getParentAppRequestContext()).ignoreAJAXUpdateOnPortlets(true);
  }
  /**
   * @param map
   * @param key
   * @param value
   */
  private void putData(Map<String, String> map, String key, String value) {
    if (value != null && !value.isEmpty()) {
      map.put(key, StringEscapeUtils.escapeHtml(value));
    }
  }
  /**
   * @param experienceSection
   * @return
   * @throws Exception
   */
  private Map<String, String> getValueExperience(UIInputSection experienceSection) throws Exception {
    String id = experienceSection.getId();
    Map<String, String> map = new HashMap<String, String>();
    putData(map, Profile.EXPERIENCES_COMPANY, experienceSection.getUIStringInput(Profile.EXPERIENCES_COMPANY + id).getValue());
    putData(map, Profile.EXPERIENCES_POSITION, experienceSection.getUIStringInput(Profile.EXPERIENCES_POSITION + id).getValue());
    putData(map, Profile.EXPERIENCES_DESCRIPTION, experienceSection.getUIFormTextAreaInput(Profile.EXPERIENCES_DESCRIPTION + id).getValue());
    putData(map, Profile.EXPERIENCES_SKILLS, experienceSection.getUIFormTextAreaInput(Profile.EXPERIENCES_SKILLS + id).getValue());
    //
    UIFormDateTimeInput startDate = experienceSection.getUIFormDateTimeInput(Profile.EXPERIENCES_START_DATE + id);
    UIFormDateTimeInput endDate = experienceSection.getUIFormDateTimeInput(Profile.EXPERIENCES_END_DATE + id);
    boolean isCurrent = experienceSection.getUICheckBoxInput(Profile.EXPERIENCES_IS_CURRENT + id).isChecked();
    
    
    // start empty
    if ((startDate.getValue() == null || startDate.getValue().isEmpty())) {
      // current is checked or end date not empty
      if ((isCurrent || (endDate.getValue() != null && !endDate.getValue().isEmpty()))) {
        warning("EmptyFieldValidator.msg.empty-input", getLabel(Profile.EXPERIENCES_START_DATE));
        return null;
      }
    } else {
      // Invalid-format-date
      if (startDate.getCalendar() == null) {
        warning("UIEditUserProfileForm.msg.Invalid-format-date", getLabel(Profile.EXPERIENCES_START_DATE), DATE_FORMAT_MMDDYYYY);
        return null;
      }
      //
      putData(map, Profile.EXPERIENCES_START_DATE, startDate.getValue());
      // start after today
      if (startDate.getCalendar().after(Calendar.getInstance())) {
        warning("UIEditUserProfileForm.msg.TheDateBeforeToday", getLabel(Profile.EXPERIENCES_START_DATE));
        return null;
      }
      if (!isCurrent) {
        // end date empty
        if (endDate.getValue() == null || endDate.getValue().isEmpty()) {
          warning("EmptyFieldValidator.msg.empty-input", getLabel(Profile.EXPERIENCES_END_DATE));
          return null;
        }
        // Invalid-format-date
        if (startDate.getCalendar() == null) {
          warning("UIEditUserProfileForm.msg.Invalid-format-date", getLabel(Profile.EXPERIENCES_START_DATE), DATE_FORMAT_MMDDYYYY);
          return null;
        }
        // end after today
        if (endDate.getCalendar().after(Calendar.getInstance())) {
          warning("UIEditUserProfileForm.msg.TheDateBeforeToday", getLabel(Profile.EXPERIENCES_END_DATE));
          return null;
        }
        // end date before start date
        if (endDate.getCalendar().before(startDate.getCalendar())) {
          warning("UIEditUserProfileForm.msg.FromDateBeforeToDate",
                  getLabel(Profile.EXPERIENCES_START_DATE), getLabel(Profile.EXPERIENCES_END_DATE));
          return null;
        }
        putData(map, Profile.EXPERIENCES_END_DATE, endDate.getValue());
        map.put(Profile.EXPERIENCES_IS_CURRENT, "false");
      } else {
        map.put(Profile.EXPERIENCES_IS_CURRENT, "true");
      }
    }
    //
    return map;
  }

  /**
   * @return
   */
  protected String getViewProfileURL() {
    return this.currentProfile.getUrl();
  }

  public static class SaveActionListener extends EventListener<UIEditUserProfileForm> {
    @Override
    public void execute(Event<UIEditUserProfileForm> event) throws Exception {
      UIEditUserProfileForm uiForm = event.getSource();
      // About me
      String aboutMe = uiForm.getUIInputSection(FIELD_ABOUT_SECTION).getUIFormTextAreaInput(Profile.ABOUT_ME).getValue();
      // Basic information
      UIInputSection baseSection = uiForm.getUIInputSection(FIELD_BASE_SECTION);
      String firstName = baseSection.getUIStringInput(Profile.FIRST_NAME).getValue();
      String lastName = baseSection.getUIStringInput(Profile.LAST_NAME).getValue();
      String email = baseSection.getUIStringInput(Profile.EMAIL).getValue();
      String position =  baseSection.getUIStringInput(Profile.POSITION).getValue();
      //
      String gender = baseSection.getUIFormSelectBox(Profile.GENDER).getValue();
      //
      List<Map<String, String>> phones = baseSection.getUIMultiValueSelection(Profile.CONTACT_PHONES).getValues();
      //
      List<Map<String, String>> ims = baseSection.getUIMultiValueSelection(Profile.CONTACT_IMS).getValues();
      //
      List<Map<String, String>> mapUrls = new ArrayList<Map<String,String>>();
      List<?> urls = baseSection.getUIFormMultiValueInputSet(Profile.CONTACT_URLS).getValue();
      for (Object url : urls) {
        Map<String, String> mUrl = new HashMap<String, String>();
        mUrl.put(UserProfileHelper.KEY, UserProfileHelper.URL_KEY);
        mUrl.put(UserProfileHelper.VALUE, (String) url);
        mapUrls.add(mUrl);
      }
      //Experiences
      List<Map<String, String>> experiences = new ArrayList<Map<String, String>>();
      List<UIInputSection> experienceSections = uiForm.getExperienceSections();
      for (UIInputSection experienSection : experienceSections) {
        Map<String, String> map = uiForm.getValueExperience(experienSection);
        if (map == null) {
          return;
        }
        if (map.size() > 0) {
          experiences.add(map);
        }
      }
      Profile profile =  uiForm.currentProfile;
      //
      profile.setProperty(Profile.ABOUT_ME, aboutMe);
      profile.setProperty(Profile.FIRST_NAME, firstName);
      profile.setProperty(Profile.LAST_NAME, lastName);
      profile.setProperty(Profile.EMAIL, email);
      profile.setProperty(Profile.POSITION, position);
      profile.setProperty(Profile.GENDER, gender);
      //
      profile.setProperty(Profile.CONTACT_PHONES, phones);
      profile.setProperty(Profile.CONTACT_IMS, ims);
      profile.setProperty(Profile.CONTACT_URLS, mapUrls);
      //
      profile.setProperty(Profile.EXPERIENCES, experiences);
      //
      Utils.getIdentityManager().updateProfile(profile);
      //
      uiForm.resetForm();
      //
      event.getRequestContext().getJavascriptManager().getRequireJS()
           .addScripts("setTimeout(function() {window.open(window.location.origin + '" +
            profile.getUrl() + "', '_self')}, 200);");
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
    }
  }

  public static class CancelActionListener extends EventListener<UIEditUserProfileForm> {
    @Override
    public void execute(Event<UIEditUserProfileForm> event) throws Exception {
      UIEditUserProfileForm editUserProfile = event.getSource();
      //
      editUserProfile.resetForm();
      //
      ((PortalRequestContext) event.getRequestContext().getParentAppRequestContext()).ignoreAJAXUpdateOnPortlets(true);
    }
  }

  public static class AddExperienceActionListener extends EventListener<UIEditUserProfileForm> {
    @Override
    public void execute(Event<UIEditUserProfileForm> event) throws Exception {
      UIEditUserProfileForm editUserProfile = event.getSource();
      String objectId = event.getRequestContext().getRequestParameter(OBJECTID);
      if (objectId != null && !objectId.startsWith(FIELD_EXPERIENCE_SECTION)) {
        return;
      }
      if (objectId.contains("empty")) {
        editUserProfile.removeChildById(FIELD_EMPTY_EXPERIENCE_SECTION);
      }
      //
      editUserProfile.getOrCreateExperienceSection(FIELD_EXPERIENCE_SECTION + editUserProfile.index);
      //
      editUserProfile.resetActionFileds();
      RequireJS requireJs = event.getRequestContext().getJavascriptManager().getRequireJS();
      requireJs.require("SHARED/user-profile", "profile")
               .addScripts("profile.chechboxUtil('" + (FIELD_EXPERIENCE_SECTION + editUserProfile.index) + "');");
      event.getRequestContext().addUIComponentToUpdateByAjax(editUserProfile);
    }
  }

  public static class RemoveExperienceActionListener extends EventListener<UIEditUserProfileForm> {
    @Override
    public void execute(Event<UIEditUserProfileForm> event) throws Exception {
      UIEditUserProfileForm editUserProfile = event.getSource();
      String objectId = event.getRequestContext().getRequestParameter(OBJECTID);
      editUserProfile.removeChildById(objectId);
      editUserProfile.experiens.remove(objectId);
      List<UIInputSection> experienceSections = editUserProfile.getExperienceSections();
      if(experienceSections.size() > 0) {
        experienceSections.get(0).setTitle("Experience");
      } else {
        editUserProfile.addEmpryExperienceSection();
      }
      //
      editUserProfile.resetActionFileds();
      event.getRequestContext().addUIComponentToUpdateByAjax(editUserProfile);
    }
  }
}
