/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Affero General Public License
* as published by the Free Software Foundation; either version 3
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.commons.api.notification.user;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserNotificationSetting {
  private String[] activeProviders;

  /**
   * Setting of value frequency to send notification
   * If value = 0 it is Immediately
   * If value = 1d it is Daily
   * If value = 1w it is Weekly, in which the number before is the day number of week
   * If value = 1m it is Monthly, in which the number before is the day number of month
   */
  private String   frequency      = "0";  // 1d; 1w 2w 3w; 1m 2m 3m;

  private boolean  isImmediately = true;

  private boolean  isDaily       = false;

  private boolean  isWeekly      = false;

  private boolean  isMonthly     = false;

  public UserNotificationSetting() {
  }

  /**
   * @return the activeProviders
   */
  public String[] getActiveProviders() {
    return activeProviders;
  }

  /**
   * @param activeProviders the activeProviders to set
   */
  public void setActiveProviders(String[] activeProviders) {
    this.activeProviders = activeProviders;
  }

  /**
   * @param activeProvider the activeProviders to add
   */
  public void addActiveProvider(String activeProvider) {
    List<String> providers = new ArrayList<String>(Arrays.asList(activeProviders));
    if (providers.contains(activeProvider) == false) {
      providers.add(activeProvider);
    }
    activeProviders = providers.toArray(new String[providers.size()]);
  }

  /**
   * @return the frequency
   */
  public String getFrequency() {
    return frequency;
  }

  /**
   * @param frequency the frequency to set
   */
  public void setFrequency(String frequency) {
    this.frequency = frequency;
    isDaily = isWeekly = isMonthly = isImmediately = false;
    if (frequency.indexOf("d") > 0) {
      isDaily = true;
    } else if (frequency.indexOf("w") > 0) {
      isWeekly = true;
    } else if (frequency.indexOf("m") > 0) {
      isMonthly = true;
    } else {
      isImmediately = true;
    }
  }
  

  /**
   * @return the isImmediately
   */
  public boolean isImmediately() {
    return isImmediately;
  }

  /**
   * @return the isDaily
   */
  public boolean isDaily() {
    return isDaily;
  }

  /**
   * @return the isWeekly
   */
  public boolean isWeekly() {
    return isWeekly;
  }

  /**
   * @return the isMonthly
   */
  public boolean isMonthly() {
    return isMonthly;
  }
}
