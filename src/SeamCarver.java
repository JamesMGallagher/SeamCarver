
import java.awt.Color;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author James
 */
public class SeamCarver {

    Picture pictureOut;
    int width;
    int height;
    int[][] pixelMatrix;
    int[][] energyMatrix;
    double[] distTo;
    int[] edgeTo;

    public SeamCarver(Picture picture) {

        picture.setOriginUpperLeft();
        width = picture.width();
        height = picture.height();
        edgeTo = new int[width * height];
        Color color;
        pixelMatrix = new int[width][height];
        energyMatrix = new int[width][height];

        distTo = new double[width * height];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                color = picture.get(i, j);
                int rgb = color.getRGB();
                pixelMatrix[i][j] = color.getRGB();
            }
        }
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                energyMatrix[col][row] = energy(col, row);
            }

        }
        for (int i = 0; i < height * width; i++) {
            distTo[i] = Double.POSITIVE_INFINITY;
        }

        // for (int k = 0; k < width; k++) {
        //    edgeTo[getNum(k, 0)] = 0;
        //}
    }

    public Picture picture() {

        pictureOut = new Picture(width, height);
        Color color;

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                color = new Color(pixelMatrix[i][j]);
                pictureOut.set(i, j, color);

            }

        }

        return pictureOut;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    private int getNum(int col, int row) {
        return row * width + col;
    }

    private int rowFromNum(int i) {
        return (i) / width;
    }

    private int colFromNum(int i) {
        return (i) % width;
    }

    private int deltaSquared(int minus, int plus) {

        int rplus = (plus) & 0xFF;
        int gplus = (plus >> 8) & 0xFF;
        int bplus = (plus >> 16) & 0xFF;

        int rminus = (minus) & 0xFF;
        int gminus = (minus >> 8) & 0xFF;
        int bminus = (minus >> 16) & 0xFF;

        return (rplus - rminus) * (rplus - rminus)
                + (gplus - gminus) * (gplus - gminus)
                + (bplus - bminus) * (bplus - bminus);

    }

    public int energy(int col, int row) {

        if (row < 0 || row >= height || col < 0 || col >= width) {
            throw new java.lang.IndexOutOfBoundsException();
        }
        if (row == 0 || row == height - 1 || col == 0 || col == width - 1) {
            return 195075;
        }
        //set top to  energy 1
        if (row == 0 && col != 0 && col != width - 1) {
            return 1;//TODO never reached

        }
        return deltaSquared(pixelMatrix[col - 1][row], pixelMatrix[col + 1][row])
                + deltaSquared(pixelMatrix[col][row - 1], pixelMatrix[col][row + 1]);

    }

    private void relaxOld(int col, int row) {
        if (row == height - 1) {
            return;
        }
        for (int k = col - 1; k <= col + 1; k++) {
            if (k < 0 || k >= width) {
                continue;
            }
            if (energyMatrix[k][row + 1] + distTo[getNum(col, row)] < distTo[getNum(k, row + 1)]) {
                distTo[getNum(k, row + 1)] = energyMatrix[k][row + 1] + distTo[getNum(col, row)];
                edgeTo[getNum(k, row + 1)] = getNum(col, row);
            }
        }
    }

    private void relax(int col, int row, boolean horizontal) {

        if (horizontal == false) {
            for (int k = col - 1; k <= col + 1; k++) {
                if (k < 0 || k >= width) {
                    continue;
                }
                if (energyMatrix[k][row + 1] + distTo[getNum(col, row)] < distTo[getNum(k, row + 1)]) {
                    distTo[getNum(k, row + 1)] = energyMatrix[k][row + 1] + distTo[getNum(col, row)];
                    edgeTo[getNum(k, row + 1)] = getNum(col, row);
                }
            }
        } else {
            for (int k = row - 1; k <= row + 1; k++) {
                if (k < 0 || k >= height) {
                    continue;
                }
                if (energyMatrix[col + 1][k] + distTo[getNum(col, row)] < distTo[getNum(col + 1, k)]) {
                    distTo[getNum(col + 1, k)] = energyMatrix[col + 1][k] + distTo[getNum(col, row)];
                    edgeTo[getNum(col + 1, k)] = getNum(col, row);
                }
            }
        }
    }

    public int[] findVerticalSeam() {
        int[] seam = new int[height];

        for (int i = 0; i < height * width; i++) {
            distTo[i] = Double.POSITIVE_INFINITY;
        }
        for (int i = 0; i < width; i++) {
            distTo[getNum(i, 0)] = 0;
        }

        for (int row = 0; row < height - 1; row++) {
            for (int col = 0; col < width; col++) {
                relax(col, row, false);
            }
        }
        double minPathLength = Double.POSITIVE_INFINITY;
        int kValue = 0;

        for (int col = 0; col < width; col++) {
            if (distTo[getNum(col, height - 1)] < minPathLength) {
                minPathLength = distTo[getNum(col, height - 1)];
                kValue = col;
            }
        }

        int seamPixel = getNum(kValue, height - 1);
        int seamIndex = height - 1;
        //int row;

        while (seamIndex > 0) {
            seam[seamIndex] = colFromNum(seamPixel);
            seamPixel = edgeTo[seamPixel];
            seamIndex--;

        }

        return seam;
    }

    public int[] findHorizontalSeam() {
        int[] seam = new int[width];

        for (int i = 0; i < height * width; i++) {
            distTo[i] = Double.POSITIVE_INFINITY;
        }
        for (int i = 0; i < height; i++) {
            distTo[getNum(0, i)] = 0;
        }

        for (int col = 0;
                col < width - 1; col++) {
            for (int row = 0; row < height; row++) {
                relax(col, row, true);
            }
        }
        double minPathLength = Double.POSITIVE_INFINITY;
        int kValue = 0;

        for (int row = 0;
                row < height;
                row++) {
            if (distTo[getNum(width - 1, row)] < minPathLength) {
                minPathLength = distTo[getNum(width - 1, row)];
                kValue = row;
            }
        }

        int seamPixel = getNum(width - 1, kValue);
        int seamIndex = width - 1;
        int row;

        while (seamIndex
                > 0) {
            seam[seamIndex] = rowFromNum(seamPixel);
            seamPixel = edgeTo[seamPixel];
            seamIndex--;

        }

        return seam;
    }

    public void removeVerticalSeam(int[] seam) {
        int[][] newPixelMatrix = new int[width - 1][height];
        int[][] newEnergyMatrix = new int[width - 1][height];

        for (int row = 0; row < height - 1; row++) {
            for (int col = 0, ncol = 0; col < width; col++) {
                if (col != seam[row]) {
                    newPixelMatrix[ncol][row] = pixelMatrix[col][row];
                    newEnergyMatrix[ncol][row] = energyMatrix[col][row];
                    ncol++;
                }
            }
        }

        for (int row = 0; row < height; row++) {
            for (int col = seam[row] - 1; col <= seam[row] + 1; col++) {
                if (col >= 0 && col < width - 1) {
                    newEnergyMatrix[col][row] = energy(col, row);
                }
            }
        }

        pixelMatrix = newPixelMatrix;
        energyMatrix = newEnergyMatrix;
        width--;
    }

    public void removeHorizontalSeam(int[] seam) {

        int[][] newPixelMatrix = new int[width][height - 1];
        int[][] newEnergyMatrix = new int[width][height - 1];

        for (int col = 0; col < width; col++) {
            for (int row = 0, nrow = 0; row < height; row++) {
                if (row != seam[col]) {
                    newPixelMatrix[col][nrow] = pixelMatrix[col][row];
                    newEnergyMatrix[col][nrow] = energyMatrix[col][row];
                    nrow++;
                }
            }
        }

        for (int col = 0; col < width; col++) {
            for (int row = seam[col] - 1; row <= seam[col] + 1; row++) {
                if (row >= 0 && row < height) {
                    newEnergyMatrix[col][row] = energy(col, row);
                }
            }
        }

        pixelMatrix = newPixelMatrix;
        height--;

    }

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage:\njava ResizeDemo [image filename] [num cols to remove] [num rows to remove]");
            return;
        }

        Picture inputImg = new Picture(args[0]);
        int removeColumns = Integer.parseInt(args[1]);
        int removeRows = Integer.parseInt(args[2]);

        System.out.printf("image is %d columns by %d rows\n", inputImg.width(), inputImg.height());
        SeamCarver sc = new SeamCarver(inputImg);

        Stopwatch sw = new Stopwatch();

        for (int i = 0; i < removeColumns; i++) {
            int[] horizontalSeam = sc.findVerticalSeam();

            for (int h : horizontalSeam) {
                System.out.println(h);
            }
        }
    }

}
