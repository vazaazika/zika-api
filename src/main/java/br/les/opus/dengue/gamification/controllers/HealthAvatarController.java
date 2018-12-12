package br.les.opus.dengue.gamification.controllers;

import br.les.opus.auth.core.domain.Token;
import br.les.opus.auth.core.domain.User;
import br.les.opus.auth.core.repositories.UserRepository;
import br.les.opus.auth.core.services.TokenService;
import br.les.opus.dengue.core.domain.Picture;
import br.les.opus.dengue.core.repositories.PictureRepository;
import br.les.opus.gamification.domain.HealthAgent;
import br.les.opus.gamification.repositories.HealthAgentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

@RestController
@Transactional
@RequestMapping("/user/health-agent")
@PropertySource(value = "classpath:dengue-api.properties")
public class HealthAvatarController {

    @Autowired
    private Environment env;

    @Autowired
    private PictureRepository pictureRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private HealthAgentRepository healthAgentRepository;

    @RequestMapping(value="{agentId}/avatar/upload", method=RequestMethod.POST)
    public ResponseEntity<Picture> handleFileUploadNewPicture(@RequestParam MultipartFile file,
                                                              HttpServletRequest request, @PathVariable Long agentId)
            throws Exception {

        HealthAgent user = healthAgentRepository.findOne(agentId);

        if (user == null) {
            return new ResponseEntity<Picture>(HttpStatus.NOT_FOUND);
        }

        Token token = tokenService.getAuthenticatedUser(request);
        User loggedUser = token.getUser();
        if (!loggedUser.isRoot() && !loggedUser.equals(user)) {
            return new ResponseEntity<Picture>(HttpStatus.UNAUTHORIZED);
        }

        String basePath = this.env.getProperty("document.upload.directory");
        if (file.isEmpty()) {
            return new ResponseEntity<Picture>(HttpStatus.BAD_REQUEST);
        }

        Picture image = new Picture();
        image.generateUniqueName(file.getOriginalFilename());
        image.setContentType(file.getContentType());
        image.saveFileInFileSystem(basePath, file.getInputStream());
        image.retrieveDimentionsFromFile(basePath);
        image.setUser(token.getUser());
        image = pictureRepository.save(image);

        Picture currentAvatar = user.getAvatar();
        if (currentAvatar != null) {
            currentAvatar.deleteFileFromFileSystem(basePath);
            pictureRepository.delete(currentAvatar);
        }

        user.setAvatar(image);
        healthAgentRepository.save(user);

        return new ResponseEntity<Picture>(image, HttpStatus.OK);
    }

    @RequestMapping(value="self/avatar/upload", method=RequestMethod.POST)
    public ResponseEntity<Picture> handleFileUploadNewPictureSelf(@RequestParam MultipartFile file,
                                                                  HttpServletRequest request)
            throws Exception {
        Token token = tokenService.getAuthenticatedUser(request);
        return handleFileUploadNewPicture(file, request, token.getUser().getId());
    }
}
