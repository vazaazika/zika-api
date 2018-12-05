package br.les.opus.auth.controllers.crud;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.les.opus.auth.core.domain.Role;
import br.les.opus.auth.core.domain.Token;
import br.les.opus.auth.core.domain.User;
import br.les.opus.auth.core.domain.UserRole;
import br.les.opus.auth.core.repositories.RoleRepository;
import br.les.opus.auth.core.repositories.UserRepository;
import br.les.opus.auth.core.repositories.UserRoleRepository;
import br.les.opus.auth.core.services.TokenService;
import br.les.opus.auth.core.services.UserService;
import br.les.opus.commons.persistence.PagingSortingFilteringRepository;
import br.les.opus.commons.persistence.filtering.Filter;
import br.les.opus.commons.rest.controllers.AbstractCRUDController;
import br.les.opus.commons.rest.exceptions.ValidationException;

@RestController
@Transactional
@RequestMapping("/user")
public class UserCrudController extends AbstractCRUDController<User>{
	
	@Autowired
	private UserRepository repository;
	
	@Autowired
	private RoleRepository roleDao;
	
	@Autowired
	private TokenService tokenService;
	
	@Autowired
	private UserRoleRepository userRoleDao;
	
	@Autowired
	private UserService userService;
	
	@Override
	protected User doFiltering(User user) {
		List<Role> roles = roleDao.findAllByUser(user);
		user.setRoles(roles);
		user.setRoles(roles);
		return super.doFiltering(user);
	}
	
	@RequestMapping(value = "/password", method=RequestMethod.PUT) 
	public ResponseEntity<User> changePassword(HttpServletRequest request, @RequestParam String password) {
		Token token = tokenService.getAuthenticatedUser(request);
		if (token == null || token.getUser() == null) {
			return new ResponseEntity<User>(HttpStatus.BAD_REQUEST);
		}
		User user = token.getUser();
		user.setPassword(DigestUtils.md5Hex(password));
		repository.save(user);
		return new ResponseEntity<User>(HttpStatus.OK);
	}
	
	@RequestMapping(value = "/{userId}/role", method=RequestMethod.POST) 
	public ResponseEntity<User> checkTokenValidity(HttpServletRequest request,@RequestBody Role role, @PathVariable Long userId) {
		User user = repository.findOne(userId);
		if (user == null) {
			return new ResponseEntity<User>(HttpStatus.NOT_FOUND);
		}
		
		role = roleDao.findOne(role.getId());
		if (role == null || role.getId() == null) {
			return new ResponseEntity<User>(HttpStatus.BAD_REQUEST);
		}
		
		UserRole userRole = new UserRole();
		userRole.setRole(role);
		userRole.setUser(user);
		userRoleDao.save(userRole);
		
		return new ResponseEntity<User>(HttpStatus.OK);
	}
	
	@RequestMapping(value = "{userId}/role/{roleId}", method=RequestMethod.DELETE) 
	public ResponseEntity<User> checkTokenValidity(HttpServletRequest request, @PathVariable Long roleId, @PathVariable Long userId) {
		User user = repository.findOne(userId);
		if (user == null) {
			return new ResponseEntity<User>(HttpStatus.NOT_FOUND);
		}
		
		Role role = roleDao.findOne(roleId);
		if (role == null || role.getId() == null) {
			return new ResponseEntity<User>(HttpStatus.BAD_REQUEST);
		}
		
		UserRole userRole = user.findUserRole(role);
		if (userRole == null) {
			return new ResponseEntity<User>(HttpStatus.BAD_REQUEST);
		}
		userRoleDao.delete(userRole);
		return new ResponseEntity<User>(HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<User> insert(@RequestBody @Valid User newObject,
										BindingResult result, HttpServletResponse response, HttpServletRequest request) {
		if (result.hasErrors()) {
			throw new ValidationException(result);
		}
		if (newObject.getPassword() != null) {
			newObject.setPassword(DigestUtils.md5Hex(newObject.getPassword()));
		}
		newObject = userService.save(newObject);
		Link detail = linkTo(this.getClass()).slash(newObject.getId()).withSelfRel();
		response.setHeader("Location", detail.getHref());
		newObject = userService.loadRolesAndResorces(newObject);

		return new ResponseEntity<User>(newObject, HttpStatus.CREATED);
	}
	
	
	@RequestMapping(value = "find-by-username", method = RequestMethod.GET)
	public ResponseEntity<User> findByUserName(@RequestParam String username) {
		User user = repository.findByUsername(username);
		if (user == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(user, HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity< PagedResources<Resource<User>> > findAll(Pageable pageable, PagedResourcesAssembler<User> assembler, 
			@RequestParam(value = "filter", required = false) List<String> stringClause) {
		
		
		logger.info("Listando todos os valores");
		
		/**
		 * Cria o objeto de filtro antes de submeter a consulta ao repositório
		 */
		Filter filter = null;
		if (stringClause != null && !stringClause.isEmpty()) {
			filter = new Filter(stringClause, super.getEntityClass());
		}

		Page<User> page = getRepository().findAll(pageable, filter);
		List<User> users = page.getContent();
		for (User user : users) {
			userService.loadRolesAndResorces(user);
		}
		PagedResources<Resource<User>> resources = null;
		return new ResponseEntity<PagedResources<Resource<User>>>(resources, HttpStatus.OK);
	}

	@RequestMapping(value="/{id}", method=RequestMethod.PUT) 
	public ResponseEntity<User> updateOne(@RequestBody User updatingObject, 
			@PathVariable Long id, BindingResult result, HttpServletRequest request) {
		if (result.hasErrors()) {
			throw new ValidationException(result);
		}
		
		PagingAndSortingRepository<User, Long> repository = getRepository();
		if (!repository.exists(id)) {
			return new ResponseEntity<User>(HttpStatus.NOT_FOUND);
		}
		
		if (updatingObject.getPassword() != null) {
			updatingObject.setPassword(DigestUtils.md5Hex(updatingObject.getPassword()));
		} else {
			User old = repository.findOne(id);
			updatingObject.setPassword(old.getPassword());
		}
		
		updatingObject.setId(id);
		
		/*
		 * Tratamento de validação.
		 * 
		 */
		if (result.hasErrors()) {
			throw new ValidationException(result);
		}
		
		repository.save(updatingObject);
		return new ResponseEntity<User>(HttpStatus.OK);
	}
	
	@RequestMapping(value = "self", method=RequestMethod.GET) 
	public ResponseEntity<User> checkTokenValidity(HttpServletRequest request) {
		Token token = tokenService.getAuthenticatedUser(request);
		User user = token.getUser();
		user = userService.loadRolesAndResorces(user);
		return new ResponseEntity<User>(user, HttpStatus.OK);
	}

	@RequestMapping(value = "/password/reset", method=RequestMethod.PUT)
	public ResponseEntity<Void> resetPassword (@RequestBody User userPassword){

		if (userPassword.getUsername() == null) {
			return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
		}

		userService.changePassword(userPassword);

		return new ResponseEntity<Void>(HttpStatus.OK);
	}

	@RequestMapping(value = "/password/renew", method=RequestMethod.PUT)
	public ResponseEntity<User> renewPassword (@RequestParam(value = "token-reset", required = false) String tokenReset,
											   @RequestBody User userPassword){

		if (userPassword.getPassword() == null) {
			return new ResponseEntity<User>(HttpStatus.BAD_REQUEST);
		}

		User user = userService.resetPassword(tokenReset, userPassword.getPassword());

		return new ResponseEntity<User>(user, HttpStatus.OK);
	}

	@RequestMapping(value="/{id}/username", method=RequestMethod.PUT)
	public ResponseEntity<User> changeUsername(@RequestBody User updatingObject,
											   @PathVariable Long id, BindingResult result, HttpServletRequest request) {
		if (result.hasErrors()) {
			throw new ValidationException(result);
		}

		repository.setUsername(id, updatingObject.getUsername());

		User user = repository.findOne(id);



		return new ResponseEntity<User>(user, HttpStatus.OK);
	}



	@Override
	protected PagingSortingFilteringRepository<User, Long> getRepository() {
		return repository;
	}

}
