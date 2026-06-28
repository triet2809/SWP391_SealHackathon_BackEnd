package com.fpt.sealhackathon.mapper;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.fpt.sealhackathon.dto.campus.CampusRequest;
import com.fpt.sealhackathon.dto.campus.CampusResponse;
import com.fpt.sealhackathon.entity.Campus;

@Mapper(componentModel = "spring")
public interface CampusMapper {

    @Mapping(target = "university", ignore = true)
    Campus toEntity(CampusRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "university", ignore = true)
    void updateEntityFromRequest(
            CampusRequest request,
            @MappingTarget Campus campus
    );

    CampusResponse toResponse(Campus campus);

    List<CampusResponse> toResponseList(List<Campus> campuses);

}
