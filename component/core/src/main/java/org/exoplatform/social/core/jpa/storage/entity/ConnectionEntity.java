/*
 * Copyright (C) 2003-2016 eXo Platform SAS.
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

package org.exoplatform.social.core.jpa.storage.entity;

import java.util.Date;

import javax.persistence.*;

import org.exoplatform.commons.api.persistence.ExoEntity;
import org.exoplatform.social.core.relationship.model.Relationship.Type;

@Entity(name = "SocConnection")
@ExoEntity
@Table(name = "SOC_CONNECTIONS",
       uniqueConstraints=@UniqueConstraint(columnNames = {"SENDER_ID", "RECEIVER_ID"}))
@NamedQueries({
        @NamedQuery(name = "getRelationships",
                query = "select r from SocConnection r"),
        @NamedQuery(name = "SocConnection.findConnectionsByIdentityIds",
                query = "SELECT c FROM SocConnection c WHERE (c.sender.id = :identityId AND c.receiver.id in (:ids) ) OR (c.receiver.id = :identityId AND c.sender.id in (:ids) )"),
        @NamedQuery(name = "SocConnection.findConnectionBySenderAndReceiver",
                query = "SELECT c FROM SocConnection c WHERE c.sender.id = :sender AND c.receiver.id = :reciver"),
        @NamedQuery(name = "SocConnection.findConnectionBySenderAndReceiverWithStatus",
                query = "SELECT c FROM SocConnection c WHERE c.sender.id = :sender AND c.receiver.id = :reciver AND c.status = :status"),
        @NamedQuery(name = "SocConnection.deleteConnectionByIdentity",
                query = "DELETE FROM SocConnection c WHERE c.sender.id = :identityId OR c.receiver.id = :identityId"),
        @NamedQuery(name = "SocConnection.getConnectionsWithStatus", query = "SELECT c FROM SocConnection c WHERE (c.sender.id = :identityId OR c.receiver.id = :identityId) AND c.status = :status AND c.sender.enabled = true AND c.receiver.enabled = true AND c.sender.deleted = false AND c.receiver.deleted = false order by c.updatedDate DESC"),
        @NamedQuery(name = "SocConnection.countConnectionsWithStatus", query = "SELECT count(distinct c.id) from SocConnection c WHERE (c.sender.id = :identityId or c.receiver.id = :identityId) AND c.status = :status"),
        @NamedQuery(name = "SocConnection.getConnectionsWithoutStatus", query = "SELECT c AS receiver FROM SocConnection c WHERE (c.sender.id = :identityId OR c.receiver.id = :identityId)"),
        @NamedQuery(name = "SocConnection.countConnectionsWithoutStatus", query = "SELECT count(distinct c.id) from SocConnection c WHERE (c.sender.id = :identityId or c.receiver.id = :identityId)"),
        @NamedQuery(name = "SocConnection.getReceiverBySenderWithStatus", query = "SELECT c FROM SocConnection c WHERE c.sender.id = :identityId AND c.status = :status"),
        @NamedQuery(name = "SocConnection.getReceiverIdsBySenderWithStatus", query = "SELECT c.receiver.id FROM SocConnection c WHERE c.sender.id = :identityId AND c.status = :status"),
        @NamedQuery(name = "SocConnection.getReceiverIdsBySenderWithoutStatus", query = "SELECT c.receiver.id FROM SocConnection c WHERE c.sender.id = :identityId"),
        @NamedQuery(name = "SocConnection.getReceiverBySenderWithoutStatus", query = "SELECT c FROM SocConnection c WHERE c.sender.id = :identityId"),
        @NamedQuery(name = "SocConnection.getSenderByReceiverWithStatus", query = "SELECT c FROM SocConnection c WHERE c.receiver.id = :identityId AND c.status = :status"),
        @NamedQuery(name = "SocConnection.getSenderIdsByReceiverWithStatus", query = "SELECT c.sender.id FROM SocConnection c WHERE c.receiver.id = :identityId AND c.status = :status"),
        @NamedQuery(name = "SocConnection.getSenderIdsByReceiverWithoutStatus", query = "SELECT c.sender.id FROM SocConnection c WHERE c.receiver.id = :identityId"),
        @NamedQuery(name = "SocConnection.getSenderByReceiverWithoutStatus", query = "SELECT c FROM SocConnection c WHERE c.receiver.id = :identityId"),
        @NamedQuery(name = "SocConnection.countReceiverBySenderWithStatus", query = "SELECT count(distinct c.receiver.id) FROM SocConnection c WHERE c.sender.id = :identityId AND c.status = :status"),
        @NamedQuery(name = "SocConnection.countReceiverBySenderWithoutStatus", query = "SELECT count(distinct c.receiver.id) FROM SocConnection c WHERE c.sender.id = :identityId"),
        @NamedQuery(name = "SocConnection.countSenderByReceiverWithStatus", query = "SELECT count(distinct c.sender.id) FROM SocConnection c WHERE c.receiver.id = :identityId AND c.status = :status"),
        @NamedQuery(name = "SocConnection.countSenderByReceiverWithoutStatus", query = "SELECT count(distinct c.sender.id) FROM SocConnection c WHERE c.receiver.id = :identityId"),
        @NamedQuery(name = "SocConnection.migrateSenderId", query = "UPDATE SocConnection c SET c.sender.id = :newId WHERE c.sender.id = :oldId"),
        @NamedQuery(name = "SocConnection.migrateReceiverId", query = "UPDATE SocConnection c SET c.receiver.id = :newId WHERE c.receiver.id = :oldId")
})
public class ConnectionEntity {

  @Id
  @SequenceGenerator(name="SEQ_SOC_CONNECTIONS_ID", sequenceName="SEQ_SOC_CONNECTIONS_ID")
  @GeneratedValue(strategy=GenerationType.AUTO, generator="SEQ_SOC_CONNECTIONS_ID")
  @Column(name = "CONNECTION_ID")
  private Long id;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "SENDER_ID", referencedColumnName = "IDENTITY_ID")
  private IdentityEntity sender;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "RECEIVER_ID", referencedColumnName = "IDENTITY_ID")
  private IdentityEntity receiver;
  
  @Enumerated
  @Column(name="STATUS", nullable = false)
  private Type status;
  
  /** */
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name="UPDATED_DATE", nullable = false)
  private Date updatedDate = new Date();

  public ConnectionEntity() {
  }

  public ConnectionEntity(IdentityEntity sender, IdentityEntity receiver) {
    this.sender = sender;
    this.receiver = receiver;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public IdentityEntity getSender() {
    return sender;
  }

  public void setSender(IdentityEntity sender) {
    this.sender = sender;
  }

  public IdentityEntity getReceiver() {
    return receiver;
  }

  public void setReceiver(IdentityEntity receiver) {
    this.receiver = receiver;
  }

  public Type getStatus() {
    return status;
  }

  public void setStatus(Type status) {
    if (status == Type.ALL) {
      throw new IllegalArgumentException("Illegal status ["+status+"]");
    }
    this.status = status;
  }

  public Date getUpdatedDate() {
    return updatedDate;
  }

  public void setUpdatedDate(Date updatedDate) {
    this.updatedDate = updatedDate;
  }

}
