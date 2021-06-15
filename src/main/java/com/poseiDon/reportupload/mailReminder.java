package com.poseiDon.reportupload;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.File;
import java.util.Timer;

@WebListener
public class mailReminder implements ServletContextListener {
    private Timer timer = null;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        timer = new Timer(true);
        sce.getServletContext().log("实验报告存储文件夹已启用,路径为:" + createReportDir(sce.getServletContext()));
        sce.getServletContext().log("邮件提醒功能已启用");
        timer.schedule(new postMan(sce.getServletContext()), 0, 1000 * 60 * 60); // 1h delay
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        timer.cancel();
        sce.getServletContext().log("邮件提醒功能已停用");
    }

    private String createReportDir(ServletContext servletContext) {
        String dir = servletContext.getRealPath("./") + File.separator + "report";
        File reportDir = new File(dir);
        if (!reportDir.exists()) {
            reportDir.mkdir();
        }
        return dir;
    }
}
