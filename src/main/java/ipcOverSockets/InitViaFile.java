package ipcOverSockets;

import java.io.*;
import java.nio.file.NoSuchFileException;
import java.util.Arrays;
import java.util.HashMap;

public class InitViaFile {
    /**
     * reads in given init file and gives back HashMap of Values
     *
     * if the ini file doesn't exists, then a example ini will be created at the given path / file
     *
     * @param file file
     * @return HashMap<blockName , HashMap<Key, Value>>
     *
     *     as example how a entry looks like in the ini file and how it will be saved in the returned Map :
     *
     *      [blockName_A]
     *      Key1a = some value
     *      Key2a = some other value
     *
     *      [blockName_B]
     *      Key1b = another
     *      Key2b = and more
     *
     *      [blockName_A -> [Key1a -> "some value", Key2a -> "some other value"],
     *       blockName_B -> [Key1b -> "another", Key2b -> "and more"]]
     *
     * @throws IOException is thrown, if the reading process failed
     */
    public static HashMap<String, HashMap<String, String>> init(File file) throws IOException {
        if (checkIniExistence(file))
            return createDefaultInitFile(file);

        String readString = readStringFromBufferedWriter(file);
        String usableLines = cleanUpReadString(readString);

        return readInMap(usableLines);
    }
    private static HashMap<String, HashMap<String, String>> readInMap(String usableLines) {
        String[] split = usableLines.split("\n");

        String blockKey = "";
        HashMap<String, String> blockValues = new HashMap<>();

        HashMap<String, HashMap<String, String>> iniList = new HashMap<>();

        for (String line : split) {
            if (line.startsWith("[") && line.endsWith("]")) {
                if (!blockValues.isEmpty())
                    iniList.put(blockKey, blockValues);

                blockValues = new HashMap<>();
                blockKey = line.substring(1, line.length()-1);
            } else {
                String[] lineSplit = line.split("=");
                String value = buildStringFromArray(lineSplit, " ", 1).trim();
                blockValues.put(lineSplit[0], value);
            }
        }
        iniList.put(blockKey, blockValues);
        return iniList;
    }
    private static String buildStringFromArray(String[] array, String separator, int from) {
        StringBuilder sb = new StringBuilder();
        Arrays.stream(array).skip(from).forEach(x -> sb.append(x).append(separator));
        return sb.toString();
    }
    private static boolean checkIniExistence(File file) throws NoSuchFileException {
        if (!file.exists()) {
            System.err.println("couldn't find " + file.getName() + " in the execution directory, " +
                    "creating new with default values");
            return true;
        } else if (!file.getName().endsWith(".ini")) {
            throw new NoSuchFileException("given file is not a .ini file");
        }
        return false;
    }
    private static String readStringFromBufferedWriter(File file) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        StringBuilder readString = new StringBuilder();
        try {
            while (br.ready())
                readString.append(br.readLine()).append("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return readString.toString();
    }
    private static String cleanUpReadString(String readString) {
        // cleanup String
        String usableLines = "";
        for (String s : readString.split("\n"))
            if (!(s.startsWith(";;") || s.isEmpty()))
                usableLines += s + "\n";

        return usableLines;
    }

    /**
     * creates ini file and returns returnable hashmap of default values
     */
    private static HashMap<String, HashMap<String, String>> createDefaultInitFile(File file) throws IOException {
        //noinspection ResultOfMethodCallIgnored
        file.createNewFile();

        FileWriter fw = new FileWriter(file);
        fw.write("[initValues] \n");
        fw.write("modules_dir = modules/\n");
        fw.write("scripts_dir = scripts/\n");
        fw.write("logger_dir = logs/\n");
        fw.write("\n");
        fw.write(";; [exampleModule]\n");
        fw.write(";; name = processName (needed)\n");
        fw.write(";; file = fileName (needed)\n");
        fw.write(";; type = single/project/script (needed)\n");
        fw.write(";; communication = none/socket (needed)\n");
        fw.write("\n");
        fw.write(";; port = any int (only needed if communication socket)\n");
        fw.write("\n");
        fw.write(";; interpreter = system interpreter (python/bash/...)\n");
        fw.write(";; compiler = system compiler (java/...)\n");
        fw.write(";; (either needed)\n");
        fw.write("\n");
        fw.write(";; (optional)\n");
        fw.write(";; parameter = any\n");
        fw.write(";; build = mvn install (only if type = project, then needed)\n");
        fw.write(";; target_jar = target/file.jar (only if type = project, then needed)\n");
        fw.flush();
        fw.close();

        return init(file);
    }
}
