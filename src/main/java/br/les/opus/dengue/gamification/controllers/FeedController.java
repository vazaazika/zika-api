package br.les.opus.dengue.gamification.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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

import com.fasterxml.jackson.annotation.JsonView;

import br.les.opus.dengue.core.json.View;
import br.les.opus.gamification.domain.Membership;
import br.les.opus.gamification.domain.PerformedTask;
import br.les.opus.gamification.domain.Team;
import br.les.opus.gamification.domain.TeamFeed;
import br.les.opus.gamification.repositories.MembershipRepository;
import br.les.opus.gamification.repositories.PerformedTaskRepository;
import br.les.opus.gamification.repositories.TeamRepository;

@RestController
@Transactional
@RequestMapping("/game")
public class FeedController {
	
	@Autowired
	public PerformedTaskRepository performedTaskDao;
	
	@Autowired
	public TeamRepository teamDao;
	
	@Autowired
	public MembershipRepository membershipDao;

	@JsonView(View.PoiSummary.class)
	@RequestMapping(value = "feed", method = RequestMethod.GET)
	public ResponseEntity<PagedResources<Resource<PerformedTask>>> findAllOrderedByLevel(Pageable pageable,
			PagedResourcesAssembler<PerformedTask> assembler) {
		
		
		Page<PerformedTask> feed = performedTaskDao.findAllPoiPerformedTaskOrderedByDataDesc(pageable);
		PagedResources<Resource<PerformedTask>> resources = assembler.toResource(feed);
		for (Resource<PerformedTask> resource : resources.getContent()) {
			//TODO remover isso depois. O EAGER loading não está funcionando pro @Any
			Hibernate.initialize(resource.getContent().getObject()); 
		}
		return new ResponseEntity<PagedResources<Resource<PerformedTask>>>(resources, HttpStatus.OK);
	}
	
	@JsonView(View.PoiSummary.class)
	@RequestMapping(value = "feed/team", method = RequestMethod.GET)
	public ResponseEntity<PagedResources<Resource<TeamFeed>>> findAllTeamsWithActiveMembers(Pageable pageable,
			PagedResourcesAssembler<TeamFeed> assembler) {
		
		List<Membership> memberships = membershipDao.findAll();
		
		HashMap<Long, TeamFeed> hashFeed = new HashMap<>();
		
		for(Membership membership: memberships) {
			Team team = membership.getTeam();
			
			TeamFeed obj = hashFeed.get(team.getId());
			
			if(obj == null) {
				obj = new TeamFeed(team, membership.getPlayer());
				hashFeed.put(team.getId(), obj);
			}else {
				obj.addMember(membership.getPlayer());
			}
		}
		List<TeamFeed> content = new ArrayList<>(hashFeed.values());
		Page<TeamFeed> feed =  new PageImpl<>(content, pageable, content.size());

		PagedResources<Resource<TeamFeed>> resources = assembler.toResource(feed);
				
		return new ResponseEntity<PagedResources<Resource<TeamFeed>>>(resources, HttpStatus.OK);
	}
}
