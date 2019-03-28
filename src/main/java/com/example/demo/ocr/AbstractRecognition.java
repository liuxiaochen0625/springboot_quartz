/**
 * Weidai
 * Copyright (C), 2011 - 2019, 微贷网.
 */
package com.example.demo.ocr;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

/**
 * @version $Id: AutoRecognition.java, v 0.1 2019-03-28 reus Exp $
 * @ClassName: AutoRecognition
 * @Description: 图片识别
 * @author: reus
 */
public abstract class AbstractRecognition {

    /**
     * 图片灰度化和二值化
     * @param bufferedImage
     * @throws IOException
     */
    protected BufferedImage cleanLinesInImage(BufferedImage bufferedImage) throws IOException {
        int[][] gray = grayImage(bufferedImage);
        int threshold = binaryThreshold(gray, bufferedImage.getWidth(), bufferedImage.getHeight());
        return binaryImage(bufferedImage, gray, threshold);
    }

    /**
     * 灰度化图片
     * @param bufferedImage
     * @return
     */
    protected abstract int[][] grayImage(BufferedImage bufferedImage);

    /**
     * 二值化图片
     * @param bufferedImage
     * @param gray
     * @return
     */
    protected abstract BufferedImage binaryImage(BufferedImage bufferedImage, int[][] gray,
                                                 int threshold);

    /**
     * 二值化分界点
     * @param gray
     * @param w
     * @param h
     * @return
     */
    protected abstract int binaryThreshold(int[][] gray, int w, int h);

    /**
     * 分割图片
     * @param image
     * @return
     */
    protected abstract List<BufferedImage> splitImage(BufferedImage image) throws Exception;

    /**
     * 获取识别后的字符串
     * @param image
     * @return
     */
    protected abstract String getCode(BufferedImage image) throws Exception;

}