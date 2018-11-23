package br.les.opus.dengue.gamification.controllers;


import br.les.opus.gamification.services.HealthAgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Transactional
@RequestMapping("/game/health-agent")
public class HealthAgentController {


    @Autowired
    private HealthAgentService healthAgentService;




}
