package br.les.opus.dengue.api.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
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
import br.les.opus.commons.persistence.PagingSortingFilteringRepository;
import br.les.opus.commons.persistence.filtering.Filter;
import br.les.opus.commons.rest.controllers.ReadOnlyController;
import br.les.opus.dengue.api.builders.FeatureCollectionBuilder;
import br.les.opus.twitter.domain.Tweet;
import br.les.opus.twitter.repositories.TweetRepository;

@RestController
@Transactional
@RequestMapping("/tweet")
public class TweetController extends ReadOnlyController<Tweet>{
	
	@Autowired
	private TweetRepository repository;
	
	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity< PagedResources<Resource<Tweet>> > findAll(Pageable pageable, PagedResourcesAssembler<Tweet> assembler, 
			@RequestParam(value = "filter", required = false) List<String> stringClause) {
		
		
		logger.info("Listando todos os valores");
		
		/**
		 * Cria o objeto de filtro antes de submeter a consulta ao repositório
		 */
		List<String> defaultClauses = new ArrayList<>();
		defaultClauses.add("geolocation!=null");
		defaultClauses.add("lang=pt");
		defaultClauses.add("classification!=null");
		defaultClauses.add("classification.id!=2");
		
		Filter filter = null;
		if (stringClause != null && !stringClause.isEmpty()) {
			filter = new Filter(stringClause, super.getEntityClass());
		} else {
			filter = new Filter(defaultClauses, super.getEntityClass());
		}
		
		Page<Tweet> page = getRepository().findAll(pageable, filter);
		PagedResources<Resource<Tweet>> resources = this.toPagedResources(page, assembler);
		return new ResponseEntity<PagedResources<Resource<Tweet>>>(resources, HttpStatus.OK);
	}
	
	@RequestMapping(value = "geojson", method = RequestMethod.GET)
	public ResponseEntity<FeatureCollection> findAllGeojson(Pageable pageable,
			@RequestParam(value = "filter", required = false) List<String> stringClause) {
		
		List<String> defaultClauses = new ArrayList<>();
		defaultClauses.add("geolocation!=null");
		defaultClauses.add("lang=pt");
		defaultClauses.add("classification!=null");
		defaultClauses.add("classification.id!=2");
		
		Filter filter = null;
		if (stringClause != null && !stringClause.isEmpty()) {
			filter = new Filter(stringClause, super.getEntityClass());
		} else {
			filter = new Filter(defaultClauses, super.getEntityClass());
		}
		Page<Tweet> page = repository.findAll(pageable, filter);
		
		FeatureCollectionBuilder builder = new FeatureCollectionBuilder();
		builder.addTweets(page.getContent());
		FeatureCollection featureCollection = builder.build();
		return new ResponseEntity<FeatureCollection>(featureCollection, HttpStatus.OK);
	}

	@Override
	protected PagingSortingFilteringRepository<Tweet, Long> getRepository() {
		return repository;
	}

}
