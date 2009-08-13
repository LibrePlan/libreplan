package org.zkoss.ganttz.util.zoom;

/**
 * @author Francisco Javier Moran Rúa
 */
public enum ZoomLevel {

    DETAIL_ONE(DetailOneTimeTrackerState.INSTANCE), DETAIL_TWO(
            DetailTwoTimeTrackerState.INSTANCE), DETAIL_THREE(
            DetailThreeTimeTrackerState.INSTANCE), DETAIL_FOUR(
            DetailFourTimeTrackerState.INSTANCE), DETAIL_FIVE(
            DetailFiveTimeTrackerState.INSTANCE);

    private final TimeTrackerState state;

    private ZoomLevel(TimeTrackerState state) {
        if (state == null)
            throw new IllegalArgumentException("state cannot be null");
        this.state = state;
    }

    /**
     * @return if there is no next, returns <code>this</code>. Otherwise returns
     *         the next one.
     */
    public ZoomLevel next() {
        final int next = ordinal() + 1;
        if (next == ZoomLevel.values().length) {
            return this;
        }
        return ZoomLevel.values()[next];
    }

    /**
     * @return if there is no previous, returns <code>this</code>. Otherwise
     *         returns the previous one.
     */
    public ZoomLevel previous() {
        if (ordinal() == 0) {
            return this;
        }
        return ZoomLevel.values()[ordinal() - 1];
    }

    public TimeTrackerState getTimeTrackerState() {
        return state;
    }

}
