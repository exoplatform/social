var eXo = window.eXo;
eXo.social = eXo.social || {};
eXo.social.profile = eXo.social.profile || {};


function FancyLabel() {
}

FancyLabel.prototype.init = function(el, form) {

  if(eXo.social.profile.Type.isString(el)) {
    el = document.getElementById(el);
  }

  var lbls = eXo.core.DOMUtil.findDescendantsByClass(el, 'span', 'label');

  lbls.each(this.initLabel, this);

  if (!form) var form = el.getElementsByTagName('form')[0];
  eXo.social.profile.Event.addListener(form, 'submit',
      function() {
        this.wipeLabels(el);
      });
}

FancyLabel.prototype.initLabel = function(span) {
  span.style.display = 'none';
  var lbl = span.getElementsByTagName('label')[0];
  var input = document.getElementById(lbl.htmlFor);
  input._label = lbl.firstChild.nodeValue;
  eXo.social.profile.Event.addListener(input, "focus", this.focusedLabel);
  eXo.social.profile.Event.addListener(input, "blur", this.blurredLabel);
  if (input.value == '') this.showLabel(input);
}

FancyLabel.prototype.wipeLabels = function(div) {
  var allInputs = eXo.core.DOMUtil.getElementsBy(function(el) {
    return (el.nodeName == "INPUT" && el.type == "text") || el.nodeName == "TEXTAREA";
  },
      '*', div)
  for (var i = 0; i < allInputs.length; i++) {
    if (allInputs[i].value == allInputs[i]._label) {
      this.hideLabel(allInputs[i]);
    }
  }
}

FancyLabel.prototype.focusedLabel = function(e) {
  var input = window.event ? window.event.srcElement : e ? e.target : null;
  if (input.value == input._label) eXo.social.profile.FancyLabel.hideLabel(input);
}

FancyLabel.prototype.blurredLabel = function(e) {
  var input = window.event ? window.event.srcElement : e ? e.target : null;
  if (input.value == '') eXo.social.profile.FancyLabel.showLabel(input);
}

FancyLabel.prototype.hideLabel = function(el) {
  el.value = '';
  eXo.core.DOMUtil.replaceClass(el, 'hint', '');
}

FancyLabel.prototype.showLabel = function(el) {
  el.value = el._label;
  eXo.core.DOMUtil.addClass(el, 'hint');
}


eXo.social.profile.FancyLabel = new FancyLabel();