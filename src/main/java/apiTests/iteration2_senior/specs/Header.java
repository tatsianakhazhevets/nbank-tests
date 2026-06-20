package apiTests.iteration2_senior.specs;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Header {
    AUTHORIZATION("Authorization");

    private final String header;
}