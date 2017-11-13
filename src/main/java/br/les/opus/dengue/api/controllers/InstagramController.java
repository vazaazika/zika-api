package br.les.opus.dengue.api.controllers;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.les.opus.commons.geojson.FeatureCollection;
import br.les.opus.commons.persistence.filtering.Filter;
import br.les.opus.dengue.api.builders.FeatureCollectionBuilder;
import br.les.opus.instagram.domain.Media;
import br.les.opus.instagram.repository.MediaRepository;

@RestController
@Transactional
@RequestMapping("/instagram")
public class InstagramController {
	
	protected Logger logger = Logger.getLogger(InstagramController.class);
	
	@Autowired
	private MediaRepository repository;
	
	protected PagedResources<Resource<Media>> toPagedResources(Page<Media> page, PagedResourcesAssembler<Media> assembler) {
		PagedResources<Resource<Media>> resources = assembler.toResource(page);
		Collection<Resource<Media>> collection = resources.getContent();
		for (Resource<Media> object : collection) {
			Resource<Media> resource = (Resource<Media>)object;
			Media t = (Media)resource.getContent();
			Link detail = linkTo(this.getClass()).slash(t.getId()).withSelfRel();
			resource.add(detail);
		}
		return resources;
	}
	
	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity< PagedResources<Resource<Media>> > findAll(Pageable pageable, PagedResourcesAssembler<Media> assembler, 
			@RequestParam(value = "filter", required = false) List<String> stringClause) {
		
		
		logger.info("Listando todos os valores");
		
		/**
		 * Cria o objeto de filtro antes de submeter a consulta ao repositório
		 */
		List<String> defaultClauses = new ArrayList<>();
		defaultClauses.add("location!=null");
		
		Filter filter = null;
		if (stringClause != null && !stringClause.isEmpty()) {
			filter = new Filter(stringClause, Media.class);
		} else {
			filter = new Filter(defaultClauses, Media.class);
		}
		
		Page<Media> page = repository.findAll(pageable, filter);
		PagedResources<Resource<Media>> resources = this.toPagedResources(page, assembler);
		return new ResponseEntity<PagedResources<Resource<Media>>>(resources, HttpStatus.OK);
	}
	
	@RequestMapping(value = "geojson", method = RequestMethod.GET)
	public ResponseEntity<FeatureCollection> findAllGeojson(Pageable pageable,
			@RequestParam(value = "filter", required = false) List<String> stringClause) {
		
		List<String> defaultClauses = new ArrayList<>();
		defaultClauses.add("location!=null");
		
		Filter filter = null;
		if (stringClause != null && !stringClause.isEmpty()) {
			filter = new Filter(stringClause, Media.class);
		} else {
			filter = new Filter(defaultClauses, Media.class);
		}
		Page<Media> page = repository.findAll(pageable, filter);
		
		FeatureCollectionBuilder builder = new FeatureCollectionBuilder();
		builder.addInstagramMedias(page.getContent());
		FeatureCollection featureCollection = builder.build();
		return new ResponseEntity<FeatureCollection>(featureCollection, HttpStatus.OK);
	}


}
