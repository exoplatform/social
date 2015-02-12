package org.exoplatform.social.user.form;

import java.io.Writer;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.user.portlet.UserProfileHelper;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.SpecialCharacterValidator;
import org.exoplatform.webui.form.validator.Validator;

@ComponentConfig(
    lifecycle = UIContainerLifecycle.class,
    events = { 
        @EventConfig(listeners = UIMultiValueSelection.AddValueActionListener.class, phase = Phase.DECODE), 
        @EventConfig(listeners = UIMultiValueSelection.RemoveValueActionListener.class, phase = Phase.DECODE) 
    }
)
public class UIMultiValueSelection extends UIFormInputSet {
  private static final Log LOG = ExoLogger.getExoLogger(UIMultiValueSelection.class);
  
  protected List<Validator> validators;
  public static final String FIELD_SELECT_KEY = "selectKey_";
  public static final String FIELD_INPUT_KEY = "inputKey_";
  private List<Map<String, String>> values;
  private List<Integer> indexs = new LinkedList<Integer>();
  private List<String> optionValues = new ArrayList<String>();

  public UIMultiValueSelection() {
  }

  public UIMultiValueSelection(String name) {
    super(name);
  }

  public UIMultiValueSelection(String name, String uiFormId, List<String> options) {
    super(name);
    setComponentConfig(getClass(), null);
    //
    this.optionValues = options;
  }

  public <E extends Validator> UIMultiValueSelection addValidator(Class<E> clazz, Object... params) throws Exception {
    if (validators == null)
      validators = new ArrayList<Validator>(3);
    if (params.length > 0) {
      Class<?>[] classes = new Class[params.length];
      for (int i = 0; i < params.length; i++) {
        classes[i] = params[i].getClass();
      }
      Constructor<E> constructor = clazz.getConstructor(classes);
      validators.add(constructor.newInstance(params));
      return this;
    }
    validators.add(clazz.newInstance());
    return this;
  }

  public List<Validator> getValidators() {
    return validators;
  }

  private List<SelectItemOption<String>> getOptions() {
    String uiFormId = getAncestorOfType(UIForm.class).getId();
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>();
    if (optionValues != null) {
      for (String option : optionValues) {
        options.add(new SelectItemOption<String>(UserProfileHelper.getLabel(null, uiFormId + ".label." + option), option));
      }
    }
    return options;
  }

  private String getSelected(String key) {
    if (optionValues == null) {
      optionValues = new ArrayList<String>();
    }
    if (optionValues.contains(key)) {
      return key;
    }
    optionValues.add(key);
    //
    return "";
  }
  
  public UIMultiValueSelection setValues(List<Map<String, String>> values) {
    this.values = values;
    //
    try {
      //
      removeChildren();
      //
      int index = 0;
      if (values != null && !values.isEmpty()) {
        for (Map<String, String> map : values) {
          String key = map.get("key");
          String value = UserProfileHelper.decodeHTML(map.get("value"));
          //
          addInput(index, getSelected(key), value);
          //
          ++index;
        }
      } else {
        addInput(index, "", "");
      }
    } catch (Exception e) {
      LOG.warn("Failed to set values for " + getId(), e);
    }
    return this;
  }

  private String getInputValue(int indexId) {
    return getUIStringInput(getInputId(indexId)).getValue();
  }

  private String getInputKey(int indexId) {
    return getUIFormSelectBox(getSelectId(indexId)).getValue();
  }

  public List<Map<String, String>> getValues() {
    this.values = new ArrayList<Map<String, String>>();
    for (Integer indexId : indexs) {
      String value = getInputValue(indexId);
      if (value != null && !value.isEmpty()) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("key", getInputKey(indexId));
        map.put("value", UserProfileHelper.encodeHTML(value));
        values.add(map);
      }
    }
    return this.values;
  }

  private String getSelectId(int index) {
    return new StringBuilder(FIELD_SELECT_KEY).append(getId()).append(index).toString();
  }

  private String getInputId(int index) {
    return new StringBuilder(FIELD_INPUT_KEY).append(getId()).append(index).toString();
  }

  private void addInput(int indexId, String selected, String value) throws Exception {
    //
    int index = indexId;
    if (indexs.contains(Integer.valueOf(indexId))) {
      index = indexs.indexOf(indexId) + 1;
      indexId = index * 10;
    }
    ((LinkedList<Integer>) indexs).add(index, indexId);
    //
    addUIFormInput(new UIFormSelectBox(getSelectId(indexId), getSelectId(indexId), getOptions()).setValue(selected));
    //
    UIFormStringInput stringInput = new UIFormStringInput(getInputId(indexId), getInputId(indexId), value);
    stringInput.setHTMLAttribute("class", "selectInput");
    if (validators != null && !validators.isEmpty()) {
      stringInput.addValidator(SpecialCharacterValidator.class);
      List<Validator> validators_ = stringInput.getValidators();
      validators_.clear();
      validators_.addAll(validators);
    }
    addUIFormInput(stringInput);
  }

  private void removeChildren() {
    getChildren().clear();
    indexs.clear();
  }

  public void processDecode(WebuiRequestContext context) throws Exception {
    super.processDecode(context);
    UIForm uiForm = getAncestorOfType(UIForm.class);
    //
    String indexId = context.getRequestParameter(OBJECTID);
    if (indexId == null || !indexId.contains(getId())) {
      return;
    }
    String action = uiForm.getSubmitAction();
    Event<UIComponent> event = createEvent(action, Event.Phase.DECODE, context);
    if (event == null)
      return;
    event.broadcast();
  }
  
  public void processRender(WebuiRequestContext context) throws Exception {
    if (indexs.isEmpty()) {
      setValues(null);
    }
    UIForm uiForm = getAncestorOfType(UIForm.class);
    String addItem = UserProfileHelper.getLabel(context, "UIFormMultiValueInputSet.label.add");
    String removeItem = UserProfileHelper.getLabel(context, "UIFormMultiValueInputSet.label.remove");
    context.getJavascriptManager().require("SHARED/jquery", "jq").require("SHARED/bts_tooltip")
           .addScripts("jq('.uiMulti-select').find('a[rel=\"tooltip\"]').tooltip();");
    //
    Writer w = context.getWriter();
    w.append("<div class=\"uiMulti-select\" id=\"").append(getId()).append("\">") ;
    int i = 0;
    for (Integer indexId : indexs) {
      w.append("<div class=\"controls-row\">") ;
      renderUIComponent(getUIFormSelectBox(getSelectId(indexId)));
      renderUIComponent(getUIStringInput(getInputId(indexId)));
      if(indexs.size() > 1) {
        // remove
        w.append("<a class=\"actionIcon\" data-placement=\"bottom\" rel=\"tooltip\" title=\"\" data-original-title=\"").append(removeItem)
         .append("\" href=\"javascript:void(0)\" onclick=\"").append(uiForm.event("RemoveValue", getId() + String.valueOf(indexId)))
         .append("\"><i class=\"uiIconClose uiIconLightGray\"></i></a>");
      }
      if(i == indexs.size() - 1) {
        // add
        w.append("<a class=\"actionIcon\" data-placement=\"bottom\" rel=\"tooltip\" title=\"\" data-original-title=\"").append(addItem)
         .append("\" href=\"javascript:void(0)\" onclick=\"").append(uiForm.event("AddValue", getId() + String.valueOf(indexId)))
         .append("\"><i class=\"uiIconPlus uiIconLightGray\"></i></a>");
      }
      w.append("</div>");
      ++i;
    }
    if (this.isMandatory())
        w.write(" *");
    w.write("</div>");
  }

  private boolean isMandatory() {
    return false;
  }

  static public class AddValueActionListener extends EventListener<UIMultiValueSelection> {
    public void execute(Event<UIMultiValueSelection> event) throws Exception {
      UIMultiValueSelection uiSelection = event.getSource();
      String indexId = event.getRequestContext().getRequestParameter(OBJECTID);
      indexId = indexId.replace(uiSelection.getId(), "");
      uiSelection.addInput(Integer.valueOf(indexId.trim()), "", "");
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSelection);
    }
  }

  static public class RemoveValueActionListener extends EventListener<UIMultiValueSelection> {
    public void execute(Event<UIMultiValueSelection> event) throws Exception {
      UIMultiValueSelection uiSelection = event.getSource();
      String indexId = event.getRequestContext().getRequestParameter(OBJECTID);
      indexId = indexId.replace(uiSelection.getId(), "");
      if(uiSelection.indexs.contains(Integer.valueOf(indexId))) {
        uiSelection.removeChildById(uiSelection.getSelectId(Integer.valueOf(indexId)));
        uiSelection.removeChildById(uiSelection.getInputId(Integer.valueOf(indexId)));
        uiSelection.indexs.remove(Integer.valueOf(indexId));
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSelection);
    }
  }

}
