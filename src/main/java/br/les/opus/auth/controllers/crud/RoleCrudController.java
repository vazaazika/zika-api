package br.les.opus.auth.controllers.crud;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.les.opus.auth.core.domain.Role;
import br.les.opus.auth.core.repositories.RoleRepository;
import br.les.opus.commons.persistence.PagingSortingFilteringRepository;
import br.les.opus.commons.rest.controllers.AbstractCRUDController;

@RestController
@Transactional
@RequestMapping("/role")
public class RoleCrudController extends AbstractCRUDController<Role> {
	
	@Autowired
	private RoleRepository repository;

	@Override
	protected PagingSortingFilteringRepository<Role, Long> getRepository() {
		return repository;
	}

}