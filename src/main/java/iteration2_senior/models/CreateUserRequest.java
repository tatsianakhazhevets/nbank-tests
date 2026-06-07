package iteration2_senior.models;

import iteration2_senior.generators.StringGeneratingRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserRequest extends BaseModel {
    @StringGeneratingRule(regex = "[A-Za-z0-9._-]{3,15}")
    private String username;
    @StringGeneratingRule(regex = "[A-Z]{3}[a-z]{3}[0-9]{3}[$]{1}")
    private String password;
    @StringGeneratingRule(regex = "USER")
    private String role;
}