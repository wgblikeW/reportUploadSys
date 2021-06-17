package com.poseiDon.reportupload;

import javax.mail.internet.*;
import javax.servlet.ServletContext;
import java.io.File;
import java.sql.ResultSet;
import java.util.*;
import javax.mail.*;
import org.zeroturnaround.zip.*;

/**
 * 实现邮件提醒功能
 */
public class postMan extends TimerTask {

    private static final int C_SCHEDULED_HOUR = 18; // 任务定时计划启动时间
    private static boolean isRunning = false;
    private ServletContext context;

    public postMan(ServletContext context) {
        this.context = context;
    }

    public void checkDeadline() {
        try {
            System.out.println("---CheckDeadLine---");
            sqlQuery query = new sqlQuery();
            Date currentDate = new Date();
            java.sql.Date SQLDate = new java.sql.Date(currentDate.getTime());
            query.deleteStudentProjectOutOfRange(SQLDate);
            ResultSet resultSet = query.reachDeadline(SQLDate);
            while (resultSet.next()) {
                System.out.println("Project Reach DeadLine");
                int project_id = resultSet.getInt("project_id");
                System.out.println("ProjectID:" + project_id);
                String PATH = context.getRealPath("./") + File.separator + "uploadReport" + File.separator;
                try {
                    query.alterTeacherManageProjectID(project_id);
                    ZipUtil.pack(new File(PATH + project_id), new File(PATH + project_id + ".zip"));
                    File dir = new File(PATH + File.separator + project_id);
                    if (dir.isDirectory()) {
                        dir.delete();
                    }
                    query.removeProject(project_id);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void beforeMail() {
        final String emailAccount = "wgblike@163.com"; // 提醒邮件发送方邮件地址
        final String emailPassword = "HZJWRBEOIQRDGHOQ"; // 此处为服务器颁发的授权码
        final String emailSMTPHost = "smtp.163.com"; // smtp HOST
        String recvMailAccount = "wgblike@qq.com";
        String mailContent = "这是一封测试邮件";
        String msgSubject = "实验报告截止时间提醒";

        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.smtp.host", emailSMTPHost);
        props.setProperty("mail.smtp.auth", "true");
        Session session = Session.getInstance(props);
        session.setDebug(false);
        System.out.println(session);
        try {
            HashMap<String, Object> dict = almostDeadline();
            System.out.println("-----almostDeadline-----");
            System.out.println(dict);
            Transport transport = session.getTransport();
            transport.connect(emailAccount, emailPassword);
            dict.forEach((username, userinfo) -> {
                ArrayList<String> temp = (ArrayList<String>) userinfo;
                try {
                    ArrayList<String> temp2 = new sqlQuery().all_from_reportinfo(username);
                    System.out.println(session);
                    MimeMessage msg = GMimeMessage(session, emailAccount, recvMailAccount, msgSubject, constructContext(temp.get(0), temp2.get(0), temp2.get(3)));
                    transport.sendMessage(msg, msg.getAllRecipients());
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("错误告警:postMan:beforeMail:transport");
                }
            });
            transport.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("错误模块检查:postMan:beforeMail");
        }

    }

    public String constructContext(String name, String deadline, String proj_name) {
        return name + "同学，你的实验课程项目:" + proj_name + "将要到提交截止日期(" + deadline + ")了，请尽快完成并上传实验报告";
    }

    public MimeMessage GMimeMessage(Session session, String emailAccount, String recvMailAccount, String msgSubject, String mailContent) {
        /**
         * 创建一封仅有文本的邮件
         * @param session 和服务器交互的会话
         * @param emailAccount SMTP服务器用户名
         * @param recvMailAccount 收件人邮箱账户
         * @param msgSubject 邮件主题
         * @param mailContent 邮件内容
         */
        try {
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(emailAccount, "Reminder", "UTF-8"));
            msg.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(recvMailAccount, "", "UTF-8"));
            msg.setSubject(msgSubject, "UTF-8");
            msg.setContent(mailContent, "text/html;charset=UTF-8"); // TODO 构造邮件提醒内容
            msg.setSentDate(new Date());
            msg.saveChanges();
            return msg;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public HashMap<String, Object> almostDeadline() {
        /**
         * 返回所有满足发送提醒邮件设定阈值的学生信息:[sname, class, email]
         */
        HashMap<String, Object> dict = new HashMap<>();
        try {
            sqlQuery query = new sqlQuery();
            ResultSet recipientSets = query.almostDeadline();
            ArrayList<String> userList = new ArrayList<>(); // 到达设定发送提醒邮件阈值的学生列表
            ArrayList<String> userinfo = new ArrayList<>(); // 满足发送提醒邮件要求的学生信息
            while (recipientSets.next()) {
                // TODO 有优化空间
                userList.add(recipientSets.getString("student_id"));
            }
            recipientSets.close();
            userList.forEach(stu -> {
                try {
                    ArrayList<String> temp;
                    temp = query.all_from_student_info(stu);
                    userinfo.add(temp.get(0));
                    userinfo.add(temp.get(1));
                    userinfo.add(temp.get(2));
                    dict.put(stu, userinfo.clone()); // e.g 1915300016:[陈铭杰， 信安191, wgblike@qq.com]
                    userinfo.clear();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("错误告警:postMan:almostDeadline:emailist.add");
                }
            });

            query.CloseConn();
            return dict;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("错误信息:postMan:almostDeadline");
            return null;
        }
    }

    @Override
    public void run() {
        Calendar c = Calendar.getInstance();
        if (!isRunning) {
            if (C_SCHEDULED_HOUR == c.get(Calendar.HOUR_OF_DAY)) {
                isRunning = true;
                context.log("邮件正在投递中");
                checkDeadline();
                beforeMail();
                context.log("邮件投递完成");
            }
            isRunning = false;
        }

    }
}
