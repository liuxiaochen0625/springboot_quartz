/**
 * Weidai
 * Copyright (C), 2011 - 2019, 微贷网.
 */
package com.example.demo.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;

/**
 * @version $Id: SafeCodeUtilsForSZ.java, v 0.1 2019-03-27 reus Exp $
 * @ClassName: SafeCodeUtilsForSZ
 * @Description:
 * @author: reus
 */
public class SafeCodeUtilsForSZ {
    private static Map<BufferedImage, String> trainMap     = null;
    // 训练库路径
    private static String                     train_path   = "shenzhen";
    private static String                     safeCodePath = "safecode/";

    public static String getSafeCode(String safeCodeName) {
        File testDataDir = new File(safeCodePath + safeCodeName);
        final String destDir = safeCodePath;
        // 去噪
        try {
            // 验证码原图 二值化
            binaryzationPic(safeCodePath + safeCodeName, safeCodePath + safeCodeName);
            // 进行图片验证
            String allChar = getAllChar(safeCodePath + safeCodeName);
            if (allChar != null && allChar.length() == 4) {
                System.out.println("验证码 读取完成！");
                return allChar;
            } else {
                System.out.println("验证码 读取失败！");
                return null;
            }
        } catch (Exception e) {
            System.out.println("图片读取异常！");
            e.printStackTrace();
        }
        return null;
    }

    // 获取所有的字符
    public static String getAllChar(String file) throws Exception {
        // 获取图像
        File imageFileA = new File(file);
        BufferedImage image = ImageIO.read(imageFileA);
        java.util.List<BufferedImage> listImg = SafeCodeUtils.splitImage(image);// 切割图片
        if (listImg.size() != 4) {
            return null;
        }
        // 缩小成32x32的缩略图
        String result = "";
        for (BufferedImage imageA : listImg) {
            imageA = AutoRecognitionUtil.scale(imageA);
            // 获取灰度像素数组
            int[] pixelsA = AutoRecognitionUtil.getPixels(imageA);
            // 获取平均灰度颜色
            int averageColorA = AutoRecognitionUtil.getAverageOfPixelArray(pixelsA);
            // 获取灰度像素的比较数组（即图像指纹序列）
            pixelsA = AutoRecognitionUtil.getPixelDeviateWeightsArray(pixelsA, averageColorA);
            trainMap = SafeCodeUtils.loadTrainData();
            result += SafeCodeUtils.getSingleChar(pixelsA, trainMap);
        }
        // ImageIO.write(img, "JPG", new File("result6\\" + result + ".jpg"));
        return result;
    }


    /**
     * 图片二值化
     * 首先拿到原有图片的色彩矩阵数据
     * 然后根据原有图片大小创建新的图片
     * 通过每个点灰度与阙值进行对比来判断颜色设置
     * @param startFilePath
     * @param endFilePath
     */
    public static void binaryzationPic(String startFilePath, String endFilePath) {
        // 图片二值化
        BufferedImage bi = null;//通过imageio将图像载入
        try {
            bi = ImageIO.read(new File(startFilePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        int h = bi.getHeight();//获取图像的高
        int w = bi.getWidth();//获取图像的宽
        //int rgb = bi.getRGB(0, 0);//获取指定坐标的ARGB的像素值
        int[][] gray = new int[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                // 获取每个点的ARGB像素值  rgb三点度数的三分之一
                gray[x][y] = getGray(bi.getRGB(x, y));
            }
        }

        // 重新创建一个图像
        BufferedImage nbi = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);
        int SW = 160; // 灰度阙值  可以设定
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                // 便利 设置颜色
                if (getAverageColor(gray, x, y, w, h) > SW) {
                    //int max = new Color(255,255,255).getRGB();
                    nbi.setRGB(x, y, new Color(255, 255, 255).getRGB());
                } else {
                    //int min = new Color(0,0,0).getRGB();
                    nbi.setRGB(x, y, new Color(0, 0, 0).getRGB());
                }
            }
        }

        try {
            ImageIO.write(nbi, "jpg", new File(endFilePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取gray
     * @param rgb
     * @return
     */
    public static int getGray(int rgb) {
        Color c = new Color(rgb);
        int r = c.getRed();
        int g = c.getGreen();
        int b = c.getBlue();
        int top = (r + g + b) / 3;
        return top;
    }

    /**
     * 自己加周围8个灰度值再除以9，算出其相对灰度值
     * @param gray
     * @param x
     * @param y
     * @param w
     * @param h
     * @return
     */
    public static int getAverageColor(int[][] gray, int x, int y, int w, int h) {
        // 边缘位置取255
        int rs = gray[x][y] + (x == 0 ? 255 : gray[x - 1][y])
                 + (x == 0 || y == 0 ? 255 : gray[x - 1][y - 1])
                 + (x == 0 || y == h - 1 ? 255 : gray[x - 1][y + 1])
                 + (y == 0 ? 255 : gray[x][y - 1]) + (y == h - 1 ? 255 : gray[x][y + 1])
                 + (x == w - 1 ? 255 : gray[x + 1][y])
                 + (x == w - 1 || y == 0 ? 255 : gray[x + 1][y - 1])
                 + (x == w - 1 || y == h - 1 ? 255 : gray[x + 1][y + 1]);
        return rs / 9;
    }
}