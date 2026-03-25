package com.faculty_evaluation_backend.fes.utilities.mapper;

import com.faculty_evaluation_backend.fes.dto.student.PageResponse;
import org.springframework.data.domain.Page;

public class PageMapper {
    public static <T>PageResponse<T> toPageResponse(Page<T> page){
        return new PageResponse<>(
                page.getContent(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize()
        );
    }
}
