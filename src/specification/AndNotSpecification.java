package specification;

public class AndNotSpecification <T> extends CompositeSpecification<T> {
    private final Specification<T> left;
    private final Specification<T> right;

    public AndNotSpecification(Specification<T> left, Specification<T> right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean isSatisfiedBy(T candidate) {
        return left.isSatisfiedBy(candidate) && !right.isSatisfiedBy(candidate);
    }
}
