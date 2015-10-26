package org.libreplan.business.email.entities;

import org.libreplan.business.common.BaseEntity;

/**
 * Created by
 * @author Vova Perebykivskiy <vova@libreplan-enterprise.com>
 * on 29.09.15.
 */
public class EmailTemplate extends BaseEntity {

    private Integer type;

    private Integer language;

    private String content;

    public int getType() {
        return type;
    }
    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getLanguage() {
        return language;
    }
    public void setLanguage(Integer language) {
        this.language = language;
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
}
