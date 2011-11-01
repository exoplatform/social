/**
 * Setting class to store settings of a user's preference.
 * This must support the lazy loading mechanism.
 *
 * How to use:
 *
 * var Setting = eXo.social.Setting;
 * var viewType = Setting.getViewType();
 * Setting.viewType(Setting.VIEW_TYPE.ICON_LIST);
 * Setting.save();
 * viewType = Setting.getViewType();
 * 
 * @since 1.2.4
 */

(function() {
  var window_ = this;

  /**
   * Class definition
   */
  function Setting() {
  }

  /**
   * Enum settings for viewType
   */

  Setting.VIEW_TYPE = {
    TEXT_LIST: "TEXT_LIST",
    ICON_LIST: "ICON_LIST"
  };


  /**
   * Enum settings for updateTimeInterval
   */
  Setting.UPDATE_TIME_INTERVAL = {
    EVERY_5_MINS: "EVERY_5_MINS",
    EVERY_10_MINS: "EVERY_10_MINS",
    EVERY_30_MINS: "EVERY_30_MINS",
    NO_UPDATE: "NO_UPDATE"
  };

  /**
   * Enum settings for itemSortedBy
   */
  Setting.ITEM_SORTED_BY = {
    RANDOM: "RANDOM",
    A_Z: "A_Z",
    Z_A: "Z_A",
    ACTIVITY_UPDATE: "ACTIVITY_UPDATE"
  };

  Setting.getViewType = function() {
  	
  };

  Setting.setViewType = function(viewType) {
  	
  };

  Setting.getUpdateTimeInterval = function() {
  	
  };

  Setting.setUpdateTimeInterval = function(updateTimeInterval) {
  	
  };

  Setting.getItemSortedBy = function() {
  	
  };

  Setting.setItemSortedBy = function(itemSortedBy) {
  	
  };

  /**
   * Store settings
   */
  Setting.save = function() {
  	
  };

  window_.exo = window_.exo || {};
  window_.exo.social = window_.exo.social || {};
  window_.exo.social.Setting = Setting;
})();
