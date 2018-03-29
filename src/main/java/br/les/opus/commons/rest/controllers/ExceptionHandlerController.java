package br.les.opus.commons.rest.controllers;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import br.les.opus.commons.rest.exceptions.ErrorList;
import br.les.opus.commons.rest.exceptions.ServiceError;
import br.les.opus.commons.rest.exceptions.ValidationError;
import br.les.opus.commons.rest.exceptions.ValidationException;

@ControllerAdvice
public class ExceptionHandlerController {
	
	private static Logger logger = Logger.getLogger(ExceptionHandlerController.class);
	
	private static final Long VALIDATION_ERROR = 1l;
	
	private static final Long UNEXPECTED_ERROR = 2l;
	
	private static final Long UNEXPECTED_CONTENT = 3l;
	
	private ResponseEntity<ErrorList> toResponseEntity(BindingResult resultado, Exception exception) {
		
		ErrorList erros = new ErrorList();
		for (ObjectError validationError : resultado.getAllErrors()) {
			ValidationError error = new ValidationError();
			error.setMessage(validationError.getDefaultMessage());
			error.setType(exception.getClass().getSimpleName());
			error.setCode(VALIDATION_ERROR);
			
			/*
			 * Se o erro for de validação, adicionamos mais detalhes
			 * à mensagem
			 */
			if (validationError instanceof FieldError) {
				FieldError fieldError = (FieldError)validationError;
				error.setField(fieldError.getField());
				
				Object valorRejeitadoObj = fieldError.getRejectedValue();
				String valorRejeitado = (valorRejeitadoObj == null)? null: valorRejeitadoObj.toString();
				error.setRejectedValue(valorRejeitado);
				error.setObjectLevelError(false);
			} else {
				error.setObjectLevelError(true);
			}
			
			erros.addError(error);
		}
		
		return new ResponseEntity<ErrorList>(erros, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(value = ValidationException.class)
	public ResponseEntity<ErrorList> validacaoHandler(HttpServletRequest request, ValidationException exception) {
		BindingResult resultado =  exception.getValidationResult();
		return toResponseEntity(resultado, exception);
	}
	
	@ExceptionHandler(value = MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorList> methodArgumentNotValidHandler(HttpServletRequest request, MethodArgumentNotValidException exception) {
		BindingResult resultado =  exception.getBindingResult();
		return toResponseEntity(resultado, exception);
	}
	
	@ExceptionHandler(value = HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorList> invalidRequestContent(HttpServletRequest request, Exception exception) {
		logger.error(exception.getMessage(), exception);
		
		ServiceError error = new ServiceError();
		error.setMessage("Make sure you sent a correct content when requesting.");
		error.setType(exception.getClass().getSimpleName());
		error.setCode(UNEXPECTED_CONTENT);
		
		ErrorList errors = new ErrorList();
		errors.addError(error);
		exception.printStackTrace();
		return new ResponseEntity<ErrorList>(errors, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(value = Exception.class)
	public ResponseEntity<ErrorList> defaultErrorHandler(HttpServletRequest request, Exception exception) {
		logger.error(exception.getMessage(), exception);
		
		ServiceError error = new ServiceError();
		error.setMessage(exception.getMessage());
		error.setType(exception.getClass().getSimpleName());
		error.setCode(UNEXPECTED_ERROR);
		
		ErrorList errors = new ErrorList();
		errors.addError(error);
		exception.printStackTrace();
		return new ResponseEntity<ErrorList>(errors, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
