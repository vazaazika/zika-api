package br.les.opus.commons.rest.controllers;

import br.les.opus.gamification.domain.MailBody;
import br.les.opus.gamification.domain.Player;
import br.les.opus.gamification.services.GamificationService;
import br.les.opus.gamification.services.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.validation.Valid;

/**
 * Created by andersonjso on 5/10/18.
 */
@RestController
@Transactional
@RequestMapping("/game/mail")
public class MailController{

    @Autowired
    private MailService mailService;

    @Autowired
    private GamificationService gameService;


    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Void> sendMail(@RequestBody @Valid MailBody mailBody, HttpServletRequest request){
        Player player = gameService.loadPlayer(request);

        String messageBody = mailService.buildMessage(player, mailBody.getText());
        String messageTitle = "VazaZika Invitation";

        mailService.send(mailBody.getTo(), messageTitle, messageBody);

        return new ResponseEntity<Void>(HttpStatus.OK);
    }
}
