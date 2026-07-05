package com.github.sansarch.task_management.application.group.port.in;

import com.github.sansarch.task_management.application.group.dto.GroupResult;

import java.util.List;

public interface ListMyGroupsUseCase {
    List<GroupResult> listMine();
}
