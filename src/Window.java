
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ForkJoinPool;
import java.awt.*;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import static javax.swing.JFrame.EXIT_ON_CLOSE;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

// CLASS WINDOW EXTENDS JFRAME FOR THE GRAPHIC PART AND HAS AN ACTION LISTEN BECAUSE IS THE CLASS THAT THE USER HAS INTERACTION WITH
class  Window extends JFrame implements ActionListener{

// DECLARATION OF BUTTONS, AND CLASSES INSTANCES
 Image_displayed area;
 Image_displayed.Filter area1;
 JFileChooser chooser; 
 String filename;
 ForkJoinPool pool;
 JButton boton1;
 JButton BAW;
 JButton saveas;
 JButton filter2;
 JButton filter3;
 JPanel panel;
 
Window(){
	
  // INITIALIZATION OF THE MENU
  area=new Image_displayed();

  boton1=new JButton("Open");
  saveas=new JButton("Save as");
  BAW=new JButton("B&W");
  filter2=new JButton("Gold");
  filter3=new JButton("Purple");
  boton1.addActionListener(this);
  saveas.addActionListener(this);
  BAW.addActionListener(this);
  filter2.addActionListener(this);
  filter3.addActionListener(this);
  
  //SET VISIBLE FALSE, IT WILL BE TRUE WHEN AN IMAGE IS LOADED IN THE IMAGE AREA
  saveas.setVisible(false);
  BAW.setVisible(false);
  filter2.setVisible(false);
  filter3.setVisible(false);
  
  //SET SIZE OF THE BUTTONS
  boton1.setPreferredSize(new Dimension(100, 100));
  BAW.setPreferredSize(new Dimension(100, 100));
  saveas.setPreferredSize(new Dimension(100, 100));
  filter2.setPreferredSize(new Dimension(100, 100));
  filter3.setPreferredSize(new Dimension(100, 100));
  
  Container cont=getContentPane();
  cont.setLayout(new BorderLayout(2, 2));
  JPanel panel = new JPanel();
  panel.setLayout(new FlowLayout(3, 3, 3));
  JPanel panel1 = new JPanel();
  panel1.setLayout(new GridLayout(8, 1, 5, 5));
 
  //ADD THE BUTTONS TO THE PANEL1...
  panel1.add(boton1);
  panel1.add(saveas);
  panel1.add(BAW);
  panel1.add(filter2);
  panel1.add(filter3);
  panel.add(panel1);

  cont.add(panel,BorderLayout.WEST);
  cont.add(area,BorderLayout.CENTER);

  Font font = new Font("Monospaced", Font.BOLD, 18);
  UIManager.put("Menu.font", font);

  pool = new ForkJoinPool();
  
  //JFRAME PROPETIES
  setTitle("Image filter project");
  setDefaultCloseOperation(EXIT_ON_CLOSE);
  setExtendedState(this.getExtendedState() | this.MAXIMIZED_BOTH);
     setVisible(true); 

  // INITIALIZATION OF FILE CHOOSER (FOR OPEN THE IMAGES)   
  chooser = new JFileChooser();
      FileNameExtensionFilter filter = new FileNameExtensionFilter("Image files", "jpg", "gif","bmp","png");
      chooser.setFileFilter(filter);
      chooser.setMultiSelectionEnabled(false);
  area.requestFocus();
  
}

// METHOD THAT GETS THE CLICK OF THE USER INSIDE A ITEM OF THE MENU

 public void actionPerformed(ActionEvent e){
  
   //OPEN FILE
  if (e.getSource() == boton1){
            OpensImage();
            area.repaint();
            area.imageLoaded=true;
            validate();
             }
  
  //BLACK AND WHITE FILTER
  if (e.getSource() == BAW){
            //System.out.println(filename);
            if (area.edited) { // IF THE IMAGE IS ALREADY EDITED, WE CONTINUE TO EDIT THAT ONE
            area1=area.new Filter(1);
            pool.invoke(area1); // INVOKES THREADPOOL
            }else{
            area1=area.new Filter(filename, "c1", 1); // IF THE IMAGE DOESN�T HAVE ANY FILTER WE NEED TO INITIALIZE IT AS A BUFFERED IMAGE TO START APPLYING FILTERS
            pool.invoke(area1); // INVOKES THREADPOOL 
            }
            area.edited=true;
             }
  
  else if(e.getSource() == filter2) {
	  System.out.println(filename);
	  if (area.edited) {
	  area1=area.new Filter(2);
	  pool.invoke(area1); // INVOKES THREADPOOL
	  }else{
	  area1=area.new Filter(filename, "c1", 2);
	  pool.invoke(area1); // INVOKES THREADPOOL
	  }
	  area.edited=true;
  }
  
  else if(e.getSource() == filter3) {
	  System.out.println(filename);
	  if (area.edited) {
	  area1=area.new Filter(3);
	  pool.invoke(area1);  // INVOKES THREADPOOL
	  }else{
	  area1=area.new Filter(filename, "c1", 3);
	  pool.invoke(area1);  // INVOKES THREADPOOL
	  }
	  area.edited=true;
  }
  else if(e.getSource() == saveas){
	  SaveFileDialog();     
   }
   
  } 
      
// SHOWS THE WINDOW WITH THE FILE EXPLORER (CLICK ON "OPEN")
 public void OpensImage(){
	 int returnVal = chooser.showOpenDialog(this);
     if(returnVal == JFileChooser.APPROVE_OPTION) {   
    	 filename=chooser.getSelectedFile().toString();
    	 area.prepareImage_ToBeDisplayed(filename);
         saveas.setVisible(true);
         BAW.setVisible(true);
         filter2.setVisible(true);
         filter3.setVisible(true);
     }      
  }

 // SHOWS THE WINDOW TO SAVE THE IMAGE (CLICK ON SAVE AS)
 public void SaveFileDialog(){
	 int returnVal = chooser.showSaveDialog(this);
	 if(returnVal == JFileChooser.APPROVE_OPTION) {  
		 String filen=chooser.getSelectedFile().toString(); 
                 //System.out.println(filen);
         area.saveToFile(filen);  
     }
 }
  
	public static void main(String[] args) {
		// INTANCE OF A JFRAME 
		Window myWindow = new Window();
		// SHOWS THE WINDOW
		myWindow.setVisible(true);
	}
}