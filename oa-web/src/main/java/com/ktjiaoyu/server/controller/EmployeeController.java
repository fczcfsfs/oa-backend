package com.ktjiaoyu.server.controller;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.afterturn.easypoi.excel.entity.enmus.ExcelType;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ktjiaoyu.server.pojo.*;
import com.ktjiaoyu.server.pojo.*;
import com.ktjiaoyu.server.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

@Api(tags = "用于处理员工基本资料相关的请求")
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Resource
    private IEmployeeService employeeService;

    /**
     * 显示员工基本资料分页列表信息
     *
     * @param currPageNo 当前页码
     * @param pageSize   每页显示的数据行数
     * @param empName    按员工名进行模糊查询
     * @param politicsId 按政治面貌ID进行精确查询
     * @param beginTime  按入职开始时间进行查询
     * @param endTime    按入职结束时间进行查询
     * @param deptId     按部门ID进行查询
     */
    @ApiOperation("显示员工基本资料分页列表信息")
    @GetMapping("/basic/list")
    public RespBean getPageUserList(
            @RequestParam(value = "currPageNo", defaultValue = "1") Integer currPageNo,
            @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
            @RequestParam(value = "empName", required = false) String empName,
            @RequestParam(value = "politicsId", required = false) Integer politicsId,
            @RequestParam(value = "deptId", required = false) Integer deptId,
            @RequestParam(value = "beginTime", required = false) String beginTime,
            @RequestParam(value = "endTime", required = false) String endTime,
            @RequestParam(value = "sort") String sort) {

        Page<Employee> page = new Page<Employee>(currPageNo, pageSize);
        IPage<Employee> adminIPage = employeeService.selectByPage(page, empName, sort, politicsId, deptId, beginTime, endTime);
        // 使用Math.toIntExact将Long类型转换成int类型
        Integer total = Math.toIntExact(adminIPage.getTotal());
        Integer current = Math.toIntExact(adminIPage.getCurrent());
        System.out.println(total);
        PageUtil<Employee> pageList = new PageUtil<Employee>(total, current, adminIPage.getRecords());
        return RespBean.success("获取员工基本资料列表成功", pageList);
    }

    @Resource
    private IPoliticsStatusService politicsStatusService;

    @ApiOperation("查询t_politics_status政治面貌表的列表信息")
    @GetMapping("/basic/politiclist")
    public List<PoliticsStatus> politiclist() {
        return politicsStatusService.list();
    }

    @Resource
    private IDepartmentService departmentService;

    @ApiOperation("查询t_department部门表的列表信息")
    @GetMapping("/basic/deptlist")
    public List<Department> deptlist() {
        return departmentService.list();
    }

    @PreAuthorize("hasAuthority('/employee/basic/exportData')")
    @ApiOperation("导出员工基本资料")
    @GetMapping(value = "/basic/export", produces = "application/octet-stream")
    public void exportEmployee(HttpServletResponse response,
                               @RequestParam(value = "currPageNo", defaultValue = "1") Integer currPageNo,
                               @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
                               @RequestParam(value = "empName", required = false) String empName,
                               @RequestParam(value = "politicsId", required = false) Integer politicsId,
                               @RequestParam(value = "deptId", required = false) Integer deptId,
                               @RequestParam(value = "beginTime", required = false) String beginTime,
                               @RequestParam(value = "endTime", required = false) String endTime,
                               @RequestParam(value = "sort", required = false) String sort) {
        // 按条件分页查询员工列表信息(实现按条件或分页导出，而不是全部导出)
        Page<Employee> page = new Page<Employee>(currPageNo, pageSize);
        IPage<Employee> adminIPage = employeeService.selectByPage(page, empName, sort, politicsId, deptId, beginTime, endTime);
        // 得到按条件查询并分页显示的员工列表信息
        List<Employee> list = adminIPage.getRecords();
        String title = "员工表_" + System.currentTimeMillis();
        // ExportParam对象的参数一title为导出的excel标题，参数二为sheetName值，参数三为导出后的excel类型
        // 其中HSSF为2003版本，XSSF为2007版本，我们这里选择兼容性更强的HSSF。
        ExportParams params = new ExportParams(title, "员工表", ExcelType.HSSF);
        // 创建工作簿对象
        Workbook workbook = ExcelExportUtil.exportExcel(params, Employee.class, list);
        // 创建输出流对象
        ServletOutputStream out = null;
        try {
            // 设置输出流的格式
            response.setHeader("content-type", "application/octet-stream");
            // 防止中文乱码
            response.setHeader("content-disposition", "attachment;filename=" + URLEncoder.encode(title + ".xls", "UTF-8"));
            out = response.getOutputStream();
            workbook.write(out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Resource
    private IPositionService positionService;
    @Resource
    private INationService nationService;
    @Resource
    private IJoblevelService joblevelService;

    @PreAuthorize("hasAuthority('/employee/basic/importData')")
    @ApiOperation("导入员工基本资料")
    @PostMapping(value = "/basic/importData")
    public RespBean importEmployee(MultipartFile file) {
        ImportParams params = new ImportParams();
        // 1、去掉标题行(因为导入的Excel表格的前两行是标题，这里需要去掉它)
        params.setTitleRows(1);
        try {
            // 2、调用ExcelImportUtil工具为的importExcel()方法得到Excel表中所有的员工信息
            List<Employee> list = ExcelImportUtil.importExcel(file.getInputStream(), Employee.class, params);
            // 使用forEach遍历导入的Excel表格里的员工基本信息，由于Excel基本信息中部门、职位等列是中文名，而我们插入到
            // t_employee表的数据是所对应的部门、职位等的ID值，所以下面要通过中文名获取所对应的ID值
            list.forEach((emp) -> {
                // 1、获取部门ID
                List<Department> departments = departmentService.list(new QueryWrapper<Department>().eq("name", emp.getDepartment().getName()));
                if (!CollectionUtils.isEmpty(departments)) emp.setDepartmentId(departments.get(0).getId());
                // 2、获取职位ID
                List<Position> positions = positionService.list(new QueryWrapper<Position>().eq("name", emp.getPosition().getName()));
                if (!CollectionUtils.isEmpty(positions)) emp.setPosId(positions.get(0).getId());
                // 3、获取政治面貌ID
                List<PoliticsStatus> statuses = politicsStatusService.list(new QueryWrapper<PoliticsStatus>().eq("name", emp.getPoliticsStatus().getName()));
                if (!CollectionUtils.isEmpty(statuses)) emp.setPoliticId(statuses.get(0).getId());
                // 4、获取民族ID
                List<Nation> nations = nationService.list(new QueryWrapper<Nation>().eq("name", emp.getNation().getName()));
                if (!CollectionUtils.isEmpty(nations)) emp.setNationId(nations.get(0).getId());
                // 5、获取职称ID
                List<Joblevel> joblevels = joblevelService.list(new QueryWrapper<Joblevel>().eq("name", emp.getJoblevel().getName()));
                if (!CollectionUtils.isEmpty(joblevels)) emp.setJobLevelId(joblevels.get(0).getId());
            });
            if (employeeService.saveBatch(list)) {
                return RespBean.success("导入成功!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RespBean.error("导入失败!");
    }

    @ApiOperation("查询t_department部门表带层级关系的部门列表信息")
    @GetMapping("/basic/treeDeptList")
    public List<Department> treeDeptList() {
        return departmentService.getAllDepartments();
    }

    //获取所有的民族信息
    @ApiOperation("获取所有的民族信息")
    @GetMapping("/basic/nations")
    public List<Nation> getAllNations() {
        return nationService.list();
    }

    @ApiOperation("获取所有的职称")
    @GetMapping("/basic/joblevels")
    public List<Joblevel> getAllJobLevel() {
        return joblevelService.list();
    }

    @ApiOperation("获取所有的职位")
    @GetMapping("/basic/positions")
    public List<Position> getAllPosition() {
        return positionService.list();
    }

    // 获取工号，原理是取t_employee表中workId列的最大值再+1
    @ApiOperation("获取工号")
    @GetMapping("/basic/maxWorkID")
    public RespBean maxWorkID() {
        return employeeService.maxWorkID();
    }

    @PreAuthorize("hasAuthority('/employee/basic/saveBasic')")
    @ApiOperation("新增员工基本资料")
    @PostMapping("/basic/saveBasic")
    public RespBean addEmp(@RequestBody Employee employee) {
        return employeeService.addEmp(employee);
    }

    @PreAuthorize("hasAuthority('/employee/basic/findById')")
    @ApiOperation("通过员工编号查询员工基本资料详情")
    @GetMapping("/basic/findById/{id}")
    public RespBean findById(@PathVariable("id") Integer id) {
        Employee employee = employeeService.getById(id);
        return RespBean.success("员工基本资料详情查询成功", employee);
    }

    @PreAuthorize("hasAuthority('/employee/basic/updateBasic')")
    @ApiOperation("修改员工基本资料")
    @PutMapping("/basic/updateBasic")
    public RespBean updateBasic(@RequestBody Employee employee) {
       /* if (employeeService.updateById(employee)) {
            return RespBean.success("修改员工基本资料成功!");
        } else {
            return RespBean.error("修改员工基本资料失败!");
        }*/
        return employeeService.updateById(employee) ? RespBean.success("修改员工资料成功!") : RespBean.error("修改员工资料失败!");
    }

    //  RespBean 类主要用于向前端返回数据，会在后端很多地方用到
    @PreAuthorize("hasAuthority('/employee/basic/delBasic')")
    @ApiOperation("删除员工基本资料")
    @DeleteMapping("/basic/delBasic/{id}")
    public RespBean delBasic(@PathVariable("id") Integer id) {
//        if (employeeService.removeById(id)) {
//            return RespBean.success("删除员工基本资料成功!");
//        } else {
//            return RespBean.error("删除员工基本资料失败!");
//        }
        return employeeService.removeById(id) ?
                RespBean.success("删除员工基本资料成功!") : RespBean.error("删除员工基本资料失败!");
    }
    @ApiOperation("获取员工基本资料")
    @GetMapping("/basic/getBasic")
    public List<Employee> test() {
        return employeeService.test();
    }
}
