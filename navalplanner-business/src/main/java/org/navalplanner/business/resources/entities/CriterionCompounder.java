package org.navalplanner.business.resources.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Compounds some {@link ICriterion} into one <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class CriterionCompounder {

    public static CriterionCompounder build() {
        return new CriterionCompounder();
    }

    public static CriterionCompounder buildAnd(List<ICriterion> criterions) {
        CriterionCompounder compounder = new CriterionCompounder();
        for (ICriterion criterion : criterions) {
            compounder = compounder.and(criterion);
        }
        return compounder;
    }

    public static CriterionCompounder atom(ICriterion criterion) {
        return build().and(criterion);
    }

    public static ICriterion not(ICriterion criterion) {
        return new Negated(criterion);
    }

    public static ICriterion not(CriterionCompounder compounder) {
        return not(compounder.getResult());
    }

    private static class Negated implements ICriterion {
        private final ICriterion criterion;

        private Negated(ICriterion criterion) {
            this.criterion = criterion;
        }

        @Override
        public boolean isSatisfiedBy(Resource resource) {
            return !criterion.isSatisfiedBy(resource);
        }

        @Override
        public boolean isSatisfiedBy(Resource resource, Date start, Date end) {
            return !criterion.isSatisfiedBy(resource, start, end);
        }
    }

    private static class OrClause implements ICriterion {

        private Collection<? extends ICriterion> criterions;

        public OrClause(Collection<? extends ICriterion> atoms) {
            this.criterions = atoms;
        }

        @Override
        public boolean isSatisfiedBy(Resource resource) {
            for (ICriterion criterion : criterions) {
                if (criterion.isSatisfiedBy(resource)) {
                    return true;
                }
            }
            return false;
        }

        public boolean isSatisfiedBy(Resource resource, Date start, Date end) {
            for (ICriterion criterion : criterions) {
                if (criterion.isSatisfiedBy(resource, start, end)) {
                    return true;
                }
            }
            return false;
        }

    }

    private static class AndClause implements ICriterion {
        private List<ICriterion> criterions;

        AndClause() {
            this.criterions = new LinkedList<ICriterion>();
        }

        private AndClause(List<ICriterion> atoms) {
            this.criterions = atoms;
        }

        public AndClause and(ICriterion criterion) {
            return new AndClause(join(criterions, criterion));
        }

        private static List<ICriterion> join(List<ICriterion> previous,
                ICriterion criterion) {
            LinkedList<ICriterion> result = new LinkedList<ICriterion>(previous);
            result.add(criterion);
            return result;
        }

        @Override
        public boolean isSatisfiedBy(Resource resource) {
            for (ICriterion criterion : criterions) {
                if (!criterion.isSatisfiedBy(resource))
                    return false;
            }
            return true;
        }

        @Override
        public boolean isSatisfiedBy(Resource resource, Date start, Date end) {
            for (ICriterion criterion : criterions) {
                if (!criterion.isSatisfiedBy(resource, start, end))
                    return false;
            }
            return true;
        }

    }

    private final List<AndClause> clauses;

    private CriterionCompounder() {
        this(new AndClause());
    }

    private CriterionCompounder(AndClause andClause) {
        clauses = new ArrayList<AndClause>();
        clauses.add(andClause);
    }

    private CriterionCompounder(List<AndClause> clauses) {
        this.clauses = clauses;
    }

    private AndClause getLast() {
        return clauses.get(clauses.size() - 1);
    }

    private List<AndClause> updateLast(AndClause clause) {
        ArrayList<AndClause> arrayList = new ArrayList<AndClause>(clauses);
        arrayList.set(arrayList.size() - 1, clause);
        return arrayList;
    }

    public CriterionCompounder and(CriterionCompounder compounder) {
        return and(compounder.getResult());
    }

    public CriterionCompounder and(ICriterion criterion) {
        return new CriterionCompounder(updateLast(getLast().and(criterion)));
    }

    public CriterionCompounder or(CriterionCompounder compounder) {
        return or(compounder.getResult());
    }

    public CriterionCompounder or(ICriterion criterion) {
        ArrayList<AndClause> copied = new ArrayList<AndClause>(clauses);
        copied.add(new AndClause());
        return new CriterionCompounder(copied).and(criterion);
    }

    public ICriterion getResult() {
        return new OrClause(clauses);
    }

}
