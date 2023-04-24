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
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.text.MessageFormat;
import java.util.*;

/**
 * @author feng
 * @date 2023/4/23 4:41 PM
 * @email:dujiafeng@gyyx.com
 * @description: 任务执行失败的企业微信报警
 */
@Component
public class WorkWxJobAlarm implements JobAlarm {

    private static final Logger logger = LoggerFactory.getLogger(WorkWxJobAlarm.class);

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
            alarmContent += "\n" + jobLog.getTriggerMsg().replaceAll("<br>","\n");
        }
        if (jobLog.getHandleCode()>0 && jobLog.getHandleCode() != ReturnT.SUCCESS_CODE) {
            alarmContent += "\n" + jobLog.getHandleMsg();
        }

        XxlJobGroup group = XxlJobAdminConfig.getAdminConfig().getXxlJobGroupDao().load(Integer.valueOf(info.getJobGroup()));
        String content = MessageFormat.format(loadEmailJobAlarmTemplate(),
                group!=null?group.getTitle():"null",
                info.getId(),
                info.getJobDesc(),
                alarmContent);
        Set<String> wxSet = new HashSet<String>(Arrays.asList(info.getAlarmWorkWx().split("\n")));
        Map<String, Object> sendMap = new HashMap<>();
        //设置消息类型 markdown文本
        sendMap.put("msgtype", "markdown");
        Map<String, String> contentMap = new HashMap<>();
        contentMap.put("content", content);
        sendMap.put("markdown", contentMap);
        logger.info("需要发送的企业微信地址: {}",wxSet);
        for (String wx: wxSet) {
            sendPostRequest(wx,sendMap);
        }
        return alarmResult;


    }

    /**
     * 加载企业微信报警模板
     *
     * @return
     */
    private static String loadEmailJobAlarmTemplate(){
        String wxBodyTemplate = "## "+ I18nUtil.getString("jobconf_monitor_detail") + "\n"+
                "> " +  I18nUtil.getString("jobinfo_field_jobgroup")+":{0}\n"+
                "> " +  I18nUtil.getString("jobinfo_field_id")+":{1}\n"+
                "> " +  I18nUtil.getString("jobinfo_field_jobdesc")+":{2}\n"+
                "> " +  I18nUtil.getString("jobconf_monitor_alarm_title")+": "+I18nUtil.getString("jobconf_monitor_alarm_type")+"\n\n"+
                "{3}\n";
        return wxBodyTemplate;
    }


    public static String sendPostRequest(String url, Object params){
        RestTemplate client = new RestTemplate();
        //新建Http头，add方法可以添加参数
        HttpHeaders headers = new HttpHeaders();
        //设置请求发送方式
        HttpMethod method = HttpMethod.POST;
        // 以表单的方式提交
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        //将请求头部和参数合成一个请求
        HttpEntity<Object> requestEntity = new HttpEntity<>(params, headers);
        //执行HTTP请求，将返回的结构使用String 类格式化（可设置为对应返回值格式的类）
        ResponseEntity<String> response = client.exchange(url, method, requestEntity, String.class);

        return response.getBody();
    }

}
