package specification;

public abstract class CompositeSpecification <T> implements Specification<T>{
    @Override public abstract boolean isSatisfiedBy(T candidate);
    @Override public Specification<T> and(Specification<T> other)    { return new AndSpecification<>(this, other); }
    @Override public Specification<T> andNot(Specification<T> other) { return new AndNotSpecification<>(this, other); }
    @Override public Specification<T> or(Specification<T> other)     { return new OrSpecification<>(this, other); }
    @Override public Specification<T> orNot(Specification<T> other)  { return new OrNotSpecification<>(this, other); }
    @Override public Specification<T> not()                          { return new NotSpecification<>(this); }
}
