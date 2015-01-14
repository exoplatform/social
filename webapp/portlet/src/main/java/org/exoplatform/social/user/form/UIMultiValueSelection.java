package org.exoplatform.social.user.form;

import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

@ComponentConfig(
    lifecycle = UIContainerLifecycle.class,
    events = { 
        @EventConfig(listeners = UIMultiValueSelection.AddValueActionListener.class, phase = Phase.DECODE), 
        @EventConfig(listeners = UIMultiValueSelection.RemoveValueActionListener.class, phase = Phase.DECODE) 
    }
)
public class UIMultiValueSelection extends UIFormInputSet {
  public static final String FIELD_SELECT_KEY = "selectKey_";
  public static final String FIELD_INPUT_KEY = "inputKey_";
  private List<Map<String, String>> values;
  private List<Integer> indexs = new LinkedList<Integer>();
  private List<SelectItemOption<String>> options;

  public UIMultiValueSelection() {
  }

  public UIMultiValueSelection(String name) {
    super(name);
    this.options = new ArrayList<SelectItemOption<String>>();
  }

  public UIMultiValueSelection(String name, String uiFormId, List<String> options) {
    super(name);
    setComponentConfig(getClass(), null);
    //
    this.options = new ArrayList<SelectItemOption<String>>();
    if (options != null && options.size() > 0) {
      for (String option : options) {
        this.options.add(new SelectItemOption<String>(UserProfileHelper.getLabel(null, uiFormId + ".label." + option), option));
      }
    }
  }
  
  private String getSelected(String uiFormId, String key) {
    for (SelectItemOption<String> selectItemOption : options) {
      if (selectItemOption.getValue().equals(key)) {
        return key;
      }
    }
    options.add(new SelectItemOption<String>(UserProfileHelper.getLabel(null, uiFormId + ".label." + key), key));
    return "";
  }
  
  public UIMultiValueSelection setValues(List<Map<String, String>> values) {
    this.values = values;
    String uiFormId = getAncestorOfType(UIForm.class).getId();
    //
    try {
      //
      removeChildren();
      //
      int index = 0;
      if (values != null && !values.isEmpty()) {
        for (Map<String, String> map : values) {
          String key = map.get("key");
          String value = map.get("value");
          //
          addInput(index, getSelected(uiFormId, key), value);
          //
          ++index;
        }
      } else {
        addInput(index, "", "");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return this;
  }

  private String getInputValue(int indexId) {
    return getUIStringInput(FIELD_INPUT_KEY + indexId).getValue();
  }

  private String getInputKey(int indexId) {
    return getUIFormSelectBox(FIELD_SELECT_KEY + indexId).getValue();
  }

  public List<Map<String, String>> getValues() {
    this.values = new ArrayList<Map<String, String>>();
    for (Integer indexId : indexs) {
      String value = getInputValue(indexId);
      if (value != null && !value.isEmpty()) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("key", getInputKey(indexId));
        map.put("value", value);
        values.add(map);
      }
    }
    return this.values;
  }

  private void addInput(int indexId, String selected, String value) {
    int index = indexId;
    if (indexs.contains(Integer.valueOf(indexId))) {
      index = indexs.indexOf(indexId) + 1;
      indexId = index * 10;
    }
    ((LinkedList<Integer>) indexs).add(index, indexId);
    //
    UIFormSelectBox selectBox = new UIFormSelectBox(FIELD_SELECT_KEY + indexId, FIELD_SELECT_KEY + indexId, options).setValue(selected);
    selectBox.setHTMLAttribute("class", "span2");
    addUIFormInput(selectBox);
    //
    UIFormStringInput stringInput = new UIFormStringInput(FIELD_INPUT_KEY + indexId, FIELD_INPUT_KEY + indexId, value);
    stringInput.setHTMLAttribute("class", "span3");
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
    Writer w = context.getWriter();
    w.append("<div class=\"uiMulti-select\" id=\"").append(getId()).append("\">") ;
    int i = 0;
    for (Integer indexId : indexs) {
      w.append("<div class=\"controls-row\">") ;
      renderUIComponent(getUIFormSelectBox(FIELD_SELECT_KEY + indexId));
      renderUIComponent(getUIStringInput(FIELD_INPUT_KEY + indexId));
      if(indexs.size() > 1) {
        // remove
        w.append("<a class=\"actionIcon\" href=\"javascript:void(0)\" onclick=\"").append(uiForm.event("RemoveValue", getId() + String.valueOf(indexId)))
         .append("\"><i class=\"uiIconClose uiIconLightGray\"></i></a>");
      }
      if(i == indexs.size() - 1) {
        // add
        w.append("<a class=\"actionIcon\" href=\"javascript:void(0)\" onclick=\"").append(uiForm.event("AddValue", getId() + String.valueOf(indexId)))
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
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSelection.getParent());
    }
  }

  static public class RemoveValueActionListener extends EventListener<UIMultiValueSelection> {
    public void execute(Event<UIMultiValueSelection> event) throws Exception {
      UIMultiValueSelection uiSelection = event.getSource();
      String indexId = event.getRequestContext().getRequestParameter(OBJECTID);
      indexId = indexId.replace(uiSelection.getId(), "");
      if(uiSelection.indexs.contains(Integer.valueOf(indexId))) {
        uiSelection.removeChildById(FIELD_SELECT_KEY + indexId);
        uiSelection.removeChildById(FIELD_INPUT_KEY + indexId);
        uiSelection.indexs.remove(Integer.valueOf(indexId));
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSelection);
    }
  }

}
