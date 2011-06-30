/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
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
package org.navalplanner.business.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math.fraction.Fraction;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class ProportionalDistributor {

    public static ProportionalDistributor create(int... initialShares) {
        return new ProportionalDistributor(toFractions(
                sumIntegerParts(initialShares), initialShares));
    }

    private static int sumIntegerParts(int[] numbers) {
        int sum = 0;
        for (int each : numbers) {
            sum += each;
        }
        return sum;
    }

    private static Fraction[] toFractions(int initialTotal, int... shares) {
        Fraction[] result = new Fraction[shares.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = initialTotal == 0 ? Fraction.ZERO : new Fraction(
                    shares[i], initialTotal);
        }
        return result;
    }

    /**
     * Note: this class has a natural ordering that is inconsistent with equals.
     *
     */
    private static class FractionWithPosition implements
            Comparable<FractionWithPosition> {

        public static List<FractionWithPosition> transform(Fraction[] fractions) {
            List<FractionWithPosition> result = new ArrayList<FractionWithPosition>();
            for (int i = 0; i < fractions.length; i++) {
                result.add(new FractionWithPosition(i, fractions[i]));
            }
            return result;
        }

        final int position;
        final Fraction fraction;

        FractionWithPosition(int position, Fraction fraction) {
            this.position = position;
            this.fraction = fraction;
        }

        @Override
        public int compareTo(FractionWithPosition other) {
            return fraction.compareTo(other.fraction);
        }

    }

    private final Fraction[] fractions;

    private ProportionalDistributor(Fraction[] fractions) {
        this.fractions = fractions;
    }

    public int[] distribute(final int total) {
        if (fractions.length == 0) {
            return new int[0];
        }
        int[] result = new int[fractions.length];
        int remaining = total - assignIntegerParts(total, result);
        if (remaining == 0) {
            return result;
        }
        Fraction[] currentFractions = toFractions(total, result);
        assignRemaining(result, currentFractions, remaining);
        return result;
    }

    private int assignIntegerParts(int current, int[] result) {
        Fraction currentAsFraction = new Fraction(current, 1);
        int substract = 0;
        for (int i = 0; i < fractions.length; i++) {
            int intValue = fractions[i].multiply(currentAsFraction).intValue();
            if (intValue > 0) {
                result[i] = result[i] + intValue;
                substract += intValue;
            }
        }
        return substract;
    }

    private void assignRemaining(int[] result, Fraction[] currentProportions,
            int remaining) {
        List<FractionWithPosition> transform = FractionWithPosition
                .transform(difference(currentProportions));
        Collections.sort(transform, Collections.reverseOrder());
        for (int i = 0; i < remaining; i++) {
            FractionWithPosition proportionWithPosition = transform.get(i
                    % currentProportions.length);
            result[proportionWithPosition.position] = result[proportionWithPosition.position] + 1;
        }
    }

    private Fraction[] difference(Fraction[] pr) {
        Fraction[] result = new Fraction[fractions.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = fractions[i].subtract(pr[i]);
        }
        return result;
    }

}
