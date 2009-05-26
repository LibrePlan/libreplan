package org.navalplanner.web.common;

/**
 * Contract for {@link RedirectorRegistry} <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface IRedirectorRegistry {

    public abstract <T> Redirector<T> getRedirectorFor(
            Class<T> klassWithLinkableMetadata);

}