package br.les.opus.dengue.api.controllers;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.annotation.JsonView;

import br.les.opus.auth.core.domain.Token;
import br.les.opus.auth.core.domain.User;
import br.les.opus.auth.core.services.TokenService;
import br.les.opus.commons.persistence.PagingSortingFilteringRepository;
import br.les.opus.commons.rest.controllers.AbstractCRUDController;
import br.les.opus.commons.rest.geo.LatLng;
import br.les.opus.dengue.core.domain.PoiStatusUpdate;
import br.les.opus.dengue.core.domain.PoiStatusUpdateType;
import br.les.opus.dengue.core.domain.PointOfInterest;
import br.les.opus.dengue.core.json.View;
import br.les.opus.dengue.core.repositories.PoiStatusUpdateRepository;

@Controller
@Transactional
@RequestMapping("/poi-status-update")
public class PoiStatusUpdateController extends AbstractCRUDController<PoiStatusUpdate> {
	
	@Autowired
	private PoiStatusUpdateRepository repository; 
	
	@Autowired
	private TokenService tokenService;
	
	@JsonView(View.PoiDetails.class)
	@RequestMapping(method = RequestMethod.POST) 
	public ResponseEntity<PoiStatusUpdate> insert(@RequestBody @Valid PoiStatusUpdate newObject, 
			BindingResult result, HttpServletResponse response, HttpServletRequest request) {
		
		if (tokenService.hasAuthenticatedUser(request)) {
			Token token = tokenService.getAuthenticatedUser(request);
			newObject.setUser(token.getUser());
		}
		
		if (newObject.getUserLocation() != null) {
			newObject.getUserLocation().setSRID(LatLng.GOOGLE_SRID);
		}
		
		User user = newObject.getUser();
		PointOfInterest poi = newObject.getPoi();
		PoiStatusUpdateType type = newObject.getType();
		List<PoiStatusUpdate> updates = repository.findByUserAndPoi(user, poi, type);
		if (!updates.isEmpty()) {
			return new ResponseEntity<PoiStatusUpdate>(HttpStatus.BAD_REQUEST);
		}
		
		return super.insert(newObject, result, response, request);
	}
	
	@Override
	protected PagingSortingFilteringRepository<PoiStatusUpdate, Long> getRepository() {
		return repository;
	}

}
