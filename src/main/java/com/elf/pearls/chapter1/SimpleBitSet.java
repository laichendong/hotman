package com.elf.pearls.chapter1;

/**
 * 简单的bitset实现
 * User: laichendong
 * Date: 12-6-5
 * Time: 下午5:29
 */
public class SimpleBitSet {
	private static final int BITS_PER_WORD = 32; //  一个码字多少位
	private static final int SHIFT = 5;
	final char[] digits = {'0', '1'};

	private int[] a;  // bitset内部表示

	public SimpleBitSet() {
		this(32); // 默认容量 32位
	}

	public SimpleBitSet(int capacity) {
		this.a = new int[1 + ((capacity - 1) >> SHIFT)];
	}

	/**
	 * 在指定位上设置成true
	 *
	 * @param i index
	 */
	public void set(int i) {
		a[i >> SHIFT] |= 1 << (BITS_PER_WORD - 1 - i);
	}

	/**
	 * 清除指定位  设置成false
	 *
	 * @param i index
	 */
	public void clr(int i) {
		a[i >> SHIFT] &= ~(1 << (BITS_PER_WORD - 1 - i));
	}

	/**
	 * 检查指定位的设置情况
	 *
	 * @param i index
	 * @return true | false
	 */
	public boolean test(int i) {
		return 1 == ((a[i >> SHIFT] >> (BITS_PER_WORD - 1 - i)) & 1);
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		for (int aa : a) {
			buf.append(toBinaryStr(aa));
			buf.append(" ");
		}
		buf.deleteCharAt(buf.length() - 1);
		return buf.toString();
	}

	/**
	 * int类型转换成32位二进制字符按串
	 *
	 * @param i int
	 * @return 二进制字符串表示
	 */
	private String toBinaryStr(int i) {
		char[] buf = new char[32];
		int charPos = 32;
		int radix = 1 << 1;
		int mask = radix - 1;
		do {
			if (i != 0) {
				buf[--charPos] = digits[i & mask];
				i >>>= 1;
				continue;
			}
			buf[--charPos] = '0';
		} while (charPos > 0);
		return new String(buf);
	}
}
