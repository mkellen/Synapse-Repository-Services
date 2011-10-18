package org.sagebionetworks.repo.model.jdo;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sagebionetworks.repo.model.BackupRestoreStatus;
import org.sagebionetworks.repo.model.BackupRestoreStatus.STATUS;
import org.sagebionetworks.repo.model.BackupRestoreStatus.TYPE;
import org.sagebionetworks.repo.model.BackupRestoreStatusDAO;
import org.sagebionetworks.repo.model.DatastoreException;
import org.sagebionetworks.repo.web.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:jdomodels-test-context.xml" })
public class BackupRestoreStatusDAOImplTest {

	@Autowired
	private BackupRestoreStatusDAO backupRestoreStatusDao;
	
	private List<String> toDelete;
	
	
	@Before
	public void before(){
		toDelete = new ArrayList<String>();
	}
	
	@After
	public void after(){
		if(toDelete != null && backupRestoreStatusDao != null){
			for(String id: toDelete){
				try {
					backupRestoreStatusDao.delete(id);
				} catch (Exception e) {}
			}
		}
	}
	
	@Test
	public void testCreateAndGetWithOptionalNulls() throws Exception{
		// First setup a DTO
		BackupRestoreStatus dto = createStatusObject(STATUS.COMPLETED, TYPE.RESTORE);
		dto.setErrorDetails(null);
		dto.setErrorMessage(null);
		dto.setBackupUrl(null);
		
		// Now create it
		String id = backupRestoreStatusDao.create(dto);
		assertNotNull(id);
		toDelete.add(id);
		// Now get it back
		BackupRestoreStatus clone = backupRestoreStatusDao.get(id);
		dto.setId(id);
		assertEquals(dto, clone);
	}

	/**
	 * Helper to create a status object with the miniumn data.
	 * @param status
	 * @param type
	 * @return
	 */
	public BackupRestoreStatus createStatusObject(STATUS status, TYPE type) {
		BackupRestoreStatus dto = new BackupRestoreStatus();
		dto.setStatus(status.name());
		dto.setType(type.name());
		dto.setStartedBy("someAdmin@sagebase.org");
		dto.setStartedOn(new Date());
		dto.setProgresssMessage("Finally finished!");
		dto.setProgresssCurrent(0);
		dto.setProgresssTotal(100);
		dto.setTotalTimeMS(0);
		dto.setErrorDetails(null);
		dto.setErrorMessage(null);
		dto.setBackupUrl(null);
		return dto;
	}
	
	@Test
	public void testCreateAndGetWithOptionalAllValues() throws Exception{
		// First setup a DTO
		BackupRestoreStatus dto = createStatusObject(STATUS.STARTED, TYPE.BACKUP);
		dto.setErrorDetails("some short message");
		dto.setErrorMessage("imagine I am a full stack trace");
		dto.setBackupUrl("https://somedomean:port/bucket/file.zip");
		
		// Now create it
		String id = backupRestoreStatusDao.create(dto);
		assertNotNull(id);
		toDelete.add(id);
		// Now get it back
		BackupRestoreStatus clone = backupRestoreStatusDao.get(id);
		dto.setId(id);
		assertEquals(dto, clone);
	}
	
	@Test (expected=NotFoundException.class)
	public void testGetDoesNotExist() throws Exception{
		// This should throw a not found exception.
		BackupRestoreStatus clone = backupRestoreStatusDao.get("12345");
	}
	
	@Test
	public void testDelete() throws DatastoreException, NotFoundException{
		// First setup a DTO
		BackupRestoreStatus dto = createStatusObject(STATUS.STARTED, TYPE.BACKUP);
		String id = backupRestoreStatusDao.create(dto);
		assertNotNull(id);
		toDelete.add(id);
		backupRestoreStatusDao.delete(id);
		try{
			backupRestoreStatusDao.get(id);
			fail("We should not have been able to find a status object after deleting it.");
		}catch(NotFoundException e){
			// expected
		}
		
	}
	
	@Test
	public void testUpdate() throws DatastoreException, NotFoundException{
		// First setup a DTO
		BackupRestoreStatus dto = createStatusObject(STATUS.STARTED, TYPE.BACKUP);
		String id = backupRestoreStatusDao.create(dto);
		assertNotNull(id);
		toDelete.add(id);
		dto = backupRestoreStatusDao.get(id);
		// change it
		dto.setStatus(STATUS.FAILED.name());
		dto.setErrorMessage("Something bad happened");
		dto.setErrorDetails("and here are the details");
		backupRestoreStatusDao.update(dto);
		BackupRestoreStatus clone = backupRestoreStatusDao.get(id);
		assertEquals(dto, clone);	
	}
	
	@Test
	public void testForceTerminate() throws DatastoreException, NotFoundException{
		BackupRestoreStatus dto = createStatusObject(STATUS.STARTED, TYPE.BACKUP);
		String id = backupRestoreStatusDao.create(dto);
		assertNotNull(id);
		toDelete.add(id);
		// This job should not be forced to terminate until told to do so.
		assertFalse(backupRestoreStatusDao.shouldJobTerminate(id));
		// Now force it to terminate and check again
		backupRestoreStatusDao.setForceTermination(id, true);
		assertTrue(backupRestoreStatusDao.shouldJobTerminate(id));
		// Flip it back
		backupRestoreStatusDao.setForceTermination(id, false);
		assertFalse(backupRestoreStatusDao.shouldJobTerminate(id));
	}
}
