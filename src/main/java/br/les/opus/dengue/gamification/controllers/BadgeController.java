package br.les.opus.dengue.gamification.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
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

}
