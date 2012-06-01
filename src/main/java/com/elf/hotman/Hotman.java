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
 * Time: 上午10:15
 */
public class Hotman extends JFrame {
	/**
	 * 窗口宽度
	 */
	public static final int W = 800;
	/**
	 * 窗口高度
	 */
	public static final int H = 600;
	/**
	 * 软件名称
	 */
	public static final String NAME = "elf hosts file manager";
	/**
	 * 版本
	 */
	private static final String VERSION = "v0.1";
	/**
	 * 作者
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
		// 基本设置
		super();
		this.setTitle(NAME + " " + VERSION + " by " + AUTHOR);
		this.setSize(W, H);
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation((int) (dimension.getWidth() / 2 - W / 2), (int) (dimension.getHeight() / 2 - H / 2));
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(false);

		// 域名列表
		this.domainList = new DomainList();
		JPanel domainListPanel = new JPanel();
		domainListPanel.add(new JScrollPane(domainList));
		this.domainList.addListSelectionListener(new DomainListListener());
		// ip列表
		this.ipList = new IpList();
		JPanel ipListPanel = new JPanel();
		ipListPanel.add(new JScrollPane(this.ipList));
		this.ipList.addMouseListener(new IpListMouseLinstener());

		// 打开hosts文件按钮
		JButton openBtn = new JButton("打开hosts文件");
		openBtn.addActionListener(new OpenBtnActionListener());
		//刷新按钮
		JButton refreshBtn = new JButton("刷新");
		refreshBtn.addActionListener(new RefreshBtnActionListener());
		//退出按钮
		JButton quiteBtn = new JButton("退出");
		quiteBtn.addActionListener(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});

		// 状态label
		this.statusLine = new JLabel();
		this.statusLine.setFont(new Font("微软雅黑", Font.BOLD, 14));
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
		// 设置界面look and feel
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		JFrame.setDefaultLookAndFeelDecorated(false);

		// 显示界面
		new Hotman();

		// 备份并转换文件格式
		HostsFile hostsFile = HostsFile.getInstance();
		if (hostsFile.isElfFormat()) { // 已经是转换过的格式 不做处理
			// slience is golden
		} else {
			try {
				// 备份hosts文件
				String bakFilename = hostsFile.backupHostsFile();

				// 重新写入格式化后的hosts文件
				hostsFile.writeToFile();

				// 提示用户操作完成
				JOptionPane.showMessageDialog(null, "首次运行。\n备份并转换hosts文件格式完成。\n备份文件路径：" + bakFilename, "备份并转换hosts文件格式", JOptionPane.INFORMATION_MESSAGE);
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(null, "备份并转换hosts文件格式失败。请联系 @laichendong", "备份并转换hosts文件格式", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**
	 * 域名列表选中监听器
	 */
	private class DomainListListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) {
			String selectedDomain = (String) domainList.getSelectedValue();
			String ip = "";
			if (selectedDomain != null) { // 如果是从选中到不选中状态，则不做任何处理
				ip = HostsFile.getInstance().getHosts().get(selectedDomain.trim());
				if (ip == null) { // 没有配置的域名。选中ip列表中的空白行
					ip = " ";
				}
				ipList.setSelectedValue(ip, true);// 选中对应的ip
			}
			// 更新状态栏
			updateStatusLine(selectedDomain, ip);
		}

	}

	/**
	 * 更新状态栏
	 *
	 * @param domain 域名
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
	 * ip列表鼠标事件监听器
	 */
	private class IpListMouseLinstener implements MouseListener {
		/**
		 * Invoked when the mouse button has been clicked (pressed
		 * and released) on a component.
		 */
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) { // 双击
				if (domainList.isSelectionEmpty()) { // 假如没有选域名，弹出提示
					JOptionPane.showMessageDialog(null, "请先选择域名", "配置host", JOptionPane.ERROR_MESSAGE);
				} else { // 修改/配置 hosts
					String selectedDomain = (String) domainList.getSelectedValue();
					String selectedIp = (String) ipList.getSelectedValue();
					HostsFile hostsFile = HostsFile.getInstance();
					hostsFile.setHost(selectedDomain, selectedIp);
					try {
						hostsFile.writeToFile();

						//更新状态栏
						updateStatusLine(selectedDomain, selectedIp);

					} catch (IOException e1) {
						JOptionPane.showMessageDialog(null, "写入hosts文件失败，请检查是否有其他程序占用hosts文件", "配置host", JOptionPane.ERROR_MESSAGE);
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
	 * "刷新"按钮事件监听器
	 */
	private class RefreshBtnActionListener implements ActionListener {
		/**
		 * Invoked when an action occurs.
		 */
		public void actionPerformed(ActionEvent e) {
			// 刷新hosts文件对象
			HostsFile hostsFile = HostsFile.getInstance();
			hostsFile.refresh();

			// 更新域名列表
			DefaultListModel domainListModel = new DefaultListModel();
			int j = 0;
			for (String domain : hostsFile.getDomains()) {
				domainListModel.add(j++, domain);
			}
			domainList.setModel(domainListModel);

			// 更新ip列表
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
	 * "打开host文件"按钮事件监听器
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
