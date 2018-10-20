package br.les.opus.commons.rest.controllers;

import br.les.opus.auth.core.domain.Token;
import br.les.opus.auth.core.domain.User;
import br.les.opus.auth.core.services.TokenService;
import br.les.opus.auth.core.services.UserService;
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
 * 
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

    @Autowired
    private UserService userService;

    @Autowired
    private TokenService tokenService;


    public void sendMail(String messageTitle, String to, String messageBody){
        mailService.setSubject(messageTitle);
        mailService.setTo(to);
        mailService.setText(messageBody);

        mailService.run();
    }

    @RequestMapping(value = "invite", method = RequestMethod.POST)
    public ResponseEntity<Void> inviteUser(@RequestBody @Valid MailBody mailBody, HttpServletRequest request){
        Token token = tokenService.getAuthenticatedUser(request);
        User user = token.getUser();

        String messageBody = "Your friend " + user.getUsername() + " is inviting you to use VazaZika.\n" +
                "Please use the following link to access and register on our page:\n" +
                "http://vazazika.inf.puc-rio.br/user/join/" + user.getInvite().getHashedToken();

        String messageTitle = "VazaZika Invitation";

        sendMail(messageTitle, mailBody.getTo(), messageBody);

        return new ResponseEntity<Void>(HttpStatus.OK);
    }

}
