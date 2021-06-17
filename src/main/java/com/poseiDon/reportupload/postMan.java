package com.poseiDon.reportupload;

import javax.mail.internet.*;
import javax.servlet.ServletContext;
import java.io.File;
import java.sql.ResultSet;
import java.util.*;
import javax.mail.*;
import org.zeroturnaround.zip.*;

/**
 * ʵ���ʼ����ѹ���
 */
public class postMan extends TimerTask {

    private static final int C_SCHEDULED_HOUR = 18; // ����ʱ�ƻ�����ʱ��
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
        final String emailAccount = "wgblike@163.com"; // �����ʼ����ͷ��ʼ���ַ
        final String emailPassword = "HZJWRBEOIQRDGHOQ"; // �˴�Ϊ�������䷢����Ȩ��
        final String emailSMTPHost = "smtp.163.com"; // smtp HOST
        String recvMailAccount = "wgblike@qq.com";
        String mailContent = "����һ������ʼ�";
        String msgSubject = "ʵ�鱨���ֹʱ������";

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
                    System.out.println("����澯:postMan:beforeMail:transport");
                }
            });
            transport.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("����ģ����:postMan:beforeMail");
        }

    }

    public String constructContext(String name, String deadline, String proj_name) {
        return name + "ͬѧ�����ʵ��γ���Ŀ:" + proj_name + "��Ҫ���ύ��ֹ����(" + deadline + ")�ˣ��뾡����ɲ��ϴ�ʵ�鱨��";
    }

    public MimeMessage GMimeMessage(Session session, String emailAccount, String recvMailAccount, String msgSubject, String mailContent) {
        /**
         * ����һ������ı����ʼ�
         * @param session �ͷ����������ĻỰ
         * @param emailAccount SMTP�������û���
         * @param recvMailAccount �ռ��������˻�
         * @param msgSubject �ʼ�����
         * @param mailContent �ʼ�����
         */
        try {
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(emailAccount, "Reminder", "UTF-8"));
            msg.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(recvMailAccount, "", "UTF-8"));
            msg.setSubject(msgSubject, "UTF-8");
            msg.setContent(mailContent, "text/html;charset=UTF-8"); // TODO �����ʼ���������
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
         * �����������㷢�������ʼ��趨��ֵ��ѧ����Ϣ:[sname, class, email]
         */
        HashMap<String, Object> dict = new HashMap<>();
        try {
            sqlQuery query = new sqlQuery();
            ResultSet recipientSets = query.almostDeadline();
            ArrayList<String> userList = new ArrayList<>(); // �����趨���������ʼ���ֵ��ѧ���б�
            ArrayList<String> userinfo = new ArrayList<>(); // ���㷢�������ʼ�Ҫ���ѧ����Ϣ
            while (recipientSets.next()) {
                // TODO ���Ż��ռ�
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
                    dict.put(stu, userinfo.clone()); // e.g 1915300016:[�����ܣ� �Ű�191, wgblike@qq.com]
                    userinfo.clear();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("����澯:postMan:almostDeadline:emailist.add");
                }
            });

            query.CloseConn();
            return dict;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("������Ϣ:postMan:almostDeadline");
            return null;
        }
    }

    @Override
    public void run() {
        Calendar c = Calendar.getInstance();
        if (!isRunning) {
            if (C_SCHEDULED_HOUR == c.get(Calendar.HOUR_OF_DAY)) {
                isRunning = true;
                context.log("�ʼ�����Ͷ����");
                checkDeadline();
                beforeMail();
                context.log("�ʼ�Ͷ�����");
            }
            isRunning = false;
        }

    }
}
