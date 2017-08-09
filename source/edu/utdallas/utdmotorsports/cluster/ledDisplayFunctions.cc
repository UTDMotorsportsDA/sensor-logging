// Author: Michael Nelson
// Date: 4/4/2017
// ledDisplayFunctions - JNI C++ functions that display 1 of 3 screens needed for UTD SAE instrument cluster.
// Note: led-matirx defines the top-left LED on the display as (0, 0)


#include "led-matrix.h"
#include "graphics.h"
#include "main.h"
#include <iostream>
#include <unistd.h>
#include <math.h>
#include <stdio.h>
#include <signal.h>
#include <string.h>
#include <stdlib.h>


using namespace rgb_matrix;


volatile bool interrupt_received = false;
const Color color1(255, 150, 0);
const Color color2(0, 150, 150);
const char *bdf_font_file = "6x9.bdf";


void InterruptHandler(int signo) {interrupt_received=true;}


JNIEXPORT jlong JNICALL Java_main_createCanvasForLedDisplay(JNIEnv *env, jobject object)
{
	RGBMatrix::Options defaults;
	RuntimeOptions runtime;
	defaults.hardware_mapping = "adafruit-hat";
	defaults.rows = 16;
	defaults.chain_length = 1;
	defaults.parallel = 1;
	defaults.show_refresh_rate = false;
	runtime.gpio_slowdown = 1;
	runtime.daemon = 0;
	runtime.drop_privileges = 0;
	Canvas *canvas = rgb_matrix::CreateMatrixFromOptions(defaults, runtime);
	return (jlong)canvas;
}


JNIEXPORT void JNICALL Java_main_deleteCanvas(JNIEnv *env, jobject object, jlong canvasLong)
{
	Canvas *canvas = (Canvas*)canvasLong;
	canvas->Clear();
	delete canvas;
}


JNIEXPORT void JNICALL Java_main_displaySpeedAndRpm(JNIEnv *env, jobject object, int speed, int rpm, jlong canvasLong)
{
	signal(SIGTERM, InterruptHandler);
	signal(SIGINT, InterruptHandler);

	Canvas *canvas = (Canvas*)canvasLong;
	canvas->Clear();

	Font font;
	font.LoadFont(bdf_font_file);
	std::string s = std::to_string(speed);
	s = s  + "m";
	std::string r = std::to_string(rpm);
	const char *thespeed = s.c_str();
	const char *therpm = r.c_str();

	// Displays values starting at right side of display
	DrawText(canvas, font, int(20 - 6*floor(log10(speed))), 7, color1, NULL, thespeed);
	DrawText(canvas, font, int(26 - 6*floor(log10(rpm))), 14, color2, NULL, therpm);
}


JNIEXPORT void JNICALL Java_main_displayTemperature(JNIEnv *env, jobject object, int temp, jlong canvasLong)
{
	signal(SIGTERM, InterruptHandler);
	signal(SIGINT, InterruptHandler);

	Canvas *canvas = (Canvas*)canvasLong;
	canvas->Clear();

	Font font;
	font.LoadFont(bdf_font_file);
	std::string t = std::to_string(temp);
	const char *thetemp = t.c_str();
	std::string farenheit = "F";
	const char *farenheitSymbol = farenheit.c_str();
	// Displays values starting at right of screen
	DrawText(canvas, font, int(20 - 6*floor(log10(temp))), 11, color2, NULL, thetemp);
	DrawText(canvas, font, 26, 11, color1, NULL, farenheitSymbol);
}


// Defined for abs(x) <= 7, abs(y) <=3
JNIEXPORT void JNICALL Java_main_displayGForceGrid(JNIEnv *env, jobject object, int x, int y, jlong canvasLong)
{
	signal(SIGTERM, InterruptHandler);
	signal(SIGINT, InterruptHandler);

	Canvas *canvas = (Canvas*)canvasLong;
	canvas->Clear();

	Color color(200, 0, 0);
	DrawLine(canvas, 15, 0, 15, 15, color); // Left vertical axis
	DrawLine(canvas, 16, 0, 16, 15, color); // Right vertical axis
	DrawLine(canvas, 0, 7, 31, 7, color); // Upper horizontal axis
	DrawLine(canvas, 0, 8, 31, 8, color); // Lower horizontal axis

	// GForce dot takes up 4 pixels
	const int xOrigin = 16;
	const int yOrigin = 8;
	const int bottomRowCoordinate = -2*y + yOrigin;
	const int topRowCoordinate = bottomRowCoordinate -1;
	const int rightColumnCoordinate = 2*x + xOrigin;
	const int leftColumnCoordinate = rightColumnCoordinate - 1;

	canvas->SetPixel(rightColumnCoordinate, bottomRowCoordinate, 0, 0, 200);
	canvas->SetPixel(rightColumnCoordinate, topRowCoordinate, 0, 0, 200);
	canvas->SetPixel(leftColumnCoordinate, bottomRowCoordinate, 0, 0, 200);
	canvas->SetPixel(leftColumnCoordinate, topRowCoordinate, 0, 0, 200);
}
