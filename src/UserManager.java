import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class UserManager extends JFrame
{
    JScrollPane scrollUsers = new JScrollPane();
    DefaultListModel listModel = new DefaultListModel<>();
    JList listUsers = new JList(listModel);

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
    UserManager()
    {
        super("User manager");
        this.setResizable(false);
        setSize(640, 480);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setLayout(null);

        scrollUsers.add(listUsers);
        scrollUsers.setViewportView(listUsers);
        scrollUsers.setBounds(10, 10, 200, 200);
        this.add(scrollUsers);

        updateUserList();

        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - this.getWidth())/2);
        int y = (int) ((dimension.getHeight() - this.getHeight())/2);
        this.setLocation(x, y);

        this.setVisible(true);
    }
}
