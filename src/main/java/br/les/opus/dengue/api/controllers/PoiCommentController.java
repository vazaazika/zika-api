package br.les.opus.dengue.api.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import br.les.opus.auth.core.domain.Token;
import br.les.opus.auth.core.services.TokenService;
import br.les.opus.commons.rest.controllers.AbstractController;
import br.les.opus.commons.rest.exceptions.ValidationException;
import br.les.opus.dengue.core.domain.PoiComment;
import br.les.opus.dengue.core.domain.PoiCommentVote;
import br.les.opus.dengue.core.repositories.PoiCommentRepository;
import br.les.opus.dengue.core.services.VoteService;

@Controller
@Transactional
@RequestMapping("/comment")
public class PoiCommentController extends AbstractController<PoiComment> {
	
	private Logger logger = Logger.getLogger(getEntityClass());
	
	@Autowired
	private PoiCommentRepository commentRepository;
	
	@Autowired
	private TokenService tokenService;
	
	@Autowired
	private VoteService voteService;
	
	@RequestMapping(value="{id}",method=RequestMethod.DELETE) 
	public ResponseEntity<Void> deleteOne(@PathVariable Long id, HttpServletRequest request) {
		logger.info("Deletando objeto de id " + id);
		
		if (!commentRepository.exists(id)) {
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		}
		
		Token token = tokenService.getAuthenticatedUser(request);
		PoiComment comment = commentRepository.findOne(id);
		if (token.getUser().isRoot() || comment.isOwnedBy(token.getUser())) {
			commentRepository.delete(id);
			return new ResponseEntity<Void>(HttpStatus.OK);
		} else {
			return new ResponseEntity<Void>(HttpStatus.UNAUTHORIZED);
		}
	}
	
	@RequestMapping(value = "{id}/vote", method = RequestMethod.POST) 
	public ResponseEntity<PoiCommentVote> insert(@RequestBody @Valid PoiCommentVote vote, BindingResult result, 
			HttpServletResponse response, HttpServletRequest request, @PathVariable Long id) {
		if (result.hasErrors()) {
			throw new ValidationException(result);
		}
		
		if (!commentRepository.exists(id)) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
		Token token = tokenService.getAuthenticatedUser(request);
		PoiComment comment = commentRepository.findOne(id);
		vote.setUser(token.getUser());
		vote.setComment(comment);

		logger.info("Inserindo novo objeto " + vote);
		voteService.vote(comment, vote);
		return new ResponseEntity<>(vote, HttpStatus.OK);
	}
}
