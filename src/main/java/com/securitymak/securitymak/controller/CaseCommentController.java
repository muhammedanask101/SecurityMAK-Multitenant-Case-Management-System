package com.securitymak.securitymak.controller;

import com.securitymak.securitymak.dto.CommentResponse;
import com.securitymak.securitymak.dto.CreateCommentRequest;
import com.securitymak.securitymak.service.CaseCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cases/{caseId}/comments")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class CaseCommentController {

    private final CaseCommentService caseCommentService;

    @PostMapping
    public CommentResponse addComment(
            @PathVariable Long caseId,
            @Valid @RequestBody CreateCommentRequest request
    ) {
        return caseCommentService.addComment(caseId, request);
    }

    @GetMapping
    public List<CommentResponse> getComments(
            @PathVariable Long caseId
    ) {
        return caseCommentService.getCommentsForCase(caseId);
    }
}
