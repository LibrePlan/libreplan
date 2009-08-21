package org.zkoss.ganttz.data.resourceload;

import org.apache.commons.lang.Validate;

public class LoadLevel {

    public enum Category {

        NO_LOAD {
            @Override
            public boolean contains(int percentage) {
                return percentage == 0;
            }
        },
        SOME_LOAD {
            @Override
            public boolean contains(int percentage) {
                return percentage < 100;
            }
        },
        FULL_LOAD {
            @Override
            public boolean contains(int percentage) {
                return percentage == 100;
            }
        },
        OVERLOAD {
            @Override
            public boolean contains(int percentage) {
                return percentage > 100;
            }
        };

        protected abstract boolean contains(int percentage);
        public static Category categoryFor(int percentage) {
            for (Category category : values()) {
                if (category.contains(percentage))
                    return category;
            }
            throw new RuntimeException("couldn't handle " + percentage);
        }
    }

    private final int percentage;

    public LoadLevel(int percentage) {
        Validate.isTrue(percentage >= 0);
        this.percentage = percentage;

    }

    public int getPercentage() {
        return percentage;
    }

    public Category getCategory() {
        return Category.categoryFor(percentage);
    }


}
