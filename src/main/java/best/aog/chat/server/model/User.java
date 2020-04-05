package best.aog.chat.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class User {
    private String userName;
    private String password;

    @Override
    public String toString() {
        return userName;
    }
}