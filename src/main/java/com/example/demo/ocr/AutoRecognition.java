/**
 * Weidai
 * Copyright (C), 2011 - 2019, 微贷网.
 */
package com.example.demo.ocr;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;

import com.example.demo.util.AutoRecognitionUtil;

import javax.imageio.ImageIO;

/**
 * @version $Id: Recognition.java, v 0.1 2019-03-28 reus Exp $
 * @ClassName: Recognition
 * @Description:
 * @author: reus
 */
public class AutoRecognition extends AbstractRecognition {
    @Override
    protected int[][] grayImage(BufferedImage bufferedImage) {
        int h = bufferedImage.getHeight();
        int w = bufferedImage.getWidth();

        // 灰度化
        int[][] gray = new int[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int argb = bufferedImage.getRGB(x, y);
                // 图像加亮（调整亮度识别率非常高）
                int g = (int) (((argb >> 8) & 0xFF) * 1.1 + 30);
                int r = (int) (((argb >> 16) & 0xFF) * 1.1 + 30);
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
        return gray;
    }

    @Override
    protected BufferedImage binaryImage(BufferedImage bufferedImage, int[][] gray, int threshold) {
        int w = bufferedImage.getWidth();
        int h = bufferedImage.getHeight();
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
        return binaryBufferedImage;
    }

    @Override
    protected int binaryThreshold(int[][] gray, int w, int h) {
        return AutoRecognitionUtil.ostu(gray, w, h);
    }

    @Override
    protected List<BufferedImage> splitImage(BufferedImage image) throws Exception {
        List<BufferedImage> subImgs = new ArrayList<>();
        int width = image.getWidth();
        int height = image.getHeight();
        List<Integer> weightlist = new ArrayList<>();
        // 获取图片中含有字符部分的宽度
        for (int x = 0; x < width; ++x) {
            int count = 0;
            for (int y = 0; y < height; ++y) {
                if (AutoRecognitionUtil.isBlackTo1(image.getRGB(x, y)) == 1) {
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
                    .removeBlank(image.getSubimage(i - length, 0, length, height)));
            }
        }
        return subImgs;
    }

    @Override
    protected String getCode(BufferedImage image) throws Exception {
        StringBuilder builder = new StringBuilder();
        BufferedImage binaryImage = cleanLinesInImage(image);
        List<BufferedImage> images = splitImage(binaryImage);
        for (BufferedImage imageA : images) {
            // 缩小成32x32的缩略图
            imageA = AutoRecognitionUtil.scale(imageA);
            // 获取灰度像素数组
            int[] pixelsA = AutoRecognitionUtil.getPixels(imageA);
            // 获取平均灰度颜色
            int averageColorA = AutoRecognitionUtil.getAverageOfPixelArray(pixelsA);
            // 获取灰度像素的比较数组（即图像指纹序列）
            pixelsA = AutoRecognitionUtil.getPixelDeviateWeightsArray(pixelsA, averageColorA);
            builder.append(getSingleChar(pixelsA));
        }
        return builder.toString();
    }

    /**
     * 获取最相似的图片值
     * @param pixelsA
     * @return
     */
    private String getSingleChar(int[] pixelsA) {
        Map<Double, String> similar = new HashMap<>();
        double similarity;
        for (String key : getCaptchaMap().keySet()) {
            int[] value = getCaptchaMap().get(key);
            // 获取两个图的汉明距离（假设另一个图也已经按上面步骤得到灰度比较数组）
            int hammingDistance = AutoRecognitionUtil.getHammingDistance(pixelsA, value);
            // 通过汉明距离计算相似度，取值范围 [0.0, 1.0]
            similarity = AutoRecognitionUtil.calSimilarity(hammingDistance);
            similar.put(similarity, key);
        }
        Set<Double> keySet = similar.keySet();
        Object[] key = keySet.toArray();
        Arrays.sort(key);
        double max = (double) key[keySet.size() - 1];
        String name = similar.get(max);
        return name;
    }

    /**
     * 读取图片像素值数组
     * @return
     */
    public final Map<String, int[]> getCaptchaMap() {
        String fileName = this.getClass().getClassLoader()
            .getResource("captchafeature/fujian_captcha.txt").getPath();
        return AutoRecognitionUtil.getMap(fileName);
    }

    public static void main(String[] args) {
        AutoRecognition recognition = new AutoRecognition();
        try {
            System.out.println(recognition.getCode(ImageIO.read(new File("safecode/code_fuzhou.jpg"))));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}