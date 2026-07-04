package vn.edu.fpt.seal.modules.support.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.seal.common.exception.ApiException;
import vn.edu.fpt.seal.modules.support.dto.*;
import vn.edu.fpt.seal.modules.support.entity.SupportTicket;
import vn.edu.fpt.seal.modules.support.repository.SupportTicketRepository;
import vn.edu.fpt.seal.modules.user.repository.UserRepository;
import vn.edu.fpt.seal.security.CurrentUser;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SupportTicketService {
    private final SupportTicketRepository repo;
    private final UserRepository userRepo;

    private static final Set<String> ALLOWED_STATUS = Set.of("open", "in_progress", "resolved", "closed");

    private boolean isCoordinator(CurrentUser user) {
        return user.getRoles() != null && user.getRoles().stream().anyMatch(r -> r.equalsIgnoreCase("coordinator"));
    }

    /**
     * Coordinators see every ticket (optionally filtered by requesterId); any other
     * authenticated user can only ever see their own tickets, regardless of the
     * requesterId they pass. This prevents students from reading each other's tickets.
     */
    @Transactional(readOnly = true)
    public List<SupportTicketResponse> list(CurrentUser user, UUID requesterId) {
        if (isCoordinator(user)) {
            List<SupportTicket> tickets = (requesterId == null)
                    ? repo.findTop100ByOrderByCreatedAtDesc()
                    : repo.findByRequesterIdOrderByCreatedAtDesc(requesterId);
            return tickets.stream().map(this::map).toList();
        }
        // Non-coordinators are locked to their own tickets.
        return repo.findByRequesterIdOrderByCreatedAtDesc(user.getId()).stream().map(this::map).toList();
    }

    @Transactional
    public SupportTicketResponse create(CreateSupportTicketRequest r, UUID requesterId) {
        var u = userRepo.findById(requesterId).orElseThrow(() -> ApiException.notFound("Requester not found"));
        return map(repo.save(SupportTicket.builder()
                .requester(u)
                .category(r.category())
                .priority(r.priority())
                .subject(r.subject().trim())
                .description(r.description().trim())
                .build()));
    }

    /** Only coordinators may change a ticket's status. */
    @Transactional
    public SupportTicketResponse updateStatus(CurrentUser user, UUID id, UpdateSupportTicketStatusRequest r) {
        if (!isCoordinator(user)) throw ApiException.forbidden("Only coordinators can update ticket status");
        String status = r.status() == null ? "" : r.status().trim().toLowerCase();
        if (!ALLOWED_STATUS.contains(status)) throw ApiException.badRequest("Invalid status: " + r.status());
        SupportTicket t = repo.findById(id).orElseThrow(() -> ApiException.notFound("Ticket not found"));
        t.setStatus(status);
        return map(repo.save(t));
    }

    private SupportTicketResponse map(SupportTicket t) {
        var u = t.getRequester();
        return new SupportTicketResponse(t.getId(), u.getId(), u.getFullName(), u.getEmail(),
                t.getCategory(), t.getPriority(), t.getSubject(), t.getDescription(),
                t.getStatus(), t.getCreatedAt(), t.getUpdatedAt());
    }
}
