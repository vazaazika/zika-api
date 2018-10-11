package br.les.opus.dengue.api.controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import br.les.opus.dengue.core.domain.enumeration.PoiStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.annotation.JsonView;

import br.les.opus.auth.core.domain.Token;
import br.les.opus.auth.core.domain.User;
import br.les.opus.auth.core.services.TokenService;
import br.les.opus.commons.geojson.FeatureCollection;
import br.les.opus.commons.persistence.PagingSortingFilteringRepository;
import br.les.opus.commons.persistence.filtering.Filter;
import br.les.opus.commons.persistence.spatial.DistanceResult;
import br.les.opus.commons.rest.controllers.AbstractCRUDController;
import br.les.opus.commons.rest.exceptions.ValidationException;
import br.les.opus.commons.rest.geo.LatLng;
import br.les.opus.dengue.api.builders.FeatureCollectionBuilder;
import br.les.opus.dengue.core.domain.Picture;
import br.les.opus.dengue.core.domain.PoiComment;
import br.les.opus.dengue.core.domain.PoiCommentVote;
import br.les.opus.dengue.core.domain.PoiVote;
import br.les.opus.dengue.core.domain.PointOfInterest;
import br.les.opus.dengue.core.domain.Vote;
import br.les.opus.dengue.core.fields.FieldValue;
import br.les.opus.dengue.core.json.View;
import br.les.opus.dengue.core.repositories.PictureRepository;
import br.les.opus.dengue.core.repositories.PoiCommentRepository;
import br.les.opus.dengue.core.repositories.PoiCommentVoteRepository;
import br.les.opus.dengue.core.repositories.PoiVoteRepository;
import br.les.opus.dengue.core.repositories.PointOfInterestRepository;
import br.les.opus.dengue.core.services.VoteService;
import br.les.opus.gamification.services.PerformedTaskService;

@Controller
@Transactional
@RequestMapping("/poi")
public class PointOfInterestController extends AbstractCRUDController<PointOfInterest>{
	
	@Autowired
	private PointOfInterestRepository poiRepository;
	
	@Autowired
	private PictureRepository documentRepository;
	
	@Autowired
	private TokenService tokenService;
	
	@Autowired
	private PoiCommentRepository commentRepository;
	
	@Autowired
	private PoiCommentVoteRepository voteRepository;
	
	@Autowired
	private Environment env;
	
	@Autowired
	private VoteService voteService;
	
	@Autowired
	private PoiVoteRepository poiVoteRepository;

	@Override
	protected PagingSortingFilteringRepository<PointOfInterest, Long> getRepository() {
		return poiRepository;
	}
	
	@JsonView(View.PoiSummary.class)
	@RequestMapping(value = "nearby", method = RequestMethod.GET)
	public ResponseEntity<PagedResources<Resource<DistanceResult>>> findAllNearby(
			@Valid LatLng origin,
			Pageable pageable, 
			PagedResourcesAssembler<DistanceResult> assembler, 
			BindingResult result,
			@RequestParam(value = "filter", required = false) List<String> stringClause) {
		
		if (result.hasErrors()) {
			throw new ValidationException(result);
		}
		
		Page<DistanceResult> page = poiRepository.findAllOrderringByDistance(origin.toPoint(), pageable);
		PagedResources<Resource<DistanceResult>> resources = assembler.toResource(page);
		return new ResponseEntity<PagedResources<Resource<DistanceResult>>>(resources, HttpStatus.OK);
	}
	
	
	@Override
	@JsonView(View.PoiSummary.class)
	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<PagedResources<Resource<PointOfInterest>>> findAll(
			Pageable pageable, 
			PagedResourcesAssembler<PointOfInterest> assembler, 
			@RequestParam(value = "filter", required = false) List<String> stringClause) {

//		SimpleMailMessage message = new SimpleMailMessage();
//		message.setTo("anderson.jose.so@gmail.com");
//		message.setSubject("teste");
//		message.setText("agora");
//		javaMailSender.send(message);
		return super.findAll(pageable, assembler, stringClause);
	}
	
	
	@Override
	@JsonView(View.PoiDetails.class)
	@RequestMapping(value="/{id}",method=RequestMethod.GET) 
	public ResponseEntity<PointOfInterest> findOne(@PathVariable Long id, HttpServletRequest request) {
		
		logger.info("Recuperando objeto com id: " + id);
		
		PointOfInterest poi = poiRepository.findOne(id);

				/**
		 * Check if the current user has voted in this poi
		 */
		if (tokenService.hasAuthenticatedUser(request)) {
			Token token = tokenService.getAuthenticatedUser(request);
			Vote currentUserVote = poiVoteRepository.findByUserAndPoi(poi, token.getUser());
			poi.setUserVote(currentUserVote);
		} 
		
		if (poi == null) {
			return new ResponseEntity<PointOfInterest>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<PointOfInterest>(poi, HttpStatus.OK);
	}
	
	@RequestMapping(value = "geojson", method = RequestMethod.GET)
	public ResponseEntity<FeatureCollection> findAll(Pageable pageable, 
			@RequestParam(value = "filter", required = false) List<String> stringClause) {
		Filter filter = null;
		if (stringClause != null && !stringClause.isEmpty()) {
			filter = new Filter(stringClause, super.getEntityClass());
		}
		List<PointOfInterest> pois = poiRepository.findAllPlain(pageable, filter);

		FeatureCollectionBuilder builder = new FeatureCollectionBuilder();
		builder.addPointsOfInterest(pois);
		FeatureCollection featureCollection = builder.build();
		return new ResponseEntity<FeatureCollection>(featureCollection, HttpStatus.OK);
	}
	
	@JsonView(View.PoiDetails.class)
	@RequestMapping(method = RequestMethod.POST) 
	public ResponseEntity<PointOfInterest> insert(@RequestBody @Valid PointOfInterest newObject, 
			BindingResult result, HttpServletResponse response, HttpServletRequest request) {
		
		if (tokenService.hasAuthenticatedUser(request)) {
			Token token = tokenService.getAuthenticatedUser(request);
			newObject.setUser(token.getUser());
		}

		
		if (newObject.getFieldValues() != null) {
			for (FieldValue value : newObject.getFieldValues()) {
				value.setPoi(newObject);
			}
		}
		
		List<Picture> documents = newObject.getPictures();
		newObject.setPictures(new ArrayList<Picture>());
		newObject.getLocation().setSRID(LatLng.GOOGLE_SRID);
		newObject.setPoiStatus(PoiStatus.OPEN);

		ResponseEntity<PointOfInterest> responseEntity = super.insert(newObject, result, response, request);
		
		/**
		 * Save all pictures
		 */
		PointOfInterest poiCreated = responseEntity.getBody();
		for (Picture document : documents) {
			document = documentRepository.findOne(document.getId());
			document.setPoi(poiCreated);
			documentRepository.save(document);
		}
		PerformedTaskService.affectedObjectStorage.set(poiCreated);
		return responseEntity;
	}
	
	@RequestMapping(value="/{id}", method=RequestMethod.PUT) 
	public ResponseEntity<PointOfInterest> updateOne(@RequestBody PointOfInterest poi, 
			@PathVariable Long id, BindingResult result, HttpServletRequest request) {
		
		PointOfInterest targetPoi = poiRepository.findOne(id);
		if (targetPoi == null) {
			return new ResponseEntity<PointOfInterest>(HttpStatus.NOT_FOUND);
		}
		
		Token token = tokenService.getAuthenticatedUser(request);
		User user = token.getUser();
		/**
		 * The user only will be able to change a point of interest if he is root or owner
		 * of the point of interest
		 */
		if (user.isRoot() || user.equals(targetPoi.getUser()) ) {
			poi.getLocation().setSRID(LatLng.GOOGLE_SRID);
			poi.setDate(new Date());
			poi.setPublished(true);
			if (user.equals(targetPoi.getUser())) {
				poi.setUser(user);
			}
			return super.updateOne(poi, id, result, request);
		} else {
			return new ResponseEntity<PointOfInterest>(HttpStatus.UNAUTHORIZED);
		}
	}
	
	
	@RequestMapping(value = "{id}/html", method = RequestMethod.GET)
	public String getSocialMediaSharingView(@PathVariable Long id, Model model) {
		PointOfInterest poi = poiRepository.findOne(id);
		model.addAttribute("poi", poi);
		model.addAttribute("bannerUrl", env.getProperty("socialmedia.image.url"));
		model.addAttribute("appUrl", env.getProperty("app.url"));
		return "poi";
	}
	
	@RequestMapping(value = "{poiId}/comment", method = RequestMethod.POST) 
	public ResponseEntity<PoiComment> insert(@RequestBody @Valid PoiComment newObject, BindingResult result, 
			HttpServletResponse response, HttpServletRequest request, @PathVariable Long poiId) {
		if (!poiRepository.exists(poiId)) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
		PointOfInterest poi = poiRepository.findOne(poiId);
		newObject.setId(null);
		newObject.setPoi(poi);
		newObject.setDate(new Date());
		newObject.setDownVoteCount(0);
		newObject.setUpVoteCount(0);
		Token token = tokenService.getAuthenticatedUser(request);
		newObject.setUser(token.getUser());
		if (result.hasErrors()) {
			throw new ValidationException(result);
		}
		
		logger.info("Inserindo novo objeto " + newObject);
		newObject = commentRepository.save(newObject);
		return new ResponseEntity<>(newObject, HttpStatus.CREATED);
	}

	
	@JsonView(View.Summary.class)
	@RequestMapping(value = "{poiId}/comment", method = RequestMethod.GET) 
	public ResponseEntity< PagedResources<Resource<PoiComment>> > findAllComments(Pageable pageable, 
			PagedResourcesAssembler<PoiComment> assembler, @PathVariable Long poiId, HttpServletRequest request) {
		
		logger.info("Listando todos os valores");
		
		List<String> stringClause = new ArrayList<>();
		stringClause.add("poi.id=" + poiId);
		Filter filter = new Filter(stringClause, PoiComment.class);
		
		Page<PoiComment> page = commentRepository.findAll(pageable, filter);
		
		if (tokenService.hasAuthenticatedUser(request)) {
			Token token = tokenService.getAuthenticatedUser(request);
			PointOfInterest poi = new PointOfInterest();
			poi.setId(poiId);
			List<PoiCommentVote> poiCommentsVotes = voteRepository.findAllVotedByUserAndPoiOrderedById(poi, token.getUser());
			for (PoiComment comment : page.getContent()) {
				comment.findUserVote(poiCommentsVotes);
			}
		} 
		
		PagedResources<Resource<PoiComment>> resources = this.toPagedResources(page, assembler);
		return new ResponseEntity<PagedResources<Resource<PoiComment>>>(resources, HttpStatus.OK);
	}
	
	@RequestMapping(value = "{id}/vote", method = RequestMethod.POST) 
	public ResponseEntity<PoiVote> insert(@RequestBody @Valid PoiVote vote, BindingResult result, 
			HttpServletResponse response, HttpServletRequest request, @PathVariable Long id) {
		if (result.hasErrors()) {
			throw new ValidationException(result);
		}
		
		if (!poiRepository.exists(id)) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
		Token token = tokenService.getAuthenticatedUser(request);
		PointOfInterest poi = poiRepository.findOne(id);
		vote.setUser(token.getUser());
		vote.setPoi(poi);

		logger.info("Inserindo novo objeto " + vote);
		voteService.vote(poi, vote);
		return new ResponseEntity<>(vote, HttpStatus.OK);
	}



	@RequestMapping(value="{id}/statusToInAnalysis", method=RequestMethod.PUT)
	public ResponseEntity<PointOfInterest> updatePoiStatusTypeToInAnalysis(@RequestBody PointOfInterest poi,
													 @PathVariable Long id, BindingResult result, HttpServletRequest request) {

		PointOfInterest targetPoi = poiRepository.findOne(id);
		if (targetPoi == null) {
			return new ResponseEntity<PointOfInterest>(HttpStatus.NOT_FOUND);
		}

		Token token = tokenService.getAuthenticatedUser(request);
		User user = token.getUser();
		/**
		 * The user only will be able to change a point of interest if he is root or owner
		 * of the point of interest
		 */
		if (user.isRoot() || user.isHealthAgent() ) {
			poi.getLocation().setSRID(LatLng.GOOGLE_SRID);
			poi.setDate(new Date());
			poi.setPublished(true);
			if (poi.getPoiStatus().equals(PoiStatus.OPEN)){
				poi.setPoiStatus(PoiStatus.IN_ANALYSIS);
			}
			if (user.equals(targetPoi.getUser())) {
				poi.setUser(user);
			}

			logger.info("Alterando status de aberto para em analise " + poi);

			return super.updateOne(poi, id, result, request);
		} else {
			return new ResponseEntity<PointOfInterest>(HttpStatus.UNAUTHORIZED);
		}
	}


	@RequestMapping(value="{id}/statusToTreated", method=RequestMethod.PUT)
	public ResponseEntity<PointOfInterest> updatePoiStatusTypeToTreated(@RequestBody PointOfInterest poi,
																		   @PathVariable Long id, BindingResult result, HttpServletRequest request) {

		PointOfInterest targetPoi = poiRepository.findOne(id);
		if (targetPoi == null) {
			return new ResponseEntity<PointOfInterest>(HttpStatus.NOT_FOUND);
		}

		Token token = tokenService.getAuthenticatedUser(request);
		User user = token.getUser();
		/**
		 * The user only will be able to change a point of interest if he is root or owner
		 * of the point of interest
		 */

		if (user.isRoot() || user.isHealthAgent() ) {
			poi.getLocation().setSRID(LatLng.GOOGLE_SRID);
			poi.setDate(new Date());
			poi.setPublished(true);
			if(poi.getPoiStatus().equals(PoiStatus.IN_ANALYSIS)){
				poi.setPoiStatus(PoiStatus.TREATED);
			}

			if (user.equals(targetPoi.getUser())) {
				poi.setUser(user);
			}
			logger.info("Alterando status de em analise para tratrado " + poi);


			return super.updateOne(poi, id, result, request);
		} else {
			return new ResponseEntity<PointOfInterest>(HttpStatus.UNAUTHORIZED);
		}
	}


}
