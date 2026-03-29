package moonlight.ws.api;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@AllArgsConstructor
@ToString
public class AuthInfo {

	private final boolean authenticated;
	private final String username;
	private final String bearerToken;
}
