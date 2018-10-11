/*
 * Copyright (C) 2015 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.exoplatform.social.core.jpa.storage.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.exoplatform.commons.api.persistence.ExoEntity;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
@Entity(name = "SocIdentityExperiences")
@ExoEntity
@Table(name = "SOC_IDENTITY_EXPERIENCES")
public class ProfileExperienceEntity implements Serializable {
  private static final long serialVersionUID = -6756289453682486794L;

  @Id
  @SequenceGenerator(name="SEQ_SOC_EXPERIENCE_ID", sequenceName="SEQ_SOC_EXPERIENCE_ID")
  @GeneratedValue(strategy= GenerationType.AUTO, generator="SEQ_SOC_EXPERIENCE_ID")
  @Column(name="EXPERIENCE_ID")
  private long id;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "IDENTITY_ID", referencedColumnName = "IDENTITY_ID")
  private IdentityEntity identity;

  @Column(name = "COMPANY")
  private String company;
  @Column(name = "POSITION")
  private String position;
  @Column(name = "START_DATE")
  private String startDate;
  @Column(name = "END_DATE")
  private String endDate;
  @Column(name = "SKILLS")
  private String skills;
  @Column(name = "DESCRIPTION")
  private String description;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public IdentityEntity getIdentity() {
    return identity;
  }

  public void setIdentity(IdentityEntity identity) {
    this.identity = identity;
  }

  public String getCompany() {
    return company;
  }

  public void setCompany(String company) {
    this.company = company;
  }

  public String getPosition() {
    return position;
  }

  public void setPosition(String position) {
    this.position = position;
  }

  public String getStartDate() {
    return startDate;
  }

  public void setStartDate(String startDate) {
    this.startDate = startDate;
  }

  public String getEndDate() {
    return endDate;
  }

  public void setEndDate(String endDate) {
    this.endDate = endDate;
  }

  public String getSkills() {
    return skills;
  }

  public void setSkills(String skills) {
    this.skills = skills;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean isCurrent() {
    return (getEndDate() == null && getStartDate() != null);
  }
}
