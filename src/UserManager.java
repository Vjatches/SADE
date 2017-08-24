import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class UserManager extends JDialog
{
    JScrollPane scrollUsers = new JScrollPane();
    DefaultListModel listModel = new DefaultListModel<>();
    JList listUsers = new JList(listModel);

    JLabel labelAddUser = new JLabel("Add user");
    JLabel labelUsername = new JLabel("Username:");
    JLabel labelPassword = new JLabel("Password:");

    JTextField jTextFieldUsername = new JTextField();
    JTextField jTextFieldPassword = new JTextField();

    JButton buttonAdd = new JButton("Add");
    JButton buttonClear = new JButton("Clear");

    JButton buttonRemove = new JButton("Remove");
    JButton buttonChangePassword = new JButton("New password");


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

    class AddUserButton implements ActionListener{

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
                String s = null;
                try
                {
                    Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "samba-tool user add " + username + " " + password});
                    BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    while ((s = stdInput.readLine()) != null)
                    {
                        JOptionPane.showMessageDialog(null, s);
                    }
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }
                updateUserList();
            }

        }
    }

    class ClearButton implements ActionListener{
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
        public void actionPerformed(ActionEvent actionEvent) {
            String selectedUser = listUsers.getSelectedValue().toString();
            if (!selectedUser.equals("")) {
                int reply = JOptionPane.showConfirmDialog(null, "Remove user \"" + selectedUser + "\"?", "choose one", JOptionPane.YES_NO_OPTION);
                if (reply == JOptionPane.YES_OPTION) {
                    String s = null;
                    try {
                        Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "samba-tool user delete " + selectedUser});
                        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                        while ((s = stdInput.readLine()) != null) {
                            JOptionPane.showMessageDialog(null, s);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    updateUserList();
                }
            }
        }
    }

    class ChangePassword implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent actionEvent)
        {
            String selectedUser = listUsers.getSelectedValue().toString();
            if(!selectedUser.equals("")) {
                int reply = JOptionPane.showConfirmDialog(null, "Change password for user \"" + selectedUser + "\"?", "Change password", JOptionPane.YES_NO_OPTION);
                if (reply == JOptionPane.YES_OPTION) {
                    String newPassword = JOptionPane.showInputDialog(null, "Input new password for\"" + selectedUser + "\":");
                    String s = null;
                    try {
                        Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "samba-tool user setpassword " + selectedUser + " --newpassword=" + newPassword});
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
    }

    UserManager()
    {
        super();
        this.setResizable(false);
        setModal(true);
        setSize(590, 370);
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

        buttonAdd.setBounds(320,100 , 125, 20);
        ActionListener addUser = new AddUserButton();
        buttonAdd.addActionListener(addUser);
        this.add(buttonAdd);
        buttonClear.setBounds(455, 100, 125, 20);
        ActionListener clearData = new ClearButton();
        buttonClear.addActionListener(clearData);
        this.add(buttonClear);

        updateUserList();

        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - this.getWidth())/2);
        int y = (int) ((dimension.getHeight() - this.getHeight())/2);
        this.setLocation(x, y);

        this.setVisible(true);
    }
}
