package com.ifox.platform.system.service.impl;

import com.ifox.platform.common.bean.SimpleOrder;
import com.ifox.platform.common.enums.EnumDao;
import com.ifox.platform.common.exception.BuildinSystemException;
import com.ifox.platform.common.page.SimplePage;
import com.ifox.platform.system.dao.RoleRepository;
import com.ifox.platform.system.entity.RoleEO;
import com.ifox.platform.system.exception.NotFoundAdminUserException;
import com.ifox.platform.system.request.role.RolePageRequest;
import com.ifox.platform.system.request.role.RoleQueryRequest;
import com.ifox.platform.system.request.role.RoleUpdateRequest;
import com.ifox.platform.system.service.RoleService;
import com.ifox.platform.utility.modelmapper.ModelMapperUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

import static com.ifox.platform.common.constant.ExceptionStatusConstant.BUILDIN_SYSTEM_EXP;
import static com.ifox.platform.common.constant.ExceptionStatusConstant.NOT_FOUND_ADMIN_USER_EXP;

@Service
@Transactional(readOnly = true)
public class RoleServiceImpl implements RoleService {

    @Resource
    private RoleRepository roleRepository;

    /**
     * 分页查询角色
     * @param pageRequest 分页参数
     * @return Page<RoleDTO>
     */
    @Override
    public SimplePage<RoleEO> page(RolePageRequest pageRequest) {
        List<SimpleOrder> simpleOrderList = pageRequest.getSimpleOrderList();
        Sort sort = null;
        for (SimpleOrder simpleOrder : simpleOrderList) {
            if (sort == null) {
                sort = new Sort(simpleOrder.getOrderMode() == EnumDao.OrderMode.DESC ? Sort.Direction.DESC : Sort.Direction.ASC, simpleOrder.getProperty());
            } else {
                sort.and(new Sort(simpleOrder.getOrderMode() == EnumDao.OrderMode.DESC ? Sort.Direction.DESC : Sort.Direction.ASC, simpleOrder.getProperty()));
            }
        }

        Pageable pageable = new PageRequest(pageRequest.getPageNo(), pageRequest.getPageSize(), sort);
        Page<RoleEO> roleEOSpringDataPage = roleRepository.findAllByNameLikeAndStatusEquals(pageRequest.getName(), pageRequest.getStatus(), pageable);

        return new SimplePage<>(roleEOSpringDataPage.getNumber(), roleEOSpringDataPage.getSize(), (int)roleEOSpringDataPage.getTotalElements(), roleEOSpringDataPage.getContent());
    }

    /**
     * 删除多个角色
     * @param ids ID
     */
    @Override
    @Transactional
    @Modifying
    public void delete(String[] ids) throws NotFoundAdminUserException, BuildinSystemException {
        for (String id : ids) {
            RoleEO roleEO = roleRepository.findOne(id);
            if (roleEO == null) {
                throw new NotFoundAdminUserException(NOT_FOUND_ADMIN_USER_EXP, "角色不存在");
            } else if(roleEO.getBuildinSystem()) {
                throw new BuildinSystemException(BUILDIN_SYSTEM_EXP, "系统内置角色，不允许删除");
            } else {
                roleRepository.delete(roleEO);
            }
        }
    }

    /**
     * 通过identifier查询角色
     * @param identifier identifier
     * @return RoleDTO
     */
    @Override
    public RoleEO getByIdentifier(String identifier) {
        List<RoleEO> roleEOList = roleRepository.findByIdentifier(identifier);
        if (!CollectionUtils.isEmpty(roleEOList)) {
            return roleEOList.get(0);
        }
        return null;
    }

    /**
     * list查询
     * @param queryRequest RoleQueryRequest
     * @return List<RoleDTO>
     */
    @Override
    public List<RoleEO> list(RoleQueryRequest queryRequest) {
        return roleRepository.findByNameLikeAndStatusEquals(queryRequest.getName(), queryRequest.getStatus());
    }

    /**
     * 保存角色
     * @param roleEO 角色实体
     */
    @Override
    @Transactional
    @Modifying
    public void save(RoleEO roleEO) {
        roleRepository.save(roleEO);
    }

    /**
     * 通过ID查询角色
     * @param id 角色ID
     * @return RoleEO
     */
    @Override
    public RoleEO get(String id) {
        return roleRepository.getOne(id);
    }

    /**
     * 更新角色
     * @param updateRequest 角色信息
     */
    @Override
    @Transactional
    @Modifying
    public void update(RoleUpdateRequest updateRequest) {
        RoleEO roleEO = roleRepository.getOne(updateRequest.getId());
        ModelMapperUtil.get().map(updateRequest, roleEO);
        roleEO.setMenuPermissionEOList(updateRequest.getMenuPermissionEOList());
    }

}
