package br.les.opus.auth.controllers;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.les.opus.auth.core.domain.Token;
import br.les.opus.auth.core.domain.User;
import br.les.opus.auth.core.repositories.TokenRepository;
import br.les.opus.auth.core.repositories.UserRepository;
import br.les.opus.auth.core.services.DatabaseAuthenticationProvider;
import br.les.opus.auth.core.services.TokenService;
import br.les.opus.auth.core.services.UserService;
import br.les.opus.auth.core.services.UsernamePasswordAuthenticationTokenBuilder;

@RestController
@Transactional
@RequestMapping("/auth")
public class AuthenticationController {
	
	@Autowired
	private DatabaseAuthenticationProvider authProvider;
	
	@Autowired
	private UsernamePasswordAuthenticationTokenBuilder authRequestBuilder;
	
	@Autowired
	private UserRepository userDao;
	
	@Autowired
	private TokenRepository tokenDao;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private TokenService tokenService;
	
	private Logger logger = Logger.getLogger(getClass());
	
	@RequestMapping(method=RequestMethod.GET) 
	public ResponseEntity<Token> performLogin(HttpServletRequest request, 
			@RequestParam(required=false, defaultValue = "false") Boolean longLasting) {
		try {
			UsernamePasswordAuthenticationToken authRequest = authRequestBuilder.build(request);
			logger.info("tentando autenticar usuário com login: " + authRequest.getPrincipal());
			authProvider.authenticate(authRequest);
			
			User user = userDao.findByUsername((String)authRequest.getPrincipal());
			
			Token token = new Token(authRequest);
			token.setUser(user);
			token.setLongLasting(longLasting);
			tokenDao.save(token);
			logger.info("usuário autenticado: " + authRequest.getPrincipal());

			userService.loadRolesAndResorces(user);
			tokenService.removeUnusedTokens(user);
			return new ResponseEntity<Token>(token, HttpStatus.CREATED);
		} catch (AuthenticationException e) {
			return new ResponseEntity<Token>(HttpStatus.UNAUTHORIZED);
		}
	}
	
	@RequestMapping(method=RequestMethod.DELETE) 
	public ResponseEntity<Token> revokeAllTokens(HttpServletRequest request) {
		try {
			UsernamePasswordAuthenticationToken authRequest = authRequestBuilder.build(request);
			logger.info("tentando revogar acesso a todos os tokens. Login: " + authRequest.getPrincipal());
			
			authProvider.authenticate(authRequest);
			User user = userDao.findByUsername((String)authRequest.getPrincipal());
			tokenDao.deleteAllFromUser(user);
			logger.info("tokens revogados do usuário " + authRequest.getPrincipal());
			return new ResponseEntity<Token>(HttpStatus.NO_CONTENT);
		} catch (AuthenticationException e) {
			return new ResponseEntity<Token>(HttpStatus.UNAUTHORIZED);
		}
	}
}
