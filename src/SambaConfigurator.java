import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

class Window extends JFrame
{
    //Переменные для хранения параметров настройки самбы и статичного сетевого подключения
    private String ipAddress = "";  //Айпи адресс
    private String netmask = "";    //Маска подсети
    private String network = "";    //Адрес сети
    private String broadcast = "";  //Широковещательный адрес
    private String gateway = "";    //Шлюз
    private String computersName = "";//Имя машины
    private String domainname = ""; //Имя домена
    private String fullName = "";   //Полное имя

    //Подписи к форме
    private JLabel labelIconInstall = new JLabel();
    private JLabel labelStatus = new JLabel("Working...");

    private JLabel labelEthernetAdapter = new JLabel("Ethernet adapter");
    private JLabel labelIpAddress = new JLabel("IP address");
    private JLabel labelNetmask = new JLabel("Netmask");
    private JLabel labelGateway = new JLabel("Gateway");
    private JLabel labelComputersName = new JLabel("Computer's name");
    private JLabel labelDomainname = new JLabel("Domain name");
    private JLabel labelFullName = new JLabel("Full name");

    private JLabel labelBadIp = new JLabel("Bad ip");
    private JLabel labelBadNetmask = new JLabel("Bad netmask");
    private JLabel labelBadGateway = new JLabel("Bad gateway");
    private JLabel labelBadComputersName = new JLabel("Bad computer's name");
    private JLabel labelBadDomainname = new JLabel("Bad domain's name");

    private JComboBox comboBoxEth = new JComboBox();    //Выпадающий список, в котором на выбор сетевой интерфейс

    //Текстовые поля для ввода данных конфигурации
    private JTextField jTextFieldIpAddress = new JTextField();
    private JTextField jTextFieldNetmask = new JTextField();
    private JTextField jTextFieldGateway = new JTextField();
    private JTextField jtextFieldComputersName = new JTextField();
    private JTextField jTextFieldDomainname = new JTextField();
    private JTextField jTextFieldFullName = new JTextField();

    //Поле вывода информации о процессе настройки и терминал
    private JTextArea jTextAreaAppOutput = new JTextArea();
    private DefaultCaret caret = (DefaultCaret)jTextAreaAppOutput.getCaret();
    private JScrollPane scrollTerminal = new JScrollPane();
    private JTextField jTextFieldCommandLine = new JTextField();
    private JButton buttonRunCommand = new JButton("Execute");

    private JButton buttonConfigureEthernet = new JButton("Configure");
    private JButton buttonUserManager = new JButton("User accounts");

    private JButton buttonSambaInfo = new JButton("Get info about AD DC in the network");

    private File configFilePath = new File("/etc/sade/sade.conf");// Конфигурационный файл приложения
    private ArrayList<String> configFile = new ArrayList<String>();

    //Класс с функциями
    private Functions f = new Functions();

    //Список строк файла и список настроенных в нём адаптеров
    private ArrayList<String> interfacesFile = new ArrayList<String>();
    private ArrayList<InterfaceData> interfaces = new ArrayList<InterfaceData>();

    JLabel labelBackGroundImage = new JLabel();
    ImageIcon icon;

    class ConfigureEthernet implements ActionListener   // Обработчик нажатия на кнопку настройки сети и самбы
    {
        void configureInterface()   // Функция конфигурации интерфейса
        {
            jTextAreaAppOutput.append("Configuring interface...\n");
            //Отключает кнопку
            buttonConfigureEthernet.setEnabled(false);
            buttonRunCommand.setEnabled(false);

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

            // Генерирует широковещательный адрес
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

            // Генерируем параметры в файле
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

            jTextAreaAppOutput.append("Interface configured\n");
        }

        void setHosts() // Настраиваем файл хостс
        {
            jTextAreaAppOutput.append("Configuring /etc/hosts ...\n");

            //Считываем файл
            ArrayList<String> hosts = new ArrayList<>();
            try
            {
                hosts = f.readFileToArrayList("/etc/hosts");
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            //Ищем стандартные параметры операционной системф
            String[] testLine;
            boolean configured = false; // Указывает, был ли сконфигурирован файл хостс
            for(int i = 0; i < hosts.size(); i++) // Переберает каждую линию файла и ищет соответствие
            {
                if(configured) break; // Если настроено - прервать
                testLine = hosts.get(i).split("\t");
                if(testLine.length == 2)
                {
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
                    if((testLine[0].equals("127.0.0.1"))||(testLine[1].equals("localhost")))
                    {
                        hosts.add(i+1, ipAddress + "\t" + fullName);
                        configured = true;
                        break;
                    }
                }
            }

            // Если не находит соответствие то вводит значение по умолчению
            if(!configured)
            {
                hosts.add(0, ipAddress + "\t" + fullName);
            }

            // Записывает файл
            try
            {
                f.writeArrayListToFile(hosts, "/etc/hosts");
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            jTextAreaAppOutput.append("Configured /etc/hosts\n");
        }

        void setHostname()  // Устанавливает имя машины
        {
            jTextAreaAppOutput.append("Setting hostname...\n");
            ArrayList<String> hostname = new ArrayList<>();
            hostname.add(fullName);

            try {
                f.writeArrayListToFile(hostname, "/etc/hostname");
            } catch (IOException e) {
                e.printStackTrace();
            }
            jTextAreaAppOutput.append("Hostname set\n");
        }

        void updatePackages() // Обновление списка пакетов и установка обновлений пакетов
        {
            jTextAreaAppOutput.append("Updating packages...\n");
            String s = null;
            try
            {
                Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "apt-get update"});    // Выполнение команды в терминале
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));// Слушатель вывода терминала
                BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));// Слушатель ошибок терминала
                while ((s = stdInput.readLine()) != null)
                {
                    jTextAreaAppOutput.append(s + "\n");    // Вывод в поле терминала результата выполнения команды
                }
                while ((s = stdError.readLine()) != null)
                {
                    jTextAreaAppOutput.append(s + "\n");    // Вывод ошибок
                }
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }

            try
            {
                Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "apt-get upgrade -y"});
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                while ((s = stdInput.readLine()) != null)
                {
                    jTextAreaAppOutput.append(s + "\n");
                }
                while ((s = stdError.readLine()) != null)
                {
                    jTextAreaAppOutput.append(s + "\n");
                }
            }
            catch(IOException e)
            {

                e.printStackTrace();
            }
            jTextAreaAppOutput.append("Packages updated\n");
        }

        void installPackages() // Выполнение установки необходимых пакетов
        {
            jTextAreaAppOutput.append("Installing packages...\n");
            String s = null;
            try
            {
                Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "DEBIAN_FRONTEND=noninteractive apt-get -y install samba smbclient winbind krb5-user"});
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                while ((s = stdInput.readLine()) != null)
                {
                    jTextAreaAppOutput.append(s + "\n");
                }
                while ((s = stdError.readLine()) != null)
                {
                    jTextAreaAppOutput.append(s + "\n");
                }
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
            jTextAreaAppOutput.append("Packages Installed\n");
        }

        void sambaConfiguration() // Настройка самбы
        {
            jTextAreaAppOutput.append("Configuring Samba...\n");

            // Удаляем конфигурационный файл самбы
            String s = null;
            try
            {
                Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "rm /etc/samba/smb.conf"});
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                while ((s = stdInput.readLine()) != null)
                {
                    jTextAreaAppOutput.append(s + "\n");
                }
                while ((s = stdError.readLine()) != null)
                {
                    jTextAreaAppOutput.append(s + "\n");
                }
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }

            // Запрашиваем новый пароль для учётной записи администратора
            String password = "";
            boolean correct = false;
            while(!correct)
            {
                password = JOptionPane.showInputDialog(null, "Input new administrator password:" +
                        "\n\nWarning! 8 letters length min, uppercase, lowercase, digits");
                correct = f.checkPassword(password);
                if(!correct) JOptionPane.showMessageDialog(null, "Password isn't strong enough");
            }

            //Разделяем доменное имя на полное и короткое
            String[] tmp = domainname.split("\\.");
            String shortName = tmp[0].toUpperCase();

            // Выводим выполняемую команду
            jTextAreaAppOutput.append("samba-tool domain provision --realm=" + domainname + " " +
                    "--domain=" + shortName + " --adminpass=\"" + password + "\" --server-role=dc " +
                    "--dns-backend=SAMBA_INTERNAL\n");

            // Выполняем конфигурация самбы как контроллер домена
            try
            {
                Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "samba-tool domain provision --realm=" + domainname + " " +
                        "--domain=" + shortName + " --adminpass=\"" + password + "\" --server-role=dc " +
                        "--dns-backend=SAMBA_INTERNAL"});
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                while ((s = stdInput.readLine()) != null)
                {
                    jTextAreaAppOutput.append(s + "\n");
                }
                while ((s = stdError.readLine()) != null)
                {
                    jTextAreaAppOutput.append(s + "\n");
                }
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }

            jTextAreaAppOutput.append("Samba configured\n");
        }

        void kerberosConfiguration() // Настраиваем конфигурационный файл Керберов
        {
            jTextAreaAppOutput.append("Configuring Kerberos5...\n");

            //Копируем файл керберос что получили после конфигурации самбы
            String s = null;
            try
            {
                Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "cp /var/lib/samba/private/krb5.conf /etc/krb5.conf"});
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                while ((s = stdInput.readLine()) != null)
                {
                    jTextAreaAppOutput.append(s + "\n");
                }
                while ((s = stdError.readLine()) != null)
                {
                    jTextAreaAppOutput.append(s + "\n");
                }
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }

            // Редактируем файл
            ArrayList<String> kerberosConfig = new ArrayList<String>();
            try {
                kerberosConfig = f.readFileToArrayList("/etc/krb5.conf");
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Добавляем в файл данные о домене
            kerberosConfig.add("[realms]");
            kerberosConfig.add("\t" + domainname.toUpperCase() + " = {");
            kerberosConfig.add("\t\t kdc = " + fullName);
            kerberosConfig.add("\t\t admin_server = " + fullName);
            kerberosConfig.add("\t}");
            // Записываем файл на диск
            try {
                f.writeArrayListToFile(kerberosConfig, "/etc/krb5.conf");
            } catch (IOException e) {
                e.printStackTrace();
            }

            jTextAreaAppOutput.append("Kerberos5 configured\n");
        }

        void resolvConfiguration()  // Вводим доменное имя в зону поиска
        {
            jTextAreaAppOutput.append("Configuring resolv.conf...\n");
            // Считываем файл
            ArrayList<String> resolvConf = new ArrayList<String>();
            try {
                resolvConf = f.readFileToArrayList("/etc/resolv.conf");
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Проверяем был ли введён прежде домен
            boolean conf = false;
            for(int i = 0; i < resolvConf.size(); i++)
            {
                String[] line = resolvConf.get(i).split(" ");
                if(line[0].equals("domain")) // Если был то заменяем
                {
                    resolvConf.remove(i);
                    resolvConf.add(i, "domain " + domainname.toUpperCase());
                    conf = true;
                }
            }
            if(!conf) // Если нет то дописываем
            {
                resolvConf.add("domain " + domainname.toUpperCase());
            }

            // Сохраняем файл
            try {
                f.writeArrayListToFile(resolvConf,"/etc/resolv.conf");
            } catch (IOException e) {
                e.printStackTrace();
            }
            jTextAreaAppOutput.append("resolv.conf configured\n");
        }

        void generatingDirectory()  // Создаём папку пользователей на машине
        {
            jTextAreaAppOutput.append("Generating directory...\n");
            String s = null;
            try
            {
                Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "mkdir -m 770 /Users"}); // Создаём папку с правам 770
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                while ((s = stdInput.readLine()) != null)
                {
                    jTextAreaAppOutput.append(s + "\n");
                }
                while ((s = stdError.readLine()) != null)
                {
                    jTextAreaAppOutput.append(s + "\n");
                }
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }

            try
            {
                Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "chmod g+s /Users"}); // Указываем доступ к папке
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                while ((s = stdInput.readLine()) != null)
                {
                    jTextAreaAppOutput.append(s + "\n");
                }
                while ((s = stdError.readLine()) != null)
                {
                    jTextAreaAppOutput.append(s + "\n");
                }
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }

            try
            {
                Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "chown root:users /Users"}); // Указываем владельца папки
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                while ((s = stdInput.readLine()) != null)
                {
                    jTextAreaAppOutput.append(s + "\n");
                }
                while ((s = stdError.readLine()) != null)
                {
                    jTextAreaAppOutput.append(s + "\n");
                }
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
            jTextAreaAppOutput.append("Directory generated\n");
        }

        void sambaPostConfig() // Пост-конфигурация самбы
        {
            jTextAreaAppOutput.append("Samba post-configuring...\n");
            // В файле изменяются настройки перенаправления ДНС и указывается папка пользователей
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

            jTextAreaAppOutput.append("Samba post-configured\n");
        }

        Runnable configureThread = new Runnable() // Отдельный поток для конфигурации контроллера домена
        {
            @Override
            public void run()
            {
                // Отключение кнопок
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

                //Проверка введённых параметров
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
                    if(!domainname.equals(""))
                    {
                        if(testDomainName.length == 1)
                        {
                            domainname = testDomainName[0] + "." + testDomainName[0];
                            jTextFieldDomainname.setText(domainname);
                        }
                    }
                    else
                    {
                        labelBadDomainname.setVisible(true);
                        labelDomainname.setForeground(Color.red);
                        error = true;
                    }
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

                if(!error) // Есди все парамерты верны выполнить настройку
                {
                    makeElementsInvisible();
                    showInstallation();

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

                    done();

                    jTextAreaAppOutput.append("\nConfigured!");
                    jTextAreaAppOutput.append("\nPlease, restart your computer");
                    JOptionPane.showMessageDialog(null, "Installation completed" +
                            "\nPlease restart your computer");
                    buttonRunCommand.setEnabled(true);
                }
            }
        };

        public void actionPerformed(ActionEvent e)
        {
            new Thread(configureThread).start();
        }
    }

    class RunCommand implements ActionListener // Выполняет введённую команду в терминале
    {
        class RunCommandThread implements Runnable
        {
            public void run()
            {
                buttonConfigureEthernet.setEnabled(false);
                buttonRunCommand.setEnabled(false);

                String command = jTextFieldCommandLine.getText();

                if(!command.equals(""))
                {
                    jTextAreaAppOutput.append("\n--Executing \"" + command + "\"--\n");
                    String s = null;
                    try
                    {
                        Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", command});
                        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                        BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                        while ((s = stdInput.readLine()) != null)
                        {
                            jTextAreaAppOutput.append(s + "\n");
                        }
                        while ((s = stdError.readLine()) != null)
                        {
                            jTextAreaAppOutput.append(s + "\n");
                        }
                    }
                    catch(IOException e)
                    {
                        e.printStackTrace();
                    }
                }

                buttonConfigureEthernet.setEnabled(true);
                buttonRunCommand.setEnabled(true);
            }
        }

        public void actionPerformed(ActionEvent e)
        {
            new RunCommandThread().run();
        }
    }

    class ButtonUsers implements ActionListener // Открывает окно управления пользователями
    {
        public void actionPerformed(ActionEvent e)
        {
            new UserManager();
        }
    }

    class ButtonGetInfo implements ActionListener // Получить информацию о контроллере домена в сети
    {
        public void actionPerformed(ActionEvent e)
        {
            String ip = "";
            boolean isIp;
            do
            {
                ip = jTextFieldIpAddress.getText();
                if(!f.validIP(ip)) ip = "";
                ip = (String)JOptionPane.showInputDialog(null, "Input ip address to get info:", "ip", JOptionPane.PLAIN_MESSAGE, null, null, ip);
                isIp = f.validIP(ip);
                if(!isIp) JOptionPane.showMessageDialog(null, "Invalid IP address");
            }
            while(!isIp);

            String answer = "";
            String s = null;
            try
            {
                Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "samba-tool domain info " + ip});
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                while ((s = stdInput.readLine()) != null)
                {
                    answer = answer + s + "\n";
                }
                while ((s = stdError.readLine()) != null)
                {
                    answer = answer + s + "\n";
                }
            }
            catch(IOException ex)
            {
                ex.printStackTrace();
            }

            JOptionPane.showMessageDialog(null, answer);
        }
    }

    class IpAddressPressed extends MouseAdapter
    {
        @Override
        public void mouseClicked(MouseEvent e)
        {
            JOptionPane.showMessageDialog(null, "You can't change IP address from here\nIf you want to change IP address please change it in another way");
        }
    }

    DocumentListener buildFullName = new DocumentListener() // Слушатель ввода текста в поля имя компьютера и имя домена
    {

        @Override
        public void insertUpdate(DocumentEvent documentEvent)
        {
            String[] tmp = jTextFieldDomainname.getText().split("\\.");
            if(tmp.length == 1)
            {
                jTextFieldFullName.setText(jtextFieldComputersName.getText() + "." + jTextFieldDomainname.getText() + "." + jTextFieldDomainname.getText());
            }
            else
            {
                jTextFieldFullName.setText(jtextFieldComputersName.getText() + "." + jTextFieldDomainname.getText());
            }
        }

        @Override
        public void removeUpdate(DocumentEvent documentEvent)
        {
            String[] tmp = jTextFieldDomainname.getText().split("\\.");
            if(tmp.length == 1)
            {
                jTextFieldFullName.setText(jtextFieldComputersName.getText() + "." + jTextFieldDomainname.getText() + "." + jTextFieldDomainname.getText());
            }
            else
            {
                jTextFieldFullName.setText(jtextFieldComputersName.getText() + "." + jTextFieldDomainname.getText());
            }
        }

        @Override
        public void changedUpdate(DocumentEvent documentEvent)
        {
            String[] tmp = jTextFieldDomainname.getText().split("\\.");
            if(tmp.length == 1)
            {
                jTextFieldFullName.setText(jtextFieldComputersName.getText() + "." + jTextFieldDomainname.getText() + "." + jTextFieldDomainname.getText());
            }
            else
            {
                jTextFieldFullName.setText(jtextFieldComputersName.getText() + "." + jTextFieldDomainname.getText());
            }
        }
    };

    boolean checkRoot() // Проверка рут доступа
    {
        // Получает ид номер пользователя. Если 0 - запущено под рутом
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

    void getInterfacesData() // Получение данных о уже настроенных интерфейсах
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

    void getComputersName() // Получить имя компьютера
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

    void getIpAdresses() // Получить айпи адрес компьютера
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

    void makeElementsInvisible() // Отключить элементы управления
    {
        labelEthernetAdapter.setVisible(false);
        labelIpAddress.setVisible(false);
        labelNetmask.setVisible(false);
        labelGateway.setVisible(false);
        labelComputersName.setVisible(false);
        labelDomainname.setVisible(false);
        labelFullName.setVisible(false);

        comboBoxEth.setVisible(false);
        jTextFieldIpAddress.setVisible(false);
        jTextFieldNetmask.setVisible(false);
        jTextFieldGateway.setVisible(false);
        jtextFieldComputersName.setVisible(false);
        jTextFieldDomainname.setVisible(false);
        jTextFieldFullName.setVisible(false);

        buttonUserManager.setEnabled(false);
        buttonSambaInfo.setEnabled(false);
    }

    void showInstallation() // Отобразить процесс настройки
    {
        labelIconInstall.setVisible(true);
        labelStatus.setText("Working...");
        labelStatus.setVisible(true);
    }

    void done() // Проверка установленной версии самбы и запись результата в конфигурационный файл
    {
        labelIconInstall.setVisible(false);

        String version = null;
        try
        {
            Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "smbclient -V"});
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            version = stdInput.readLine();
            configFile.set(0, "SambaConfigured=yes");
            configFile.set(1, "SambaVersion=" + version);
            f.writeArrayListToFile(configFile, "/etc/sade/sade.conf");
        }
        catch(IOException e)
        {
            System.out.println("exception happened - heres what i know:");
            e.printStackTrace();
        }
        buttonSambaInfo.setEnabled(true);

        labelStatus.setText("Installed");
    }

    public Window()
    {
        super("SADE");

        icon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(Window.class.getResource("icon.png")));

        // Проверка рут прав
        boolean root = checkRoot();
        if(!root)
        {
            JOptionPane.showMessageDialog(null, "No root permissions. Run application via terminal with sudo or using superuser");
            System.exit(1);
        }

        this.setResizable(false);
        setSize(690,385);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(null);

        getInterfacesData();
        getComputersName();
        getIpAdresses();

        labelStatus.setBounds(120, 180, 100, 20);
        labelStatus.setVisible(false);
        this.add(labelStatus);

        ImageIcon gifInstall = new ImageIcon(Toolkit.getDefaultToolkit().createImage(Window.class.getResource("loading.gif")));
        labelIconInstall.setIcon(gifInstall);
        labelIconInstall.setBounds(35, 0, 230, 200);
        labelIconInstall.setVisible(false);
        this.add(labelIconInstall);

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
        jTextFieldIpAddress.setEditable(false);
        MouseAdapter ipAddress = new IpAddressPressed();
        jTextFieldIpAddress.addMouseListener(ipAddress);
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
        caret.setUpdatePolicy(DefaultCaret.OUT_BOTTOM);
        jTextAreaAppOutput.setBackground(Color.black);
        jTextAreaAppOutput.setForeground(Color.white);
        jTextAreaAppOutput.setFont(jTextAreaAppOutput.getFont().deriveFont(10f));
        scrollTerminal = new JScrollPane();
        scrollTerminal.add(jTextAreaAppOutput);
        scrollTerminal.setViewportView(jTextAreaAppOutput);
        scrollTerminal.setBounds(320, 10, 360, 300);
        this.add(scrollTerminal);
        jTextAreaAppOutput.append("Ready\n");

        jTextFieldCommandLine.setBounds(320, 320, 260, 20);
        this.add(jTextFieldCommandLine);
        buttonRunCommand.setBounds(590, 320, 90, 20);
        ActionListener runCommand = new RunCommand();
        buttonRunCommand.addActionListener(runCommand);
        this.add(buttonRunCommand);

        buttonConfigureEthernet.setBounds(10, 290, 140, 20);
        ActionListener configureEthernetAL = new ConfigureEthernet();
        buttonConfigureEthernet.addActionListener(configureEthernetAL);
        this.add(buttonConfigureEthernet);
        buttonUserManager.setBounds(160, 290, 140, 20);
        ActionListener buttonUsers = new ButtonUsers();
        buttonUserManager.addActionListener(buttonUsers);
        buttonUserManager.setEnabled(false);
        this.add(buttonUserManager);

        buttonSambaInfo.setBounds(10, 320, 290, 20);
        ActionListener buttonGetInfo = new ButtonGetInfo();
        buttonSambaInfo.addActionListener(buttonGetInfo);
        this.add(buttonSambaInfo);

        labelBackGroundImage.setBounds(-20, 5, 690, 375);
        ImageIcon background = new ImageIcon(Toolkit.getDefaultToolkit().createImage(Window.class.getResource("bg-leftcentered.jpg")));
        labelBackGroundImage.setIcon(background);
        this.add(labelBackGroundImage);


        setIconImage(icon.getImage());

        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - this.getWidth())/2);
        int y = (int) ((dimension.getHeight() - this.getHeight())/2);
        this.setLocation(x, y);
        this.setVisible(true);

        JOptionPane.showMessageDialog(null, "This application works only with root permissions");

        if(configFilePath.exists()) // Проверка существования конфигурационного файла и чтения настроек
        {
            try {
                configFile = f.readFileToArrayList("/etc/sade/sade.conf");
                String[] sambaStatus = configFile.get(0).split("=");
                if(sambaStatus[1].equals("no")) jTextAreaAppOutput.append("Samba is not configured\n");
                else
                {
                    if(sambaStatus[1].equals("yes"))
                    {
                        String[] sambaVersion = configFile.get(1).split("=");
                        jTextAreaAppOutput.append("Samba installed.\nVersion " + sambaVersion[1]);
                        buttonUserManager.setEnabled(true);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();

            }
        }
        else // Если файла нет то создаем и вводим базовые параметры
        {
            configFile.add("SambaConfigured=no");
            configFile.add("SambaVersion=0");
            jTextAreaAppOutput.append("First launch detected.\nSamba is not configured");
            try {
                configFilePath.getParentFile().mkdirs();
                Path path = Paths.get("/etc/sade/sade.conf");
                Files.createDirectories(path.getParent());
                Files.createFile(path);
                f.writeArrayListToFile(configFile, "/etc/sade/sade.conf");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

class NoGuiFunctions // Функции для такстового режима
{
    boolean checkRoot() // проверка рут
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
}

public class SambaConfigurator
{
    public static void main(String[] args)
    {
        try
        {
            if(args[0].equals("nogui")) // Если был передан аргумент запустить в текстовом режиме
            {
                boolean sambaIsConfigured = false;
                NoGuiFunctions noGuifunctions = new NoGuiFunctions(); // библиотека функций

                boolean root = noGuifunctions.checkRoot();
                if(!root)
                {
                    System.out.println("No root permissions. Run application via terminal with sudo or using superuser");
                    System.exit(1);
                }

                File configFilePath = new File("/etc/sade/sade.conf"); // Конфигурационный файл
                ArrayList<String> configFile = new ArrayList<String>();
                Functions f = new Functions();

                // Если файл есть считать параметры, если нет записать базовые
                if(configFilePath.exists())
                {
                    try {
                        configFile = f.readFileToArrayList("/etc/sade/sade.conf");
                        String[] sambaStatus = configFile.get(0).split("=");
                        if(sambaStatus[1].equals("no"))
                        {
                            sambaIsConfigured = false;
                            System.out.println("Samba is not configured\n");
                        }
                        else
                        {
                            if(sambaStatus[1].equals("yes"))
                            {
                                sambaIsConfigured = true;
                                String[] sambaVersion = configFile.get(1).split("=");
                                System.out.println("Samba installed.\nVersion " + sambaVersion[1]);
                                //buttonUserManager.setEnabled(true);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();

                    }
                }
                else
                {
                    configFile.add("SambaConfigured=no");
                    configFile.add("SambaVersion=0");
                    System.out.println("First launch detected.\nSamba is not configured");
                    try {
                        configFilePath.getParentFile().mkdirs();
                        Path path = Paths.get("/etc/sade/sade.conf");
                        Files.createDirectories(path.getParent());
                        Files.createFile(path);
                        f.writeArrayListToFile(configFile, "/etc/sade/sade.conf");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                Scanner keyScanner = new Scanner(System.in); // Сканер ввода с клавиатуры
                int menu = -1;
                do
                {
                    System.out.print("\033[H\033[2J"); // Очистка экрана
                    System.out.println("-----------------------------------------");
                    System.out.println("Welcome to Samba configurator");
                    System.out.println("-----------------------------------------");
                    System.out.println("Menu:");
                    System.out.println("1. Show app configuration");
                    System.out.println("2. Configure Samba as a domain controller");
                    System.out.println("3. User accounts manager");
                    System.out.println("4. Show AD DC information");
                    System.out.println("\n0. Exit");
                    System.out.println("-----------------------------------------");
                    System.out.print("Your choice: ");
                    try
                    {
                        menu = keyScanner.nextInt();
                    }
                    catch(InputMismatchException e)
                    {
                        menu = -1;
                    }

                    switch(menu) // В случае выбора пункта меню переходить в определённую ветку
                    {
                        case 1:
                        {
                            String[] tmp = configFile.get(0).split("=");
                            String sambaConfigured = tmp[1];
                            tmp = configFile.get(1).split("=");
                            String sambaVersion =  tmp[1];
                            System.out.print("\033[H\033[2J");

                            if(sambaConfigured.equals("no"))
                            {
                                System.out.println("Samba wasn't configured");
                            }
                            else
                            {
                                System.out.println("Samba configured");
                                System.out.println("Installed version: " + sambaVersion);
                            }

                            System.out.println("Press 'Enter' key to continue");
                            try {
                                System.in.read();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            break;
                        }

                        case 2:
                        {
                            keyScanner.nextLine();
                            String[] tmp = configFile.get(0).split("=");
                            boolean isConfigured;
                            boolean configPermit = false;
                            if(tmp[1].equals("yes"))
                            {
                                isConfigured = true;
                            }
                            else
                            {
                                isConfigured = false;
                            }
                            System.out.print("\033[H\033[2J");
                            if(isConfigured) // Если сервер был настроен, спросить нужно ли его перенастраивать заново
                            {
                                System.out.println("Samba AD DC already configured.\nType 'yes' if you want to reconfigure it.\nType 'no' to cancel.");
                                System.out.println("Your choice: ");
                                String answer = keyScanner.nextLine();
                                if(answer.equals("yes"))
                                {
                                    configPermit = true;
                                }
                                else
                                {
                                    configPermit = false;
                                }
                            }
                            else
                            {
                                configPermit = true;
                            }
                            if(configPermit)
                            {
                                System.out.print("\033[H\033[2J");
                                System.out.println("Getting recommended settinds...");
                                //Считываем файл в память
                                ArrayList<String> interfacesFile = new ArrayList<String>();
                                ArrayList<InterfaceData> interfaces = new ArrayList<InterfaceData>();
                                String selectedEth = "";
                                String selectedHostname = "";
                                String selectedIpAddress = "";
                                String selectedNetmask = "";
                                String selectedBroadcast = "";
                                String selectedGateway = "";
                                String selectedNetwork = "";
                                String selectedDomainName = "";
                                String selectedFullName = "";
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
                                selectedEth = interfaces.get(0).name;

                                // Получаем имя машины

                                ArrayList<String> hostName = new ArrayList<>();
                                try {
                                    hostName = f.readFileToArrayList("/etc/hostname");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                String[] hostNameSplit = hostName.get(0).split("\\.");
                                selectedHostname = hostNameSplit[0];

                                // Получаем ip адреса

                                String ip = null;
                                try
                                {
                                    Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "hostname -I"});
                                    BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                                    ip = stdInput.readLine();
                                    ip = ip.replaceAll(" ", "");
                                    selectedIpAddress = ip;
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
                                    tmp = ip.split(" ");
                                    selectedGateway = tmp[2];
                                }
                                catch(IOException e)
                                {
                                    System.out.println("exception happened - heres what i know:");
                                    e.printStackTrace();
                                }
                                selectedNetmask = "255.255.255.0";
                                boolean checked;
                                do
                                {
                                    checked = true;
                                    System.out.println("Input domain name(ex: linux.local): ");
                                    selectedDomainName = keyScanner.nextLine();
                                    String[] testDomainName = selectedDomainName.split("\\.");

                                    if(testDomainName.length <2 )
                                    {
                                        if(!selectedDomainName.equals(""))
                                        {
                                            if(testDomainName.length == 1)
                                            {
                                                selectedDomainName = testDomainName[0] + "." + testDomainName[0];
                                            }
                                        }
                                        else
                                        {
                                            checked = false;
                                        }
                                    }
                                    if(!checked) System.out.println("Invalid hostname.");
                                }
                                while(!checked);

                                // Вывод сгенерированных настроек на экран
                                selectedFullName = selectedHostname + "." + selectedDomainName;
                                System.out.print("\033[H\033[2J");
                                System.out.println("Parameters generated.\n");

                                System.out.println("Selected parameters: ");
                                System.out.println("Ethernet interface: " + selectedEth);
                                System.out.println("IP address: " + selectedIpAddress);
                                System.out.println("Netmask: " + selectedNetmask);
                                System.out.println("Gateway: " + selectedGateway);
                                System.out.println("Computer's name: " + selectedHostname);
                                System.out.println("Domain name: " + selectedDomainName);
                                System.out.println("Full name: " + selectedFullName);
                                System.out.println("\nType 'yes' if you want to configure AD DC with recommended parameters.");
                                System.out.println("Type 'modify' if you want to modify selected parameters.");
                                System.out.println("Your choice: ");
                                String choice = keyScanner.nextLine();
                                if(choice.equals("modify"))
                                {
                                    // Если пользователь хочет модифицировать настройки запросить ввод всех настроек
                                    System.out.print("\033[H\033[2J");
                                    boolean check;

                                    System.out.println("Configuring static IP address");
                                    System.out.print("Available ethernet adapters:");
                                    for(int i = 0; i < interfaces.size(); i++) System.out.print(interfaces.get(i).name + " ");
                                    System.out.println();

                                    do
                                    {
                                        System.out.println("Select ethernet adapter: ");
                                        selectedEth = keyScanner.nextLine();
                                        check = true;
                                        for(int i = 0 ; i < interfaces.size(); i++)
                                        {
                                            if(selectedEth.equals(interfaces.get(i).name)) check = false;
                                        }
                                        if(check) System.out.println("Invalid ethernet adapter");
                                    }
                                    while(check);

                                    System.out.println("Your default ip address is " + selectedIpAddress + " ." +
                                        "\nIf you want to change it please change it manually and reboot the system" +
                                        "\nType 'yes' if you want to continue configuration with this address" +
                                        "\nType 'no' to abort the installation");
                                    String answer = keyScanner.nextLine();
                                    if(answer.equals("no")) System.exit(2);

                                    do
                                    {
                                        System.out.println("Input netmask address: ");
                                        selectedNetmask = keyScanner.nextLine();
                                        check = f.validIP(selectedNetmask);
                                        check = !check;
                                        if(check) System.out.println("Invalid netmask");
                                    }
                                    while(check);

                                    do
                                    {
                                        System.out.println("Input gateway: ");
                                        selectedGateway = keyScanner.nextLine();
                                        check = f.validIP(selectedGateway);
                                        check = !check;
                                        if(check) System.out.println("Invalid gateway");
                                    }
                                    while(check);

                                    do
                                    {
                                        System.out.println("Input hostname: ");
                                        check = false;
                                        selectedHostname = keyScanner.nextLine();
                                        String[] testComputersName = selectedHostname.split("\\.");
                                        if(testComputersName.length > 1)
                                        {
                                            System.out.println("Can't include dots");
                                            check = true;
                                        }
                                    }
                                    while(check);

                                    do
                                    {
                                        System.out.println("Input domain name: ");
                                        selectedDomainName = keyScanner.nextLine();
                                        check = false;
                                        String[] testDomainName = selectedDomainName.split("\\.");
                                        if(testDomainName.length <2 )
                                        {
                                            if(!selectedDomainName.equals(""))
                                            {
                                                if(testDomainName.length == 1)
                                                {
                                                    selectedDomainName = testDomainName[0] + "." + testDomainName[0];
                                                }
                                            }
                                            else
                                            {
                                                System.out.println("Invalid domain name");
                                                check = true;
                                            }
                                        }
                                    }
                                    while(check);

                                    selectedFullName = selectedHostname + "." + selectedDomainName;

                                    System.out.print("\033[H\033[2J");
                                    System.out.println("Parameters generated.\n");

                                    System.out.println("Selected parameters: ");
                                    System.out.println("Ethernet interface: " + selectedEth);
                                    System.out.println("IP address: " + selectedIpAddress);
                                    System.out.println("Netmask: " + selectedNetmask);
                                    System.out.println("Gateway: " + selectedGateway);
                                    System.out.println("Computer's name: " + selectedHostname);
                                    System.out.println("Domain name: " + selectedDomainName);
                                    System.out.println("Full name: " + selectedFullName);
                                    System.out.println("\nPress 'Enter' key to start configuration process");
                                    try {
                                        System.in.read();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }

                                // Процесс конфиурации
                                System.out.println("Configuring...");

                                System.out.println("Configuring interface...\n");

                                //Ищет нужный интерфейс в списке
                                String eth = selectedEth;
                                int number = -1;
                                for(int i = 0; i < interfaces.size(); i++)
                                {
                                    if(interfaces.get(i).name.equals(eth)) number = i;
                                }

                                //Удаляет указанный интерфейс из файла
                                int quantity = interfaces.get(number).endPos - interfaces.get(number).startPos;
                                for(int i = 0; i < quantity; i++) interfacesFile.remove(interfaces.get(number).startPos);

                                //Генерирует адрес сети
                                String[] mask = selectedNetmask.split("\\.");
                                String[] ipAddr = selectedIpAddress.split("\\.");

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
                                selectedNetwork = ipSubnet.toString();

                                String[] invertedMask = f.invertMask(selectedNetmask).split("\\.");

                                String[] networkIp = selectedNetwork.split("\\.");
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

                                selectedBroadcast = ipBroadcast.toString();

                                if(interfaces.get(number).startPos == interfaces.get(number).endPos)
                                {
                                    interfacesFile.add("");
                                    interfacesFile.add("#The primary network intarface");
                                    interfacesFile.add("");
                                    interfacesFile.add("auto " + interfaces.get(number).name);
                                    interfacesFile.add("iface " + interfaces.get(number).name + " inet static");
                                    interfacesFile.add("");
                                    interfacesFile.add("address " + selectedIpAddress);
                                    interfacesFile.add("netmask " + selectedNetmask);
                                    interfacesFile.add("network " + selectedNetwork);
                                    interfacesFile.add("broadcast " + selectedBroadcast);
                                    interfacesFile.add("gateway " + selectedGateway);
                                    interfacesFile.add("dns-nameservers " + selectedIpAddress + " 8.8.8.8");
                                    interfacesFile.add("dns-search " + selectedDomainName);
                                    interfacesFile.add("");
                                }
                                else
                                {
                                    interfacesFile.add(interfaces.get(number).startPos, "auto " + interfaces.get(number).name);
                                    interfacesFile.add(interfaces.get(number).startPos + 1, "iface " + interfaces.get(number).name + " inet static");
                                    interfacesFile.add(interfaces.get(number).startPos + 2, "");
                                    interfacesFile.add(interfaces.get(number).startPos + 3, "address " + selectedIpAddress);
                                    interfacesFile.add(interfaces.get(number).startPos + 4, "netmask " + selectedNetmask);
                                    interfacesFile.add(interfaces.get(number).startPos + 5, "network " + selectedNetwork);
                                    interfacesFile.add(interfaces.get(number).startPos + 6, "broadcast " + selectedBroadcast);
                                    interfacesFile.add(interfaces.get(number).startPos + 7, "gateway " + selectedGateway);
                                    interfacesFile.add(interfaces.get(number).startPos + 8, "dns-nameservers " + selectedIpAddress + " 8.8.8.8");
                                    interfacesFile.add(interfaces.get(number).startPos + 9, "dns-search " + selectedDomainName);
                                    interfacesFile.add(interfaces.get(number).startPos + 10, "");
                                }

                                //Записывает файл
                                try {
                                    f.writeArrayListToFile(interfacesFile, "/etc/network/interfaces");
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }

                                System.out.println("Interface configured");

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
                                        if((testLine[0].equals("127.0.1.1"))||(testLine[1].equals(selectedHostname)))
                                        {
                                            hosts.remove(i);
                                            hosts.add(i, selectedIpAddress + "\t" + selectedFullName);
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
                                        if(testLine[0].equals(selectedIpAddress))
                                        {
                                            hosts.remove(i);
                                            hosts.add(i, selectedIpAddress + "\t" + selectedFullName);
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
                                        if((testLine[0].equals("127.0.0.1"))||(testLine[1].equals("localhost")))
                                        {
                                            hosts.add(i+1, selectedIpAddress + "\t" + selectedFullName);
                                            configured = true;
                                            break;
                                        }
                                    }
                                }

                                if(!configured)
                                {
                                    hosts.add(0, selectedIpAddress + "\t" + selectedFullName);
                                }

                                try {
                                    f.writeArrayListToFile(hosts, "/etc/hosts");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                System.out.println("Configured /etc/hosts");

                                System.out.println("Setting hostname...");
                                ArrayList<String> hostname = new ArrayList<>();
                                hostname.add(selectedFullName);
                                try {
                                    f.writeArrayListToFile(hostname, "/etc/hostname");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                System.out.println("Hostname set");

                                System.out.println("Updating packages...");
                                String s = null;
                                try
                                {
                                    Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "apt-get update"});
                                    BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                                    BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                                    while ((s = stdInput.readLine()) != null)
                                    {
                                        System.out.println(s);
                                    }
                                    while ((s = stdError.readLine()) != null)
                                    {
                                        System.out.println(s);
                                    }
                                }
                                catch(IOException e)
                                {
                                    e.printStackTrace();
                                }
                                try
                                {
                                    Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "apt-get upgrade -y"});
                                    BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                                    BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                                    while ((s = stdInput.readLine()) != null)
                                    {
                                        System.out.println(s);
                                    }
                                    while ((s = stdError.readLine()) != null)
                                    {
                                        System.out.println(s);
                                    }
                                }
                                catch(IOException e)
                                {

                                    e.printStackTrace();
                                }
                                System.out.println("Packages updated");

                                System.out.println("Installing packages...");
                                s = null;
                                try
                                {
                                    Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "DEBIAN_FRONTEND=noninteractive apt-get -y install samba smbclient winbind krb5-user"});
                                    BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                                    BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                                    while ((s = stdInput.readLine()) != null)
                                    {
                                        System.out.println(s);
                                    }
                                    while ((s = stdError.readLine()) != null)
                                    {
                                        System.out.println(s);
                                    }
                                }
                                catch(IOException e)
                                {
                                    e.printStackTrace();
                                }
                                System.out.println("Packages Installed");

                                System.out.println("Configuring Samba...");

                                s = null;
                                try
                                {
                                    Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "rm /etc/samba/smb.conf"});
                                    BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                                    BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                                    while ((s = stdInput.readLine()) != null)
                                    {
                                        System.out.println(s);
                                    }
                                    while ((s = stdError.readLine()) != null)
                                    {
                                        System.out.println(s);
                                    }
                                }
                                catch(IOException e)
                                {
                                    e.printStackTrace();
                                }

                                String password = "";
                                boolean correct = false;
                                while(!correct)
                                {
                                    System.out.println("Input new administrator password: \nWarning! 8 letters length min, uppercase, lowercase, digits");
                                    password = keyScanner.nextLine();
                                    correct = f.checkPassword(password);
                                    if(!correct) JOptionPane.showMessageDialog(null, "Password isn't strong enough");
                                }


                                tmp = selectedDomainName.split("\\.");
                                String shortName = tmp[0].toUpperCase();


                                System.out.println("samba-tool domain provision --realm=" + selectedDomainName + " " +
                                        "--domain=" + shortName + " --adminpass=\"" + password + "\" --server-role=dc " +
                                        "--dns-backend=SAMBA_INTERNAL");

                                try
                                {
                                    Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "samba-tool domain provision --realm=" + selectedDomainName + " " +
                                            "--domain=" + shortName + " --adminpass=\"" + password + "\" --server-role=dc " +
                                            "--dns-backend=SAMBA_INTERNAL"});
                                    BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                                    BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                                    while ((s = stdInput.readLine()) != null)
                                    {
                                        System.out.println(s);
                                    }
                                    while ((s = stdError.readLine()) != null)
                                    {
                                        System.out.println(s);
                                    }
                                }
                                catch(IOException e)
                                {
                                    e.printStackTrace();
                                }

                                System.out.println("Samba configured");

                                System.out.println("Configuring Kerberos5...");
                                s = null;
                                try
                                {
                                    Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "cp /var/lib/samba/private/krb5.conf /etc/krb5.conf"});
                                    BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                                    BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                                    while ((s = stdInput.readLine()) != null)
                                    {
                                        System.out.println(s);
                                    }
                                    while ((s = stdError.readLine()) != null)
                                    {
                                        System.out.println(s);
                                    }
                                }
                                catch(IOException e)
                                {
                                    e.printStackTrace();
                                }

                                ArrayList<String> kerberosConfig = new ArrayList<String>();
                                try {
                                    kerberosConfig = f.readFileToArrayList("/etc/krb5.conf");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                kerberosConfig.add("[realms]");
                                kerberosConfig.add("\t" + selectedDomainName.toUpperCase() + " = {");
                                kerberosConfig.add("\t\t kdc = " + selectedFullName);
                                kerberosConfig.add("\t\t admin_server = " + selectedFullName);
                                kerberosConfig.add("\t}");
                                try {
                                    f.writeArrayListToFile(kerberosConfig, "/etc/krb5.conf");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                System.out.println("Kerberos5 configured\n");

                                System.out.println("Configuring resolv.conf...\n");
                                ArrayList<String> resolvConf = new ArrayList<String>();
                                try {
                                    resolvConf = f.readFileToArrayList("/etc/resolv.conf");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                boolean conf = false;
                                for(int i = 0; i < resolvConf.size(); i++)
                                {
                                    String[] line = resolvConf.get(i).split(" ");
                                    if(line[0].equals("domain"))
                                    {
                                        resolvConf.remove(i);
                                        resolvConf.add(i, "domain " + selectedDomainName.toUpperCase());
                                        conf = true;
                                    }
                                }
                                if(!conf)
                                {
                                    resolvConf.add("domain " + selectedDomainName.toUpperCase());
                                }

                                try {
                                    f.writeArrayListToFile(resolvConf,"/etc/resolv.conf");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                System.out.println("resolv.conf configured");

                                System.out.println("Generating directory...\n");
                                s = null;
                                try
                                {
                                    Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "mkdir -m 770 /Users"});
                                    BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                                    BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                                    while ((s = stdInput.readLine()) != null)
                                    {
                                        System.out.println(s);
                                    }
                                    while ((s = stdError.readLine()) != null)
                                    {
                                        System.out.println(s);
                                    }
                                }
                                catch(IOException e)
                                {
                                    e.printStackTrace();
                                }

                                try
                                {
                                    Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "chmod g+s /Users"});
                                    BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                                    BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                                    while ((s = stdInput.readLine()) != null)
                                    {
                                        System.out.println(s);
                                    }
                                    while ((s = stdError.readLine()) != null)
                                    {
                                        System.out.println(s);
                                    }
                                }
                                catch(IOException e)
                                {
                                    e.printStackTrace();
                                }

                                try
                                {
                                    Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "chown root:users /Users"});
                                    BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                                    BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                                    while ((s = stdInput.readLine()) != null)
                                    {
                                        System.out.println(s);
                                    }
                                    while ((s = stdError.readLine()) != null)
                                    {
                                        System.out.println(s);
                                    }
                                }
                                catch(IOException e)
                                {
                                    e.printStackTrace();
                                }
                                System.out.println("Directory generated");

                                System.out.print("Samba post-configuring...");
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

                                String version = null;
                                try
                                {
                                    Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "smbclient -V"});
                                    BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                                    version = stdInput.readLine();
                                    configFile.set(0, "SambaConfigured=yes");
                                    configFile.set(1, "SambaVersion=" + version);
                                    f.writeArrayListToFile(configFile, "/etc/sade/sade.conf");
                                }
                                catch(IOException e)
                                {
                                    System.out.println("exception happened - heres what i know:");
                                    e.printStackTrace();
                                }

                                System.out.println("\n");
                                System.out.println("Configuration completed");
                                System.out.println("Please reboot your system to activating AD DC");
                                System.out.print("Press 'Enter' key to continue");
                                try
                                {
                                    System.in.read();
                                }
                                catch(IOException e)
                                {

                                }
                            }
                            break;
                        }

                        case 3: // Меню управления пользователями
                        {
                            if(!sambaIsConfigured)
                            {
                                System.out.print("\033[H\033[2J");
                                System.out.println("Samba is not configured\nPlease configure samba firstly\n");
                                System.out.print("Press 'Enter' key to continue");
                                try
                                {
                                    System.in.read();
                                }
                                catch(IOException e)
                                {

                                }
                                break;
                            }
                            int menu2 = -1;
                            do
                            {
                                System.out.print("\033[H\033[2J");
                                System.out.println("-----------------------------------------");
                                System.out.println("User manager");
                                System.out.println("-----------------------------------------");
                                System.out.println("Menu:");
                                System.out.println("1. List users");
                                System.out.println("2. Add user");
                                System.out.println("3. Change user's password");
                                System.out.println("4. Remove user");
                                System.out.println("5. Show user' password policy");
                                System.out.println("6. Manage groups");
                                System.out.println("\n0. Back");
                                System.out.println("-----------------------------------------");
                                System.out.print("Your choice: ");
                                try
                                {
                                    menu2 = keyScanner.nextInt();
                                }
                                catch(InputMismatchException e)
                                {
                                    menu2 = -1;
                                }

                                switch(menu2)
                                {
                                    case 1:
                                    {
                                        System.out.print("\033[H\033[2J");
                                        String s = null;
                                        String userlist = "";
                                        try
                                        {
                                            Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "samba-tool user list"});
                                            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                                            while ((s = stdInput.readLine()) != null)
                                            {
                                                userlist = userlist + s + ", ";
                                            }
                                        }
                                        catch(IOException e)
                                        {
                                            e.printStackTrace();
                                        }

                                        if(userlist.length() > 0) userlist = userlist.substring(0,userlist.length()-2);
                                        System.out.println(userlist);

                                        System.out.println("Press 'Enter' key to continue");
                                        try {
                                            System.in.read();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                        break;
                                    }

                                    case 2:
                                    {
                                        System.out.print("\033[H\033[2J");
                                        System.out.println("Input new username: ");
                                        keyScanner.nextLine();
                                        String newUsername = keyScanner.nextLine();

                                        boolean correct;
                                        String newPassword = "";
                                        do
                                        {
                                            System.out.println("Input new user's password: ");
                                            newPassword = keyScanner.nextLine();
                                            correct = f.checkPassword(newPassword);
                                            if(!correct)
                                            {
                                                System.out.println("Password isn't strong enough");
                                            }
                                        }
                                        while(!correct);

                                        int changePassword = 0;
                                        do
                                        {
                                            System.out.println("Request change password after login?(yes/no): ");
                                            String answer = keyScanner.nextLine();
                                            if(answer.equals("yes")) changePassword = 1;
                                            if(answer.equals("no")) changePassword = 2;
                                            if(changePassword == 0) System.out.println("Incorrect value");
                                        }
                                        while(changePassword == 0);

                                        int expiryDays = -1;

                                        do
                                        {
                                            System.out.print("Expiration of password(days, 0 - no expiry, max 42): ");
                                            try
                                            {
                                                int answer = keyScanner.nextInt();
                                                if((answer > 42)||(answer < 0)) System.out.println("Incorrect value");
                                                else
                                                {
                                                    expiryDays = answer;
                                                }
                                            }
                                            catch(InputMismatchException e)
                                            {

                                            }

                                        }
                                        while(expiryDays==-1);

                                        String s = null;
                                        try
                                        {
                                            String command = "samba-tool user add \"" + newUsername + "\" " + newPassword;
                                            if(changePassword == 1) command = command + " --must-change-at-next-login";

                                            System.out.println(command);
                                            Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", command});
                                            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                                            while ((s = stdInput.readLine()) != null)
                                            {
                                                System.out.println(s);
                                            }


                                            command = "samba-tool user setexpiry \"" + newUsername+"\"";
                                            if(expiryDays == 0)
                                            {
                                                command = command + " --noexpiry";
                                            }
                                            else
                                            {
                                                String days = "" + expiryDays;
                                                command = command + " --days="+days;
                                            }
                                            p = Runtime.getRuntime().exec(new String[]{"bash", "-c", command});
                                            stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                                            while ((s = stdInput.readLine()) != null)
                                            {
                                                System.out.println(s);
                                            }
                                            System.out.println(command);
                                        }
                                        catch(IOException e)
                                        {
                                            e.printStackTrace();
                                        }

                                        System.out.println("Press 'Enter' key to continue");
                                        try {
                                            System.in.read();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                        break;
                                    }

                                    case 3:
                                    {
                                        System.out.print("\033[H\033[2J");
                                        System.out.println("Input username: ");
                                        keyScanner.nextLine();
                                        String username = keyScanner.nextLine();

                                        boolean correct;
                                        String newPassword = "";
                                        do
                                        {
                                            System.out.println("Input new user's password: ");
                                            newPassword = keyScanner.nextLine();
                                            correct = f.checkPassword(newPassword);
                                            if(!correct)
                                            {
                                                System.out.println("Password isn't strong enough");
                                            }
                                        }
                                        while(!correct);

                                        String s = null;
                                        try {
                                            Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "samba-tool user setpassword \"" + username + "\" --newpassword=" + newPassword});
                                            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                                            while ((s = stdInput.readLine()) != null)
                                            {
                                                System.out.println(s);
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                        System.out.println("Press 'Enter' key to continue");
                                        try {
                                            System.in.read();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                        break;
                                    }

                                    case 4:
                                    {
                                        System.out.print("\033[H\033[2J");
                                        System.out.println("Input username who you want to delete: ");
                                        keyScanner.nextLine();
                                        String username = keyScanner.nextLine();
                                        String s = null;
                                        try
                                        {
                                            Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "samba-tool user delete \"" + username + "\""});
                                            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                                            while ((s = stdInput.readLine()) != null)
                                            {
                                                System.out.println(s);
                                            }
                                        }
                                        catch (IOException e)
                                        {
                                            e.printStackTrace();
                                        }

                                        System.out.println("Press 'Enter' key to continue");
                                        try {
                                            System.in.read();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                        break;
                                    }

                                    case 5:
                                    {
                                        System.out.print("\033[H\033[2J");
                                        String answer = "";
                                        String s = null;
                                        try {
                                            Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "samba-tool domain passwordsettings show"});
                                            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                                            while ((s = stdInput.readLine()) != null) {
                                                answer = answer + s + "\n";
                                            }
                                            System.out.println(answer);
                                        } catch (IOException ex) {
                                            ex.printStackTrace();
                                        }

                                        System.out.println("Press 'Enter' key to continue");
                                        try {
                                            System.in.read();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                        break;
                                    }

                                    case 6: // Меню управления группами
                                    {
                                        int menu3 = -1;
                                        do
                                        {
                                            System.out.print("\033[H\033[2J");
                                            System.out.println("-----------------------------------------");
                                            System.out.println("Group manager");
                                            System.out.println("-----------------------------------------");
                                            System.out.println("Menu:");
                                            System.out.println("1. List groups");
                                            System.out.println("2. List users of group");
                                            System.out.println("3. Add group");
                                            System.out.println("4. Remove group");
                                            System.out.println("5. Add user to group");
                                            System.out.println("6. Remove user from group");
                                            System.out.println("\n0. Back");
                                            System.out.println("-----------------------------------------");
                                            System.out.print("Your choice: ");
                                            try
                                            {
                                                menu3 = keyScanner.nextInt();
                                            }
                                            catch(InputMismatchException e)
                                            {
                                                menu3 = -1;
                                            }

                                            switch(menu3)
                                            {
                                                case 1:
                                                {
                                                    System.out.print("\033[H\033[2J");
                                                    String s = null;
                                                    String answer = "";
                                                    try
                                                    {
                                                        Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "samba-tool group list"});
                                                        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                                                        while ((s = stdInput.readLine()) != null)
                                                        {
                                                            answer = answer + s + ", ";
                                                        }
                                                    }
                                                    catch(IOException e)
                                                    {
                                                        e.printStackTrace();
                                                    }
                                                    if(answer.length() > 0) answer = answer.substring(0, answer.length() - 2);
                                                    System.out.println(answer);

                                                    System.out.println("Press 'Enter' key to continue");
                                                    try {
                                                        System.in.read();
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }

                                                    break;
                                                }
                                                case 2:
                                                {
                                                    System.out.print("\033[H\033[2J");
                                                    System.out.println("Input group's name: ");
                                                    keyScanner.nextLine();
                                                    String group = keyScanner.nextLine();
                                                    String s = null;
                                                    String answer = "";
                                                    ArrayList<String> users = new ArrayList<String>();
                                                    try
                                                    {
                                                        Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "samba-tool group listmembers \"" + group + "\""});
                                                        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                                                        while ((s = stdInput.readLine()) != null)
                                                        {
                                                            answer = answer + s + ", ";
                                                        }
                                                        if(answer.length() > 0) answer = answer.substring(0, answer.length()-2);
                                                    }
                                                    catch(IOException e)
                                                    {
                                                        e.printStackTrace();
                                                    }

                                                    System.out.println(answer);

                                                    System.out.println("Press 'Enter' key to continue");
                                                    try {
                                                        System.in.read();
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }

                                                    break;
                                                }
                                                case 3:
                                                {
                                                    System.out.print("\033[H\033[2J");
                                                    System.out.println("Input new group's name: ");
                                                    keyScanner.nextLine();
                                                    String newGroup = keyScanner.nextLine();
                                                    String s = null;
                                                    ArrayList<String> users = new ArrayList<String>();
                                                    try
                                                    {
                                                        Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "samba-tool group add \"" + newGroup + "\""});
                                                        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                                                        while ((s = stdInput.readLine()) != null)
                                                        {
                                                            System.out.println(s);
                                                        }
                                                    }
                                                    catch(IOException ex)
                                                    {
                                                        ex.printStackTrace();
                                                    }

                                                    System.out.println("Press 'Enter' key to continue");
                                                    try {
                                                        System.in.read();
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }

                                                    break;
                                                }
                                                case 4:
                                                {
                                                    System.out.print("\033[H\033[2J");
                                                    System.out.println("Input group's name to delete: ");
                                                    keyScanner.nextLine();
                                                    String group = keyScanner.nextLine();

                                                    String s = null;
                                                    try
                                                    {
                                                        Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "samba-tool group delete \"" + group + "\""});
                                                        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                                                        while ((s = stdInput.readLine()) != null) {
                                                            System.out.println(s);
                                                        }
                                                    }
                                                    catch (IOException ex)
                                                    {
                                                        ex.printStackTrace();
                                                    }

                                                    System.out.println("Press 'Enter' key to continue");
                                                    try {
                                                        System.in.read();
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }

                                                    break;
                                                }
                                                case 5:
                                                {
                                                    System.out.print("\033[H\033[2J");
                                                    System.out.println("Input username: ");
                                                    keyScanner.nextLine();
                                                    String username = keyScanner.nextLine();
                                                    System.out.println("Input group's name to add user into: ");
                                                    String group = keyScanner.nextLine();

                                                    String s = null;
                                                    try
                                                    {
                                                        Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "samba-tool group addmembers \"" + group + "\" \"" + username + "\""});
                                                        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                                                        while ((s = stdInput.readLine()) != null) {
                                                            System.out.println(s);
                                                        }
                                                    }
                                                    catch (IOException exe)
                                                    {
                                                        exe.printStackTrace();
                                                    }

                                                    System.out.println("Press 'Enter' key to continue");
                                                    try {
                                                        System.in.read();
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }

                                                    break;
                                                }
                                                case 6:
                                                {
                                                    System.out.print("\033[H\033[2J");
                                                    System.out.println("Input username: ");
                                                    keyScanner.nextLine();
                                                    String username = keyScanner.nextLine();
                                                    System.out.println("Input group's name from what group remove user: ");
                                                    String group = keyScanner.nextLine();

                                                    String s = null;
                                                    try
                                                    {
                                                        Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "samba-tool group removemembers \"" + group + "\" \"" + username + "\""});
                                                        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                                                        while ((s = stdInput.readLine()) != null) {
                                                            System.out.println(s);
                                                        }
                                                    }
                                                    catch (IOException exe)
                                                    {
                                                        exe.printStackTrace();
                                                    }

                                                    System.out.println("Press 'Enter' key to continue");
                                                    try {
                                                        System.in.read();
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }

                                                    break;
                                                }
                                            }
                                        }
                                        while(menu3 != 0);
                                    }
                                }
                            }
                            while(menu2 != 0);
                            break;
                        }

                        case 4:
                        {
                            if(!sambaIsConfigured)
                            {
                                System.out.print("\033[H\033[2J");
                                System.out.println("Samba is not configured\nPlease configure samba firstly\n");
                                System.out.print("Press 'Enter' key to continue");
                                try
                                {
                                    System.in.read();
                                }
                                catch(IOException e)
                                {

                                }
                                break;
                            }
                            System.out.print("\033[H\033[2J");
                            String selectedIpAddress = "";
                            boolean check;
                            do
                            {
                                System.out.println("Input IP address: ");
                                keyScanner.nextLine();
                                selectedIpAddress = keyScanner.nextLine();
                                check = f.validIP(selectedIpAddress);
                                check = !check;
                                if(check) System.out.println("Invalid IP address");
                            }
                            while(check);

                            String answer = "";
                            String s = null;
                            try
                            {
                                Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "samba-tool domain info " + selectedIpAddress});
                                BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                                BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                                while ((s = stdInput.readLine()) != null)
                                {
                                    answer = answer + s + "\n";
                                }
                                while ((s = stdError.readLine()) != null)
                                {
                                    answer = answer + s + "\n";
                                }
                            }
                            catch(IOException ex)
                            {
                                ex.printStackTrace();
                            }

                            System.out.println(answer);

                            System.out.println("Press 'Enter' key to continue");
                            try {
                                System.in.read();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            break;
                        }

                        case 0:
                        {
                            System.out.print("\033[H\033[2J");
                            break;
                        }
                    }
                }
                while(menu!=0);
            }
        }
        catch(ArrayIndexOutOfBoundsException e)
        {
            new Window();
        }
    }
}
