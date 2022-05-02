package com.ktjiaoyu.server.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ktjiaoyu.server.mapper.LeavebillMapper;
import com.ktjiaoyu.server.pojo.*;
import com.ktjiaoyu.server.service.ILeavebillService;
import com.ktjiaoyu.server.vo.WorkFlowVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author jieGe
 * @since 2022-04-09
 */
@RestController
@Api(value = "处理请假单相关的请求")
@RequestMapping("/leavebill")
public class LeavebillController {

    @Resource
    private ILeavebillService leavebillService;

    @Resource
    private LeavebillMapper leavebillMapper;

    /**
     * 显示请假单分页列表信息
     *
     * @param currPageNo 当前页码
     * @param pageSize   每页显示的数据行数
     * @param title      请假标题
     * @param beginTime  按请假开始时间进行查询
     * @param endTime    按请假结束时间进行查询
     */
    @ApiOperation("显示请假单分页列表信息")
    @GetMapping("/list")
    public RespBean getPageUserList(
            @RequestParam(value = "currPageNo", defaultValue = "1") Integer currPageNo,
            @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "beginTime", required = false) String beginTime,
            @RequestParam(value = "endTime", required = false) String endTime,
            @RequestParam(value = "sort") String sort) {

        Page<Leavebill> page = new Page<Leavebill>(currPageNo, pageSize);
        // 得到当前登录用户对象
        Admin admin = (Admin) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
// 如果当前登录用户对象ID为1，即系统管理员，则可以查询所有的请假单列表，否则只能查看自己的请假单列表
        IPage<Leavebill> adminIPage = leavebillService.selectByPage(page, title, content, sort, beginTime, endTime, admin.getId());
        // 使用Math.toIntExact将Long类型转换成int类型
        Integer total = Math.toIntExact(adminIPage.getTotal());
        Integer current = Math.toIntExact(adminIPage.getCurrent());
        PageUtil<Leavebill> pageList = new PageUtil<Leavebill>(total, current, adminIPage.getRecords());
        return RespBean.success("获取请假单列表成功", pageList);
    }

    @PostMapping("/saveLeaveBill")
    @PreAuthorize("hasAuthority('/leavebill/saveLeaveBill')")
    @ApiOperation("新增请假单信息")
    public RespBean saveLeaveBill(@RequestBody Leavebill leavebill) {
        // 得到当前登录用户对象
        Admin admin = (Admin) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        leavebill.setUserid(admin.getId());
        if (leavebillService.save(leavebill)) {
            return RespBean.success("新增请假单信息成功!");
        } else {
            return RespBean.error("新增请假单信息失败!");
        }
    }


    // 通过请假单ID查询请假单详情信息
    @GetMapping("/getLeaveById/{id}")
    @ApiOperation("通过请假单ID查询请假单详情信息")
    public RespBean getLeaveById(@PathVariable("id") Integer id) {
        Leavebill leavebill = leavebillService.getById(id);
        return RespBean.success("查询请假单详情信息成功", leavebill);
    }

    // 保存修改后的请假单信息
    @PostMapping("/updateLeaveBill")
    @PreAuthorize("hasAuthority('/leavebill/updateLeaveBill')")
    @ApiOperation("保存修改后的请假单信息")
    public RespBean updateLeaveBill(@RequestBody Leavebill leavebill) {
        // 得到当前登录用户对象
        Admin admin = (Admin) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        leavebill.setUserid(admin.getId());
        if (leavebillService.saveOrUpdate(leavebill)) {
            return RespBean.success("修改请假单信息成功!");
        } else {
            return RespBean.error("修改请假单信息失败!");
        }
    }

    @GetMapping("/delLeaveBill/{id}")
    @PreAuthorize("hasAuthority('/leavebill/delLeaveBill')")
    @ApiOperation("删除请假单")
    public RespBean delLeaveBill(@PathVariable("id") Integer id) {
        if (leavebillService.removeById(id)) {
            return RespBean.success("删除请假单成功!");
        } else {
            return RespBean.error("删除请假单失败!");
        }
    }

    @ApiOperation("批量删除请假单申请")
    @GetMapping("batchDeleteLeaveBill")
    public RespBean batchDeleteLeaveBill(WorkFlowVo vo) {
        String[] demPloYeeId = vo.getIds();
        System.out.println(demPloYeeId.length);
        leavebillService.batchDeleteLeaveBill(demPloYeeId);
//        for (String de : demPloYeeId) {
//            leavebillService.removeById(de);
//        }
        return RespBean.success("删除请假单信息成功!");
    }

}

