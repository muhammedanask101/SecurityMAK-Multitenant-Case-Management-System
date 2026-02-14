package com.securitymak.securitymak.controller;

import com.securitymak.securitymak.dto.InviteView;
import com.securitymak.securitymak.model.Invite;
import com.securitymak.securitymak.model.InviteStatus;
import com.securitymak.securitymak.model.SensitivityLevel;
import com.securitymak.securitymak.service.InviteService;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invites")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class InviteController {

    private final InviteService inviteService;

    @PostMapping
    public InviteView createInvite(@RequestBody CreateInviteRequest request) {
        return inviteService.createInvite(
                request.getEmail(),
                request.getRole(),
                request.getClearanceLevel()
        );
    }

    @PostMapping("/{id}/terminate")
    public void terminateInvite(@PathVariable Long id) {
        inviteService.terminateInvite(id);
    }

    @Data
    public static class CreateInviteRequest {
        private String email;
        private String role;
        private SensitivityLevel clearanceLevel;
    }

    @DeleteMapping("/{id}")
    public void deleteInvite(@PathVariable Long id) {
        inviteService.deleteInvite(id);
    }

    @PostMapping("/accept")
    @PreAuthorize("permitAll()")
    public void acceptInvite(@RequestBody AcceptInviteRequest request) {
        inviteService.registerViaInvite(
                request.getToken(),
                request.getEmail(),
                request.getPassword()
        );
    }



@Data
public static class AcceptInviteRequest {
    private String token;
    private String email;
    private String password;
}

@PostMapping("/{id}/approve")
public void approve(@PathVariable Long id) {
    inviteService.approveInvite(id);
}

@PostMapping("/{id}/reject")
public void reject(@PathVariable Long id) {
    inviteService.rejectInvite(id);
}

@GetMapping
public org.springframework.data.domain.Page<InviteView> listInvites(
        @RequestParam(required = false) InviteStatus status,
        org.springframework.data.domain.Pageable pageable
) {
    return inviteService.getInvites(status, pageable);
}

@GetMapping("/{id}")
public InviteView getInvite(@PathVariable Long id) {
    return inviteService.getInviteById(id);
}
}
