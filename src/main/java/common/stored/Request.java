package common.stored;

import java.io.Serializable;

public record Request(CArgs cArgs, Route route, String login, String password) implements Serializable {
}
