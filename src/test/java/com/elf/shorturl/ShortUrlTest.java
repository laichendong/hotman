package com.elf.shorturl;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: laichendong
 * Date: 12-4-24
 * Time: ����11:07
 */
public class ShortUrlTest {

	/**
	 * ����ToShort��������ײ��
	 *
	 * @throws Exception e
	 */
	@Test
	public void testToShortCollision() throws Exception {
		String seedUrl = "http://www.qzhidao.com/";
		Set<String> us = new HashSet<String>();
		Set<String> urls = getUrls(seedUrl);
		us.addAll(urls);
//		for(String url : urls){
//			Set<String> level1 = getUrls(url);
//			us.addAll(level1);
//		}
		Set<String> shortedUrls = new HashSet<String>();
		long t = System.currentTimeMillis();
		for (String url : us) {
			shortedUrls.add(new ShortUrl().toShort(url));
		}
		t = System.currentTimeMillis() - t;
		System.out.println("url����:" + us.size());
		System.out.println("Hash�����:" + shortedUrls.size());
		System.out.printf("��ײ��%10.9f%%\n", (1 - (float) shortedUrls.size() / (float) us.size()) * 100);
		System.out.println("��ʱ��" + t + "ms");
	}

	/**
	 * ��ȡseedUrlҳ���������url
	 *
	 * @param seedUrl ����url
	 * @return ҳ�����url�б�
	 */
	private Set<String> getUrls(String seedUrl) {
		Set<String> urls = new HashSet<String>();
		Pattern p = Pattern.compile("^.*href=\"([^\"]*)\".*$");
		URL url = null;
		try {
			url = new URL(seedUrl);
			InputStream in = url.openStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line = reader.readLine();
			while (line != null) {
				Matcher m = p.matcher(line);
				if (m.matches()) {
					String u = m.group(1);
					if (u != null) {
						for(int i=0; i<1000; i++){
							urls.add(Math.random() + u + Math.random() + Math.random());
						}
					}
				}
				line = reader.readLine();
			}
			reader.close();
			in.close();
		} catch (IOException e) {
			//
		}
		return urls;
	}
}
