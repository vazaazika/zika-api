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

import br.les.opus.gamification.domain.Badge;
import br.les.opus.gamification.domain.Membership;
import br.les.opus.gamification.domain.Player;
import br.les.opus.gamification.repositories.BadgeRepository;
import br.les.opus.gamification.repositories.PlayerRepository;
import br.les.opus.gamification.services.GamificationService;
import br.les.opus.gamification.services.MembershipService;

@RestController
@Transactional
@RequestMapping("/game/player")
public class PlayerController {
	
	@Autowired
	private BadgeRepository badgeDao;
	
	@Autowired
	private GamificationService gameService;

	@Autowired
	private PlayerRepository playerDao;

	@Autowired
	private MembershipService membershipService;

	@RequestMapping(value="self/badges", method = RequestMethod.GET) 
	public ResponseEntity< List<Badge> > findAllBadgesAndProgressionsSelf(HttpServletRequest request) {
		Player player = gameService.loadPlayer(request);
		
		List<Badge> badges = badgeDao.findAllWithProgressions(player);
		return new ResponseEntity<>(badges, HttpStatus.OK);
	}
	
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
	
	@RequestMapping(value="{playerId}", method = RequestMethod.GET) 
	public ResponseEntity<Player> findPlayer(@PathVariable Long playerId) {
		Player player = playerDao.findOne(playerId);
		if (player == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Membership membership = membershipService.findCurrentMembership(player);
		if (membership != null) {
			player.setTeam(membership.getTeam());
		}
		return new ResponseEntity<>(player, HttpStatus.OK);
	}
	
	@RequestMapping(value="self", method = RequestMethod.GET) 
	public ResponseEntity<Player> findPlayerSelf(HttpServletRequest request) {
		Player player = gameService.loadPlayer(request);
		Membership membership = membershipService.findCurrentMembership(player);
		if (membership != null) {
			player.setTeam(membership.getTeam());
		}
		return new ResponseEntity<>(player, HttpStatus.OK);
	}
}
