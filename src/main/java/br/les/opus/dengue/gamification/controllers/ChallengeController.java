package br.les.opus.dengue.gamification.controllers;

import java.util.ArrayList;
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
import br.les.opus.gamification.domain.challenge.ChallengeInvitation;
import br.les.opus.gamification.domain.challenge.ChallengeName;
import br.les.opus.gamification.domain.challenge.FightChallenge;
import br.les.opus.gamification.domain.challenge.InvitationStatus;
import br.les.opus.gamification.domain.challenge.OnTop;
import br.les.opus.gamification.domain.challenge.StrikeChallenge;
import br.les.opus.gamification.domain.challenge.TeamUpChallenge;
import br.les.opus.gamification.repositories.ChallengeRepository;
import br.les.opus.gamification.repositories.FightChallengeRepository;
import br.les.opus.gamification.repositories.OnTopRepository;
import br.les.opus.gamification.repositories.PlayerRepository;
import br.les.opus.gamification.repositories.StrikeChallengeRepository;
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
	private StrikeChallengeRepository strikeDao;

	
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
		
		List<Challenge> challenges = getPlayerChallenges(loggedPlayer);
		
		if(challenges.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
		return new ResponseEntity<List<Challenge>>(challenges, HttpStatus.OK);
	}
	
	private List<Challenge> getPlayerChallenges(Player player) {
		List<Challenge> challenges = new ArrayList<>();
		Challenge challenge = null;
		
		if(strikeDao.isPlayerEnrolledInChallenge(player)){
			challenge = repository.findChallengeByName(ChallengeName.STRIKE.getName());
			if(challenge != null) {
				challenges.add(challenge);
			}
			
		}
		
		if(fightDao.isPlayerEnrolledInChallenge(player)){
			challenge = repository.findChallengeByName(ChallengeName.FIGHT.getName());
			if(challenge != null) {
				challenges.add(challenge);
			}
		}
		
		if(onTopDao.isPlayerTeamEnrolledInOnTopChallenge(player)){
			challenge = repository.findChallengeByName(ChallengeName.ONTOP.getName());
			if(challenge != null) {
				challenges.add(challenge);
			}
		}
		
		if(tucDao.isPlayerEnrolledInChallenge(player)){
			challenge = repository.findChallengeByName(ChallengeName.TEAMUP.getName());
			if(challenge != null) {
				challenges.add(challenge);
			}
		}
		
		return challenges;
	}
	
	
	/* ******************************************************************************************
	 * 
	 * 										Strike Challenge
	 * 
	 * *****************************************************************************************/
	
	
	@RequestMapping(value = "/strike", method = RequestMethod.POST)
	public ResponseEntity<StrikeChallenge> challengeEnroll(HttpServletRequest request){
		Player loggedPlayer = gameService.loadPlayer(request);
		
		if (!loggedPlayer.equals(loggedPlayer) && !loggedPlayer.isRoot()) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		
		Challenge challenge = repository.findChallengeByName(ChallengeName.STRIKE.getName());
		
		if(challenge == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
		List<StrikeChallenge> challenges = strikeDao.findAllIncompleteStrikeChallengeByPlayer(loggedPlayer);
		
		
		//check if player is already enrolled in a challenge
		if(challenges != null && !challenges.isEmpty()) {
			return new ResponseEntity<>(challenges.get(0), HttpStatus.FORBIDDEN);
		}
		
		//enroll the player into the challenge
		return enroll(loggedPlayer);
	}
	
	private ResponseEntity<StrikeChallenge> enroll(Player player){
		StrikeChallenge strike = new StrikeChallenge(player);
		
		strike = strikeDao.save(strike);
		
		return new ResponseEntity<StrikeChallenge>(strike, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/strike/status", method = RequestMethod.GET)
	public ResponseEntity<StrikeChallenge> verifyStrikeStatus(HttpServletRequest request){
		Player loggedPlayer = gameService.loadPlayer(request);
		
		if (!loggedPlayer.equals(loggedPlayer) && !loggedPlayer.isRoot()) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		
		Challenge challenge = repository.findChallengeByName(ChallengeName.STRIKE.getName());
		
		if(challenge == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
				
		
		List<StrikeChallenge> pChallenges = strikeDao.findAllIncompleteStrikeChallengeByPlayer(loggedPlayer);
		
		
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
		
		//verify if team is already on top challenge
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
		
		TeamUpChallenge challenge = tucDao.findInCompletedByPlayers(challengerTeam, rivalTeam);
		
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
	
	
	
	
	@RequestMapping(value = "/teamup/status", method = RequestMethod.GET)
	public ResponseEntity<PagedResources<Resource<TeamUpChallenge>>> verifyOnlyTeamUpChallengeStatus(Pageable pageable,
			PagedResourcesAssembler<TeamUpChallenge> assembler, HttpServletRequest request){
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
		
				
		Page<TeamUpChallenge> page = tucDao.findOpenByTeam(challengerTeam, pageable);
		PagedResources<Resource<TeamUpChallenge>> resources = this.toPagedResources(page, assembler);
		
		return new ResponseEntity<PagedResources<Resource<TeamUpChallenge>>>(resources, HttpStatus.OK);
	}
	
}
