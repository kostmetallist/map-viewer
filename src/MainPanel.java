import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import java.io.File;
// import java.io.FileWriter;
// import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileNotFoundException;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;


class Point2d {

    public double x;
    public double y;

    public Point2d(double x, double y) {

        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return x + "," + y;
    }
}


class MapPanel extends JPanel {

    private double xFrom;
    private double yFrom;
    private double xRange;
    private double yRange;
    private double scaleFactor = 2;

    // contains numerous lists with points representing 
    // polygone vertices
    private List<List<Point2d>> data;


    public MapPanel() {

        final JPanel thisPanel = this;
        this.data = new ArrayList<>();
        this.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent me) {

                int xClicked = me.getX();
                int yClicked = me.getY();
                // System.out.println("clicked " + xClicked + "," + yClicked);
                int panelWidth = thisPanel.getWidth();
                int panelHeight = thisPanel.getHeight();

                double xRealClicked = screen2real(xClicked, 
                    0, panelWidth, xFrom, xFrom+xRange);
                double yRealClicked = screen2real(panelHeight-yClicked, 
                    0, panelHeight, yFrom, yFrom+yRange);

                // alteration relative to center
                xFrom += xRealClicked - (xFrom + xRange/2);
                yFrom += yRealClicked - (yFrom + yRange/2);
                redraw();
            }
        });
        this.addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent mwe) {

                // getParent().dispatchEvent(mwe);
                int rotations = mwe.getWheelRotation();
                changeZoom(rotations);
                // wheel up
                // if (rotations < 0) {

                //     xRange /= scaleFactor;
                //     xFrom += xRange/scaleFactor;
                //     yRange /= scaleFactor;
                //     yFrom += yRange/scaleFactor;
                // }

                // else {

                //     xFrom -= xRange/scaleFactor;
                //     xRange *= scaleFactor;
                //     yFrom -= yRange/scaleFactor;
                //     yRange *= scaleFactor;
                // }

                // redraw();
            }
        });
    }

    // zoomDirection < 0 => zooming in
    // else zooming out
    public void changeZoom(int zoomDirection) {

        if (zoomDirection < 0) {

            xRange /= scaleFactor;
            xFrom += xRange/scaleFactor;
            yRange /= scaleFactor;
            yFrom += yRange/scaleFactor;
        }

        else {

            xFrom -= xRange/scaleFactor;
            xRange *= scaleFactor;
            yFrom -= yRange/scaleFactor;
            yRange *= scaleFactor;
        }

        redraw();
    }

    public void fillMapData(List<List<Point2d>> data) {

        if (!data.isEmpty()) {

            this.data = data;
            int nonEmptyIdx = 0;
            for (List<Point2d> subList : data) {
            	if (!subList.isEmpty()) {
            		break;
            	}

            	nonEmptyIdx++;
            }

            if (nonEmptyIdx == data.size()-1) {
            	System.err.println("fillMapData: not detected initial scaling");
            	return;
            }

            double xMin = data.get(nonEmptyIdx).get(0).x,
                xMax = data.get(nonEmptyIdx).get(0).x,
                yMin = data.get(nonEmptyIdx).get(0).y, 
                yMax = data.get(nonEmptyIdx).get(0).y;

            for (List<Point2d> subList : data) {
            	for (Point2d each : subList) {

	                if (each.x < xMin)
	                    xMin = each.x;
	                if (each.x > xMax)
	                    xMax = each.x;
	                if (each.y < yMin)
	                    yMin = each.y;
	                if (each.y > yMax)
	                    yMax = each.y;
            	}
            }

            this.xFrom = xMin;
            this.xRange = xMax - xMin;
            this.yFrom = yMin;
            this.yRange = yMax - yMin;
        }
    }

    public void redraw() {
        this.repaint();
    }

    private double screen2real(int screenVal, int screenFrom, int screenTo,
        double realFrom, double realTo) {

        if (screenFrom > screenTo || screenVal < screenFrom ||
            screenVal > screenTo || realFrom > realTo) {

            // System.err.println("screen2real: invalid arguments");
            return 0;
        }

        int screenTotal = screenTo - screenFrom;
        double realTotal = realTo - realFrom;
        return realTo - (screenTo-screenVal)*realTotal/screenTotal;
    }

    private int real2screen(double realVal, double realFrom, double realTo, 
        int screenFrom, int screenTo) {

        if (realFrom > realTo || realVal < realFrom ||
            realVal > realTo || screenFrom > screenTo) {

            // System.err.println("real2screen: invalid arguments");
            return (realVal>realTo)? screenTo: 
                (realVal<realFrom? screenFrom: 0);
        }

        int screenTotal = screenTo - screenFrom;
        double realTotal = realTo - realFrom;
        return (int) (screenTo - (realTo-realVal)*screenTotal/realTotal);
    }

    private void drawPoint(Graphics g, int screenX, int screenY) {
        g.drawLine(screenX, screenY, screenX+1, screenY+1);
        g.drawLine(screenX, screenY+1, screenX+1, screenY);
    }

    private void drawRegion(Graphics g, List<Point2d> regionPoints, 
        int mapWidth, int mapHeight) {

        int pointNumber = regionPoints.size();
        int[] xDots = new int[pointNumber], 
            yDots = new int[pointNumber];

        for (int i = 0; i < pointNumber; i++) {

            Point2d point = regionPoints.get(i);
            double x = point.x, y = point.y;

            xDots[i] = real2screen(x, this.xFrom, 
                this.xFrom+this.xRange, 0, mapWidth);
            yDots[i] = mapHeight - real2screen(y, this.yFrom, 
                this.yFrom+this.yRange, 0, mapHeight);
        }

        Polygon region = new Polygon(xDots, yDots, pointNumber);
        g.fillPolygon(region);
    }

    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        int mapWidth = this.getWidth();
        int mapHeight = this.getHeight();

        // dark green
        g2d.setColor(new Color(0, 102, 34));
        for (List<Point2d> polygonList : this.data) {
            drawRegion(g2d, polygonList, mapWidth, mapHeight);
	    }

        // map coordinate lines
        int verticalLinesNum = 10;
        int verticalH = mapWidth/verticalLinesNum;
        int vertCoord = verticalH - verticalH/2;
        g2d.setFont(new Font(Font.DIALOG, Font.PLAIN, 10));
        for (int i = 0; i < verticalLinesNum; i++) { 

            double appropriateX = this.screen2real(vertCoord, 0, mapWidth, 
                xFrom, xFrom+xRange);
            g2d.setColor(new Color(0, 0, 0, 45));
            g2d.drawLine(vertCoord, mapHeight, vertCoord, 0);
            g2d.setColor(new Color(0, 0, 0, 190));
            g2d.drawString(String.format("%.2f", appropriateX), 
                vertCoord, mapHeight);
            vertCoord += verticalH;
        }

        int horizontalLinesNum = 10;
        int horizontalH = mapHeight/horizontalLinesNum;
        int horCoord = mapHeight - horizontalH + horizontalH/2;
        for (int i = 0; i < horizontalLinesNum; i++) { 

            double appropriateY = this.screen2real(mapHeight-horCoord, 0, 
                mapHeight, yFrom, yFrom+yRange);
            g2d.setColor(new Color(0, 0, 0, 65));
            g2d.drawLine(0, horCoord, mapWidth, horCoord);
            g2d.setColor(new Color(0, 0, 0, 220));
            g2d.drawString(String.format("%.2f", appropriateY), 
                0, horCoord);
            horCoord -= horizontalH;
        }
    }
}


public class MainPanel extends JPanel {

    private static Random rand = new Random();
    private static final int MAIN_WINDOW_WIDTH = 300;
    private static final int MAIN_WINDOW_HEIGHT = 150;

    private static enum ScanMode {
        DATA_SEARCH,
        REGION_SEARCH,
        COORDS_SEARCH,
    }


    public List<List<Point2d>> readMapDataFile(String path) {

        FileReader fr = null;
        BufferedReader br = null;
        List<List<Point2d>> result = new ArrayList<>();

        try {

            File file = new File(path);   
            fr = new FileReader(file);
            br = new BufferedReader(fr);

            // must be initialized later
            // TODO in region loop
            int regionLineIdx = -1, 
            	regionLineNum = 0;

            List<Point2d> regionList = null;
            ScanMode mode = ScanMode.DATA_SEARCH;
            String line = br.readLine();
            while (line != null) {

            	// System.out.println(line);
            	if (mode == ScanMode.DATA_SEARCH) {
                    if (line.equals("Data")) {

                        mode = ScanMode.REGION_SEARCH;
                        line = br.readLine();
                        if (line == null || !line.trim().equals("")) {
                            System.err.println("readMapDataFile: " + 
                                "no empty line specified after \"Data\"");
                            break;
                        }
                    }
                }

                else if (mode == ScanMode.REGION_SEARCH) {

                    if (line.trim().startsWith("Region")) {

                        mode = ScanMode.COORDS_SEARCH;
                        line = br.readLine();
                        if (line == null) {
                        	System.err.println("readMapDataFile: no line " + 
                        		"number specified after \"Region\"");
                        	break;
                        }

                        regionLineIdx = 0;
                        regionLineNum = Integer.parseInt(line.trim());
                        regionList = new ArrayList<>();
                    }
                }

                else if (mode == ScanMode.COORDS_SEARCH) {

                	if (regionLineIdx < regionLineNum) {

	                	String[] splitResult = line.trim().split(" ");
	                    Point2d point = 
	                        new Point2d(Double.parseDouble(splitResult[0]), 
	                                Double.parseDouble(splitResult[1]));

	                    regionList.add(point);
	                    regionLineIdx++;
	                }

	                else {

	                	mode = ScanMode.REGION_SEARCH;
	                	result.add(regionList);
	                }
                }

                line = br.readLine();
            }

            // final state
            switch (mode) {

				case DATA_SEARCH: 

	                System.err.println("readMapDataFile: no \"data\"" + 
	                	" line found");
	                break;

	            // case in which didn't find appendix info after coords
	            case COORDS_SEARCH: 
	            	result.add(regionList);
            }
        }

        catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        catch (IOException e) {
            e.printStackTrace();
        }

        finally {

            try {
                br.close();
                fr.close();
            }

            catch (NullPointerException e) {}

            catch (IOException e) {
                e.printStackTrace();
            }

            finally {
                return result;
            }
        }
    }


    public MainPanel() {

        super(new GridLayout(1, 1));

        final JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            ".MIF", "mif");
        chooser.setFileFilter(filter);

        final MainPanel thisPanel = this;
        JButton buttonLoad = new JButton("Open...");
        this.add(buttonLoad);
        buttonLoad.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actEvent) {

                int returnVal = chooser.showOpenDialog(thisPanel);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    System.out.println("opening file " +
                        chooser.getSelectedFile().getName());
                }

                List<List<Point2d>> dataList = thisPanel.readMapDataFile(
                    chooser.getSelectedFile().getAbsolutePath());

                if (dataList.isEmpty()) {
                    System.err.println("MainPanel: no significant data read");
                    return;
                }

                JFrame mapFrame = new JFrame();
                MapPanel mapPanel = new MapPanel();
                mapPanel.fillMapData(dataList);
                mapFrame.add(BorderLayout.CENTER, mapPanel);

                JPanel toolPanel = new JPanel(new GridLayout(10, 1, 2, 2));
                JButton zoomInButton = new JButton("+");
                toolPanel.add(zoomInButton);
                zoomInButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        mapPanel.changeZoom(-1);
                    }
                });
                JButton zoomOutButton = new JButton("-");
                toolPanel.add(zoomOutButton);
                zoomOutButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        mapPanel.changeZoom(1);
                    }
                });
                mapFrame.add(BorderLayout.EAST, toolPanel);
                mapFrame.setSize(800, 800);
                mapFrame.setVisible(true);

                Point mainPosition = thisPanel.getLocationOnScreen();
                mapFrame.setLocation(
                    (int) mainPosition.getX()+MAIN_WINDOW_WIDTH+
                        5+rand.nextInt(15), 
                    (int) mainPosition.getY()+5+rand.nextInt(15));
                mapFrame.setTitle("2d Map");
            }
        });
    }

    public static void main(String[] args) {

        final MainPanel mainPanel = new MainPanel();
        JFrame frame = new JFrame();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(mainPanel);
        frame.setSize(MAIN_WINDOW_WIDTH, MAIN_WINDOW_HEIGHT);
        frame.setTitle("Map file manager");  
        frame.setVisible(true);
    }
}