package org.libreplan.importers;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

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
