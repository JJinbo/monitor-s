package com.ideal.login.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ideal.login.mapper.LoginMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @Auther: devoty
 * @Date: 2018/12/24 14:21
 * @Description:
 * 微信登录请求地址：GET https://api.weixin.qq.com/sns/jscode2session?appid=APPID&secret=SECRET&js_code=JSCODE&grant_type=authorization_code
 * appid	string		是	小程序 appId       wxe4b8de111db51cf5                   wxe73443b05ccf371f
 * secret	string		是	小程序 appSecret   ae7295ace6dee7edff2030bae13258a2     01b154dfc0bdb95f41b6a8f16e48d906
 * js_code	string		是	登录时获取的 code
 * grant_type	string		是	授权类型，此处只需填写 authorization_code
 */

@Controller
public class LoginController {
//
//    @Autowired
//    RestTemplate restTemplate;
//

    @Autowired
    LoginMapper loginMapper;



    //能查到用户  （是否有用户处于登录状态）
    //查不到用户 （注册）
    //
    @RequestMapping("/login")
    @ResponseBody
    public Map<String ,Object> login(String code){

        Map<String ,Object> user = new HashMap<>();
//        System.out.println(code);

        String appID = "wxe73443b05ccf371f";

        String secret = "01b154dfc0bdb95f41b6a8f16e48d906";

        RestTemplate restTemplate = new RestTemplate();
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid="+appID+"&secret="+secret+"&js_code="+code+"&grant_type=authorization_code";
        String result = restTemplate.getForObject(url, String.class);

        JSONObject jsStr = JSONObject.parseObject(result);


        String openId = (String) jsStr.get("openid");

        List<Map> userInfos =  loginMapper.queryUserInfo(openId);

        if(null == userInfos || userInfos.size()==0){
            user.put("statu", 1);
            return user;
        }

        for(Map userInfo : userInfos){

            int statu = (Integer)userInfo.get("status");
            if (statu == 0){
                //正常登陆
                user.put("statu", 0);
                user.putAll(userInfo);
                return user;
            }else {
                //已注册，但未登陆
                user.put("statu", 2);
            }


        }




        System.out.println(user);

//        System.out.println(result);

        return user;

    }

    @RequestMapping("/loginAregister")
    @ResponseBody
    public Map<String, Object> loginAregister(String code, String userName, String passWd){
        Map<String, Object> loginInfo = new HashMap<>();
        String appID = "wxe73443b05ccf371f";

        String secret = "01b154dfc0bdb95f41b6a8f16e48d906";

        RestTemplate restTemplate = new RestTemplate();
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid="+appID+"&secret="+secret+"&js_code="+code+"&grant_type=authorization_code";
        String result = restTemplate.getForObject(url, String.class);

        JSONObject jsStr = JSONObject.parseObject(result);


        String openId = (String) jsStr.get("openid");


        Map<String , String> userInfo =  loginMapper.queryUserInfoByName(userName);

        Map<String, String> user = new HashMap<>();
        user.put("userName", userName);
        user.put("passWd", passWd);
        user.put("openId", openId);
        //未查到用户，注册新用户
        if(null == userInfo ){
            //注册
            loginMapper.addUser(user);
            loginInfo.putAll(user);
            loginInfo.put("statu", 0);
            return loginInfo;
        }


        String PWD = userInfo.get("password");
        if (null != PWD && !"".equals(PWD) && !PWD.equals(passWd)) {
            //密码错误
            loginInfo.put("statu", 3);

        }else{
            //登录成功
            loginMapper.upUser(user);
            loginInfo.putAll(user);
            loginInfo.put("statu", 0);
        }

        return loginInfo;


    }

    @RequestMapping("/loginOut")
    @ResponseBody
    public  Map<String, Object> loginOut(String userName){
        Map<String, Object> loginInfo = new HashMap<>();
        loginMapper.loginOut(userName);
        loginInfo.put("statu", 0);

        return loginInfo;

    }




}
