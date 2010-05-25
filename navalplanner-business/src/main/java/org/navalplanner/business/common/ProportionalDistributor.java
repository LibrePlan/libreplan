/*
 * This file is part of NavalPlan
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
package org.navalplanner.business.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class ProportionalDistributor {

    public static ProportionalDistributor create(int... initialShares) {
        return new ProportionalDistributor(toProportions(
                sumIntegerParts(initialShares), initialShares));
    }

    private static int sumIntegerParts(int[] numbers) {
        int sum = 0;
        for (Number each : numbers) {
            sum += each.intValue();
        }
        return sum;
    }

    private static BigDecimal[] toProportions(int initialTotal, int... shares) {
        BigDecimal total = new BigDecimal(initialTotal);
        BigDecimal[] result = new BigDecimal[shares.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = (total.equals(BigDecimal.ZERO)) ? BigDecimal.ZERO
                    : new BigDecimal(shares[i]).divide(total, 4,
                            RoundingMode.DOWN);
        }
        return result;
    }

    private static class ProportionWithPosition implements
            Comparable<ProportionWithPosition> {

        public static List<ProportionWithPosition> transform(
                BigDecimal[] proportions) {
            List<ProportionWithPosition> result = new ArrayList<ProportionWithPosition>();
            for (int i = 0; i < proportions.length; i++) {
                result.add(new ProportionWithPosition(i, proportions[i]));
            }
            return result;
        }

        final int position;
        final BigDecimal proportion;

        ProportionWithPosition(int position, BigDecimal proportion) {
            this.position = position;
            this.proportion = proportion;
        }

        @Override
        public int compareTo(ProportionWithPosition other) {
            return proportion.compareTo(other.proportion);
        }

    }

    private final BigDecimal[] proportions;

    private ProportionalDistributor(BigDecimal[] proportions) {
        this.proportions = proportions;
    }

    public int[] distribute(final int total) {
        int[] result = new int[proportions.length];
        int remaining = total - assignIntegerParts(total, result);
        if (remaining == 0) {
            return result;
        }
        BigDecimal[] currentProportions = toProportions(total, result);
        assignRemaining(result, currentProportions, remaining);
        return result;
    }

    private int assignIntegerParts(int current, int[] result) {
        int substract = 0;
        for (int i = 0; i < proportions.length; i++) {
            int intValue = proportions[i].multiply(new BigDecimal(current))
                    .intValue();
            if (intValue > 0) {
                result[i] = result[i] + intValue;
                substract += intValue;
            }
        }
        return substract;
    }

    private void assignRemaining(int[] result, BigDecimal[] currentProportions,
            int remaining) {
        List<ProportionWithPosition> transform = ProportionWithPosition
                .transform(difference(currentProportions));
        Collections.sort(transform, Collections.reverseOrder());
        for (int i = 0; i < remaining; i++) {
            ProportionWithPosition proportionWithPosition = transform.get(i
                    % currentProportions.length);
            result[proportionWithPosition.position] = result[proportionWithPosition.position] + 1;
        }
    }

    private BigDecimal[] difference(BigDecimal[] pr) {
        BigDecimal[] result = new BigDecimal[proportions.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = proportions[i].subtract(pr[i]);
        }
        return result;
    }

}
