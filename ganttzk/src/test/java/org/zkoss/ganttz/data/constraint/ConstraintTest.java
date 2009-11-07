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
package org.zkoss.ganttz.data.constraint;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.zkoss.ganttz.data.constraint.Constraint.IConstraintViolationListener;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public class ConstraintTest {

    private Constraint<Integer> biggerThanFive = new Constraint<Integer>() {

        @Override
        protected Integer applyConstraintTo(Integer currentValue) {
            return Math.max(6, currentValue);
        }

        @Override
        public boolean isSatisfiedBy(Integer value) {
            return value != null && value > 5;
        }
    };
    private Constraint<Integer> lessThanFive = new Constraint<Integer>() {

        @Override
        public boolean isSatisfiedBy(Integer value) {
            return value!=null && value <5;
        }

        @Override
        protected Integer applyConstraintTo(Integer currentValue) {
            return Math.min(4, currentValue);
        }
    };

    @Test
    public void ifThereIsNoConstraintsTheOriginalValueIsReturned() {
        assertThat(Constraint.apply(2), equalTo(2));
    }

    @Test
    public void aNewValueFullfillingTheConstraintIsReturned() {
        Constraint<Integer> biggerThanFive = new Constraint<Integer>() {

            @Override
            protected Integer applyConstraintTo(Integer currentValue) {
                return Math.max(6, currentValue);
            }

            @Override
            public boolean isSatisfiedBy(Integer value) {
                return value != null && value > 5;
            }
        };
        assertThat(biggerThanFive.applyTo(5), equalTo(6));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void applyingSeveralConstraintsCallsApplyToOfEveryone() {
        List<Constraint<Object>> constraints = new ArrayList<Constraint<Object>>();
        final int numerOfConstraints = 5;
        for (int i = 0; i < numerOfConstraints; i++) {
            Constraint constraint = createNiceMock(Constraint.class);
            expect(constraint.applyConstraintTo(isA(Object.class)))
                    .andReturn(2).atLeastOnce();
            expect(constraint.isSatisfiedBy(isA(Object.class))).andReturn(true)
                    .atLeastOnce();
            constraints.add(constraint);
        }
        replay(constraints.toArray());
        Constraint.apply(2, constraints);
        verify(constraints.toArray());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void theLastConstraintHasPriority() {
        Integer result = Constraint.apply(6, biggerThanFive,
                lessThanFive);
        assertThat(result, equalTo(4));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void theViolatedConstraintsNotifiesItsListeners() {
        final Constraint<Integer>[] constraintViolated = new Constraint[1];
        biggerThanFive
                .addConstraintViolationListener(new IConstraintViolationListener<Integer>() {

                    @Override
                    public void constraintViolated(
                            Constraint<Integer> constraint, Integer value) {
                        constraintViolated[0] = constraint;
                    }
                });
        Constraint.apply(6, biggerThanFive, lessThanFive);
        assertThat(constraintViolated[0], equalTo(biggerThanFive));
    }

}
