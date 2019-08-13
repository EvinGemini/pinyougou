package com.pinyougou.shop.security;

import com.alibaba.fastjson.JSON;
import com.pinyougou.http.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Component
@EnableWebSecurity
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    /*****
     * 忽略一些公开链接的权限设置
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/seller/add.shtml");
        web.ignoring().antMatchers("/*.html");
        web.ignoring().antMatchers("/css/**");
        web.ignoring().antMatchers("/img/**");
        web.ignoring().antMatchers("/js/**");
        web.ignoring().antMatchers("/plugins/**");
    }


    /****
     * 其他非公开链接的权限设置以及其他访问设置
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //其他链接地址都需要SELLER角色
        http.authorizeRequests().antMatchers("/**").access("hasRole('SELLER')");

        //登录设置
        http.formLogin().loginPage("/shoplogin.html")           //登录跳转地址
                .loginProcessingUrl("/login")                   //登录处理地址
                //.defaultSuccessUrl("/admin/index.html",true)    //登录后始终跳转到后台首页
                .successHandler(new AuthenticationSuccessHandler() {
                    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                        //响应数据封装
                        Result result = new Result(true, "/admin/index.html");
                        writerResult(response, result);


                    }
                })
                //.failureForwardUrl("/shoplogin.html");          //登录失败后跳转地址
                .failureHandler(new AuthenticationFailureHandler() {
                    //授权失败处理对象
                    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
                        //响应数据封装
                        Result result = new Result(false, "账号或者密码不正确！");

                        //将相应数据转成JSON
                        writerResult(response, result);
                    }
                });

        //登出设置
        http.logout().invalidateHttpSession(true)           //让session无效
                .logoutUrl("/logout")                       //登出处理地址
                .logoutSuccessUrl("/shoplogin.html");       //登出后跳转地址

        //发生异常跳转地址
        http.exceptionHandling().accessDeniedPage("/error.html");

        //允许跳转iframe
        http.headers().frameOptions().disable();

        //关闭csrf
        http.csrf().disable();
    }

    /***
     * 输出响应结果
     * @param response
     * @param result
     * @throws IOException
     */
    public void writerResult(HttpServletResponse response, Result result) throws IOException {
        //将相应数据转成JSON
        String jsonResult = JSON.toJSONString(result);

        //设置相应编码
        response.setContentType("application/json;charset=utf-8");

        //获得输出对象
        PrintWriter writer = response.getWriter();
        writer.write(jsonResult);
        writer.flush();
        writer.close();
    }

    /****
     * 用户授权
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        //使用自定义的认证类实现授权
        auth.userDetailsService(userDetailsService)
                .passwordEncoder(bCryptPasswordEncoder);    //指定加密对象
    }
}
