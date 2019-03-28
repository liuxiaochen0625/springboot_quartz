/**
 *
 * Copyright (C), 2011 - 2019, .
 */
package com.example.demo.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @version $Id: AutoRecognitionUtil.java, v 0.1 2019-03-28 reus Exp $
 * @ClassName: AutoRecognitionUtil
 * @Description: 图片自动识别工具类
 * @author: reus
 */
public class AutoRecognitionUtil {

    /**
     * 获取两个缩略图的平均像素比较数组的汉明距离（距离越大差异越大）
     * @param a
     * @param b
     * @return
     */
    public static int getHammingDistance(int[] a, int[] b) {
        int sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += a[i] == b[i] ? 0 : 1;
        }
        return sum;
    }

    /**
     * 通过汉明距离计算相似度
     * @param hammingDistance
     * @return
     */
    public static double calSimilarity(int hammingDistance) {
        int length = 32 * 32;
        double similarity = (length - hammingDistance) / (double) length;

        // 使用指数曲线调整相似度结果
        similarity = java.lang.Math.pow(similarity, 2);
        return similarity;
    }

    /**
     * 计算二值化阈值，区分背景与实体之间的临界点
     * @param gray
     * @param w
     * @param h
     * @return
     */
    public static int ostu(int[][] gray, int w, int h) {
        int[] histData = new int[w * h];
        // Calculate histogram
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int red = 0xFF & gray[x][y];
                histData[red]++;
            }
        }

        // Total number of pixels
        int total = w * h;

        float sum = 0;
        for (int t = 0; t < 256; t++)
            sum += t * histData[t];

        float sumB = 0;
        int wB = 0;
        int wF;

        float varMax = 0;
        int threshold = 0;

        for (int t = 0; t < 256; t++) {
            wB += histData[t]; // Weight Background
            if (wB == 0)
                continue;

            wF = total - wB; // Weight Foreground
            if (wF == 0)
                break;

            sumB += (float) (t * histData[t]);

            float mB = sumB / wB; // Mean Background
            float mF = (sum - sumB) / wF; // Mean Foreground

            // Calculate Between Class Variance
            float varBetween = (float) wB * (float) wF * (mB - mF) * (mB - mF);

            // Check if new maximum found
            if (varBetween > varMax) {
                varMax = varBetween;
                threshold = t;
            }
        }

        return threshold;
    }

    // 判断颜色为黑
    public static int isBlackTo1(int colorInt) {
        Color color = new Color(colorInt);
        if (color.getRed() + color.getGreen() + color.getBlue() <= 100) {
            return 1;
        }
        return 0;
    }

    /**
     * 去除无用的空白
     * @param img
     * @return
     * @throws Exception
     */
    public static BufferedImage removeBlank(BufferedImage img) throws Exception {
        int width = img.getWidth();
        int height = img.getHeight();
        int start = 0;
        int end = 0;
        Label1: for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                if (AutoRecognitionUtil.isBlackTo1(img.getRGB(x, y)) == 1) {
                    start = y;
                    break Label1;
                }
            }
        }
        Label2: for (int y = height - 1; y >= 0; --y) {
            for (int x = 0; x < width; ++x) {
                if (AutoRecognitionUtil.isBlackTo1(img.getRGB(x, y)) == 1) {
                    end = y;
                    break Label2;
                }
            }
        }
        return img.getSubimage(0, start, width, end - start + 1);
    }

    /**
     * 缩放至32x32像素缩略图
     * @param image
     * @return
     */
    public static BufferedImage scale(BufferedImage image) {
        Image img = image;
        img = img.getScaledInstance(32, 32, Image.SCALE_SMOOTH);
        BufferedImage image2 = convertToBufferedFrom(img);
        return image2;
    }

    /**
     * 将任意Image类型图像转换为BufferedImage类型，方便后续操作
     * @param srcImage
     * @return
     */
    public static BufferedImage convertToBufferedFrom(Image srcImage) {
        BufferedImage bufferedImage = new BufferedImage(srcImage.getWidth(null),
            srcImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bufferedImage.createGraphics();
        g.drawImage(srcImage, null, null);
        g.dispose();
        return bufferedImage;
    }

    /**
     * 获取像素数组
     * @param image
     * @return
     */
    public static int[] getPixels(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);
        return pixels;
    }

    /**
     * 获取灰度图的平均像素颜色值
     * @param pixels
     * @return
     */
    public static int getAverageOfPixelArray(int[] pixels) {
        Color color;
        long sumRed = 0;
        for (int i = 0; i < pixels.length; i++) {
            color = new Color(pixels[i], true);
            sumRed += color.getRed();
        }
        int averageRed = (int) (sumRed / pixels.length);
        return averageRed;
    }

    /**
     * 获取灰度图的像素比较数组（平均值的离差）
     * @param pixels
     * @param averageColor
     * @return
     */
    public static int[] getPixelDeviateWeightsArray(int[] pixels, final int averageColor) {
        Color color;
        int[] dest = new int[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            color = new Color(pixels[i], true);
            dest[i] = color.getRed() - averageColor > 0 ? 1 : 0;
        }
        return dest;
    }

    /**
     * 读取图片像素数组
     * @param fileName
     * @return
     */
    public static Map<String, int[]> getMap(String fileName) {
        Map<String, int[]> map = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName));) {
            String line;
            while ((line = reader.readLine()) != null) {
                int pos = line.indexOf(":");
                String key = line.substring(0, pos);
                String[] value = line.substring(pos + 1).split(",");
                int[] values = new int[value.length];
                for (int i = 0; i < value.length; i++)
                    values[i] = Integer.parseInt(value[i]);
                map.put(key, values);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 判断是否为黑
     * @param colorInt
     * @return
     */
    public static boolean isBlack(int colorInt) {
        Color color = new Color(colorInt);
        if (color.getRed() + color.getGreen() + color.getBlue() <= 300) {
            return true;
        }
        return false;
    }

    /**
     * 获取训练结果图片的像素数组
     * @param path
     * @throws IOException
     */
    public static void getTrainMap(String path) throws IOException {
        File dir = new File(path);
        File[] files = dir.listFiles();
        for (File file : files) {
            BufferedImage image = ImageIO.read(file);
            BufferedImage imageB = AutoRecognitionUtil.scale(image);
            int[] pixelsB = AutoRecognitionUtil.getPixels(imageB);
            int averageColorB = AutoRecognitionUtil.getAverageOfPixelArray(pixelsB);
            pixelsB = AutoRecognitionUtil.getPixelDeviateWeightsArray(pixelsB, averageColorB);
            System.out.print(pixelsB[0]);
            for (int i = 1; i < pixelsB.length; i++) {
                System.out.print("," + pixelsB[i]);
            }
            System.out.println();
        }
    }
}