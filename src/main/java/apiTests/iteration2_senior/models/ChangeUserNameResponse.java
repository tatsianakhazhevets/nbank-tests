package apiTests.iteration2_senior.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChangeUserNameResponse extends BaseModel {
    private String message;
    private UserProfileNestedResponse customer;
}