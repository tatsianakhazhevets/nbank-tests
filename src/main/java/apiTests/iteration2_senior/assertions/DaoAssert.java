package apiTests.iteration2_senior.assertions;

import apiTests.iteration2_senior.dao.comparator.DaoComparator;

import java.util.List;

public class DaoAssert<T1, T2> {
    private final T1 dto;
    private final T2 dao;

    public DaoAssert(T1 dto, T2 dao) {
        this.dto = dto;
        this.dao = dao;
    }

    public void match() {
        List<String> errors = DaoComparator.compare(dto, dao);

        System.out.println();
        System.out.println("==============================================");
        System.out.println("DTO and DAO");
        System.out.println("==============================================");

        System.out.println("DTO = " + dto);
        System.out.println("DAO = " + dao);
        System.out.println("ERRORS = " + errors);

        if (!errors.isEmpty()) {
            throw new AssertionError("DAO mismatch:\n" + String.join("\n", errors));
        }
    }
}