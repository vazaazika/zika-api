package br.les.opus.dengue.gamification.controllers;

import org.hibernate.Hibernate;
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

import com.fasterxml.jackson.annotation.JsonView;

import br.les.opus.dengue.core.json.View;
import br.les.opus.gamification.domain.PerformedTask;
import br.les.opus.gamification.repositories.PerformedTaskRepository;

@RestController
@Transactional
@RequestMapping("/game")
public class FeedController {
	
	@Autowired
	public PerformedTaskRepository performedTaskDao;

	@JsonView(View.PoiSummary.class)
	@RequestMapping(value = "feed", method = RequestMethod.GET)
	public ResponseEntity<PagedResources<Resource<PerformedTask>>> findAllOrderedByLevel(Pageable pageable,
			PagedResourcesAssembler<PerformedTask> assembler) {
		
		
		Page<PerformedTask> feed = performedTaskDao.findAllOrderedByDateDesc(pageable);
		PagedResources<Resource<PerformedTask>> resources = assembler.toResource(feed);
		for (Resource<PerformedTask> resource : resources.getContent()) {
			//TODO remover isso depois. O EAGER loading não está funcionando pro @Any
			Hibernate.initialize(resource.getContent().getObject()); 
		}
		return new ResponseEntity<PagedResources<Resource<PerformedTask>>>(resources, HttpStatus.OK);
	}
}
