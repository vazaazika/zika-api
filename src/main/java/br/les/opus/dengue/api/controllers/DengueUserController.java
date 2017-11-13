package br.les.opus.dengue.api.controllers;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import br.les.opus.auth.core.domain.User;
import br.les.opus.auth.core.repositories.UserRepository;
import br.les.opus.auth.core.services.TokenService;
import br.les.opus.commons.rest.controllers.AbstractController;
import br.les.opus.dengue.core.domain.PointOfInterest;
import br.les.opus.dengue.core.repositories.PointOfInterestRepository;

@RestController
@Transactional
@RequestMapping("/user")
public class DengueUserController extends AbstractController<User> {
	
	private Logger logger = Logger.getLogger(DengueUserController.class);
	
	@Autowired
	private PointOfInterestRepository poiRepository;
	
	@Autowired
	private TokenService tokenService;
	
	@Autowired
	private UserRepository userRepository;
	
	@RequestMapping(value = "poi", method = RequestMethod.GET)
	public ResponseEntity< PagedResources<Resource<PointOfInterest>> > findAllPois(Pageable pageable, 
			PagedResourcesAssembler<PointOfInterest> assembler, 
			HttpServletRequest request) {
		
		User user = tokenService.getAuthenticatedUser(request).getUser();
		return findAllPoisWithUserId(pageable, assembler, user.getId(), request);
	}
	
	@RequestMapping(value = "{userId}/poi", method = RequestMethod.GET)
	public ResponseEntity< PagedResources<Resource<PointOfInterest>> > findAllPoisWithUserId(Pageable pageable, 
			PagedResourcesAssembler<PointOfInterest> assembler, 
			@PathVariable Long userId,
			HttpServletRequest request) {
		
		User user = tokenService.getAuthenticatedUser(request).getUser();
		User targetUser = userRepository.findOne(userId);
		if (targetUser == null) {
			 new ResponseEntity<PagedResources<Resource<PointOfInterest>>>(HttpStatus.NOT_FOUND);
		}
		
		if (!user.isRoot() && !user.equals(targetUser)) {
			new ResponseEntity<PagedResources<Resource<PointOfInterest>>>(HttpStatus.UNAUTHORIZED);
		}
		
		logger.info("Listando todos os POIs reportados pelo usuário " + user);
		Page<PointOfInterest> page = poiRepository.findAllByUser(user, pageable);
		PagedResources<Resource<PointOfInterest>> resources = this.toPagedResources(page, assembler);
		return new ResponseEntity<PagedResources<Resource<PointOfInterest>>>(resources, HttpStatus.OK);
	}
}
