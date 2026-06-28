package com.fpt.sealhackathon.mapper;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.fpt.sealhackathon.dto.university.UniversityRequest;
import com.fpt.sealhackathon.dto.university.UniversityResponse;
import com.fpt.sealhackathon.entity.University;

@Mapper(componentModel = "spring")
public interface UniversityMapper {

    University toEntity(UniversityRequest request);

    UniversityResponse toResponse(University university);

    List<UniversityResponse> toResponseList(List<University> universities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(
            UniversityRequest request,
            @MappingTarget University university
    );
}
