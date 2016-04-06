package org.wso2.appcloud.tierapi.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.bind.annotation.XmlRootElement;

import org.wso2.appcloud.tierapi.bean.ContainerSpecifications;
import org.wso2.appcloud.tierapi.bean.Plan;
import org.wso2.appcloud.tierapi.dao.PlanDao;
import org.wso2.appcloud.tierapi.util.DBConfiguration;

@XmlRootElement
public class PlanDaoImpl implements PlanDao{

    @Override
    public List<Plan> getAllPlans() throws SQLException{
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;

        List<Plan> plans = new ArrayList<Plan>();
        String sql="select * from AC_SUBSCRIPTION_PLANS";

        try {
        DBConfiguration dbCon=new DBConfiguration();
        dbConnection= dbCon.getConnection();


        preparedStatement= dbConnection.prepareStatement(sql);
        ResultSet rs = preparedStatement.executeQuery();

        while (rs.next()) {
            Plan plan = new Plan();
            plan.setId(rs.getInt("PLAN_ID"));
            plan.setPlanName(rs.getString("PLAN_NAME"));
            plan.setMaxApplications(rs.getInt("MAX_APPLICATIONS"));

            plans.add(plan);
        }
        } catch (SQLException e) {
            throw e;
        } finally {

            if (preparedStatement != null) {
                preparedStatement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }
        }
        return plans;
    }

    @Override
    public Plan getPlanByPlanId(int planId) throws SQLException {
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        Logger log = Logger.getLogger(PlanDaoImpl.class.getName()); 
        Plan plan = new Plan();
        String sql="select * from AC_SUBSCRIPTION_PLANS WHERE PLAN_ID ="+planId;
        try {
        DBConfiguration dbCon=new DBConfiguration();
        dbConnection= dbCon.getConnection();
        preparedStatement= dbConnection.prepareStatement(sql);
        ResultSet rs = preparedStatement.executeQuery();
        while (rs.next()) {
            plan.setId(rs.getInt("PLAN_ID"));
            plan.setPlanName(rs.getString("PLAN_NAME"));
            plan.setMaxApplications(rs.getInt("MAX_APPLICATIONS"));
        }
        rs.close();
        } catch (SQLException e) {
            throw e;
        } finally {

            if (preparedStatement != null) {
                preparedStatement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }
        }
        return plan;
    }

    @Override
    public Plan definePlan(Plan plan) throws SQLException {
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;

        String sql="INSERT INTO AC_SUBSCRIPTION_PLANS (PLAN_NAME, TEAM, MAX_INSTANCES) VALUES (?, ?, ?)";

        try {
            DBConfiguration dbCon=new DBConfiguration();
            dbConnection= dbCon.getConnection();
            preparedStatement = dbConnection.prepareStatement(sql);
            preparedStatement.setString(1, plan.getPlanName());
            preparedStatement.setInt(2, plan.getTeam());
            preparedStatement.setInt(3, plan.getMaxApplications());

            preparedStatement.executeUpdate();
            preparedStatement.close();

            String sql2="select * from Plan WHERE PLAN_NAME= ?";
            preparedStatement= dbConnection.prepareStatement(sql2);
            preparedStatement.setString(1, plan.getPlanName());
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                plan.setId(rs.getInt("PLAN_ID"));
                plan.setPlanName(rs.getString("PLAN_NAME"));
                plan.setMaxApplications(rs.getInt("MAX_APPLICATIONS"));
            }

        } catch (SQLException e) {
            System.out.println(e);
            throw e;
        } finally {

            if (preparedStatement != null) {
                preparedStatement.close();
            }

            if (dbConnection != null) {
                dbConnection.close();
            }
        }
        return plan;
    }

    @Override
    public boolean deletePlanById(int planId) throws SQLException {
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        boolean isDeleted;
        String sql="DELETE FROM AC_SUBSCRIPTION_PLANS WHERE PLAN_ID="+planId;
        try {
        DBConfiguration dbCon=new DBConfiguration();
        dbConnection= dbCon.getConnection();
        preparedStatement= dbConnection.prepareStatement(sql);
        isDeleted = preparedStatement.executeUpdate() == 1 ? true : false;
        } catch (SQLException e) {
            System.out.println(e);
            throw e;
        } finally {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }
        }
        return isDeleted;
    }

    @Override
    public Plan updatePlanById(int planId, Plan plan) throws SQLException {
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        String sql="Update AC_SUBSCRIPTION_PLANS SET PLAN_NAME=?, TEAM= ?, MAX_INSTANCES=?, MAX_LC=? WHERE PLAN_ID = ?";
        try {
            DBConfiguration dbCon=new DBConfiguration();
            dbConnection= dbCon.getConnection();
            preparedStatement = dbConnection.prepareStatement(sql);

            preparedStatement.setString(1, plan.getPlanName());
            preparedStatement.setInt(2, plan.getTeam());
            preparedStatement.setInt(3, plan.getMaxApplications());
            preparedStatement.setInt(4, planId);
            preparedStatement.executeUpdate();
            preparedStatement.close();

            String sql2="select * from AC_SUBSCRIPTION_PLANS WHERE PLAN_ID= ?";
            preparedStatement= dbConnection.prepareStatement(sql2);
            preparedStatement.setInt(1, planId);
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                plan.setId(rs.getInt("PLAN_ID"));
                plan.setPlanName(rs.getString("PLAN_NAME"));
                plan.setMaxApplications(rs.getInt("MAX_APPLICATIONS"));
            }
        } catch (SQLException e) {
            System.out.println(e);
            throw e;
        } finally {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }
        }
        return plan;
    }

    @Override
    public List<ContainerSpecifications> getAllowedConSpecs(int planId) throws SQLException {
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        List<ContainerSpecifications> allowedContainerSpecs = new ArrayList<ContainerSpecifications>();
        String sqlAllContainerSpecs="select * from AC_CONTAINER_SPECIFICATIONS WHERE CON_SPEC_ID NOT IN "
                + "(SELECT CON_SPEC_ID FROM AC_SUBSCRIPTION_PLANS JOIN RestrictedPlanContainerSpecs ON"
                + " AC_SUBSCRIPTION_PLANS.PLAN_ID = RestrictedPlanContainerSpecs.PLAN_ID WHERE"
                + " RestrictedPlanContainerSpecs.PLAN_ID ="+planId+")";
        try {
        DBConfiguration dbCon=new DBConfiguration();
        dbConnection= dbCon.getConnection();
        preparedStatement= dbConnection.prepareStatement(sqlAllContainerSpecs);
        ResultSet rs = preparedStatement.executeQuery();
        while (rs.next()) {
            ContainerSpecifications containerSpecification = new ContainerSpecifications();
            containerSpecification.setId(rs.getInt("CON_SPEC_ID"));
            containerSpecification.setConSpecName(rs.getString("CON_SPEC_NAME"));
            containerSpecification.setCpu(rs.getInt("CPU"));
            containerSpecification.setMemory(rs.getInt("MEMORY"));
            containerSpecification.setCostPerHour(rs.getInt("COST_PER_HOUR"));
           allowedContainerSpecs.add(containerSpecification);
        }
        } catch (SQLException ex) {
            throw ex;
        } finally {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }
        }
        return allowedContainerSpecs;
    }
}
