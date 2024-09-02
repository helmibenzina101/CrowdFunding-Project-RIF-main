package com.rif.categories.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Data
public class CategoryRequest {

    // member variables
    @NotEmpty(message = "Category name must not be empty")
    @Size(min = 3, max = 30, message = "Category name must between 3  and 20 characters ")
    private String name;


}
