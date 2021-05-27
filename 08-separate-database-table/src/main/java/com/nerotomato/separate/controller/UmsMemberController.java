package com.nerotomato.separate.controller;

import com.nerotomato.separate.entity.PageRequest;
import com.nerotomato.separate.entity.UmsMember;
import com.nerotomato.separate.service.UmsMemberService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 会员用户controller
 * Created by nero on 2021/4/28.
 */
@Api(tags = "UmsMemberController", value = "会员用户管理api")
@Controller
@RequestMapping("/umsMember")
public class UmsMemberController {

    @Autowired
    UmsMemberService umsMemberService;

    @ApiOperation(value = "会员注册", notes = "会员注册任务详细描述", httpMethod = "POST", produces = MediaType.APPLICATION_JSON_VALUE)
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @ResponseBody
    public Object save(@RequestBody UmsMember member) {
        return umsMemberService.save(member);
    }

    @ApiOperation(value = "删除会员", notes = "删除会员任务详细描述", httpMethod = "DELETE", produces = MediaType.APPLICATION_JSON_VALUE)
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    @ResponseBody
    public Object delete(@RequestBody UmsMember member) {
        umsMemberService.delete(member);
        return "delete user of:" + member.getUsername() + "successfully!";
    }

    @ApiOperation(value = "查询所有会员", notes = "查询所有会员任务详细描述", httpMethod = "GET", produces = MediaType.APPLICATION_JSON_VALUE)
    @RequestMapping(value = "/findAll", method = RequestMethod.GET)
    @ResponseBody
    public Object findAll() {
        List<UmsMember> allMembers = umsMemberService.findAll();
        return allMembers;
    }

    @ApiOperation(value = "分页查询会员", notes = "分页查询会员任务详细描述",
            httpMethod = "POST", produces = MediaType.APPLICATION_JSON_VALUE)
    /*@ApiImplicitParams({
            @ApiImplicitParam(paramType = "query",name = "page", value = "页数", required = true, dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "size", value = "条数", required = true, dataType = "int")
    })*/
    @ApiResponses({
            @ApiResponse(code = 500, message = "系统异常")
    })
    @RequestMapping(value = "/findByPage", method = RequestMethod.POST)
    @ResponseBody
    public Object findByPage(@RequestBody PageRequest pageRequest) {
        return umsMemberService.findByPage(pageRequest);
    }
}
