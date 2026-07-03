package vn.edu.fpt.seal.modules.event.dto;

import jakarta.validation.constraints.Size;

public record UpdateEventRequest(
        @Size(max = 255) String title,
        @Size(max = 10000) String description
) {
}
