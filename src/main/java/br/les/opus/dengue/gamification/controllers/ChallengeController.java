package br.les.opus.dengue.gamification.controllers;

import java.util.Arrays;
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

import br.les.opus.commons.persistence.PagingSortingFilteringRepository;
import br.les.opus.commons.rest.controllers.AbstractCRUDController;
import br.les.opus.gamification.domain.Membership;
import br.les.opus.gamification.domain.OnTop;
import br.les.opus.gamification.domain.Player;
import br.les.opus.gamification.domain.Team;
import br.les.opus.gamification.domain.challenge.Challenge;
import br.les.opus.gamification.domain.challenge.ChallengeEntity;
import br.les.opus.gamification.domain.challenge.ChallengeName;
import br.les.opus.gamification.domain.challenge.PerformedChallenge;
import br.les.opus.gamification.repositories.ChallengeEntityRepository;
import br.les.opus.gamification.repositories.ChallengeRepository;
import br.les.opus.gamification.repositories.OnTopRepository;
import br.les.opus.gamification.repositories.PerformedChallengeRepository;
import br.les.opus.gamification.services.GamificationService;
import br.les.opus.gamification.services.MembershipService;

@RestController
@Transactional
@RequestMapping("/game/challenge")
public class ChallengeController extends AbstractCRUDController<Challenge>{
	@Autowired
	private GamificationService gameService;
	
	@Autowired
	private ChallengeRepository repository;
	
	@Autowired
	private PerformedChallengeRepository pcDao;
	
	@Autowired
	private ChallengeEntityRepository entityDao;
	
	@Autowired
	private MembershipService membershipService;
	
	@Autowired
	private OnTopRepository onTopDao;

	@Override
	protected PagingSortingFilteringRepository<Challenge, Long> getRepository() {
		return repository;
	}
	
	@RequestMapping(value = "/self", method = RequestMethod.GET)
	public ResponseEntity<List<Challenge>> getChallengeStatus(HttpServletRequest request) {
		Player loggedPlayer = gameService.loadPlayer(request);
		
		if (!loggedPlayer.equals(loggedPlayer) && !loggedPlayer.isRoot()) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		
		List<Challenge> challenges = repository.findAllOpenedChallengesByPlayer(loggedPlayer.getId());
		List<Challenge> teamChallenges = repository.findAllOpenedChallengesByPlayerTeam(loggedPlayer);
		
		if(challenges == null && teamChallenges == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
		if(challenges != null && teamChallenges == null) {
			return new ResponseEntity<List<Challenge>>(challenges, HttpStatus.OK);
		}
		
		if(challenges == null && teamChallenges != null) {
			return new ResponseEntity<List<Challenge>>(teamChallenges, HttpStatus.OK);
		}
		
		challenges.addAll(teamChallenges);
		
		return new ResponseEntity<List<Challenge>>(challenges, HttpStatus.OK);
	}
	
	
	
	@RequestMapping(value = "/strike", method = RequestMethod.POST)
	public ResponseEntity<PerformedChallenge> challengeEnroll(HttpServletRequest request){
		Player loggedPlayer = gameService.loadPlayer(request);
		
		if (!loggedPlayer.equals(loggedPlayer) && !loggedPlayer.isRoot()) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		
		Challenge challenge = repository.findChallengeByName(ChallengeName.STRIKE.getName());
		
		if(challenge == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
				
		
		List<PerformedChallenge> pChallenges = pcDao.findAllIncompletePerformedChallengeByPlayerAndChallenge(loggedPlayer, challenge);
		
		
		//check if player is already enrolled in a challenge
		if(pChallenges != null && !pChallenges.isEmpty()) {
			return new ResponseEntity<>(pChallenges.get(0), HttpStatus.FORBIDDEN);
		}
		
		//enroll the player into the challenge
		return enroll(loggedPlayer, challenge);
	}
	
	@RequestMapping(value = "/ontop", method = RequestMethod.POST)
	public ResponseEntity<OnTop> challengeEnrollTeamOnTop( HttpServletRequest request){
		Player loggedPlayer = gameService.loadPlayer(request);
		
		if (!loggedPlayer.equals(loggedPlayer) && !loggedPlayer.isRoot()) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		
		//get the player team
		Membership membership = membershipService.findCurrentMembership(loggedPlayer);
		
		if(membership == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
		Team team = membership.getTeam();
		
		//verify if team is already on top, if not add it on the challenge
		OnTop onTop = onTopDao.findOneByTeam(team.getId());
		
		if(onTop == null) {
			onTop = new OnTop();
			onTop.setTeam(team);
			onTop =  onTopDao.save(onTop);
			
			return new ResponseEntity<OnTop>(onTop, HttpStatus.OK);
		}else {
			return new ResponseEntity<>(onTop, HttpStatus.FORBIDDEN);
		}
	}
	
	
	private ResponseEntity<PerformedChallenge> enroll(Player player, Challenge challenge){
		PerformedChallenge pfc = new PerformedChallenge();
		pfc.setChallenge(challenge);
		pfc = pcDao.save(pfc);
		
		ChallengeEntity entity = new ChallengeEntity(player.getId(), ChallengeEntity.PLAYERTYPE);
		entity.setPerformedChallenge(pfc);
		entity = entityDao.save(entity);
		
		pfc.setEntities(Arrays.asList(entity));
		
		return new ResponseEntity<PerformedChallenge>(pfc, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/strike/status", method = RequestMethod.GET)
	public ResponseEntity<PerformedChallenge> verifyStrikeStatus(HttpServletRequest request){
		Player loggedPlayer = gameService.loadPlayer(request);
		
		if (!loggedPlayer.equals(loggedPlayer) && !loggedPlayer.isRoot()) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		
		Challenge challenge = repository.findChallengeByName(ChallengeName.STRIKE.getName());
		
		if(challenge == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
				
		
		List<PerformedChallenge> pChallenges = pcDao.findAllIncompletePerformedChallengeByPlayerAndChallenge(loggedPlayer, challenge);
		
		
		//check if player is already enrolled in a challenge
		if(pChallenges != null && !pChallenges.isEmpty()) {
			return new ResponseEntity<>(pChallenges.get(0), HttpStatus.FOUND);
		}else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
	
	@RequestMapping(value = "/ontop/status", method = RequestMethod.GET)
	public ResponseEntity<OnTop> verifyOnTopStatus(HttpServletRequest request){
		Player loggedPlayer = gameService.loadPlayer(request);
		
		if (!loggedPlayer.equals(loggedPlayer) && !loggedPlayer.isRoot()) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		
		//get the player team
		Membership membership = membershipService.findCurrentMembership(loggedPlayer);
		
		if(membership == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
		Team team = membership.getTeam();
		
		//verify if team is already on top, if not add it on the challenge
		OnTop onTop = onTopDao.findOneByTeam(team.getId());
		
		if(onTop == null) {
			return new ResponseEntity<OnTop>(HttpStatus.NOT_FOUND);
		}else {
			return new ResponseEntity<>(onTop, HttpStatus.FOUND);
		}
	}
	
	
	
	/*@RequestMapping(value = "/self/constraint", method = RequestMethod.GET)
	public ResponseEntity<DurationConstraint> getChallengeTest(HttpServletRequest request) {
		Player loggedPlayer = gameService.loadPlayer(request);
		
		if (!loggedPlayer.equals(loggedPlayer) && !loggedPlayer.isRoot()) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		return new ResponseEntity<DurationConstraint>(repository.getDurationConstraint(8L), HttpStatus.OK);
		
	}*/
}
