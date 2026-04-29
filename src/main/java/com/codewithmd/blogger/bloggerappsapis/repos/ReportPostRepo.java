package com.codewithmd.blogger.bloggerappsapis.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.codewithmd.blogger.bloggerappsapis.entities.ReportPost;

@Repository
public interface ReportPostRepo extends JpaRepository<ReportPost,Integer> {

}
