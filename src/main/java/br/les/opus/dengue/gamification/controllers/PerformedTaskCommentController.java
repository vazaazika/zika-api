package br.les.opus.dengue.gamification.controllers;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import br.les.opus.commons.rest.controllers.AbstractController;
import br.les.opus.dengue.core.domain.PerformedTaskComment;
import br.les.opus.gamification.domain.Player;
import br.les.opus.gamification.repositories.PerformedTaskCommentRepository;
import br.les.opus.gamification.services.GamificationService;

@Controller
@Transactional
@RequestMapping("/game/performed-task/comment")
public class PerformedTaskCommentController extends AbstractController<PerformedTaskComment> {
	
	private Logger logger = Logger.getLogger(getEntityClass());
	
	@Autowired
	private PerformedTaskCommentRepository commentDao;
	
	@Autowired
	private GamificationService gameService;
	
	@RequestMapping(value="{id}",method=RequestMethod.DELETE) 
	public ResponseEntity<Void> deleteOne(@PathVariable Long id, HttpServletRequest request) {
		logger.info("Deletando objeto de id " + id);
		
		if (!commentDao.exists(id)) {
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		}
		
		Player player = gameService.loadPlayer(request);
		PerformedTaskComment comment = commentDao.findOne(id);
		if (player.isRoot() || comment.isOwnedBy(player)) {
			commentDao.delete(id);
			return new ResponseEntity<Void>(HttpStatus.OK);
		} else {
			return new ResponseEntity<Void>(HttpStatus.UNAUTHORIZED);
		}
	}
	
}
