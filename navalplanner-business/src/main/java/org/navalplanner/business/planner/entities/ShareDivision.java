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

package org.navalplanner.business.planner.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.Validate;

public class ShareDivision {

    public static ShareDivision create(Collection<? extends Share> shares) {
        return new ShareDivision(shares);
    }

    private static class ShareWrapper implements Comparable<ShareWrapper> {
        private Share share;

        private int originalPosition;

        public ShareWrapper(Share share, int originalPosition) {
            this.share = share;
            this.originalPosition = originalPosition;

        }

        @Override
        public int compareTo(ShareWrapper other) {
            long thisHours = share.getHours();
            long otherHours = other.share.getHours();
            return Long.signum(thisHours - otherHours);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ShareWrapper) {
                ShareWrapper other = (ShareWrapper) obj;
                return getHours() == other.getHours();
            }
            return false;
        }

        @Override
        public int hashCode() {
            return getHours();
        }

        public static List<ShareWrapper> wrap(List<Share> shares) {
            List<ShareWrapper> result = new ArrayList<ShareWrapper>();
            int i = 0;
            for (Share share : shares) {
                result.add(new ShareWrapper(share, i));
                i++;
            }
            return result;
        }

        public boolean haveSameHours(ShareWrapper other) {
            return getHours() == other.getHours();
        }

        int getHours() {
            return this.share.getHours();
        }

        void add(int hours) {
            this.share = share.plus(hours);
        }

        public static void sortByOriginalPosition(List<ShareWrapper> bucket) {
            Collections.sort(bucket, new Comparator<ShareWrapper>() {

                @Override
                public int compare(ShareWrapper o1, ShareWrapper o2) {
                    return o1.originalPosition - o2.originalPosition;
                }
            });
        }

    }

    private final List<Share> shares;

    private ShareDivision(Collection<? extends Share> shares) {
        Validate.notNull(shares);
        Validate.noNullElements(shares);
        this.shares = new ArrayList<Share>(shares);
    }

    public List<Share> getShares() {
        return Collections.unmodifiableList(shares);
    }

    public ShareDivision plus(final int increase) {
        int remainderIncrease = increase;
        List<ShareWrapper> wrapped = ShareWrapper.wrap(shares);
        Collections.sort(wrapped);
        int i = 0;
        while (i < wrapped.size()) {
            if (remainderIncrease == 0) {
                break;
            }
            int nextBigger = findNextBigger(wrapped, i);
            FillingBucket bucket = fillingBuckectFor(wrapped, nextBigger,
                    remainderIncrease);
            bucket.doTheDistribution();
            i = nextBigger;
            remainderIncrease = remainderIncrease - bucket.getIncreaseDone();
            assert remainderIncrease >= 0;
        }
        assert remainderIncrease == 0 || shares.isEmpty() : "all is assigned";
        return ShareDivision.create(fromWrappers(wrapped));
    }

    private ArrayList<Share> fromWrappers(List<ShareWrapper> wrapped) {
        ArrayList<Share> newShares = new ArrayList<Share>(shares.size());
        for (int i = 0; i < wrapped.size(); i++) {
            newShares.add(null);
        }
        for (ShareWrapper shareWrapper : wrapped) {
            newShares.set(shareWrapper.originalPosition, shareWrapper.share);
        }
        return newShares;
    }

    private static class FillingBucket {
        private List<ShareWrapper> bucket;

        private int increment;

        private FillingBucket(List<ShareWrapper> bucket, int increment) {
            this.bucket = bucket;
            this.increment = increment;
        }

        public void doTheDistribution() {
            int incrementPerShare = increment / bucket.size();
            int remainder = increment % bucket.size();
            if (remainder > 0) {
                ShareWrapper.sortByOriginalPosition(this.bucket);
                // so the first original elements receive the remainder
            }
            for (ShareWrapper wrapper : bucket) {
                wrapper.add(incrementPerShare + Math.min(1, remainder));
                if (remainder > 0) {
                    remainder--;
                }
            }
        }

        int getIncreaseDone() {
            return increment;
        }
    }

    private static int findNextBigger(List<ShareWrapper> wrappers, int start) {
        ShareWrapper startWrapper = wrappers.get(start);
        for (int i = start + 1; i < wrappers.size(); i++) {
            ShareWrapper current = wrappers.get(i);
            if (!startWrapper.haveSameHours(current)) {
                return i;
            }
        }
        return wrappers.size();
    }

    private static FillingBucket fillingBuckectFor(List<ShareWrapper> wrappers,
            int end, int remaining) {
        int hoursToDistribute = end == wrappers.size() ? remaining : (int) Math
                .min(hoursNeededToBeEqual(wrappers, 0, end), remaining);
        return new FillingBucket(wrappers.subList(0, end), hoursToDistribute);
    }

    private static long hoursNeededToBeEqual(List<ShareWrapper> wrappers,
            int startInclusive, int endExclusive) {
        ShareWrapper nextLevel = wrappers.get(endExclusive);
        ShareWrapper currentLevel = wrappers.get(startInclusive);
        long currentLevelHours = (long) currentLevel.getHours();
        long difference = nextLevel.getHours() - currentLevelHours;
        // difference must be long in order to avoid integer overflow
        assert difference > 0;
        return (endExclusive - startInclusive) * difference;
    }

    @Override
    public String toString() {
        return shares.toString();
    }

    public int[] to(ShareDivision newDivison) {
        Validate.isTrue(shares.size() == newDivison.shares.size());
        int[] result = new int[shares.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = newDivison.shares.get(i).getHours()
                    - shares.get(i).getHours();
        }
        return result;
    }

}
