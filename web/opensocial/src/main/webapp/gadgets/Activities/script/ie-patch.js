/**
 * ie-patch.js
 * patch file for IE
 * @author  <a href="mailto:hoatlevan@gmail.com">hoatle</a>
 * @since   Oct 12, 2009
 * @copyright   eXo Platform SEA
 */

//Array.lastIndexOf()
if (!Array.prototype.lastIndexOf) {
  Array.prototype.lastIndexOf = function(elt /*, from*/)  {
    var len = this.length;
    var from = Number(arguments[1]);
    if (isNaN(from)) {
      from = len - 1;
    }
    else {
      from = (from < 0)
           ? Math.ceil(from)
           : Math.floor(from);
      if (from < 0) {
        from += len;
      } else if (from >= len) {
        from = len - 1;
      }
    }

    for (; from > -1; from--) {
      if (from in this &&
          this[from] === elt)
        return from;
    }
    return -1;
  };
}

//Array.indexOf()
if (!Array.prototype.indexOf) {
  Array.prototype.indexOf = function(obj){
    for(var i = 0, l = this.length; i < l; i++){
      if(this[i] === obj){
        return i;
      }
    }
    return -1;
  }
}