package com.elf.hotman;

import com.elf.hotman.hostsfile.HostsFile;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

/**
 * User: laichendong
 * Date: 12-5-16
 * Time: ����10:15
 */
public class Hotman extends JFrame {
	/**
	 * ���ڿ��
	 */
	public static final int W = 800;
	/**
	 * ���ڸ߶�
	 */
	public static final int H = 600;
	/**
	 * �������
	 */
	public static final String NAME = "elf hosts file manager";
	/**
	 * �汾
	 */
	private static final String VERSION = "v0.1";
	/**
	 * ����
	 */
	private static final String AUTHOR = "dongdong";

	public static final int LIST_VISIABLE_ROW_COUNT = 28;
	public static final int LIST_FIXED_CELL_WIDTH = 320;

	JList domainList;
	JList ipList;
	JLabel statusLine;

	/**
	 * Creates a new, initially invisible <code>Frame</code> with the
	 * specified title.
	 * <p/>
	 * This constructor sets the component's locale property to the value
	 * returned by <code>JComponent.getDefaultLocale</code>.
	 *
	 * @throws java.awt.HeadlessException if GraphicsEnvironment.isHeadless()
	 *                                    returns true.
	 * @see java.awt.GraphicsEnvironment#isHeadless
	 * @see java.awt.Component#setSize
	 * @see java.awt.Component#setVisible
	 * @see javax.swing.JComponent#getDefaultLocale
	 */
	public Hotman() throws HeadlessException {
		// ��������
		super();
		this.setTitle(NAME + " " + VERSION + " by " + AUTHOR);
		this.setSize(W, H);
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation((int) (dimension.getWidth() / 2 - W / 2), (int) (dimension.getHeight() / 2 - H / 2));
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(false);

		// �����б�
		this.domainList = new DomainList();
		JPanel domainListPanel = new JPanel();
		domainListPanel.add(new JScrollPane(domainList));
		this.domainList.addListSelectionListener(new DomainListListener());
		// ip�б�
		this.ipList = new IpList();
		JPanel ipListPanel = new JPanel();
		ipListPanel.add(new JScrollPane(this.ipList));
		this.ipList.addMouseListener(new IpListMouseLinstener());

		// ��hosts�ļ���ť
		JButton openBtn = new JButton("��hosts�ļ�");
		openBtn.addActionListener(new OpenBtnActionListener());
		//ˢ�°�ť
		JButton refreshBtn = new JButton("ˢ��");
		refreshBtn.addActionListener(new RefreshBtnActionListener());
		//�˳���ť
		JButton quiteBtn = new JButton("�˳�");
		quiteBtn.addActionListener(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});

		// ״̬label
		this.statusLine = new JLabel();
		this.statusLine.setFont(new Font("΢���ź�", Font.BOLD, 14));
		this.statusLine.setForeground(new Color(255, 0, 0));

		// listPanel
		JPanel listPanel = new JPanel();
		listPanel.add(domainListPanel);
		listPanel.add(ipListPanel);
		// btnPanel
		JPanel btnPanel = new JPanel();
		btnPanel.setLayout(new FlowLayout());
		btnPanel.add(refreshBtn);
		btnPanel.add(openBtn);
		btnPanel.add(quiteBtn);
		// statusPanel
		JPanel statusPanel = new JPanel();
		statusPanel.setLayout(new FlowLayout());
		statusPanel.add(this.statusLine);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(listPanel);
		panel.add(statusPanel);
		panel.add(btnPanel);

		this.add(panel);
		this.setVisible(true);
	}

	public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, IllegalAccessException, InstantiationException {
		// ���ý���look and feel
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		JFrame.setDefaultLookAndFeelDecorated(false);

		// ��ʾ����
		new Hotman();

		// ���ݲ�ת���ļ���ʽ
		HostsFile hostsFile = HostsFile.getInstance();
		if (hostsFile.isElfFormat()) { // �Ѿ���ת�����ĸ�ʽ ��������
			// slience is golden
		} else {
			try {
				// ����hosts�ļ�
				String bakFilename = hostsFile.backupHostsFile();

				// ����д���ʽ�����hosts�ļ�
				hostsFile.writeToFile();

				// ��ʾ�û��������
				JOptionPane.showMessageDialog(null, "�״����С�\n���ݲ�ת��hosts�ļ���ʽ��ɡ�\n�����ļ�·����" + bakFilename, "���ݲ�ת��hosts�ļ���ʽ", JOptionPane.INFORMATION_MESSAGE);
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(null, "���ݲ�ת��hosts�ļ���ʽʧ�ܡ�����ϵ @laichendong", "���ݲ�ת��hosts�ļ���ʽ", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**
	 * �����б�ѡ�м�����
	 */
	private class DomainListListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) {
			String selectedDomain = (String) domainList.getSelectedValue();
			String ip = "";
			if (selectedDomain != null) { // ����Ǵ�ѡ�е���ѡ��״̬�������κδ���
				ip = HostsFile.getInstance().getHosts().get(selectedDomain.trim());
				if (ip == null) { // û�����õ�������ѡ��ip�б��еĿհ���
					ip = " ";
				}
				ipList.setSelectedValue(ip, true);// ѡ�ж�Ӧ��ip
			}
			// ����״̬��
			updateStatusLine(selectedDomain, ip);
		}

	}

	/**
	 * ����״̬��
	 *
	 * @param domain ����
	 * @param ip     ip
	 */
	private void updateStatusLine(String domain, String ip) {
		if (domain != null && !domain.isEmpty()) {
			statusLine.setText(domain + " -> " + ip);
		} else {
			statusLine.setText("");
		}
	}

	/**
	 * ip�б�����¼�������
	 */
	private class IpListMouseLinstener implements MouseListener {
		/**
		 * Invoked when the mouse button has been clicked (pressed
		 * and released) on a component.
		 */
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) { // ˫��
				if (domainList.isSelectionEmpty()) { // ����û��ѡ������������ʾ
					JOptionPane.showMessageDialog(null, "����ѡ������", "����host", JOptionPane.ERROR_MESSAGE);
				} else { // �޸�/���� hosts
					String selectedDomain = (String) domainList.getSelectedValue();
					String selectedIp = (String) ipList.getSelectedValue();
					HostsFile hostsFile = HostsFile.getInstance();
					hostsFile.setHost(selectedDomain, selectedIp);
					try {
						hostsFile.writeToFile();

						//����״̬��
						updateStatusLine(selectedDomain, selectedIp);

					} catch (IOException e1) {
						JOptionPane.showMessageDialog(null, "д��hosts�ļ�ʧ�ܣ������Ƿ�����������ռ��hosts�ļ�", "����host", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}

		/**
		 * Invoked when a mouse button has been pressed on a component.
		 */
		public void mousePressed(MouseEvent e) {
		}

		/**
		 * Invoked when a mouse button has been released on a component.
		 */
		public void mouseReleased(MouseEvent e) {
		}

		/**
		 * Invoked when the mouse enters a component.
		 */
		public void mouseEntered(MouseEvent e) {
		}

		/**
		 * Invoked when the mouse exits a component.
		 */
		public void mouseExited(MouseEvent e) {
		}
	}

	/**
	 * "ˢ��"��ť�¼�������
	 */
	private class RefreshBtnActionListener implements ActionListener {
		/**
		 * Invoked when an action occurs.
		 */
		public void actionPerformed(ActionEvent e) {
			// ˢ��hosts�ļ�����
			HostsFile hostsFile = HostsFile.getInstance();
			hostsFile.refresh();

			// ���������б�
			DefaultListModel domainListModel = new DefaultListModel();
			int j = 0;
			for (String domain : hostsFile.getDomains()) {
				domainListModel.add(j++, domain);
			}
			domainList.setModel(domainListModel);

			// ����ip�б�
			DefaultListModel ipListModel = new DefaultListModel();
			int i = 0;
			for (String ip : hostsFile.getIps()) {
				ipListModel.add(i++, ip);
			}
			ipList.setModel(ipListModel);

			updateStatusLine("", "");
		}
	}

	/**
	 * "��host�ļ�"��ť�¼�������
	 */
	private class OpenBtnActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String cmd = "notepad " + HostsFile.HOST_FILE_PATH;
			try {
				Runtime.getRuntime().exec(cmd);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
}
