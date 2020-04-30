package ru.mauveferret;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.*;


public class Main  {

    public static void main(String[] args) {
        String currentJar = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String currentDir = new File(currentJar).getParent();
        System.out.println(currentDir);

        for (String filePaths: searching(new File(currentDir)))
        {
            boolean iSConvertingSuccessed = convert(filePaths);
            System.out.println(filePaths+" converting is"+ ((iSConvertingSuccessed)? " OK":" not OK"));
        }
        System.out.println("Converting is finished! Say bye :)");
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
        scanner.close();
    }

    private static boolean convert(String filePath)
    {

        try {

            //creating dir far all masses
            String dir= new File(filePath).getParent()+File.separator+new File(filePath).getName().replace(".csv","");
            //temp
            System.out.println("dir is created: "+dir);
            if (!new File(dir).mkdir() && !new File(dir).exists() ) throw new Exception("dir wasn't created");    //ha-ha
            HashMap<Integer, FileOutputStream> massFiles = new HashMap<>();



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
                    mass = mass.substring(0, mass.indexOf("."));
                    int massNumber = Integer.parseInt(mass.trim());

                    if (!massFiles.containsKey(massNumber))
                    {
                        File f = new File(dir+File.separator+new File(filePath).getName().
                                replace(".csv","")+"_mass_"+massNumber+".txt");
                        FileOutputStream writer = new FileOutputStream(f);
                        massFiles.put(massNumber,writer);
                    }

                    //date?!
                    int msPassed = convertDate(lineArray[0]);

                    massFiles.get(massNumber).write((msPassed+" "+lineArray[2]+"\n").getBytes());
                }
                if (line.contains("</ConfigurationData>")) configurationIsSkipped = true;
            }

            //close all files
            for (FileOutputStream f: massFiles.values()) f.close();
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