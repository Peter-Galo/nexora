package com.nexora.util;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Utility class for generic mapping using ModelMapper.
 */
@Component
public class EntityMapper {

    private final ModelMapper modelMapper;

    public EntityMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public <D, T> D mapToDTO(T entity, Class<D> outClass) {
        if (entity == null) return null;
        return modelMapper.map(entity, outClass);
    }

    public <D, T> T mapToEntity(D dto, Class<T> entityClass) {
        if (dto == null) return null;
        return modelMapper.map(dto, entityClass);
    }

    public <D, T> void mapToExistingEntity(D dto, T entity) {
        if (dto != null && entity != null) {
            modelMapper.map(dto, entity);
        }
    }

}