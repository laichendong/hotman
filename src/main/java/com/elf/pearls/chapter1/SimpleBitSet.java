package com.elf.pearls.chapter1;

/**
 * �򵥵�bitsetʵ��
 * User: laichendong
 * Date: 12-6-5
 * Time: ����5:29
 */
public class SimpleBitSet {
	private static final int BITS_PER_WORD = 32; //  һ�����ֶ���λ
	private static final int SHIFT = 5;
	final char[] digits = {'0', '1'};

	private int[] a;  // bitset�ڲ���ʾ

	public SimpleBitSet() {
		this(32); // Ĭ������ 32λ
	}

	public SimpleBitSet(int capacity) {
		this.a = new int[1 + ((capacity - 1) >> SHIFT)];
	}

	/**
	 * ��ָ��λ�����ó�true
	 *
	 * @param i index
	 */
	public void set(int i) {
		a[i >> SHIFT] |= 1 << (BITS_PER_WORD - 1 - i);
	}

	/**
	 * ���ָ��λ  ���ó�false
	 *
	 * @param i index
	 */
	public void clr(int i) {
		a[i >> SHIFT] &= ~(1 << (BITS_PER_WORD - 1 - i));
	}

	/**
	 * ���ָ��λ���������
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
	 * int����ת����32λ�������ַ�����
	 *
	 * @param i int
	 * @return �������ַ�����ʾ
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
