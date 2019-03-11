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

        int key[] = new int[24];

        //Extract Key
        int keyPixel = stegoImage.getPixel(0, 0);

        int red = Color.red(keyPixel);
        int green = Color.green(keyPixel);
        int blue = Color.blue(keyPixel);

        Log.d("EXT", "Key2: " + red + " " + green + " " + blue);

        String red_bin = Integer.toBinaryString(red);
        red_bin = "00000000" + red_bin;
        red_bin = red_bin.substring(red_bin.length() - 8);

        for (int i = 0; i <= 7; i++) {
            key[i] = (red_bin.charAt(i) == '1' ? 1 : 0);
        }

        String green_bin = Integer.toBinaryString(green);
        green_bin = "00000000" + green_bin;
        green_bin = green_bin.substring(green_bin.length() - 8);

        for (int i = 0; i <= 7; i++) {
            key[i + 8] = (green_bin.charAt(i) == '1' ? 1 : 0);
        }

        String blue_bin = Integer.toBinaryString(blue);
        blue_bin = "00000000" + blue_bin;
        blue_bin = blue_bin.substring(blue_bin.length() - 8);

        for (int i = 0; i <= 7; i++) {
            key[i + 16] = (blue_bin.charAt(i) == '1' ? 1 : 0);
        }

        int typePixel = stegoImage.getPixel(0, 1);
        int tRed = Color.red(typePixel);
        int tGreen = Color.green(typePixel);
        int tBlue = Color.blue(typePixel);

        //Constants.COLOR_RGB_TEXT
        if (!(tRed == 135 && tGreen == 197 && tBlue == 245)) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        int keyPos = 0;
        outerloop:
        for (int x = 0; x < width; ++x) {
            for (int y = 2; y < height; ++y) {
                int pixel = stegoImage.getPixel(x, y);

                int colors[] = {Color.red(pixel), Color.green(pixel), Color.blue(pixel)};

                //Colors.COLOR_RGB_END
                if (colors[0] == 96 && colors[1] == 62 && colors[2] == 148) {
                    break outerloop;
                } else {

                    for (int c = 0; c < 3; c++) {

                        if ((key[keyPos] ^ LSB2(colors[c])) == 1) {
                            int lsb = LSB(colors[c]);
                            sb.append(lsb);
                            keyPos = (keyPos + 1) % key.length;
                        }
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
        Bitmap coverImage = BitmapFactory.decodeFile(input.getPath());
        Bitmap stegoImage = coverImage.copy(Bitmap.Config.ARGB_8888, true);
        stegoImage.setPremultiplied(false);

        String sTextInBin = HelperMethods.stringToBinaryStream(secretText);

        int secretMessageLen = sTextInBin.length();
        int action, embMesPos = 0, keyPos = 0;

        int width = coverImage.getWidth();
        int height = coverImage.getHeight();

        //If secret message is too long (3 bits in each pixel + skipping of some pixels)
        if (secretMessageLen > width * height * 2) {
            return null;
        }

        //Generate and place random 24 bit array of 0-1 in (0,0) pixel
        int key[] = generateKey();
        int temp_number;

        int red_sum = 0;
        for (int j = 0; j <= 7; ++j) {
            if (key[j] == 1) {
                temp_number = (int) Math.pow(2, 7 - j);
            } else {
                temp_number = 0;
            }
            red_sum += temp_number;
        }

        int green_sum = 0;
        for (int j = 8; j <= 15; ++j) {
            if (key[j] == 1) {
                temp_number = (int) Math.pow(2, 15 - j);
            } else {
                temp_number = 0;
            }
            green_sum += temp_number;
        }

        int blue_sum = 0;
        for (int j = 16; j <= 23; ++j) {
            if (key[j] == 1) {
                temp_number = (int) Math.pow(2, 23 - j);
            } else {
                temp_number = 0;
            }
            blue_sum += temp_number;
        }

        //Update (0,1) pixel with RGB_888 as for key values
        stegoImage.setPixel(0, 0, Color.rgb(red_sum, green_sum, blue_sum));
        Log.d("EMB", "Key1: " + red_sum + " " + green_sum + " " + blue_sum);

        //To check if secret message is text. (0,0,COLOR_RGB_TEXT)
        stegoImage.setPixel(0, 1, Constants.COLOR_RGB_TEXT);

        int endX = 0, endY = 2;

        outerloop:
        for (int x = 0; x < width; x++) {
            for (int y = 2; y < height; y++) {
                int pixel = coverImage.getPixel(x, y);

                if (embMesPos < secretMessageLen) {
                    int colors[] = {Color.red(pixel), Color.green(pixel), Color.blue(pixel)};

                    for (int c = 0; c < 3; c++) {
                        if (embMesPos == secretMessageLen) {
                            break;
                        }

                        //Action for LSB
                        if ((key[keyPos] ^ LSB2(colors[c])) == 1) {
                            action = action(colors[c], sTextInBin.charAt(embMesPos));
                            colors[c] += action;
                            embMesPos++;
                            keyPos = (keyPos + 1) % key.length;
                        }
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

    private static int LSB2(int number) {
        return (number >> 1) & 1;
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

    private static int[] generateKey() {
        final int[] bits = {0, 1};
        int[] result = new int[24];

        int n, i;
        Random random = new Random();

        for (i = 0; i < result.length; ++i) {
            n = random.nextInt(2);
            result[i] = bits[n];
        }
        return result;
    }
}
