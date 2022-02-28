import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
// import java.lang.module.ModuleDescriptor.Builder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
// import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

// import javax.swing.border.Border;
// import javax.swing.border.EmptyBorder;
// import java.awt.Color;
import java.awt.Component;

class Server extends Thread {

    private JLabel fileNameLabel;
    private JFrame receiveFileFrame;

    public Server(JLabel fileNameLabel, JFrame receiveFileFrame) {
        this.fileNameLabel = fileNameLabel;
        this.receiveFileFrame = receiveFileFrame;
    }

    @Override
    public void run() {
        ServerSocket serverSocket = null;
        try {

            serverSocket = new ServerSocket(4444);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(receiveFileFrame, e, "Message", 0);
            try {
                serverSocket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        while (true) {
            try {
                Socket socket = serverSocket.accept();
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                int fileNameLength = dataInputStream.readInt();
                byte[] fileNameSize = new byte[fileNameLength];
                dataInputStream.readFully(fileNameSize, 0, fileNameSize.length);
                String fileName = new String(fileNameSize);
                fileNameLabel.setText(fileName);
                int fileContentLength = dataInputStream.readInt();
                byte[] fileContentSize = new byte[fileContentLength];
                dataInputStream.readFully(fileContentSize, 0, fileContentLength);
                File fileDownload = new File(fileName);
                FileOutputStream fileOutputStream = new FileOutputStream(fileDownload);
                fileOutputStream.write(fileContentSize);
                fileOutputStream.close();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(receiveFileFrame, e, "Message", 0);

            }
        }
    }
}

class App {

    public static File[] file = new File[1];

    public static void sendFile() {

        JFrame sendFileFrame;
        JPanel mainSendFilePanel, buttonSendFilePanel;
        JLabel fileNameLabel;
        JButton chooseFileButton, sendFileButton;

        sendFileFrame = new JFrame();
        mainSendFilePanel = new JPanel();
        buttonSendFilePanel = new JPanel();
        fileNameLabel = new JLabel();
        chooseFileButton = new JButton();
        sendFileButton = new JButton();

        sendFileFrame.setBounds(750, 300, 400, 200);
        sendFileFrame.setLayout(new BoxLayout(sendFileFrame.getContentPane(), BoxLayout.Y_AXIS));

        fileNameLabel.setText("Choose File");
        fileNameLabel.setFont(new Font("Serif", Font.BOLD, 20));
        fileNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        chooseFileButton.setText("Choose File");
        chooseFileButton.setSize(100, 50);
        chooseFileButton.setFont(new Font("Serif", Font.PLAIN, 15));

        sendFileButton.setText("Send File");
        sendFileButton.setSize(100, 50);
        sendFileButton.setFont(new Font("Serif", Font.PLAIN, 15));

        buttonSendFilePanel.add(chooseFileButton);
        buttonSendFilePanel.add(sendFileButton);

        mainSendFilePanel.add(fileNameLabel);
        mainSendFilePanel.add(Box.createHorizontalStrut(5));
        mainSendFilePanel.add(buttonSendFilePanel);
        mainSendFilePanel.setLayout(new BoxLayout(mainSendFilePanel, BoxLayout.Y_AXIS));

        sendFileFrame.add(mainSendFilePanel);

        sendFileFrame.setVisible(true);

        sendFileFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        chooseFileButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                // FileChooser
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Choose a file to send");

                // Condition for choose file
                if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {

                    // Store file into fileSend variable which is chosen by FileChooser
                    file[0] = fileChooser.getSelectedFile();
                    fileNameLabel.setText(file[0].getName());

                }
            }
        });

        sendFileButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                if (file[0] == null) {

                    JOptionPane.showMessageDialog(sendFileFrame, "Select File", "File", 0);

                } else {

                    try {

                        Socket socket = new Socket("localhost", 4444);

                        FileInputStream fileInputStream = new FileInputStream(file[0].getAbsolutePath());
                        // DataOutputStream for write data in another computer
                        DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

                        // Stored file name in fileName variable
                        String fileName = file[0].getName();

                        // Encode fileName in UTF-8
                        byte[] fileNameByte = fileName.getBytes();

                        // Create byte array
                        byte[] fileContentByte = new byte[(int) file[0].length()];

                        // It read file data
                        fileInputStream.read(fileContentByte);

                        // Write an int to the output stream
                        outputStream.writeInt(fileNameByte.length);

                        // It write bytes
                        outputStream.write(fileNameByte);

                        outputStream.writeInt(fileContentByte.length);
                        outputStream.write(fileContentByte);

                    } catch (Exception error) {

                        JOptionPane.showMessageDialog(sendFileFrame, error, "Message", 0);
                        error.printStackTrace();

                    }

                }
            }
        });

    }

    public static void receiveFile() {

        JFrame receiveFileFrame;
        JPanel receiveFilePanel;
        JLabel fileNameLabel, headerLabel;

        receiveFileFrame = new JFrame();
        receiveFilePanel = new JPanel();
        fileNameLabel = new JLabel();
        headerLabel = new JLabel();

        receiveFileFrame.setBounds(750, 300, 400, 200);
        receiveFileFrame.setLayout(new BoxLayout(receiveFileFrame.getContentPane(), BoxLayout.Y_AXIS));

        fileNameLabel.setFont(new Font("Serif", Font.PLAIN, 18));
        fileNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerLabel.setText("Received Files");
        headerLabel.setFont(new Font("Serif", Font.BOLD, 20));
        headerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        receiveFilePanel.add(fileNameLabel);

        receiveFileFrame.add(headerLabel);
        receiveFileFrame.add(receiveFilePanel);

        receiveFileFrame.setVisible(true);

        receiveFileFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Thread t = new Server(fileNameLabel, receiveFileFrame);
        t.start();

    }

    public static void main(String[] args) throws Exception {
        JFrame mainFrame = new JFrame();
        JButton send, receive;
        JPanel mainPanel, buttoPanel;
        JLabel heading;
        // Border border = BorderFactory.createLineBorder(Color.black);

        heading = new JLabel();
        send = new JButton();
        receive = new JButton();
        mainPanel = new JPanel();
        buttoPanel = new JPanel();

        mainFrame.setBounds(700, 250, 400, 200);
        mainFrame.setLayout(new BoxLayout(mainFrame.getContentPane(), BoxLayout.Y_AXIS));

        heading.setText("File Transfer");
        heading.setFont(new Font("Serif", Font.BOLD, 20));
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);

        send.setText("Send");
        send.setSize(100, 50);
        send.setFont(new Font("Serif", Font.PLAIN, 15));

        receive.setText("Receive");
        receive.setSize(100, 50);
        receive.setFont(new Font("Serif", Font.PLAIN, 15));

        buttoPanel.add(send);
        buttoPanel.add(receive);

        mainPanel.add(heading);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(Box.createHorizontalStrut(10));
        mainPanel.add(buttoPanel);

        mainFrame.add(mainPanel);

        mainFrame.setVisible(true);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        send.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                sendFile();

            }
        });

        receive.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                receiveFile();

            }
        });
    }
}
