package br.les.opus.dengue.gamification.controllers;


import br.les.opus.auth.core.domain.Resource;
import br.les.opus.auth.core.domain.Token;
import br.les.opus.auth.core.domain.User;
import br.les.opus.auth.core.services.TokenService;
import br.les.opus.commons.rest.controllers.AbstractController;
import br.les.opus.dengue.core.domain.PointOfInterest;
import br.les.opus.dengue.core.domain.PointOfInterestFilter;
import br.les.opus.dengue.core.repositories.PointOfInterestRepository;
import br.les.opus.gamification.domain.DashboardResults;
import br.les.opus.gamification.domain.HealthAgent;
import br.les.opus.gamification.domain.Player;
import br.les.opus.gamification.services.HealthAgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;


@RestController
@Transactional
@RequestMapping("/dashboard-health-agent")
public class DashboardHealthAgentController {

    @Autowired
    private HealthAgentService healthAgentService;

    @Autowired
    private PointOfInterestRepository pointOfInterestRepository;

    @Autowired
    private TokenService tokenService;


    @RequestMapping(value = "/filter", method = RequestMethod.GET)
    public ResponseEntity<DashboardResults> findAllPoiByFilters(PointOfInterestFilter point,
                                                                Pageable pageable, HttpServletRequest request) {
        Token token = tokenService.getAuthenticatedUser(request);

        User user = token.getUser();


        if (user.isHealthAgent()) {

            HealthAgent healthAgent = healthAgentService.findById(user);

            if (healthAgent == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }else if (point == null) {
                DashboardResults dashboardResults = new DashboardResults();

                Page<PointOfInterest> pointOfInterests = pointOfInterestRepository.findAll(pageable);
                dashboardResults.setPointOfInterestPage(pointOfInterests);

                return new ResponseEntity<DashboardResults>(dashboardResults, HttpStatus.OK);

            } else {

                DashboardResults dashboardResults = pointOfInterestRepository.findAllPoiByFilters(healthAgent, point, pageable);

                return new ResponseEntity<DashboardResults>(dashboardResults,HttpStatus.OK);

            }
        } else {
            return new ResponseEntity<DashboardResults>(HttpStatus.UNAUTHORIZED);
        }

    }
}



