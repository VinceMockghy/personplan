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
            throw new BusinessException("�û���Ӧ����1����30���ַ�֮�䣡");
        }
        if (pwd.length() <= 0 || pwd.length() >= 30) {
            throw new BusinessException("����Ӧ����1����30���ַ�֮�䣡");
        }
        if (!pwd.equals(pwd2)) {
            throw new BusinessException("������������벻һ��");
        }
        Connection conn = null;
        try {
            conn = DBUtil_pool.getConnection();
            String sql = "select user_id from tbl_user where user_id = ?";
            java.sql.PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, userid);
            java.sql.ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                throw new BusinessException("�˺��Ѵ���");
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
            throw new BusinessException("�û���Ӧ����1����30���ַ�֮�䣡");
        }
        if (pwd.length() <= 0 || pwd.length() >= 30) {
            throw new BusinessException("����Ӧ����1����30���ַ�֮�䣡");
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
                    throw new BusinessException("�������");
                }
            } else {
                throw new BusinessException("�˺Ų����ڣ�");
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
            throw new BusinessException("ԭ����Ӧ����1����30���ַ�֮�䣡");
        }
        if (newPwd.length() <= 0 || newPwd.length() >= 30) {
            throw new BusinessException("������Ӧ����1����30���ַ�֮�䣡");
        }
        if (!newPwd.equals(newPwd2)) {
            throw new BusinessException("���������벻һ��");
        }
        if (oldPwd.equals(newPwd)) {
            throw new BusinessException("ԭ���벻������������ͬ��");
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
                throw new BusinessException("�����ԭ���벻��ȷ��");
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
