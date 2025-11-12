package jdbc.project;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.event.*;
import java.awt.*;
import java.sql.*;
import java.io.*;
import java.util.*;

@SuppressWarnings("serial")
class QueryTableModel extends AbstractTableModel
{
	Vector modelData; //will hold String[] objects
	int colCount;
	String[] headers=new String[0] ;
	Connection con;
	Statement stmt = null;
	String[] record;
	ResultSet rs = null;

	public QueryTableModel(){
		modelData = new Vector();
	}//end constructor QueryTableModel

	public String getColumnName(int i){
		return headers[i];
	}	
	public int getColumnCount(){
		return colCount;
	}
	
	public int getRowCount(){
		return modelData.size();
	}
	
	public Object getValueAt(int row, int col){
		return ((String[])modelData.elementAt(row))[col];
	}

	public static String getTableQuery(int tabIndex, boolean withIDs) {
		//Determine table to display based on selected tab
		switch (tabIndex) {
		default:
		case(0): // 0 = Animals tab
			return withIDs ? "SELECT * FROM Animals" : "SELECT * FROM Animals_Info";
		case(1): // 1 = Enclosures tab
			return withIDs ? "SELECT *, fnCountAnimalsInEnclosure(EnclosureID) AS 'Population' FROM Enclosures"
					: "SELECT EnclosureLabel, fnCountAnimalsInEnclosure(EnclosureID) AS 'Population' FROM Enclosures";
		case(2): // 2 = Feeding Times tab
			return withIDs ? "SELECT f.FeedingID, f.FeedingHour, e.EnclosureID, e.EnclosureLabel, f.FedToday FROM FeedingTimes f LEFT JOIN Enclosures e ON f.EnclosureID = e.EnclosureID ORDER BY f.FeedingHour ASC, e.EnclosureID ASC" 
					: "SELECT f.FeedingHour, e.EnclosureLabel, f.FedToday FROM FeedingTimes f LEFT JOIN Enclosures e ON f.EnclosureID = e.EnclosureID ORDER BY f.FeedingHour ASC, e.EnclosureID ASC";
		case(3): // 3 = Merged Table tab
			return withIDs ? "SELECT * FROM Merged_View_withIDs" : "SELECT * FROM Merged_View";
		case(4): // 4 = Audit Log tab
			return "SELECT * FROM AuditLog";
		}
	}
	
	public void refreshFromDB(Statement stmt1, int tabIndex, boolean withIDs)
	{
		//modelData is the data stored by the table
		//when set query is called the data from the 
		//DB is queried using �SELECT * FROM myInfo� 
		//and the data from the result set is copied 
		//into the modelData. Every time refreshFromDB is
		//called the DB is queried and a new 
		//modelData is created  

		modelData = new Vector();
		stmt = stmt1;
		try{
			String query = getTableQuery(tabIndex, withIDs);

			//Execute the query and store the result set and its metadata
			rs = stmt.executeQuery(query);
			ResultSetMetaData meta = rs.getMetaData();
		
			//to get the number of columns
			colCount = meta.getColumnCount(); 
			// Now must rebuild the headers array with the new column names
			headers = new String[colCount];
	
			for(int h = 0; h<colCount; h++)
			{
				headers[h] = meta.getColumnName(h+1);
			}//end for loop
		
			// fill the cache with the records from the query, ie get all the rows
		
			while(rs.next())
			{
				record = new String[colCount];
				for(int i = 0; i < colCount; i++)
				{
					String colName = meta.getColumnName(i+1);
					String content = rs.getString(i+1);
					
					// Format certain strings to make them more readable
					if (meta.getColumnTypeName(i+1).equals("BIT")) {
						record[i] = rs.getBoolean(i+1) ? "Yes" : "No";
					} else if (colName.equals("AnimalName") && rs.wasNull()) {
						record[i] = "No name";
					} else if (colName.equals("AnimalGender") && rs.wasNull()) {
						record[i] = "Unknown gender";
					} else if (colName.equals("AnimalAge") && (content == null || content.equals("-1"))) {
						record[i] = "Unknown age";
					} else if (colName.equals("EnclosureID") && (content == null || content.equals("0"))) {
						record[i] = "Not assigned to any enclosure";
					} else if (colName.equals("EnclosureLabel") && rs.wasNull()) {
						record[i] = "Enclosure not labelled";
					} else {
						record[i] = content;
					}
				}//end for loop
				modelData.addElement(record);
			}// end while loop
			fireTableChanged(null); //Tell the listeners a new table has arrived.
		}//end try clause
		catch(Exception e) {
					System.out.println("Error with refreshFromDB Method\n"+e.toString());
		} // end catch clause to query table
	}//end refreshFromDB method
}// end class QueryTableModel
