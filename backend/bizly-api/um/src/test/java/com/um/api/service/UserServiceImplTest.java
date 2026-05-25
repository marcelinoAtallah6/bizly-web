package com.um.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.um.api.dto.user.add.AddUserRequest;
import com.um.api.domain.UserType;
import com.um.api.dto.user.get.GetUserResponse;
import com.um.api.dto.user.gets.GetsUsersRequest;
import com.um.api.dto.user.update.UpdateUserRequest;
import com.um.api.model.role.Role;
import com.um.api.model.role.UserRole;
import com.um.api.model.user.User;
import com.um.api.repository.business.BusinessRepository;
import com.um.api.repository.role.RoleRepository;
import com.um.api.repository.role.UserRoleRepository;
import com.um.api.repository.user.UserRepository;
import com.um.api.service.user.UserServiceImpl;
import com.um.common.PasswordUtil;
import com.um.security.BusinessContextHolder;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

	@Mock
	private UserRepository repository;

	@Mock
	private BusinessRepository businessRepository;

	@Mock
	private RoleRepository roleRepository;

	@Mock
	private UserRoleRepository userRoleRepository;

	@Mock
	private PasswordUtil passwordUtil;

	@Mock
	private com.um.api.service.role.RolePolicyService rolePolicyService;

	@InjectMocks
	private UserServiceImpl service;

	private User user;

	@BeforeEach
	public void setup() {
		user = new User();
		user.setId(1L);
		user.setUsername("johndoe");
		user.setFirstName("John");
		user.setLastName("Doe");
		user.setEmail("john.doe@example.com");
		user.setMobileNumber("+1234567890");
		user.setStatus("ACTIVE");
		BusinessContextHolder.set(null, "ADMIN", null);
	}

	@AfterEach
	public void tearDown() {
		BusinessContextHolder.clear();
	}

	@Test
	public void testAddUser() throws Exception {
		when(repository.save(any(User.class))).thenAnswer(invocation -> {
			User entity = invocation.getArgument(0);
			entity.setId(1L);
			return entity;
		});
		when(passwordUtil.decryptPassword(any(String.class))).thenReturn("decryptedPassword");
		when(roleRepository.findById(anyLong())).thenReturn(Optional.of(new Role()));
		when(userRoleRepository.save(any(UserRole.class))).thenReturn(new UserRole());

		AddUserRequest request = new AddUserRequest();
		request.setUsername("johndoe");
		request.setFirstName("John");
		request.setLastName("Doe");
		request.setEmail("john.doe@example.com");
		request.setMobileNumber("+1234567890");
		request.setPassword("encryptedPassword");
		request.setRoleIds(List.of(1L));
		request.setStatus("ACTIVE");

		var addResponse = service.add(request);
		assertNotNull(addResponse);
		assertEquals(1L, addResponse.getId());
	}

	@Test
	public void testUpdateUser() {
		when(repository.findById(anyLong())).thenReturn(Optional.of(user));
		when(repository.save(any(User.class))).thenReturn(user);

		UpdateUserRequest request = new UpdateUserRequest();
		request.setId(1L);
		request.setUsername("johndoe");
		request.setFirstName("John");
		request.setLastName("Doe");
		request.setEmail("john.doe@example.com");
		request.setMobileNumber("+1234567890");
		request.setStatus("ACTIVE");
		request.setRoleIds(List.of(1L));

		var updateResponse = service.update(request);
		assertNotNull(updateResponse);
		assertEquals(1L, updateResponse.getId());
	}

	@Test
	public void testDeleteUser() {
		when(repository.existsById(anyLong())).thenReturn(true);
		doNothing().when(repository).deleteById(anyLong());

		com.um.api.dto.user.delete.DeleteUserRequest request = new com.um.api.dto.user.delete.DeleteUserRequest();
		request.setId(1L);

		var deleteResponse = service.delete(request);
		assertNotNull(deleteResponse);
		assertEquals(1L, deleteResponse.getId());
	}

	@Test
	public void testGetUser() {
		when(repository.findById(anyLong())).thenReturn(Optional.of(user));

		com.um.api.dto.user.get.GetUserRequest request = new com.um.api.dto.user.get.GetUserRequest();
		request.setId(1L);

		GetUserResponse response = service.get(request);
		assertNotNull(response);
		assertEquals("johndoe", response.getUsername());
	}

	@Test
	public void testGetsUsers_portalAdminScopeWhenNoBusinessSelected() {
		user.setUserType(UserType.PORTAL_ADMIN.name());
		Page<User> page = new PageImpl<>(List.of(user), PageRequest.of(0, 10), 1);
		when(repository.findAllByBusinessIdIsNullAndUserType(eq(UserType.PORTAL_ADMIN.name()), any(Pageable.class)))
				.thenReturn(page);
		when(userRoleRepository.findById_UserId(anyLong())).thenReturn(List.of());

		GetsUsersRequest request = new GetsUsersRequest();
		request.setPageNumber(0);
		request.setPageSize(10);

		var response = service.gets(request);
		assertNotNull(response);
		assertEquals(1, response.getItems().size());
	}

	@Test
	public void testGetsUsers_scopedToSelectedBusiness() {
		BusinessContextHolder.set(null, "ADMIN", 42L);
		user.setBusinessId(42L);
		Page<User> page = new PageImpl<>(List.of(user), PageRequest.of(0, 10), 1);
		when(repository.findAllByBusinessId(eq(42L), any(Pageable.class))).thenReturn(page);
		when(userRoleRepository.findById_UserId(anyLong())).thenReturn(List.of());
		when(businessRepository.findAllById(any())).thenReturn(List.of());

		GetsUsersRequest request = new GetsUsersRequest();
		request.setPageNumber(0);
		request.setPageSize(10);

		var response = service.gets(request);
		assertNotNull(response);
		assertEquals(1, response.getItems().size());
		assertEquals(42L, response.getItems().get(0).getBusinessId());
	}
}
