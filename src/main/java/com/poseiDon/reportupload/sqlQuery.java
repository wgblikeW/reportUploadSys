package com.poseiDon.reportupload;

import javax.persistence.criteria.CriteriaBuilder;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * 处理业务逻辑与数据库的请求交互
 */
public class sqlQuery {
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/srms?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    static final String USER = "root";
    static final String PASSWD = "";
    private Connection conn;

    public boolean getSubmitStatus(String username, String projectName, String projectCourse) throws Exception {
        String SQLQuery = "SELECT * FROM student_project WHERE student_id=? AND EXISTS(SELECT * FROM project WHERE project.project_name=? AND project.project_course=? AND project.project_id=student_project.project_id)";
        PreparedStatement preparedStatement = this.conn.prepareStatement(SQLQuery);
        preparedStatement.setString(1, username);
        preparedStatement.setString(2, projectName);
        preparedStatement.setString(3, projectCourse);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            return resultSet.getBoolean("submit");
        }
        return false;
    }

    public void updateTeacherManageID(String teacher_id, String class_id) throws Exception {
        String SQLQuery = "UPDATE teacher SET teacher_manage_id=? WHERE teacher_id=?";
        PreparedStatement preparedStatement = this.conn.prepareStatement(SQLQuery);
        preparedStatement.setString(2, teacher_id);
        String teacher_manage_id = getTeacherManageID(teacher_id);
        if (teacher_manage_id.equals("")) {
            teacher_manage_id = class_id;
        } else teacher_manage_id = teacher_manage_id + ";" + class_id;
        preparedStatement.setString(1, teacher_manage_id);
        preparedStatement.executeUpdate();
    }

    public void updateStudentProject(String student_id, String project_id) throws Exception {
        String SQLQuery = "INSERT INTO student_project VALUES(?,?,0)";
        PreparedStatement preparedStatement = this.conn.prepareStatement(SQLQuery);
        preparedStatement.setString(1, student_id);
        preparedStatement.setString(2, project_id);
        preparedStatement.executeUpdate();
    }

    public String getStudentID(String class_id) throws Exception {
        String SQLQuery = "SELECT * FROM class WHERE class_id=?";
        PreparedStatement preparedStatement = this.conn.prepareStatement(SQLQuery);
        preparedStatement.setString(1, class_id);
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.next();
        return resultSet.getString("class_student_id");
    }

    public void updateTeacherManageProjectID(String teacher_id) throws Exception {
        String SQLQuery = "UPDATE teacher SET teacher_project_id=? WHERE teacher_id=?";
        String currentTeacherManageProjectID = getTeacherManageProjectID(teacher_id);
        PreparedStatement preparedStatement = this.conn.prepareStatement(SQLQuery);
        if (currentTeacherManageProjectID.equals("")) {
            currentTeacherManageProjectID = getProjectID();
        } else {
            currentTeacherManageProjectID = currentTeacherManageProjectID + ";" + getProjectID();
        }
        System.out.println(currentTeacherManageProjectID);
        preparedStatement.setString(1, currentTeacherManageProjectID);
        preparedStatement.setString(2, teacher_id);
        preparedStatement.executeUpdate();
    }

    public String getTeacherManageProjectID(String teacher_id) throws Exception {
        String SQLQuery = "SELECT * FROM teacher WHERE teacher_id=?";
        PreparedStatement preparedStatement = this.conn.prepareStatement(SQLQuery);
        preparedStatement.setString(1, teacher_id);
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.next();
        return resultSet.getString("teacher_project_id");
    }

    public String getProjectID() throws Exception {
        String SQLQuery = "SELECT * FROM project";
        PreparedStatement preparedStatement = this.conn.prepareStatement(SQLQuery);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (!resultSet.next()) {
            System.out.println("resultSet.isFirst");
            return "0";
        }
        resultSet.last();
        return resultSet.getString("project_id");

    }

    public String getProjectID(String project_name) throws Exception {
        String SQLQuery = "SELECT * FROM project WHERE project_name=?";
        PreparedStatement preparedStatement = this.conn.prepareStatement(SQLQuery);
        preparedStatement.setString(1, project_name);
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.last();
        return resultSet.getString("project_id");

    }

    public void constructNewProject(String Path, String project_course, String project_name, String project_teacher, String project_deadline, String class_id) throws Exception {
        String SQLQuery = "INSERT INTO project VALUES(?,?,?,?,?,?,?)";
        PreparedStatement preparedStatement = this.conn.prepareStatement(SQLQuery);
        String project_id = getProjectID();
        project_id = String.valueOf(Integer.valueOf(project_id) + 1);
        String project_url = Path;
        preparedStatement.setString(1, project_id);
        preparedStatement.setString(2, project_name);
        preparedStatement.setString(3, project_course);
        preparedStatement.setString(4, project_teacher);
        preparedStatement.setString(5, project_url);
        preparedStatement.setString(6, project_deadline);
        preparedStatement.setString(7, class_id);
        preparedStatement.executeUpdate();
    }

    public String getTeacherName(String teacher_id) throws Exception {
        String SQLQuery = "SELECT * FROM teacher WHERE teacher_id=?";
        PreparedStatement preparedStatement = this.conn.prepareStatement(SQLQuery);
        preparedStatement.setString(1, teacher_id);
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.next();
        return resultSet.getString("teacher_name");
    }

    public String getTeacherManageClass(String teacher_id) throws Exception {
        String SQLQuery = "SELECT * FROM teacher WHERE teacher_id=?";
        PreparedStatement preparedStatement = this.conn.prepareStatement(SQLQuery);
        preparedStatement.setString(1, teacher_id);
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.next();
        return resultSet.getString("teacher_manage_id");
    }

    public String getClassName(String class_id) throws Exception {
        String SQLQuery = "SELECT * FROM class WHERE class_id=?";
        PreparedStatement preparedStatement = this.conn.prepareStatement(SQLQuery);
        preparedStatement.setString(1, class_id);
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.next();
        return resultSet.getString("class_name");
    }

    public sqlQuery() throws Exception {
        /**
         * 建立与mysql数据库的连接
         * conn:返回Conn对象以便执行preparedStatement操作
         */
        Class.forName(JDBC_DRIVER);
        this.conn = DriverManager.getConnection(DB_URL, USER, PASSWD);
    }

    public String updateClass(String class_name, String class_student_id) throws Exception {
        String SQLQuery = "INSERT INTO class VALUES(?,?,?)";
        PreparedStatement preparedStatement = this.conn.prepareStatement(SQLQuery);
        String class_id = String.valueOf(Integer.valueOf(getClassID()) + 1);
        preparedStatement.setString(1, class_id);
        preparedStatement.setString(2, class_name);
        preparedStatement.setString(3, class_student_id);
        preparedStatement.executeUpdate();
        return class_id;
    }

    public void addNewStudent(String student_id, String init_password) throws Exception {
        String SQLQuery = "INSERT INTO student(student_id,student_password) VALUES(?,?)";
        PreparedStatement preparedStatement = this.conn.prepareStatement(SQLQuery);
        preparedStatement.setString(1, student_id);
        preparedStatement.setString(2, init_password);
        preparedStatement.executeUpdate();
    }

    public boolean whetherInstduent(String student_id) throws Exception {
        String SQLQuery = "SELECT * FROM student WHERE student_id=?";
        PreparedStatement preparedStatement = this.conn.prepareStatement(SQLQuery);
        preparedStatement.setString(1, student_id);
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.first()) {
            return true;
        } else return false;
    }

    public String getTeacherManageID(String teacher_id) throws Exception {
        String SQLQuery = "SELECT * FROM teacher WHERE teacher_id=?";
        PreparedStatement preparedStatement = this.conn.prepareStatement(SQLQuery);
        preparedStatement.setString(1, teacher_id);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (!resultSet.first()) {
            return "";
        }
        return resultSet.getString("teacher_manage_id");
    }

    public String getClassID() throws Exception {
        String SQLQuery = "SELECT * FROM class";
        PreparedStatement preparedStatement = this.conn.prepareStatement(SQLQuery);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (!resultSet.first()) {
            return "0";
        }
        resultSet.last();
        return resultSet.getString("class_id");
    }

    public String getClassID(String class_id) throws Exception {
        String SQLQuery = "SELECT * FROM class WHERE class_id=?";
        PreparedStatement preparedStatement = this.conn.prepareStatement(SQLQuery);
        preparedStatement.setString(1, class_id);
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.last();
        return resultSet.getString("class_id");
    }

    public boolean checkClass(String class_name) throws Exception {
        String SQLQuery = "SELECT * FROM class";
        PreparedStatement preparedStatement = this.conn.prepareStatement(SQLQuery);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            if (resultSet.getString("class_name").equals(class_name)) {
                return false;
            }
        }
        return true;
    }

    public ResultSet loginQuery(String username, String password, boolean isTeacher) throws Exception {
        /**
         * 验证用户名与密码之间的配对
         * @param:username:登录用户名。学生登录使用学号作为用户名、教师使用邮箱作为登录用户名
         * @param:password:登录密码
         * @return:返回游标ResultSet
         */
        String SQLQuery;
        SQLQuery = isTeacher ? "SELECT * FROM teacher WHERE teacher_email=? AND teacher_password=?" : "SELECT * FROM student WHERE student_id=? AND student_password=?";
        PreparedStatement preparedStatement = this.conn.prepareStatement(SQLQuery);
        preparedStatement.setString(1, username);
        preparedStatement.setString(2, password);
        return preparedStatement.executeQuery();
    }

    public ResultSet StatusQuery(String username) throws Exception {
        /**
         * 用于查询合法用户下的实验报告信息以展现在前端实验报告端
         * @param username:已合法登录的用户名
         */
        String SQLQuery = "SELECT * FROM project WHERE EXISTS(SELECT * FROM student_project WHERE student_project.project_id=project.project_id AND student_project.student_id=?)";
        PreparedStatement preparedStatement = this.conn.prepareStatement(SQLQuery);
        preparedStatement.setString(1, username);
        return preparedStatement.executeQuery();
    }

    public String teacherID(String username) throws Exception {
        String SQLQuery = "SELECT * FROM teacher WHERE teacher_email=?";
        PreparedStatement preparedStatement = this.conn.prepareStatement(SQLQuery);
        preparedStatement.setString(1, username);
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.next();
        String teacherID = resultSet.getString("teacher_id");
        return teacherID;
    }

    public ResultSet StatusQueryFromTeacher(String teacher_id) throws Exception {
        // TODO
        String SQLQuery = "SELECT * FROM teacher WHERE teacher_id=?";
        PreparedStatement preparedStatement = this.conn.prepareStatement(SQLQuery);
        preparedStatement.setString(1, teacher_id);
        return preparedStatement.executeQuery();
    }

    public ResultSet project_info(String project_id) throws Exception {
        String SQLQuery = "SELECT * FROM project WHERE project_id=?";
        PreparedStatement preparedStatement = this.conn.prepareStatement(SQLQuery);
        preparedStatement.setString(1, project_id);
        return preparedStatement.executeQuery();
    }

    public int statistic_members(String project_id) throws Exception {
        String SQLQuery = "SELECT * FROM class WHERE EXISTS(SELECT * FROM project WHERE project.project_id=? AND project.class_id=class.class_id)";
        PreparedStatement preparedStatement = this.conn.prepareStatement(SQLQuery);
        preparedStatement.setString(1, project_id);
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.next();
        String class_student_id = resultSet.getString("class_student_id");
        resultSet.close();
        return class_student_id.split(";").length;
    }

    public String project_class(String project_id) throws Exception {
        String SQLQuery = "SELECT * FROM class WHERE EXISTS(SELECT * FROM project WHERE project.class_id=class.class_id AND project.project_id=?)";
        PreparedStatement preparedStatement = this.conn.prepareStatement(SQLQuery);
        preparedStatement.setString(1, project_id);
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.next();
        return resultSet.getString("class_name");
    }

    public int statistic_submit_members(String project_id) throws Exception {
        String SQLQuery = "SELECT * FROM student_project WHERE project_id=? and submit=1";
        PreparedStatement preparedStatement = this.conn.prepareStatement(SQLQuery);
        preparedStatement.setString(1, project_id);
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.last();
        return resultSet.getRow();
    }

    public String ofClass(String username) throws Exception {
        /**
         * 用于查询合法用户所在班级
         * username:登录用户名
         */
        String SQLQuery = "SELECT * FROM student WHERE student_id=?";
        PreparedStatement preparedStatement = this.conn.prepareStatement(SQLQuery);
        preparedStatement.setString(1, username);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            return resultSet.getString("student_class");
        }
        return null;
    }

    public void conformSubmit(String username, String proj_name) throws Exception {
        String SQLQuery = "UPDATE student_project SET submit=1 WHERE student_id=? AND EXISTS(SELECT * FROM project WHERE project.project_name=? AND project.project_id=student_project.project_id)";
        PreparedStatement preparedStatement = this.conn.prepareStatement(SQLQuery);
        preparedStatement.setString(1, username);
        preparedStatement.setString(2, proj_name);
        preparedStatement.executeUpdate();
        return;
    }

    public ResultSet almostDeadline() throws Exception {
        /**
         * 返回所有距离DDL仅剩三天的实验报告记录
         */
        // TODO DEBUG 邮件系统
        String SQLQuery = "SELECT * FROM student_project WHERE EXISTS(SELECT * FROM project WHERE project.project_deadline=? AND project.project_id=student_project.project_id)";
        Date currentDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.DAY_OF_MONTH, 3);
        java.sql.Date plus3date = new java.sql.Date(calendar.getTimeInMillis());
        PreparedStatement preparedStatement = this.conn.prepareStatement(SQLQuery);
        preparedStatement.setDate(1, plus3date);
        return preparedStatement.executeQuery();
    }

    public ArrayList<String> all_from_student_info(String username) throws Exception {
        ArrayList<String> list = new ArrayList<>();
        String SQLQuery = "SELECT * FROM student WHERE student_id=?";
        PreparedStatement preparedStatement = conn.prepareStatement(SQLQuery);
        preparedStatement.setString(1, username);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            list.add(resultSet.getString("student_name")); // list[0] --> 学生姓名
            list.add(resultSet.getString("student_class")); // list[1] --> 学生所属班级
            list.add(resultSet.getString("student_email")); // list[2] --> 学生邮箱信息
        }
        resultSet.close();
        return list;
    }

    public ArrayList<String> all_from_reportinfo(String username) throws Exception {
        ArrayList<String> list = new ArrayList<>();
        String SQLQuery = "SELECT * FROM project WHERE EXISTS(SELECT * FROM student_project WHERE project.project_id=student_project.project_id AND student_id=?)";
        PreparedStatement preparedStatement = conn.prepareStatement(SQLQuery);
        preparedStatement.setString(1, username);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            list.add(resultSet.getString("project_name"));
            list.add(resultSet.getString("project_course"));
            list.add(resultSet.getString("project_teacher"));
            list.add(resultSet.getString("project_deadline"));
        }
        resultSet.close();
        return list;
    }

    public void CloseConn() throws SQLException {
        this.conn.close();
    }
}
