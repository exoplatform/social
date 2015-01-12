package org.exoplatform.social.user.form;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputBase;
import org.exoplatform.webui.form.UIFormMultiValueInputSet;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.validator.EmailAddressValidator;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.PersonalNameValidator;
import org.exoplatform.webui.form.validator.StringLengthValidator;

@ComponentConfig(
   lifecycle = UIFormLifecycle.class,
   template = "app:/groovy/social/portlet/user/UIEditUserProfileForm.gtmpl",
   events = {
     @EventConfig(listeners = UIEditUserProfileForm.AddURLActionListener.class),
     @EventConfig(listeners = UIEditUserProfileForm.SaveActionListener.class)
   }
)
public class UIEditUserProfileForm extends UIForm {
  public static final String PLACEHOLDER_KEY = "placeholder";

  public static final String FIELD_ABOUT_SECTION = "AboutSection";
  public static final String FIELD_BASE_SECTION = "baseSection";
  public static final String OPTION_MALE = "male";
  public static final String OPTION_FEMALE = "female";
  //
  /** PHONE_TYPES. */
  public static final String[] PHONE_TYPES = new String[] {"work","home","other"};
  /** IM_TYPES. */
  public static final String[] IM_TYPES = new String[] {"gtalk","msn","skype","yahoo","other"};
  private Profile viewProfile;
  
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
    
    //
    addUIFormInput(aboutSection);
    addUIFormInput(baseSection);
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
  }
  
  private UIInputSection getUIInputSection(String id) {
    return (UIInputSection) getChildById(id);
  }

  private UIMultiValueSelection getUIMultiValueSelection(String id) {
    return (UIMultiValueSelection) getChildById(id);
  }
  
  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
    this.viewProfile = Utils.getViewerIdentity(true).getProfile();
    this.initPlaceholder();
    super.processRender(context);
  }
  
  protected String getViewProfileURL() {
    return this.viewProfile.getUrl();
  }

  public static class SaveActionListener extends EventListener<UIEditUserProfileForm> {
    @Override
    public void execute(Event<UIEditUserProfileForm> event) throws Exception {
      UIEditUserProfileForm avatarContainer = event.getSource();
      //
      event.getRequestContext().addUIComponentToUpdateByAjax(avatarContainer);
    }
  }

  public static class AddURLActionListener extends EventListener<UIEditUserProfileForm> {
    @Override
    public void execute(Event<UIEditUserProfileForm> event) throws Exception {
      UIEditUserProfileForm avatarContainer = event.getSource();
      //
      event.getRequestContext().addUIComponentToUpdateByAjax(avatarContainer);
    }
  }
  
  
}
