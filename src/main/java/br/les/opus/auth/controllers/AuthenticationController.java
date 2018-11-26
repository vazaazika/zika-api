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
import br.les.opus.auth.core.domain.TokenPlayer;
import br.les.opus.auth.core.domain.User;
import br.les.opus.auth.core.repositories.TokenRepository;
import br.les.opus.auth.core.repositories.UserRepository;
import br.les.opus.auth.core.services.DatabaseAuthenticationProvider;
import br.les.opus.auth.core.services.TokenService;
import br.les.opus.auth.core.services.UserService;
import br.les.opus.auth.core.services.UsernamePasswordAuthenticationTokenBuilder;
import br.les.opus.gamification.domain.Membership;
import br.les.opus.gamification.domain.Player;
import br.les.opus.gamification.repositories.PlayerRepository;
import br.les.opus.gamification.services.MembershipService;

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

	@Autowired
	private PlayerRepository playerDao;

	@Autowired
	private MembershipService membershipService;

	private Logger logger = Logger.getLogger(getClass());

	@RequestMapping(method=RequestMethod.GET)
	public ResponseEntity<TokenPlayer> performLogin(HttpServletRequest request,
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


            TokenPlayer tokenPlayer = new TokenPlayer(authRequest);
            tokenPlayer.setUser(user);
            tokenPlayer.setLongLasting(longLasting);

            Player player = playerDao.findOne(user.getId());

            tokenPlayer.setPlayer(player);

            Membership membership = membershipService.findCurrentMembership(player);

            /*
             * We use the TokenPlayer here to avoid Infinite Recursion
             * It's no the most elegant way to do it, but it works
             */

            if (membership != null) {
                tokenPlayer.setTeam(membership.getTeam());
            }


            return new ResponseEntity<TokenPlayer>(tokenPlayer, HttpStatus.CREATED);
        } catch (AuthenticationException e) {
            return new ResponseEntity<TokenPlayer>(HttpStatus.UNAUTHORIZED);
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