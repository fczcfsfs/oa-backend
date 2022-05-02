package com.ktjiaoyu.server;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ktjiaoyu.server.mapper.EmployeeMapper;
import com.ktjiaoyu.server.mapper.MenuMapper;
import com.ktjiaoyu.server.pojo.Employee;
import com.ktjiaoyu.server.pojo.Menu;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.*;

@SpringBootTest
class OaWebApplicationTests {
    @Resource
    private MenuMapper menuMapper;

    @Autowired
    private EmployeeMapper employeeMapper;


    @Test
    void contextLoads() {

    }

    @Test
    public void test() {
        // 根据一级菜单获取二级和三级菜单的值
        List<Menu> menus = menuMapper.selectList(new QueryWrapper<Menu>().eq("menuLevel", 1));
        if (menus != null) {
            menus.forEach(menu -> {
                menu.setChildren(menuMapper.selectList(new QueryWrapper<Menu>().eq("parentId", menu.getId()).eq("menuLevel", menu.getMenuLevel() + 1)));
                if (menu.getChildren() != null) {
                    menu.getChildren().forEach(menu1 -> {
                        menu1.setChildren(menuMapper.selectList(new QueryWrapper<Menu>().eq("parentId", menu.getId()).eq("menuLevel", menu.getMenuLevel() + 1)));
                    });
                }
            });
        }

    }

    @Test
    public void TestMap() {
        Map<String, Object> list = new HashMap<String, Object>();

        Object value = null;
        String key = null;
        list.put("name", "小明");
        list.put("age", 18);
        for (Map.Entry<String, Object> entry : list.entrySet()) {
            value = entry.getValue();
            key = entry.getKey();
            System.out.println("key值为:"+key+",值为:"+value);
        }
        System.out.println(111);

    }
}

