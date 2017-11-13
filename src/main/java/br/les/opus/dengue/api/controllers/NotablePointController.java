package br.les.opus.dengue.api.controllers;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import br.les.opus.dengue.core.domain.NotablePoint;
import br.les.opus.dengue.core.repositories.NotablePointRepository;

@RestController
@Transactional
@RequestMapping("/notable-points")
public class NotablePointController {
	
	protected Logger logger = Logger.getLogger(NotablePointController.class);
	
	@Autowired
	private NotablePointRepository repository;
	
	@RequestMapping(value = "geojson", method = RequestMethod.GET)
	public ResponseEntity<FeatureCollection> findAllGeojson(Pageable pageable,
			@RequestParam(value = "filter", required = false) List<String> stringClause) {
		
		Filter filter = null;
		if (stringClause != null && !stringClause.isEmpty()) {
			filter = new Filter(stringClause, NotablePoint.class);
		} 
		
		Page<NotablePoint> page = repository.findAll(pageable, filter);
		
		FeatureCollectionBuilder builder = new FeatureCollectionBuilder();
		builder.addNotablePoints(page.getContent());
		FeatureCollection featureCollection = builder.build();
		return new ResponseEntity<FeatureCollection>(featureCollection, HttpStatus.OK);
	}


}
