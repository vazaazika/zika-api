package br.les.opus.dengue.gamification.controllers;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import br.les.opus.commons.persistence.PagingSortingFilteringRepository;
import br.les.opus.commons.rest.controllers.ReadOnlyController;
import br.les.opus.gamification.domain.Badge;
import br.les.opus.gamification.repositories.BadgeRepository;

@RestController
@Transactional
@RequestMapping("/game/badge")
public class BadgeController extends ReadOnlyController<Badge> {
	
	@Autowired
	private BadgeRepository repository;

	@Override
	protected PagingSortingFilteringRepository<Badge, Long> getRepository() {
		return repository;
	}
	
	@RequestMapping(value = "{id}/download", method = RequestMethod.GET)
	public ResponseEntity<Void> getFile(@PathVariable Long id, HttpServletResponse response){
		try {
			Badge badge = repository.findOne(id);
			
			if (badge == null) {
				return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
			}

			File file = new File(badge.getImageUrl());
			FileInputStream inputStream = new FileInputStream(file);
			IOUtils.copy(inputStream, response.getOutputStream());

			response.setHeader("Content-disposition", "attachment;filename=" + file.getName());
			response.setContentType("image/png");

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
			Badge badge = repository.findOne(id);
			
			if (badge == null) {
				return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
			}
			
			File file = new File(badge.getImageUrl());
			String basePath = badge.getImageUrl();

			InputStream inputStream = getScaledInstanceStream(basePath, width, height);
			IOUtils.copy(inputStream, response.getOutputStream());

			response.setHeader("Content-disposition", "attachment;filename="+ file.getName());
			response.setContentType("image/png");

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
	
	public InputStream getScaledInstanceStream(String basePath, int width, int height) throws IOException {
		File file = new File(basePath);
		
		BufferedImage bufferedImage = ImageIO.read(file);
		bufferedImage = Scalr.resize(bufferedImage, Scalr.Method.QUALITY, width, Scalr.OP_ANTIALIAS);
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ImageIO.write(bufferedImage, "png", os);
		InputStream inputStream = new ByteArrayInputStream(os.toByteArray());
		return inputStream;
	}
	
	@RequestMapping(value = "{id}/file", method = RequestMethod.GET)
	public ResponseEntity<Badge> getBadgeFile(@PathVariable Long id, HttpServletResponse response){
		Badge badge = null;
		try {
			badge = repository.findOne(id);
			
			if (badge == null) {
				return new ResponseEntity<Badge>(HttpStatus.NOT_FOUND);
			}

			File file = new File(badge.getImageUrl());
			
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream(1000);
			BufferedImage img = ImageIO.read(file);
			
			ImageIO.write(img, "png", baos);
			baos.flush();
			
			String base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());
			
			
			badge.setBase64Image(base64Image);
			baos.close();
		
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<Badge>(HttpStatus.NOT_FOUND);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<Badge>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<Badge>(badge, HttpStatus.OK);
	}

}
