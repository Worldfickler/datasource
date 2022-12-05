package com.example.datasource.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author dell
 * @version 1.0
 * 防疫物资
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MaterialBean {

    private String  name;//物资名称
    private String from;//物资来源
    private Integer count;//物资数量

}
