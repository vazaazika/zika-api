package br.les.opus.dengue.gamification.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.les.opus.commons.persistence.PagingSortingFilteringRepository;
import br.les.opus.commons.rest.controllers.AbstractCRUDController;
import br.les.opus.gamification.domain.Challenge;
import br.les.opus.gamification.repositories.ChallengeRepository;

@RestController
@Transactional
@RequestMapping("/game/challenge")
public class ChallengeController extends AbstractCRUDController<Challenge>{
	
	@Autowired
	private ChallengeRepository repository;

	@Override
	protected PagingSortingFilteringRepository<Challenge, Long> getRepository() {
		return repository;
	}

}
