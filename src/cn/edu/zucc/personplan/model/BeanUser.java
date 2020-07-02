package cn.edu.zucc.personplan.model;

import java.util.Date;

public class BeanUser {
    public static BeanUser currentLoginUser = null;
    private  String userid;


    public BeanUser(String userid) {
        this.userid=userid;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

}
