package com.umitsilwal.stegogram;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;

import com.umitsilwal.stegogram.Utils.Constants;
import com.umitsilwal.stegogram.Utils.HelperMethods;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

public class Steganography {

    public static String DecodeMessage(Bitmap stegoImage) {

        int width = stegoImage.getWidth();
        int height = stegoImage.getHeight();

        int typePixel = stegoImage.getPixel(0, 0);
        int tRed = Color.red(typePixel);
        int tGreen = Color.green(typePixel);
        int tBlue = Color.blue(typePixel);

        //Constants.COLOR_RGB_TEXT
        if (!(tRed == 135 && tGreen == 197 && tBlue == 245)) {
            return "";
        }

        StringBuilder sb = new StringBuilder();


        outerloop:
        for (int x = 0; x < width; ++x) {
            for (int y = 1; y < height; ++y) {
                int pixel = stegoImage.getPixel(x, y);

                int colors[] = {Color.red(pixel), Color.green(pixel), Color.blue(pixel)};

                //Colors.COLOR_RGB_END
                if (colors[0] == 96 && colors[1] == 62 && colors[2] == 148) {
                    break outerloop;
                } else {
                    for (int c = 0; c < 3; c++) {
                        int lsb = LSB(colors[c]);
                        sb.append(lsb);
                    }
                }
            }
        }

        String sm = sb.toString();
        int sL = sm.length();

        //Cut unnecessary [0-7] pixels
        sm = sm.substring(0, sL - sL % 8);

        return sm;
    }

    public static File EncodeMessage(File input, String secretText) {
        if(secretText.length() <= 0) return input;
        //convert path into bitmap object
        Bitmap coverImage = BitmapFactory.decodeFile(input.getPath());

        //make a new bitmap based on the dimensions of this bitmap
        // ARGB_8888 - Each pixel is stored on 4 bytes.
        Bitmap stegoImage = coverImage.copy(Bitmap.Config.ARGB_8888, true);

        stegoImage.setPremultiplied(false);

        //convert text into binary stream
        String sTextInBin = HelperMethods.stringToBinaryStream(secretText);

        int secretMessageLen = sTextInBin.length();
        int action, embMesPos = 0;

        int width = coverImage.getWidth();
        int height = coverImage.getHeight();

        //If secret message is too long (3 bits in each pixel + skipping of some pixels)
        if (secretMessageLen > width * height) {
            return null;
        }

        //To check if secret message is text. (0,0,COLOR_RGB_TEXT)
        stegoImage.setPixel(0, 0, Constants.COLOR_RGB_TEXT);

        int endX = 0, endY = 1;

        outerloop:
        for (int x = 0; x < width; x++) {
            for (int y = 1; y < height; y++) {
                int pixel = coverImage.getPixel(x, y);

                if (embMesPos < secretMessageLen) {
                    int colors[] = {Color.red(pixel), Color.green(pixel), Color.blue(pixel)};

                    for (int c = 0; c < 3; c++) {
                        if (embMesPos == secretMessageLen) {
                            break;
                        }

                        action = action(colors[c], sTextInBin.charAt(embMesPos));
                        colors[c] += action;
                        embMesPos++;
                    }

                    int newPixel = Color.rgb(colors[0], colors[1], colors[2]);
                    stegoImage.setPixel(x, y, newPixel);
                } else {

                    if (y < height - 1) {
                        endX = x;
                        endY = y + 1;
                    } else if (endX < width - 1) {
                        endX = x + 1;
                        endY = y;
                    } else {
                        endX = width - 1;
                        endY = height - 1;
                    }

                    break outerloop;
                }
            }
        }

        //End of secret message flag. (0,2,COLOR_RGB_END)
        stegoImage.setPixel(endX, endY, Constants.COLOR_RGB_END);

        try {
            OutputStream os = new BufferedOutputStream(new FileOutputStream(input));
            stegoImage.compress(Bitmap.CompressFormat.PNG, 100, os);
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return input;
    }

    private static int LSB(int number) {
        return number & 1;
    }


    private static int action(int color, char bit) {
        if (LSB(color) == 1 && bit == '0') {
            return -1;
        } else if (LSB(color) == 0 && bit == '1') {
            return 1;
        } else {
            return 0;
        }
    }
}
