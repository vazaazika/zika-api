package br.les.opus.dengue.api.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.les.opus.commons.persistence.PagingSortingFilteringRepository;
import br.les.opus.commons.rest.controllers.AbstractCRUDController;
import br.les.opus.dengue.core.domain.Denunciation;
import br.les.opus.dengue.core.repositories.DenuntiationRepository;

@RestController
@Transactional
@RequestMapping("/poi-report")
public class DenunciationController extends AbstractCRUDController<Denunciation> {
	
	@Autowired
	private DenuntiationRepository repository;

	@Override
	protected PagingSortingFilteringRepository<Denunciation, Long> getRepository() {
		return repository;
	}

}
