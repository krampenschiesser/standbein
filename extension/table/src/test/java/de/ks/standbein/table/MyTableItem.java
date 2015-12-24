/**
 * Copyright [2015] [Christian Loehnert]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ks.standbein.table;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class MyTableItem {
  protected LocalDateTime time;
  protected LocalDate date;
  protected String name;
  protected short count;
  protected double sum;

  public LocalDateTime getTime() {
    return time;
  }

  public MyTableItem setTime(LocalDateTime time) {
    this.time = time;
    return this;
  }

  public String getName() {
    return name;
  }

  public MyTableItem setName(String name) {
    this.name = name;
    return this;
  }

  public short getCount() {
    return count;
  }

  public MyTableItem setCount(short count) {
    this.count = count;
    return this;
  }

  public double getSum() {
    return sum;
  }

  public MyTableItem setSum(double sum) {
    this.sum = sum;
    return this;
  }

  public LocalDate getDate() {
    return date;
  }

  public MyTableItem setDate(LocalDate date) {
    this.date = date;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof MyTableItem)) {
      return false;
    }

    MyTableItem that = (MyTableItem) o;

    return name.equals(that.name);

  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }
}
