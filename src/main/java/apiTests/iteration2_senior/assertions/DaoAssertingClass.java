package apiTests.iteration2_senior.assertions;

public class DaoAssertingClass {
    public static <T1, T2> DaoAssert<T1, T2> assertThat(T1 dto, T2 dao) {
        return new DaoAssert<>(dto, dao);
    }
}