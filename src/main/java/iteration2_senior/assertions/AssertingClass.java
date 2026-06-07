package iteration2_senior.assertions;

public class AssertingClass {

    public static <T1, T2> DtoAssert<T1, T2> assertThat(T1 left, T2 right) {
        return new DtoAssert<>(left, right);
    }
}