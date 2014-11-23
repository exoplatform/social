package org.exoplatform.social.user.form;

import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.webui.UIAvatarUploader;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.social.webui.composer.PopupContainer;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
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
     @EventConfig(listeners = UIEditUserProfileForm.SaveActionListener.class, phase=Phase.DECODE)
   }
)
public class UIEditUserProfileForm extends UIForm {
  public static final String FIELD_FIRST_NAME_INPUT              = "firstName";
  private Profile viewProfile;
  
  public UIEditUserProfileForm() throws Exception {
    addUIFormInput(new UIFormTextAreaInput(Profile.ABOUT_ME, Profile.ABOUT_ME, null));
    addUIFormInput(new UIFormStringInput(Profile.FIRST_NAME, Profile.FIRST_NAME, null)
      .addValidator(MandatoryValidator.class).addValidator(PersonalNameValidator.class).addValidator(StringLengthValidator.class, 1, 45));
    addUIFormInput(new UIFormStringInput(Profile.LAST_NAME, Profile.LAST_NAME, null)
      .addValidator(MandatoryValidator.class).addValidator(PersonalNameValidator.class).addValidator(StringLengthValidator.class, 1, 45));
    addUIFormInput(new UIFormStringInput(Profile.EMAIL, Profile.EMAIL, null)
      .addValidator(MandatoryValidator.class).addValidator(EmailAddressValidator.class));
    //
    UIChangeAvatarContainer avatarContainer = createUIComponent(UIChangeAvatarContainer.class, null, "Avatar");
    addUIFormInput(avatarContainer);
    
    
    setActions(new String[]{"Save", "Cancel"});
  }
  
  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
    this.viewProfile = Utils.getViewerIdentity(true).getProfile();
    super.processRender(context);
  }
  
  protected String getViewProfileURL() {
    return this.viewProfile.getUrl();
  }

  public static class SaveActionListener extends EventListener<UIEditUserProfileForm> {
    @Override
    public void execute(Event<UIEditUserProfileForm> event) throws Exception {
      UIEditUserProfileForm avatarContainer = event.getSource();
      PopupContainer uiPopup = avatarContainer.getAncestorOfType(UIPortletApplication.class).getChild(PopupContainer.class);
      uiPopup.activate(UIAvatarUploader.class, 500, "UIWindowAvatarUploader");
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup);
    }
  }
  
  
}
