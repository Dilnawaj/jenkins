package com.codewithmd.blogger.bloggerappsapis.account.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import com.codewithmd.blogger.bloggerappsapis.account.entity.ClientRole;

@Repository
public interface ClientRoleRepo extends JpaRepository<ClientRole, Long> {
	ClientRole findByClientId(Long clientId);
}
