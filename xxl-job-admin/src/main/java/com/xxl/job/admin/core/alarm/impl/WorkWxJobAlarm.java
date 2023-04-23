package com.xxl.job.admin.core.alarm.impl;

import com.xxl.job.admin.core.alarm.JobAlarm;
import com.xxl.job.admin.core.conf.XxlJobAdminConfig;
import com.xxl.job.admin.core.model.XxlJobGroup;
import com.xxl.job.admin.core.model.XxlJobInfo;
import com.xxl.job.admin.core.model.XxlJobLog;
import com.xxl.job.admin.core.util.I18nUtil;
import com.xxl.job.core.biz.model.ReturnT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author feng
 * @date 2023/4/23 4:41 PM
 * @email:dujiafeng@gyyx.com
 * @description: 任务执行失败的企业微信报警
 */
public class WorkWxJobAlarm implements JobAlarm {

    private static final Logger logger = LoggerFactory.getLogger(EmailJobAlarm.class);

    /**
     * fail alarm
     *
     * @param jobLog
     */
    @Override
    public boolean doAlarm(XxlJobInfo info, XxlJobLog jobLog){
        boolean alarmResult = true;
        if (info==null||info.getAlarmWorkWx()==null||info.getAlarmWorkWx().trim().length()==0){
            return alarmResult;
        }
        String alarmContent = "Alarm Job LogId=" + jobLog.getId();
        if (jobLog.getTriggerCode() != ReturnT.SUCCESS_CODE) {
            alarmContent += "<br>TriggerMsg=<br>" + jobLog.getTriggerMsg();
        }
        if (jobLog.getHandleCode()>0 && jobLog.getHandleCode() != ReturnT.SUCCESS_CODE) {
            alarmContent += "<br>HandleCode=" + jobLog.getHandleMsg();
        }

        XxlJobGroup group = XxlJobAdminConfig.getAdminConfig().getXxlJobGroupDao().load(Integer.valueOf(info.getJobGroup()));
        String personal = I18nUtil.getString("admin_name_full");
        String title = I18nUtil.getString("jobconf_monitor");
        String content = MessageFormat.format(loadEmailJobAlarmTemplate(),
                group!=null?group.getTitle():"null",
                info.getId(),
                info.getJobDesc(),
                alarmContent);
        Set<String> wxSet = new HashSet<String>(Arrays.asList(info.getAlarmWorkWx().split("\n")));
        for (String email: wxSet) {

        }
        return alarmResult;


    }

    /**
     * 加载企业微信报警模板
     *
     * @return
     */
    private static final String loadEmailJobAlarmTemplate(){
        String mailBodyTemplate = "<h5>" + I18nUtil.getString("jobconf_monitor_detail") + "：</span>" +
                "<table border=\"1\" cellpadding=\"3\" style=\"border-collapse:collapse; width:80%;\" >\n" +
                "   <thead style=\"font-weight: bold;color: #ffffff;background-color: #ff8c00;\" >" +
                "      <tr>\n" +
                "         <td width=\"20%\" >"+ I18nUtil.getString("jobinfo_field_jobgroup") +"</td>\n" +
                "         <td width=\"10%\" >"+ I18nUtil.getString("jobinfo_field_id") +"</td>\n" +
                "         <td width=\"20%\" >"+ I18nUtil.getString("jobinfo_field_jobdesc") +"</td>\n" +
                "         <td width=\"10%\" >"+ I18nUtil.getString("jobconf_monitor_alarm_title") +"</td>\n" +
                "         <td width=\"40%\" >"+ I18nUtil.getString("jobconf_monitor_alarm_content") +"</td>\n" +
                "      </tr>\n" +
                "   </thead>\n" +
                "   <tbody>\n" +
                "      <tr>\n" +
                "         <td>{0}</td>\n" +
                "         <td>{1}</td>\n" +
                "         <td>{2}</td>\n" +
                "         <td>"+ I18nUtil.getString("jobconf_monitor_alarm_type") +"</td>\n" +
                "         <td>{3}</td>\n" +
                "      </tr>\n" +
                "   </tbody>\n" +
                "</table>";

        return mailBodyTemplate;
    }

}
