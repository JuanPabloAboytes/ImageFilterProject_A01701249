/*

Copyright (C) 2019 Juan Pablo Aboytes Novoa

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

*/
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import static java.util.concurrent.ForkJoinTask.invokeAll;
import java.util.concurrent.RecursiveAction;
import javax.imageio.ImageIO;

// CLASS IMGAREA IS THE ONE THAT MANIPULATES THE IMAGE
class Image_displayed extends Canvas{
	// DECLARATION OF THE VARIABLES NEEDED
	Image orImg;
	BufferedImage orBufferedImage; // WILL ALWAYS KEEP THE ORIGINAL IMAGE
	BufferedImage bimg; // INTERACTS WITH THREADPOOL FILTERS
	BufferedImage img; // IS WHERE THE ACTUAL IMAGE IS STORED
	Dimension ds;
	int mX;
	int mY;
	int x;
	int y;
	static boolean imageLoaded;
	boolean drawn;
	boolean edited;
	MediaTracker mt;
	String imgFileName;
	int filter;
        
  // CREATES THE CANVAS BASED ON THE SCREEN SIZE
  public Image_displayed() {
     ds=getToolkit().getScreenSize(); 
     mX=(int)ds.getWidth()/2; 
     mY=(int)ds.getHeight()/2;
  }	  
	  
public void paint(Graphics g){
	Graphics2D g2d=(Graphics2D)g; //create Graphics2D object   
	if(imageLoaded){ // DRAWS THE UPDATED IMAGE
		if(drawn ){
                    x=mX-bimg.getWidth()/2;
		    y=mY-bimg.getHeight()/2;
		    g2d.translate(x,y);  
		    g2d.drawImage(bimg,0,0,null); // DRAWS THE UODATED IMAGE IN THE CANVAS 
		 }else{ // DRAWS THE SOURCE ORIGINAL IMAGE
		    x=mX-orBufferedImage.getWidth()/2;
		    y=mY-orBufferedImage.getHeight()/2;
		    g2d.translate(x,y); 
		    g2d.drawImage(orBufferedImage,0,0,null); // DRAWS ORIGINAL IMAGE
		 }
	}
	g2d.dispose(); // CLEANS
}

// PREPARES IMAGE TO BE DISPLAYED
public void prepareImage_ToBeDisplayed(String filename){
   imageLoaded=false; 
   drawn=false;
   edited=false;
   try{
	   mt=new MediaTracker(this);    
	   orImg=Toolkit.getDefaultToolkit().getImage(filename); 
	   mt.addImage(orImg,0);
	   mt.waitForID(0); 
	   int width=orImg.getWidth(null); // GET IMAGE WIDTH
	   int height=orImg.getHeight(null); // GET IMAGE HEIGHT
	   orBufferedImage=create_BufferedImage_FromImage(orImg,width,height,false); // CREATES BUFFERED IMAGE
	   bimg = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);  // THIS BUFFEREDIMAGE WILL STORE THE IMG WITH FILTERS
	   imageLoaded=true; 
	   }catch(Exception e){System.exit(-1);}
}

// METHOD THAT MAKES A COPY OF A BUFFERED IMAGE
 public BufferedImage create_BufferedImage_FromImage(Image image, int width, int height, boolean tran){ 
	 BufferedImage dest ;
	 if(tran) 
		 dest = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	 else
		 dest = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
   Graphics2D g2 = dest.createGraphics();
   g2.drawImage(image, 0, 0, null); // DRAWS IMAGE
   g2.dispose();  // CLEANS
   return dest; // RETURNS THE DEST IMAGE
}

// BUFFERED IMAGE TO FILE
 public void saveToFile(String filename){
	 String ftype=filename.substring(filename.lastIndexOf('.')+1);
	 try{
		 if(drawn)
			 ImageIO.write(bimg,ftype,new File(filename));
     }catch(IOException e){System.out.println("Error in saving the file");}
  }

 // REPAINTS THE CANVA
  public void act(BufferedImage im){
	  this.bimg = im;
	  drawn = true;
	  repaint();
	  }
	  
//CLASS WITH THE FILTERS.
//IT SPLITS THE IMAGE AND DIVIDES IT IN THREADS TO EXECUTE THE SAME TASK WITH DIFFERENT PARTS OF THE IMAGE AT THE SAME TIME
    class Filter extends RecursiveAction {

	private static final long serialVersionUID = 1L;
        private int mStart;
        private int mLength;
        BufferedImage img;
        BufferedImage dst;
  	
 // CLASS CONSTRUCTOR  PARA FILTERIMAGE EN actionPerformed DE WINDOW CLASS
  	public Filter (String src, String dst, int num) {
  	    try {
  	    	img = ImageIO.read(new File(src));
  	    }catch(Exception e) {
  	    	System.out.println(e.getMessage());
  	    }
  	    filter=num;
            mStart = 0;
  	    mLength =img.getWidth();
  	    this.dst = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB); 
  	    bimg=this.dst;
  	    act(bimg); // UPDATESX IMAGE IN CANVAS
  	}

  	//CLASS CONSTRUCTOR    I CALL THIS FROM WINDOW, IS THE NUMBER THAT YOU USE IN THE COMPUTE DIRECTLY
  	public Filter (int num) {	
            filter=num;
            img=bimg;
            mStart = 0;
  	    mLength =img.getWidth();
  	    this.dst = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB); 
  	    bimg=this.dst;
  	    act(bimg);  	   
  	}

  	//CLASS CONSTRUCTOR  FOR INVOKEALL CALL
  	public Filter(BufferedImage src, int start, int length, BufferedImage dst) {
  	    img = src;
  	    mStart = start;
  	    mLength = length;
  	    this.dst = dst;
  	}

  	protected void computeDirectly() {
  		int heigth=img.getHeight();        	
  	    	if (filter==1) {
  	    		for (int i = mStart; i < mStart + mLength; i++) {
  		            for (int j = 0; j < heigth; j++) {
  		            	int rgb = img.getRGB(i, j);  // rgb contian all number coded within a single integer concatenaed as red/green/blue
  			            int red = rgb & 0xFF;  // & uses  0000000111 with the rgb number to eliminate all the bits but the las 3 (which represent 8 position which are used for 0 to 255 values) 
  			            int green = (rgb >> 8) & 0xFF;  // >> Bitwise shifts 8 positions  & makes  uses  0000000111 with the number and eliminates the rest
  			            int blue = (rgb >> 16) & 0xFF; // >> Bitwise shifts 16 positions  & makes  uses  0000000111 with the number and eliminates the rest
  			            float L = (float) (0.2126 * (float) red + 0.7152 * (float) green + 0.0722 * (float) blue);
  			            int color;
  			            color = 153 * (int) L / 255;
  			            color = (color << 8) | 153 * (int) L / 255;
  			            color = (color << 8) | 153 * (int) L / 255;
  			            dst.setRGB(i, j, color); // sets the pixeles to specified color  (negative image)
  			            }
  		        }
                        //yellow
  	       } else if (filter ==2) {
  	    	   for (int i = mStart; i < mStart + mLength; i++) {
  	    		   for (int j = 0; j < heigth; j++) {
  	    			   int rgb = img.getRGB(i, j);  // rgb contian all number coded within a single integer concatenaed as red/green/blue
  			            int red = rgb & 0xFF;  // & uses  0000000111 with the rgb number to eliminate all the bits but the las 3 (which represent 8 position which are used for 0 to 255 values) 
  			            int green = (rgb >> 8) & 0xFF;  // >> Bitwise shifts 8 positions  & makes  uses  0000000111 with the number and eliminates the rest
  			            int blue = (rgb >> 16) & 0xFF; // >> Bitwise shifts 16 positions  & makes  uses  0000000111 with the number and eliminates the rest
  		                float L = (float) (0.3000 * (float) red + 0.6152 * (float) green + 0.2000 * (float) blue);
  		                int color;
  		                color = 234 * (int) L / 255;
  		                color = (color << 8) | 176 * (int) L / 255;
  		                color = (color << 8) | 3 * (int) L / 255;
  		                dst.setRGB(i, j, color); // sets the pixeles to specified color  (negative image) 
  		            }
  	    	   } //purple
  	       } else if (filter ==3) {
  	    	   for (int i = mStart; i < mStart + mLength; i++) {
  		            for (int j = 0; j < heigth; j++) {
  		            	int rgb = img.getRGB(i, j);  // rgb contian all number coded within a single integer concatenaed as red/green/blue
  		                int red = rgb & 0xFF;  // & uses  0000000111 with the rgb number to eliminate all the bits but the las 3 (which represent 8 position which are used for 0 to 255 values) 
  		                int green = (rgb >> 8) & 0xFF;  // >> Bitwise shifts 8 positions  & makes  uses  0000000111 with the number and eliminates the rest
  		                int blue = (rgb >> 16) & 0xFF; // >> Bitwise shifts 16 positions  & makes  uses  0000000111 with the number and eliminates the rest
  		                float L = (float) (0.7000 * (float) red + 0.3200 * (float) green + 0.1000 * (float) blue);
  		                int color;
  		                color = 230 * (int) L / 255;
  		                color = (color << 8) | 200 * (int) L / 255;
  		                color = (color << 8) | 260 * (int) L / 255;	
  		                dst.setRGB(i, j, color); // sets the pixeles to specified color  (negative image) 	        		
  			         }
  	            }
  	       }  
  	    
 }
  			    
int sThreshold = 1000;

// METHOD THAT DIVIDES THE PROBLEM AND ASSIGNS IT TO THREADS
protected void compute() {
	if (mLength < sThreshold) { 
	    computeDirectly(); 
	    return;
	}  
    int split = mLength / 2;
    invokeAll(new Filter(img, mStart, split, dst), new Filter(img, mStart + split, mLength - split, dst)); // IF THE SIZE IS NOT SNALL ENOUGH, DIVIDE IT AGAIN
}
}

}
