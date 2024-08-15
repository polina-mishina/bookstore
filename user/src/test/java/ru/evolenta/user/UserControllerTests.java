package ru.evolenta.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.evolenta.user.config.SecurityConfiguration;
import ru.evolenta.user.controller.UserController;
import ru.evolenta.user.dto.UpdateUserRequest;
import ru.evolenta.user.model.Role;
import ru.evolenta.user.model.User;
import ru.evolenta.user.repository.UserRepository;
import ru.evolenta.user.service.JwtService;
import ru.evolenta.user.service.UserService;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(SecurityConfiguration.class)
class UserControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private UserService userService;

	@MockBean
	private JwtService jwtService;

	@MockBean
	private UserRepository userRepository;

	@MockBean
	private PasswordEncoder passwordEncoder;

	@MockBean
	private AuthenticationManager authenticationManager;

	@MockBean
	private AuthenticationProvider authenticationProvider;

	@Test
	@WithMockUser
	void testGetUserMeSuccess() throws Exception {
		User user = new User(
				1L,
				"Firstname",
				"Surname",
				"Lastname",
				"username",
				passwordEncoder.encode("password"),
				Role.ROLE_USER
		);

		Mockito.when(this.userService
						.getCurrentUser())
				.thenReturn(user);

		mockMvc.perform(get("/user/me"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(user.getId()))
				.andExpect(jsonPath("$.firstname").value(user.getFirstname()))
				.andExpect(jsonPath("$.surname").value(user.getSurname()))
				.andExpect(jsonPath("$.lastname").value(user.getLastname()))
				.andExpect(jsonPath("$.username").value(user.getUsername()))
				.andExpect(jsonPath("$.password").value(user.getPassword()))
				.andExpect(jsonPath("$.role").value(user.getRole().toString()));
	}

	@Test
	@WithAnonymousUser
	void testGetUserMeForbidden() throws Exception {
		mockMvc.perform(get("/user/me"))
				.andDo(print())
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser
	void testUpdateUserMeSuccess() throws Exception {
		User user = new User(
				1L,
				"Firstname",
				"Surname",
				"Lastname",
				"username",
				passwordEncoder.encode("password"),
				Role.ROLE_USER
		);

		UpdateUserRequest userRequest = new UpdateUserRequest();
		userRequest.setFirstname("New Firstname");
		userRequest.setSurname("New Surname");
		userRequest.setLastname("New Lastname");
		userRequest.setUsername("New Username");

		BeanUtils.copyProperties(userRequest, user);

		Mockito.when(this.userService
						.updateCurrentUser(userRequest))
				.thenReturn(ResponseEntity.ok(user));

		mockMvc.perform(put("/user/me")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(userRequest)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(user.getId()))
				.andExpect(jsonPath("$.firstname").value(user.getFirstname()))
				.andExpect(jsonPath("$.surname").value(user.getSurname()))
				.andExpect(jsonPath("$.lastname").value(user.getLastname()))
				.andExpect(jsonPath("$.username").value(user.getUsername()));
	}

	@Test
	@WithAnonymousUser
	void testUpdateUserMeForbidden() throws Exception {
		UpdateUserRequest userRequest = new UpdateUserRequest();
		userRequest.setFirstname("New Firstname");
		userRequest.setSurname("New Surname");
		userRequest.setLastname("New Lastname");
		userRequest.setUsername("New Username");

		mockMvc.perform(put("/user/me")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(userRequest)))
				.andDo(print())
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = {"ADMIN"})
	void testGetUsersSuccess() throws Exception {
		User user1 = new User(
				1L,
				"Firstname",
				"Surname",
				"Lastname",
				"user1",
				passwordEncoder.encode("password"),
				Role.ROLE_USER
		);
		User user2 = new User(
				2L,
				"Firstname",
				"Surname",
				"Lastname",
				"user2",
				passwordEncoder.encode("password"),
				Role.ROLE_USER
		);

		Mockito.when(this.userRepository
						.findAll())
				.thenReturn(List.of(user1, user2));

		mockMvc.perform(get("/user"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.size()").value(2))
				.andExpect(jsonPath("$[0].id").value(user1.getId()))
				.andExpect(jsonPath("$[0].username").value(user1.getUsername()))
				.andExpect(jsonPath("$[1].id").value(user2.getId()))
				.andExpect(jsonPath("$[1].username").value(user2.getUsername()));
	}

	@Test
	@WithMockUser
	void testGetUsersForbidden() throws Exception {
		mockMvc.perform(get("/user"))
				.andDo(print())
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = {"ADMIN"})
	void testGetUserByIdSuccess() throws Exception {
		User user1 = new User(
				1L,
				"Firstname",
				"Surname",
				"Lastname",
				"user1",
				passwordEncoder.encode("password"),
				Role.ROLE_USER
		);
		User user2 = new User(
				2L,
				"Firstname",
				"Surname",
				"Lastname",
				"user2",
				passwordEncoder.encode("password"),
				Role.ROLE_USER
		);
		List<User> users = List.of(user1, user2);

		int targetUserId = 2;
		User targetUser = users.get(targetUserId-1);
		Mockito.when(this.userService.getUser(targetUserId))
				.thenReturn(ResponseEntity.ok(targetUser));

		mockMvc.perform(get("/user/" + targetUserId))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value((targetUser.getId())))
				.andExpect(jsonPath("$.firstname").value(targetUser.getFirstname()))
				.andExpect(jsonPath("$.surname").value(targetUser.getSurname()))
				.andExpect(jsonPath("$.lastname").value(targetUser.getLastname()))
				.andExpect(jsonPath("$.username").value(targetUser.getUsername()))
				.andExpect(jsonPath("$.password").value(targetUser.getPassword()))
				.andExpect(jsonPath("$.role").value(targetUser.getRole().toString()));
	}

	@Test
	@WithMockUser(roles = {"ADMIN"})
	void testGetUserByIdNotFound() throws Exception {
		int targetUserId = 5;
		Mockito.when(this.userService.getUser(targetUserId))
				.thenReturn(ResponseEntity.notFound().build());

		mockMvc.perform(get("/user/" + targetUserId))
				.andDo(print())
				.andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser
	void testGetUserByIdForbidden() throws Exception {
		int targetUserId = 5;
		mockMvc.perform(get("/user/" + targetUserId))
				.andDo(print())
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = {"ADMIN"})
	void testUpdateUserSuccess() throws Exception {
		User user = new User(
				1L,
				"Firstname",
				"Surname",
				"Lastname",
				"username",
				passwordEncoder.encode("password"),
				Role.ROLE_USER
		);

		UpdateUserRequest userRequest = new UpdateUserRequest();
		userRequest.setFirstname("New Firstname");
		userRequest.setSurname("New Surname");
		userRequest.setLastname("New Lastname");
		userRequest.setUsername("New Username");

		BeanUtils.copyProperties(userRequest, user);

		long targetUserId = 1L;
		Mockito.when(this.userService
						.updateUser(targetUserId, userRequest))
				.thenReturn(ResponseEntity.ok(user));

		mockMvc.perform(put("/user/" + targetUserId)
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(userRequest)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(user.getId()))
				.andExpect(jsonPath("$.firstname").value(user.getFirstname()))
				.andExpect(jsonPath("$.surname").value(user.getSurname()))
				.andExpect(jsonPath("$.lastname").value(user.getLastname()))
				.andExpect(jsonPath("$.username").value(user.getUsername()));
	}

	@Test
	@WithMockUser(roles = {"ADMIN"})
	void testUpdateUserNotFound() throws Exception {
		UpdateUserRequest userRequest = new UpdateUserRequest();
		userRequest.setFirstname("New Firstname");
		userRequest.setSurname("New Surname");
		userRequest.setLastname("New Lastname");
		userRequest.setUsername("New Username");

		long targetUserId = 5L;
		Mockito.when(this.userService
						.updateUser(targetUserId, userRequest))
				.thenReturn(ResponseEntity.notFound().build());

		mockMvc.perform(put("/user/" + targetUserId)
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(userRequest)))
				.andDo(print())
				.andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser
	void testUpdateUserForbidden() throws Exception {
		UpdateUserRequest userRequest = new UpdateUserRequest();
		userRequest.setFirstname("New Firstname");
		userRequest.setSurname("New Surname");
		userRequest.setLastname("New Lastname");
		userRequest.setUsername("New Username");

		long targetUserId = 5L;
		mockMvc.perform(put("/user/" + targetUserId)
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(userRequest)))
				.andDo(print())
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = {"ADMIN"})
	void testDeleteUserSuccess() throws Exception {
		User user = new User(
				1L,
				"Firstname",
				"Surname",
				"Lastname",
				"username",
				passwordEncoder.encode("password"),
				Role.ROLE_USER
		);

		long targetUserId = 1L;
		Mockito.when(this.userService
						.deleteUser(targetUserId))
				.thenReturn(ResponseEntity.ok(user));

		mockMvc.perform(delete("/user/" + targetUserId)
						.with(csrf()))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(user.getId()))
				.andExpect(jsonPath("$.firstname").value(user.getFirstname()))
				.andExpect(jsonPath("$.surname").value(user.getSurname()))
				.andExpect(jsonPath("$.lastname").value(user.getLastname()))
				.andExpect(jsonPath("$.username").value(user.getUsername()))
				.andExpect(jsonPath("$.password").value(user.getPassword()))
				.andExpect(jsonPath("$.role").value(user.getRole().toString()));
	}

	@Test
	@WithMockUser(roles = {"ADMIN"})
	void testDeleteUserNotFound() throws Exception {
		long targetUserId = 5L;
		Mockito.when(this.userService
						.deleteUser(targetUserId))
				.thenReturn(ResponseEntity.notFound().build());

		mockMvc.perform(delete("/user/" + targetUserId)
						.with(csrf()))
				.andDo(print())
				.andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser
	void testDeleteUserForbidden() throws Exception {
		long targetUserId = 5L;
		mockMvc.perform(delete("/user/" + targetUserId)
						.with(csrf()))
				.andDo(print())
				.andExpect(status().isForbidden());
	}
}