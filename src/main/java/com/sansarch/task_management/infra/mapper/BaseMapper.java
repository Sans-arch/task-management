package com.sansarch.task_management.infra.mapper;

public interface BaseMapper<D, M> {
    D toDomain(M model);
    M toModel(D domain);
}
