package cn.edu.zucc.personplan.control.example;

import cn.edu.zucc.personplan.itf.IUserManager;
import cn.edu.zucc.personplan.model.BeanUser;
import cn.edu.zucc.personplan.util.BaseException;
import cn.edu.zucc.personplan.util.BusinessException;
import cn.edu.zucc.personplan.util.DBUtil_pool;

import java.sql.Connection;
import java.sql.SQLException;

public class ExampleUserManager implements IUserManager {

    @Override
    public BeanUser reg(String userid, String pwd, String pwd2) throws BaseException {
        // TODO Auto-generated method stub
        if (userid.length() <= 0 || userid.length() >= 30) {
            throw new BusinessException("用户名应该在1——30个字符之间！");
        }
        if (pwd.length() <= 0 || pwd.length() >= 30) {
            throw new BusinessException("密码应该在1——30个字符之间！");
        }
        if (!pwd.equals(pwd2)) {
            throw new BusinessException("两次输入的密码不一致");
        }
        Connection conn = null;
        try {
            conn = DBUtil_pool.getConnection();
            String sql = "select user_id from tbl_user where user_id = ?";
            java.sql.PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, userid);
            java.sql.ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                throw new BusinessException("账号已存在");
            }
            rs.close();
            pst.close();
            sql = "insert into tbl_user(user_id,user_pwd,register_time) values(?,?,?)";
            pst = conn.prepareStatement(sql);
            pst.setString(1, userid);
            pst.setString(2, pwd);
            pst.setTimestamp(3, new java.sql.Timestamp(System.currentTimeMillis()));
            pst.execute();
            return new BeanUser(userid);


        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


    @Override
    public BeanUser login(String userid, String pwd) throws BaseException {
        // TODO Auto-generated method stub
        if (userid.length() <= 0 || userid.length() >= 30) {
            throw new BusinessException("用户名应该在1——30个字符之间！");
        }
        if (pwd.length() <= 0 || pwd.length() >= 30) {
            throw new BusinessException("密码应该在1——30个字符之间！");
        }
        Connection conn = null;
        try {
            conn = DBUtil_pool.getConnection();
            String sql = "select user_pwd from tbl_user where user_id = ?";
            java.sql.PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, userid);
            java.sql.ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                if (rs.getString(1).equals(pwd)) {
                    return new BeanUser(userid);
                } else {
                    throw new BusinessException("密码错误");
                }
            } else {
                throw new BusinessException("账号不存在！");
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


    @Override
    public void changePwd(BeanUser user, String oldPwd, String newPwd,
                          String newPwd2) throws BaseException {
        // TODO Auto-generated method stub
        if (oldPwd.length() <= 0 || oldPwd.length() >= 30) {
            throw new BusinessException("原密码应该在1——30个字符之间！");
        }
        if (newPwd.length() <= 0 || newPwd.length() >= 30) {
            throw new BusinessException("新密码应该在1——30个字符之间！");
        }
        if (!newPwd.equals(newPwd2)) {
            throw new BusinessException("两次新密码不一致");
        }
        if (oldPwd.equals(newPwd)) {
            throw new BusinessException("原密码不能与新密码相同！");
        }
        Connection conn = null;
        try {
            conn = DBUtil_pool.getConnection();
            String sql = "select user_pwd from tbl_user where user_id=?";
            java.sql.PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, user.getUserid());
            java.sql.ResultSet rs = pst.executeQuery();
            rs.next();
            if (!rs.getString(1).equals(oldPwd)) {
                throw new BusinessException("输入的原密码不正确！");
            }
            pst.close();
            rs.close();
            sql = "update tbl_user set user_pwd = ? where user_id = ?";
            pst = conn.prepareStatement(sql);
            pst.setString(1, newPwd);
            pst.setString(2, user.getUserid());
            pst.execute();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

}
