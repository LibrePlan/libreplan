package org.zkoss.ganttz.data.resourceload;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Test;

public class LoadTimelineTest {

    private LoadTimeLine loadTimeLine;
    private String conceptName;

    @Test(expected = IllegalArgumentException.class)
    public void aLoadTimelineMustHaveANotNullName() {
        new LoadTimeLine(null, Collections.<LoadPeriod> emptyList());
    }

    @Test(expected = IllegalArgumentException.class)
    public void aLoadTimelineMustHaveANotEmptyName() {
        new LoadTimeLine("", Collections.<LoadPeriod> emptyList());
    }

    @Test(expected = IllegalArgumentException.class)
    public void aLoadTimelineCannotHaveNullLoadPeriods() {
        new LoadTimeLine("bla", null);
    }

    @Test
    public void theConceptNameCanBeRetrieved() {
        givenValidLoadTimeLine();
        assertThat(conceptName, equalTo(loadTimeLine.getConceptName()));
    }

    private void givenValidLoadTimeLine() {
        conceptName = "bla";
        loadTimeLine = new LoadTimeLine(conceptName, Arrays
                .asList(new LoadPeriod(new LocalDate(2009, 10, 5),
                        new LocalDate(2009, 10, 11), new LoadLevel(20))));
    }

    @Test
    public void aLoadTimelineCanHaveZeroLoadPeriods() {
        new LoadTimeLine("bla", Collections.<LoadPeriod> emptyList());
    }

    @Test
    public void aLoadTimelineSortsItsReceivedPeriods() {
        LoadPeriod l1 = new LoadPeriod(new LocalDate(2009, 10, 5),
                new LocalDate(2009, 10, 11), new LoadLevel(20));
        LoadPeriod l2 = new LoadPeriod(new LocalDate(2009, 5, 3),
                new LocalDate(2009, 6, 3), new LoadLevel(20));
        LoadTimeLine loadTimeLine = new LoadTimeLine("bla", Arrays.asList(l1, l2));

        List<LoadPeriod> loadPeriods = loadTimeLine.getLoadPeriods();
        assertThat(loadPeriods.get(0), sameInstance(l2));
        assertThat(loadPeriods.get(1), sameInstance(l1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void theLoadPeriodsMustNotOverlap() {
        LoadPeriod l1 = new LoadPeriod(new LocalDate(2009, 10, 5),
                new LocalDate(2009, 10, 11), new LoadLevel(20));
        LoadPeriod l2 = new LoadPeriod(new LocalDate(2009, 5, 3),
                new LocalDate(2009, 10, 10), new LoadLevel(20));
        new LoadTimeLine("bla", Arrays.asList(l1, l2));
    }

}
