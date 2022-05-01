package fileIO;

import Setting.Setting;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MyFileWriter {

    private Writer writer;
    private String outputFilePath;
    private final String separator;

    public MyFileWriter() {
        separator = File.separator;
    }

    public void recordData(String fileName, Boolean isExp) {
        switchDirector(isExp);
        generateFileName(fileName);
        setUpWriter();
    }

    private void switchDirector(Boolean isExp) {
        try {
            String directName = "Data";
            if (isExp)
                directName = "Experiment";

            SimpleDateFormat sdf = new SimpleDateFormat();
            sdf.applyPattern("yyyy-MM-dd");
            Date date = new Date();


            MyFileReader.findDirector();

            if (isExp)
                outputFilePath = Setting.pathDirection + separator + directName + sdf.format(date);
            else
                outputFilePath = Setting.pathDirection + separator + directName;

            File directory = new File(outputFilePath);
            if (!directory.exists()) {
                directory.mkdir();
            }
        } catch (Exception e) {
            System.out.println("Cannot create director!");
        }

    }

    public boolean checkFileExist(String fileName, boolean isExp) {
        switchDirector(isExp);
        String fullName = outputFilePath + separator + fileName;
        File tempFile = new File(fullName);
        return tempFile.exists();
    }

    //data set is checked before, so only exp data need to checkFileExist.
    private void generateFileName(String fileName) {
        try {
            outputFilePath = outputFilePath + separator + fileName + ".txt";
        } catch (Exception e) {
            System.out.println("Cannot get the root path!");
        }
    }

    private void setUpWriter() {
        try {
            File file = new File(outputFilePath);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, "utf-8");
            writer = new BufferedWriter(outputStreamWriter);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void writeToFile(String content) {

        try {
            writer.write(content);
            writer.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
