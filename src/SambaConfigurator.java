import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.InetAddress;
import java.util.ArrayList;

class Window extends JFrame
{
    String ipAddress = "";
    String netmask = "";
    String network = "";
    String broadcast = "";
    String gateway = "";
    String computersName = "";
    String domainname = "";
    String fullName = "";

    JLabel labelEthernetAdapter = new JLabel("Ethernet adapter");
    JLabel labelIpAddress = new JLabel("IP address");
    JLabel labelNetmask = new JLabel("Netmask");
    JLabel labelGateway = new JLabel("Gateway");
    JLabel labelComputersName = new JLabel("Computer's name");
    JLabel labelDomainname = new JLabel("Domain name");
    JLabel labelFullName = new JLabel("Full name");

    JLabel labelBadIp = new JLabel("Bad ip");
    JLabel labelBadNetmask = new JLabel("Bad netmask");
    JLabel labelBadGateway = new JLabel("Bad gateway");
    JLabel labelBadComputersName = new JLabel("Bad computer's name");
    JLabel labelBadDomainname = new JLabel("Bad domain's name");

    JComboBox comboBoxEth = new JComboBox();
    JTextField jTextFieldIpAddress = new JTextField();
    JTextField jTextFieldNetmask = new JTextField();
    JTextField jTextFieldGateway = new JTextField();
    JTextField jtextFieldComputersName = new JTextField();
    JTextField jTextFieldDomainname = new JTextField();
    JTextField jTextFieldFullName = new JTextField();

    JButton buttonConfigureEthernet = new JButton("Configure");
    //Класс с функциями
    Functions f = new Functions();

    //Список строк файла и список настроенных в нём адаптеров
    ArrayList<String> interfacesFile = new ArrayList<String>();
    ArrayList<InterfaceData> interfaces = new ArrayList<InterfaceData>();

    class ConfigureEthernet implements ActionListener
    {
        void configureInterface()
        {
            System.out.println("Configuring interface...");
            //Отключает кнопку
            buttonConfigureEthernet.setEnabled(false);

            //Ищет нужный интерфейс в списке
            String eth = comboBoxEth.getSelectedItem().toString();
            int number = -1;
            for(int i = 0; i < interfaces.size(); i++)
            {
                if(interfaces.get(i).name.equals(eth)) number = i;
            }

            //Удаляет указанный интерфейс из файла
            int quantity = interfaces.get(number).endPos - interfaces.get(number).startPos;
            for(int i = 0; i < quantity; i++) interfacesFile.remove(interfaces.get(number).startPos);

            //Генерирует адрес сети
            String[] mask = netmask.split("\\.");
            String[] ipAddr = ipAddress.split("\\.");

            StringBuffer ipSubnet = new StringBuffer();
            for(int i = 0; i < 4; i++)
            {
                try
                {
                    if(ipSubnet.length()>0) ipSubnet.append('.');
                    ipSubnet.append(Integer.parseInt(ipAddr[i]) & Integer.parseInt(mask[i]));
                }catch(Exception ex)
                {
                    break;
                }
            }
            network = ipSubnet.toString();

            String[] invertedMask = f.invertMask(netmask).split("\\.");

            String[] networkIp = network.split("\\.");
            StringBuffer ipBroadcast = new StringBuffer();
            for(int i = 0; i < 4; i++)
            {
                try
                {
                    if(ipBroadcast.length()>0) ipBroadcast.append('.');
                    ipBroadcast.append(Integer.parseInt(networkIp[i]) | Integer.parseInt(invertedMask[i]));
                }catch(Exception ex)
                {
                    break;
                }
            }

            broadcast = ipBroadcast.toString();

            if(interfaces.get(number).startPos == interfaces.get(number).endPos)
            {
                interfacesFile.add("");
                interfacesFile.add("#The primary network intarface");
                interfacesFile.add("");
                interfacesFile.add("auto " + interfaces.get(number).name);
                interfacesFile.add("iface " + interfaces.get(number).name + " inet static");
                interfacesFile.add("");
                interfacesFile.add("address " + ipAddress);
                interfacesFile.add("netmask " + netmask);
                interfacesFile.add("network " + network);
                interfacesFile.add("broadcast " + broadcast);
                interfacesFile.add("gateway " + gateway);
                interfacesFile.add("dns-nameservers " + ipAddress + " 8.8.8.8");
                interfacesFile.add("dns-search " + domainname);
                interfacesFile.add("");
            }
            else
            {
                interfacesFile.add(interfaces.get(number).startPos, "auto " + interfaces.get(number).name);
                interfacesFile.add(interfaces.get(number).startPos + 1, "iface " + interfaces.get(number).name + " inet static");
                interfacesFile.add(interfaces.get(number).startPos + 2, "");
                interfacesFile.add(interfaces.get(number).startPos + 3, "address " + ipAddress);
                interfacesFile.add(interfaces.get(number).startPos + 4, "netmask " + netmask);
                interfacesFile.add(interfaces.get(number).startPos + 5, "network " + network);
                interfacesFile.add(interfaces.get(number).startPos + 6, "broadcast " + broadcast);
                interfacesFile.add(interfaces.get(number).startPos + 7, "gateway " + gateway);
                interfacesFile.add(interfaces.get(number).startPos + 8, "dns-nameservers " + ipAddress + " 8.8.8.8");
                interfacesFile.add(interfaces.get(number).startPos + 9, "dns-search " + domainname);
                interfacesFile.add(interfaces.get(number).startPos + 10, "");
            }

            //Записывает файл
            try {
                f.writeArrayListToFile(interfacesFile, "/etc/network/interfaces");
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            System.out.println("Interface configured");
        }

        void setHosts()
        {
            System.out.println("Configuring /etc/hosts ...");
            ArrayList<String> hosts = new ArrayList<>();
            try
            {
                hosts = f.readFileToArrayList("/etc/hosts");
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            String[] testLine;
            boolean configured = false;
            for(int i = 0; i < hosts.size(); i++)
            {
                if(configured) break;
                testLine = hosts.get(i).split("\t");
                if(testLine.length == 2)
                {
                    System.out.println("found");
                    if((testLine[0].equals("127.0.1.1"))||(testLine[1].equals(computersName)))
                    {
                        hosts.remove(i);
                        hosts.add(i, ipAddress + "\t" + fullName);
                        configured = true;
                        break;
                    }
                }
            }

            for(int i = 0; i < hosts.size(); i++)
            {
                if(configured) break;
                testLine = hosts.get(i).split("\t");
                if(testLine.length == 2)
                {
                    System.out.println("found");
                    if(testLine[0].equals(ipAddress))
                    {
                        hosts.remove(i);
                        hosts.add(i, ipAddress + "\t" + fullName);
                        configured = true;
                        break;
                    }
                }
            }

            for(int i = 0; i < hosts.size(); i++)
            {
                if(configured) break;
                testLine = hosts.get(i).split("\t");
                if(testLine.length == 2)
                {
                    System.out.println("found");
                    if((testLine[0].equals("127.0.0.1"))||(testLine[1].equals("localhost")))
                    {
                        hosts.add(i+1, ipAddress + "\t" + fullName);
                        configured = true;
                        break;
                    }
                }
            }

            if(!configured)
            {
                hosts.add(0, ipAddress + "\t" + fullName);
            }

            try {
                f.writeArrayListToFile(hosts, "/etc/hosts");
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Configured /etc/hosts");
        }

        void setHostname()
        {
            System.out.println("Setting hostname...");
            ArrayList<String> hostname = new ArrayList<>();
            hostname.add(fullName);
            try {
                f.writeArrayListToFile(hostname, "/etc/hostname");
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Hostname set");
        }

        void updatePackages()
        {
            System.out.println("Updating packages...");
            String s = null;
            try
            {
                Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "apt-get update"});
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                System.out.println("Here is the standard output of the command:\n");
                while ((s = stdInput.readLine()) != null)
                {
                    System.out.println(s);
                }
                System.out.println("Here is the standard error of the command:\n");
                while ((s = stdError.readLine()) != null)
                {
                    System.out.println(s);
                }
            }
            catch(IOException e)
            {
                System.out.println("exception happened - heres what i know:");
                e.printStackTrace();
            }
            try
            {
                Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "apt-get upgrade -y"});
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                System.out.println("Here is the standard output of the command:\n");
                while ((s = stdInput.readLine()) != null)
                {
                    System.out.println(s);
                }
                System.out.println("Here is the standard error of the command:\n");
                while ((s = stdError.readLine()) != null)
                {
                    System.out.println(s);
                }
            }
            catch(IOException e)
            {
                System.out.println("exception happened - heres what i know:");
                e.printStackTrace();
            }
            System.out.println("Packages updated");
        }

        void installPackages()
        {
            System.out.println("Installing packages...");
            String s = null;
            try
            {
                Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "DEBIAN_FRONTEND=noninteractive apt-get -y install samba smbclient winbind krb5-user"});
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                System.out.println("Here is the standard output of the command:\n");
                while ((s = stdInput.readLine()) != null)
                {
                    System.out.println(s);
                }
                System.out.println("Here is the standard error of the command:\n");
                while ((s = stdError.readLine()) != null)
                {
                    System.out.println(s);
                }
            }
            catch(IOException e)
            {
                System.out.println("exception happened - heres what i know:");
                e.printStackTrace();
            }
            System.out.println("Packages Installed");
        }

        void sambaConfiguration()
        {
            System.out.println("Configuring Samba...");

            String s = null;
            try
            {
                Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "rm /etc/samba/smb.conf"});
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                System.out.println("Here is the standard output of the command:\n");
                while ((s = stdInput.readLine()) != null)
                {
                    System.out.println(s);
                }
                System.out.println("Here is the standard error of the command:\n");
                while ((s = stdError.readLine()) != null)
                {
                    System.out.println(s);
                }
            }
            catch(IOException e)
            {
                System.out.println("exception happened - heres what i know:");
                e.printStackTrace();
            }

            String password = JOptionPane.showInputDialog(null, "Input new administrator passwprd:" +
                    "\n\nWarning! 8 letters length min, uppercase, lowercase, digits");

            String[] tmp = domainname.split("\\.");
            String shortName = tmp[0].toUpperCase();


            System.out.println("samba-tool domain provision --realm=" + domainname + " " +
                    "--domain=" + shortName + " --adminpass=\"" + password + "\" --server-role=dc " +
                    "--dns-backend=SAMBA_INTERNAL");

            try
            {
                Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "samba-tool domain provision --realm=" + domainname + " " +
                        "--domain=" + shortName + " --adminpass=\"" + password + "\" --server-role=dc " +
                        "--dns-backend=SAMBA_INTERNAL"});
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                System.out.println("Here is the standard output of the command:\n");
                while ((s = stdInput.readLine()) != null)
                {
                    System.out.println(s);
                }
                System.out.println("Here is the standard error of the command:\n");
                while ((s = stdError.readLine()) != null)
                {
                    System.out.println(s);
                }
            }
            catch(IOException e)
            {
                System.out.println("exception happened - heres what i know:");
                e.printStackTrace();
            }

            System.out.println("Samba configured");
        }

        void kerberosConfiguration()
        {
            System.out.println("Configuring Kerberos5...");
            String s = null;
            try
            {
                Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "cp /var/lib/samba/private/krb5.conf /etc/krb5.conf"});
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                System.out.println("Here is the standard output of the command:\n");
                while ((s = stdInput.readLine()) != null)
                {
                    System.out.println(s);
                }
                System.out.println("Here is the standard error of the command:\n");
                while ((s = stdError.readLine()) != null)
                {
                    System.out.println(s);
                }
            }
            catch(IOException e)
            {
                System.out.println("exception happened - heres what i know:");
                e.printStackTrace();
            }

            ArrayList<String> kerberosConfig = new ArrayList<String>();
            try {
                kerberosConfig = f.readFileToArrayList("/etc/krb5.conf");
            } catch (IOException e) {
                e.printStackTrace();
            }
            kerberosConfig.add("[realms]");
            kerberosConfig.add("\t" + domainname.toUpperCase() + " = {");
            kerberosConfig.add("\t\t kdc = " + fullName);
            kerberosConfig.add("\t\t admin_server = " + fullName);
            kerberosConfig.add("\t}");
            try {
                f.writeArrayListToFile(kerberosConfig, "/etc/krb5.conf");
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("Kerberos5 configured");
        }

        void resolvConfiguration()
        {
            System.out.println("Configuring resolv.conf...");
            ArrayList<String> resolvConf = new ArrayList<String>();
            try {
                resolvConf = f.readFileToArrayList("/etc/resolv.conf");
            } catch (IOException e) {
                e.printStackTrace();
            }

            for(int i = 0; i < resolvConf.size(); i++) System.out.println(resolvConf.get(i));

            boolean conf = false;
            for(int i = 0; i < resolvConf.size(); i++)
            {
                String[] line = resolvConf.get(i).split(" ");
                if(line[0].equals("domain"))
                {
                    resolvConf.remove(i);
                    resolvConf.add(i, "domain " + domainname.toUpperCase());
                    conf = true;
                }
            }
            if(!conf)
            {
                resolvConf.add("domain " + domainname.toUpperCase());
            }

            try {
                f.writeArrayListToFile(resolvConf,"/etc/resolv.conf");
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("resolv.conf configured");
        }

        void generatingDirectory()
        {
            System.out.println("Generating directory...");
            String s = null;
            try
            {
                Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "mkdir -m 770 /Users"});
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                System.out.println("Here is the standard output of the command:\n");
                while ((s = stdInput.readLine()) != null)
                {
                    System.out.println(s);
                }
                System.out.println("Here is the standard error of the command:\n");
                while ((s = stdError.readLine()) != null)
                {
                    System.out.println(s);
                }
            }
            catch(IOException e)
            {
                System.out.println("exception happened - heres what i know:");
                e.printStackTrace();
            }

            try
            {
                Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "chmod g+s /Users"});
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                System.out.println("Here is the standard output of the command:\n");
                while ((s = stdInput.readLine()) != null)
                {
                    System.out.println(s);
                }
                System.out.println("Here is the standard error of the command:\n");
                while ((s = stdError.readLine()) != null)
                {
                    System.out.println(s);
                }
            }
            catch(IOException e)
            {
                System.out.println("exception happened - heres what i know:");
                e.printStackTrace();
            }

            try
            {
                Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "chown root:users /Users"});
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                System.out.println("Here is the standard output of the command:\n");
                while ((s = stdInput.readLine()) != null)
                {
                    System.out.println(s);
                }
                System.out.println("Here is the standard error of the command:\n");
                while ((s = stdError.readLine()) != null)
                {
                    System.out.println(s);
                }
            }
            catch(IOException e)
            {
                System.out.println("exception happened - heres what i know:");
                e.printStackTrace();
            }
            System.out.println("Directory generated");
        }

        void sambaPostConfig()
        {
            System.out.println("Samba post-configuring...");
            ArrayList<String> smbConf = new ArrayList<>();
            try {
                smbConf = f.readFileToArrayList("/etc/samba/smb.conf");
            } catch (IOException e) {
                e.printStackTrace();
            }

            for(int i = 0; i < smbConf.size(); i++)
            {
                String line = smbConf.get(i);
                line = line.replaceAll("\t", "");
                line = line.replaceAll(" ", "");
                String[] lineSeparate = line.split("=");
                if(lineSeparate[0].equals("dnsforwarder"))
                {
                    smbConf.remove(i);
                    smbConf.add(i, "\tdns forwarder = 8.8.8.8");
                }
            }

            smbConf.add("");
            smbConf.add("[Users]");
            smbConf.add("\tdirectory_mode: parameter = 0700");
            smbConf.add("\tread only = no");
            smbConf.add("\tpath = /Users");
            smbConf.add("\tcsc policy = documents");

            try {
                f.writeArrayListToFile(smbConf, "/etc/samba/smb.conf");
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("Samba post-configured");
        }

        public void actionPerformed(ActionEvent e)
        {
            labelIpAddress.setForeground(Color.black);
            labelNetmask.setForeground(Color.black);
            labelGateway.setForeground(Color.black);
            labelComputersName.setForeground(Color.black);
            labelDomainname.setForeground(Color.black);

            labelBadIp.setForeground(Color.red);
            labelBadNetmask.setForeground(Color.red);
            labelBadGateway.setForeground(Color.red);
            labelBadComputersName.setForeground(Color.red);
            labelBadDomainname.setForeground(Color.red);

            boolean error = false;
            InetAddress ip;

            ipAddress = jTextFieldIpAddress.getText();
            if(!f.validIP(ipAddress))
            {
                labelBadIp.setVisible(true);
                labelIpAddress.setForeground(Color.red);
                error = true;
            }

            netmask = jTextFieldNetmask.getText();
            if(!f.validIP(netmask))
            {
                labelBadNetmask.setVisible(true);
                labelNetmask.setForeground(Color.red);
                error = true;
            }

            gateway = jTextFieldGateway.getText();
            if(!f.validIP(gateway))
            {
                labelBadGateway.setVisible(true);
                labelGateway.setForeground(Color.red);
                error = true;
            }

            domainname = jTextFieldDomainname.getText();
            String[] testDomainName = domainname.split("\\.");
            if(testDomainName.length <2 )
            {
                labelBadDomainname.setVisible(true);
                labelDomainname.setForeground(Color.red);
                error = true;
            }

            computersName = jtextFieldComputersName.getText();
            if(computersName.isEmpty())
            {
                labelBadComputersName.setVisible(true);
                labelBadComputersName.setText("Bad computer's name");
                labelComputersName.setForeground(Color.red);
                error = true;
            }
            else
            {
                String[] testComputersName = computersName.split("\\.");
                if(testComputersName.length > 1)
                {
                    labelBadComputersName.setText("Can't include dots");
                    labelComputersName.setForeground(Color.red);
                    labelBadComputersName.setVisible(true);
                    error = true;
                }
            }
            fullName = computersName + "." + domainname;

            if(!error)
            {
                configureInterface();
                setHosts();
                setHostname();
                updatePackages();
                installPackages();
                sambaConfiguration();
                kerberosConfiguration();
                resolvConfiguration();
                generatingDirectory();
                sambaPostConfig();

                System.out.println("Configured!");
            }

        }
    }
    DocumentListener buildFullName = new DocumentListener()
    {

        @Override
        public void insertUpdate(DocumentEvent documentEvent)
        {
            jTextFieldFullName.setText(jtextFieldComputersName.getText() + "." + jTextFieldDomainname.getText());
        }

        @Override
        public void removeUpdate(DocumentEvent documentEvent)
        {
            jTextFieldFullName.setText(jtextFieldComputersName.getText() + "." + jTextFieldDomainname.getText());
        }

        @Override
        public void changedUpdate(DocumentEvent documentEvent)
        {
            jTextFieldFullName.setText(jtextFieldComputersName.getText() + "." + jTextFieldDomainname.getText());
        }
    };

    boolean checkRoot()
    {
        String s = null;
        try
        {
            Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "id -u"});
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            s = stdInput.readLine();
        }
        catch(IOException e)
        {
            System.out.println("exception happened - heres what i know:");
            e.printStackTrace();
        }
        if(s.equals("0")) return true;
        else return false;
    }

    void getInterfacesData()
    {
        //Считываем файл в память
        try
        {
            interfacesFile = f.readFileToArrayList("/etc/network/interfaces");
        }
        catch(IOException e)
        {

        }

        //Переменные для хранения данных о интерфейсе
        String interfaceName = "";
        int startPos = 0;
        int endPos = 0;

        //Поиск интерфейсов в файле
        for(int j = 0; j < 4; j++)
        {
            interfaceName = "";
            startPos = 0;
            endPos = 0;

            for(int i = 0; i< interfacesFile.size(); i++)
            {
                if(interfacesFile.get(i).equals("auto eth" + j))
                {
                    interfaceName = "eth" + j;
                    startPos = i;
                }
                if(interfacesFile.get(i).equals("auto eth" + (j+1)))
                {
                    endPos = i-1;
                }
            }

            if((startPos != 0)&&(endPos ==0)) endPos = interfacesFile.size();

            //Если интерфейс находит то добавляет его в список интерфейсов
            if(interfaceName != "")
            {
                interfaces.add(new InterfaceData(interfaceName, startPos, endPos));
            }
        }

        if(interfaces.isEmpty())
        {
            interfaces.add(new InterfaceData("eth0", interfacesFile.size()-1, interfacesFile.size()-1));
        }
    }

    void getComputersName()
    {
        ArrayList<String> hostName = new ArrayList<>();
        try {
            hostName = f.readFileToArrayList("/etc/hostname");
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] hostNameSplit = hostName.get(0).split("\\.");
        jtextFieldComputersName.setText(hostNameSplit[0]);
    }

    void getIpAdresses()
    {
        String ip = null;
        try
        {
            Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "hostname -I"});
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            ip = stdInput.readLine();
            ip = ip.replaceAll(" ", "");
            jTextFieldIpAddress.setText(ip);
        }
        catch(IOException e)
        {
            System.out.println("exception happened - heres what i know:");
            e.printStackTrace();
        }

        try
        {
            Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "ip route show 0.0.0.0/0 dev eth0"});
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            ip = stdInput.readLine();
            String[] tmp = ip.split(" ");
            jTextFieldGateway.setText(tmp[2]);
        }
        catch(IOException e)
        {
            System.out.println("exception happened - heres what i know:");
            e.printStackTrace();
        }
        jTextFieldNetmask.setText("255.255.255.0");
    }

    public Window()
    {
        super("Samba configurator");

        boolean root = checkRoot();
        if(!root)
        {
            JOptionPane.showMessageDialog(null, "No root permissions. Run application via terminal with sudo or using superuser");
            System.exit(1);
        }

        this.setResizable(false);
        setSize(800,480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(null);

        getInterfacesData();
        getComputersName();
        getIpAdresses();

        labelEthernetAdapter.setBounds(10, 10, 130, 20);
        this.add(labelEthernetAdapter);

        labelIpAddress.setBounds(10, 50, 130, 20);
        this.add(labelIpAddress);

        labelNetmask.setBounds(10, 90, 130, 20);
        this.add(labelNetmask);

        labelGateway.setBounds(10, 130, 130, 20);
        this.add(labelGateway);

        labelComputersName.setBounds(10, 170, 130, 20);
        this.add(labelComputersName);

        labelDomainname.setBounds(10, 210, 130, 20);
        this.add(labelDomainname);

        labelFullName.setBounds(10, 250, 130, 20);
        this.add(labelFullName);

        ArrayList<String> interfacesNames = new ArrayList<String>();
        for(int i = 0; i < interfaces.size(); i++) interfacesNames.add(interfaces.get(i).name);
        comboBoxEth = new JComboBox(interfacesNames.toArray());
        comboBoxEth.setBounds(140,10, 160, 20);
        this.add(comboBoxEth);

        jTextFieldIpAddress.setBounds(140, 50, 160, 20);
        this.add(jTextFieldIpAddress);
        labelBadIp.setBounds(140, 70, 160, 20);
        this.add(labelBadIp);

        jTextFieldNetmask.setBounds(140, 90, 160, 20);
        this.add(jTextFieldNetmask);
        labelBadNetmask.setBounds(140, 110, 160, 20);
        this.add(labelBadNetmask);

        jTextFieldGateway.setBounds(140, 130,160,20);
        this.add(jTextFieldGateway);
        labelBadGateway.setBounds(140, 150, 160, 20);
        this.add(labelBadGateway);

        jtextFieldComputersName.setBounds(140, 170, 160, 20);
        jtextFieldComputersName.getDocument().addDocumentListener(buildFullName);
        this.add(jtextFieldComputersName);
        labelBadComputersName.setBounds(140, 190, 160, 20);
        this.add(labelBadComputersName);

        jTextFieldDomainname.setBounds(140, 210, 160, 20);
        jTextFieldDomainname.getDocument().addDocumentListener(buildFullName);
        this.add(jTextFieldDomainname);
        labelBadDomainname.setBounds(140, 230, 160, 20);
        this.add(labelBadDomainname);

        labelBadIp.setVisible(false);
        labelBadNetmask.setVisible(false);
        labelBadGateway.setVisible(false);
        labelBadComputersName.setVisible(false);
        labelBadDomainname.setVisible(false);

        jTextFieldFullName.setBounds(140, 250, 160, 20);
        jTextFieldFullName.setEditable(false);
        this.add(jTextFieldFullName);

        buttonConfigureEthernet.setBounds(100, 290, 120, 20);
        ActionListener configureEthernetAL = new ConfigureEthernet();
        buttonConfigureEthernet.addActionListener(configureEthernetAL);
        this.add(buttonConfigureEthernet);



        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - this.getWidth())/2);
        int y = (int) ((dimension.getHeight() - this.getHeight())/2);
        this.setLocation(x, y);
        this.setVisible(true);

        JOptionPane.showMessageDialog(null, "This application works only with root permissions" +
                "\nBe sure that the program was started via terminal");
    }
}

public class SambaConfigurator
{
    public static void main(String[] args)
    {
        new Window();
    }
}
