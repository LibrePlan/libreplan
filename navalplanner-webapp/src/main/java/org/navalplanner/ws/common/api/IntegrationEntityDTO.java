/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.navalplanner.ws.common.api;

import java.util.Set;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAttribute;

import org.apache.commons.lang.StringUtils;

/**
 * DTO for <code>IntegrationEntity</code>. All DTOs corresponding to entities
 * to be used in application integration must extend from this DTO.
 * <br/>
 * <br/>
 * All entities must redefine <code>getEntityType()</code>. Additionally,
 * entities that can be bulk imported may need to redefine the following
 * methods (ordered by probability of being redefined):
 * <ul>
 * <li><code>getNaturalKeyValues()</code>.</li>
 * <li><code>checkDuplicateCode()</code>.</li>
 * <li><code>getUniversalCodePrefix()</code>.</li>
 * <li><code>getUniversalNaturalKeyPrefix()</code>.</li>
 * <li><code>checkDuplicateNaturalKey()</code>.</li>
 * </ul>
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
public abstract class IntegrationEntityDTO {

    public final static String CODE_ATTRIBUTE_NAME = "code";

    @XmlAttribute(name=CODE_ATTRIBUTE_NAME)
    public String code;

    public IntegrationEntityDTO() {}

    public IntegrationEntityDTO(String code) {
        this.code = code;
    }

    /**
     * It returns the String to use in
     * <code>InstanceConstraintViolationsDTOId.entityType</code>.
     */
    public abstract String getEntityType();

    /**
     * It returns the prefix specified in <code>getUniversalCode()</code>. The
     * default implementation returns the same value as
     * <code>getEntityType()</code>. Entities that can be bulk imported, and
     * that have derived entities, must redefine this method to return the
     * same String for all child entities if they share the code space.
     */
    protected String getUniversalCodePrefix() {
        return getEntityType();
    }

    /**
     * It returns the prefix specified in <code>getUniversalNaturalKey()</code>.
     * The default implementation returns the same value as
     * <code>getEntityType()</code>. Entities that can be bulk imported, and
     * that have derived entities, must redefine this method to return the
     * same String for all child entities if they share the natural key space.
     */
    protected String getUniversalNaturalKeyPrefix() {
        return getEntityType();
    }

    /**
     * It returns the values (as String) of the fields that represent the
     * natural key. If the entity has no natural key fields, it must return
     * <code>null</code> or an empty array. The default implementation
     * returns <code>null</code>. Entities, with natural key, that can be bulk
     * imported must redefine this method.
     */
    protected String[] getNaturalKeyValues() {
        return null;
    }

    /**
     * It checks if this entity or one its contained entities has a code
     * (the entity code is obtained by calling on
     * <code>getUniversalCode()</code>) contained in the set of keys passed as
     * a parameter. If the code is not contained, it is added to the set of
     * keys. No other class should manipulate this set. Comparison is case
     * insensitive and leading and trailing whitespace is discarded. If the
     * code is whitespace, empty ("") or <code>null</code>, the check is
     * considered OK (and the code is not added to the set of existing keys).
     * <br/>
     * <br/>
     * The default implementation only provides this semantics for the
     * container (this) entity. Entities, with contained entities, that can be
     * bulk imported must redefine this method to apply it recursively to their
     * contained entities.
     */
    public void checkDuplicateCode(Set<String> existingKeys)
        throws DuplicateCodeBeingImportedException {

        if (containsKeyAndAdd(existingKeys, getUniversalCode())) {
            throw new DuplicateCodeBeingImportedException(getEntityType(),
                code);
        }

    }

    /**
     * It checks if this entity or one its contained entities has a natural key
     * (the entity natural key is obtained by calling on
     * <code>getUniversalNaturalKey()</code>) contained in the set of keys
     * passed as a parameter. If the natural key is not contained, it is added
     * to the set of keys. No other class should manipulate this set.
     * Comparison is case insensitive and leading and trailing whitespace is
     * discarded. If some value of the natural key is whitespace, empty ("") or
     * <code>null</code>, the check is considered OK (and the natural key is
     * not added to the set of existing keys).
     * <br/>
     * <br/>
     * The default implementation only provides this semantics for the
     * container entity. Entities that can be bulk imported could be interested
     * in redefining this method. However, the default implementation is
     * probably good enough for them, since probably only the container
     * entities will have natural keys.
     */
    public void checkDuplicateNaturalKey(Set<String> existingKeys)
        throws DuplicateNaturalKeyBeingImportedException {

        if (containsKeyAndAdd(existingKeys, getUniversalNaturalKey())) {
            throw new DuplicateNaturalKeyBeingImportedException(getEntityType(),
                getNaturalKeyValues());
        }

    }

    /**
     * This method is useful to implement constructors (in subclasses) that
     * automatically generate a unique code. Such constructors are useful for
     * the implementation of test cases that add new instances (such instances
     * will have a unique code).
     */
    protected static String generateCode() {
        return UUID.randomUUID().toString();
    }

    /**
     * It returns a String with the format
     * <code>getUniversalCodePrefix().code.<<code-value>></code>, or
     * <code>null</code> if <code>code</code> is whitespace, empty ("") or
     * <code>null</code>.
     */
    private String getUniversalCode() {

        if (!StringUtils.isBlank(code)) {
            return getUniversalCodePrefix() + ".code." + StringUtils.trim(code);
        } else {
            return null;
        }

    }

    /**
     * It returns a String with the format
     * <code>getUniversalNaturalKeyPrefix().naturalkey.<<getNaturalKeyValues()>></code>,
     * or <code>null</code> if some natural key value is whitespace, empty ("")
     * or <code>null</code>.
     */
    protected final String getUniversalNaturalKey() {

        String[] naturalKeyValues = getNaturalKeyValues();

        if (naturalKeyValues == null || naturalKeyValues.length == 0) {
            return null;
        }

        String universalNaturalKey = getUniversalNaturalKeyPrefix() +
            ".natural-key.";

        for (String k : naturalKeyValues) {

            if (StringUtils.isBlank(k)) {
                return null;
            }

            universalNaturalKey += StringUtils.trim(k);

        }

        return universalNaturalKey;

    }

    /**
     * It checks if a key is contained in a set of existing keys. If not
     * contained, the key is added to the set of existing keys. Comparison is
     * case insensitive and leading and trailing whitespace is discarded. If
     * the key is whitespace, empty ("") or <code>null</code>, it
     * returns <code>false</code> (and the key is not added to the set of
     * existing keys).
     */
    protected boolean containsKeyAndAdd(Set<String> existingKeys, String newKey) {

        if (StringUtils.isBlank(newKey)) {
            return false;
        }

        String key = StringUtils.trim(newKey).toLowerCase();

        if (!existingKeys.contains(key)) {
            existingKeys.add(key);
            return false;
        }

        return true;

    }

}
