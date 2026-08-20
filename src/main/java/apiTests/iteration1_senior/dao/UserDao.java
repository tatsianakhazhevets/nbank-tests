package apiTests.iteration1_senior.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDao {
    //описание конкретной строчки в базе данных
    private Long id;
    private String username;
    private String password;
    private String role;
    private String name;
}