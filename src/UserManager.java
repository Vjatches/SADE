import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class UserManager extends JDialog
{
    Functions f = new Functions();
    JScrollPane scrollUsers = new JScrollPane();
    DefaultListModel listModel = new DefaultListModel<>();
    JList listUsers = new JList(listModel);

    JLabel labelAddUser = new JLabel("Add user");
    JLabel labelUsername = new JLabel("Username:");
    JLabel labelPassword = new JLabel("Password:");

    JTextField jTextFieldUsername = new JTextField();
    JTextField jTextFieldPassword = new JTextField();

    JCheckBox checkboxNoExpiry = new JCheckBox("No expiry password");
    JCheckBox checkboxChangePassAfterLogin = new JCheckBox("Change password after login");
    JLabel labelPasswordExpires = new JLabel("Password expires in");
    JSpinner spinnerDays = new JSpinner();
    JLabel labelDays = new JLabel("days");

    JButton buttonAdd = new JButton("Add");
    JButton buttonClear = new JButton("Clear");

    JButton buttonShowPasswordSettings = new JButton("Show password settings");

    JButton buttonRemove = new JButton("Remove");
    JButton buttonChangePassword = new JButton("New password");

    JButton buttonGroupManager = new JButton("Manage groups");

    public void updateUserList()
    {
        String s = null;
        ArrayList<String> users = new ArrayList<String>();
        try
        {
            Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "samba-tool user list"});
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((s = stdInput.readLine()) != null)
            {
                users.add(s);
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        listModel.clear();
        for(int i = 0; i < users.size(); i++)
        {
            listModel.addElement(users.get(i));
        }
    }

    class AddUserButton implements ActionListener
    {

        @Override
        public void actionPerformed(ActionEvent actionEvent)
        {
            String username = jTextFieldUsername.getText();
            String password = jTextFieldPassword.getText();

            if(username.isEmpty())
            {
                JOptionPane.showMessageDialog(null, "Username can't be empty");
            }
            else if(password.isEmpty())
            {
                JOptionPane.showMessageDialog(null, "Password can't be empty");

            }
            else
            {
                boolean correct = f.checkPassword(password);
                if(!correct)
                {
                    JOptionPane.showMessageDialog(null, "Password isn't strong enough");
                }
                else
                {
                    String s = null;
                    try
                    {
                        String command = "samba-tool user add \"" + username + "\" " + password;
                        if(checkboxChangePassAfterLogin.isSelected()) command = command + " --must-change-at-next-login";

                        Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", command});
                        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                        while ((s = stdInput.readLine()) != null)
                        {
                            JOptionPane.showMessageDialog(null, s);
                        }

                        command = "samba-tool user setexpiry " + username;
                        if(checkboxNoExpiry.isSelected())
                        {
                            command = command + " --noexpiry";
                        }
                        else
                        {
                            String days = spinnerDays.getValue().toString();
                            command = command + " --days="+days;
                        }
                        p = Runtime.getRuntime().exec(new String[]{"bash", "-c", command});
                        stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                        while ((s = stdInput.readLine()) != null)
                        {
                            JOptionPane.showMessageDialog(null, s);
                        }
                        jTextFieldUsername.setText("");
                        jTextFieldPassword.setText("");
                    }
                    catch(IOException e)
                    {
                        e.printStackTrace();
                    }
                }
                updateUserList();
            }

        }
    }

    class ClearButton implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent actionEvent)
        {
            jTextFieldUsername.setText("");
            jTextFieldPassword.setText("");
        }
    }

    class RemoveButton implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent actionEvent)
        {
            try
            {
                String selectedUser = listUsers.getSelectedValue().toString();
                if (!selectedUser.equals(""))
                {
                    int reply = JOptionPane.showConfirmDialog(null, "Remove user \"" + selectedUser + "\"?", "choose one", JOptionPane.YES_NO_OPTION);
                    if (reply == JOptionPane.YES_OPTION)
                    {
                        String s = null;
                        try
                        {
                            Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "samba-tool user delete \"" + selectedUser + "\""});
                            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                            while ((s = stdInput.readLine()) != null) {
                                JOptionPane.showMessageDialog(null, s);
                            }
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                        updateUserList();
                    }
                }
            }
            catch(NullPointerException ex)
            {
                JOptionPane.showMessageDialog(null, "User is not selected");
            }

        }
    }

    class ChangePassword implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent actionEvent)
        {
            try
            {
                String selectedUser = listUsers.getSelectedValue().toString();
                if(!selectedUser.equals("")) {
                    int reply = JOptionPane.showConfirmDialog(null, "Change password for user \"" + selectedUser + "\"?", "Change password", JOptionPane.YES_NO_OPTION);
                    if (reply == JOptionPane.YES_OPTION) {

                        boolean correct = true;
                        String newPassword = "";
                        while(!correct)
                        {
                            newPassword = JOptionPane.showInputDialog(null, "Input new password for\"" + selectedUser + "\":");
                            correct = f.checkPassword(newPassword);
                            if(!correct)
                            {
                                JOptionPane.showMessageDialog(null, "Password isn't strong enough");
                            }
                        }

                        String s = null;
                        try {
                            Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "samba-tool user setpassword \"" + selectedUser + "\" --newpassword=" + newPassword});
                            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                            while ((s = stdInput.readLine()) != null) {
                                JOptionPane.showMessageDialog(null, s);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            catch(NullPointerException ex)
            {
                JOptionPane.showMessageDialog(null, "User is not selected");
            }

        }
    }

    class GroupManagerButton implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            new GroupsManager();
        }
    }

    class ShowPasswordSettings implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            String answer = "";
            String s = null;
            try {
                Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "samba-tool domain passwordsettings show"});
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                while ((s = stdInput.readLine()) != null) {
                    answer = answer + s + "\n";
                }
                JOptionPane.showMessageDialog(null, answer);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    class EnableNoExpiry implements ItemListener
    {
        @Override
        public void itemStateChanged(ItemEvent itemEvent)
        {
            if(checkboxNoExpiry.isSelected())
            {
                spinnerDays.setEnabled(false);
            }
            else
            {
                spinnerDays.setEnabled(true);
            }
        }
    }

    UserManager()
    {
        super();
        this.setResizable(false);
        setModal(true);
        setSize(590, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setLayout(null);

        scrollUsers.add(listUsers);
        scrollUsers.setViewportView(listUsers);
        scrollUsers.setBounds(10, 10, 300, 300);
        this.add(scrollUsers);

        buttonRemove.setBounds(10, 320, 145, 20);
        ActionListener removeUser = new RemoveButton();
        buttonRemove.addActionListener(removeUser);
        this.add(buttonRemove);
        buttonChangePassword.setBounds(165, 320, 145, 20);
        ActionListener changePassword = new ChangePassword();
        buttonChangePassword.addActionListener(changePassword);
        this.add(buttonChangePassword);
        buttonGroupManager.setBounds(10, 350, 300, 20);
        ActionListener groupManagerButton = new GroupManagerButton();
        buttonGroupManager.addActionListener(groupManagerButton);
        this.add(buttonGroupManager);

        labelAddUser.setBounds(445, 10, 100, 20);
        this.add(labelAddUser);
        labelUsername.setBounds(320, 40, 100, 20);
        this.add(labelUsername);
        labelPassword.setBounds(320, 70, 100, 20);
        this.add(labelPassword);

        jTextFieldUsername.setBounds(430, 40, 150, 20);
        this.add(jTextFieldUsername);
        jTextFieldPassword.setBounds(430, 70, 150, 20);
        this.add(jTextFieldPassword);

        checkboxChangePassAfterLogin.setBounds(320, 100, 300, 20);
        this.add(checkboxChangePassAfterLogin);
        checkboxNoExpiry.setBounds(320, 130, 300, 20 );
        ItemListener noExpiryListener = new EnableNoExpiry();
        checkboxNoExpiry.addItemListener(noExpiryListener);
        checkboxNoExpiry.setSelected(true);
        this.add(checkboxNoExpiry);
        labelPasswordExpires.setBounds(320, 160, 150, 20);
        this.add(labelPasswordExpires);
        SpinnerModel spinnerModelDays = new SpinnerNumberModel(41, 1, 42, 1);
        spinnerDays.setModel(spinnerModelDays);
        spinnerDays.setBounds(470, 160, 40, 20);
        this.add(spinnerDays);
        labelDays.setBounds(520, 160, 50, 20);
        this.add(labelDays);

        buttonAdd.setBounds(320,190, 125, 20);
        ActionListener addUser = new AddUserButton();
        buttonAdd.addActionListener(addUser);
        this.add(buttonAdd);
        buttonClear.setBounds(455, 190, 125, 20);
        ActionListener clearData = new ClearButton();
        buttonClear.addActionListener(clearData);
        this.add(buttonClear);

        buttonShowPasswordSettings.setBounds(320, 350, 260, 20);
        ActionListener showPasswordSettings = new ShowPasswordSettings();
        buttonShowPasswordSettings.addActionListener(showPasswordSettings);
        this.add(buttonShowPasswordSettings);

        updateUserList();

        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - this.getWidth())/2);
        int y = (int) ((dimension.getHeight() - this.getHeight())/2);
        this.setLocation(x, y);

        this.setVisible(true);
    }
}
