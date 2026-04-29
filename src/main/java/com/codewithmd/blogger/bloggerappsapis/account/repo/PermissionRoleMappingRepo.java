package com.codewithmd.blogger.bloggerappsapis.account.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import com.codewithmd.blogger.bloggerappsapis.account.entity.PermissionRoleMapping;


@Repository
public interface PermissionRoleMappingRepo extends JpaRepository<PermissionRoleMapping, Integer> {

}
