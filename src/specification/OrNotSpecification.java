package specification;

public class OrNotSpecification <T> extends CompositeSpecification <T>{
    private final Specification<T> left;
    private final Specification<T> right;

    public OrNotSpecification(Specification<T> left, Specification<T> right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean isSatisfiedBy(T candidate) {
        return left.isSatisfiedBy(candidate) && !right.isSatisfiedBy(candidate);
    }
}
