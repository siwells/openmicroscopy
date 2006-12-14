package src.adminTool.main;import java.awt.Image;import javax.swing.ImageIcon;import javax.swing.UIManager;/* * @(#)Splasher.java  2.0  January 31, 2004 * * Copyright (c) 2003-2004 Werner Randelshofer * Staldenmattweg 2, Immensee, CH-6405, Switzerland. * This software is in the public domain. */import src.adminTool.ui.ImageFactory;/** * * @author  werni */public class Splasher {    /**     * Shows the splash screen, launches the application and then disposes     * the splash screen.     * @param args the command line arguments     */    public static void main(String[] args) {                String laf = UIManager.getSystemLookAndFeelClassName() ;        //laf = "ch.randelshofer.quaqua.QuaquaLookAndFeel";        //laf = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";        //laf = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";        //laf = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";        //laf = "javax.swing.plaf.metal.MetalLookAndFeel";        if (laf.equals("apple.laf.AquaLookAndFeel"))        {            System.setProperty("Quaqua.design", "panther");                        try {                UIManager.setLookAndFeel(                    "ch.randelshofer.quaqua.QuaquaLookAndFeel"                );           } catch (Exception e) { System.err.println(laf + " not supported.");}        } else {            try {                UIManager.setLookAndFeel(laf);            } catch (Exception e)             { System.err.println(laf + " not supported."); }        }                //Image image = new ImageIcon("resources/graphx/AdminSplash.png").getImage();        //SplashWindow.splash(Splasher.class.getResource("/resources/graphx/AdminSplash.png"));        SplashWindow.splash(ImageFactory.get().image(ImageFactory.SPLASH_SCREEN).getImage());        try        {            Thread.sleep(1000);        } catch (InterruptedException e)  {}        SplashWindow.disposeSplash();        SplashWindow.invokeMain("src.adminTool.main.AdminMain", args);    }    }