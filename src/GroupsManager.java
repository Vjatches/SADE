import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class GroupsManager extends JDialog
{
    Functions f = new Functions();

    JScrollPane scrollGroups = new JScrollPane();
    DefaultListModel listModel1 = new DefaultListModel<>();
    JList listGroups = new JList(listModel1);

    JButton buttonAddGroup = new JButton("Add group");
    JButton buttonRemoveGroup = new JButton("Remove group");

    JButton buttonRemoveFromGroup = new JButton("Remove from group");

    JScrollPane scrollUsers = new JScrollPane();
    DefaultListModel listModel2 = new DefaultListModel<>();
    JList listUsers = new JList(listModel2);

    JComboBox comboBoxUsers = new JComboBox();
    JButton buttonAddUserToGroup = new JButton("Add to group");

    public void updateGroupsList()
    {
        String s = null;
        ArrayList<String> groups = new ArrayList<String>();
        try
        {
            Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "samba-tool group list"});
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((s = stdInput.readLine()) != null)
            {
                groups.add(s);
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        listModel1.clear();
        for(int i = 0; i < groups.size(); i++)
        {
            listModel1.addElement(groups.get(i));
        }
    }

    public void updateUserList(String group)
    {
        String s = null;
        ArrayList<String> users = new ArrayList<String>();
        try
        {
            Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "samba-tool group listmembers \"" + group + "\""});
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

        listModel2.clear();
        for(int i = 0; i < users.size(); i++)
        {
            listModel2.addElement(users.get(i));
        }
    }

    class ListGroupSelectListener implements ListSelectionListener
    {
        public void valueChanged(ListSelectionEvent e)
        {
            if(!e.getValueIsAdjusting())
            {
                try
                {
                    String group = listGroups.getSelectedValue().toString();
                    updateUserList(group);
                }
                catch(NullPointerException ex)
                {

                }
            }
        }
    }

    class AddGroup implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            String newGroup = JOptionPane.showInputDialog(null, "Input new group's name:");
            String s = null;
            ArrayList<String> users = new ArrayList<String>();
            try
            {
                Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "samba-tool group add \"" + newGroup + "\""});
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                while ((s = stdInput.readLine()) != null)
                {
                    JOptionPane.showMessageDialog(null, s);
                }
            }
            catch(IOException ex)
            {
                ex.printStackTrace();
            }
            updateGroupsList();
        }
    }

    class RemoveGroup implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            String selectedGroup = listGroups.getSelectedValue().toString();
            if (!selectedGroup.equals(""))
            {
                int reply = JOptionPane.showConfirmDialog(null, "Remove group \"" + selectedGroup + "\"?", "Choose one", JOptionPane.YES_NO_OPTION);
                if (reply == JOptionPane.YES_OPTION)
                {
                    String s = null;
                    try
                    {
                        Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "samba-tool group delete \"" + selectedGroup + "\""});
                        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                        while ((s = stdInput.readLine()) != null) {
                            JOptionPane.showMessageDialog(null, s);
                        }
                    }
                    catch (IOException ex)
                    {
                        ex.printStackTrace();
                    }
                    updateGroupsList();
                }
            }
        }
    }

    class RemoveFromGroup implements ActionListener
    {
        public void actionPerformed(ActionEvent ex)
        {
            try
            {
                String username = listUsers.getSelectedValue().toString();
                String group = listGroups.getSelectedValue().toString();

                int reply = JOptionPane.showConfirmDialog(null, "Remove user \"" + username + "\" from group \"" + group + "\"?", "Choose one", JOptionPane.YES_NO_OPTION);
                if (reply == JOptionPane.YES_OPTION)
                {
                    String s = null;
                    try
                    {
                        Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "samba-tool group removemembers \"" + group + "\" \"" + username + "\""});
                        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                        while ((s = stdInput.readLine()) != null) {
                            JOptionPane.showMessageDialog(null, s);
                        }
                    }
                    catch (IOException exe)
                    {
                        exe.printStackTrace();
                    }
                    updateUserList(group);
                }
            }
            catch(NullPointerException e)
            {

            }

        }
    }

    class AddUserToGroup implements ActionListener
    {
        public void actionPerformed(ActionEvent ex)
        {
            try
            {
                String username = comboBoxUsers.getSelectedItem().toString();
                String group = listGroups.getSelectedValue().toString();

                int reply = JOptionPane.showConfirmDialog(null, "Add user \"" + username + "\" to group \"" + group + "\"?", "Choose one", JOptionPane.YES_NO_OPTION);
                if (reply == JOptionPane.YES_OPTION)
                {
                    String s = null;
                    try
                    {
                        Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "samba-tool group addmembers \"" + group + "\" \"" + username + "\""});
                        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                        while ((s = stdInput.readLine()) != null) {
                            JOptionPane.showMessageDialog(null, s);
                        }
                    }
                    catch (IOException exe)
                    {
                        exe.printStackTrace();
                    }
                    updateUserList(group);
                }
            }
            catch(NullPointerException exe)
            {

            }
        }
    }

    public void updateUsers()
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
        comboBoxUsers = new JComboBox(users.toArray());
    }

    GroupsManager()
    {
        super();
        this.setResizable(false);
        setModal(true);
        setSize(800, 270);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setLayout(null);

        scrollGroups.add(listGroups);
        scrollGroups.setViewportView(listGroups);
        scrollGroups.setBounds(10, 10, 340, 200);
        ListSelectionListener scrollGroupsListener = new ListGroupSelectListener();
        listGroups.addListSelectionListener(scrollGroupsListener);
        this.add(scrollGroups);

        buttonAddGroup.setBounds(10, 220, 165, 20);
        ActionListener addGroup = new AddGroup();
        buttonAddGroup.addActionListener(addGroup);
        this.add(buttonAddGroup);
        buttonRemoveGroup.setBounds(185, 220, 165, 20);
        ActionListener removeGroup = new RemoveGroup();
        buttonRemoveGroup.addActionListener(removeGroup);
        this.add(buttonRemoveGroup);

        scrollUsers.add(listUsers);
        scrollUsers.setViewportView(listUsers);
        scrollUsers.setBounds(360, 10, 210, 200);
        this.add(scrollUsers);
        buttonRemoveFromGroup.setBounds(395, 220, 175, 20);
        ActionListener removeFromGroup = new RemoveFromGroup();
        buttonRemoveFromGroup.addActionListener(removeFromGroup);
        this.add(buttonRemoveFromGroup);

        updateUsers();
        comboBoxUsers.setBounds(580, 10, 210, 20);
        this.add(comboBoxUsers);
        buttonAddUserToGroup.setBounds(580, 40, 210, 20);
        ActionListener addUserToGroup = new AddUserToGroup();
        buttonAddUserToGroup.addActionListener(addUserToGroup);
        this.add(buttonAddUserToGroup);

        updateGroupsList();

        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - this.getWidth())/2);
        int y = (int) ((dimension.getHeight() - this.getHeight())/2);
        this.setLocation(x, y);

        this.setVisible(true);
    }
}
