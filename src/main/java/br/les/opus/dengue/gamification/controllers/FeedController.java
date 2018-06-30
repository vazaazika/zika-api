package br.les.opus.dengue.gamification.controllers;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import br.les.opus.dengue.core.json.View;
import br.les.opus.gamification.domain.Membership;
import br.les.opus.gamification.domain.PerformedTask;
import br.les.opus.gamification.domain.Player;
import br.les.opus.gamification.domain.Team;
import br.les.opus.gamification.domain.TeamFeed;
import br.les.opus.gamification.repositories.MembershipRepository;
import br.les.opus.gamification.repositories.PerformedTaskRepository;
import br.les.opus.gamification.repositories.TeamRepository;

@RestController
@Transactional
@RequestMapping("/game")
public class FeedController{ //extends ReadOnlyController<PerformedTask>{
	
	@Autowired
	public PerformedTaskRepository performedTaskDao;
	
	@Autowired
	public TeamRepository teamDao;
	
	@Autowired
	public MembershipRepository membershipDao;

	@JsonView(View.PoiSummary.class)
	@RequestMapping(value = "/feed", method = RequestMethod.GET)
	public ResponseEntity<PagedResources<Resource<PerformedTask>>> findAllOrderedByLevel(Pageable pageable,
			PagedResourcesAssembler<PerformedTask> assembler) {
		
		
		Page<PerformedTask> feed = performedTaskDao.findAllPoiPerformedTaskOrderedByDataDesc(pageable);
		
		PagedResources<Resource<PerformedTask>> resources = assembler.toResource(feed);
		Collection<Resource<PerformedTask>> collection = resources.getContent();
		
		for (Resource<PerformedTask> object : collection) {
			Resource<PerformedTask> resource = object;
			PerformedTask t = resource.getContent();
			Link detail = linkTo(this.getClass()).slash("/feed/" + t.getId()).withSelfRel();
			resource.add(detail);
		}
		
		for (Resource<PerformedTask> resource : resources.getContent()) {
			//TODO remover isso depois. O EAGER loading não está funcionando pro @Any
			Hibernate.initialize(resource.getContent().getObject()); 
		}
		return new ResponseEntity<PagedResources<Resource<PerformedTask>>>(resources, HttpStatus.OK);
	}
	
	@JsonView(View.PoiSummary.class)
	@RequestMapping(value="/feed/{feedId}", method = RequestMethod.GET) 
	public ResponseEntity<PerformedTask> findElementFeed(@PathVariable Long feedId) {
		PerformedTask performedTask = performedTaskDao.findOne(feedId);
		
		if (performedTask == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
		Hibernate.initialize(performedTask.getObject());
		return new ResponseEntity<>(performedTask, HttpStatus.OK);
	}
	
	
	@RequestMapping(value = "/feed/team", method = RequestMethod.GET)
	public ResponseEntity<PagedResources<Resource<TeamFeed>>> findAllTeamsWithActiveMembers(Pageable pageable,
			PagedResourcesAssembler<TeamFeed> assembler) {
		
		Page<Team> teams = membershipDao.findAllMembershipActiveDistinctTeam(pageable);
		
		List<TeamFeed> content = new ArrayList<>();
		
		for(Team t: teams) {
			TeamFeed teamFeed = new TeamFeed(t);

			teamFeed.addMembers(getAllActiveMembers(t));
			
			content.add(teamFeed);
		}
		Page<TeamFeed> feed =  new PageImpl<>(content, pageable, teams.getTotalElements());

		//PagedResources<Resource<TeamFeed>> resources = assembler.toResource(feed);
		
		PagedResources<Resource<TeamFeed>> resources = assembler.toResource(feed);
		Collection<Resource<TeamFeed>> collection = resources.getContent();
		
		for (Resource<TeamFeed> object : collection) {
			Resource<TeamFeed> resource = object;
			TeamFeed t = resource.getContent();
			Link detail = linkTo(this.getClass()).slash("/feed/team/" + t.getTeam().getId()).withSelfRel();
			resource.add(detail);
		}
				
		return new ResponseEntity<PagedResources<Resource<TeamFeed>>>(resources, HttpStatus.OK);
	}
	
	@JsonView(View.PoiSummary.class)
	@RequestMapping(value="/feed/team/{teamId}", method = RequestMethod.GET) 
	public ResponseEntity<TeamFeed> findElementTeamFeed(@PathVariable Long teamId) {
		Team team = teamDao.findOne(teamId);
		
		if (team == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
		TeamFeed teamFeed = new TeamFeed(team);
		teamFeed.addMembers(getAllActiveMembers(team));
		
		return new ResponseEntity<>(teamFeed, HttpStatus.OK);
	}

	private List<Player> getAllActiveMembers(Team team) {
		List<Membership> memberships = team.getMemberships();
		
		List<Player> members = new ArrayList<>();
		
		for(Membership m: memberships) {
			members.add(m.getPlayer());
		}
		
		return members;
	}
	


}
