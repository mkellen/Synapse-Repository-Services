package org.sagebionetworks.repo.web.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.sagebionetworks.repo.manager.UserManager;
import org.sagebionetworks.repo.manager.UserProfileManager;
import org.sagebionetworks.repo.model.AuthorizationConstants;
import org.sagebionetworks.repo.model.ConflictingUpdateException;
import org.sagebionetworks.repo.model.DatastoreException;
import org.sagebionetworks.repo.model.InvalidModelException;
import org.sagebionetworks.repo.model.PaginatedResults;
import org.sagebionetworks.repo.model.QueryResults;
import org.sagebionetworks.repo.model.ServiceConstants;
import org.sagebionetworks.repo.model.UnauthorizedException;
import org.sagebionetworks.repo.model.UserInfo;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.attachment.PresignedUrl;
import org.sagebionetworks.repo.model.attachment.S3AttachmentToken;
import org.sagebionetworks.repo.web.NotFoundException;
import org.sagebionetworks.repo.web.UrlHelpers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller

public class UserProfileController extends BaseController {

	@Autowired
	UserProfileManager userProfileManager;
	
	@Autowired
	UserManager userManager;

	@Autowired
	ObjectTypeSerializer objectTypeSerializer;

	/**
	 * Get a user profile
	 * @param userId - The user that is making the request.
	 * @param request
	 * @return The UserProfile
	 * @throws DatastoreException - Thrown when there is a server-side problem.
	 */
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value = UrlHelpers.USER_PROFILE, method = RequestMethod.GET)
	public @ResponseBody
	UserProfile getMyOwnUserProfile(
			@RequestParam(value = AuthorizationConstants.USER_ID_PARAM, required = false) String userId
			) throws DatastoreException, UnauthorizedException, NotFoundException {
		UserInfo userInfo = userManager.getUserInfo(userId);
		return userProfileManager.getUserProfile(userInfo, userInfo.getIndividualGroup().getId());
	}

	/**
	 * Get a user profile specifying the individual group id for the user of interest
	 * @param userId - The user that is making the request.
	 * @param request
	 * @return The UserProfile
	 * @throws DatastoreException - Thrown when there is a server-side problem.
	 */
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value = UrlHelpers.USER_PROFILE_ID, method = RequestMethod.GET)
	public @ResponseBody
	UserProfile getUserProfileByOwnerId(
			@RequestParam(value = AuthorizationConstants.USER_ID_PARAM, required = false) String userId,
			@PathVariable String profileId) throws DatastoreException, UnauthorizedException, NotFoundException {
		UserInfo userInfo = userManager.getUserInfo(userId);
		return userProfileManager.getUserProfile(userInfo, profileId);
	}

	/**
	 * Get all the UserProfiles in the system (paginated
	 * @param userId - The user that is making the request.
	 * @param request
	 * @return The UserProfiles 
	 * @throws DatastoreException - Thrown when there is a server-side problem.
	 */
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value = UrlHelpers.USER, method = RequestMethod.GET)
	public @ResponseBody
	PaginatedResults<UserProfile> getUserProfilesPaginated(HttpServletRequest request,
			@RequestParam(value = AuthorizationConstants.USER_ID_PARAM, required = false)  String userId,
			@RequestParam(value = ServiceConstants.PAGINATION_OFFSET_PARAM, required = false, defaultValue = ServiceConstants.DEFAULT_PAGINATION_OFFSET_PARAM_NEW) Integer offset,
			@RequestParam(value = ServiceConstants.PAGINATION_LIMIT_PARAM, required = false, defaultValue = ServiceConstants.DEFAULT_PRINCIPALS_PAGINATION_LIMIT_PARAM) Integer limit,
			@RequestParam(value = ServiceConstants.SORT_BY_PARAM, required = false, defaultValue = ServiceConstants.DEFAULT_SORT_BY_PARAM) String sort,
			@RequestParam(value = ServiceConstants.ASCENDING_PARAM, required = false, defaultValue = ServiceConstants.DEFAULT_ASCENDING_PARAM) Boolean ascending
			) throws DatastoreException, UnauthorizedException, NotFoundException {
		UserInfo userInfo = userManager.getUserInfo(userId);
		long endExcl = offset+limit;
		QueryResults<UserProfile >results = userProfileManager.getInRange(userInfo, offset, endExcl);
		
		return new PaginatedResults<UserProfile>(
				request.getServletPath()+UrlHelpers.USER, 
				results.getResults(),
				(int)results.getTotalNumberOfResults(), 
				offset, 
				limit,
				sort, 
				ascending);
	}

	/**
	 * Get a user profile
	 * @param userId - The user that is making the request.
	 * @param request
	 * @return The updated UserProfile
	 * @throws DatastoreException - Thrown when there is a server-side problem.
	 */
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value = UrlHelpers.USER_PROFILE, method = RequestMethod.PUT)
	public @ResponseBody
	UserProfile updateUserProfile(
			@RequestParam(value = AuthorizationConstants.USER_ID_PARAM, required = false) String userId,
			@RequestHeader HttpHeaders header,
			@RequestHeader(ServiceConstants.ETAG_HEADER) String etag,
			HttpServletRequest request)
			throws NotFoundException, ConflictingUpdateException,
			DatastoreException, InvalidModelException, UnauthorizedException, IOException {
		UserInfo userInfo = userManager.getUserInfo(userId);
		UserProfile entity = (UserProfile) objectTypeSerializer.deserialize(request.getInputStream(), header, UserProfile.class, header.getContentType());
		if(etag != null){
			entity.setEtag(etag.toString());
		}
		return userProfileManager.updateUserProfile(userInfo, entity);
	}

	/**
	 * Create a security token for use for a particular with a particular
	 * locationable user profile picture to be stored in AWS S3
	 * 
	 * @param userId
	 * @param id
	 * @param etag
	 * @param s3Token
	 * @param request
	 * @return a filled-in S3Token
	 * @throws NotFoundException
	 * @throws DatastoreException
	 * @throws UnauthorizedException
	 * @throws InvalidModelException
	 */
	@ResponseStatus(HttpStatus.CREATED)
	@RequestMapping(value = { UrlHelpers.USER_PROFILE_S3_ATTACHMENT_TOKEN }, method = RequestMethod.POST)
	public @ResponseBody
	S3AttachmentToken createUserProfileS3AttachmentToken(
			@RequestParam(value = AuthorizationConstants.USER_ID_PARAM, required = false) String userId,
			@PathVariable String profileId, @RequestBody S3AttachmentToken token,
			HttpServletRequest request) throws NotFoundException,
			DatastoreException, UnauthorizedException, InvalidModelException {
		UserInfo userInfo = userManager.getUserInfo(userId);
		return userProfileManager.createS3UserProfileAttachmentToken(userInfo, profileId, token);
	}
	/**
	 * Create a token used to upload an attachment.
	 * 
	 * @param userId
	 * @param id
	 * @param token
	 * @param request
	 * @return
	 * @throws NotFoundException
	 * @throws DatastoreException
	 * @throws UnauthorizedException
	 * @throws InvalidModelException
	 */
	@ResponseStatus(HttpStatus.CREATED)
	@RequestMapping(value = { UrlHelpers.USER_PROFILE_ATTACHMENT_URL }, method = RequestMethod.POST)
	public @ResponseBody
	PresignedUrl getUserProfileAttachmentUrl(
			@RequestParam(value = AuthorizationConstants.USER_ID_PARAM, required = false) String userId,
			@PathVariable String profileId,
			@RequestBody PresignedUrl url,
			HttpServletRequest request) throws NotFoundException,
			DatastoreException, UnauthorizedException, InvalidModelException {
		if(url == null) throw new IllegalArgumentException("A PresignedUrl must be provided");
		// Pass it along.
		UserInfo userInfo = userManager.getUserInfo(userId);
		return userProfileManager.getUserProfileAttachmentUrl(userInfo, profileId, url.getTokenID());
	}

	
}
