package org.zkoss.ganttz.timetracker;

import java.util.List;

public class PairOfLists<A, B> {

    private final List<A> first;

    private final List<B> second;

    public PairOfLists(List<A> first, List<B> second) {
        this.first = first;
        this.second = second;
    }

    public List<A> getFirst() {
        return first;
    }

    public List<B> getSecond() {
        return second;
    }

}
