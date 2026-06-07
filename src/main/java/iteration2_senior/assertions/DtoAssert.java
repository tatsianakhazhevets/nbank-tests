package iteration2_senior.assertions;

import iteration2_senior.models.comparator.DtoComparator;

import java.util.List;

public class DtoAssert<T1, T2> {

    private final T1 left;
    private final T2 right;

    public DtoAssert(T1 left, T2 right) {
        this.left = left;
        this.right = right;
    }

    public void match() {

        List<String> errors = DtoComparator.compare(left, right);

        if (!errors.isEmpty()) {
            throw new AssertionError("DTO mismatch:\n" + String.join("\n", errors));
        }
    }
}