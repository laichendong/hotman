package com.elf.pearls.chapter1;

import org.junit.Test;

import java.io.*;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;

/**
 * User: laichendong
 * Date: 12-6-4
 * Time: ����8:04
 */
public class Chapter1 {
	private static final String FILE_NAME = "integerFile.dat";
	private static final int FILE_SIZE = 1000000;
	private static final int MAX_NUMBER = 10000000;
	Random r = new Random();

	@Test
	public void mkFile() {
		long t = System.currentTimeMillis();
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(FILE_NAME)));
			int[] seed = new int[MAX_NUMBER];
			int i = 0;
			do {
				seed[i] = i++;
			} while (i < MAX_NUMBER);
			for (int j = 0; j < FILE_SIZE; j++) {
				swap(seed, j, random(j, FILE_SIZE));
				writer.write(String.valueOf(seed[j]));
				writer.newLine();
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.printf("mkFile, time spend : %1d ms", System.currentTimeMillis() - t);
	}

	private void swap(int[] seed, int i, int j) {
		int t = seed[i];
		seed[i] = seed[j];
		seed[j] = t;
	}

	/**
	 * left right֮���һ�������
	 *
	 * @param left  l
	 * @param right r
	 * @return [left, right)
	 */
	private int random(int left, int right) {
		return left + r.nextInt(right - left);
	}

	@Test
	public void libSort() {
		long t = System.currentTimeMillis();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(FILE_NAME)));
			int[] ints = new int[FILE_SIZE];
			int i = 0;
			String line;
			do {
				line = reader.readLine();
				if (line != null) {
					ints[i++] = Integer.parseInt(line);
				}
			} while (line != null);
			Arrays.sort(ints);
//			System.out.println(Arrays.toString(ints));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.printf("java lib qsort, time spend : %1d ms", System.currentTimeMillis() - t);
	}

	@Test
	public void bitSort() {
		long t = System.currentTimeMillis();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(FILE_NAME)));
			BitSet bitSet = new BitSet(FILE_SIZE);
			String line;
			do {
				line = reader.readLine();
				if (line != null) {
					bitSet.set(Integer.parseInt(line));
				}
			} while (line != null);

//			for (int i=0; i<FILE_SIZE; i++){
//				if(bitSet.get(i)){
//					System.out.println(i);
//				}
//			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.printf("bit sort, time spend : %1d ms", System.currentTimeMillis() - t);
	}

	@Test
	public void bitSortWithSimpleBitSet() {
		long t = System.currentTimeMillis();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(FILE_NAME)));
			SimpleBitSet simpleBitSet = new SimpleBitSet(FILE_SIZE);
			String line;
			do {
				line = reader.readLine();
				if (line != null) {
					simpleBitSet.set(Integer.parseInt(line));
				}
			} while (line != null);

//			for (int i=0; i<FILE_SIZE; i++){
//				if(simpleBitSet.test(i)){
//					System.out.println(i);
//				}
//			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.printf("bit sort with simple bit set, time spend : %1d ms", System.currentTimeMillis() - t);
	}

	/**
	 * �������ظ�����ʱ������
	 */
	@Test
	public void testByteSort(){
		long t = System.currentTimeMillis();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(FILE_NAME)));
			byte[] bytes = new byte[FILE_SIZE];
			String line;
			do {
				line = reader.readLine();
				if (line != null) {
					bytes[Integer.parseInt(line)] += 49;
				}
			} while (line != null);

//			System.out.println(new String(bytes));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.printf("bit sort with simple bit set, time spend : %1d ms", System.currentTimeMillis() - t);
	}

	@Test
	public void testSimpleBitSet() {
		SimpleBitSet simpleBitSet = new SimpleBitSet();
		System.out.println(simpleBitSet.toString());
		simpleBitSet.set(7);
		simpleBitSet.set(3);
		simpleBitSet.clr(7);
		System.out.println(simpleBitSet.toString());
		System.out.println(simpleBitSet.test(7));
		System.out.println(simpleBitSet.test(3));
	}


	@Test
	public void testBasicTypes() {
		// byte
		System.out.println("�������ͣ�byte ������λ����" + Byte.SIZE);
		System.out.println("��װ�ࣺjava.lang.Byte");
		System.out.println("��Сֵ��Byte.MIN_VALUE=" + Byte.MIN_VALUE);
		System.out.println("���ֵ��Byte.MAX_VALUE=" + Byte.MAX_VALUE);
		System.out.println();

		// short
		System.out.println("�������ͣ�short ������λ����" + Short.SIZE);
		System.out.println("��װ�ࣺjava.lang.Short");
		System.out.println("��Сֵ��Short.MIN_VALUE=" + Short.MIN_VALUE);
		System.out.println("���ֵ��Short.MAX_VALUE=" + Short.MAX_VALUE);
		System.out.println();

		// int
		System.out.println("�������ͣ�int ������λ����" + Integer.SIZE);
		System.out.println("��װ�ࣺjava.lang.Integer");
		System.out.println("��Сֵ��Integer.MIN_VALUE=" + Integer.MIN_VALUE);
		System.out.println("���ֵ��Integer.MAX_VALUE=" + Integer.MAX_VALUE);
		System.out.println();

		// long
		System.out.println("�������ͣ�long ������λ����" + Long.SIZE);
		System.out.println("��װ�ࣺjava.lang.Long");
		System.out.println("��Сֵ��Long.MIN_VALUE=" + Long.MIN_VALUE);
		System.out.println("���ֵ��Long.MAX_VALUE=" + Long.MAX_VALUE);
		System.out.println();

		// float
		System.out.println("�������ͣ�float ������λ����" + Float.SIZE);
		System.out.println("��װ�ࣺjava.lang.Float");
		System.out.println("��Сֵ��Float.MIN_VALUE=" + Float.MIN_VALUE);
		System.out.println("���ֵ��Float.MAX_VALUE=" + Float.MAX_VALUE);
		System.out.println();

		// double
		System.out.println("�������ͣ�double ������λ����" + Double.SIZE);
		System.out.println("��װ�ࣺjava.lang.Double");
		System.out.println("��Сֵ��Double.MIN_VALUE=" + Double.MIN_VALUE);
		System.out.println("���ֵ��Double.MAX_VALUE=" + Double.MAX_VALUE);
		System.out.println();

		// char
		System.out.println("�������ͣ�char ������λ����" + Character.SIZE);
		System.out.println("��װ�ࣺjava.lang.Character");
		// ����ֵ��ʽ�������ַ���ʽ��Character.MIN_VALUE���������̨
		System.out.println("��Сֵ��Character.MIN_VALUE="
				+ (int) Character.MIN_VALUE);
		// ����ֵ��ʽ�������ַ���ʽ��Character.MAX_VALUE���������̨
		System.out.println("���ֵ��Character.MAX_VALUE="
				+ (int) Character.MAX_VALUE);
	}


}
