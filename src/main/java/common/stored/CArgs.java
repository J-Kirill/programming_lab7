package common.stored;

import common.InvalidData;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record CArgs(String group1, String group2) implements Serializable {
    public static CArgs parse(String command) throws InvalidData {
        Pattern p = Pattern.compile(
                "([\\wА-Яа-яёЁ]+)" +
                        "(?:\\s([\\wА-Яа-яёЁ]+)?)?",
                Pattern.DOTALL
        );
        Matcher m = p.matcher(command.stripTrailing());
        if (m.find()) {
            return new CArgs(m.group(1), m.group(2));
        } else {
            throw new InvalidData("Некорректный ввод");
        }
    }
}
