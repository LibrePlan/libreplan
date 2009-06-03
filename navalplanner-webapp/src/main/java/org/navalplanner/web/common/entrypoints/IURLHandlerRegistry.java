package org.navalplanner.web.common.entrypoints;

/**
 * Contract for {@link URLHandlerRegistry} <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface IURLHandlerRegistry {

    public abstract <T> URLHandler<T> getRedirectorFor(
            Class<T> klassWithLinkableMetadata);

}