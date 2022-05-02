package com.ktjiaoyu.server.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ktjiaoyu.server.pojo.*;
import com.ktjiaoyu.server.service.IRoleService;
import com.ktjiaoyu.server.pojo.*;
import com.ktjiaoyu.server.service.IMenuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author jieGe
 * @since 2022-02-12
 */
@Api(tags = "用于处理角色模块的相关请求")
@RestController
@RequestMapping("/role")

public class RoleController {
    @Resource
    private IRoleService roleService;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Resource
    private IMenuService menuService;

    @ApiOperation("查询所有角色列表信息")
    @RequestMapping("/list")
    public List<Role> list() {
        return roleService.roleList();
    }

    @ApiOperation("为用户分配角色")
    @PostMapping("/assignRole")
    public RespBean assignRole(@RequestBody AdminRoleVo vo) {
        if (roleService.batchInsertAdminRole(vo)) {
            return RespBean.success("分配角色成功!");
        } else {
            return RespBean.error("分配角色失败!");
        }
    }

    @ApiOperation("通过操作员ID查询其拥有的角色列表权限")
    @RequestMapping("/getAdminRoleList/{adminId}")
    public AdminRoleVo getAdminRoleList(@PathVariable("adminId") Integer adminId) {
        return roleService.getAdminRoleList(adminId);
    }

    /**
     * 查询t_role角色表的分页列表信息
     * @param currPageNo 当前页码
     * @param pageSize 每页显示的数据行数
     * @param roleName 角色的英文和中文名称(查询条件)
     */
    @ApiOperation("查询t_role角色表的分页列表信息")
    @RequestMapping("/pageRoleList")
    public RespBean pageRoleList(
            @RequestParam(value ="currPageNo", defaultValue = "1")Integer currPageNo,
            @RequestParam(value ="pageSize", defaultValue = "4")Integer pageSize,
            @RequestParam(value = "roleName", required = false) String roleName,
            @RequestParam(value = "sort")String sort) {

        Page<Role> page = new Page<Role>(currPageNo, pageSize);
        IPage<Role> adminIPage = roleService.selectByPage(page, roleName, sort);
        // 使用Math.toIntExact将Long类型转换成int类型
        Integer total = Math.toIntExact(adminIPage.getTotal());
        Integer current = Math.toIntExact(adminIPage.getCurrent());
        PageUtil<Role> pageList = new PageUtil<Role>(total,current,adminIPage.getRecords());
        return RespBean.success("获取角色列表列表成功", pageList);
    }

    /**
     * 根据角色ID删除对应的权限
     * @param roleId 角色ID
     * @param menuId 要删除的权限ID
     * @return 删除成功后，将当前角色最新的权限列表进行返回
     */

    @ApiOperation("根据角色ID删除对应的权限")
    @RequestMapping("/delMenuByRoleId/{roleId}/{menuId}")
    @PreAuthorize("hasRole('admin')")
    public RespBean delMenuByRoleId(@PathVariable("roleId")Integer roleId,
                                    @PathVariable("menuId")Integer menuId) {
        // 根据角色ID删除对应的权限,将当前角色最新的权限列表进行返回
        List<Menu> menus = roleService.delMenuByRoleId(roleId, menuId);
        return RespBean.success("权限删除成功", menus);
    }

    @ApiOperation("添加角色")
    @RequestMapping("/saveRole")
    public RespBean saveRole(@RequestBody Role role){
        if (roleService.saveRole(role)){
            return RespBean.success("添加角色信息成功!");
        }else{
            return RespBean.success("添加角色信息失败!");
        }
    }

    @ApiOperation("通过角色id查询角色详情对象信息")
    @GetMapping("/getById/{id}")
    public Role getById(@PathVariable("id")Integer id) {
        return roleService.getById(id);
    }

    @ApiOperation("保存修改后角色信息")
    @PostMapping("/updateRole")
    public RespBean updateRole(@RequestBody Role role) {
        if (roleService.updateRole(role)) {
            return RespBean.success("修改角色信息成功！");
        } else {
            return RespBean.error("修改角色信息失败");
        }
    }

    @ApiOperation("为角色分配多个菜单权限")
    @PostMapping("/assignMenu")
    public RespBean assignMenu(@RequestBody RoleMenuVo vo) {
        if (roleService.batchInsertRoleMenu(vo)) {
            Integer adminId = ((Admin)(SecurityContextHolder.getContext().getAuthentication().getPrincipal())).getId();
            // 2、从redis中清空之前缓存的菜单列表数据
            redisTemplate.delete("menu_" + adminId);
            return RespBean.success("分配权限成功!");
        } else {
            return RespBean.error("分配权限失败!");
        }
    }

    @ApiOperation("通过角色ID删除角色信息")
    @RequestMapping("/delRole/{roleId}")
    public RespBean delRoleById(@PathVariable("roleId")Integer roleId) {
        if(roleService.delRoleById(roleId)) {
            return RespBean.success("删除角色成功！");
        } else {
            return  RespBean.success("删除角色失败！");
        }
    }


}
