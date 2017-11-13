package br.les.opus.auth.controllers;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.les.opus.auth.core.domain.Token;
import br.les.opus.auth.core.domain.User;
import br.les.opus.auth.core.services.FacebookService;
import br.les.opus.auth.core.services.TokenService;
import br.les.opus.auth.core.services.UserService;
import facebook4j.FacebookException;

@RestController
@Transactional
@RequestMapping("/auth/facebook")
public class FacebookController {
	
	private Logger logger = Logger.getLogger(getClass());
	
	@Autowired
	private FacebookService fbService;
	
	@Autowired
	private TokenService tokenService;
	
	@Autowired
	private UserService userService;

	@RequestMapping(method=RequestMethod.GET) 
	public ResponseEntity<Token> facebookLogin(@RequestParam String accessToken,
			@RequestParam(defaultValue="true") Boolean longLasting) {
		try {
			User user = fbService.loadOrCreateUser(accessToken);
			Token token = tokenService.generateToken(user, true);
			user = userService.loadRolesAndResorces(user);
			tokenService.removeUnusedTokens(user);
			logger.info("Usuário autenticado via Facebook " + user);
			return new ResponseEntity<>(token, HttpStatus.CREATED);
		} catch (FacebookException e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
}
