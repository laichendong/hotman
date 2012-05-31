package com.elf.hotman.hostsfile;

import com.elf.hotman.Hotman;

import java.io.*;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: laichendong
 * Date: 12-5-16
 * Time: ����11:06
 */
public class HostsFile {

	public static final String HOST_FILE_NAME = "hosts";

	public static final String HOST_FILE_DIR = "C:\\Windows\\System32\\drivers\\etc";
	/**
	 * host�ļ�·��
	 */
	public static final String HOST_FILE_PATH = HOST_FILE_DIR + File.separator + HOST_FILE_NAME;

	private static final String HOSTS_BAK_DIR_NAME = "hosts_bak";
	/**
	 * ע����������ʽ
	 */
	public static final Pattern COMMENT_LINE_PATTERN = Pattern.compile("^(\\s*#+)(.*)$");
	/**
	 * "��Ч��"������ʽ
	 */
	public static final Pattern VALID_LINE_PATTERN = Pattern.compile("^(\\s*#*\\s*)(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})(\\s+)([\\w\\.\\s-]+)$");
	/**
	 * ע���� ǰ׺
	 */
	private static final String COMMENT_LINE_PREFIX = "# ";
	/**
	 * ��������ļ���ʽ���
	 */
	private static final String TAG_FILE_MARKER = "##--elf hosts file--##";
	private static final String TAG_HOST_CONFIG_START = "##--host config start--##";
	private static final String TAG_HOST_CONFIG_END = "##--host config end--##";
	private static final String TAG_SPARE_DOMAINS_START = "##--spare domains start--##";
	private static final String TAG_SPARE_DOMAINS_END = "##--spare domains end--##";
	private static final String TAG_SPARE_IPS_START = "##--spare ips start--##";
	private static final String TAG_SPARE_IPS_END = "##--spare ips end--##";
	private static final String TAG_IGNORE_LINES_START = "##--ignore lines start--##";
	private static final String TAG_IGNORE_LINES_END = "##--ignore lines end--##";

	/**
	 * ����ʵ��
	 */
	private static final HostsFile instance = new HostsFile();

	/**
	 * ��������
	 */
	private TreeSet<String> domains;
	/**
	 * ip����
	 */
	private TreeSet<String> ips;
	/**
	 * ��Ч��hosts����
	 */
	private Map<String, String> hosts;
	/**
	 * ���Դ������
	 */
	private List<String> ignoreLines;

	/**
	 * �ļ���ʽ��ʾ��true ��ʾ���Ѵ�����ĸ�ʽ
	 */
//	private boolean elfFormat;
	private HostsFile() {
		init();
	}

	public void refresh() {
		init();
	}

	public void init() {
		// ��ȡ�ͽ���host�ļ�
		this.domains = new TreeSet<String>(new Comparator<String>() {
			public int compare(String o1, String o2) {
				return new StringBuffer(o2).reverse().toString().compareTo(new StringBuilder(o1).reverse().toString());
			}
		});
		this.ips = new TreeSet<String>();
		this.ips.add(" ");
		this.hosts = new TreeMap<String, String>();
		this.ignoreLines = new ArrayList<String>();
		if (this.isElfFormat()) {
			parseElfFile();
		} else {
			parseNomalFile();
		}
	}

	/**
	 * ��ȡʵ��
	 *
	 * @return ʵ��
	 */
	public static HostsFile getInstance() {
		return instance;
	}

	/**
	 * ����elf��ʽ��host�ļ�
	 */
	private void parseElfFile() {
		BufferedReader reader = openHostsFileReader();

		String line = null;
		try {
			line = reader.readLine();
			LineType lineType = LineType.NORMAL_LINE;
			while (line != null) {
				lineType = determineLineType(line, lineType);
				switch (lineType) {
					case HOST_CONFIG:
						String ip = ipInLine(line);
						this.ips.add(ip.trim());
						for (String domain : domainsInLine(line)) {
							this.hosts.put(domain, ip);
							this.domains.add(domain.trim());
						}
						break;
					case SPARE_DOMAIN:
						this.domains.add(unCommentLine(line));
						break;
					case SPARE_IP:
						this.ips.add(unCommentLine(line));
						break;
					case IGNORE_LINE:
						this.ignoreLines.add(line);
						break;
					default:
						break;
				}
				line = reader.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeHostsFileReader(reader);
		}
	}

	/**
	 * ������ͨ��ʽ��hosts�ļ�
	 */
	private void parseNomalFile() {
		BufferedReader reader = openHostsFileReader();
		String line = null;
		boolean inIgnoreLinesBlock = false; // �Ƿ���ignore lines���е�״̬��ʶ
		try {
			line = reader.readLine();
			while (line != null) {
				// ��⵽ignore lines�鿪ʼ��ǩ����״̬��ʶ��Ϊ�棬��������
				if (TAG_IGNORE_LINES_START.equals(line)) {
					inIgnoreLinesBlock = true;
				} else {
					if (inIgnoreLinesBlock) {
						// ��ignore lines���У����˼���Ƿ��˳������⣬ֱ�Ӱ��м���ignoreLines ��������
						if (TAG_IGNORE_LINES_END.equals(line)) {
							inIgnoreLinesBlock = false;
						} else {
							this.ignoreLines.add(line);
						}
					} else { // ����ignore lines���� ���д���
						if (isValiedLine(line)) { // ֻ������Ч����
							Set<String> domainsInLine = domainsInLine(line);
							String ip = ipInLine(line);
							this.domains.addAll(domainsInLine);
							this.ips.add(ip);
							if (!isCommentLine(line)) {
								for (String domain : domainsInLine) {
									this.hosts.put(domain, ip);
								}
							}
						}
					}
				}
				line = reader.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeHostsFileReader(reader);
		}
	}

	/**
	 * д��hosts�ļ�
	 *
	 * @throws IOException ����
	 */
	public void writeToFile() throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(HOST_FILE_PATH)));

		// �ļ�ͷ
		writer.write(TAG_FILE_MARKER);
		writer.newLine();
		writer.write(COMMENT_LINE_PREFIX + "this file is generated by " + Hotman.NAME);
		writer.newLine();

		// ��Чhosts
		writer.newLine();
		writer.write(TAG_HOST_CONFIG_START);
		writer.newLine();
		for (String domain : hosts.keySet()) {
			writer.write(String.format("%1$-20s%2$s", hosts.get(domain), domain));
			writer.newLine();
		}
		writer.write(TAG_HOST_CONFIG_END);
		writer.newLine();

		// ��ѡ�����б�
		writer.newLine();
		writer.write(TAG_SPARE_DOMAINS_START);
		writer.newLine();
		for (String domain : domains) {
			if (!domain.trim().isEmpty()) {
				writer.write(COMMENT_LINE_PREFIX + domain);
				writer.newLine();
			}
		}
		writer.write(TAG_SPARE_DOMAINS_END);
		writer.newLine();

		// ��ѡip�б�
		writer.newLine();
		writer.write(TAG_SPARE_IPS_START);
		writer.newLine();
		for (String ip : ips) {
			if (!ip.trim().isEmpty()) {
				writer.write(COMMENT_LINE_PREFIX + ip);
				writer.newLine();
			}
		}
		writer.write(TAG_SPARE_IPS_END);
		writer.newLine();

		// ���Դ������
		writer.newLine();
		writer.write(TAG_IGNORE_LINES_START);
		writer.newLine();
		for (String line : ignoreLines) {
			writer.write(line);
			writer.newLine();
		}
		writer.write(TAG_IGNORE_LINES_END);
		writer.newLine();

		writer.close();
	}


	/**
	 * ����host�ļ�
	 */
	public String backupHostsFile() throws IOException {
		String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		String bakFilePath = HOST_FILE_DIR + File.separator + HOSTS_BAK_DIR_NAME + File.separator + HOST_FILE_NAME + "." + timestamp + ".bak";
		FileInputStream fis = null;
        FileOutputStream fos = null;
        FileChannel input = null;
        FileChannel output = null;
        try {
            fis = new FileInputStream(new File(HOST_FILE_PATH));
            fos = new FileOutputStream(new File(bakFilePath));
            input  = fis.getChannel();
            output = fos.getChannel();
            long size = input.size();
            long pos = 0;
            long count = 0;
            while (pos < size) {
                count = (size - pos) > 50*1024*1024 ? 50*1024*1024 : (size - pos);
                pos += output.transferFrom(input, pos, count);
            }
        } finally {
            closeQuietly(output);
            closeQuietly(fos);
            closeQuietly(input);
            closeQuietly(fis);
        }
		return bakFilePath;
	}

	/**
	 * �����Ĺر���Դ
	 * @param closeable �ɹرյ���Դ
	 */
	private static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }


	/**
	 * ��ȡ���е�ip��ַ
	 *
	 * @param line ��
	 * @return ip
	 */
	private String ipInLine(String line) {
		line = unCommentLine(line);
		return line.split("\\s+")[0];
	}

	/**
	 * �ҳ����е�������������
	 *
	 * @param line ��
	 * @return ���е�������������
	 */
	private Set<String> domainsInLine(String line) {
		Set<String> domainsInLine = new TreeSet<String>();
		String[] splited = line.split("\\s+");
		for (int i = 1; i < splited.length; i++) {
			domainsInLine.add(splited[i]);
		}
		return domainsInLine;
	}

	/**
	 * �ж�һ���Ƿ��ǡ���Ч�ġ�����Ч�� ��ʾ ���е������� IP + �������������Ƿ�ע�ͣ���������Ч��
	 *
	 * @param line ��
	 * @return true | false
	 */
	public boolean isValiedLine(String line) {
		return VALID_LINE_PATTERN.matcher(line).find();
	}

	/**
	 * ��һ��ȡ��ע��
	 *
	 * @param line ��
	 * @return ȡ��ע�ͺ���У����������ע����,�򷵻��б���, ���ȥ���п�ͷ�Ŀո��#�ź�ɶҲ��ʣ���򷵻ؿ��ַ���
	 */
	public String unCommentLine(String line) {
		if (isCommentLine(line)) {
			Matcher m = COMMENT_LINE_PATTERN.matcher(line);
			m.find();
			return (m.group(2) == null) ? "" : m.group(2).trim();
		} else {
			return line.trim();
		}
	}

	/**
	 * ע��ĳһ��
	 *
	 * @param line ��
	 * @return �����ע���У�����trim����У��������ע���У�trim���ٿ�ͷ���ϡ�# ��
	 */
	public String commentLine(String line) {
		if (isCommentLine(line)) {
			return line.trim();
		} else {
			return COMMENT_LINE_PREFIX + line.trim();
		}
	}

	/**
	 * �ж�һ���ǲ���ע��
	 *
	 * @param line ��
	 * @return true | false
	 */
	public boolean isCommentLine(String line) {
		return COMMENT_LINE_PATTERN.matcher(line).find();
	}

	public TreeSet<String> getDomains() {
		return domains;
	}

	public Map<String, String> getHosts() {
		return hosts;
	}

	public TreeSet<String> getIps() {
		return ips;
	}

	/**
	 * �ر�hosts�ļ�reader
	 *
	 * @param reader ���رյ�reader
	 */
	private void closeHostsFileReader(BufferedReader reader) {
		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ��hosts�ļ�reader
	 *
	 * @return reader
	 */
	private BufferedReader openHostsFileReader() {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(new File(HOST_FILE_PATH)));
		} catch (FileNotFoundException e) {
			System.exit(1);
		}
		return reader;
	}

	/**
	 * ���hosts�ļ��Ƿ���elf��������ģ���ʽ.
	 *
	 * @return ���ҽ��� �ļ���һ���� @link={com.elf.hotman.hostsfile.HostsFile#FILE_MARKER} ʱ����true
	 */
	public boolean isElfFormat() {
		BufferedReader reader = openHostsFileReader();
		String line = null;
		try {
			line = reader.readLine();
		} catch (IOException e) {
			System.exit(1);
		} finally {
			closeHostsFileReader(reader);
		}
		return TAG_FILE_MARKER.equals(line);
	}

	/**
	 * ���ݵ�ǰ�����ݺ���һ�е������� ������ǰ�е�������
	 *
	 * @param line        ��
	 * @param oldLineType ��һ�е�������
	 * @return ��ǰ�е�������
	 */
	private LineType determineLineType(String line, LineType oldLineType) {
		LineType newLineType;
		if (TAG_FILE_MARKER.equals(line)) {
			newLineType = LineType.FILE_MARKER;
		} else if (TAG_HOST_CONFIG_START.equals(line)) {
			newLineType = LineType.HOST_CONFIG_START;
		} else if (TAG_HOST_CONFIG_END.equals(line)) {
			newLineType = LineType.HOST_CONFIG_END;
		} else if (TAG_SPARE_DOMAINS_START.equals(line)) {
			newLineType = LineType.SPARE_DOMAINS_START;
		} else if (TAG_SPARE_DOMAINS_END.equals(line)) {
			newLineType = LineType.SPARE_DOMAINS_END;
		} else if (TAG_SPARE_IPS_START.equals(line)) {
			newLineType = LineType.SPARE_IPS_START;
		} else if (TAG_SPARE_IPS_END.equals(line)) {
			newLineType = LineType.SPARE_IPS_END;
		} else if (TAG_IGNORE_LINES_START.equals(line)) {
			newLineType = LineType.IGNORE_LINES_START;
		} else if (TAG_IGNORE_LINES_END.equals(line)) {
			newLineType = LineType.IGNORE_LINES_END;
		} else {
			switch (oldLineType) {
				case HOST_CONFIG_START:
				case HOST_CONFIG:
					newLineType = LineType.HOST_CONFIG;
					break;
				case SPARE_DOMAINS_START:
				case SPARE_DOMAIN:
					newLineType = LineType.SPARE_DOMAIN;
					break;
				case SPARE_IPS_START:
				case SPARE_IP:
					newLineType = LineType.SPARE_IP;
					break;
				case IGNORE_LINES_START:
				case IGNORE_LINE:
					newLineType = LineType.IGNORE_LINE;
					break;
				default:
					newLineType = LineType.NORMAL_LINE;
					break;
			}
		}
		return newLineType;
	}

	public void setHost(String selectedDomain, String selectedIp) {
		this.hosts.put(selectedDomain, selectedIp);
	}
}
