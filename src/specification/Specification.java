package specification;

public interface Specification<T> {
    boolean isSatisfiedBy(T candidate);

    Specification<T> and(Specification<T> other);
    Specification<T> or(Specification<T> other);
    Specification<T> not();
    Specification<T> andNot(Specification<T> other);
    Specification<T> orNot(Specification<T> other);

    //Anoter combinatory operator
    Specification<T> implies(Specification<T> other);

}
