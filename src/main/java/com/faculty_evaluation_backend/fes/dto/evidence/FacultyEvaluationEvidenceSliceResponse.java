package com.faculty_evaluation_backend.fes.dto.evidence;

import lombok.Builder;
import org.springframework.data.domain.Slice;

import java.util.List;

@Builder
public record FacultyEvaluationEvidenceSliceResponse(
        List<FacultyEvaluationEvidenceResponse> content,
        int page,
        int size,
        int numberOfElements,
        boolean first,
        boolean last,
        boolean hasNext,
        boolean hasPrevious
) {
    public static FacultyEvaluationEvidenceSliceResponse from(
            Slice<FacultyEvaluationEvidenceResponse> slice
    ) {
        return FacultyEvaluationEvidenceSliceResponse.builder()
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
