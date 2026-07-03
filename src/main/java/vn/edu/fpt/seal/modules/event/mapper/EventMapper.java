package vn.edu.fpt.seal.modules.event.mapper;

import vn.edu.fpt.seal.modules.event.dto.EventResponse;
import vn.edu.fpt.seal.modules.event.entity.Event;

public final class EventMapper {

    private EventMapper() {
    }

    public static EventResponse toResponse(Event e) {
        return EventResponse.builder()
                .id(e.getId())
                .title(e.getTitle())
                .description(e.getDescription())
                .status(e.getStatus())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
