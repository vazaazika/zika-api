package br.les.opus.dengue.gamification.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import br.les.opus.commons.rest.controllers.AbstractController;
import br.les.opus.gamification.domain.Player;
import br.les.opus.gamification.domain.pojos.RankedPlayer;
import br.les.opus.gamification.repositories.PlayerRepository;

@RestController
@Transactional
@RequestMapping("/game/ranking")
public class RankingController extends AbstractController<Player> {
	
	@Autowired
	private PlayerRepository playerDao;

	@RequestMapping(value = "level", method = RequestMethod.GET)
	public ResponseEntity<PagedResources<Resource<Player>>> findAllOrderedByLevel(Pageable pageable,
			PagedResourcesAssembler<Player> assembler) {

		Page<Player> page = playerDao.findOrderedByLevel(pageable);
		PagedResources<Resource<Player>> resources = this.toPagedResources(page, assembler);
		return new ResponseEntity<PagedResources<Resource<Player>>>(resources, HttpStatus.OK);
	}
	
	@RequestMapping(value = "report", method = RequestMethod.GET)
	public ResponseEntity<PagedResources<Resource<RankedPlayer>>> findAllOrderedByReportNumber(Pageable pageable,
			PagedResourcesAssembler<RankedPlayer> assembler) {

		Page<RankedPlayer> page = playerDao.findOrderedByReportNumber(pageable);
		PagedResources<Resource<RankedPlayer>> resources = assembler.toResource(page);
		return new ResponseEntity<PagedResources<Resource<RankedPlayer>>>(resources, HttpStatus.OK);
	}
	
	@RequestMapping(value = "verification", method = RequestMethod.GET)
	public ResponseEntity<PagedResources<Resource<RankedPlayer>>> findAllOrderedByVerificationNumber(Pageable pageable,
			PagedResourcesAssembler<RankedPlayer> assembler) {

		Page<RankedPlayer> page = playerDao.findOrderedByVerificationNumber(pageable);
		PagedResources<Resource<RankedPlayer>> resources = assembler.toResource(page);
		return new ResponseEntity<PagedResources<Resource<RankedPlayer>>>(resources, HttpStatus.OK);
	}
}
