package org.zkoss.ganttz.data.resourceload;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.zkoss.ganttz.data.resourceload.LoadLevel.Category;

public class LoadLevelTest {

    private LoadLevel loadLevel;

    @Test
    public void thePercengateCanBeZero() {
        givenLoadLevelWithPercentage(0);
        assertThat(loadLevel.getPercentage(), equalTo(0));
    }

    private void givenLoadLevelWithPercentage(int percentage) {
        this.loadLevel = new LoadLevel(percentage);
    }

    @Test
    public void thePercentageCanBeGreaterThan100() {
        givenLoadLevelWithPercentage(101);
        assertThat(loadLevel.getPercentage(), equalTo(101));
    }

    @Test(expected = IllegalArgumentException.class)
    public void thePercentageCannotBeNegative() {
        givenLoadLevelWithPercentage(-1);
    }

    @Test
    public void categoryForZero() {
        givenLoadLevelWithPercentage(0);
        thenTheCategoryIs(Category.NO_LOAD);
    }

    private void thenTheCategoryIs(Category category) {
        assertThat(loadLevel.getCategory(), sameInstance(category));
    }

    @Test
    public void categoryForLessThan100AndMoreThanZero() {
        givenLoadLevelWithPercentage(60);
        thenTheCategoryIs(Category.SOME_LOAD);
    }

    @Test
    public void categoryFor100() {
        givenLoadLevelWithPercentage(100);
        thenTheCategoryIs(Category.FULL_LOAD);
    }

    @Test
    public void categoryForMoreThan100() {
        givenLoadLevelWithPercentage(101);
        thenTheCategoryIs(Category.OVERLOAD);
    }

    @Test(expected = IllegalArgumentException.class)
    public void theCategoryThrowsExceptionIfCantHandleThePercentage() {
        Category.categoryFor(-1);
    }

}
