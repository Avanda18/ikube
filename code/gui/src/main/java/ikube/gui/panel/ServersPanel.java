package ikube.gui.panel;

import java.util.Date;

import ikube.gui.Application;
import ikube.gui.IConstant;
import ikube.gui.data.IContainer;

import com.vaadin.terminal.ClassResource;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TreeTable;

public class ServersPanel extends Panel {
	
	private static final String SERVER_COLUMN = "Server";
	private static final String SIZE_COLUMN = "Size";
	private static final String DOCS_COLUMN = "Docs";
	private static final String TIMESTAMP_COLUMN = "Timestamp";
	private static final String PER_MINUTE_COLUMN = "Per min";
	private static final String ACTION_NAME_COLUMN = "Action name";
	private static final String ACTION_COLUMN = "Action";
	
	private Resource serverIcon;
	private Resource sizeIcon;
	private Resource docsIcon;
	private Resource timestampIcon;
	private Resource perMinIcon;
	private Resource actionNameIcon;
	private Resource actionIcon;

	public ServersPanel() {
		setSizeFull();
		getContent().setSizeFull();
		setImmediate(Boolean.TRUE);
		
		createIcons();
		TreeTable treeTable = createTable();

		addComponent(treeTable);
	}
	
	private TreeTable createTable() {
		TreeTable treeTable = new TreeTable(IConstant.SERVERS);
		treeTable.setWidth(100, Sizeable.UNITS_PERCENTAGE);
		treeTable.setSelectable(Boolean.TRUE);
		treeTable.setImmediate(Boolean.TRUE);
		treeTable.setSortDisabled(Boolean.TRUE);
		treeTable.setDescription(IConstant.SERVERS_PANEL_TABLE);
		
		treeTable.addContainerProperty(SERVER_COLUMN, String.class, null, SERVER_COLUMN, serverIcon, null);
		treeTable.addContainerProperty(SIZE_COLUMN, String.class, null, SIZE_COLUMN, sizeIcon, null);
		treeTable.addContainerProperty(DOCS_COLUMN, Long.class, null, DOCS_COLUMN, docsIcon, null);
		treeTable.addContainerProperty(TIMESTAMP_COLUMN, Date.class, null, TIMESTAMP_COLUMN, timestampIcon, null);
		treeTable.addContainerProperty(PER_MINUTE_COLUMN, Long.class, null, PER_MINUTE_COLUMN, perMinIcon, null);
		treeTable.addContainerProperty(ACTION_NAME_COLUMN, Component.class, null, ACTION_NAME_COLUMN, actionNameIcon, null);
		treeTable.addContainerProperty(ACTION_COLUMN, Component.class, null, ACTION_COLUMN, actionIcon, null);
		
		return treeTable;
	}
	
	private void createIcons() {
		serverIcon = new ClassResource(this.getClass(), "/images/icons/index.gif", Application.getApplication());
		sizeIcon = new ClassResource(this.getClass(), "/images/icons/index.gif", Application.getApplication());
		docsIcon = new ClassResource(this.getClass(), "/images/icons/index.gif", Application.getApplication());
		timestampIcon = new ClassResource(this.getClass(), "/images/icons/index.gif", Application.getApplication());
		perMinIcon = new ClassResource(this.getClass(), "/images/icons/index.gif", Application.getApplication());
		actionNameIcon = new ClassResource(this.getClass(), "/images/icons/index.gif", Application.getApplication());
		actionIcon = new ClassResource(this.getClass(), "/images/icons/index.gif", Application.getApplication());

	}

	public void setData(Object data) {
		((IContainer) data).setData(this);
	}

}