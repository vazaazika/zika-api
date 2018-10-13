package br.les.opus.dengue.gamification.controllers;

import br.les.opus.auth.core.domain.Token;
import br.les.opus.auth.core.domain.User;
import br.les.opus.auth.core.services.TokenService;
import br.les.opus.gamification.domain.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import br.les.opus.gamification.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@Transactional
@RequestMapping("/game/notification")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private TokenService tokenService;

    private static Logger logger = LoggerFactory.getLogger(NotificationController.class);


    @RequestMapping(value = "user", method = RequestMethod.GET)
    public ResponseEntity<List<Notification>> getNotificationsByUserWithLimit(@RequestParam(required = false, defaultValue = "") String limit, HttpServletRequest request) {

        Integer intLimit = Integer.parseInt(limit);

        if (intLimit == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Token token = tokenService.getAuthenticatedUser(request);
        User user = token.getUser();

        List<Notification> notifications = notificationService.findByUser(user, intLimit);

        if (notifications == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(notifications, HttpStatus.OK);
    }


    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<Notification> updateUserNotification(@RequestBody Notification notification, @PathVariable Long id, BindingResult result, HttpServletRequest request) {

        Notification targetNotification = notificationService.findNotificationById(id);

        if (targetNotification == null) {
            return new ResponseEntity<Notification>(HttpStatus.NOT_FOUND);
        } else {
            Token token = tokenService.getAuthenticatedUser(request);
            User user = token.getUser();

            targetNotification.setMessage(notification.getMessage());
            targetNotification.setRead(true);
            targetNotification.setUser(user);

            Notification newNotification = notificationService.updateUserNotification(targetNotification);

            if (newNotification == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);

            } else {

                return new ResponseEntity<>(HttpStatus.OK);
            }


        }
    }
}

