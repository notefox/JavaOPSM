import java.io.*;
import java.nio.file.NoSuchFileException;
import java.util.Arrays;
import java.util.HashMap;

public class InitViaFile {
    public static HashMap<String, HashMap<String, String>> init(File file) throws IOException {
        if (checkIniExistence(file))
            return createDefaultInitFile(file);

        String readString = readStringFromBufferedWriter(file);
        String usableLines = cleanUpReadString(readString);
        HashMap<String, HashMap<String, String>> returner = readInMap(usableLines);

        return returner;
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
    private static HashMap<String, HashMap<String, String>> createDefaultInitFile(File file) throws IOException {
        //noinspection ResultOfMethodCallIgnored
        file.createNewFile();

        FileWriter fw = new FileWriter(file);
        fw.write("[DefaultVariables] \n");
        fw.write("modules_dir = modules/\n");
        fw.write("scripts_dir = scripts/\n");
        fw.write("\n");
        fw.write(";; [exampleModule]\n");
        fw.write(";; name = fileName/projectDirName\n");
        fw.write(";; type = single/project/script/other\n");
        fw.write(";; communication = none/socket\n");
        fw.write(";; parameter = any\n");
        fw.flush();
        fw.close();

        HashMap<String, HashMap<String, String>> iniList = new HashMap<>();
        String blockName = "defaultVariable";
        HashMap<String, String> blockValues = new HashMap<>();
        blockValues.put("modules_dir", "modules/");
        blockValues.put("scripts_dir", "scripts/");
        iniList.put(blockName, blockValues);

        return iniList;
    }
}
