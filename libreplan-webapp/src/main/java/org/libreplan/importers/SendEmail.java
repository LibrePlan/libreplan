package org.libreplan.importers;


import org.libreplan.business.email.daos.INotificationQueueDAO;
import org.libreplan.business.email.daos.NotificationQueueDAO;
import org.libreplan.business.email.entities.NotificationQueue;
import org.libreplan.business.users.daos.UserDAO;
import org.libreplan.business.users.entities.User;
import org.libreplan.web.email.EmailTemplateModel;
import org.libreplan.web.email.INotificationQueueModel;

import org.libreplan.web.email.NotificationQueueModel;
import org.libreplan.web.users.UserModel;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;

import java.util.List;

/**
 * Created by
 * @author Vova Perebykivskiy <vova@libreplan-enterprise.com>
 * on 13.10.15.
 *
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SendEmail implements ISendEmail {

    private INotificationQueueModel notificationQueueModel;


    @Override
    public void sendEmail() {
        /*
        // TODO
        1. check all added classes to identity (Licenses, annotations, comments, ...)
        2. the mvn compile flags -Ddefault.passwordsControl=false -Ddefault.exampleUsersDisabled=false are used to compile the demo version of LibrePlan.
           There must also be a flag like "sendingEmail" that can be set to false to make sure that the demo edition will not become the worlds biggest spammer.
        */

        // TODO nullPointer on getAll()


        System.out.println("Start");


        List<NotificationQueue> notifications = notificationQueueModel.getAll();

        System.out.println("End of list.");

    }
}
