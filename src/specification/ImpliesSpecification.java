package specification;

public class ImpliesSpecification<T> extends CompositeSpecification<T>{
    private final Specification<T> left;
    private final Specification<T> right;

    public ImpliesSpecification(Specification<T> left, Specification<T> right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean isSatisfiedBy(T candidate) {
        //Not Or
        return !left.isSatisfiedBy(candidate) || right.isSatisfiedBy(candidate);
    }
}
