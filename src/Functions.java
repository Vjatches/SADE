import java.io.*;
import java.util.ArrayList;

public class Functions
{
    ArrayList<String> readFileToArrayList(String path) throws IOException
    {
        ArrayList<String> file = new ArrayList<String>();
        File f = new File(path);
        BufferedReader fin = new BufferedReader(new FileReader(f));
        String line;
        while ((line = fin.readLine())!=null) file.add(line);

        return file;
    }

    void writeArrayListToFile(ArrayList<String> strings, String path) throws IOException
    {
        PrintWriter pw = new PrintWriter(path);
        for(int i = 0; i < strings.size(); i++) pw.println(strings.get(i));
        pw.close();
    }

    String invertMask(String ip)
    {
        String invertedIp = "";

        switch(ip)
        {
            case "255.255.255.255":
            {
                invertedIp = "0.0.0.0";
                break;
            }
            case "255.255.255.254":
            {
                invertedIp = "0.0.0.1";
                break;
            }
            case "255.255.255.252":
            {
                invertedIp = "0.0.0.3";
                break;
            }
            case "255.255.255.248":
            {
                invertedIp = "0.0.0.7";
                break;
            }
            case "255.255.255.240":
            {
                invertedIp = "0.0.0.15";
                break;
            }
            case "255.255.255.224":
            {
                invertedIp = "0.0.0.31";
                break;
            }
            case "255.255.255.192":
            {
                invertedIp = "0.0.0.63";
                break;
            }
            case "255.255.255.128":
            {
                invertedIp = "0.0.0.127";
                break;
            }
            case "255.255.255.0":
            {
                invertedIp = "0.0.0.255";
                break;
            }
            case "255.255.254.0":
            {
                invertedIp = "0.0.1.255";
                break;
            }
            case "255.255.252.0":
            {
                invertedIp = "0.0.3.255";
                break;
            }
            case "255.255.248.0":
            {
                invertedIp = "0.0.7.255";
                break;
            }
            case "255.255.240.0":
            {
                invertedIp = "0.0.15.255";
                break;
            }
            case "255.255.224.0":
            {
                invertedIp = "0.0.31.255";
                break;
            }
            case "255.255.192.0":
            {
                invertedIp = "0.0.63.255";
                break;
            }
            case "255.255.128.0":
            {
                invertedIp = "0.0.127.255";
                break;
            }
            case "255.255.0.0":
            {
                invertedIp = "0.0.255.255";
                break;
            }
            case "255.254.0.0":
            {
                invertedIp = "0.1.255.255";
                break;
            }
            case "255.252.0.0":
            {
                invertedIp = "0.3.255.255";
                break;
            }
            case "255.248.0.0":
            {
                invertedIp = "0.7.255.255";
                break;
            }
            case "255.240.0.0":
            {
                invertedIp = "0.15.255.255";
                break;
            }
            case "255.224.0.0":
            {
                invertedIp = "0.31.255.255";
                break;
            }
            case "255.192.0.0":
            {
                invertedIp = "0.63.255.255";
                break;
            }
            case "255.128.0.0":
            {
                invertedIp = "0.127.255.255";
                break;
            }
            case "255.0.0.0":
            {
                invertedIp = "0.255.255.255";
                break;
            }
            case "254.0.0.0":
            {
                invertedIp = "1.255.255.255";
                break;
            }
            case "252.0.0.0":
            {
                invertedIp = "3.255.255.255";
                break;
            }
            case "248.0.0.0":
            {
                invertedIp = "7.255.255.255";
                break;
            }
            case "240.0.0.0":
            {
                invertedIp = "15.255.255.255";
                break;
            }
            case "224.0.0.0":
            {
                invertedIp = "31.255.255.255";
                break;
            }
            case "192.0.0.0":
            {
                invertedIp = "63.255.255.255";
                break;
            }
            case "128.0.0.0":
            {
                invertedIp = "127.255.255.255";
                break;
            }
            case "0.0.0.0":
            {
                invertedIp = "255.255.255.255";
                break;
            }
        }


        return invertedIp;
    }

    boolean validIP (String ip) {
        try {
            if ( ip == null || ip.isEmpty() ) {
                return false;
            }

            String[] parts = ip.split( "\\." );
            if ( parts.length != 4 ) {
                return false;
            }

            for ( String s : parts ) {
                int i = Integer.parseInt( s );
                if ( (i < 0) || (i > 255) ) {
                    return false;
                }
            }
            if ( ip.endsWith(".") ) {
                return false;
            }

            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    boolean checkPassword(String password)
    {
        System.out.println("----------");
        boolean answer = true;
        if(password.length() >=8)
        {
            if(!password.matches(".*[\\s].*"))
            {
                if(!password.matches(".*[0-9].*"))
                {
                    answer = false;
                }
                if(!password.matches(".*[a-z].*"))
                {
                    answer = false;
                }
                if(!password.matches(".*[A-Z].*"))
                {
                    answer = false;
                }
            }
            else
            {
                answer = false;
            }
        }
        else
        {
            answer = false;
        }
        System.out.println(answer);
        return answer;
    }
}