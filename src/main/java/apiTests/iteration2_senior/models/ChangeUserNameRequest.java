package apiTests.iteration2_senior.models;

import apiTests.iteration2_senior.generators.StringGeneratingRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChangeUserNameRequest extends BaseModel {
    @StringGeneratingRule(regex = "[A-Za-z]{1,20}+ [A-Za-z]{1,20}")
    private String name;
}