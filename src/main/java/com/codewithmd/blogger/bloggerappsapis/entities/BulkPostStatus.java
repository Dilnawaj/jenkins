package com.codewithmd.blogger.bloggerappsapis.entities;

import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "BulkPostStatus")
public class BulkPostStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String errorMessage;

    private Integer status;

    private String postTitle;


    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime savedAt;

}
