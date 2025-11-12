package jdbc.project;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.sql.*;

@SuppressWarnings("serial")
public class JDBCMainWindowContent extends JInternalFrame implements ActionListener, ChangeListener
{	
	String cmd = null;

	// DB Connectivity Attributes
	private Connection con = null;
	private Statement stmt = null;
	private ResultSet rs = null;
	private String url = "jdbc:mysql://localhost:3307/db4_jdbc_assignment";
	private String username = "root";
	private String password = "";

	// Layout
	private Container content;
	private JTabbedPane tabPanel = new JTabbedPane();
	private JPanel animalDetailsPanel = new JPanel();
	private JPanel enclosureDetailsPanel = new JPanel();
	private JPanel feedingDetailsPanel = new JPanel();
	private JPanel actionsPanel = new JPanel();
	private JPanel exportButtonPanel = new JPanel();
	private JScrollPane dbContentsPanel;

	private Border lineBorder;

	// Animal tab labels & input
	private JLabel labelAnimalID = new JLabel("Animal ID:");
	private JLabel labelAnimalType = new JLabel("Type:");
	private JLabel labelAnimalName = new JLabel("Name:");
	private JLabel labelAnimalGender = new JLabel("Gender:");
	private JLabel labelAnimalAge = new JLabel("Age:");
	private JLabel labelAnimalsEnclosureID = new JLabel("Enclosure ID:");
	private JSpinner inputAnimalID = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
	private JTextField inputAnimalType = new JTextField(1);
	private JTextField inputAnimalName = new JTextField(1);
	private JTextField inputAnimalGender = new JTextField(1);
	private JSpinner inputAnimalAge = new JSpinner(new SpinnerNumberModel(-1, -1, Integer.MAX_VALUE, 1));
	private JSpinner inputAnimalsEnclosureID = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));

	// Enclosure tab labels & input
	private JLabel labelEnclosureID = new JLabel("Enclosure ID:");
	private JLabel labelEnclosureLabel = new JLabel("Label:");
	private JSpinner inputEnclosureID = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
	private JTextField inputEnclosureLabel = new JTextField(1);

	// Feeding tab labels & input
	private JLabel labelFeedingID = new JLabel("Feeding ID:");
	private JLabel labelFeedingEnclosureID = new JLabel("Enclosure ID:");
	private JLabel labelFeedingHour = new JLabel("Feeding Hour:");
	private JLabel labelFedToday = new JLabel("Fed today?");
	private JSpinner inputFeedingID = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
	private JSpinner inputFeedingEnclosureID = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
	private JTextField inputFeedingHour = new JTextField(1);
	private JCheckBox inputFedToday = new JCheckBox("Fed");

	private static QueryTableModel TableModel = new QueryTableModel();
	//Add the models to JTabels
	private JTable TableofDBContents=new JTable(TableModel);
	//Buttons for inserting, and updating members
	//also a clear button to clear details panel
	private JButton updateButton = new JButton("Update");
	private JButton insertButton = new JButton("Insert");
	private JButton exportButton  = new JButton("Export");
	private JButton deleteButton  = new JButton("Delete");
	private JButton clearButton  = new JButton("Clear");
	private JCheckBox showIDsCheckbox = new JCheckBox("Show IDs");

	private JButton exportAnimalTypesButton = new JButton("List all Animals");
	private JButton exportMaxPopulation  = new JButton("Highest Populated Enclosure(s)");

	public JDBCMainWindowContent( String aTitle)
	{	
		//setting up the GUI
		super(aTitle, false,false,false,false);
		setEnabled(true);
		
		initiate_db_conn();
		//add the 'main' panel to the Internal Frame
		content=getContentPane();
		content.setLayout(null);
		content.setBackground(Color.lightGray);
		lineBorder = BorderFactory.createEtchedBorder(15, new Color(200,221,242), new Color(122,138,153));

		//setup details panel and add the components to it
		animalDetailsPanel.setLayout(new GridLayout(10,2));
		animalDetailsPanel.setBackground(Color.lightGray);
//		animalDetailsPanel.setBorder(BorderFactory.createTitledBorder(lineBorder, "CRUD Actions"));
		enclosureDetailsPanel.setLayout(new GridLayout(10,2));
		enclosureDetailsPanel.setBackground(Color.lightGray);
		feedingDetailsPanel.setLayout(new GridLayout(10,2));
		feedingDetailsPanel.setBackground(Color.lightGray);
		inputFedToday.setBackground(Color.lightGray);

		animalDetailsPanel.add(labelAnimalID);			
		animalDetailsPanel.add(inputAnimalID);
		animalDetailsPanel.add(labelAnimalType);		
		animalDetailsPanel.add(inputAnimalType);
		animalDetailsPanel.add(labelAnimalName);		
		animalDetailsPanel.add(inputAnimalName);
		animalDetailsPanel.add(labelAnimalGender);	
		animalDetailsPanel.add(inputAnimalGender);
		animalDetailsPanel.add(labelAnimalAge);		
		animalDetailsPanel.add(inputAnimalAge);
		animalDetailsPanel.add(labelAnimalsEnclosureID);
		animalDetailsPanel.add(inputAnimalsEnclosureID);
		
		enclosureDetailsPanel.add(labelEnclosureID);
		enclosureDetailsPanel.add(inputEnclosureID);
		enclosureDetailsPanel.add(labelEnclosureLabel);
		enclosureDetailsPanel.add(inputEnclosureLabel);
		
		feedingDetailsPanel.add(labelFeedingID);
		feedingDetailsPanel.add(inputFeedingID);
		feedingDetailsPanel.add(labelFeedingHour);
		feedingDetailsPanel.add(inputFeedingHour);
		feedingDetailsPanel.add(labelFeedingEnclosureID);
		feedingDetailsPanel.add(inputFeedingEnclosureID);
		feedingDetailsPanel.add(labelFedToday);
		feedingDetailsPanel.add(inputFedToday);
		
		// Adding a few empty labels fixes the grid where there are less than 6 inputs
		enclosureDetailsPanel.add(new JLabel());
		enclosureDetailsPanel.add(new JLabel());
		enclosureDetailsPanel.add(new JLabel());
		enclosureDetailsPanel.add(new JLabel());
		enclosureDetailsPanel.add(new JLabel());
		enclosureDetailsPanel.add(new JLabel());
		enclosureDetailsPanel.add(new JLabel());
		feedingDetailsPanel.add(new JLabel());
		feedingDetailsPanel.add(new JLabel());
		feedingDetailsPanel.add(new JLabel());
		
		// Consolidate the details panels into tabs
		tabPanel.addTab("Animal", animalDetailsPanel);
		tabPanel.addTab("Enclosure", enclosureDetailsPanel);
		tabPanel.addTab("Feeding Time", feedingDetailsPanel);
		tabPanel.addTab("All", new JLabel("CRUD Operations not availabe in merged view"));
		tabPanel.addTab("Audit", new JLabel("Audit log cannot be altered manually"));
		tabPanel.addChangeListener(this);
		tabPanel.setSize(360, 300);
		tabPanel.setLocation(3,0);
		content.add(tabPanel);

		//setup details panel and add the components to it
		exportButtonPanel.setLayout(new FlowLayout());
		exportButtonPanel.setBackground(Color.lightGray);
		exportButtonPanel.setBorder(BorderFactory.createTitledBorder(lineBorder, "Export Data"));
		exportButtonPanel.add(exportAnimalTypesButton);
		exportButtonPanel.add(exportMaxPopulation);
		exportButtonPanel.setSize(500, 100);
		exportButtonPanel.setLocation(3, 300);
		content.add(exportButtonPanel);
		
		// Actions panel
		actionsPanel.setLayout(new GridLayout(6,1));
		actionsPanel.setBackground(Color.lightGray);
		actionsPanel.setBorder(BorderFactory.createTitledBorder(lineBorder, "Actions"));
		
		actionsPanel.setSize(100, 300);
		actionsPanel.setLocation(370, 0);
		
		actionsPanel.add(insertButton);
		actionsPanel.add(updateButton);
		actionsPanel.add(exportButton);
		actionsPanel.add(deleteButton);
		actionsPanel.add(clearButton);
		
		showIDsCheckbox.setBackground(Color.lightGray);
		showIDsCheckbox.setSelected(true);
		actionsPanel.add(showIDsCheckbox);
		
		content.add(actionsPanel);
		
		insertButton.addActionListener(this);
		updateButton.addActionListener(this);
		exportButton.addActionListener(this);
		deleteButton.addActionListener(this);
		clearButton.addActionListener(this);
		showIDsCheckbox.addActionListener(this);

		exportAnimalTypesButton.addActionListener(this);
		exportMaxPopulation.addActionListener(this);

		TableofDBContents.setPreferredScrollableViewportSize(new Dimension(900, 300));

		dbContentsPanel=new JScrollPane(TableofDBContents,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		dbContentsPanel.setBackground(Color.lightGray);
		dbContentsPanel.setBorder(BorderFactory.createTitledBorder(lineBorder,"Database Content"));
		
		dbContentsPanel.setSize(700, 300);
		dbContentsPanel.setLocation(477, 0);
		content.add(dbContentsPanel);
		
		setSize(982,645);
		setVisible(true);
		
		refreshTable();
	}

	public void initiate_db_conn()
	{
		try
		{
			// Load the JConnector Driver
			Class.forName("com.mysql.cj.jdbc.Driver");
            			
			// Connect to DB using DB URL, Username and password
			con = DriverManager.getConnection(url, username, password);
			
			//Create a generic statement which is passed to the TestInternalFrame1
			stmt = con.createStatement();
		}
		catch(Exception e)
		{
			System.out.println("Error: Failed to connect to database\n"+e.getMessage());
		}
	}

	//event handling 
	public void actionPerformed(ActionEvent e) {		
		Object target = e.getSource();
		int tabIndex = tabPanel.getSelectedIndex();
		
		// INSERT button
		if (target == insertButton) {
			switch (tabIndex) {
			case(0): // 0 = Animals tab 
				try {
					int id = (int)inputAnimalID.getValue();
					String type = inputAnimalType.getText().trim();
					if (type.isEmpty()) type = null;
					String name = inputAnimalName.getText().trim();
					if (name.isEmpty()) name = null;
					String gender = inputAnimalGender.getText().trim();
					if (gender.isEmpty()) gender = null;
					int age = (int)inputAnimalAge.getValue();
					int eId = (int)inputAnimalsEnclosureID.getValue();
					
					PreparedStatement ps = con.prepareStatement("INSERT INTO Animals (AnimalType, AnimalName, AnimalGender, AnimalAge, EnclosureID) VALUES (?, ?, ?, ?, ?)");
					ps.setString(1, type);
					ps.setString(2, name);
					ps.setString(3, gender);
					ps.setInt(4, age);
					ps.setInt(5, eId);
					
					ps.executeUpdate();
					
					// After successful insert, inform the user that ID cannot be entered manually if they tried to do so
					if (id > 0) {
						JOptionPane.showMessageDialog(null, "Animal ID is incremented automatically in the database.\nManual input is not supported.", "Note", JOptionPane.INFORMATION_MESSAGE);
					}
				} catch (SQLException sqle) {
					System.err.println("Error with  insert:\n" + sqle.getMessage());
					JOptionPane.showMessageDialog(null, sqle.getMessage(), "Error Inserting", JOptionPane.ERROR_MESSAGE);
				}
				break;
			case(1): // 1 = Enclosures tab
				try {
					int id = (int)inputEnclosureID.getValue();
					String label = inputEnclosureLabel.getText().trim();
					if (label.isEmpty()) label = null;
					
					PreparedStatement ps = con.prepareStatement("INSERT INTO Enclosures (EnclosureLabel) VALUES (?)");
					ps.setString(1, label);
					
					ps.executeUpdate();
					
					// After successful insert, inform the user that ID cannot be entered manually if they tried to do so
					if (id > 0) {
						JOptionPane.showMessageDialog(null, "Enclosure ID is incremented automatically in the database.\nManual input is not supported.", "Note", JOptionPane.INFORMATION_MESSAGE);
					}
				} catch (SQLException sqle) {
					System.err.println("Error with  insert:\n" + sqle.getMessage());
					JOptionPane.showMessageDialog(null, sqle.getMessage(), "Error Inserting", JOptionPane.ERROR_MESSAGE);
				}
				break;
			case(2): // 2 = Feeding Times tab
				try {
					int id = (int)inputFeedingID.getValue();
					int eId = (int)inputFeedingEnclosureID.getValue();
					String hour = inputFeedingHour.getText().trim();
					if (hour.isEmpty()) hour = null;
					boolean fed = inputFedToday.isSelected();
					
					
					PreparedStatement ps = con.prepareStatement("INSERT INTO FeedingTimes (EnclosureID, FeedingHour, FedToday) VALUES (?, ?, ?)");
					ps.setInt(1, eId);
					ps.setString(2, hour);
					ps.setBoolean(3, fed);
					
					ps.executeUpdate();
					
					// After successful insert, inform the user that ID cannot be entered manually if they tried to do so
					if (id > 0) {
						JOptionPane.showMessageDialog(null, "Feeding ID is incremented automatically in the database.\nManual input is not supported.", "Note", JOptionPane.INFORMATION_MESSAGE);
					}
				} catch (Exception sqle) {
					System.err.println("Error with  insert:\n" + sqle.getMessage());
					JOptionPane.showMessageDialog(null, sqle.getMessage(), "Error Inserting", JOptionPane.ERROR_MESSAGE);
				}
				break;
			default:
				JOptionPane.showMessageDialog(null, "Can't insert from this tab", "Warning", JOptionPane.WARNING_MESSAGE);
				break;
			}
			
		}
		
		// UPDATE button
		if (target == updateButton) {
			switch (tabIndex) {
			case(0): // 0 = Animals tab 
				try {
					int id = (int)inputAnimalID.getValue();
					String type = inputAnimalType.getText().trim();
					String name = inputAnimalName.getText().trim();
					String gender = inputAnimalGender.getText().trim();
					int age = (int)inputAnimalAge.getValue();
					int eId = (int)inputAnimalsEnclosureID.getValue();
					
					// Get the old values for validation
					PreparedStatement oldValues = con.prepareStatement("SELECT * FROM Animals WHERE AnimalID = ?");
					oldValues.setInt(1, id);
					
					ResultSet rs = oldValues.executeQuery();
					if (rs.next()) {
						// For empty fields, keep values the same
						if (type.isEmpty()) type = rs.getString("AnimalType");
						if (name.isEmpty()) name = rs.getString("AnimalName");
						if (gender.isEmpty()) gender = rs.getString("AnimalGender");
						if (age < 0) age = rs.getInt("AnimalAge");
						if (eId < 1) eId = rs.getInt("EnclosureID");
					} else {
						// Empty result set, update won't be possible
						JOptionPane.showMessageDialog(null, "No animal found with ID " + id + ".\nCannot execute update!", "Error Updating", JOptionPane.WARNING_MESSAGE);
						break;
					}
					
					PreparedStatement ps = con.prepareStatement("UPDATE Animals SET AnimalType = ?, AnimalName = ?, AnimalGender = ?, AnimalAge = ?, EnclosureID = ? WHERE AnimalID = ?");
					ps.setString(1, type);
					ps.setString(2, name);
					ps.setString(3, gender);
					ps.setInt(4, age);
					ps.setInt(5, eId);
					ps.setInt(6, id);
					
					ps.executeUpdate();
					
				} catch (SQLException sqle) {
					System.err.println("Error with  update:\n" + sqle.getMessage());
					JOptionPane.showMessageDialog(null, sqle.getMessage(), "Error Updating", JOptionPane.ERROR_MESSAGE);
				}
				break;
			case(1): // 1 = Enclosures tab
				try {
					int id = (int)inputEnclosureID.getValue();
					String label = inputEnclosureLabel.getText().trim();
					
					// Get the old values for validation
					PreparedStatement oldValues = con.prepareStatement("SELECT * FROM Enclosures WHERE EnclosureID = ?");
					oldValues.setInt(1, id);
					
					ResultSet rs = oldValues.executeQuery();
					if (rs.next()) {
						// For empty fields, keep values the same
						if (label.isEmpty()) label = rs.getString("EnclosureLabel");
					} else {
						// Empty result set, update won't be possible
						JOptionPane.showMessageDialog(null, "No Enclosure found with ID " + id + ".\nCannot execute update!", "Error Updating", JOptionPane.WARNING_MESSAGE);
						break;
					}
					
					PreparedStatement ps = con.prepareStatement("UPDATE Enclosures SET EnclosureLabel = ? WHERE EnclosureID = ?");
					ps.setString(1, label);
					ps.setInt(2, id);
					
					ps.executeUpdate();
				} catch (SQLException sqle) {
					System.err.println("Error with  update:\n" + sqle.getMessage());
					JOptionPane.showMessageDialog(null, sqle.getMessage(), "Error Updating", JOptionPane.ERROR_MESSAGE);
				}
				break;
			case(2): // 2 = Feeding Times tab
				try {
					int id = (int)inputFeedingID.getValue();
					int eId = (int)inputFeedingEnclosureID.getValue();
					String hour = inputFeedingHour.getText().trim();
					boolean fed = inputFedToday.isSelected();
					
					// Get the old values for validation
					PreparedStatement oldValues = con.prepareStatement("SELECT * FROM FeedingTimes WHERE FeedingID = ?");
					oldValues.setInt(1, id);
					
					ResultSet rs = oldValues.executeQuery();
					if (rs.next()) {
						// For empty fields, keep values the same
						if (eId < 1) eId = rs.getInt("EnclosureID");
						if (hour.isEmpty()) hour = rs.getString("FeedingHour");
						// Unfortunately, boolean must be checked manually each time
					} else {
						// Empty result set, update won't be possible
						JOptionPane.showMessageDialog(null, "No Enclosure found with ID " + id + ".\nCannot execute update!", "Error Updating", JOptionPane.WARNING_MESSAGE);
						break;
					}
					
					PreparedStatement ps = con.prepareStatement("UPDATE FeedingTimes SET EnclosureID = ?, FeedingHour = ?, FedToday = ? WHERE FeedingID = ?");
					ps.setInt(1, eId);
					ps.setString(2, hour);
					ps.setBoolean(3, fed);
					ps.setInt(4, id);
					
					ps.executeUpdate();
				} catch (Exception sqle) {
					System.err.println("Error with  update:\n" + sqle.getMessage());
					JOptionPane.showMessageDialog(null, sqle.getMessage(), "Error Updating", JOptionPane.ERROR_MESSAGE);
				}
				break;
			default:
				JOptionPane.showMessageDialog(null, "Can't update from this tab", "Warning", JOptionPane.WARNING_MESSAGE);
				break;
			}
		}
		
		// DELETE button
		if (target == deleteButton) {
			switch (tabIndex) {
			case(0): // 0 = Animals tab 
				try {
					int id = (int)inputAnimalID.getValue();
					
					PreparedStatement ps = con.prepareStatement("DELETE FROM Animals WHERE AnimalID = ?");
					ps.setInt(1, id);
					
					ps.executeUpdate();
					
				} catch (SQLException sqle) {
					System.err.println("Error with  delete:\n" + sqle.getMessage());
					JOptionPane.showMessageDialog(null, sqle.getMessage(), "Error Deleting", JOptionPane.ERROR_MESSAGE);
				}
				break;
			case(1): // 1 = Enclosures tab
				try {
					int id = (int)inputEnclosureID.getValue();
					
					PreparedStatement ps = con.prepareStatement("DELETE FROM Enclosures WHERE EnclosureID = ?");
					ps.setInt(1, id);
					
					ps.executeUpdate();
					
				} catch (SQLException sqle) {
					System.err.println("Error with  delete:\n" + sqle.getMessage());
					JOptionPane.showMessageDialog(null, sqle.getMessage(), "Error Deleting", JOptionPane.ERROR_MESSAGE);
				}
				break;
			case(2): // 2 = Feeding Times tab
				try {
					int id = (int)inputFeedingID.getValue();
					
					PreparedStatement ps = con.prepareStatement("DELETE FROM FeedingTimes WHERE FeedingID = ?");
					ps.setInt(1, id);
					
					ps.executeUpdate();
					
				} catch (SQLException sqle) {
					System.err.println("Error with  delete:\n" + sqle.getMessage());
					JOptionPane.showMessageDialog(null, sqle.getMessage(), "Error Deleting", JOptionPane.ERROR_MESSAGE);
				}
				break;
			default:
				JOptionPane.showMessageDialog(null, "Can't delete from this tab", "Warning", JOptionPane.WARNING_MESSAGE);
				break;
			}
		}

		// Refresh the table after a CRUD action has been taken
		refreshTable();
		
		// EXPORT button
		if (target == exportButton) {
			String fileName = tabPanel.getTitleAt(tabPanel.getSelectedIndex()) + " Table";
			if (fileName.equals("All Table")) fileName = "Merged Table";
			// Use the same method as table query to generate the CSV file table
			cmd = QueryTableModel.getTableQuery(tabPanel.getSelectedIndex(), showIDsCheckbox.isSelected());
			
			try{					
				rs= stmt.executeQuery(cmd);
				writeToFile(rs, fileName);
			}
			catch(Exception e1){e1.printStackTrace();}
		}
		
		// CLEAR button
		if (target == clearButton) {
			// Only clear the contents on the current tab
			switch (tabIndex) {
			case(0): // 0 = Animals tab
				inputAnimalID.setValue(0);
				inputAnimalType.setText("");
				inputAnimalName.setText("");
				inputAnimalGender.setText("");
				inputAnimalAge.setValue(-1);
				inputAnimalsEnclosureID.setValue(0);
				break;
			case(1): // 1 = Enclosures tab
				inputEnclosureID.setValue(0);
				inputEnclosureLabel.setText("");
				break;
			case(2): // 2 = Feeding Times tab
				inputFeedingID.setValue(0);
				inputFeedingHour.setText("");
				inputFeedingEnclosureID.setValue(0);
				inputFedToday.setSelected(false);
				break;
			default:
				System.out.println("Nothing to clear on this tab");
			}
		}

		/////////////////////////////////////////////////////////////////////////////////////
		//I have only added functionality of 2 of the button on the lower right of the template
		///////////////////////////////////////////////////////////////////////////////////

		if(target == exportAnimalTypesButton){
			cmd = "SELECT DISTINCT AnimalType FROM Animals;";

			try{					
				rs= stmt.executeQuery(cmd);
				writeToFile(rs, "Animal Types");
			}
			catch(Exception e1){e1.printStackTrace();}

		}

		if(target == exportMaxPopulation){

			cmd = "SELECT * FROM max_population_enclosures;"; // view from MySQL Workbench
			/*
				SELECT EnclosureLabel,
				fnCountAnimalsInEnclosure(EnclosureID) AS 'Enclosure Population'
				FROM Enclosures
				WHERE fnCountAnimalsInEnclosure(EnclosureID) IN
				(SELECT max(fnCountAnimalsInEnclosure(EnclosureID)) FROM Enclosures);
			*/

			try{					
				rs= stmt.executeQuery(cmd); 	
				writeToFile(rs, "Highest Populated Enclosures");
			}
			catch(Exception e1){e1.printStackTrace();}

		} 

	}
	///////////////////////////////////////////////////////////////////////////

	private void writeToFile(ResultSet rs, String fileName){
		try{
			System.out.println("Exporting " + fileName);
			File dir = new File("exports/");
			dir.mkdir();
			FileWriter outputFile = new FileWriter("exports/"+fileName+".csv");
			PrintWriter printWriter = new PrintWriter(outputFile);
			ResultSetMetaData rsmd = rs.getMetaData();
			int numColumns = rsmd.getColumnCount();

			for(int i=0;i<numColumns;i++){
				printWriter.print(rsmd.getColumnLabel(i+1)+",");
			}
			printWriter.print("\n");
			while(rs.next()){
				for(int i=0;i<numColumns;i++){
					String colName = rsmd.getColumnName(i+1);
					String content = rs.getString(i+1);
					
					// Format certain strings to make them more readable
					if (rsmd.getColumnTypeName(i+1).equals("BIT")) {
						printWriter.print(rs.getBoolean(i+1) ? "Yes," : "No,");
					} else if (colName.equals("AnimalName") && rs.wasNull()) {
						printWriter.print("No name,");
					} else if (colName.equals("AnimalGender") && rs.wasNull()) {
						printWriter.print("Unknown gender,");
					} else if (colName.equals("AnimalAge") && (content == null || content.equals("-1"))) {
						printWriter.print("Unknown age,");
					} else if (colName.equals("EnclosureID") && (content == null || content.equals("0"))) {
						printWriter.print("Not assigned to any enclosure,");
					} else if (colName.equals("EnclosureLabel") && rs.wasNull()) {
						printWriter.print("Enclosure not labelled,");
					} else {
						printWriter.print(content+",");
					}
				}
				printWriter.print("\n");
				printWriter.flush();
			}
			printWriter.close();
		}
		catch(Exception e){e.printStackTrace();}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		refreshTable();
	}
	
	private void refreshTable() {
		TableModel.refreshFromDB(stmt, tabPanel.getSelectedIndex(), showIDsCheckbox.isSelected());
	}
}
