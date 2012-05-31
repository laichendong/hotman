package com.elf.hotman;

import com.elf.hotman.hostsfile.HostsFile;

import javax.swing.*;

/**
 * User: laichendong
 * Date: 12-5-26
 * Time: обнГ12:03
 */
public class DomainList extends JList {

	/**
	 * Constructs a <code>JList</code> with an empty, read-only, model.
	 */
	public DomainList() {
		super(HostsFile.getInstance().getDomains().toArray());
		this.setVisibleRowCount(Hotman.LIST_VISIABLE_ROW_COUNT);
		this.setFixedCellWidth(Hotman.LIST_FIXED_CELL_WIDTH);
		DefaultListCellRenderer cellRenderer = new DefaultListCellRenderer();
		cellRenderer.setHorizontalAlignment(JLabel.RIGHT);
		this.setCellRenderer(cellRenderer);
	}
}
