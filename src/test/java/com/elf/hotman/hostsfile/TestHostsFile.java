package com.elf.hotman.hostsfile;

import junit.framework.Assert;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * User: laichendong
 * Date: 12-5-16
 * Time: ÏÂÎç1:12
 */
public class TestHostsFile {
	@Test
	public void unCommentTest() throws FileNotFoundException {
		HostsFile hosts = HostsFile.getInstance();
		Assert.assertEquals("10.10.225.199 passport.360buy.net", hosts.unCommentLine("#10.10.225.199 passport.360buy.net"));
		Assert.assertEquals("10.10.225.199 passport.360buy.net", hosts.unCommentLine("##10.10.225.199 passport.360buy.net"));
		Assert.assertEquals(" 10.10.225.199 passport.360buy.net", hosts.unCommentLine("  ### 10.10.225.199 passport.360buy.net"));
		Assert.assertEquals("10.10.225.199 passport.360buy.net", hosts.unCommentLine("10.10.225.199 passport.360buy.net"));
		Assert.assertEquals("", hosts.unCommentLine("##"));
	}

	@Test
	public void isCommentLineTest() throws FileNotFoundException {
		HostsFile hosts = HostsFile.getInstance();
		Assert.assertTrue(hosts.isCommentLine("#10.10.225.199 passport.360buy.net"));
		Assert.assertTrue(hosts.isCommentLine("   #10.10.225.199 passport.360buy.net"));
		Assert.assertTrue(hosts.isCommentLine("   ##"));
		Assert.assertTrue(!hosts.isCommentLine("abc"));
		Assert.assertTrue(!hosts.isCommentLine("abc##"));
		Assert.assertTrue(!hosts.isCommentLine(""));
	}

	@Test
	public void backupHostsFileTest() throws IOException {
		HostsFile hosts = HostsFile.getInstance();
		hosts.backupHostsFile();
	}
}
