package org.libreplan.web.email;

import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.email.entities.NotificationQueue;
import org.libreplan.business.settings.entities.Language;

import org.libreplan.business.email.entities.EmailTemplateEnum;
import org.libreplan.web.common.IMessagesForUser;
import org.libreplan.web.common.Level;
import org.libreplan.web.common.MessagesForUser;
import org.zkoss.zk.ui.Component;


import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.util.GenericForwardComposer;

import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Textbox;

import java.util.*;

import static org.libreplan.web.I18nHelper._;

/**
 * Created by
 * @author Vova Perebykivskiy <vova@libreplan-enterprise.com>
 * on 25.09.15.
 */
public class EmailTemplateController extends GenericForwardComposer{

    private IEmailTemplateModel emailTemplateModel;


    private IMessagesForUser messages;

    private Component messagesContainer;

    private Textbox contentsTextbox;


    public static ListitemRenderer languagesRenderer = new ListitemRenderer() {
        @Override
        public void render(org.zkoss.zul.Listitem item, Object data)
                throws Exception {
            Language language = (Language) data;
            String displayName = language.getDisplayName();
            if (language.equals(Language.BROWSER_LANGUAGE)) {
                displayName = _(language.getDisplayName());
            }
            item.setLabel(displayName);
        }
    };

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("emailTemplateController", this, true);
        messages = new MessagesForUser(messagesContainer);
        contentsTextbox.setValue(getInitialContentData());
    }

    public boolean save(){
        try {
            setSelectedContent();
            emailTemplateModel.confirmSave();
            messages.clearMessages();
            messages.showMessage(Level.INFO, _("E-mail template saved"));
            return true;
        } catch (ValidationException e) {
            messages.showInvalidValues(e);
        }
        return false;
    }

    public void cancel() throws InterruptedException {
        Executions.getCurrent().sendRedirect("../planner/index.zul");
    }

    public Language getSelectedLanguage() {
        return emailTemplateModel.getLanguage();
    }
    public void setSelectedLanguage(Language language){
        emailTemplateModel.setLanguage(language);

        getContentDataBySelectedLanguage();
    }

    public static ListitemRenderer getLanguagesRenderer() {
        return languagesRenderer;
    }
    public List<Language> getLanguages() {
        List<Language> languages = Arrays.asList(Language.values());
        Collections.sort(languages, new Comparator<Language>() {
            @Override
            public int compare(Language o1, Language o2) {
                if (o1.equals(Language.BROWSER_LANGUAGE)) {
                    return -1;
                }
                if (o2.equals(Language.BROWSER_LANGUAGE)) {
                    return 1;
                }
                return o1.getDisplayName().compareTo(o2.getDisplayName());
            }
        });
        return languages;
    }


    public EmailTemplateEnum getSelectedEmailTemplateEnum() {
        return emailTemplateModel.getEmailTemplateEnum();
    }
    public void setSelectedEmailTemplateEnum(EmailTemplateEnum emailTemplateEnum){
        emailTemplateModel.setEmailTemplateEnum(emailTemplateEnum);

        getContentDataBySelectedTemplate();
    }

    public ListitemRenderer getEmailTemplateEnumRenderer() {
        return new ListitemRenderer() {
            @Override
            public void render(Listitem item, Object data) throws Exception {
                EmailTemplateEnum template = (EmailTemplateEnum) data;
                item.setLabel(_(template.getTemplateType()));
                item.setValue(template);
            }
        };
    }
    public List<EmailTemplateEnum> getEmailTemplateEnum() {
        return Arrays.asList(EmailTemplateEnum.values());
    }


    public void setSelectedContent(){
        emailTemplateModel.setContent(contentsTextbox.getValue());
    }
    public String getInitialContentData(){
        return emailTemplateModel.initializeContent();
    }

    private void getContentDataBySelectedLanguage(){
        contentsTextbox.setValue(emailTemplateModel.getContentBySelectedLanguage(getSelectedLanguage().ordinal(), getSelectedEmailTemplateEnum().ordinal()));
    }
    private void getContentDataBySelectedTemplate(){
        contentsTextbox.setValue( emailTemplateModel.getContentBySelectedTemplate( getSelectedEmailTemplateEnum().ordinal(), getSelectedLanguage().ordinal() ) );
    }
}
