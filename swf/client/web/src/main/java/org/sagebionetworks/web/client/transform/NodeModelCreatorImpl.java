package org.sagebionetworks.web.client.transform;

import org.sagebionetworks.gwt.client.schema.adapter.JSONObjectGwt;
import org.sagebionetworks.repo.model.Agreement;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.shared.DownloadLocation;
import org.sagebionetworks.web.shared.EntityTypeResponse;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.LayerPreview;
import org.sagebionetworks.web.shared.PagedResults;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.inject.Inject;

/**
 * This class exists to isolate JSONObject creation from any classes that need JVM based tests
 * This class doesn't need to be tested as the business logic is located elsewhere.
 * (JSONObect creation and JSONParser should not be used in classes that need testing)
 * @author dburdick
 *
 */
public class NodeModelCreatorImpl implements NodeModelCreator {		
	
	JSONEntityFactory factory;	
	
	@Inject
	public NodeModelCreatorImpl(JSONEntityFactory factory) {
		this.factory = factory;
	}
	
	@Override
	public Entity createEntity(EntityWrapper entityWrapper) throws RestServiceException {
		if(entityWrapper.getRestServiceException() != null) {
			throw entityWrapper.getRestServiceException();
		}
		try {
			return (Entity) factory.createEntity(entityWrapper.getEntityJson(), entityWrapper.getEntityClassName());
		} catch (JSONObjectAdapterException e) {
			throw new RestServiceException(e.getMessage());
		}
	}
	
	/**
	 * 
	 * @param <T>
	 * @param jsonString
	 * @param clazz
	 * @return
	 * @throws RestServiceException
	 */
	@Override
	public <T extends JSONEntity> T createEntity(String jsonString, Class<? extends T> clazz) throws RestServiceException{
		try {
			return factory.createEntity(jsonString, clazz);
		} catch (JSONObjectAdapterException e) {
			throw new RestServiceException(e.getMessage());
		}
	}
	
	@Override
	public <T extends JSONEntity> T createEntity(EntityWrapper entityWrapper, Class<? extends T> clazz) throws RestServiceException {
		if(entityWrapper.getRestServiceException() != null) {
			throw entityWrapper.getRestServiceException();
		}
		return createEntity(entityWrapper.getEntityJson(), clazz);
	}
	
	@Override
	public <T extends JSONEntity> T initializeEntity(String json, T newEntity)	throws RestServiceException {
		try {
			return factory.initializeEntity(json, newEntity);
		} catch (JSONObjectAdapterException e) {
			throw new RestServiceException(e.getMessage());
		}
	}
	
	@Override
	public String createAgreementJSON(Agreement agreement)	throws JSONObjectAdapterException {
		// Write it to an adapter
		JSONObjectAdapter adapter = agreement.writeToJSONObject(JSONObjectGwt.createNewAdapter());
		return adapter.toJSONString();
	}

	@Override
	public PagedResults createPagedResults(String json) throws RestServiceException {
		JSONObject obj = JSONParser.parseStrict(json).isObject();
		DisplayUtils.checkForErrors(obj);
		return new PagedResults(obj);
	}

	@Override
	public LayerPreview createLayerPreview(String json) throws RestServiceException {
		JSONObject obj = JSONParser.parseStrict(json).isObject();
		DisplayUtils.checkForErrors(obj);
		return new LayerPreview(obj);
	}

	@Override
	public DownloadLocation createDownloadLocation(String json) throws RestServiceException {
		JSONObject obj = JSONParser.parseStrict(json).isObject();
		DisplayUtils.checkForErrors(obj);
		return new DownloadLocation(obj);
	}

	@Override
	public EntityTypeResponse createEntityTypeResponse(String json) throws RestServiceException {
		JSONObject obj = JSONParser.parseStrict(json).isObject();
		DisplayUtils.checkForErrors(obj);
		return new EntityTypeResponse(obj);
	}

	@Override
	public void validate(String json) throws RestServiceException {
		if(!"".equals(json)) {
			JSONObject obj = JSONParser.parseStrict(json).isObject();
			DisplayUtils.checkForErrors(obj);
		}
	}



}
