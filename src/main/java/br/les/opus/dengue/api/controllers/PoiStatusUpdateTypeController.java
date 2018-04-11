package br.les.opus.dengue.api.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;

import br.les.opus.commons.persistence.PagingSortingFilteringRepository;
import br.les.opus.commons.rest.controllers.AbstractCRUDController;
import br.les.opus.dengue.core.domain.PoiStatusUpdateType;
import br.les.opus.dengue.core.repositories.PoiStatusUpdateTypeRepository;

@Controller
@Transactional
@RequestMapping("/poi-status-update-type")
public class PoiStatusUpdateTypeController extends AbstractCRUDController<PoiStatusUpdateType> {
	
	@Autowired
	private PoiStatusUpdateTypeRepository repository;

	@Override
	protected PagingSortingFilteringRepository<PoiStatusUpdateType, Long> getRepository() {
		return repository;
	} 
	

}
