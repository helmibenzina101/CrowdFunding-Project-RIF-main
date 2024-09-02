package com.rif.categories.entity;

import com.rif.authentication.models.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.Base64;
import java.util.Date;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "categories")
public class Category {

    // member variables
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(length = 65555)
    private String imagePath;
    @Column(updatable = false)
    private Date createdAt;
    private Date updatedAt;


    // n:1 relationship : Many categories could belong to one user(admin role)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User creator;



    // getters & setters
    @PrePersist
    protected void setCreatedAt() {
        this.createdAt = new Date();
    }

    @PreUpdate
    protected void setUpdatedAt() {
        this.updatedAt = new Date();
    }

}
