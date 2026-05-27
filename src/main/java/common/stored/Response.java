package common.stored;

import java.io.Serializable;

public record Response(String message, Boolean success) implements Serializable {
}
