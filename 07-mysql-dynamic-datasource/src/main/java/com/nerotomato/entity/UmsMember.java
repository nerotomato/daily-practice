package com.nerotomato.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @Entity: 实体类, 必须
 * @Table: 对应数据库中的表, 必须, name=表名,
 * Indexes是声明表里的索引, columnList是索引的列, 同时声明此索引列是否唯一, 默认false
 * Created by nero on 2021/4/27.
 */
@ApiModel
@Data
public class UmsMember {
    private long id;
    private String username;
    private String password;
    private String nickname;
    private String telephone;
    private int status;
    private int gender;
    //@JsonFormat注解会将数据库中数据按照如下格式返回给前端
    @ApiModelProperty(value = "生日", example = "2021-5-12")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date birthday;
    private String city;
    private String job;
    //@JsonFormat注解会将数据库中数据按照如下格式返回给前端
    @ApiModelProperty(value = "创建时间", example = "yyyy-MM-dd HH:mm:ss.SSS")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
    private Date create_time;
    //@JsonFormat注解会将数据库中数据按照如下格式返回给前端

    @ApiModelProperty(value = "修改时间", example = "yyyy-MM-dd HH:mm:ss.SSS")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
    private Date update_time;
}
