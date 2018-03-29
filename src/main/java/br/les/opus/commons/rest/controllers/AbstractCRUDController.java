package br.les.opus.commons.rest.controllers;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import br.les.opus.commons.persistence.IdAware;
import br.les.opus.commons.rest.exceptions.ValidationException;

@Transactional
public abstract class AbstractCRUDController<T extends IdAware<Long>> extends ReadOnlyController<T> {
	
	protected Logger logger = Logger.getLogger(getEntityClass());
	
	protected Validator getValidator() {
		return null;
	}
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		if (getValidator() != null) {
			binder.setValidator(getValidator());
		}
		super.initBinder(binder);
	}


	@RequestMapping(method = RequestMethod.POST) 
	public ResponseEntity<T> insert(@RequestBody @Valid T newObject, BindingResult result, HttpServletResponse response, HttpServletRequest request) {
		if (result.hasErrors()) {
			throw new ValidationException(result);
		}
		logger.info("Inserindo novo objeto " + newObject);
		newObject = (T)getRepository().save(newObject);
		Link detail = linkTo(this.getClass()).slash(newObject.getId()).withSelfRel();
		response.setHeader("Location", detail.getHref());
		return new ResponseEntity<T>(newObject, HttpStatus.CREATED);
	}

	@RequestMapping(value="/{id}",method=RequestMethod.POST) 
	public ResponseEntity<T> insertWithId(@PathVariable String id) {
		logger.info("Tentativa de POST em um objeto específico " + id);
		return new ResponseEntity<T>(HttpStatus.NOT_IMPLEMENTED);
	}

	@RequestMapping(method = RequestMethod.PUT) 
	public ResponseEntity<T> updateAll() {
		return new ResponseEntity<T>(HttpStatus.NOT_IMPLEMENTED);
	}

	@RequestMapping(value="/{id}", method=RequestMethod.PUT) 
	public ResponseEntity<T> updateOne(@RequestBody @Valid T t, @PathVariable Long id, BindingResult result, HttpServletRequest request) {
		if (result.hasErrors()) {
			throw new ValidationException(result);
		}
		
		logger.info("Atualizando objeto com id " + id + " com valores " + t);
		
		PagingAndSortingRepository<T, Long> repository = getRepository();
		if (!repository.exists(id)) {
			return new ResponseEntity<T>(HttpStatus.NOT_FOUND);
		}
		t.setId(id);
		
		/*
		 * Tratamento de validação.
		 */
		if (result.hasErrors()) {
			throw new ValidationException(result);
		}
		
		repository.save(t);
		return new ResponseEntity<T>(HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.DELETE) 
	public ResponseEntity<T> deleteAll() {
		logger.info("Tentativa de DELETE na raiz");
		return new ResponseEntity<T>(HttpStatus.NOT_IMPLEMENTED);
	}

	@RequestMapping(value="/{id}",method=RequestMethod.DELETE) 
	public ResponseEntity<T> deleteOne(@PathVariable Long id, HttpServletRequest request) {
		logger.info("Deletando objeto de id " + id);
		PagingAndSortingRepository<T, Long> repository = getRepository();
		if (!repository.exists(id)) {
			return new ResponseEntity<T>(HttpStatus.NOT_FOUND);
		}
		repository.delete(id);
		return new ResponseEntity<T>(HttpStatus.OK);
	}
}
