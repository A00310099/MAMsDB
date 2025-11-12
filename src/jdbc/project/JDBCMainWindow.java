package jdbc.project;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class JDBCMainWindow extends JFrame {
	private JMenuItem exitItem;

	public JDBCMainWindow()
	{
		// Sets the Window Title
		super( "JDBC 2025 Assignment" );
		setResizable(false);

		// Create an instance of our class JDBCMainWindowContent 
		JDBCMainWindowContent aWindowContent = new JDBCMainWindowContent( "Many Animals Management system");
		// Add the instance to the main section of the window
		getContentPane().add( aWindowContent );

		setSize( 1200, 500 );
		setVisible( true );
		setLocationRelativeTo(null); // opens in centre of the screen
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}	
}