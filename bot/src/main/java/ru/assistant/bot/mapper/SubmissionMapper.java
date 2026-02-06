package ru.assistant.bot.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.assistant.bot.model.Submission;
import ru.assistant.bot.model.dto.request.SubmissionCreateRequest;
import ru.assistant.bot.model.dto.request.SubmissionUpdateRequest;
import ru.assistant.bot.model.dto.response.SubmissionResponse;
import ru.assistant.bot.model.enums.SubmissionStatus;

/**
 * SubmissionMapper
 * @author agent
 * @since 03.02.2026
 */
@Mapper(componentModel = "spring", uses = {StudentMapper.class, AssignmentMapper.class, AdminMapper.class})
public interface SubmissionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "assignment", ignore = true)
    @Mapping(target = "status", constant = "SUBMITTED")
    @Mapping(target = "submittedAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "reviewer", ignore = true)
    @Mapping(target = "reviewedAt", ignore = true)
    @Mapping(target = "resubmittedAt", ignore = true)
    @Mapping(target = "autoChecksPassed", ignore = true)
    @Mapping(target = "prNumber", ignore = true)
    @Mapping(target = "githubRepo", ignore = true)
    @Mapping(target = "commitHash", ignore = true)
    @Mapping(target = "branchName", ignore = true)
    @Mapping(target = "score", ignore = true)
    @Mapping(target = "reviewerComment", ignore = true)
    @Mapping(target = "version", constant = "0L")
    Submission toEntity(SubmissionCreateRequest request);

    @Mapping(source = "status", target = "statusDisplayName", qualifiedByName = "statusToDisplayName")
    @Mapping(source = "status", target = "statusEmoji", qualifiedByName = "statusToEmoji")
    SubmissionResponse toResponse(Submission submission);

    @Named("statusToDisplayName")
    default String statusToDisplayName(SubmissionStatus status) {
        return status.getDisplayName();
    }

    @Named("statusToEmoji")
    default String statusToEmoji(SubmissionStatus status) {
        return switch (status) {
            case SUBMITTED -> "📤";
            case UNDER_REVIEW -> "🔍";
            case NEEDS_REVISION -> "⚠️";
            case ACCEPTED -> "✅";
            case REJECTED -> "❌";
        };
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "assignment", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "submittedAt", ignore = true)
    @Mapping(target = "reviewedAt", ignore = true)
    @Mapping(target = "reviewer", ignore = true)
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    void updateEntity(@MappingTarget Submission submission, SubmissionUpdateRequest request);
}