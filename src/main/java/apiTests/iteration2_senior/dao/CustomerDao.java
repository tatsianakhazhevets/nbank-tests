package apiTests.iteration2_senior.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerDao {
    private Integer id;
    private String username;
    private String password;
    private String role;
    private String name;
}