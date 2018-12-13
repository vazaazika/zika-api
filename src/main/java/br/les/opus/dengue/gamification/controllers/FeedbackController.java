
package br.les.opus.dengue.gamification.controllers;


import br.les.opus.auth.core.domain.Device;
import br.les.opus.auth.core.domain.Token;
import br.les.opus.auth.core.domain.User;
import br.les.opus.auth.core.services.TokenService;
import br.les.opus.commons.persistence.PagingSortingFilteringRepository;
import br.les.opus.commons.rest.controllers.AbstractCRUDController;
import br.les.opus.commons.rest.exceptions.ValidationException;
import br.les.opus.dengue.api.controllers.Constant;
import br.les.opus.dengue.core.domain.PointOfInterest;
import br.les.opus.dengue.core.repositories.PointOfInterestRepository;
import br.les.opus.gamification.domain.feedback.Feedback;
import br.les.opus.gamification.domain.feedback.FeedbackPoiInformationQuality;
import br.les.opus.gamification.domain.feedback.FeedbackType;
import br.les.opus.gamification.services.FeedbackService;
import br.les.opus.gamification.services.NotificationService;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@RestController
@Transactional
@RequestMapping("/feedback-poi")
public class FeedbackController extends AbstractCRUDController<FeedbackPoiInformationQuality> {

    protected Logger logger = Logger.getLogger(getEntityClass());

    @Autowired
    private PointOfInterestRepository poiRepository;

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private TokenService tokenService;

    @RequestMapping(value = "{poiId}/quality-information", method = RequestMethod.POST)
    public ResponseEntity<FeedbackPoiInformationQuality> createFeedbackQualityInformation(@PathVariable Long poiId, @RequestBody FeedbackPoiInformationQuality poiInformationQuality,
                                                                                          BindingResult result, HttpServletResponse response, HttpServletRequest request) {

        System.out.println("Meu querindooo: "+poiId);

        System.out.println("Meu querindooo 2: "+poiInformationQuality.getBody());

        System.out.println("Meu querindooo 3: "+poiInformationQuality.getTitle());


        System.out.println("Meu querindooo 4: "+poiInformationQuality.getFeedbackType());

        System.out.println("Meu querindooo 4: "+poiInformationQuality.getFeedbackType().get(0).getId());


        if (!poiRepository.exists(poiId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        PointOfInterest poi = poiRepository.findOne(poiId);

        Token token = tokenService.getAuthenticatedUser(request);
        User user = token.getUser();

        if (result.hasErrors()) {
            throw new ValidationException(result);
        }

        if (poiInformationQuality == null){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        poiInformationQuality.setDate(new Date());

        if (user.isHealthAgent()) {

            poiInformationQuality.setObject(poi);
            poiInformationQuality.setUser(user);
            logger.info("Inserindo novo feedback " + poiInformationQuality);
            FeedbackPoiInformationQuality received = feedbackService.saveFeedbackInformationQuality(poiInformationQuality);

            //messages
            if( poi.getUser() != null && poi.getUser().getDevices()!=null) {
                Map<String, String> mapa = new HashMap<>();
                mapa.put("type", Constant.POI_QUALITY_INFORMATION);
                mapa.put("title", poiInformationQuality.getTitle());
                mapa.put("message", "Novo feedback sobre a qualidade da informação: " + poiInformationQuality.getBody());
                mapa.put("id", "" + received.getId());
                //mapa.put("type_feedback", "" + received.getFeedbackType());


                for(Device dev: user.getDevices())
                    notificationService.sendNotificationId(mapa, dev.getToken());
            }

            return new ResponseEntity<>(received, HttpStatus.CREATED);

        } else {
            System.out.println("MEU FI");
            return new ResponseEntity<FeedbackPoiInformationQuality>(HttpStatus.UNAUTHORIZED);


        }
    }


    @RequestMapping(value = "/quality-information-type", method = RequestMethod.POST)
    public ResponseEntity<FeedbackType> createFeedbackType(@RequestBody FeedbackType feedbackType, BindingResult result, HttpServletResponse response, HttpServletRequest request) {

        Token token = tokenService.getAuthenticatedUser(request);
        User user = token.getUser();

        if (result.hasErrors()) {
            throw new ValidationException(result);
        }

        if (user.isHealthAgent()) {

            logger.info("Inserindo novo feedback Tyoe" + feedbackType);
            FeedbackType feedbackType2 = feedbackService.saveFeedbackType(feedbackType);

            return new ResponseEntity<>(feedbackType2, HttpStatus.CREATED);

        } else {
            System.out.println("MEU FI");
            return new ResponseEntity<FeedbackType>(HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    protected PagingSortingFilteringRepository<FeedbackPoiInformationQuality, Long> getRepository() {
        return null;
    }
}