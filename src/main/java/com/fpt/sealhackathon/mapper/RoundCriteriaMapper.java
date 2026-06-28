package com.fpt.sealhackathon.mapper;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.fpt.sealhackathon.dto.RoundCriteria.RoundCriteriaRequest;
import com.fpt.sealhackathon.dto.RoundCriteria.RoundCriteriaResponse;
import com.fpt.sealhackathon.entity.RoundCriteria;

@Mapper(componentModel = "spring", uses = {
        EventMapper.class,
        RoundMapper.class,
        RoundTrackMapper.class,
        CriteriaTemplateMapper.class
})
public interface RoundCriteriaMapper {

    RoundCriteriaResponse toResponse(RoundCriteria entity);

    List<RoundCriteriaResponse> toResponseList(List<RoundCriteria> list);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "round", ignore = true)
    @Mapping(target = "roundTrack", ignore = true)
    @Mapping(target = "template", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    RoundCriteria toEntity(RoundCriteriaRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "round", ignore = true)
    @Mapping(target = "roundTrack", ignore = true)
    @Mapping(target = "template", ignore = true)
    void updateEntity(RoundCriteriaRequest request,
            @MappingTarget RoundCriteria entity);
}
