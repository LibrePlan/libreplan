package org.libreplan.importers;

import org.libreplan.business.email.entities.NotificationQueue;
import org.libreplan.web.email.INotificationQueueModel;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.List;

/**
 * Created by
 * @author Vova Perebykivskiy <vova@libreplan-enterprise.com>
 * on 13.10.15.
 *
 */

public class SendEmailJob extends QuartzJobBean {

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        ApplicationContext applicationContext = (ApplicationContext) context.getJobDetail().
                getJobDataMap().get("applicationContext");

        ISendEmail sendEmail = (ISendEmail) applicationContext.getBean("sendEmail");

        sendEmail.sendEmail();
    }

}
