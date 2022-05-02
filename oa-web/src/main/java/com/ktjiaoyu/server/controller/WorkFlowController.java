package com.ktjiaoyu.server.controller;

import com.ktjiaoyu.server.pojo.RespBean;
import com.ktjiaoyu.server.service.IWorkFlowService;
import com.ktjiaoyu.server.vo.WorkFlowVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * 工作流的控制器类
 * @Author JieGe
 * @Create 2022/4/10 - 21:49
 */
@Api("工作流的控制器类")
@RestController
@RequestMapping("/workFlow")
public class WorkFlowController {

    @Autowired
    private IWorkFlowService workFlowService;

    @ApiOperation("加载部署流程分页列表信息")
    @GetMapping("/loadAllDeployment")
    public RespBean loadAllDeployement(WorkFlowVo workFlowVo) {
        return workFlowService.queryProcessDeploy(workFlowVo);
    }

    @ApiOperation("加载流程定义信息数据")
    @GetMapping("/loadAllProcessDefinition")
    public RespBean loadAllProcessDefinition(WorkFlowVo workFlowVo) {
        return workFlowService.loadAllProcessDefinition(workFlowVo);
    }

    /**
     * 添加流程部署
     */
    @PostMapping("/addWorkFlow")
    @ApiOperation("添加流程部署")
    public RespBean addWorkFlow(WorkFlowVo workFlowVo) {
        try {
            workFlowService.addWorkFlow(workFlowVo.getUploadFile().getInputStream(), workFlowVo.getDeploymentName());
            return RespBean.success("流程部署成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return RespBean.error("流程部署失败!");
        }
    }

    /**
     * 通过流程部署ID删除流程部署信息
     */
    @GetMapping("/deleteWorkFlow/{deploymentId}")
    @ApiOperation("通过流程部署ID删除流程部署信息")
    public RespBean deleteWorkFlow(@PathVariable("deploymentId")String deploymentId) {
        try {
            workFlowService.deleteWorkFlow(deploymentId);
            return RespBean.success("删除流程部署信息成功!");
        } catch (Exception e) {
            e.printStackTrace();
         return RespBean.error("删除流程信息部署失败!");
        }
    }
    @GetMapping("/batchDeleteWorkFlow")
    @ApiOperation("通过流程部署ID集合,批量删除流程及流程定义流程信息")
    public RespBean batchDeleteWorkFlow(WorkFlowVo vo){
        String [] deployment = vo.getIds();
        for (String de : deployment) {
            this.workFlowService.deleteWorkFlow(de);
        }
        return RespBean.success("批量删除流程部署及流程定义信息成功!");
    }

    /**
     * 查看流程图
     */
    @ApiOperation("查看流程图")
    @GetMapping("/findProcessImage/{deploymentId}")
    public void findProcessImage(@PathVariable("deploymentId")String deploymentId, HttpServletResponse response) {
        InputStream strem = workFlowService.queryProcessDeploymentImage(deploymentId);
        BufferedImage image = null;
        ServletOutputStream outputStream = null;
        try {
            image = ImageIO.read(strem);
            outputStream = response.getOutputStream();
            // 将生成的流程图图片写入到输出流outputStream
            ImageIO.write(image, "JPEG", outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                strem.close();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
