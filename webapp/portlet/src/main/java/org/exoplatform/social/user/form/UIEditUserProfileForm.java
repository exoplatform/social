package org.exoplatform.social.user.form;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.user.form.UIInputSection.ActionData;
import org.exoplatform.social.user.portlet.UserProfileHelper;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormDateTimeInput;
import org.exoplatform.webui.form.UIFormInputBase;
import org.exoplatform.webui.form.UIFormMultiValueInputSet;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.input.UICheckBoxInput;
import org.exoplatform.webui.form.validator.EmailAddressValidator;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.PersonalNameValidator;
import org.exoplatform.webui.form.validator.StringLengthValidator;

@ComponentConfig(
   lifecycle = UIFormLifecycle.class,
   template = "app:/groovy/social/portlet/user/UIEditUserProfileForm.gtmpl",
   events = {
     @EventConfig(listeners = UIEditUserProfileForm.AddExperienceActionListener.class, phase = Phase.DECODE),
     @EventConfig(listeners = UIEditUserProfileForm.RemoveExperienceActionListener.class, phase = Phase.DECODE),
     @EventConfig(listeners = UIEditUserProfileForm.SaveActionListener.class)
   }
)
public class UIEditUserProfileForm extends UIForm {
  public static final String PLACEHOLDER_KEY = "placeholder";

  public static final String FIELD_ABOUT_SECTION = "AboutSection";
  public static final String FIELD_BASE_SECTION = "BaseSection";
  public static final String FIELD_EXPERIENCE_SECTION = "ExperienceSection";
  public static final String OPTION_MALE = "male";
  public static final String OPTION_FEMALE = "female";
  public static final String DATE_FORMAT_MMDDYYYY = "MM/dd/yyyy";
  /** PHONE_TYPES. */
  public static final String[] PHONE_TYPES = new String[] {"work","home","other"};
  /** IM_TYPES. */
  public static final String[] IM_TYPES = new String[] {"gtalk","msn","skype","yahoo","other"};
  private Profile currentProfile;
  private int index = 0;
  private int experienSize = 0;
  
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
    baseSection.addUIFormInput(phoneSelection);
    //
    UIMultiValueSelection imsSelection = new UIMultiValueSelection(Profile.CONTACT_IMS, getId(), Arrays.asList(IM_TYPES));
    baseSection.addUIFormInput(imsSelection);
    //
    UIFormMultiValueInputSet urlMultiValueInput = new UIFormMultiValueInputSet(Profile.CONTACT_URLS, Profile.CONTACT_URLS);
    urlMultiValueInput.setType(UIFormStringInput.class);
    urlMultiValueInput.setValue(Arrays.asList(""));
    urlMultiValueInput.setLabel(Profile.CONTACT_URLS);
    baseSection.addUIFormInput(urlMultiValueInput);
    //
    addUIFormInput(aboutSection);
    addUIFormInput(baseSection);
    getOrCreateExperienceSection(FIELD_EXPERIENCE_SECTION + index);
  }
  
  @Override
  public String getLabel(ResourceBundle res, String id) {
    String label = getId() + ".label." + id;
    try {
      return res.getString(label);
    } catch (MissingResourceException e) {
      System.out.println("Missing: " + label);
      return id;
    }
  }

  private UIInputSection getOrCreateExperienceSection(String id) throws Exception {
    UIInputSection experienceSection = getChildById(id);
    if(experienceSection != null) {
      return experienceSection;
    }
    String label = (experienSize == 0) ? "Experience" : "";
    experienceSection = new UIInputSection(id, label);
    //
    List<ActionData> actions = new ArrayList<UIInputSection.ActionData>();
    if(experienSize > 0) {
      ActionData removeAction = new ActionData();
      removeAction.setAction("RemoveExperience")
                  .setIcon("uiIconClose")
                  .setTooltip("Remove this experience")
                  .setObjectId(id);
      actions.add(removeAction);
    }
    ActionData addAction = new ActionData();
    addAction.setAction("AddExperience")
             .setTooltip("Add more experience")
             .setIcon("uiIconPlus")
             .setObjectId(id);
    actions.add(addAction);
    //
    UIFormStringInput company = createUIFormStringInput(Profile.EXPERIENCES_COMPANY + id, false);
    company.setLabel(Profile.EXPERIENCES_COMPANY);
    experienceSection.addUIFormInput(company , actions);
    //
    experienceSection.addUIFormInput(createUIFormStringInput(Profile.EXPERIENCES_POSITION + id, false), Profile.EXPERIENCES_POSITION);
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
    ++experienSize;
    ++index;
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

  protected void setValueBasicInfo() {
    //about me
    getUIInputSection(FIELD_ABOUT_SECTION).getUIFormTextAreaInput(Profile.ABOUT_ME)
                                          .setValue(getStringValueProfile(Profile.ABOUT_ME));
    //
    UIInputSection baseSection = getUIInputSection(FIELD_BASE_SECTION);
    baseSection.getUIStringInput(Profile.FIRST_NAME).setValue(getStringValueProfile(Profile.FIRST_NAME));
    baseSection.getUIStringInput(Profile.LAST_NAME).setValue(getStringValueProfile(Profile.LAST_NAME));
    baseSection.getUIStringInput(Profile.POSITION).setValue(getStringValueProfile(Profile.POSITION));
    //
    baseSection.getUIFormSelectBox(Profile.GENDER).setValue(getStringValueProfile(Profile.GENDER));
    //
    List<Map<String, String>> phones = UserProfileHelper.getMultiValues(currentProfile, Profile.CONTACT_PHONES);
    baseSection.getUIMultiValueSelection(Profile.CONTACT_PHONES).setValues(phones);
    //
    List<Map<String, String>> ims = UserProfileHelper.getMultiValues(currentProfile, Profile.CONTACT_IMS);
    baseSection.getUIMultiValueSelection(Profile.CONTACT_IMS).setValues(ims);
    
  }
  
  protected Calendar stringToCalendar(String sDate) {
    try {
      SimpleDateFormat sd = new SimpleDateFormat(DATE_FORMAT_MMDDYYYY, Locale.ENGLISH);
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(sd.parse(sDate));
      return calendar;
    } catch (ParseException e) {
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
        ((UIFormInputBase<?>)uiComponent).setHTMLAttribute(PLACEHOLDER_KEY, "Input your urls (http://sampleurl.com)");
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
  
  private UIInputSection getUIInputSection(String id) {
    return (UIInputSection) getChildById(id);
  }

  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
    this.currentProfile = Utils.getViewerIdentity(true).getProfile();
    this.initPlaceholder();
    super.processRender(context);
  }
  
  protected String getViewProfileURL() {
    return this.currentProfile.getUrl();
  }

  public static class SaveActionListener extends EventListener<UIEditUserProfileForm> {
    @Override
    public void execute(Event<UIEditUserProfileForm> event) throws Exception {
      UIEditUserProfileForm avatarContainer = event.getSource();
      //
      event.getRequestContext().addUIComponentToUpdateByAjax(avatarContainer);
    }
  }

  public static class AddExperienceActionListener extends EventListener<UIEditUserProfileForm> {
    @Override
    public void execute(Event<UIEditUserProfileForm> event) throws Exception {
      UIEditUserProfileForm editUserProfile = event.getSource();
      //
      editUserProfile.getOrCreateExperienceSection(FIELD_EXPERIENCE_SECTION + editUserProfile.index);
      //
      event.getRequestContext().addUIComponentToUpdateByAjax(editUserProfile);
    }
  }

  public static class RemoveExperienceActionListener extends EventListener<UIEditUserProfileForm> {
    @Override
    public void execute(Event<UIEditUserProfileForm> event) throws Exception {
      UIEditUserProfileForm editUserProfile = event.getSource();
      String objectId = event.getRequestContext().getRequestParameter(OBJECTID);
      editUserProfile.removeChildById(objectId);
      --editUserProfile.experienSize;
      //
      event.getRequestContext().addUIComponentToUpdateByAjax(editUserProfile);
    }
  }
}
