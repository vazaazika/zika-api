package br.les.opus.dengue.gamification.controllers;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import br.les.opus.commons.persistence.PagingSortingFilteringRepository;
import br.les.opus.commons.rest.controllers.ReadOnlyController;
import br.les.opus.dengue.core.json.View;
import br.les.opus.gamification.domain.Badge;
import br.les.opus.gamification.domain.Membership;
import br.les.opus.gamification.domain.Player;
import br.les.opus.gamification.domain.PlayerInfo;
import br.les.opus.gamification.repositories.BadgeRepository;
import br.les.opus.gamification.repositories.IBGERepository;
import br.les.opus.gamification.repositories.PlayerRepository;
import br.les.opus.gamification.services.BadgeService;
import br.les.opus.gamification.services.GamificationService;
import br.les.opus.gamification.services.MembershipService;

@RestController
@Transactional
@RequestMapping("/game/player")
public class PlayerController extends ReadOnlyController<Player>{
	@Autowired
	private IBGERepository ibgeDao;
	
	@Autowired
	private BadgeRepository badgeDao;
	
	@Autowired
	private GamificationService gameService;
	
	@Autowired
	private PlayerRepository playerDao;
	
	@Autowired
	private MembershipService membershipService;
	
	@Autowired
	private BadgeService badgeService;

	@RequestMapping(value="self/badges", method = RequestMethod.GET) 
	public ResponseEntity< List<Badge> > findAllBadgesAndProgressionsSelf(HttpServletRequest request) {
		Player player = gameService.loadPlayer(request);
		
		List<Badge> badges = badgeDao.findAllWithProgressions(player);
		return new ResponseEntity<>(badges, HttpStatus.OK);
	}
	
	
	/*@RequestMapping(value="all", method = RequestMethod.GET) 
	public ResponseEntity<PagedResources<Resource<TeamUpChallenge>>> findAllPlayers(HttpServletRequest request) {
		Player player = gameService.loadPlayer(request);
		
		List<Badge> badges = badgeDao.findAllWithProgressions(player);
		return new ResponseEntity<>(badges, HttpStatus.OK);
	}*/

	
	@RequestMapping(value="{playerId}/badges", method = RequestMethod.GET) 
	public ResponseEntity< List<Badge> > findAllBadgesAndProgressionsPlayer(
			@PathVariable Long playerId, HttpServletRequest request) {
		Player player = playerDao.findOne(playerId);
		if (player == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
		List<Badge> badges = badgeDao.findAllWithProgressions(player);
		return new ResponseEntity<>(badges, HttpStatus.OK);
	}
	
	@RequestMapping(value="self/badges/icons", method = RequestMethod.GET) 
	public ResponseEntity< List<Badge> > findAllBadgesWithIconsAndProgressionsPlayer(HttpServletRequest request) {
		Player player = gameService.loadPlayer(request);
		if (player == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
		List<Badge> badges = badgeDao.findAllWithProgressions(player);
		badgeService.encodeIcons(badges);
		
		return new ResponseEntity<>(badges, HttpStatus.OK);
	}
	
	@RequestMapping(value="{playerId}", method = RequestMethod.GET) 
	public ResponseEntity<PlayerInfo> findPlayer(@PathVariable Long playerId) {
		Player player = playerDao.findOne(playerId);
		
		if (player == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
		Membership membership = membershipService.findCurrentMembership(player);
		
		/*
		 * We use the PlayerInfo here to avoid Infinite Recursion
		 * It's no the most elegant way to do it, but it works
		 */
		PlayerInfo playerInfo = new PlayerInfo(player);
		
		if (membership != null) {
			playerInfo.setTeam(membership.getTeam());
		}
		
		return new ResponseEntity<>(playerInfo, HttpStatus.OK);
	}
	
	@JsonView(View.Summary.class)
	@RequestMapping(value="self", method = RequestMethod.GET) 
	public ResponseEntity<PlayerInfo> findPlayerSelf(HttpServletRequest request) {
		Player player = gameService.loadPlayer(request);
		Membership membership = membershipService.findCurrentMembership(player);

		
		/*
		 * We use the PlayerInfo here to avoid Infinite Recursion
		 * It's no the most elegant way to do it, but it works
		 */
		PlayerInfo playerInfo = new PlayerInfo(player);
		
		if (membership != null) {
			playerInfo.setTeam(membership.getTeam());
		}
		return new ResponseEntity<>(playerInfo, HttpStatus.OK);
	}

	@Override
	protected PagingSortingFilteringRepository<Player, Long> getRepository() {
		// TODO Auto-generated method stub
		return playerDao;
	}
	
}
