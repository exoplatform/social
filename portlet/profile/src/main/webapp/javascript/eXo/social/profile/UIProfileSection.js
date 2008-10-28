var eXo = window.eXo;
eXo.social = eXo.social || {};
eXo.social.profile = eXo.social.profile || {};


function UIProfileSection() {
  this.propertyCnt = {};
  this.propertyInit = {};
}

UIProfileSection.prototype.getFormEl = function(/* String */ blockID) {
  return document.getElementById("form_" + blockID);
}

UIProfileSection.prototype.submitForm = function(/* String */ blockID, /* String */ action) {
  var form = this.getFormEl(blockID);

  if(!this.validate(form))
    return;

  form.elements['op'].value = action;
  this.ajaxPost(form, null);
}

UIProfileSection.prototype.ajaxPost = function(formElement, callback) {
  if (!callback) callback = null ;
  var queryString = eXo.webui.UIForm.serializeForm(formElement);
  var params = queryString.split("&");
  var newparams = [];
  for (var i = 0; i < params.length; i++) {
    if(params[i].indexOf("__ID__") == -1)
      newparams.push(params[i]);
  }
  queryString = newparams.join("&");
  var url = formElement.action + "&ajaxRequest=true" ;
  doRequest("POST", url, queryString, callback);
  this.propertyCnt = {};
}

UIProfileSection.prototype.addValidator = function(/* String */ elIdTemplate, /* eXo.social.profile.Validate */ validate, /* String */ id) {
  var elId;
  if(id) {
    elId = elIdTemplate.replace(/__ID__/g, id); 
  }
  else {
    elId = elIdTemplate;
  }


  var validator = new eXo.social.profile.LiveValidation(elId);
  validator.add(validate);

  var el = document.getElementById(elId);

  var liEl = eXo.core.DOMUtil.findAncestorByTagName(el, "li");

  liEl.validator = validator;

  this.addValidatorToList(validator);
}

UIProfileSection.prototype.removeValidatorFromList = function(el) {
  var form = eXo.core.DOMUtil.findAncestorByTagName(el, "form");

  //we use this before finding something better
  this.cleanupValidatorList(form);
}


UIProfileSection.prototype.cleanupValidatorList = function(form) {
  var validators = form.toValidate;
  if (!validators) {
    return;
  }

  for (var i = 0; i < validators.length; i++) {
    if (eXo.social.profile.Type.isNull(validators[i].insertAfterWhatNode.parentNode)) {
      console.debug("remove", validators[i]);
      form.toValidate.remove(validators[i]);
    }
  }
}

UIProfileSection.prototype.addValidatorToList = function(/* eXo.social.profile.LiveValidation */ validator) {
  var form = validator.form;
  if (!form.toValidate) {
    form.toValidate = [];
  }
  validator.wait = 1000;
  validator.onValid = function(){};

  form.toValidate.push(validator);
}

UIProfileSection.prototype.initValidator = function(/* Node */ el, /* eXo.social.profile.Validate */ validate) {

  var validator = new eXo.social.profile.LiveValidation(elId);
  validator.add(validate);

  this.addValidatorToList(validator);
}

UIProfileSection.prototype.validate = function(/* Form */ form) {
  var toValidate = form.toValidate;
  if(toValidate) {
    if(!eXo.social.profile.LiveValidation.massValidate(toValidate)) {
      alert("The form is not valid");
      return false;
    }
  }
  return true;
}


UIProfileSection.prototype.increasePropertyCounter = function(/* String */ propName) {
  this.propertyCnt[propName] = (this.propertyCnt[propName] == undefined ? 1 : this.propertyCnt[propName] + 1)
}

UIProfileSection.prototype.getPropertyCounter = function(/* String */ propName) {
  var cnt = this.propertyCnt[propName];
  return (cnt == undefined ? 0 : cnt);
}

UIProfileSection.prototype.insertProperty = function(/* String */ propName) {
  var node = document.getElementById(propName + ".template");
  var newNode = node.cloneNode(true);

  if(!node)
    return;
  var id = this.getPropertyCounter(propName);
  newNode = this.replaceId(newNode, "" + id);
  this.increasePropertyCounter(propName);

  var parent = document.getElementById(propName + ".container");  
  var addedNode = parent.appendChild(newNode.firstChild);

  var isEmptyEl = document.getElementById(propName + ".isEmpty");
  if(isEmptyEl)
    isEmptyEl.style.display = "none";

  var callback = this.propertyInit[propName];
  if(callback) {
    callback(addedNode, id);
  }
  eXo.social.profile.FancyLabel.init(addedNode);
}


 UIProfileSection.prototype.replaceId = function(/* Node */ node, /* String */ id) {
   if(node.id) {
     node.id = node.id.replace(/__ID__/g, id);
   }

   if(node.name) {
     node.name = node.name.replace(/__ID__/g, id);
   }

   if(node.htmlFor) {
     node.htmlFor = node.htmlFor.replace(/__ID__/g, id);
   }

   if(node.className) {
     node.className = node.className.replace(/__TMPL__/g, "");
   }

   var children = node.childNodes;
   for (var i = 0; i < children.length; i++) {
     this.replaceId(children[i], id);
   }
   return node;
 }

UIProfileSection.prototype.initForm = function(formName) {
  eXo.social.profile.FancyLabel.init(formName, formName);
}

UIProfileSection.prototype.addInitPropertyFunction = function(key, callback){
  this.propertyInit[key] = callback;
}

UIProfileSection.prototype.initProperty = function(propName) {
//  if(this.getPropertyCounter(propName) == 0)
//    this.insertProperty(propName);
  var parent = document.getElementById(propName + ".container");
  var el = document.createElement("input");
  el.setAttribute("name", propName + ".isEditing");
  el.setAttribute("type", "hidden");
  el.setAttribute("value", "true");
  parent.appendChild(el);
}

UIProfileSection.prototype.removeEl = function(el) {
  var form = eXo.core.DOMUtil.findAncestorByTagName(el, "form");

  var delEl = eXo.core.DOMUtil.findAncestorByTagName(el, "li");
  if(delEl && delEl.validator) {
    form.toValidate.remove(delEl.validator);  
  }
  if(delEl) {
    var parent = delEl.parentNode;
    parent.removeChild(delEl);
    var lis = parent.getElementsByTagName("li");
    if(lis.length == 1) {
      var isEmptyEl = lis[0];

      if(isEmptyEl.id.indexOf(".isEmpty") > 0)
        isEmptyEl.style.display = "list-item"; 
    }
  }
}

UIProfileSection.prototype.toggleEndDate = function(ev) {
  if(ev.target.value === "true")
    eXo.core.DOMUtil.findFirstChildByClass(ev.target.parentNode, "div", "endDate").style.display = "none";
  else
    eXo.core.DOMUtil.findFirstChildByClass(ev.target.parentNode, "div", "endDate").style.display = "block";
}

eXo.social.profile.UIProfileSection = new UIProfileSection();
