package com.taotie.service;

import com.taotie.dto.EmployeeLoginDTO;
import com.taotie.entity.Employee;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);
}
