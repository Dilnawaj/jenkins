package com.codewithmd.blogger.bloggerappsapis.account.service;

import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.stereotype.Service;

import com.codewithmd.blogger.bloggerappsapis.account.entity.ClientRole;
import com.codewithmd.blogger.bloggerappsapis.account.repo.ClientRoleRepo;


@Service
public class ClientRoleService {

	@Autowired
	private ClientRoleRepo clientRoleRepo;

	public ClientRole save(ClientRole clientRole) {
		return clientRoleRepo.save(clientRole);
	}
}