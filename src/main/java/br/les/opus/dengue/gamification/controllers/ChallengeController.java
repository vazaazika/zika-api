package br.les.opus.dengue.gamification.controllers;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
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
import br.les.opus.gamification.domain.Player;
import br.les.opus.gamification.domain.Team;
import br.les.opus.gamification.domain.challenge.Challenge;
import br.les.opus.gamification.domain.challenge.ChallengeEntity;
import br.les.opus.gamification.domain.challenge.ChallengeInvitation;
import br.les.opus.gamification.domain.challenge.ChallengeName;
import br.les.opus.gamification.domain.challenge.FightChallenge;
import br.les.opus.gamification.domain.challenge.InvitationStatus;
import br.les.opus.gamification.domain.challenge.OnTop;
import br.les.opus.gamification.domain.challenge.PerformedChallenge;
import br.les.opus.gamification.domain.challenge.TeamUpChallenge;
import br.les.opus.gamification.repositories.ChallengeEntityRepository;
import br.les.opus.gamification.repositories.ChallengeRepository;
import br.les.opus.gamification.repositories.FightChallengeRepository;
import br.les.opus.gamification.repositories.OnTopRepository;
import br.les.opus.gamification.repositories.PerformedChallengeRepository;
import br.les.opus.gamification.repositories.PlayerRepository;
import br.les.opus.gamification.repositories.TeamRepository;
import br.les.opus.gamification.repositories.TeamUpChallengeRepository;
import br.les.opus.gamification.services.ChallengeService;
import br.les.opus.gamification.services.GamificationService;
import br.les.opus.gamification.services.MembershipService;

@RestController
@Transactional
@RequestMapping("/game/challenge")
public class ChallengeController extends AbstractCRUDController<Challenge>{
	@Autowired
	private GamificationService gameService;
	
	@Autowired
	private ChallengeService challengeService;
	
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
	
	@Autowired
	private PlayerRepository playerDao;
	
	@Autowired
	private FightChallengeRepository fightDao;
	
	@Autowired
	private TeamRepository teamDao;
	
	@Autowired
	private TeamUpChallengeRepository tucDao;

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
	
	
	/* ******************************************************************************************
	 * 
	 * 										Strike Challenge
	 * 
	 * *****************************************************************************************/
	
	
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
	
	/* ******************************************************************************************
	 * 
	 * 										Fight Challenge
	 * 
	 * *****************************************************************************************/
	
	@RequestMapping(value = "/fight/{playerId}", method = RequestMethod.POST)
	public ResponseEntity<FightChallenge> enrollFightChallenge(@PathVariable Long playerId, HttpServletRequest request){
		Player loggedPlayer = gameService.loadPlayer(request);
		Player rival = playerDao.findOne(playerId);
				
		//verify players
		if (!loggedPlayer.equals(loggedPlayer) && !loggedPlayer.isRoot()) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		
		if (rival == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
		if(loggedPlayer.equals(rival)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
				
		FightChallenge challenge = fightDao.findOneByPlayers(loggedPlayer, rival);
		
		
		if(challenge != null) {
			return new ResponseEntity<>(challenge, HttpStatus.FORBIDDEN);
		}
		
		challenge = new FightChallenge(loggedPlayer, rival);
		challenge = fightDao.save(challenge);
		
		challengeService.sendInvitationFightChallente(loggedPlayer, rival, challenge.getId());

		return new ResponseEntity<>(challenge, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/fight/accept/{fcId}", method = RequestMethod.POST)
	public ResponseEntity<String> acceptFightChallenge(@PathVariable Long fcId, HttpServletRequest request){
		ChallengeInvitation invitation = new ChallengeInvitation();
		
		FightChallenge fc = fightDao.findOne(fcId);
		
		if(fc == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
		Player rival = fc.getRival();
		
		Challenge fightChallenge = repository.findChallengeByName(ChallengeName.FIGHT.getName()); 
		
		if(!fc.getStatus().equals(InvitationStatus.ONHOLD.getValue())) {
			return invitation.getAlreadyEnrolledMessage(rival, fightChallenge);
		}
		
		if(!challengeService.checkValidDate(fc.getCreatedDate())) {
			fc.setStatus(InvitationStatus.EXPIRED.getValue());
			
			return invitation.getExpirationMessage(rival, fightChallenge);
		}
		
		fc.setStartDate(new Date());
		fc.setStatus(InvitationStatus.ACCEPTED.getValue());
		
		return invitation.getAcceptanceMessage(rival, fightChallenge);
	}
	
	@RequestMapping(value = "/fight/{playerId}/status", method = RequestMethod.GET)
	public ResponseEntity<PagedResources<Resource<FightChallenge>>> verifyFightChallengeStatus(Pageable pageable,
			PagedResourcesAssembler<FightChallenge> assembler, @PathVariable Long playerId, HttpServletRequest request){
		Player loggedPlayer = gameService.loadPlayer(request);
		Player rival = playerDao.findOne(playerId);
				
		//verify players
		if (!loggedPlayer.equals(loggedPlayer) && !loggedPlayer.isRoot()) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		
		if (rival == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
		if(loggedPlayer.equals(rival)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
				
		Page<FightChallenge> page = fightDao.findAllByPlayers(loggedPlayer, rival, pageable);
		PagedResources<Resource<FightChallenge>> resources = this.toPagedResources(page, assembler);
		return new ResponseEntity<PagedResources<Resource<FightChallenge>>>(resources, HttpStatus.OK);
	}
	
	/* ******************************************************************************************
	 * 
	 * 										OnTop Challenge
	 * 
	 * *****************************************************************************************/
	
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
	
	/* ******************************************************************************************
	 * 
	 * 										TeamUp Challenge
	 * 
	 * *****************************************************************************************/
	
	@RequestMapping(value = "/teamup/{teamId}", method = RequestMethod.POST)
	public ResponseEntity<TeamUpChallenge> enrollTeamUpChallenge(@PathVariable Long teamId, HttpServletRequest request){
		Player loggedPlayer = gameService.loadPlayer(request);
		Team rivalTeam = teamDao.findOne(teamId);
				
		//verify player
		if (!loggedPlayer.equals(loggedPlayer) && !loggedPlayer.isRoot()) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		
		//get the player team
		Membership membership = membershipService.findCurrentMembership(loggedPlayer);
		
		if(membership == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
		Team challengerTeam = membership.getTeam();
		
		
		if (rivalTeam == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
		if(challengerTeam.equals(rivalTeam)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		
		TeamUpChallenge challenge = tucDao.findOneByPlayers(challengerTeam, rivalTeam);
		
		if(challenge != null) {
			return new ResponseEntity<>(challenge, HttpStatus.FORBIDDEN);
		}
		
		challenge = new TeamUpChallenge(challengerTeam, rivalTeam);
		challenge = tucDao.save(challenge);
		
		challengeService.sendInvitationTeamUpChallente(loggedPlayer, challengerTeam, rivalTeam, challenge.getId());

		return new ResponseEntity<>(challenge, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/teamup/accept/{tuId}", method = RequestMethod.GET)
	public ResponseEntity<String> acceptTeamUpChallenge(@PathVariable Long tuId, HttpServletRequest request){
		ChallengeInvitation invitation = new ChallengeInvitation();
		
		TeamUpChallenge tu = tucDao.findOne(tuId);
		
		if(tu == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
		Challenge challenge = repository.findChallengeByName(ChallengeName.TEAMUP.getName()); 
		
		if(!tu.getStatus().equals(InvitationStatus.ONHOLD.getValue())) {
			return invitation.getAlreadyEnrolledTeamMessage(challenge);
		}
		
		if(!challengeService.checkValidDate(tu.getCreatedDate())) {
			tu.setStatus(InvitationStatus.EXPIRED.getValue());
			
			return invitation.getTeamExpirationMessage(challenge);
		}
		
		tu.setStartDate(new Date());
		tu.setStatus(InvitationStatus.ACCEPTED.getValue());
		
		return invitation.getTeamAcceptanceMessage(challenge);
	}
	
	
	@RequestMapping(value = "/teamup/{teamId}/status", method = RequestMethod.GET)
	public ResponseEntity<PagedResources<Resource<TeamUpChallenge>>> verifyTeamUpChallengeStatus(Pageable pageable,
			PagedResourcesAssembler<TeamUpChallenge> assembler, @PathVariable Long teamId, HttpServletRequest request){
		Player loggedPlayer = gameService.loadPlayer(request);
		
				
		//verify players
		if (!loggedPlayer.equals(loggedPlayer) && !loggedPlayer.isRoot()) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		
		//get the player team
		Membership membership = membershipService.findCurrentMembership(loggedPlayer);
		
		if(membership == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
		Team challengerTeam = membership.getTeam();
		
		Team rivalTeam = teamDao.findOne(teamId);
		
		if (rivalTeam == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
		if(challengerTeam.equals(rivalTeam)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
				
		Page<TeamUpChallenge> page = tucDao.findAllByTeams(challengerTeam, rivalTeam, pageable);
		PagedResources<Resource<TeamUpChallenge>> resources = this.toPagedResources(page, assembler);
		return new ResponseEntity<PagedResources<Resource<TeamUpChallenge>>>(resources, HttpStatus.OK);
	}
	
}
