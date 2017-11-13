package br.les.opus.commons.rest.controllers;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import br.les.opus.commons.persistence.IdAware;
import br.les.opus.commons.persistence.PagingSortingFilteringRepository;
import br.les.opus.commons.persistence.filtering.Filter;

@Transactional
public abstract class ReadOnlyController<T extends IdAware<Long>> extends AbstractController<T>{
	
	protected Logger logger = Logger.getLogger(getEntityClass());
	
	protected abstract PagingSortingFilteringRepository<T, Long> getRepository();
	

	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity< PagedResources<Resource<T>> > findAll(Pageable pageable, PagedResourcesAssembler<T> assembler, 
			@RequestParam(value = "filter", required = false) List<String> stringClause) {
		
		
		logger.info("Listando todos os valores");
		
		/**
		 * Cria o objeto de filtro antes de submeter a consulta ao repositório
		 */
		Filter filter = null;
		if (stringClause != null && !stringClause.isEmpty()) {
			filter = new Filter(stringClause, super.getEntityClass());
		}
		
		Page<T> page = getRepository().findAll(pageable, filter);
		PagedResources<Resource<T>> resources = this.toPagedResources(page, assembler);
		return new ResponseEntity<PagedResources<Resource<T>>>(resources, HttpStatus.OK);
	}
	
	@RequestMapping(value="/{id}",method=RequestMethod.GET) 
	public ResponseEntity<T> findOne(@PathVariable Long id, HttpServletRequest request) {
		
		logger.info("Recuperando objeto com id: " + id);
		
		T t = (T)getRepository().findOne(id);
		t = doFiltering(t);
		if (t == null) {
			return new ResponseEntity<T>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<T>(t, HttpStatus.OK);
	}
	
}
