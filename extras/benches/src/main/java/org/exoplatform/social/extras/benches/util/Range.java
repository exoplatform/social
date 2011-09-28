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
 * Define the Range class which is used for DataInjector
 * 
 * Example User which has index from 10 -> 50 has 20 activities
 * 
 * Range[low = 10; high = 50; amount =20]
 * 
 * Created by The eXo Platform SAS
 * Author : Thanh_VuCong
 *          thanhvc@exoplatform.com
 * Sep 5, 2011.
 */
public class Range {

  private long low;
  private long high;
  private long amount;
  /**
   * Constructor for low, high and amount.
   * @param low
   * @param high
   * @param amount
   */
  public Range(long low, long high, long amount) {
    this.low = low;
    this.high = high;
    this.amount = amount;

  }

  /**
   * Gets low of this range
   * @return
   */
  public long getLow() {
    return low;
  }

  /**
   * Sets low for this range
   * @param low
   */
  public void setLow(long low) {
    this.low = low;
  }

  /**
   * Gets high for this <code>Range</code>
   * @return
   */
  public long getHigh() {
    return high;
  }

  /**
   * Sets high for this <code>Range</code>
   * @param high
   */
  public void setHigh(long high) {
    this.high = high;
  }

  /**
   * Gets amout of this <code>Range</code>
   * @return
   */
  public long getAmount() {
    return amount;
  }

  /**
   * Sets amount for this <code>Range</code>
   * @param amount
   */
  public void setAmount(long amount) {
    this.amount = amount;
  }

  @Override
  public String toString() {
    return "[low = " + getLow() + " high = " + getHigh() + " with Amount = " + getAmount() + "]";
  }
}
