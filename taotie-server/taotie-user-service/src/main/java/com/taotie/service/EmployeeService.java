package com.taotie.service;

import com.taotie.dto.EmployeeDTO;
import com.taotie.dto.EmployeeLoginDTO;
import com.taotie.dto.EmployeePageQueryDTO;
import com.taotie.entity.Employee;
import com.taotie.result.PageResult;

public interface EmployeeService {

    /**
     * 新增员工
     *
     * @paramEmployeeDTO
     * @return
     */
    void save(EmployeeDTO employeeDTO);

    /**
     * 员工分页查询
     *
     * @param employeePageQueryDTO
     * @return
     */
    PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 启用禁用员工账号
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);

    /**
     * 根据员工id查询
     * @param id
     * @return
     */
    Employee getById(Long id);

    /**
     * 编辑员工
     * @param employeeDTO
     */
    void update(EmployeeDTO employeeDTO);
}
