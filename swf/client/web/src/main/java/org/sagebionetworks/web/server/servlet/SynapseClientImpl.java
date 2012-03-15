package org.sagebionetworks.web.server.servlet;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;

import org.sagebionetworks.client.Synapse;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.search.SearchResults;
import org.sagebionetworks.repo.model.search.query.SearchQuery;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.EntityFactory;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.SynapseClient;
import org.sagebionetworks.web.shared.EntityBundleTransport;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.SerializableWhitelist;
import org.sagebionetworks.web.shared.exceptions.ExceptionUtil;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.inject.Inject;

@SuppressWarnings("serial")
public class SynapseClientImpl extends RemoteServiceServlet implements SynapseClient, TokenProvider  {

	@SuppressWarnings("unused")	
	private static Logger logger = Logger.getLogger(SynapseClientImpl.class.getName());
	private TokenProvider tokenProvider = this;
	JSONObjectAdapter jsonObjectAdapter = new JSONObjectAdapterImpl();
	
	/**
	 * Injected with Gin
	 */
	@SuppressWarnings("unused")
	private ServiceUrlProvider urlProvider;
		
	/**
	 * Essentially the constructor. Setup synapse client.
	 * @param provider
	 */
	@Inject
	public void setServiceUrlProvider(ServiceUrlProvider provider){
		this.urlProvider = provider;
	}
				
	/**
	 * Injected with Gin
	 */
	@SuppressWarnings("unused")
	private SynapseProvider synapseProvider = new SynapseProviderImpl();
		
	/**
	 * This allows tests provide mock Synapse ojbects
	 * @param provider
	 */
	public void setSynapseProvider(SynapseProvider provider){
		this.synapseProvider = provider;
	}

	/**
	 * This allows integration tests to override the token provider.
	 * @param tokenProvider
	 */
	public void setTokenProvider(TokenProvider tokenProvider){
		this.tokenProvider = tokenProvider;
	}

	/**
	 * Validate that the service is ready to go. If any of the injected data is
	 * missing then it cannot run. Public for tests.
	 */
	public void validateService() {
		if (synapseProvider == null)
			throw new IllegalStateException(
					"The SynapseProvider was not set");
		if(tokenProvider == null){
			throw new IllegalStateException(
			"The token provider was not set");
		}
	}
	
	@Override
	public String getSessionToken() {
		// By default, we get the token from the request cookies.
		return UserDataProvider.getThreadLocalUserToken(this.getThreadLocalRequest());
	}

	
	/*
	 * SynapseClient Service Methods
	 */
	
	/**
	 * Get an Entity by its id
	 */
	@Override
	public EntityWrapper getEntity(String entityId) {
		Synapse synapseClient = createSynapseClient();
		try {
			Entity entity = synapseClient.getEntityById(entityId);
			JSONObjectAdapter entityJson = entity.writeToJSONObject(jsonObjectAdapter.createNew());
			return new EntityWrapper(entityJson.toJSONString(), entity.getClass().getName(), null);		
		} catch (SynapseException e) {
			return new EntityWrapper(null, null, ExceptionUtil.convertSynapseException(e));
		} catch (JSONObjectAdapterException e) {
			return new EntityWrapper(null, null, new UnknownErrorException(e.getMessage()));
		}		
	}
	
	@Override
	public String getEntityTypeRegistryJSON() {		
		return SynapseClientImpl.getEntityTypeRegistryJson();
	}	

	@Override	
	public EntityWrapper getEntityPath(String entityId) { 
		Synapse synapseClient = createSynapseClient();
		try {
			EntityPath entityPath = synapseClient.getEntityPath(entityId); 
			JSONObjectAdapter entityPathJson = entityPath.writeToJSONObject(jsonObjectAdapter.createNew());
			return new EntityWrapper(entityPathJson.toJSONString(), entityPath.getClass().getName(), null);			
		}catch (SynapseException e) {
			return new EntityWrapper(null, null, ExceptionUtil.convertSynapseException(e));
		} catch (JSONObjectAdapterException e) {
			return new EntityWrapper(null, null, new UnknownErrorException(e.getMessage()));
		}
	}
	
	@Override
	public EntityWrapper search(String searchQueryJson) {
		Synapse synapseClient = createSynapseClient();
		try {
			JSONObjectAdapter adapter = new JSONObjectAdapterImpl();
			SearchResults searchResults = synapseClient.search(new SearchQuery(adapter.createNew(searchQueryJson)));
			searchResults.writeToJSONObject(adapter);
			return new EntityWrapper(adapter.toJSONString(), SearchResults.class.getName(), null);		
		}catch (SynapseException e) {
			return new EntityWrapper(null, null, ExceptionUtil.convertSynapseException(e));
		} catch (JSONObjectAdapterException e) {
			return new EntityWrapper(null, null, new UnknownErrorException(e.getMessage()));
		} catch (UnsupportedEncodingException e) {
			return new EntityWrapper(null, null, new UnknownErrorException(e.getMessage()));
		}		
	}

	
	
	/*
	 * Private Methods
	 */

	/**
	 * The synapse client is stateful so we must create a new one for each request
	 */
	private Synapse createSynapseClient() {
		// Create a new syanpse
		Synapse synapseClient = synapseProvider.createNewClient();
		synapseClient.setSessionToken(tokenProvider.getSessionToken());
		synapseClient.setRepositoryEndpoint(urlProvider.getRepositoryServiceUrl());
		synapseClient.setAuthEndpoint(urlProvider.getPublicAuthBaseUrl());
		return synapseClient;
	}	

	/**
	 * Read an input stream into a string.
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	private static String readToString(InputStream in) throws IOException {
		try {
			BufferedInputStream bufferd = new BufferedInputStream(in);
			byte[] buffer = new byte[1024];
			StringBuilder builder = new StringBuilder();
			int index = -1;
			while ((index = bufferd.read(buffer, 0, buffer.length)) > 0) {
				builder.append(new String(buffer, 0, index, "UTF-8"));
			}
			return builder.toString();
		} finally {
			in.close();
		}
	}

	@Override
	public SerializableWhitelist junk(SerializableWhitelist l) {
		return null;
	}

	
	public static String getEntityTypeRegistryJson() {
		ClassLoader classLoader = EntityType.class.getClassLoader();
		InputStream in = classLoader.getResourceAsStream(EntityType.REGISTER_JSON_FILE_NAME);
		if(in == null) throw new IllegalStateException("Cannot find the "+EntityType.REGISTER_JSON_FILE_NAME+" file on the classpath");
		String jsonString = "";
		try {
			jsonString = readToString(in);
		} catch (IOException e) {
			// error reading file
		}
		return jsonString;
	}

	@Override
	public EntityBundleTransport getEntityBundle(String entityId, int partsMask) throws RestServiceException {
		try{
			// Get all of the requested parts
			EntityBundleTransport transport = new EntityBundleTransport();
			Synapse synapseClient = createSynapseClient();
			// Add the entity?
			handleEntity(entityId, partsMask, transport, synapseClient);
			// Add the annotations?
			handleAnnotaions(entityId, partsMask, transport, synapseClient);
			// Add the permissions?
			handlePermissions(entityId, partsMask, transport, synapseClient);
			// Add the path?
			handleEntityPath(entityId, partsMask, transport, synapseClient);
			return transport;
		}catch(SynapseException e){
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	
	/**
	 * Set the entity path if requested
	 * @param entityId
	 * @param partsMask
	 * @param transport
	 * @param synapseClient
	 * @throws SynapseException
	 * @throws JSONObjectAdapterException
	 */
	public void handleEntityPath(String entityId, int partsMask, EntityBundleTransport transport, Synapse synapseClient) throws SynapseException, JSONObjectAdapterException {
		if((EntityBundleTransport.ENTITY_PATH & partsMask) > 0){
			EntityPath path = synapseClient.getEntityPath(entityId);
			transport.setEntityPathJson(EntityFactory.createJSONStringForEntity(path));
		}
	}

	/**
	 * Add the permissions to the bundle if requested.
	 * @param entityId
	 * @param partsMask
	 * @param transport
	 * @param synapseClient
	 * @throws SynapseException
	 * @throws JSONObjectAdapterException
	 */
	public void handlePermissions(String entityId, int partsMask,	EntityBundleTransport transport, Synapse synapseClient) throws SynapseException, JSONObjectAdapterException {
		if((EntityBundleTransport.PERMISSIONS & partsMask) > 0){
			UserEntityPermissions permissions = synapseClient.getUsersEntityPermissions(entityId);
			transport.setPermissionsJson(EntityFactory.createJSONStringForEntity(permissions));
		}
	}

	/**
	 * Add the annotations to the bundle if requested.
	 * @param entityId
	 * @param partsMask
	 * @param transport
	 * @param synapseClient
	 * @throws SynapseException
	 * @throws JSONObjectAdapterException
	 */
	public void handleAnnotaions(String entityId, int partsMask,	EntityBundleTransport transport, Synapse synapseClient) throws SynapseException, JSONObjectAdapterException {
		if((EntityBundleTransport.ANNOTATIONS & partsMask) > 0){
			Annotations annos = synapseClient.getAnnotations(entityId);
			transport.setAnnotaionsJson(EntityFactory.createJSONStringForEntity(annos));
		}
	}

	/**
	 * Add an entity to the bundle if requested
	 * @param entityId
	 * @param partsMask
	 * @param transport
	 * @param synapseClient
	 * @throws SynapseException
	 * @throws JSONObjectAdapterException
	 */
	public void handleEntity(String entityId, int partsMask, EntityBundleTransport transport, Synapse synapseClient) throws SynapseException, JSONObjectAdapterException {
		if((EntityBundleTransport.ENTITY & partsMask) > 0){
			Entity e = synapseClient.getEntityById(entityId);
			transport.setEntityJson(EntityFactory.createJSONStringForEntity(e));
		}
	}

}