package br.les.opus.dengue.api.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import br.les.opus.auth.core.domain.Token;
import br.les.opus.auth.core.services.TokenService;
import br.les.opus.commons.persistence.PagingSortingFilteringRepository;
import br.les.opus.commons.rest.controllers.AbstractCRUDController;
import br.les.opus.dengue.core.domain.Picture;
import br.les.opus.dengue.core.repositories.PictureRepository;

@RestController
@Transactional
@RequestMapping("/picture")
@PropertySource(value = "classpath:dengue-api.properties")
public class PictureController extends AbstractCRUDController<Picture> {

	private Logger logger = Logger.getLogger(getClass());

	@Autowired
	private Environment env;

	@Autowired
	private PictureRepository pictureRepository;

	@Autowired
	private TokenService tokenService;


	public PictureController() {
	}

	@RequestMapping(value="upload", method=RequestMethod.POST)
	public ResponseEntity<Picture> handleFileUploadNewPicture(@RequestParam MultipartFile file, HttpServletRequest request)
			throws Exception {
		
		String basePath = this.env.getProperty("document.upload.directory");

		if (file.isEmpty()) {
			return new ResponseEntity<Picture>(HttpStatus.BAD_REQUEST);
		}

		Picture image = new Picture();
		image.generateUniqueName(file.getOriginalFilename());
		image.setContentType(file.getContentType());
		image.saveFileInFileSystem(basePath, file.getInputStream());
		image.retrieveDimentionsFromFile(basePath);

		try {
			Token token = tokenService.getAuthenticatedUser(request);
			image.setUser(token.getUser());
		} catch (Exception e) {
			logger.info("Enviando novo arquivo anonimamente " + file);
		}

		image = pictureRepository.save(image);
		return new ResponseEntity<Picture>(image, HttpStatus.OK);
	}


	@RequestMapping(value="{id}/upload", method=RequestMethod.POST)
	public ResponseEntity<Void> handleFileUpload(@PathVariable Long id, 
			@RequestParam MultipartFile file, HttpServletRequest request)  throws Exception {

		String basePath = this.env.getProperty("document.upload.directory");
		Picture document = pictureRepository.findOne(id);
		if (document == null) {
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		}
		if (!file.isEmpty()) {
			document.saveFileInFileSystem(basePath, file.getInputStream());

			try {
				Token token = tokenService.getAuthenticatedUser(request);
				document.setUser(token.getUser());
			} catch (Exception e) {
				logger.info("Enviando novo arquivo anonimamente " + file);
			}

			pictureRepository.save(document);
			return new ResponseEntity<Void>(HttpStatus.OK);
		} else {
			return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "{id}/download", method = RequestMethod.GET)
	public ResponseEntity<Void> getFile(@PathVariable Long id, HttpServletResponse response){
		try {
			String basePath = this.env.getProperty("document.upload.directory");
			Picture document = pictureRepository.findOne(id);

			String fileName = document.getFileName(basePath);
			File file = new File(fileName);
			FileInputStream inputStream = new FileInputStream(file);
			IOUtils.copy(inputStream, response.getOutputStream());

			response.setHeader("Content-disposition", "attachment;filename="+fileName);
			response.setContentType(document.getMimeType());

			response.flushBuffer();
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<Void>(HttpStatus.OK);
	}
	
	@RequestMapping(value = "{id}/thumb/{width}/{height}", method = RequestMethod.GET)
	public ResponseEntity<Void> getThumb(@PathVariable Long id, HttpServletResponse response, 
			@PathVariable Integer width, @PathVariable Integer height){
		try {
			String basePath = this.env.getProperty("document.upload.directory");
			Picture picture = pictureRepository.findOne(id);

			InputStream inputStream = picture.getScaledInstanceStream(basePath, width, height);
			IOUtils.copy(inputStream, response.getOutputStream());

			response.setHeader("Content-disposition", "attachment;filename="+picture.getFileName());
			response.setContentType(picture.getMimeType());

			response.flushBuffer();
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<Void>(HttpStatus.OK);
	}
	
	@RequestMapping(value = "{id}/thumb/{width}", method = RequestMethod.GET)
	public ResponseEntity<Void> getThumbByWidth(@PathVariable Long id, HttpServletResponse response, 
			@PathVariable Integer width){
		return getThumb(id, response, width, width);
	}
	
	@RequestMapping(value="/{id}",method=RequestMethod.DELETE) 
	public ResponseEntity<Picture> deleteOne(@PathVariable Long id, HttpServletRequest request) {
		
		Picture picture = pictureRepository.findOne(id);
		if (picture == null) {
			return new ResponseEntity<Picture>(HttpStatus.NOT_FOUND);
		}
		
		Token token = tokenService.getAuthenticatedUser(request);
		if (token != null && (token.getUser().isRoot() || picture.isOwnedBy(token.getUser()))) {
			pictureRepository.delete(id);
			return new ResponseEntity<Picture>(HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<Picture>(HttpStatus.UNAUTHORIZED);
		}
	}
	
	@Override
	
	@RequestMapping(value="/{id}", method=RequestMethod.PUT) 
	public ResponseEntity<Picture> updateOne(Picture t, Long id,
			BindingResult result, HttpServletRequest request) {
		return new ResponseEntity<Picture>(HttpStatus.NOT_IMPLEMENTED);
	}
	
	@Override
	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<Picture> insert(Picture newObject,
			BindingResult result, HttpServletResponse response,
			HttpServletRequest request) {
		return new ResponseEntity<Picture>(HttpStatus.NOT_IMPLEMENTED);
	}


	@Override
	protected PagingSortingFilteringRepository<Picture, Long> getRepository() {
		return pictureRepository;
	}
}
