package br.les.opus.dengue.api.controllers;

import br.les.opus.auth.core.domain.Token;
import br.les.opus.auth.core.domain.User;
import br.les.opus.auth.core.services.TokenService;
import br.les.opus.dengue.core.domain.PointOfInterest;
import br.les.opus.dengue.core.repositories.PointOfInterestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import br.les.opus.commons.persistence.PagingSortingFilteringRepository;
import br.les.opus.commons.rest.controllers.AbstractCRUDController;
import br.les.opus.dengue.core.domain.PoiStatusUpdateType;
import br.les.opus.dengue.core.repositories.PoiStatusUpdateTypeRepository;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;

@Controller
@Transactional
@RequestMapping("/poi-status-update-type")
public class PoiStatusUpdateTypeController extends AbstractCRUDController<PoiStatusUpdateType> {
	
	@Autowired
	private PoiStatusUpdateTypeRepository repository;

	@Autowired
	private PointOfInterestRepository poiRepository;

	@Autowired
	private TokenService tokenService;

	@Override
	protected PagingSortingFilteringRepository<PoiStatusUpdateType, Long> getRepository() {
		return repository;
	}








}
