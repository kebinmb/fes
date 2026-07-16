package com.faculty_evaluation_backend.fes.services.data.admin;

import com.faculty_evaluation_backend.fes.dto.faculty.ClassFacultyAssignmentResponse;
import com.faculty_evaluation_backend.fes.dto.faculty.ClassFacultyReassignmentRequest;
import com.faculty_evaluation_backend.fes.dto.faculty.ClassFacultyReassignmentResponse;
import com.faculty_evaluation_backend.fes.dto.faculty.FacultyAssignmentOptionResponse;
import com.faculty_evaluation_backend.fes.dto.student.PageResponse;
import com.faculty_evaluation_backend.fes.entities.data.SchoolYearAndSemester;
import com.faculty_evaluation_backend.fes.entities.primary.PrimaryClass;
import com.faculty_evaluation_backend.fes.entities.primary.PrimaryFaculty;
import com.faculty_evaluation_backend.fes.entities.primary.enums.Status;
import com.faculty_evaluation_backend.fes.exceptions.BadRequestException;
import com.faculty_evaluation_backend.fes.exceptions.ResourceNotFoundException;
import com.faculty_evaluation_backend.fes.repositories.data.SchoolYearAndSemesterRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimaryClassRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimaryFacultyRepository;
import com.faculty_evaluation_backend.fes.utilities.mapper.PageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
class AdminClassAssignmentService {

    private final SchoolYearAndSemesterRepository schoolYearAndSemesterRepository;
    private final PrimaryClassRepository primaryClassRepository;
    private final PrimaryFacultyRepository primaryFacultyRepository;

    PageResponse<ClassFacultyAssignmentResponse> getClassAssignments(
            int page,
            int size,
            String search,
            String legacyDatabase
    ) {
        DashboardTerm term = resolveDashboardTerm();
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(
                safePage,
                safeSize,
                Sort.by(
                        Sort.Order.asc("subjectCode"),
                        Sort.Order.asc("classCode"),
                        Sort.Order.asc("primaryClassId")
                )
        );

        Page<ClassFacultyAssignmentResponse> assignments =
                primaryClassRepository.findAdminClassAssignments(
                        term.schoolYear(),
                        term.semester(),
                        normalizeOptional(search),
                        normalizeOptional(legacyDatabase),
                        pageable
                ).map(this::toClassFacultyAssignmentResponse);

        return PageMapper.toPageResponse(assignments);
    }

    List<FacultyAssignmentOptionResponse> getClassAssignmentFacultyOptions(
            String legacyDatabase
    ) {
        return primaryFacultyRepository.findAssignmentOptions(
                        Status.ACTIVE,
                        normalizeOptional(legacyDatabase)
                ).stream()
                .map(this::toFacultyAssignmentOptionResponse)
                .toList();
    }

    ClassFacultyReassignmentResponse reassignClassFaculty(
            Long primaryClassId,
            ClassFacultyReassignmentRequest request
    ) {
        if (primaryClassId == null) {
            throw new BadRequestException("Primary class ID is required.");
        }

        PrimaryClass primaryClass = primaryClassRepository
                .findAssignmentById(primaryClassId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Class assignment not found.")
                );

        DashboardTerm term = resolveDashboardTerm();
        if (!Objects.equals(primaryClass.getSchoolYear(), term.schoolYear())
                || !term.semester().equalsIgnoreCase(
                        primaryClass.getSemester()
                )) {
            throw new BadRequestException(
                    "Only classes in the current school year and semester can be reassigned."
            );
        }

        String previousFacultyId = normalizeOptional(
                primaryClass.getFacultyId()
        );
        String expectedFacultyId = normalizeOptional(
                request.expectedCurrentFacultyId()
        );
        if (!Objects.equals(previousFacultyId, expectedFacultyId)) {
            throw new BadRequestException(
                    "This class assignment has changed. Refresh the list and try again."
            );
        }

        String newFacultyId = request.facultyId().trim();
        PrimaryFaculty newFaculty = primaryFacultyRepository
                .findByFacultyId(newFacultyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Faculty not found.")
                );

        if (newFaculty.getStatus() != Status.ACTIVE) {
            throw new BadRequestException(
                    "Only an active faculty member can be assigned."
            );
        }

        String classDatabase = normalizeOptional(
                primaryClass.getLegacyDatabase()
        );
        String facultyDatabase = normalizeOptional(
                newFaculty.getLegacyDatabase()
        );
        if (classDatabase != null
                && !classDatabase.equalsIgnoreCase(facultyDatabase)) {
            throw new BadRequestException(
                    "The faculty member must belong to the same source database as the class."
            );
        }

        boolean changed = !Objects.equals(previousFacultyId, newFacultyId);
        if (changed) {
            primaryClass.setFacultyId(newFacultyId);
            primaryClassRepository.saveAndFlush(primaryClass);
            primaryClass.setFaculty(newFaculty);
        }

        return new ClassFacultyReassignmentResponse(
                toClassFacultyAssignmentResponse(primaryClass),
                previousFacultyId,
                newFacultyId,
                changed
        );
    }

    private DashboardTerm resolveDashboardTerm() {
        SchoolYearAndSemester activeTerm =
                schoolYearAndSemesterRepository.findByStatus(Status.ACTIVE)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "No active semester found."
                                )
                        );

        return new DashboardTerm(
                activeTerm.getSchoolYear(),
                activeTerm.getSemester().getValue()
        );
    }

    private ClassFacultyAssignmentResponse toClassFacultyAssignmentResponse(
            PrimaryClass primaryClass
    ) {
        PrimaryFaculty faculty = primaryClass.getFaculty();

        return new ClassFacultyAssignmentResponse(
                primaryClass.getPrimaryClassId(),
                primaryClass.getClassCode(),
                primaryClass.getSubjectCode(),
                primaryClass.getSubject() == null
                        ? null
                        : primaryClass.getSubject().getDescriptiveTitle(),
                primaryClass.getSectionId(),
                primaryClass.getSection() == null
                        ? null
                        : primaryClass.getSection().getProgramCode(),
                primaryClass.getSection() == null
                        ? null
                        : primaryClass.getSection().getYearLevel(),
                primaryClass.getSection() == null
                        ? null
                        : primaryClass.getSection().getSectionCode(),
                primaryClass.getFacultyId(),
                faculty == null ? null : formatFacultyName(faculty),
                primaryClass.getSchoolYear(),
                primaryClass.getSemester(),
                primaryClass.getLegacyDatabase(),
                primaryClass.getSourceCampus() == null
                        ? null
                        : primaryClass.getSourceCampus().name()
        );
    }

    private FacultyAssignmentOptionResponse toFacultyAssignmentOptionResponse(
            PrimaryFaculty faculty
    ) {
        return new FacultyAssignmentOptionResponse(
                faculty.getFacultyId(),
                formatFacultyName(faculty),
                faculty.getPosition(),
                faculty.getCollege() == null
                        ? null
                        : faculty.getCollege().name(),
                faculty.getLegacyDatabase()
        );
    }

    private String formatFacultyName(PrimaryFaculty faculty) {
        String firstName = normalizeOptional(faculty.getFirstname());
        String middleName = normalizeOptional(faculty.getMiddlename());
        String lastName = normalizeOptional(faculty.getLastname());

        return String.join(
                " ",
                java.util.stream.Stream.of(
                                firstName,
                                middleName,
                                lastName
                        )
                        .filter(Objects::nonNull)
                        .toList()
        );
    }

    private String normalizeOptional(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private record DashboardTerm(
            Integer schoolYear,
            String semester
    ) {
    }
}
