package com.ifox.platform.adminuser.rest;

import com.google.common.reflect.TypeToken;
import com.ifox.platform.adminuser.dto.ResourceDTO;
import com.ifox.platform.adminuser.request.resource.ResourcePageRequest;
import com.ifox.platform.adminuser.request.resource.ResourceSaveRequest;
import com.ifox.platform.adminuser.request.resource.ResourceUpdateRequest;
import com.ifox.platform.adminuser.response.ResourceVO;
import com.ifox.platform.adminuser.service.ResourceService;
import com.ifox.platform.common.page.Page;
import com.ifox.platform.common.rest.BaseController;
import com.ifox.platform.common.rest.PageInfo;
import com.ifox.platform.common.rest.response.BaseResponse;
import com.ifox.platform.common.rest.response.MultiResponse;
import com.ifox.platform.common.rest.response.OneResponse;
import com.ifox.platform.common.rest.response.PageResponse;
import com.ifox.platform.entity.common.ResourceEO;
import com.ifox.platform.utility.common.UUIDUtil;
import com.ifox.platform.utility.modelmapper.ModelMapperUtil;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Api(tags = "资源管理")
@Controller
@RequestMapping(value = "/resource", headers = {"api-version=1.0", "Authorization"})
public class ResourceController extends BaseController<ResourceVO> {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ResourceService resourceService;

    @ApiOperation("添加资源")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public @ResponseBody
    BaseResponse save(@ApiParam @RequestBody ResourceSaveRequest resource){
        String uuid = UUIDUtil.randomUUID();
        logger.info("保存资源 resource:{}, uuid:{}", resource.toString(), uuid);

        ResourceEO resourceEO = ModelMapperUtil.get().map(resource, ResourceEO.class);
        resourceService.save(resourceEO);

        logger.info(successSave + " uuid:{}", uuid);
        return successSaveBaseResponse();
    }

    @ApiOperation("删除资源")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ApiResponses({@ApiResponse(code = 400, message = "无效请求：ids为空"),
        @ApiResponse(code = 404, message = "资源不存在")})
    public @ResponseBody
    BaseResponse delete(@ApiParam @RequestBody String[] ids, HttpServletResponse response){
        String uuid = UUIDUtil.randomUUID();
        logger.info("删除资源 ids:{}, uuid:{}", Arrays.toString(ids), uuid);

        if (ids.length == 0){
            logger.info("无效请求,ids为空 uuid:{}", uuid);
            return invalidRequestBaseResponse(response);
        }

        try {
            resourceService.deleteMulti(ids);
        } catch (IllegalArgumentException e) {
            logger.info("资源不存在 uuid:{}", uuid);
            return notFoundBaseResponse("资源不存在", response);
        }

        logger.info(successDelete + " uuid:{}", uuid);
        return successDeleteBaseResponse();
    }

    @ApiOperation("获取指定资源")
    @RequestMapping(value = "/get/{resourceId}", method = RequestMethod.GET)
    @ApiResponses({@ApiResponse(code = 404, message = "资源不存在")})
    @SuppressWarnings("unchecked")
    public @ResponseBody
    OneResponse<ResourceVO> get(@ApiParam @PathVariable(name = "resourceId") String id, HttpServletResponse response){
        String uuid = UUIDUtil.randomUUID();
        logger.info("查询单个指定资源 id:{}, uuid:{}", id, uuid);

        ResourceEO resourceEO = resourceService.get(id);
        if (resourceEO == null){
            logger.info("资源不存在 id:{}, uuid:{}", id, uuid);
            return super.notFoundOneResponse("资源不存在", response);
        }

        ResourceVO resourceVO = ModelMapperUtil.get().map(resourceEO, ResourceVO.class);
        logger.info(successQuery + " uuid:{}", uuid);

        return successQueryOneResponse(resourceVO);
    }

    @ApiOperation("更新资源")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    @ApiResponses({@ApiResponse(code = 404, message = "资源不存在")})
    public @ResponseBody
    BaseResponse update(@ApiParam @RequestBody ResourceUpdateRequest resource, HttpServletResponse response){
        String uuid = UUIDUtil.randomUUID();
        logger.info("更新资源 resource:{}, uuid:{}", resource, uuid);

        String id = resource.getId();
        ResourceEO resourceEO = resourceService.get(id);
        if (resourceEO == null){
            logger.info("资源不存在 id:{}, uuid:{}", id, uuid);
            return super.notFoundOneResponse("资源不存在", response);
        }

        ModelMapperUtil.get().map(resource, resourceEO);
        resourceService.update(resourceEO);
        logger.info(successUpdate + " uuid:{}", uuid);

        return successUpdateBaseResponse();
    }

    @ApiOperation("分页查询资源")
    @RequestMapping(value = "/page", method = RequestMethod.POST)
    public @ResponseBody
    @SuppressWarnings("unchecked")
    PageResponse<ResourceVO> page(@ApiParam @RequestBody ResourcePageRequest pageRequest){
        String uuid = UUIDUtil.randomUUID();
        logger.info("分页查询资源 pageRequest:{}, uuid:{}", pageRequest, uuid);

        Page<ResourceDTO> resourceDTOPage = resourceService.page(pageRequest);
        List<ResourceDTO> resourceDTOList = resourceDTOPage.getContent();

        PageInfo pageInfo = resourceDTOPage.convertPageInfo();
        List<ResourceVO> resourceVOList = ModelMapperUtil.get().map(resourceDTOList, new TypeToken<List<ResourceVO>>() {}.getType());

        logger.info(successQuery + " uuid:{}", uuid);
        return successQueryPageResponse(pageInfo, resourceVOList);
    }

    @ApiOperation("获取所有资源")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public @ResponseBody
    MultiResponse<ResourceVO> list(){
        String uuid = UUIDUtil.randomUUID();
        logger.info("获取所有资源 uuid:{}", uuid);

        List<ResourceDTO> resourceDTOList = ModelMapperUtil.get().map(resourceService.listAll(), new TypeToken<List<ResourceDTO>>() {}.getType());
        List<ResourceVO> resourceVOList = ModelMapperUtil.get().map(resourceDTOList, new TypeToken<List<ResourceVO>>() {}.getType());

        logger.info(successQuery + " uuid:{}", uuid);
        return successQueryMultiResponse(resourceVOList);
    }
}
