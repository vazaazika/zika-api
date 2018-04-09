package br.les.opus.dengue.gamification.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import br.les.opus.commons.persistence.PagingSortingFilteringRepository;
import br.les.opus.commons.rest.controllers.AbstractCRUDController;
import br.les.opus.commons.rest.exceptions.ValidationException;
import br.les.opus.gamification.domain.Membership;
import br.les.opus.gamification.domain.Player;
import br.les.opus.gamification.domain.Team;
import br.les.opus.gamification.repositories.PlayerRepository;
import br.les.opus.gamification.repositories.TeamRepository;
import br.les.opus.gamification.services.GamificationService;
import br.les.opus.gamification.services.MembershipService;

@RestController
@Transactional
@RequestMapping("/game/team")
public class TeamController extends AbstractCRUDController<Team> {
	
	@Autowired
	private TeamRepository repository;
	
	@Autowired
	private GamificationService gameService;
	
	@Autowired
	private MembershipService membershipService;
	
	@Autowired
	private PlayerRepository playerDao;
	
	private boolean canProceed(HttpServletRequest request, Long teamId) {
		Player player = gameService.loadPlayer(request);
		Team team = repository.findOne(teamId);
		return team.wasCreatedBy(player) || player.isRoot();
	}
	
	@RequestMapping(value = "{teamId}/members", method = RequestMethod.GET)
	public ResponseEntity<PagedResources<Resource<Player>>> findAllOrderedByLevel(Pageable pageable,
			PagedResourcesAssembler<Player> assembler, @PathVariable Long teamId) {

		Page<Player> page = repository.findAllActiveMembers(pageable, teamId);
		PagedResources<Resource<Player>> resources = this.toPagedResources(page, assembler);
		return new ResponseEntity<PagedResources<Resource<Player>>>(resources, HttpStatus.OK);
	}
	
	@RequestMapping(value = "{teamId}/members/{playerId}", method = RequestMethod.POST)
	public ResponseEntity<Membership> addMember(@PathVariable Long playerId, @PathVariable Long teamId, HttpServletRequest request) {
		Player loggedPlayer = gameService.loadPlayer(request);
		Player player = playerDao.findOne(playerId);
		if (!loggedPlayer.equals(player) && !loggedPlayer.isRoot()) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		
		Team team = repository.findOne(teamId);
		if (team == null || player == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
		Membership membership = membershipService.addMember(team, player);
		return new ResponseEntity<>(membership, HttpStatus.CREATED);
	}
	
	@RequestMapping(value = "{teamId}/members/self", method = RequestMethod.POST)
	public ResponseEntity<Membership> addMemberSelf(@PathVariable Long teamId, HttpServletRequest request) {
		Player loggedPlayer = gameService.loadPlayer(request);
		return addMember(loggedPlayer.getId(), teamId, request);
	}
	
	@RequestMapping(value = "{teamId}/members/{playerId}", method = RequestMethod.DELETE)
	public ResponseEntity<Void> removeMember(@PathVariable Long playerId, @PathVariable Long teamId, HttpServletRequest request) {
		Player loggedPlayer = gameService.loadPlayer(request);
		Player player = playerDao.findOne(playerId);
		if (!loggedPlayer.equals(player) && !loggedPlayer.isRoot()) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		
		Team team = repository.findOne(teamId);
		if (team == null || player == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
		membershipService.removeMember(team, player);
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@RequestMapping(value = "{teamId}/members/self", method = RequestMethod.DELETE)
	public ResponseEntity<Void> removeMemberSelf(@PathVariable Long teamId, HttpServletRequest request) {
		Player loggedPlayer = gameService.loadPlayer(request);
		return removeMember(loggedPlayer.getId(), teamId, request);
	}
	
	@Override
	public ResponseEntity<Team> insert(@RequestBody @Valid Team newObject, BindingResult result, HttpServletResponse response,
			HttpServletRequest request) {
		Player player = gameService.loadPlayer(request);
		newObject.setCreator(player);
		ResponseEntity<Team> responseEntity = super.insert(newObject, result, response, request);
		Team team = responseEntity.getBody();
		membershipService.addMember(team, player);
		return responseEntity;
	}
	
	@Override
	public ResponseEntity<Team> updateOne(@RequestBody @Valid Team t, @PathVariable Long id, BindingResult result, HttpServletRequest request) {
		if (result.hasErrors()) {
			throw new ValidationException(result);
		}
		if (!canProceed(request, id)) {
			return new ResponseEntity<Team>(HttpStatus.UNAUTHORIZED);
		}
		Player player = gameService.loadPlayer(request);
		t.setCreator(player);
		return super.updateOne(t, id, result, request);
	}
	
	@Override
	public ResponseEntity<Team> deleteOne(@PathVariable Long id, HttpServletRequest request) {
		if (!canProceed(request, id)) {
			return new ResponseEntity<Team>(HttpStatus.UNAUTHORIZED);
		}
		return super.deleteOne(id, request);
	}
	

	@Override
	protected PagingSortingFilteringRepository<Team, Long> getRepository() {
		return repository;
	}

}
