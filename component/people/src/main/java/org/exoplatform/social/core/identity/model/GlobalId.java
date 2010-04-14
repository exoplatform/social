package org.exoplatform.social.core.identity.model;


/**
 * A GlobalId according to the definition of OpenSocial.
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class GlobalId {

  /**
   * the ':' separator character
   */
  private static final String SEPARATOR = ":";
  
  /**
   * Domain-Name part
   */
  private String domain;
  
  /**
   * Local-Id part
   */
  private String localId;
  
  /**
   * Creates a new GlobalId
   * @param id string representation of the global id. Must be of the form domain:localId
   * @throws IllegalArgumentException if the id does not have the expected form
   */
  public GlobalId(String id) {
    if (!isValid(id)) {
      throw new IllegalArgumentException(id + " is not a valid GlobalId. " 
                                         + "According to Opensocial specification, it should be of the form: "
                                         + "Global-Id   = Domain-Name \":\" Local-Id ");
    }
    String[] globalId = id.split(SEPARATOR);
    domain = globalId[0];
    localId = globalId[1];
  }

  public static boolean isValid(String id) {
    return (id!=null && id.indexOf(SEPARATOR) > 0);
  }

  public String getDomain() {
    return domain;
  }
  
  public String getLocalId() {
    return localId;
  }
  
  public String toString() {
    return domain + SEPARATOR + localId;
  }
  
  /**
   * creates a global id based on provider and remote id
   * @param providerId
   * @param remoteId
   * @return
   */
  public static GlobalId create(String providerId, String remoteId) {
    if (providerId == null ) {
      throw new IllegalArgumentException("Could not create a valid GlobalId with null providerId");
    }
    if (remoteId == null ) {
      throw new IllegalArgumentException("Could not create a valid GlobalId with null remoteId");
    }   
    return new GlobalId(providerId + SEPARATOR + remoteId);
  }

}
