package com.rif.categories.dto;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Setter
@Getter
@Data
public class CategoryResponse {

    // Member variables
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long category_id;
    private String name;
    @Column(length = 8000000)
    private String image;

    @Column(updatable = false)
    private Date createdAt;
    private Date updatedAt;

    // Beans constructor
    public CategoryResponse() {
    }

    // Full args constructor
    public CategoryResponse(Long category_id, Date updatedAt, Date createdAt, String image, String name) {
        this.category_id = category_id;
        this.updatedAt = updatedAt;
        this.createdAt = createdAt;
        this.image = image;
        this.name = name;
    }

    // Getters and setters

    @PrePersist
    protected void setCreatedAt() {
        this.createdAt = new Date();
    }

    @PreUpdate
    protected void setUpdatedAt() {
        this.updatedAt = new Date();
    }
}
