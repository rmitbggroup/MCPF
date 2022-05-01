package fileIO;

import Setting.Setting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class MyFileReader {

    private BufferedReader bufferedReader;

    public static void findDirector() {
        if (Setting.pathDirection != null && !Setting.pathDirection.equals(""))
            return;
        String path = "";
        String separator = File.separator;

        switch (Setting.system) {
            case 0:
                path = System.getProperty("user.dir");
                break;

            case 1:
                path = System.getProperty("user.dir");
                break;
            case 2:
                System.out.println("Running Path " + System.getProperty("user.dir"));
                for (String director : System.getProperty("user.dir").split("/")) {
                    if (director.equals(""))
                        continue;
                    if (director.equals("target"))
                        break;
                    path += separator + director;
                }
                break;
            default:
                System.out.println("File reading fault!");
                throw new IllegalStateException("Undetected system type " + Setting.system);
        }
        Setting.pathDirection = path;
    }

    public boolean initializeReader(String inputFilePath) {
        try {
            findDirector();

            String path = Setting.pathDirection;
            String separator = File.separator;

            path += separator + "Data";

            path += separator + inputFilePath;
            System.out.print("MyFileReader - initializeReader ");
            System.out.println(path);

            bufferedReader = new BufferedReader(new FileReader(path));
            return true;

        } catch (Exception e) {
            System.out.println("File is not existed!");
        }
        return false;
    }

    public String getNextLine() {
        try {
            String line = bufferedReader.readLine();
            if (line != null) {
                return line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
