package com.fpt.sealhackathon.mapper;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.fpt.sealhackathon.dto.criteriaTemplate.CriteriaTemplateRequest;
import com.fpt.sealhackathon.dto.criteriaTemplate.CriteriaTemplateResponse;
import com.fpt.sealhackathon.entity.CriteriaTemplate;

@Mapper(componentModel = "spring")
public interface CriteriaTemplateMapper {

    CriteriaTemplateResponse toResponse(CriteriaTemplate entity);

    List<CriteriaTemplateResponse> toResponseList(List<CriteriaTemplate> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    CriteriaTemplate toEntity(CriteriaTemplateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(CriteriaTemplateRequest request,
            @MappingTarget CriteriaTemplate entity);
}
