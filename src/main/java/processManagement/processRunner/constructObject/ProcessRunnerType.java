package processManagement.processRunner.constructObject;

import java.util.ArrayList;
import java.util.List;

public enum ProcessRunnerType {
    STANDARD_RUNNER ("standard"),
    DIRECT_COMMAND_RUNNER ("direct"),
    SOCKET_COMMUNICATION_RUNNER ("socket_com"),
    PRECOMPILE_RUNNER("precomp"),
    SCRIPT_RUNNER("script"),
    CUSTOM("custom");

    private final String value;

    ProcessRunnerType(String value) {
        this.value = value;
    }

    public static ProcessRunnerType parseValue(String typeValue) {
        if (getAllTypes().stream().noneMatch(x -> x.value.equals(typeValue)))
            throw new TypeNotPresentException("type non existing : " + typeValue, new Exception("ProcessRunnerTypeBuilderException"));

        return (ProcessRunnerType) getAllTypes()
                .stream()
                .filter(x -> x.getValue().equals(typeValue))
                .toArray()[0];
    }

    public String getValue() {
        return value;
    }

    public static List<ProcessRunnerType> getAllTypes() {
        ArrayList<ProcessRunnerType> list = new ArrayList<>();
        list.add(STANDARD_RUNNER);
        list.add(DIRECT_COMMAND_RUNNER);
        list.add(SOCKET_COMMUNICATION_RUNNER);
        list.add(PRECOMPILE_RUNNER);
        list.add(SCRIPT_RUNNER);
        list.add(CUSTOM);
        return list;
    }
}
