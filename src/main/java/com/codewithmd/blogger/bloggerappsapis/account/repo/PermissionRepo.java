package com.codewithmd.blogger.bloggerappsapis.account.repo;



import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import com.codewithmd.blogger.bloggerappsapis.account.entity.Permission;


@Repository
public interface PermissionRepo extends JpaRepository<Permission, Integer> {

	

	public Permission findByName(String name);

	public Permission findByCode(String code);

}
