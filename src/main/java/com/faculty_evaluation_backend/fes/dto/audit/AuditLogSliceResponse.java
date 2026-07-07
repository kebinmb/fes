package com.faculty_evaluation_backend.fes.dto.audit;

import lombok.Builder;
import org.springframework.data.domain.Slice;

import java.util.List;

@Builder
public record AuditLogSliceResponse(
        List<AuditLogResponse> content,
        int page,
        int size,
        int numberOfElements,
        boolean first,
        boolean last,
        boolean hasNext,
        boolean hasPrevious
) {
    public static AuditLogSliceResponse from(Slice<AuditLogResponse> slice) {
        return AuditLogSliceResponse.builder()
                .content(slice.getContent())
                .page(slice.getNumber())
                .size(slice.getSize())
                .numberOfElements(slice.getNumberOfElements())
                .first(slice.isFirst())
                .last(slice.isLast())
                .hasNext(slice.hasNext())
                .hasPrevious(slice.hasPrevious())
                .build();
    }
}
