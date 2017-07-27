package com.ifox.platform.adminuser.service;

import com.ifox.platform.adminuser.dto.RoleDTO;
import com.ifox.platform.adminuser.request.role.RolePageRequest;
import com.ifox.platform.baseservice.GenericService;
import com.ifox.platform.common.page.Page;
import com.ifox.platform.entity.sys.RoleEO;

public interface RoleService extends GenericService<RoleEO, String> {

    /**
     * 分页查询角色
     * @param pageRequest 分页参数
     * @return Page<RoleDTO>
     */
    Page<RoleDTO> page(RolePageRequest pageRequest);

}
