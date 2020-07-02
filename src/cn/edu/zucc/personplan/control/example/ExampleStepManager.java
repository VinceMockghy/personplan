package cn.edu.zucc.personplan.control.example;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import cn.edu.zucc.personplan.itf.IStepManager;
import cn.edu.zucc.personplan.model.BeanPlan;
import cn.edu.zucc.personplan.model.BeanStep;
import cn.edu.zucc.personplan.model.BeanUser;
import cn.edu.zucc.personplan.util.BaseException;
import cn.edu.zucc.personplan.util.BusinessException;
import cn.edu.zucc.personplan.util.DBUtil;

public class ExampleStepManager implements IStepManager {

    private static SimpleDateFormat Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void add(BeanPlan plan, String name, String planstartdate,
                    String planfinishdate) throws BaseException {
        if (name == null || "".equals(name) || name.length() > 50) {
            throw new BusinessException("步骤名称 必须是 1~255 个字符");
        }
        try {
            Format.parse(planfinishdate);
            Format.parse(planstartdate);
        } catch (ParseException e) {
            throw new BusinessException("输入的时间格式不对！");
        }

        Connection conn = null;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);
            int insertstep = 0;
            String sql = "select max(step_order) from tbl_step where plan_id = ?";
            java.sql.PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, plan.getPlan_id());
            java.sql.ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                insertstep = rs.getInt(1) + 1;
            } else {
                insertstep = 1;
            }

            sql = "insert into tbl_step(plan_id,step_order,step_name,plan_begin_time,plan_end_time) values(?,?,?,?,?)";
            pst = conn.prepareStatement(sql);
            pst.setInt(1, plan.getPlan_id());
            pst.setInt(2, insertstep);
            pst.setString(3, name);
            pst.setString(4, planstartdate);
            pst.setString(5, planfinishdate);
            pst.executeUpdate();
            pst.close();

            sql = "update tbl_plan set step_count = ? where plan_id = ?";
            pst = conn.prepareStatement(sql);
            pst.setInt(1, insertstep);
            pst.setInt(2, plan.getPlan_id());
            pst.executeUpdate();


            conn.commit();
            pst.close();

        } catch (SQLException throwables) {
            try {
                assert conn != null;
                conn.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
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

    @Override
    public List<BeanStep> loadSteps(BeanPlan plan) throws BaseException {
        List<BeanStep> result = new ArrayList<BeanStep>();
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "select step_order, step_name, plan_begin_time, plan_end_time,real_begin_time, real_end_time, step_id ,plan_id "
                    + "from tbl_step " + "where plan_id=?";
            java.sql.PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, plan.getPlan_id());
            java.sql.ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                BeanStep s = new BeanStep();
                s.setStep_order(rs.getInt(1));
                s.setStep_name(rs.getString(2));
                s.setPlan_begin_time(Format.parse(rs.getString(3)));
                s.setPlan_end_time(Format.parse(rs.getString(4)));
                if (rs.getString(5) == null) {
                    s.setReal_begin_time(null);
                } else {
                    s.setReal_begin_time(Format.parse(rs.getString(5)));
                }
                if (rs.getString(6) == null) {
                    s.setReal_end_time(null);
                } else {
                    s.setReal_end_time(Format.parse(rs.getString(6)));
                }
                s.setStep_id(rs.getInt(7));
                s.setPlan_id(rs.getInt(8));
                result.add(s);
            }
        } catch (SQLException | ParseException throwables) {
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
    public void deleteStep(BeanStep step) throws BaseException {
        // TODO Auto-generated method stub
        Connection conn = null;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            String sql = "delete from tbl_step where step_id = ?";
            java.sql.PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, step.getStep_id());
            pst.execute();
            pst.close();

            int max_order_step = 1;
            sql = " select max(step_order) from tbl_step where plan_id = ?";
            pst = conn.prepareStatement(sql);
            pst.setInt(1, step.getPlan_id());
            System.out.println(step.getPlan_id());
            java.sql.ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                max_order_step = rs.getInt(1);
                System.out.println(max_order_step);
            } else {
                return;
            }
            for (int i = step.getStep_order() + 1; i <= max_order_step; i++) {
                sql = "update tbl_step set step_order = ? where plan_id = ? and step_order = ?";
                pst = conn.prepareStatement(sql);
                pst.setInt(1, i - 1);
                pst.setInt(2, step.getPlan_id());
                pst.setInt(3, i);
                pst.executeUpdate();
            }

            sql = "update tbl_plan set step_count = ? where plan_id = ?";
            pst = conn.prepareStatement(sql);
            if (max_order_step == 0) {
                pst.setInt(1, 0);
            } else {
                pst.setInt(1, max_order_step - 1);
            }
            pst.setInt(2, step.getPlan_id());
            pst.executeUpdate();

            conn.commit();
            pst.close();


        } catch (SQLException throwables) {
            try {
                assert conn != null;
                conn.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
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

    @Override
    public void startStep(BeanStep step) throws BaseException {
        // TODO Auto-generated method stub
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            String sql = "update tbl_step set real_begin_time = ? where step_id = ?";
            java.sql.PreparedStatement pst = conn.prepareStatement(sql);
            pst.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            pst.setInt(2, step.getStep_id());
            pst.executeUpdate();
            pst.close();
            int start_step_count;
            sql = "select start_step_count from tbl_plan where plan_id=?";
            pst = conn.prepareStatement(sql);
            pst.setInt(1, step.getPlan_id());
            java.sql.ResultSet rs = pst.executeQuery();
            rs.next();
            start_step_count = rs.getInt(1);
            rs.close();
            pst.close();
            sql = "update tbl_plan set start_step_count=? where plan_id=?";
            pst = conn.prepareStatement(sql);
            pst.setInt(1, start_step_count + 1);
            pst.setInt(2, step.getPlan_id());
            pst.executeUpdate();

            conn.commit();
            pst.close();

        } catch (SQLException throwables) {
            try {
                assert conn != null;
                conn.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
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

    @Override
    public void finishStep(BeanStep step) throws BaseException {
        // TODO Auto-generated method stub
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            String sql = "update tbl_step set real_end_time = ? where step_id = ?";
            java.sql.PreparedStatement pst = conn.prepareStatement(sql);
            pst.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            pst.setInt(2, step.getStep_id());
            pst.executeUpdate();
            pst.close();
            int finished_step_count;
            sql = "select finished_step_count from tbl_plan where plan_id=?";
            pst = conn.prepareStatement(sql);
            pst.setInt(1, step.getPlan_id());
            java.sql.ResultSet rs = pst.executeQuery();
            rs.next();
            finished_step_count = rs.getInt(1);
            rs.close();
            pst.close();
            sql = "update tbl_plan set finished_step_count=? where plan_id=?";
            pst = conn.prepareStatement(sql);
            pst.setInt(1, finished_step_count + 1);
            pst.setInt(2, step.getPlan_id());
            pst.executeUpdate();

            conn.commit();
            pst.close();

        } catch (SQLException throwables) {
            try {
                assert conn != null;
                conn.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
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

    @Override
    public void moveUp(BeanStep step) throws BaseException {
        // TODO Auto-generated method stub
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            if(step.getStep_order()==1){
                throw new BusinessException("该Step无法上移");
            }

            String sql = "update tbl_step set step_order=? where plan_id=? and step_order=?";
            java.sql.PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, 0);
            pst.setInt(2, step.getPlan_id());
            pst.setInt(3, step.getStep_order() - 1);
            pst.executeUpdate();

            sql = "update tbl_step set step_order=? where plan_id=? and step_order=?";
            pst = conn.prepareStatement(sql);
            pst.setInt(1, step.getStep_order() - 1);
            pst.setInt(2, step.getPlan_id());
            pst.setInt(3, step.getStep_order());
            pst.executeUpdate();

            sql = "update tbl_step set step_order=? where plan_id=? and step_order=?";
            pst = conn.prepareStatement(sql);
            pst.setInt(1, step.getStep_order());
            pst.setInt(2, step.getPlan_id());
            pst.setInt(3, 0);
            pst.executeUpdate();


            conn.commit();
            pst.close();

        } catch (SQLException throwables) {
            try {
                assert conn != null;
                conn.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
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

    @Override
    public void moveDown(BeanStep step) throws BaseException {
        // TODO Auto-generated method stub
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);
            int max_step = 0;
            String sql = "select max(step_order) from tbl_step where plan_id = ?";
            java.sql.PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1,step.getPlan_id());
            java.sql.ResultSet rs = pst.executeQuery();
            if(rs.next()){
                max_step=rs.getInt(1);
                if(step.getStep_order()==max_step){
                    throw new BusinessException("该Step无法下移");
                }
            }else {
                throw new BusinessException("error");
            }


            sql = "update tbl_step set step_order=? where plan_id=? and step_order=?";
            pst = conn.prepareStatement(sql);
            pst.setInt(1, 0);
            pst.setInt(2, step.getPlan_id());
            pst.setInt(3, step.getStep_order() + 1);
            pst.executeUpdate();

            sql = "update tbl_step set step_order=? where plan_id=? and step_order=?";
            pst = conn.prepareStatement(sql);
            pst.setInt(1, step.getStep_order() + 1);
            pst.setInt(2, step.getPlan_id());
            pst.setInt(3, step.getStep_order());
            pst.executeUpdate();

            sql = "update tbl_step set step_order=? where plan_id=? and step_order=?";
            pst = conn.prepareStatement(sql);
            pst.setInt(1, step.getStep_order());
            pst.setInt(2, step.getPlan_id());
            pst.setInt(3, 0);
            pst.executeUpdate();


            conn.commit();
            pst.close();

        } catch (SQLException throwables) {
            try {
                assert conn != null;
                conn.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
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
