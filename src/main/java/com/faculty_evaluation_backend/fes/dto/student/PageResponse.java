package com.faculty_evaluation_backend.fes.dto.student;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class PageResponse<T>{
    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int page;
    private int size;
}
