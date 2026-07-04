package vn.edu.fpt.seal.modules.support.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateSupportTicketStatusRequest(@NotBlank @Size(max = 30) String status) {
}
