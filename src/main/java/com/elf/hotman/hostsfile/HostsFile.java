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
 * Time: 上午11:06
 */
public class HostsFile {

	public static final String HOST_FILE_NAME = "hosts";

	public static final String HOST_FILE_DIR = "C:\\Windows\\System32\\drivers\\etc";
	/**
	 * host文件路径
	 */
	public static final String HOST_FILE_PATH = HOST_FILE_DIR + File.separator + HOST_FILE_NAME;

	private static final String HOSTS_BAK_DIR_NAME = "hosts_bak";
	/**
	 * 注释行正则表达式
	 */
	public static final Pattern COMMENT_LINE_PATTERN = Pattern.compile("^(\\s*#+)(.*)$");
	/**
	 * "有效行"正则表达式
	 */
	public static final Pattern VALID_LINE_PATTERN = Pattern.compile("^(\\s*#*\\s*)(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})(\\s+)([\\w\\.\\s-]+)$");
	/**
	 * 注释行 前缀
	 */
	private static final String COMMENT_LINE_PREFIX = "# ";
	/**
	 * 处理过的文件格式标记
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
	 * 单例实例
	 */
	private static final HostsFile instance = new HostsFile();

	/**
	 * 域名集合
	 */
	private TreeSet<String> domains;
	/**
	 * ip集合
	 */
	private TreeSet<String> ips;
	/**
	 * 有效的hosts配置
	 */
	private Map<String, String> hosts;
	/**
	 * 忽略处理的行
	 */
	private List<String> ignoreLines;

	/**
	 * 文件格式表示，true 表示是已处理过的格式
	 */
//	private boolean elfFormat;
	private HostsFile() {
		init();
	}

	public void refresh() {
		init();
	}

	public void init() {
		// 读取和解析host文件
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
	 * 获取实例
	 *
	 * @return 实例
	 */
	public static HostsFile getInstance() {
		return instance;
	}

	/**
	 * 解析elf格式的host文件
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
	 * 解析普通格式的hosts文件
	 */
	private void parseNomalFile() {
		BufferedReader reader = openHostsFileReader();
		String line = null;
		boolean inIgnoreLinesBlock = false; // 是否在ignore lines块中的状态标识
		try {
			line = reader.readLine();
			while (line != null) {
				// 检测到ignore lines块开始标签，把状态标识设为真，跳过本行
				if (TAG_IGNORE_LINES_START.equals(line)) {
					inIgnoreLinesBlock = true;
				} else {
					if (inIgnoreLinesBlock) {
						// 在ignore lines块中，除了检测是否退出块以外，直接把行加入ignoreLines 不做处理
						if (TAG_IGNORE_LINES_END.equals(line)) {
							inIgnoreLinesBlock = false;
						} else {
							this.ignoreLines.add(line);
						}
					} else { // 不在ignore lines块中 按行处理
						if (isValiedLine(line)) { // 只处理有效的行
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
	 * 写入hosts文件
	 *
	 * @throws IOException “”
	 */
	public void writeToFile() throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(HOST_FILE_PATH)));

		// 文件头
		writer.write(TAG_FILE_MARKER);
		writer.newLine();
		writer.write(COMMENT_LINE_PREFIX + "this file is generated by " + Hotman.NAME);
		writer.newLine();

		// 有效hosts
		writer.newLine();
		writer.write(TAG_HOST_CONFIG_START);
		writer.newLine();
		for (String domain : hosts.keySet()) {
			writer.write(String.format("%1$-20s%2$s", hosts.get(domain), domain));
			writer.newLine();
		}
		writer.write(TAG_HOST_CONFIG_END);
		writer.newLine();

		// 备选域名列表
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

		// 备选ip列表
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

		// 忽略处理的行
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
	 * 备份host文件
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
	 * 安静的关闭资源
	 * @param closeable 可关闭的资源
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
	 * 截取本行的ip地址
	 *
	 * @param line 行
	 * @return ip
	 */
	private String ipInLine(String line) {
		line = unCommentLine(line);
		return line.split("\\s+")[0];
	}

	/**
	 * 找出该行的所有域名集合
	 *
	 * @param line 行
	 * @return 该行的所有域名集合
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
	 * 判断一行是否是“有效的”。有效的 表示 该行的内容是 IP + 域名。而不管是否被注释，都算是有效的
	 *
	 * @param line 行
	 * @return true | false
	 */
	public boolean isValiedLine(String line) {
		return VALID_LINE_PATTERN.matcher(line).find();
	}

	/**
	 * 对一行取消注释
	 *
	 * @param line 行
	 * @return 取消注释后的行，如果本身不是注释行,则返回行本身, 如果去除行开头的空格和#号后啥也不剩，则返回空字符串
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
	 * 注释某一行
	 *
	 * @param line 行
	 * @return 如果是注释行，返回trim后的行，如果不是注释行，trim后再开头加上“# ”
	 */
	public String commentLine(String line) {
		if (isCommentLine(line)) {
			return line.trim();
		} else {
			return COMMENT_LINE_PREFIX + line.trim();
		}
	}

	/**
	 * 判断一行是不是注释
	 *
	 * @param line 行
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
	 * 关闭hosts文件reader
	 *
	 * @param reader 待关闭的reader
	 */
	private void closeHostsFileReader(BufferedReader reader) {
		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 打开hosts文件reader
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
	 * 检查hosts文件是否是elf（处理过的）格式.
	 *
	 * @return 当且仅当 文件第一行是 @link={com.elf.hotman.hostsfile.HostsFile#FILE_MARKER} 时返回true
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
	 * 根据当前行内容和上一行的行类型 决定当前行的行类型
	 *
	 * @param line        行
	 * @param oldLineType 上一行的行类型
	 * @return 当前行的行类型
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
