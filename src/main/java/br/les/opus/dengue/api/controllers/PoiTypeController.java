package br.les.opus.dengue.api.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import br.les.opus.dengue.core.domain.PoiType;
import br.les.opus.dengue.core.repositories.PoiTypeRepository;

@RestController
@Transactional
@RequestMapping("/poi-type")
public class PoiTypeController {

	@Autowired
	private PoiTypeRepository repository;

	@RequestMapping(method = RequestMethod.GET) 
	public ResponseEntity< List<PoiType> > findAll() {
		return new ResponseEntity<>(repository.findAll(), HttpStatus.OK);
	}
}
