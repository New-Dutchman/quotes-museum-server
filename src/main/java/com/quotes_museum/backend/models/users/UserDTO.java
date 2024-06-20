package com.quotes_museum.backend.models.users;
import lombok.*;

@RequiredArgsConstructor
@Data
public class UserDTO {

    private final String name;

    private final String hashedPassword;

    private final String role;

}
