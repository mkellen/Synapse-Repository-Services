package org.sagebionetworks.repo.manager;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sagebionetworks.StackConfiguration;
import org.sagebionetworks.ids.IdGenerator;
import org.sagebionetworks.repo.manager.image.ImagePreviewUtils;
import org.sagebionetworks.repo.model.DatastoreException;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.InvalidModelException;
import org.sagebionetworks.repo.model.UnauthorizedException;
import org.sagebionetworks.repo.model.UserInfo;
import org.sagebionetworks.repo.model.attachment.AttachmentData;
import org.sagebionetworks.repo.model.attachment.PreviewState;
import org.sagebionetworks.repo.web.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author jmhill
 *
 */
public class AttachmentManagerImpl implements AttachmentManager{
	
	/**
	 * The max dimentions of a preview image.
	 */
	private static int MAX_PREVIEW_PIXELS = StackConfiguration.getMaximumPreivewPixels();
		
	@Autowired
	AmazonS3Utility s3Utility;
	@Autowired
	private IdGenerator idGenerator;
		
	/**
	 * This is the default used by spring.
	 */
	public AttachmentManagerImpl(){
	}
	/**
	 * This constructor is used for unit tests with Mock
	 * 
	 * @param tokenManager
	 */
	public AttachmentManagerImpl(AmazonS3Utility utlitity, IdGenerator idGen){
		this.s3Utility = utlitity;
		this.idGenerator = idGen;
	}
	
	private static Set<String> IMAGE_TYPES = new HashSet<String>();
	static{
		IMAGE_TYPES.add("GIF");
		IMAGE_TYPES.add("PNG");
		IMAGE_TYPES.add("JPEG");
		IMAGE_TYPES.add("JPG");
		IMAGE_TYPES.add("BMP");
		IMAGE_TYPES.add("WBMP");
	}

	@Override
	public void checkAttachmentsForPreviews(Entity entity) throws NotFoundException, DatastoreException, UnauthorizedException, InvalidModelException {
		if(entity != null){
			List<AttachmentData> attachments = entity.getAttachments();
			if(attachments != null){
				// We only create previews for images currently.
				for(AttachmentData data: attachments){
					validateAndCheckForPreview(entity.getId(), data);
				}
			}
		}
	}

	/**
	 * Validate the passed attachment data and attempt to create a preview if it does not exist
	 * @param userId
	 * @param entity
	 * @param data
	 * @throws NotFoundException
	 * @throws DatastoreException
	 * @throws UnauthorizedException
	 * @throws InvalidModelException
	 */
	public void validateAndCheckForPreview(String entityId, AttachmentData data) throws NotFoundException, DatastoreException,
			UnauthorizedException, InvalidModelException {
		// validate the data
		validateAttachmentData(data);
		// We can skip any attachment with a preview state already set
		if(data.getPreviewState() != null)	return;
		// Is this a type we can make a preview for?
		File tempDownload = null;
		File tempUpload = null;
		try{
			if(isPreviewType(data.getName())){
				// This is an image format
				tempDownload = downloadImage(entityId, data);
				// Now write the preview to a temp image
				tempUpload = File.createTempFile("AttachmentManagerPreviewUpload", ".tmp");
				// create the preview
				FileInputStream in = new FileInputStream(tempDownload);
				FileOutputStream out = new FileOutputStream(tempUpload);
				try{
					// This will create the preview
					ImagePreviewUtils.createPreviewImage(in, MAX_PREVIEW_PIXELS, out);
					// Create a previewID
					data.setPreviewId(idGenerator.generateNewId().toString());
					// The last step is to upload the file to s3
					String previewPath = S3TokenManagerImpl.createAttachmentPath(entityId, data.getPreviewId());
					s3Utility.uploadToS3(tempUpload, previewPath);
					data.setPreviewState(PreviewState.PREVIEW_EXISTS);
				}finally{
					in.close();
					out.close();
				}
			}else{
				// This is not an image format
				data.setPreviewState(PreviewState.NOT_COMPATIBLE);
			}
		} catch (IOException e) {
			throw new DatastoreException(e);
		}finally{
			// Cleanup the temp files
			if(tempDownload != null){
				tempDownload.delete();
			}
			// cleanup the temp files
			if(tempUpload != null){
				tempUpload.delete();
			}
			// If we did not set the preview state then this is a failure
			if(data.getPreviewState() == null){
				data.setPreviewState(PreviewState.FAILED);
			}
		}
	}
	

	/**
	 * Get the Download URL that will be used to create a preview.
	 * @param userId
	 * @param entity
	 * @param data
	 * @return
	 * @throws NotFoundException
	 * @throws DatastoreException
	 * @throws UnauthorizedException
	 * @throws InvalidModelException
	 */
	public File downloadImage(String entityId, AttachmentData data)	throws NotFoundException, DatastoreException,
			UnauthorizedException, InvalidModelException {
		if(data.getTokenId() != null){
			// Get a url for this token
			String key = S3TokenManagerImpl.createAttachmentPath(entityId, data.getTokenId());
			return s3Utility.downloadFromS3(key);
		}else{
			// When the give us a URL we just download it.
			return downlaodFileFromUrl(data.getUrl());
		}
	}
	
	/**
	 * Downlaod a file from the given url
	 * @param todownlaod
	 * @return
	 * @throws DatastoreException
	 */
	private File downlaodFileFromUrl(String todownlaod) throws DatastoreException{
		try {
			URL url = new URL(todownlaod);
			File temp = File.createTempFile("AttachmentManager", ".tmp");
			// Read the file
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(temp));
			InputStream in = null;
			try{
				byte[] buffer = new byte[1024];
				in = url.openStream();
				int length = 0;
				while((length = in.read(buffer)) > 0){
					out.write(buffer, 0, length);
				}
				return temp;
			}finally{
				if(in != null){
					in.close();
				}
				out.close();
			}
		} catch (IOException e) {
			throw new DatastoreException(e);
		}
	}
	
	/**
	 * Is this a valid attachment data object?
	 * @param data
	 */
	public static void validateAttachmentData(AttachmentData data){
		if(data.getName() == null) throw new IllegalArgumentException("Attachment name cannot be null");
		// All attachments must have either a URL or a token
		if(data.getTokenId() == null && data.getUrl() == null) throw new IllegalArgumentException("Attachment must have either a tokenId or a URL");
		if(data.getUrl() != null){
			// Is it a valid URL?
			try{
				URL url = new URL(data.getUrl());
			}catch (MalformedURLException e){
				throw new IllegalArgumentException("The attachment URL was malformed: "+data.getUrl(), e);
			}
		}
		if(data.getTokenId() != null){
			try{
				Long value = Long.parseLong(data.getTokenId());
			}catch(NumberFormatException e){
				throw new IllegalArgumentException("The attachment tokenId was not valid: "+data.getTokenId());
			}

		}
	}
	
	/**
	 * Is this content type a type that we create previews for?
	 * @param contentType
	 * @return
	 */
	static boolean isPreviewType(String fileName){
		if(fileName == null) return false;
		String[] split = fileName.split("\\.");
		if(split.length != 2) return false;
		return IMAGE_TYPES.contains(split[1].toUpperCase());
	}

}