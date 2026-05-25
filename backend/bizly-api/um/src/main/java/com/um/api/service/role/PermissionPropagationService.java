package com.um.api.service.role;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.um.api.model.role.RoleMenuPermission;
import com.um.api.model.role.RoleMenuPermissionId;
import com.um.api.repository.role.RoleMenuPermissionRepository;

/**
 * Copies menu permissions from a template role to a team role when the business
 * owner creates it.
 */
@Service
public class PermissionPropagationService {

	@Autowired
	private RoleMenuPermissionRepository roleMenuPermissionRepository;

	@Transactional
	public void copyFromTemplate(Long templateRoleId, Long teamRoleId) {
		copyFromRole(templateRoleId, teamRoleId);
	}

	@Transactional
	public void copyFromRole(Long sourceRoleId, Long targetRoleId) {
		List<RoleMenuPermission> source = roleMenuPermissionRepository.findByIdRoleId(sourceRoleId);
		roleMenuPermissionRepository.deleteByIdRoleId(targetRoleId);
		for (RoleMenuPermission src : source) {
			RoleMenuPermission copy = new RoleMenuPermission();
			RoleMenuPermissionId id = new RoleMenuPermissionId();
			id.setRoleId(targetRoleId);
			id.setMenuId(src.getId().getMenuId());
			copy.setId(id);
			copy.setAllowView(src.isAllowView());
			copy.setAllowAdd(src.isAllowAdd());
			copy.setAllowEdit(src.isAllowEdit());
			copy.setAllowDelete(src.isAllowDelete());
			roleMenuPermissionRepository.save(copy);
		}
	}
}
