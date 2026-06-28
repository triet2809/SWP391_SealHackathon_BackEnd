package com.fpt.sealhackathon.mapper;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.fpt.sealhackathon.dto.RoundTrack.RoundTrackRequest;
import com.fpt.sealhackathon.dto.RoundTrack.RoundTrackResponse;
import com.fpt.sealhackathon.entity.RoundTrack;

@Mapper(componentModel = "spring", uses = {
        EventMapper.class,
        RoundMapper.class
})
public interface RoundTrackMapper {
    RoundTrackResponse toResponse(RoundTrack entity);

    List<RoundTrackResponse> toResponseList(List<RoundTrack> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "round", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    RoundTrack toEntity(RoundTrackRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "round", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(
            RoundTrackRequest request,
            @MappingTarget RoundTrack entity);
}
