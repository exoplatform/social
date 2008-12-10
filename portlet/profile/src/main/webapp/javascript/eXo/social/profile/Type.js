/*
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