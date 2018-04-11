package br.les.opus.dengue.gamification.controllers;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import br.les.opus.commons.persistence.filtering.Filter;
import br.les.opus.commons.rest.controllers.AbstractController;
import br.les.opus.commons.rest.exceptions.ValidationException;
import br.les.opus.dengue.core.domain.PerformedTaskComment;
import br.les.opus.gamification.domain.PerformedTask;
import br.les.opus.gamification.domain.Player;
import br.les.opus.gamification.repositories.PerformedTaskCommentRepository;
import br.les.opus.gamification.repositories.PerformedTaskRepository;
import br.les.opus.gamification.services.GamificationService;

@Controller
@Transactional
@RequestMapping("/game/performed-task")
public class PerformedTaskController extends AbstractController<PerformedTask> {
	
	@Autowired
	private PerformedTaskRepository taskDao;
	
	@Autowired
	private PerformedTaskCommentRepository commentDao;
	
	@Autowired
	private GamificationService gameService;
	
	@RequestMapping(value = "{taskId}/comment", method = RequestMethod.POST) 
	public ResponseEntity<PerformedTaskComment> insert(@RequestBody @Valid PerformedTaskComment newObject, BindingResult result, 
			HttpServletResponse response, HttpServletRequest request, @PathVariable Long taskId) {
		
		if (!taskDao.exists(taskId)) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
		PerformedTask task = taskDao.findOne(taskId);
		newObject.setId(null);
		newObject.setPerformedTask(task);
		Player player = gameService.loadPlayer(request);
		newObject.setPlayer(player);
		
		if (result.hasErrors()) {
			throw new ValidationException(result);
		}
		
		newObject = commentDao.save(newObject);
		return new ResponseEntity<>(newObject, HttpStatus.CREATED);
	}
	
	@RequestMapping(value = "{taskId}/comment", method = RequestMethod.GET) 
	public ResponseEntity< PagedResources<Resource<PerformedTaskComment>> > findAllComments(Pageable pageable, 
			PagedResourcesAssembler<PerformedTaskComment> assembler, @PathVariable Long taskId, HttpServletRequest request) {
		
		List<String> stringClause = new ArrayList<>();
		stringClause.add("performedTask.id=" + taskId);
		Filter filter = new Filter(stringClause, PerformedTaskComment.class);
		
		Page<PerformedTaskComment> page = commentDao.findAll(pageable, filter);
		PagedResources<Resource<PerformedTaskComment>> resources = assembler.toResource(page);
		return new ResponseEntity<PagedResources<Resource<PerformedTaskComment>>>(resources, HttpStatus.OK);
	}

}
