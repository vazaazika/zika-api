package br.les.opus.auth.controllers;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class AuthExceptionHandlerController {
	
	private static Logger logger = Logger.getLogger(AuthExceptionHandlerController.class);
	
	@ExceptionHandler(value = BadCredentialsException.class)
	public ResponseEntity<Void> badCredentialsErrorHandler(HttpServletRequest request, Exception exception) {
		logger.error(exception.getMessage(), exception);
		return new ResponseEntity<Void>(HttpStatus.UNAUTHORIZED);
	}
}
