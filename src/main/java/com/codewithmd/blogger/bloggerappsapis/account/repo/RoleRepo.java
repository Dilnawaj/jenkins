package com.codewithmd.blogger.bloggerappsapis.account.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.codewithmd.blogger.bloggerappsapis.account.entity.ClientRole;
import com.codewithmd.blogger.bloggerappsapis.account.entity.Role;

@Repository
public interface RoleRepo extends JpaRepository<Role, Integer> {

	public Role findRoleByName(String name);

	public Role findByUserType(String userType);
	
	public Role findByRoleId(Integer roleId);

	public Role findTopByOrderByPriorityDesc();

	@Query(value = "SELECT new com.codewithmd.blogger.bloggerappsapis.account.entity.ClientRole(cr.id, cr.clientId, cr.roleId) FROM ClientRole cr WHERE cr.clientId = ?1")
	public ClientRole getClientRoleFromClientId(Long userId);

}
