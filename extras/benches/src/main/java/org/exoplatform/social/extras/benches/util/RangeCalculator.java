/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.extras.benches.util;

/**
 * Created by The eXo Platform SAS
 * Author : Thanh_VuCong
 *          thanhvc@exoplatform.com
 * Sep 5, 2011  
 */
public class RangeCalculator {

  /**
   * Calculate and creates the Range[]
   * @param totalNumber
   * @param ranks
   * @return
   */
  public static Range[] calculateRange(long totalNumber, long[] ranks) {
    if (totalNumber < 0) {
      throw new IllegalArgumentException("Rank's value is invalid.");
    }
    //process ranks == null or empty
    Range[] result = null;
    if (ranks == null || ranks.length == 0) {
      result = new Range[1];
      long low = 0;
      long high = totalNumber -1;
      result[0] = new Range(low, high, totalNumber);
      return result;
    }  
    
    if (totalNumber < ranks.length) {
      throw new IllegalArgumentException("Rank's value is invalid.");
    }
    //process ranks is not empty
    result = new Range[ranks.length];
    long rangeValue = totalNumber / ranks.length;
    // number of range
    long loop = ranks.length;
    long low = 0;
    long high = 0;
    for (int i = 0; i < loop; i++) {
      low = i * rangeValue;
      high = (i + 1) * rangeValue;
      result[i] = new Range(low, high, ranks[i]);

    }

    // Add more reminder value when divide.
    if (result[ranks.length - 1] != null) {
      high = result[ranks.length - 1].getHigh();
      high += totalNumber % loop;
      result[ranks.length - 1].setHigh(high);
    }
    return result;
  }
}
