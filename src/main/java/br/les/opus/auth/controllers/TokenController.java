package br.les.opus.auth.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import br.les.opus.auth.core.domain.Resource;
import br.les.opus.auth.core.domain.Token;
import br.les.opus.auth.core.repositories.TokenRepository;
import br.les.opus.auth.core.services.TokenService;

@RestController
@Transactional
@RequestMapping("/token")
public class TokenController {
	
	@Autowired
	private TokenRepository tokenDao;
	
	@Autowired
	private TokenService service;

	private Logger logger = Logger.getLogger(getClass());
	
	/**
	 * Verifica se um token tem acesso a um recurso específico de um aplicação
	 * @param request requisição HTTP
	 * @param targetResource recurso que se deseja acessar
	 * @return HttpStatus.OK em caso de sucesso ou HttpStatus.UNAUTHORIZED em caso de falha
	 */
	@RequestMapping(method=RequestMethod.GET) 
	public ResponseEntity<Void> checkTokenValidity(HttpServletRequest request, @Valid Resource targetResource) {
		Token token = service.getAuthenticatedUser(request);
		
		if (!token.hasGrantedPermission(targetResource)) {
			logger.info("Usuário " + token.getUser() + " sem acesso ao recurso " + targetResource);
			return new ResponseEntity<Void>(HttpStatus.UNAUTHORIZED);
		}
		
		logger.info("Usuário " + token.getUser() + " com acesso liberado ao recurso " + targetResource);
		return new ResponseEntity<Void>(HttpStatus.OK);
	}
	
	/**
	 * Revoga as permissões de um Token específico
	 * @param request requisição HTTP com o token informado
	 * @return
	 */
	@RequestMapping(method=RequestMethod.DELETE) 
	public ResponseEntity<Token> revokeToken(HttpServletRequest request) {
		Token token = service.getAuthenticatedUser(request);
		logger.info("Deletando token " + token);
		tokenDao.delete(token);
		return new ResponseEntity<Token>(HttpStatus.NO_CONTENT);
	}
}
