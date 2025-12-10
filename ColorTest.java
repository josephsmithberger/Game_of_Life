import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ColorTest extends JFrame
{
    private JPanel p;
    private JButton b;
    private JColorChooser cc;

    public ColorTest()
    {
        p = new JPanel();
        b  = new JButton( "Show Color Chooser" );
        cc = new JColorChooser();
        b.addActionListener( new ColorLauncher() );
        p.add( b );
        getContentPane().add( p );
        addWindowListener( new ExitHandler() );
        setSize( 300, 300 );
        setVisible( true );
    }

    public static void main( String[] args )
    {
        new ColorTest();
    }

    private class ColorLauncher implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            Color newColor = JColorChooser.showDialog( cc, "Color Chooser", ColorTest.this.getBackground() );
            p.setBackground( newColor );
            p.repaint();
        }
    }

    private class ExitHandler extends WindowAdapter
    {
        public void windowClosing( WindowEvent e )
        {
            System.exit( 0 );
        }
    }
}