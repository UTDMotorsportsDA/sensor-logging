// Author: Michael Nelson
// Date: 7/30/2017
// main - Consolidates all UTD SAE instrument cluster functionality and drives LED display with ledScreenDisplays library

import java.io.*;
import java.util.*;


public class main
{
	static private long canvas;

	private native long createCanvasForLedDisplay();
	private native void deleteCanvas(long canvas);
	private native void displaySpeedAndRpm(int speed, int rpm, long theCanvas);
	private native void displayTemperature(int temperature, long theCanvas);
	private native void displayGForceGrid(int x, int y, long theCanvas);


	public static void main(String[] args)
	{
		System.loadLibrary("LedDisplayFunctions");
		main functions = new main();
		canvas = functions.createCanvasForLedDisplay();

		boolean finished = false;
		while (!finished)
		{
			System.out.printf("\nType 1 for speed and rpm");
			System.out.printf("\nType 2 for temperature");
			System.out.printf("\nType 3 for G-force grid\n");
			Scanner in = new Scanner(System.in);
			int selection = in.nextInt();
			switch (selection)
			{
				case 1:	System.out.println("What speed?");
					int speed = in.nextInt();
					System.out.println("What rpm?");
					int rpm = in.nextInt();
					functions.displaySpeedAndRpm(speed, rpm, canvas);
					break;
				case 2:	System.out.println("What temperature?");
					int temperature = in.nextInt();
					functions.displayTemperature(temperature, canvas);
					break;
				case 3:	System.out.println("Where's x?");
					int x = in.nextInt();
					System.out.println("Where's y?");
					int y = in.nextInt();
					functions.displayGForceGrid(x, y, canvas);
					break;
				default:	finished = true;
			}
		}
		functions.deleteCanvas(canvas);
	}
}

