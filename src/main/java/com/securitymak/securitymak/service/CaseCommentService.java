package com.securitymak.securitymak.service;

import com.securitymak.securitymak.dto.CommentResponse;
import com.securitymak.securitymak.dto.CreateCommentRequest;
import com.securitymak.securitymak.exception.CaseNotFoundException;
import com.securitymak.securitymak.exception.InvalidCaseTransitionException;
import com.securitymak.securitymak.exception.ResourceNotFoundException;
import com.securitymak.securitymak.exception.UnauthorizedCaseAccessException;
import com.securitymak.securitymak.model.AuditAction;
import com.securitymak.securitymak.model.Case;
import com.securitymak.securitymak.model.CaseComment;
import com.securitymak.securitymak.model.CaseStatus;
import com.securitymak.securitymak.model.User;
import com.securitymak.securitymak.repository.CaseCommentRepository;
import com.securitymak.securitymak.repository.CaseRepository;
import com.securitymak.securitymak.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CaseCommentService {

    private final CaseRepository caseRepository;
    private final CaseCommentRepository caseCommentRepository;
    private final AuditService auditService;
    private final CaseAccessService caseAccessService;

    @Transactional
    public CommentResponse addComment(Long caseId, CreateCommentRequest request) {

        User currentUser = SecurityUtils.getCurrentUser();
        boolean isAdmin = SecurityUtils.isAdmin();

        Case c = caseRepository.findById(caseId)
                .orElseThrow(CaseNotFoundException::new);

        // 🔐 Centralized access validation
        caseAccessService.validateTenantAccess(c);
        caseAccessService.validateCaseAccess(c, currentUser);

        // Comment sensitivity must not exceed user clearance
        if (!currentUser.getClearanceLevel()
                .canAccess(request.sensitivityLevel())) {
            throw new UnauthorizedCaseAccessException("Cannot comment above your clearance level");
        }

        // Freeze CLOSED and ARCHIVED for normal users
        if (!isAdmin &&
                (c.getStatus() == CaseStatus.CLOSED
                        || c.getStatus() == CaseStatus.ARCHIVED)) {
            throw new InvalidCaseTransitionException("Cannot comment on closed or archived case");
        }

        CaseComment comment = CaseComment.builder()
                .caseEntity(c)
                .author(currentUser)
                .content(request.content())
                .sensitivityLevel(request.sensitivityLevel())
                .createdAt(LocalDateTime.now())
                .build();

        caseCommentRepository.save(comment);

        auditService.log(
                currentUser.getEmail(),
                AuditAction.COMMENT_ADDED,                "CASE",
                caseId,
                null,
                "Comment ID: " + comment.getId(),
                SecurityUtils.getCurrentTenantId()
        );

        return toResponse(comment);
    }

  public List<CommentResponse> getCommentsForCase(Long caseId) {

    User currentUser = SecurityUtils.getCurrentUser();

    Case c = caseRepository.findById(caseId)
            .orElseThrow(CaseNotFoundException::new);

    caseAccessService.validateTenantAccess(c);
    caseAccessService.validateCaseAccess(c, currentUser);

    return caseCommentRepository
            .findAllCommentsForCase(
                    caseId,
                    SecurityUtils.getCurrentTenantId()
            )
            .stream()
            .filter(comment ->
                    currentUser.getClearanceLevel()
                            .canAccess(comment.getSensitivityLevel())
            )
            .map(this::toResponse)
            .toList();
}

    private CommentResponse toResponse(CaseComment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getAuthor().getEmail(),
                comment.getContent(),
                comment.getSensitivityLevel(),
                comment.getCreatedAt()
        );
    }

    @Transactional
public void deleteComment(Long commentId) {

    SecurityUtils.requireAdmin();

    CaseComment comment = caseCommentRepository.findById(commentId)
            .orElseThrow(() -> new ResourceNotFoundException("Case not found"));

    caseCommentRepository.delete(comment);

    auditService.log(
            SecurityUtils.getCurrentUserEmail(),
            AuditAction.COMMENT_DELETED,
            "COMMENT",
            commentId,
            null,
            null,
            SecurityUtils.getCurrentTenantId()
    );
}
}
