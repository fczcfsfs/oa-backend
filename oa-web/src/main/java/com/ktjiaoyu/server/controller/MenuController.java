package com.ktjiaoyu.server.controller;

import com.ktjiaoyu.server.pojo.RespBean;
import com.ktjiaoyu.server.pojo.Menu;
import com.ktjiaoyu.server.service.IMenuService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author JieGe
 * @Create 2022/1/26 - 22:52
 * @Description 菜单控制器
 */
@RestController
@RequestMapping("/system/cfg")
public class MenuController {

    @Resource
    private IMenuService menuService;

    @ApiOperation(value = "通过用户ID查询菜单列表")
    @GetMapping("/menu")
    public List<Menu> getMenusByAdminId() {
        return menuService.getMenusByAdminId();
    }


    @ApiOperation("获取所有的菜单列表(非分页带层级关系用于绑定树形菜单控件)")
    @RequestMapping("/getAllMenuList")
    public List<Menu> getAllMenuList() {
        return menuService.getAllMenuList();
    }

    @ApiOperation("根据角色ID查询当前角色所拥有的权限id集合(格式为以逗号分隔的字符串)")
    @RequestMapping("/getMenuIdsByRoleId/{roleId}")
    public String getMenuIdsByRoleId(@PathVariable("roleId") Integer roleId) {
        return menuService.getMenuIdsByRoleId(roleId);
    }

    @ApiOperation("保存菜单信息")
    @RequestMapping("/saveMenu")
    public RespBean saveMenu(@RequestBody Menu menu) {
        if (menuService.saveMenu(menu)) {
            return RespBean.success("菜单信息保存成功!");
        } else {
            return RespBean.error("菜单信息保存失败!");
        }
    }

    @ApiOperation("根据菜单ID删除指定的菜单信息")
    @RequestMapping("/delMenuById/{id}")
    public RespBean delMenuById(@PathVariable("id") Integer id) {
        if (menuService.delMenuById(id)) {
            return RespBean.success("菜单信息删除成功!");
        } else {
            return RespBean.error("菜单信息删除失败!");
        }
    }

    @ApiOperation("通过菜单id查询菜单详情对象信息")
    @GetMapping("/findByMenuId/{id}")
    public Menu getById(@PathVariable("id") Integer id) {
        return menuService.getById(id);
    }

    @ApiOperation("保存修改后菜单信息")
    @PostMapping("/updateMenu")
    public RespBean updateMenu(@RequestBody Menu menu) {
        if (menuService.updateMenu(menu)) {
            return RespBean.success("修改菜单信息成功！");
        } else {
            return RespBean.error("修改菜单信息失败");
        }
    }
    @ApiOperation("查询")
    @PostMapping("/getList")
    public List<Menu> getList() {
        return menuService.getList();
    }
}
