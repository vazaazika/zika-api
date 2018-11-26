
package br.les.opus.dengue.gamification.controllers;


import br.les.opus.auth.core.domain.Device;
import br.les.opus.auth.core.domain.Token;
import br.les.opus.auth.core.domain.User;
import br.les.opus.auth.core.services.TokenService;
import br.les.opus.commons.persistence.PagingSortingFilteringRepository;
import br.les.opus.commons.rest.controllers.AbstractCRUDController;
import br.les.opus.commons.rest.exceptions.ValidationException;
import br.les.opus.dengue.core.domain.PointOfInterest;
import br.les.opus.dengue.core.repositories.PointOfInterestRepository;
import br.les.opus.gamification.domain.feedback.Feedback;
import br.les.opus.gamification.domain.feedback.FeedbackPoiInformationQuality;
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
public class FeedbackController extends AbstractCRUDController<Feedback> {


    protected Logger logger = Logger.getLogger(getEntityClass());


    @Autowired
    private PointOfInterestRepository poiRepository;

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private TokenService tokenService;

    @RequestMapping(value = "{poiId}/qualityInformation", method = RequestMethod.POST)
    public ResponseEntity<Feedback> createFeedbackQualityInformation(@PathVariable Long poiId, String body,
                                                                     BindingResult result, HttpServletResponse response, HttpServletRequest request) {

        if (!poiRepository.exists(poiId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        PointOfInterest poi = poiRepository.findOne(poiId);

        Token token = tokenService.getAuthenticatedUser(request);
        User user = token.getUser();

        if (result.hasErrors()) {
            throw new ValidationException(result);
        }

        if (user.isHealthAgent()) {
            FeedbackPoiInformationQuality informationQuality = new FeedbackPoiInformationQuality();
            informationQuality.setDate(new Date());
            informationQuality.setTitle("");
            informationQuality.setBody(body);
            informationQuality.setObject(poi);

            logger.info("Inserindo novo feedback " + informationQuality);
            FeedbackPoiInformationQuality received = feedbackService.saveFeedbackInformationQuality(informationQuality);


            //messages
            if(poi.getUser().getDevices()!=null) {
                Map<String, String> mapa = new HashMap<>();
                mapa.put("type", "QualityInformation");
                mapa.put("message", "New information Quality: "+informationQuality.getBody());
                mapa.put("id", "" + received.getId());

                for(Device dev: user.getDevices())
                    notificationService.sendNotificationId(mapa, dev.getToken());
            }


            return new ResponseEntity<>(HttpStatus.CREATED);

        } else {
            return new ResponseEntity<Feedback>(HttpStatus.UNAUTHORIZED);


        }
    }







    @Override
    protected PagingSortingFilteringRepository<Feedback, Long> getRepository() {
        return null;
    }
}