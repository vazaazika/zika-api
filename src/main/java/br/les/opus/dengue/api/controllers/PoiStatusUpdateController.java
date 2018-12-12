package br.les.opus.dengue.api.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import br.les.opus.dengue.core.repositories.PoiStatusUpdateTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import com.fasterxml.jackson.annotation.JsonView;
import br.les.opus.auth.core.domain.Token;
import br.les.opus.auth.core.services.TokenService;
import br.les.opus.commons.persistence.PagingSortingFilteringRepository;
import br.les.opus.commons.rest.controllers.AbstractCRUDController;
import br.les.opus.dengue.core.domain.PoiStatusUpdateType;
import br.les.opus.dengue.core.json.View;

@Controller
@Transactional
@RequestMapping("/poi-status-type-update")
public class PoiStatusUpdateController extends AbstractCRUDController<PoiStatusUpdateType> {

	@Autowired
	private PoiStatusUpdateTypeRepository repository;

	@Autowired
	private TokenService tokenService;

	@JsonView(View.PoiDetails.class)
	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<PoiStatusUpdateType> insert(@RequestBody @Valid PoiStatusUpdateType newObject,
													  BindingResult result, HttpServletResponse response, HttpServletRequest request) {

		if (tokenService.hasAuthenticatedUser(request)) {
			Token token = tokenService.getAuthenticatedUser(request);
		}

		return super.insert(newObject, result, response, request);
	}

	@Override
	protected PagingSortingFilteringRepository<PoiStatusUpdateType, Long> getRepository() {
		return repository;
	}

}