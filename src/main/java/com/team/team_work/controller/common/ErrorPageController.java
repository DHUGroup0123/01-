/**
 * 严肃声明：
 * 开源版本请务必保留此注释头信息，若删除我方将保留所有法律责任追究！
 * 本系统已申请软件著作权，受国家版权局知识产权以及国家计算机软件著作权保护！
 * 可正常分享和学习源码，不得用于违法犯罪活动，违者必究！
 * Copyright (c) 2019-2020 十三 all rights reserved.
 * 版权所有，侵权必究！
 */
package com.team.team_work.controller.common;
import org.springframework.boot.webmvc.autoconfigure.error.ErrorViewResolver;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
@Controller public class ErrorPageController implements ErrorViewResolver {
 public ModelAndView resolveErrorView(HttpServletRequest request,HttpStatus status,Map<String,Object> model){return new ModelAndView(status==HttpStatus.NOT_FOUND?"error/error_404":status==HttpStatus.BAD_REQUEST?"error/error_400":"error/error_5xx");}
}
