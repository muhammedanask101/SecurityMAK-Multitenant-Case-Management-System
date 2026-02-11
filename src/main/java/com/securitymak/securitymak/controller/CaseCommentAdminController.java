package com.securitymak.securitymak.controller;

import com.securitymak.securitymak.service.CaseCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CaseCommentAdminController {

    private final CaseCommentService caseCommentService;

    @DeleteMapping("/{commentId}")
    public void deleteComment(@PathVariable Long commentId) {
        caseCommentService.deleteComment(commentId);
    }
}