package apiTests.iteration1_senior.models;

import apiTests.iteration1_senior.generators.GeneratingRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateUserRequest extends BaseModel {
    @GeneratingRule(regex = "^[A-Za-z0-9]{3,15}$")
    private String username;
    @GeneratingRule(regex = "^[A-Z]{3}[a-z]{4}[0-9]{3}[$%&]{2}$")
    private String password;
    @GeneratingRule(regex = "^USER$")
    private String role;

    // создай класс.ю который получает на вход data class с описанием поля и типа данных (как любой java class) и генерирует сущность с рандомными значениями на основании типа данных
    // и поддержи требования, что если есть аннотация @GeneratingRule, то используй для генерации regex в ее аргументе
}