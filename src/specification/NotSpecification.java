package specification;

public class NotSpecification<T> extends CompositeSpecification<T> {
    private final Specification<T> other;

    public NotSpecification(Specification<T> other) {
        this.other = other;
    }

    @Override
    public boolean isSatisfiedBy(T candidate) {
        return !other.isSatisfiedBy(candidate);
    }
}
