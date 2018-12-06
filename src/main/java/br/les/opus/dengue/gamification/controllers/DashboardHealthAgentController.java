package br.les.opus.dengue.gamification.controllers;


import br.les.opus.auth.core.domain.Token;
import br.les.opus.auth.core.domain.User;
import br.les.opus.auth.core.services.TokenService;
import br.les.opus.dengue.core.domain.PointOfInterest;
import br.les.opus.dengue.core.repositories.PointOfInterestRepository;
import br.les.opus.gamification.domain.DashboardResults;
import br.les.opus.gamification.domain.HealthAgent;
import br.les.opus.gamification.services.HealthAgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;


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


    @RequestMapping(value="/dashboard", method = RequestMethod.GET)
    public ResponseEntity<DashboardResults> findAllPoiByFilters(@RequestBody PointOfInterest point,
                                                                Pageable pageable, HttpServletRequest request) {

        Token token = tokenService.getAuthenticatedUser(request);
        User user = token.getUser();

        if (user.isHealthAgent()) {
            pointOfInterestRepository.findAllPoiByFilters((HealthAgent) user, point, pageable);
        } else {
            return new ResponseEntity<DashboardResults>(HttpStatus.UNAUTHORIZED);
        }

        return new ResponseEntity<DashboardResults>(HttpStatus.OK);
    }





}


