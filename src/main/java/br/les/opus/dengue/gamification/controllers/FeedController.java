package br.les.opus.dengue.gamification.controllers;

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

import br.les.opus.gamification.domain.PerformedTask;
import br.les.opus.gamification.repositories.PerformedTaskRepository;

@RestController
@Transactional
@RequestMapping("/game")
public class FeedController {
	
	@Autowired
	public PerformedTaskRepository performedTaskDao;

	@RequestMapping(value = "feed", method = RequestMethod.GET)
	public ResponseEntity<PagedResources<Resource<PerformedTask>>> findAllOrderedByLevel(Pageable pageable,
			PagedResourcesAssembler<PerformedTask> assembler) {
		
		Page<PerformedTask> feed = performedTaskDao.findAllOrderedByDateDesc(pageable);
		PagedResources<Resource<PerformedTask>> resources = assembler.toResource(feed);
		return new ResponseEntity<PagedResources<Resource<PerformedTask>>>(resources, HttpStatus.OK);
	}
}
