package org.navalplanner.web.common.components.finders;

import java.util.List;

import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.web.common.components.Autocomplete;
import org.navalplanner.web.common.components.finders.Finder.SimpleListModelExt;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ComboitemRenderer;

/**
 * Interface for providing, displaying and matching elements for an
 * {@link Autocomplete} combobox
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
public interface IFinder {

    /**
     * Text displayed for each {@link Comboitem}
     *
     * @param value
     * @return
     */
    String _toString(Object value);

    /**
     * Get list of {@link BaseEntity} to fill {@link Autocomplete}
     *
     * Executed once only when {@link Autocomplete} is rendered for the first
     * time
     *
     * @return
     */
    List<? extends BaseEntity> getAll();

    /**
     * Returns customize {@link ComboitemRenderer}
     *
     * When creating your own Renderer, labels should always use
     * _toString(Object value)
     *
     * @return
     */
    ComboitemRenderer getItemRenderer();

    /**
     * Returns a {@link SimpleListModelExt}
     *
     * @return
     */
    SimpleListModelExt getModel();

    /**
     * Boolean function to evaluate whether an entry matches with input text
     *
     * @param entry
     * @param text
     * @return
     */
    boolean entryMatchesText(String entry, String text);

}
