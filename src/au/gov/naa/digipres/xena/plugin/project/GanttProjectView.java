package au.gov.naa.digipres.xena.plugin.project;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

import au.gov.naa.digipres.xena.gui.MainFrame;
import au.gov.naa.digipres.xena.helper.XmlContentHandlerSplitter;
import au.gov.naa.digipres.xena.kernel.PluginLocator;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.view.XenaView;

/**
 * View a Xena project file using the Gantt free project planner.
 *
 * @author Chris Bitmead
 */
public class GanttProjectView extends XenaView {

	byte[] result;

    
    
	public GanttProjectView() {
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public boolean canShowTag(String tag) throws XenaException {
		return tag.equals("Project");
	}

	public String getViewName() {
		return "Gantt Project";
	}

	public ContentHandler getContentHandler() throws XenaException {

		SAXTransformerFactory tf = (SAXTransformerFactory)SAXTransformerFactory.
			newInstance();
		TransformerHandler write = null;
		ByteArrayOutputStream bao = null;
		try {
			write = tf.newTransformerHandler();
			bao = new ByteArrayOutputStream();
			StreamResult streamResult = new StreamResult(bao);
			write.setResult(streamResult);
		} catch (TransformerConfigurationException x) {
			throw new XenaException(x);
		}
		final TransformerHandler writer = write;
		final ByteArrayOutputStream baos = bao;
		final AttributesImpl empty = new AttributesImpl();
//		AbstractTreeTableModel treeTableModel = null;
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss");
		final SimpleDateFormat ganttsdf = new SimpleDateFormat("dd'/'MM'/'yyyy");
		XmlContentHandlerSplitter splitter = new XmlContentHandlerSplitter();
		splitter.addContentHandler(getTmpFileContentHandler());
		splitter.addContentHandler(new XMLFilterImpl() {
			StringBuffer sb;

			Task task;

			Predecessor predecessor;

			Resource resource;

			Allocation allocation;

			Map taskByOutlineNo = new HashMap();

			Map taskById = new HashMap();

			Set roles = new HashSet();

			int depth;

			public void startDocument() throws SAXException {
				writer.startDocument();
			}

			public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
				depth++;
				/*				if (qName.equals("Project")) {
					 writer.startElement(null, "project", "project", empty);
					} else */
				if (qName.equals("Tasks")) {
					writer.startElement(null, "tasks", "tasks", empty);
				} else if (qName.equals("Assignments")) {
					writer.startElement(null, "allocations", "allocations", empty);
				} else if (qName.equals("Resources")) {
					writer.startElement(null, "resources", "resources", empty);
				} else if (qName.equals("Task")) {
					assert task == null;
					depth = 0;
					task = new Task();
//					System.out.println("Task");
				} else if (qName.equals("Resource")) {
					assert resource == null;
					depth = 0;
					resource = new Resource();
				} else if (qName.equals("Assignment")) {
					assert allocation == null;
					depth = 0;
					allocation = new Allocation();
				} else if (task != null && qName.equals("PredecessorLink")) {
					assert predecessor == null;
					predecessor = new Predecessor();
				}
				sb = new StringBuffer();
			}

			public void endElement(String uri, String localName, String qName) throws SAXException {
//				System.out.println(qName);
				if (qName.equals("StartDate")) {
					try {
						java.util.Date create = sdf.parse(sb.toString());
						final AttributesImpl att = new AttributesImpl();
						att.addAttribute(null, "view-date", "view-date", "CDATA", ganttsdf.format(create));
						writer.startElement(null, "project", "project", att);
					} catch (ParseException x) {
						throw new SAXException(x);
					}
				} else if (qName.equals("Project")) {
					Iterator it = roles.iterator();
					while (it.hasNext()) {
						String role = (String)it.next();
						AttributesImpl att = new AttributesImpl();
						att.addAttribute(null, "roleset-name", "roleset-name", "CDATA", role);
						writer.startElement(null, "roles", "roles", att);
						writer.endElement(null, "roles", "roles");
					}
					writer.endElement(null, null, "project");
				} else if (qName.equals("Tasks")) {
					List tops = new ArrayList();
					Iterator it = taskByOutlineNo.entrySet().iterator();
					while (it.hasNext()) {
						Map.Entry e = (Map.Entry)it.next();
						Task tsk = (Task)e.getValue();
						if (tsk.parent == null) {
							tops.add(e.getValue());
						} else {
							Task t = (Task)taskByOutlineNo.get(tsk.parent);
							t.children.add(e.getValue());
						}
						Iterator it2 = tsk.predecessors.iterator();
						while (it2.hasNext()) {
							Predecessor p = (Predecessor)it2.next();
							Task pt = (Task)taskById.get(p.id);
							Predecessor depend = new Predecessor();
							depend.id = tsk.id;
							depend.type = p.type;
							pt.dependancies.add(depend);
//							System.out.println("D: " + pt.id + " " + depend.id + " ");
						}
					}
					Iterator it3 = tops.iterator();
					while (it3.hasNext()) {
						Task top = (Task)it3.next();
						top.print(writer);
					}
					writer.endElement(null, null, "tasks");
				} else if (qName.equals("Task")) {
					taskByOutlineNo.put(task.outlineNumber, task);
					taskById.put(task.id, task);
//					System.out.println("T: " + task.id + " E: " + task.finish);
					task = null;
				} else if (qName.equals("Resources")) {
					writer.endElement(null, null, "resources");
				} else if (qName.equals("Assignments")) {
					writer.endElement(null, null, "allocations");
				} else if (qName.equals("Resource")) {
					final AttributesImpl att = new AttributesImpl();
					att.addAttribute(null, "id", "id", "ID", resource.id);
					if (resource.name == null) {
						resource.name = "Unnamed-" + resource.id;
					}

					att.addAttribute(null, "name", "name", "CDATA", resource.name);
					if (resource.group == null) {
						resource.group = "Default";
					}
					roles.add(resource.group);
					att.addAttribute(null, "function", "function", "CDATA", resource.group + ":0");
					att.addAttribute(null, "contacts", "contacts", "CDATA", "foo@bar.com");
					writer.startElement(null, "resource", "resource", att);
					writer.endElement(null, "resource", "resource");
					resource = null;
				} else if (qName.equals("Assignment")) {
					final AttributesImpl att = new AttributesImpl();
					att.addAttribute(null, "task-id", "task-id", "CDATA", allocation.taskId);
					att.addAttribute(null, "resource-id", "resource-id", "CDATA", allocation.resourceId);
					att.addAttribute(null, "load", "load", "CDATA", Double.toString(allocation.units));
					writer.startElement(null, "allocation", "allocation", att);
					writer.endElement(null, "allocation", "allocation");
					allocation = null;
				} else if (task != null && predecessor != null) {
					if (qName.equals("PredecessorLink")) {
						task.predecessors.add(predecessor);
//						System.out.println("P: " + task.id + " " + predecessor.id + " ");
						predecessor = null;
					} else if (qName.equals("PredecessorUID")) {
						predecessor.id = sb.toString();
					} else if (qName.equals("Type")) {
						int tp = Integer.parseInt(sb.toString());
						if (tp == 0) {
							predecessor.type = "2";
						} else if (tp == 1) {
							predecessor.type = "1";
						} else if (tp == 2) {
							predecessor.type = "3";
						} else if (tp == 3) {
							predecessor.type = "4";
						} else {
							assert false:"unknown type";
						}
					}
				} else if (task != null && depth == 1) {
//					if (task.id != null && task.id.equals("49")) {
//						System.out.println("QN: " + qName + " : " + sb);
//					}
					if (qName.equals("OutlineNumber")) {
						task.outlineNumber = sb.toString();
						if (task.outlineNumber.endsWith(".0")) {
							task.outlineNumber = task.outlineNumber.substring(0, task.outlineNumber.length() - 2);
						}
						int c;
						if (0 <= (c = task.outlineNumber.lastIndexOf('.'))) {
							task.parent = task.outlineNumber.substring(0, c);
							task.order = Integer.parseInt(task.outlineNumber.substring(c + 1));
						}
//						System.out.println("tsk: " + task.outlineNumber + " par: " + task.parent);
					} else if (qName.equals("Name")) {
						task.name = sb.toString();
					} else if (qName.equals("UID")) {
						task.id = sb.toString();
					} else if (qName.equals("Start")) {
						try {
							task.start = sdf.parse(sb.toString());
						} catch (ParseException x) {
							throw new SAXException(x);
						}
					} else if (qName.equals("Finish")) {
						try {
//							if (task.id.equals("49")) {
//								System.out.println("SB: " + sb + " O: " + task.finish);
//							}
							task.finish = sdf.parse(sb.toString());
						} catch (ParseException x) {
							throw new SAXException(x);
						}
					} else if (qName.equals("Milestone")) {
						task.milestone = Integer.parseInt(sb.toString()) != 0;
					} else if (qName.equals("PercentComplete")) {
						task.percentComplete = sb.toString();
					} else if (qName.equals("Priority")) {
						int p = Integer.parseInt(sb.toString());
						if (p == 500) {
							task.priority = 1;
						} else if (p < 500) {
							task.priority = 0;
						} else {
							task.priority = 2;
						}
					}
				} else if (resource != null && depth == 1) {
					if (qName.equals("Name")) {
						resource.name = sb.toString();
					} else if (qName.equals("UID")) {
						resource.id = sb.toString();
					} else if (qName.equals("Group")) {
						resource.group = sb.toString();
					}
				} else if (allocation != null && depth == 1) {
					if (qName.equals("TaskUID")) {
						allocation.taskId = sb.toString();
					} else if (qName.equals("ResourceUID")) {
						allocation.resourceId = sb.toString();
					} else if (qName.equals("Units")) {
						Double f = Double.parseDouble(sb.toString());
						allocation.units = f * 100.0;
					}
				}
				sb = null;
				depth--;
			}

			public void characters(char[] ch, int start, int length) throws SAXException {
				if (sb != null) {
					sb.append(ch, start, length);
				}
			}

			public void endDocument() throws SAXException {
				writer.endDocument();
				result = baos.toByteArray();
			}
		});
		return splitter;
	}

	private void jbInit() throws Exception {
		launchGanttButton.setText("View in External Window");
		launchGanttButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				launchGanttButton_actionPerformed(e);
			}
		});
		this.add(launchGanttButton, java.awt.BorderLayout.CENTER);
	}

	private JButton launchGanttButton = new JButton();

	public void launchGanttButton_actionPerformed(ActionEvent e) {
		try {
			File tmpFile = File.createTempFile("gantt", ".xml");
			tmpFile.deleteOnExit();
			FileOutputStream fos = new FileOutputStream(tmpFile);
//			System.out.println(tmpFile.toString());
			fos.write(result);
			fos.close();
			File gantt = new File(PluginLocator.getExternalDir(), "ganttproject.jar");
			System.out.println("gantt: " + gantt.toString() + " " + tmpFile.toString() + " " + gantt.exists() + " d:" + System.getProperty("user.dir"));
			Process p = Runtime.getRuntime().exec(new String[] {"java", "-jar", gantt.toString(), tmpFile.toString()}, null,
												  tmpFile.getParentFile());
		} catch (IOException x) {
			MainFrame.singleton().showError(x);
		}
	}

	class Predecessor {
		String id;

		String type;
	}

	class Task {

		String parent;

		int order;

		List children = new ArrayList();

		String outlineNumber;

		List predecessors = new ArrayList();

		List dependancies = new ArrayList();

		String id;

		String name;

		java.util.Date start;

		java.util.Date finish;

		boolean milestone;

		String percentComplete;

		int priority;

		final long millisPerDay = 1000 * 60 * 60 * 24;

		public void print(ContentHandler ch) throws SAXException {
			java.util.Collections.sort(children, new Comparator() {
				public int compare(Object o1, Object o2) {
					return ((Task)o1).order - ((Task)o2).order;
				}

				public boolean equals(Object obj) {
					return false;
				}
			});
			final AttributesImpl att = new AttributesImpl();
			att.addAttribute(null, "id", "id", "ID", id);
			att.addAttribute(null, "name", "name", "CDATA", name);
			att.addAttribute(null, "meeting", "meeting", "CDATA", milestone ? "true" : "false");
			SimpleDateFormat sdf = new SimpleDateFormat("dd'/'MM'/'yyyy");
			att.addAttribute(null, "start", "start", "CDATA", sdf.format(start));
			long millis = finish.getTime() - start.getTime();
			if (millis < 0) {
				throw new SAXException("Task duration for task: " + id + " is negative. Looks like the data has an error");
			}
			long days = millis / millisPerDay;
			if ((millis % millisPerDay) != 0) {
				days++;
			}
//			if (id.equals("49")) {
//				System.out.println("ST: " + start + " EN: " + finish + " millis: " + millis + " DY: " + days);
//			}
			att.addAttribute(null, "duration", "duration", "CDATA", Long.toString(days));
			att.addAttribute(null, "complete", "complete", "CDATA", percentComplete);
//			att.addAttribute(null, "fixed-start", "fixed-start", "CDATA", "true");
//			att.addAttribute(null, "priority", "priority", "CDATA", Integer.toString(priority));
			ch.startElement(null, "task", "task", att);
			Iterator it = dependancies.iterator();
			while (it.hasNext()) {
				Predecessor depend = (Predecessor)it.next();
				final AttributesImpl datt = new AttributesImpl();
				datt.addAttribute(null, "id", "id", "ID", depend.id);
				datt.addAttribute(null, "type", "type", "CDATA", depend.type);
				ch.startElement(null, "depend", "depend", datt);
				ch.endElement(null, "depend", "depend");
			}
//			System.out.print("s");
			it = children.iterator();
			while (it.hasNext()) {
				Task e = (Task)it.next();
				e.print(ch);
			}
			ch.endElement(null, "task", "task");
//			System.out.print("e");
		}
	}

	class Resource {
		String name;

		String id;

		String group;
	}

	class Allocation {
		String taskId;

		String resourceId;

		double units;
	}
}
