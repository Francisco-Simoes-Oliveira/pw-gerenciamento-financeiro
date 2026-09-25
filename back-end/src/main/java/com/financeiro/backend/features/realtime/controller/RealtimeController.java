package com.financeiro.backend.features.realtime.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.financeiro.backend.common.dto.ApiResponse;
import com.financeiro.backend.features.realtime.dto.RealtimeTicketResponse;
import com.financeiro.backend.features.realtime.service.RealtimeTicketService;
import com.financeiro.backend.security.services.UserDetailsImpl;

@RestController
@RequestMapping("/api/realtime")
public class RealtimeController {

    private final RealtimeTicketService ticketService;

    public RealtimeController(RealtimeTicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping("/ticket")
    public ResponseEntity<ApiResponse<RealtimeTicketResponse>> createTicket() {
        return ResponseEntity.ok(ApiResponse.success(ticketService.issue(getCurrentUserId())));
    }

    private UUID getCurrentUserId() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        return userDetails.getId();
    }
}
