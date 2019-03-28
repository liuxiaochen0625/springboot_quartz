/**
 * Weidai
 * Copyright (C), 2011 - 2019, 微贷网.
 */
package com.example.demo.util;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

import javax.imageio.ImageIO;

/**
 * @version $Id: SafeCodeUtils.java, v 0.1 2019-03-27 reus Exp $
 * @ClassName: SafeCodeUtils
 * @Description:
 * @author: reus
 */
public class SafeCodeUtils {
    private static Map<BufferedImage, String> trainMap     = null;
    // 训练库路径
    private static String                     train_path   = "train/";
    private static String                     safeCodePath = "safecode/";

    public static void main(String[] args) {
        System.out.println(getSafeCode("code_fuzhou.jpg"));
    }

    public static String getSafeCode(String safeCodeName) {
        File testDataDir = new File(safeCodePath + safeCodeName);
        final String destDir = safeCodePath;
        // 去噪
        try {
            cleanLinesInImage(testDataDir, destDir);
            String allChar = getAllChar(safeCodePath + safeCodeName);
            if (allChar != null && allChar.length() == 4) {
                System.out.println("验证码读取完成！");
                return allChar;
            } else {
                System.out.println("验证码读取失败！");
                return null;
            }
        } catch (Exception e) {
            System.out.println("图片读取异常！");
            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     * @param sfile
     *            需要去噪的图像
     * @param destDir
     *            去噪后的图像保存地址
     * @throws IOException
     */
    public static void cleanLinesInImage(File sfile, String destDir) throws IOException {
        File destF = new File(destDir);
        if (!destF.exists()) {
            destF.mkdirs();
        }

        BufferedImage bufferedImage = ImageIO.read(sfile);
        int h = bufferedImage.getHeight();
        int w = bufferedImage.getWidth();

        // 灰度化
        int[][] gray = new int[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int argb = bufferedImage.getRGB(x, y);
                // 图像加亮（调整亮度识别率非常高）
                int r = (int) (((argb >> 16) & 0xFF) * 1.1 + 30);
                int g = (int) (((argb >> 8) & 0xFF) * 1.1 + 30);
                int b = (int) (((argb >> 0) & 0xFF) * 1.1 + 30);
                if (r >= 255) {
                    r = 255;
                }
                if (g >= 255) {
                    g = 255;
                }
                if (b >= 255) {
                    b = 255;
                }
                gray[x][y] = (int) Math.pow((Math.pow(r, 2.2) * 0.2973 + Math.pow(g, 2.2) * 0.6274
                                             + Math.pow(b, 2.2) * 0.0753),
                    1 / 2.2);
            }
        }

        // 二值化
        int threshold = AutoRecognitionUtil.ostu(gray, w, h);
        BufferedImage binaryBufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                if (gray[x][y] > threshold) {
                    gray[x][y] |= 0x00FFFF;
                } else {
                    gray[x][y] &= 0xFF0000;
                }
                binaryBufferedImage.setRGB(x, y, gray[x][y]);
            }
        }

        //矩阵打印
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (AutoRecognitionUtil.isBlack(binaryBufferedImage.getRGB(x, y))) {
                    System.out.print("*");
                } else {
                    System.out.print(" ");
                }
            }
            System.out.println();
        }

        ImageIO.write(binaryBufferedImage, "jpg", new File(destDir, sfile.getName()));
    }

    /**
     * 去除干扰线条
     * @param h
     * @param w
     * @param binaryBufferedImage
     */
    public static void removeLine(int h, int w, BufferedImage binaryBufferedImage) {
        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
                boolean flag = false;
                if (AutoRecognitionUtil.isBlack(binaryBufferedImage.getRGB(x, y))) {
                    // 左右均为空时，去掉此点
                    if (isWhite(binaryBufferedImage.getRGB(x - 1, y))
                        && isWhite(binaryBufferedImage.getRGB(x + 1, y))) {
                        flag = true;
                    }
                    // 上下均为空时，去掉此点
                    if (isWhite(binaryBufferedImage.getRGB(x, y + 1))
                        && isWhite(binaryBufferedImage.getRGB(x, y - 1))) {
                        flag = true;
                    }
                    // 斜上下为空时，去掉此点
                    if (isWhite(binaryBufferedImage.getRGB(x - 1, y + 1))
                        && isWhite(binaryBufferedImage.getRGB(x + 1, y - 1))) {
                        flag = true;
                    }
                    if (isWhite(binaryBufferedImage.getRGB(x + 1, y + 1))
                        && isWhite(binaryBufferedImage.getRGB(x - 1, y - 1))) {
                        flag = true;
                    }
                    if (flag) {
                        binaryBufferedImage.setRGB(x, y, -1);
                    }
                }
            }
        }
    }

    /**
     * 判断是否为白
     * @param colorInt
     * @return
     */
    public static boolean isWhite(int colorInt) {
        Color color = new Color(colorInt);
        if (color.getRed() + color.getGreen() + color.getBlue() > 300) {
            return true;
        }
        return false;
    }

    /**
     * 获取相似度最高的字符
     * @param imageAname
     * @param pixelsA
     * @throws Exception
     */
    public static void getSingleChar(String imageAname, int[] pixelsA) throws Exception {
        Map<BufferedImage, String> map = loadTrainData();
        Map<Double, String> similar = new HashMap<>();
        double similarity = 0;
        for (BufferedImage bi : map.keySet()) {
            // File imageFileB = new File("D:/train2/j-14.jpg");
            // Image imageB = ImageIO.read(imageFileB);
            BufferedImage imageB = AutoRecognitionUtil.scale(bi);
            int[] pixelsB = AutoRecognitionUtil.getPixels(imageB);
            int averageColorB = AutoRecognitionUtil.getAverageOfPixelArray(pixelsB);
            pixelsB = AutoRecognitionUtil.getPixelDeviateWeightsArray(pixelsB, averageColorB);
            // 获取两个图的汉明距离（假设另一个图也已经按上面步骤得到灰度比较数组）
            int hammingDistance = AutoRecognitionUtil.getHammingDistance(pixelsA, pixelsB);
            // 通过汉明距离计算相似度，取值范围 [0.0, 1.0]
            similarity = AutoRecognitionUtil.calSimilarity(hammingDistance);
            similar.put(similarity, map.get(bi));
            if (similarity > 0.5) {
                System.out.println(imageAname + "与" + map.get(bi) + "的相似度：" + similarity);
            }
        }
        if (similarity > 0 && similar != null) {
            Set<Double> keySet = similar.keySet();
            Object[] key = keySet.toArray();
            Arrays.sort(key);
            double max = (double) key[keySet.size() - 1];
            String name = similar.get(max);
            System.out.println(imageAname + "与" + name + "的相似度最大，最大相似度是：" + max);
        }
    }

    /**
     * 转换至灰度图
     * @param image
     * @return
     */
    public static BufferedImage toGrayscale(Image image) {
        BufferedImage sourceBuffered = AutoRecognitionUtil.convertToBufferedFrom(image);
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        ColorConvertOp op = new ColorConvertOp(cs, null);
        BufferedImage grayBuffered = op.filter(sourceBuffered, null);
        return grayBuffered;
    }

    /**
     * 加载训练库
     * @return
     * @throws Exception
     */
    public static Map<BufferedImage, String> loadTrainData() throws Exception {
        if (trainMap == null) {
            Map<BufferedImage, String> map = new HashMap<>();
            File dir = new File(train_path);
            File[] files = dir.listFiles();
            for (File file : files) {
                map.put(ImageIO.read(file), file.getName().charAt(0) + "");
            }
            System.out.println("加载训练库完成");
            trainMap = map;
        }
        return trainMap;
    }

    /**
     * 获取所有的字符
     * @param file
     * @return
     * @throws Exception
     */
    public static String getAllChar(String file) throws Exception {
        // 获取图像
        File imageFileA = new File(file);
        BufferedImage image = ImageIO.read(imageFileA);
        List<BufferedImage> listImg = splitImage(image);// 切割图片
        if (listImg.size() != 4) {
            return null;
        }
        String result = "";
        for (BufferedImage imageA : listImg) {
            // 缩小成32x32的缩略图
            imageA = AutoRecognitionUtil.scale(imageA);
            // 获取灰度像素数组
            int[] pixelsA = AutoRecognitionUtil.getPixels(imageA);
            // 获取平均灰度颜色
            int averageColorA = AutoRecognitionUtil.getAverageOfPixelArray(pixelsA);
            // 获取灰度像素的比较数组（即图像指纹序列）
            pixelsA = AutoRecognitionUtil.getPixelDeviateWeightsArray(pixelsA, averageColorA);
            trainMap = loadTrainData();
            result += getSingleChar(pixelsA, trainMap);
        }
        return result;
    }

    /**
     * 切割图片
     * 测试每个 x 轴点上的y直线是否经过黑色
     * @param img
     * @return
     * @throws Exception
     */
    public static List<BufferedImage> splitImage(BufferedImage img) throws Exception {
        List<BufferedImage> subImgs = new ArrayList<>();
        int width = img.getWidth();
        int height = img.getHeight();
        List<Integer> weightlist = new ArrayList<>();
        // 获取图片中含有字符部分的宽度
        for (int x = 0; x < width; ++x) {
            int count = 0;
            for (int y = 0; y < height; ++y) {
                if (AutoRecognitionUtil.isBlackTo1(img.getRGB(x, y)) == 1) {
                    count++;
                }
            }
            weightlist.add(count);
        }
        // 主流程 副流程 使用相同的判断标准 两种流程判断同时候向前走
        // 副流程记录所需要的位置
        for (int i = 0; i < weightlist.size(); i++) {
            int length = 0;
            while (i < weightlist.size() && weightlist.get(i) > 0) {
                i++;
                length++;
            }
            if (length > 2) {
                subImgs.add(AutoRecognitionUtil
                    .removeBlank(img.getSubimage(i - length, 0, length, height)));
            }
        }
        return subImgs;
    }

    // 获取单个字符
    public static String getSingleChar(int[] pixelsA,
                                       Map<BufferedImage, String> map) throws Exception {
        Map<Double, String> similar = new HashMap<>();
        double similarity;
        for (BufferedImage bi : map.keySet()) {
            // File imageFileB = new File("D:/train2/j-14.jpg");
            // Image imageB = ImageIO.read(imageFileB);
            BufferedImage imageB = AutoRecognitionUtil.scale(bi);
            int[] pixelsB = AutoRecognitionUtil.getPixels(imageB);
            int averageColorB = AutoRecognitionUtil.getAverageOfPixelArray(pixelsB);
            pixelsB = AutoRecognitionUtil.getPixelDeviateWeightsArray(pixelsB, averageColorB);
            // 获取两个图的汉明距离（假设另一个图也已经按上面步骤得到灰度比较数组）
            int hammingDistance = AutoRecognitionUtil.getHammingDistance(pixelsA, pixelsB);
            // 通过汉明距离计算相似度，取值范围 [0.0, 1.0]
            similarity = AutoRecognitionUtil.calSimilarity(hammingDistance);
            similar.put(similarity, map.get(bi));
            if (similarity > 0.5) {
                // System.out.println(imageAname + "与" + map.get(bi) + "的相似度：" + similarity);
            }
        }
        // if (similarity > 0 && similar != null) {
        Set<Double> keySet = similar.keySet();
        Object[] key = keySet.toArray();
        Arrays.sort(key);
        double max = (double) key[keySet.size() - 1];
        String name = similar.get(max);
        // System.out.println(imageAname + "与" + name + "的相似度最大，最大相似度是：" + max);
        // }
        return name;
    }
}