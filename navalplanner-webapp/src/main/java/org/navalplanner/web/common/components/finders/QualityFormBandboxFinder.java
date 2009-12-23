package org.navalplanner.web.common.components.finders;

import static org.navalplanner.web.I18nHelper._;

import java.util.List;

import org.navalplanner.business.qualityforms.daos.IQualityFormDAO;
import org.navalplanner.business.qualityforms.entities.QualityForm;
import org.navalplanner.business.qualityforms.entities.QualityFormItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;

/**
 * Implements all the methods needed to comply IBandboxFinder This is a finder
 * for {@link QualityForm}l in a {@link Bandbox}. Provides how many columns for
 * {@link QualityForm} will be shown, how to render {@link QualityForm} object ,
 * how to do the matching, what text to show when an element is selected, etc
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
@Repository
public class QualityFormBandboxFinder extends BandboxFinder implements
        IBandboxFinder {

    @Autowired
    private IQualityFormDAO qualityFormDAO;

    private final String headers[] = { _("Name"), _("Type") };

    @Override
    @Transactional(readOnly = true)
    public List<QualityForm> getAll() {
        List<QualityForm> qualityForms = qualityFormDAO.getAll();
        initializeQualityForms(qualityForms);
        return qualityForms;
    }

    private void initializeQualityForms(List<QualityForm> qualityForms) {
        for (QualityForm qualityForm : qualityForms) {
            initializeQualityForm(qualityForm);
        }
    }

    private void initializeQualityForm(QualityForm qualityForm) {
        qualityForm.getName();
        qualityForm.getQualityFormType();
        for (QualityFormItem qualityFormItem : qualityForm
                .getQualityFormItems()) {
            qualityFormItem.getName();
        }
    }

    @Override
    public boolean entryMatchesText(Object obj, String text) {
        final QualityForm qualityForm = (QualityForm) obj;
        text = text.toLowerCase();
        return (qualityForm.getQualityFormType().name().toLowerCase()
                .contains(text.toLowerCase()) || qualityForm.getName()
                .toLowerCase().contains(
                text));
    }

    @Override
    public String objectToString(Object obj) {
        return ((QualityForm) obj).getName();
    }

    @Override
    public String[] getHeaders() {
        return headers.clone();
    }

    @Override
    public ListitemRenderer getItemRenderer() {
        return qualityFormRenderer;
    }

    /**
     * Render for {@link QualityForm}
     * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
     */
    private final ListitemRenderer qualityFormRenderer = new ListitemRenderer() {

        @Override
        public void render(Listitem item, Object data) throws Exception {
            QualityForm qualityForm = (QualityForm) data;
            item.setValue(data);

            final Listcell labelName = new Listcell();
            labelName.setLabel(qualityForm.getName());
            labelName.setParent(item);

            final Listcell labelType = new Listcell();
            labelType.setLabel(qualityForm.getQualityFormType().name());
            labelType.setParent(item);

        }
    };
}
