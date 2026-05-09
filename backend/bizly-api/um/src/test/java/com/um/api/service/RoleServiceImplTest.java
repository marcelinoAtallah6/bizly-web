package com.um.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

import com.um.api.dto.role.add.AddRoleRequest;
import com.um.api.dto.role.get.GetRoleResponse;
import com.um.api.dto.role.gets.GetsRolesRequest;
import com.um.api.dto.role.update.UpdateRoleRequest;
import com.um.api.model.role.Role;
import com.um.api.repository.role.RoleRepository;
import com.um.api.service.role.RoleServiceImpl;

@ExtendWith(MockitoExtension.class)
public class RoleServiceImplTest {

	@Mock
	private RoleRepository repository;

	@InjectMocks
	private RoleServiceImpl service;

	private Role role;

	@BeforeEach
	public void setup() {
		role = new Role();
		role.setId(1L);
		role.setName("ADMIN");
		role.setRoleType(1);
		role.setCreatedAt(LocalDateTime.now());
	}

	@Test
	public void testAddRole() {
		when(repository.save(any(Role.class))).thenAnswer(invocation -> {
			Role entity = invocation.getArgument(0);
			entity.setId(1L);
			return entity;
		});

		AddRoleRequest request = new AddRoleRequest();
		request.setName("ADMIN");
		request.setRoleType(1);

		var addResponse = service.add(request);
		assertNotNull(addResponse);
		assertEquals(1L, addResponse.getId());
	}

	@Test
	public void testUpdateRole() {
		when(repository.findById(anyLong())).thenReturn(Optional.of(role));
		when(repository.save(any(Role.class))).thenReturn(role);

		UpdateRoleRequest request = new UpdateRoleRequest();
		request.setId(1L);
		request.setName("ADMIN");
		request.setRoleType(1);

		var updateResponse = service.update(request);
		assertNotNull(updateResponse);
		assertEquals(1L, updateResponse.getId());
	}

	@Test
	public void testDeleteRole() {
		when(repository.existsById(anyLong())).thenReturn(true);
		doNothing().when(repository).deleteById(anyLong());

		com.um.api.dto.role.delete.DeleteRoleRequest request = new com.um.api.dto.role.delete.DeleteRoleRequest();
		request.setId(1L);

		var deleteResponse = service.delete(request);
		assertNotNull(deleteResponse);
		assertEquals(1L, deleteResponse.getId());
	}

	@Test
	public void testGetRole() {
		when(repository.findById(anyLong())).thenReturn(Optional.of(role));

		com.um.api.dto.role.get.GetRoleRequest request = new com.um.api.dto.role.get.GetRoleRequest();
		request.setId(1L);

		GetRoleResponse response = service.get(request);
		assertNotNull(response);
		assertEquals("ADMIN", response.getName());
	}

	@Test
	public void testGetsRoles() {
		Page<Role> page = new PageImpl<>(List.of(role), PageRequest.of(0, 10), 1);
		when(repository.findAll(any(Pageable.class))).thenReturn(page);

		GetsRolesRequest request = new GetsRolesRequest();
		request.setPageNumber(0);
		request.setPageSize(10);

		assertNotNull(service.gets(request));
		assertEquals(1, service.gets(request).getItems().size());
	}
}