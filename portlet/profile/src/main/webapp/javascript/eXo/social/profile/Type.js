var eXo = window.eXo;
eXo.social = eXo.social || {};
eXo.social.profile = eXo.social.profile || {};


function Type() {

}

Type.prototype.isArray = function(o) {
  if (o) {
    return this.isNumber(o.length) && this.isFunction(o.splice);
  }
  return false;
}

Type.prototype.isBoolean = function(o) {
  return typeof o === 'boolean';
}

Type.prototype.isFunction = function(o) {
  return typeof o === 'function';
}

Type.prototype.isNull = function(o) {
  return o === null;
}

Type.prototype.isNumber = function(o) {
  return typeof o === 'number' && isFinite(o);
}

Type.prototype.isObject = function(o) {
  return (o && (typeof o === 'object' || L.isFunction(o))) || false;
}

Type.prototype.isString = function(o) {
  return typeof o === 'string';
}

Type.prototype.isUndefined = function(o) {
  return typeof o === 'undefined';
}


eXo.social.profile.Type = new Type();