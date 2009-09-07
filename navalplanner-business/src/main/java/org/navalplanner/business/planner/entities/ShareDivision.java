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
            return share.getHours() - other.share.getHours();
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
            this.share = share.add(hours);
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

    public ShareDivision add(int increment) {
        List<ShareWrapper> wrapped = ShareWrapper.wrap(shares);
        Collections.sort(wrapped);
        int i = 0;
        while (i < wrapped.size()) {
            FillingBucket bucket = findNextFillingBucket(wrapped, i, increment);
            bucket.doTheDistribution();
            increment = increment - bucket.getIncreaseDone();
            assert increment >= 0;
            if (increment == 0)
                break;
            i++;
        }
        assert increment == 0 : "all is assigned";
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

    private static FillingBucket findNextFillingBucket(
            List<ShareWrapper> wrappers, int start, int remaining) {
        ShareWrapper startWrapper = wrappers.get(start);
        for (int i = start + 1; i < wrappers.size(); i++) {
            ShareWrapper current = wrappers.get(i);
            if (!startWrapper.haveSameHours(current)) {
                return new FillingBucket(wrappers.subList(0, i), Math.min(
                        hoursNeededToBeEqual(wrappers, 0, i), remaining));
            }
        }
        return new FillingBucket(wrappers, remaining);
    }

    private static int hoursNeededToBeEqual(List<ShareWrapper> wrappers,
            int startInclusive, int endExclusive) {
        ShareWrapper nextLevel = wrappers.get(endExclusive);
        ShareWrapper currentLevel = wrappers.get(startInclusive);
        int difference = nextLevel.getHours() - currentLevel.getHours();
        assert difference > 0;
        return (endExclusive - startInclusive) * difference;
    }

    @Override
    public String toString() {
        return shares.toString();
    }

}
