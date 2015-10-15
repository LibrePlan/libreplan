package org.libreplan.importers;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Created by
 * @author Vova Perebykivskiy <vova@libreplan-enterprise.com>
 * on 13.10.15.
 *
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SendEmail implements ISendEmail {

    @Override
    public void sendEmail() {
        System.out.println("SendEmail class!");
    }
}
