package org.sagebionetworks.repo.manager;

import java.util.List;
import java.util.Set;

import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.AuthorizationConstants;
import org.sagebionetworks.repo.model.ConflictingUpdateException;
import org.sagebionetworks.repo.model.DatastoreException;
import org.sagebionetworks.repo.model.InvalidModelException;
import org.sagebionetworks.repo.model.Node;
import org.sagebionetworks.repo.model.Nodeable;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.UnauthorizedException;
import org.sagebionetworks.repo.model.UserInfo;
import org.sagebionetworks.repo.web.NotFoundException;

public interface NodeManager {
	
	// for testing (in prod it's autowired)
	public void setAuthorizationManager(AuthorizationManager authorizationManager);
	
	/**
	 * Create a new no
	 * @param userId
	 * @param newNode
	 * @return
	 */
	public String createNewNode(Node newNode, UserInfo userInfo) throws DatastoreException,
			InvalidModelException, NotFoundException, UnauthorizedException;
	
	/**
	 * Delete a node using its id.
	 * @param userName
	 * @param nodeId
	 * @throws UnauthorizedException 
	 * @throws DatastoreException 
	 * @throws NotFoundException 
	 */
	public void delete(UserInfo userInfo, String nodeId) throws NotFoundException, DatastoreException, UnauthorizedException;
	
	/**
	 * Get a node using its id.
	 * @param userName
	 * @param nodeId
	 * @return
	 * @throws UnauthorizedException 
	 * @throws DatastoreException 
	 * @throws NotFoundException 
	 */
	public Node get(UserInfo userInfo, String nodeId) throws NotFoundException, DatastoreException, UnauthorizedException;
	
	/**
	 * Get a node for a given version number.
	 * @param userInfo
	 * @param nodeId
	 * @return
	 * @throws UnauthorizedException 
	 * @throws DatastoreException 
	 * @throws NotFoundException 
	 */
	public Node getNodeForVersionNumber(UserInfo userInfo, String nodeId, Long versionNumber) throws NotFoundException, DatastoreException, UnauthorizedException;
	
	/**
	 * Update a node using the provided node.
	 * @param userName
	 * @param updated
	 * @return 
	 * @throws UnauthorizedException 
	 * @throws DatastoreException 
	 * @throws NotFoundException 
	 * @throws Exception 
	 */
	public Node update(UserInfo userInfo, Node updated) throws ConflictingUpdateException, NotFoundException, DatastoreException, UnauthorizedException, InvalidModelException;
	
	/**
	 * Update a node and its annotations in the same call.  This means we only need to acquire the lock once.
	 * @param username
	 * @param updatedNode
	 * @param updatedAnnoations
	 * @param newVersion - Should a new version be created for this update?
	 * @throws UnauthorizedException 
	 * @throws DatastoreException 
	 * @throws NotFoundException 
	 * @throws ConflictingUpdateException 
	 * @throws InvalidModelException 
	 */
	public Node update(UserInfo userInfo, Node updatedNode, Annotations updatedAnnoations, boolean newVersion) throws ConflictingUpdateException, NotFoundException, DatastoreException, UnauthorizedException, InvalidModelException;
	
	/**
	 * Use case:  Need to find out if a user can download a resource.
	 * 
	 * @param resourceId the ID of the resource of interest
	 * @param user
	 * @param accessType
	 * @return
	 */
	public boolean hasAccess(String resourceId, AuthorizationConstants.ACCESS_TYPE  accessType, UserInfo userInfo) throws NotFoundException, DatastoreException ;
	
	/**
	 * Get the annotations for a node
	 * @param username
	 * @param nodeId
	 * @return
	 * @throws UnauthorizedException 
	 * @throws DatastoreException 
	 * @throws NotFoundException 
	 */
	public Annotations getAnnotations(UserInfo userInfo, String nodeId) throws NotFoundException, DatastoreException, UnauthorizedException;
	
	/**
	 * Get the annotations for a given version number.
	 * @param userInfo
	 * @param nodeId
	 * @return
	 * @throws NotFoundException
	 * @throws DatastoreException
	 * @throws UnauthorizedException
	 */
	public Annotations getAnnotationsForVersion(UserInfo userInfo, String nodeId, Long versionNumber) throws NotFoundException, DatastoreException, UnauthorizedException;
	
	/**
	 * Update the annotations of a node.
	 * @param username
	 * @param nodeId
	 * @return
	 * @throws ConflictingUpdateException 
	 * @throws UnauthorizedException 
	 * @throws DatastoreException 
	 * @throws NotFoundException 
	 * @throws InvalidModelException 
	 */
	public Annotations updateAnnotations(UserInfo userInfo, String nodeId, Annotations updated) throws ConflictingUpdateException, NotFoundException, DatastoreException, UnauthorizedException, InvalidModelException;

	/**
	 * Get the children of a node
	 * @param userId
	 * @param parentId
	 * @return
	 * @throws NotFoundException 
	 */
	public Set<Node> getChildren(UserInfo userInfo, String parentId) throws NotFoundException;
	
	/**
	 * Get a list of all of the version numbers for a node.
	 * @param userInfo
	 * @param nodeId
	 * @return
	 * @throws NotFoundException
	 * @throws UnauthorizedException 
	 * @throws DatastoreException 
	 */
	public List<Long> getAllVersionNumbersForNode(UserInfo userInfo, String nodeId) throws NotFoundException, DatastoreException, UnauthorizedException;

	/**
	 * Get the node type of an entity
	 * @param userInfo
	 * @param entityId
	 * @return
	 * @throws UnauthorizedException 
	 * @throws DatastoreException 
	 * @throws NotFoundException 
	 */
	public ObjectType getNodeType(UserInfo userInfo, String entityId) throws NotFoundException, DatastoreException, UnauthorizedException;

	/**
	 * Delete a specific version of a node.
	 * @param userInfo
	 * @param id
	 * @param long1
	 * @throws DatastoreException 
	 * @throws NotFoundException 
	 * @throws UnauthorizedException 
	 * @throws ConflictingUpdateException 
	 */
	public void deleteVersion(UserInfo userInfo, String id, Long versionNumber) throws NotFoundException, DatastoreException, UnauthorizedException, ConflictingUpdateException;

}
