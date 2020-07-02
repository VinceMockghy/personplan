package cn.edu.zucc.personplan.control.example;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import cn.edu.zucc.personplan.itf.IPlanManager;
import cn.edu.zucc.personplan.model.BeanPlan;
import cn.edu.zucc.personplan.model.BeanUser;
import cn.edu.zucc.personplan.util.BaseException;
import cn.edu.zucc.personplan.util.BusinessException;
import cn.edu.zucc.personplan.util.DBUtil_pool;

public class ExamplePlanManager implements IPlanManager {

    @Override
    public BeanPlan addPlan(String name) throws BaseException {
        // TODO Auto-generated method stub
        if (name == null || "".equals(name) || name.length() > 255) {
            throw new BusinessException("计划的名字 必须是 1~255 个字符");
        }
        Connection conn = null;
        BeanPlan p = new BeanPlan();
        try {
            conn = DBUtil_pool.getConnection();
            String user_id = BeanUser.currentLoginUser.getUserid();
            int plan_ord = 0;
            int plan_id = 0;
            String sql = "select plan_id from tbl_plan where user_id =? and plan_name = ?";
            java.sql.PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, user_id);
            pst.setString(2, name);
            java.sql.ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                rs.close();
                pst.close();
                throw new BusinessException("计划已存在");
            }
            sql = "select max(plan_order) from tbl_plan where user_id=?";
            pst = conn.prepareStatement(sql);
            pst.setString(1, user_id);
            rs = pst.executeQuery();
            if (rs.next()) {
                plan_ord = rs.getInt(1) + 1;
            } else {
                plan_ord = 1;
            }
            rs.close();
            pst.close();

            sql = "insert into tbl_plan(" +
                    "user_id,plan_order,plan_name,create_time,step_count,start_step_count,finished_step_count) " +
                    "values(?,?,?,?,0,0,0)";
            pst = conn.prepareStatement(sql);
            pst.setString(1, user_id);
            pst.setInt(2, plan_ord);
            pst.setString(3, name);
            pst.setTimestamp(4, new java.sql.Timestamp(System.currentTimeMillis()));
            pst.execute();
            pst.close();

            sql = "select max(plan_id) from tbl_plan where user_id=?";
            pst = conn.prepareStatement(sql);
            pst.setString(1, user_id);
            rs = pst.executeQuery();
            if (rs.next()) {
                plan_id = rs.getInt(1) + 1;
            } else {
                plan_id = 1;
            }
            rs.close();
            pst.close();
            p.setPlan_id(plan_id);
            p.setUser_id(user_id);
            p.setPlan_order(plan_ord);
            p.setPlan_name(name);

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }


        return p;
    }

    @Override
    public List<BeanPlan> loadAll() throws BaseException {
        List<BeanPlan> result = new ArrayList<BeanPlan>();
        Connection conn = null;

        try {
            conn = DBUtil_pool.getConnection();
            String sql = "SELECT plan_id, plan_name,step_count,finished_step_count ,plan_order from tbl_plan where user_id=?";
            java.sql.PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, BeanUser.currentLoginUser.getUserid());
            java.sql.ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                BeanPlan p = new BeanPlan();
                p.setPlan_id(rs.getInt(1));
                p.setPlan_name(rs.getString(2));
                p.setStep_count(rs.getInt(3));
                p.setFinished_step_count(rs.getInt(4));
                p.setPlan_order(rs.getInt(5));
                result.add(p);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
        return result;
    }

    @Override
    public void deletePlan(BeanPlan plan) throws BaseException {
        int plan_id = plan.getPlan_id();
        Connection conn = null;
        try {
            conn = DBUtil_pool.getConnection();
            String sql = "select count(*) from tbl_step where plan_id= " + plan_id;
            java.sql.Statement st = conn.createStatement();
            java.sql.ResultSet rs = st.executeQuery(sql);
            if (rs.next()) {
                if (rs.getInt(1) > 0) {
                    rs.close();
                    st.close();
                    throw new BusinessException("该计划已经存在步骤，不能删除");
                }
            }
            sql = "select plan_order,user_id from tbl_plan where plan_id= " + plan_id;
            rs = st.executeQuery(sql);
            int plan_ord = 0;
            String plan_user_id = null;
            if (rs.next()) {
                plan_ord = rs.getInt(1);
                plan_user_id = rs.getString(2);
            } else {
                throw new BusinessException("该计划不存在");
            }
            rs.close();
            if (!BeanUser.currentLoginUser.getUserid().equals(plan_user_id)) {
                st.close();
                throw new BusinessException("不能删除别人的计划");
            }

            sql = "delete from tbl_plan where plan_id = " + plan_id;
            st.execute(sql);
            st.close();

            int maxplan_order = 0;
            sql = "select max(plan_order) from tbl_plan where user_id = ? ";
            java.sql.PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, plan_user_id);
            rs = pst.executeQuery();
            if (rs.next()) {
                maxplan_order = rs.getInt(1);
            } else {
                return;
            }
            for (int i = plan_ord + 1; i <= maxplan_order; i++) {
                sql = " update tbl_plan set plan_order = ? where user_id = ? and plan_order = ?";
                pst = conn.prepareStatement(sql);
                pst.setInt(1, i - 1);
                pst.setString(2, plan_user_id);
                pst.setInt(3, i);
                pst.executeUpdate();
            }


        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }


    }

}
