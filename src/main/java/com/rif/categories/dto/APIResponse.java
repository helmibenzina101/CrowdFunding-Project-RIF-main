package com.rif.categories.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Data
@AllArgsConstructor
@Getter
@Setter
public class APIResponse<T> {

    private int count;
    private T response;

}
