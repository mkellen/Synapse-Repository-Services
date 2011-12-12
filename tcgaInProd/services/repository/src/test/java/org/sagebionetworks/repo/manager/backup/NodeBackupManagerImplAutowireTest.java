package org.sagebionetworks.repo.manager.backup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sagebionetworks.repo.manager.NodeManager;
import org.sagebionetworks.repo.manager.TestUserDAO;
import org.sagebionetworks.repo.manager.UserManager;
import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.DatastoreException;
import org.sagebionetworks.repo.model.Node;
import org.sagebionetworks.repo.model.NodeBackup;
import org.sagebionetworks.repo.model.NodeQueryDao;
import org.sagebionetworks.repo.model.NodeRevision;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.UserInfo;
import org.sagebionetworks.repo.model.query.BasicQuery;
import org.sagebionetworks.repo.model.query.Compartor;
import org.sagebionetworks.repo.model.query.CompoundId;
import org.sagebionetworks.repo.model.query.Expression;
import org.sagebionetworks.repo.model.util.RandomNodeUtil;
import org.sagebionetworks.repo.web.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test-context.xml" })
public class NodeBackupManagerImplAutowireTest {
	
	@Autowired
	NodeBackupManager backupManager;
	
	@Autowired
	NodeManager nodeManager;
	
	@Autowired
	public UserManager userManager;
	
	@Autowired
	NodeQueryDao nodeQueryDao;
	
	List<String> usersToDelete;
	List<String> nodesToDelete;
	
	UserInfo adminUser;	
	UserInfo nonAdminUser;
	String nonAdminPrincipalName;
	
	String newNodeId;
	String uniqueAnnotationName;
	String uniqueAnnotationValue;
	
	BasicQuery queryForNode;
	
	
	@Before
	public void before() throws Exception{
		usersToDelete = new ArrayList<String>();
		nodesToDelete =  new ArrayList<String>();
		adminUser = userManager.getUserInfo(TestUserDAO.ADMIN_USER_NAME);
		
		// First create a new user just for this test
		nonAdminPrincipalName = "NodeBackupManagerImplAutowireTest@testRestore";
		// Delete this principal if they already exist
		userManager.deletePrincipal(nonAdminPrincipalName);
		userManager.createPrincipal(nonAdminPrincipalName, true);
		usersToDelete.add(nonAdminPrincipalName);
		nonAdminUser = userManager.getUserInfo(nonAdminPrincipalName);

		
		Node randomNode = RandomNodeUtil.generateRandom(443);
		randomNode.setNodeType(ObjectType.folder.name());
		randomNode.setParentId(null);
		
		// First create a node to restore
		newNodeId = nodeManager.createNewNode(randomNode, nonAdminUser);
		nodesToDelete.add(newNodeId);

		// Create a second revision
		randomNode = nodeManager.get(nonAdminUser, newNodeId);
		Annotations annos = org.sagebionetworks.repo.model.util.RandomAnnotationsUtil.generateRandom(987, 40);
		annos.setId(randomNode.getId());
		annos.setEtag(randomNode.getETag());
		nodeManager.updateAnnotations(nonAdminUser, newNodeId, annos);
		
		annos = nodeManager.getAnnotations(nonAdminUser, newNodeId);
		uniqueAnnotationName = "onlyOnV299999990000011111";
		uniqueAnnotationValue ="This is only on the second version";
		annos.addAnnotation(uniqueAnnotationName, uniqueAnnotationValue);
		randomNode = nodeManager.get(nonAdminUser, newNodeId);
		randomNode.setVersionLabel("2.0");
		// Now create a new version of the node
		randomNode = nodeManager.update(nonAdminUser, randomNode, annos, true);
		
		// We should be able to find this node with this query
		queryForNode = new BasicQuery();
		queryForNode.setFrom(ObjectType.folder);
		queryForNode.addExpression(new Expression(new CompoundId(null, uniqueAnnotationName), Compartor.EQUALS, uniqueAnnotationValue));
		assertEquals(1, nodeQueryDao.executeCountQuery(queryForNode, nonAdminUser));
	}
	
	@After
	public void after(){
		if(usersToDelete != null && userManager != null){
			for(String userName: usersToDelete){
				try {
					userManager.deletePrincipal(userName);
				} catch (Exception e) {}
			}
		}
		if(nodesToDelete != null && nodeManager != null){
			for(String nodeId: nodesToDelete){
				try {
					nodeManager.delete(adminUser, nodeId);
				} catch (Exception e) {}
			}
		}
	}
	
	
	@Test
	public void testGetRootAndGetNode() throws DatastoreException, NotFoundException{
		// There should already be a root folder so get it.
		NodeBackup copy = backupManager.getRoot();
		assertNotNull(copy);
		assertNotNull(copy.getNode());
		assertNotNull(copy.getNode().getId());
		assertNotNull(copy.getAcl());
		assertNotNull(copy.getBenefactor());
		assertEquals("The root node should be its own benefactor",copy.getNode().getId(), copy.getBenefactor());
		assertNotNull(copy.getChildren());
		assertTrue(copy.getChildren().size() > 0);

		assertNotNull(copy.getRevisions());
		assertTrue(copy.getRevisions().size() > 0);
		
		// Make sure we can get each child
		for(String childId: copy.getChildren()){
			NodeBackup child = backupManager.getNode(childId);
			assertNotNull(child);
			assertNotNull(child.getNode());
			assertEquals("The child node's parent ID should match root!",copy.getNode().getId(), child.getNode().getParentId());
			assertNotNull(child.getChildren());
			assertNotNull(child.getRevisions());
			assertTrue(child.getRevisions().size() > 0);
		}
	}
	
	@Test
	public void testGetNodeRevision() throws DatastoreException, NotFoundException{
		NodeBackup root = backupManager.getRoot();
		assertNotNull(root);
		assertNotNull(root.getNode());
		assertNotNull(root.getNode().getId());
		assertNotNull(root.getRevisions());
		assertTrue(root.getRevisions().size() > 0);
		// Make sure we can get all revision of root
		for(Long revNumber: root.getRevisions()){
			NodeRevision rev = backupManager.getNodeRevision(root.getNode().getId(), revNumber);
			assertNotNull(rev);
			assertNotNull(rev.getNodeId());
			assertNotNull(rev.getAnnotations());
			assertNotNull(rev.getLabel());
			assertNotNull(rev.getComment());
			assertNotNull(rev.getModifiedBy());
			assertNotNull(rev.getModifiedOn());
		}
	}
	
	@Test
	public void testGetTotalNodeCount(){
		long count = backupManager.getTotalNodeCount();
		assertTrue(count > 0);
	}
	
	@Test
	public void testRestoreNonExisting() throws Exception{
		// Now get the node backup data 
		List<NodeRevision> revisions = new ArrayList<NodeRevision>();
		NodeBackup backup = backupManager.getNode(newNodeId);
		assertNotNull(backup);
		assertNotNull(backup.getNode());
		assertNotNull(backup.getAcl());
		assertEquals("This node should be its own benefactor",newNodeId, backup.getBenefactor());
		// Get all of the revisions
		for(Long revNumer: backup.getRevisions()){
			NodeRevision rev = backupManager.getNodeRevision(newNodeId, revNumer);
			revisions.add(rev);
		}
	
		// Now delete the node.
		nodeManager.delete(adminUser, newNodeId);
		// Now delete the user
		userManager.deletePrincipal(nonAdminPrincipalName);
		// Now restore the node
		backupManager.createOrUpdateNode(backup);
		// Restore each revision
		for(NodeRevision rev: revisions){
			backupManager.createOrUpdateRevision(rev);
		}
		
		//Now get the backup data again and make sure it matches the originals
		List<NodeRevision> cloneRevision = new ArrayList<NodeRevision>();
		NodeBackup cloneBackup = backupManager.getNode(newNodeId);
		// The only think that will not match is the etag
		cloneBackup.getNode().setETag(backup.getNode().getETag());
		cloneBackup.getAcl().setEtag((backup.getAcl().getEtag()));
		assertEquals(backup, cloneBackup);
		for(Long revNumer: cloneBackup.getRevisions()){
			NodeRevision rev = backupManager.getNodeRevision(newNodeId, revNumer);
			cloneRevision.add(rev);
		}
		assertEquals(revisions, cloneRevision);
		// We should also be able to find our restored node using its special query
		nonAdminUser = userManager.getUserInfo(nonAdminPrincipalName);
		assertEquals(1, nodeQueryDao.executeCountQuery(queryForNode, nonAdminUser));		
	}
	
	@Test
	public void testRestoreExisting() throws Exception{
		// Now get the node backup data 
		List<NodeRevision> revisions = new ArrayList<NodeRevision>();
		NodeBackup backup = backupManager.getNode(newNodeId);
		assertNotNull(backup);
		assertNotNull(backup.getNode());
		assertNotNull(backup.getAcl());
		assertEquals("This node should be its own benefactor",newNodeId, backup.getBenefactor());
		// Get all of the revisions
		for(Long revNumer: backup.getRevisions()){
			NodeRevision rev = backupManager.getNodeRevision(newNodeId, revNumer);
			revisions.add(rev);
		}

		// This time the node should still exist.
		backupManager.createOrUpdateNode(backup);
		// Restore each revision
		for(NodeRevision rev: revisions){
			backupManager.createOrUpdateRevision(rev);
		}
		
		//Now get the backup data again and make sure it matches the originals
		List<NodeRevision> cloneRevision = new ArrayList<NodeRevision>();
		NodeBackup cloneBackup = backupManager.getNode(newNodeId);
		// The only think that will not match is the etag
		cloneBackup.getNode().setETag(backup.getNode().getETag());
		cloneBackup.getAcl().setEtag((backup.getAcl().getEtag()));
		assertEquals(backup, cloneBackup);
		for(Long revNumer: cloneBackup.getRevisions()){
			NodeRevision rev = backupManager.getNodeRevision(newNodeId, revNumer);
			cloneRevision.add(rev);
		}
		assertEquals(revisions, cloneRevision);
		// We should also be able to find our restored node using its special query
		nonAdminUser = userManager.getUserInfo(nonAdminPrincipalName);
		assertEquals(1, nodeQueryDao.executeCountQuery(queryForNode, nonAdminUser));		
	}

}