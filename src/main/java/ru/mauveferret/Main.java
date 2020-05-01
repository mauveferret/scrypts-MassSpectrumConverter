package ru.mauveferret;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.*;


public class Main  {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String currentJar = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String currentDir = new File(currentJar).getParent();
        System.out.print("Do you want to get time dependencies? If no, i'l make mass spectrum (Y/N or Д/Н) ");
        String answer = scanner.nextLine();
        boolean doTimeDependencies;
        doTimeDependencies = answer.contains("Y") | answer.contains("y") | answer.contains("д") | answer.contains("н") ;
        if (doTimeDependencies) System.out.println("Started converting to time dependencies...");
        else
            System.out.println("Started creating mass spectrum...");
        for (String filePaths: searching(new File(currentDir)))
        {
            boolean iSConvertingSuccessed = convert(filePaths, doTimeDependencies);
            System.out.println(filePaths+" converting is"+ ((iSConvertingSuccessed)? " OK":" not OK"));
        }
        System.out.println("Converting is finished! Say bye :)");

        scanner.nextLine();
        scanner.close();
    }

    private static boolean convert(String filePath, boolean doTimeDependencies)
    {

        try {
            //String timeLineForMassSpectrum = "AtomicNumber/time ";
            String dir = "";
            FileOutputStream spectrumWriter = new FileOutputStream(new File("shit"));
            if (doTimeDependencies)
            {
                //creating dir far all masses
                dir = new File(filePath).getParent() + File.separator + new File(filePath).getName().replace(".csv", "");
            //temp
            System.out.println("dir is created: " + dir);
            if (!new File(dir).mkdir() && !new File(dir).exists()) throw new Exception("dir wasn't created");    //ha-ha
            }
            else
            {
                String path = new File(filePath).getParent() + File.separator + new File(filePath).getName().replace(".csv", ".txt");
                spectrumWriter = new FileOutputStream(new File(path));
            }
            HashMap<Double, FileOutputStream> massFiles = new HashMap<>();
            HashMap<Double, Stack<String>> massSpectrum = new HashMap<>();



            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line = "";
            boolean configurationIsSkipped = false;

            //reading all lines
            while (reader.ready())
            {
                line = reader.readLine();
                if (line.contains("version")) configurationIsSkipped = false;
                if (configurationIsSkipped)
                {
                    String[] lineArray = line.split(",");
                    //what mass?
                    String mass = lineArray[1];
                    //mass = mass.substring(0, mass.indexOf("."));
                    double massNumber = Double.parseDouble(mass.trim());

                    if (doTimeDependencies) {
                        if (!massFiles.containsKey(massNumber)) {
                            File f = new File(dir + File.separator + new File(filePath).getName().
                                    replace(".csv", "") + "_mass_" + massNumber + ".txt");
                            FileOutputStream writer = new FileOutputStream(f);
                            massFiles.put(massNumber, writer);
                        }
                        //date?!
                        int msPassed = convertDate(lineArray[0]);
                        massFiles.get(massNumber).write((msPassed + " " + lineArray[2] + "\n").getBytes());
                    }
                    else
                    {
                        //creating load for the operating memory, then will write to file
                        if (!massSpectrum.containsKey(massNumber)) massSpectrum.put(massNumber, new Stack<String>());
                        massSpectrum.get(massNumber).push(lineArray[2]);
                        //timeLineForMassSpectrum+=( (int) convertDate(lineArray[0])/1000)+" ";

                    }
                }
                if (line.contains("</ConfigurationData>")) configurationIsSkipped = true;
            }

            //closing streams, writing files to spectrumtable

            if (doTimeDependencies)
                for (FileOutputStream f: massFiles.values()) f.close(); //close all files
            else
            {
                //spectrumWriter.write((timeLineForMassSpectrum+"\n").getBytes());
                String lineToWrite;

                SortedSet<Double> sortedMasses = new TreeSet();

                for (double massNumber: massSpectrum.keySet()) sortedMasses.add(massNumber);

                for (double massNumber: sortedMasses)
                {
                    lineToWrite = massNumber+" ";
                    while (!massSpectrum.get(massNumber).isEmpty())
                        lineToWrite+=massSpectrum.get(massNumber).pop()+" ";
                    spectrumWriter.write((lineToWrite+"\n").getBytes());
                }
                spectrumWriter.close();
            }

            return true;
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            return  false;
        }
    }


    private  static boolean isFirstDate =true;
    private static int firstDate;

    private static int convertDate(String date)
    {
        date = date.trim();
        //System.out.println(date);
        int hours = Integer.parseInt(date.substring(11,13));
        int minutes = Integer.parseInt(date.substring(14,16));
        int seconds = Integer.parseInt(date.substring(17,19));
        int ms = Integer.parseInt(date.substring(20,23));

        ms = (hours*3600+minutes*60+seconds)*1000+ms;

        /*if (isFirstDate)
        {
            firstDate = ms;
            isFirstDate = false;
        }

         */

        // made to write dates from local zero
        //return ms-firstDate;
        return ms;
    }


    private static List<String> searching(File rootDir) {
        List<String> result = new ArrayList<>();

        LinkedList<File> dirList = new LinkedList<>();
        if (rootDir.isDirectory()) {
            dirList.addLast(rootDir);
        }

        while (dirList.size() > 0) {
            File[] filesList = dirList.getFirst().listFiles();
            if (filesList != null) {
                for (File path : filesList) {
                    if (path.isDirectory()) {
                        //wouldn't watch inside folders
                        //dirList.addLast(path);
                    } else {
                        String simpleFileName = path.getName();

                        if (simpleFileName.endsWith(".csv")) {
                            result.add(path.getAbsolutePath().toString());
                        }
                    }
                }
            }
            dirList.removeFirst();
        }
        return result;
    }

}