package common;

/**
 * Класс-Исключение для указания на некорректность указанных данных.
 */
public class InvalidData extends Exception {
    public InvalidData(String message) {
        super(message);
    }
}
