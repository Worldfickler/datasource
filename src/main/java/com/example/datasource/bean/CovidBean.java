package com.example.datasource.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author dell
 * @version 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CovidBean {

    private String provinceName;//省份名称
    private String provinceShortName;//省份短名
    private String cityName;//城市名称
    private Integer currentConfirmedCount;//当前确诊人数
    private Integer confirmedCount;//累计确诊人数
    private Integer suspectedCount;//疑似病例人数
    private Integer curedCount;//治愈人数
    private Integer deadCount;//死亡人数
    private Integer locationId;//地区位置id
    private Integer pid;//城市所归属的省份的位置位置id(父id)
    private String statisticsData;//每一天的统计数据
    private String cities;//下属城市
    private String datetime;//时间

}
