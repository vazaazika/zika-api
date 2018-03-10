package br.les.opus.dengue.gamification.controllers;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import br.les.opus.gamification.domain.Badge;
import br.les.opus.gamification.domain.Player;
import br.les.opus.gamification.repositories.BadgeRepository;
import br.les.opus.gamification.services.GamificationService;

@RestController
@Transactional
@RequestMapping("/game/player")
public class PlayerController {
	
	@Autowired
	private BadgeRepository badgeDao;
	
	@Autowired
	private GamificationService gameService;

	@RequestMapping(value="badges", method = RequestMethod.GET) 
	public ResponseEntity< List<Badge> > findAllBadgesAndProgressions(HttpServletRequest request) {
		Player player = gameService.loadPlayer(request);
		
		List<Badge> badges = badgeDao.findAllWithProgressions(player);
		return new ResponseEntity<>(badges, HttpStatus.OK);
	}
}
