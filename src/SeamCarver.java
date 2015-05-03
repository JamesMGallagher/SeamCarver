
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
    int[] distTo;
    int[] edgeTo;

    public SeamCarver(Picture picture) {

        picture.setOriginUpperLeft();
        width = picture.width();
        height = picture.height();
        edgeTo = new int[width * height + 1];
        Color color;
        pixelMatrix = new int[width][height];

        distTo = new int[width * height + 1];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                color = picture.get(i, j);
                int rgb = color.getRGB();
                pixelMatrix[i][j] = color.getRGB();
                //pixels[i][j] = picture.get(i, j).getRGB();

            }
        }

        for (int k = 0; k < width; k++) {
            edgeTo[getNum(0, k)] = 0;
        }
    }

    public Picture picture() {

        pictureOut = new Picture(width, height);
        Color color;

        for (int i = 0; i <= width; i++) {
            for (int j = 0; j <= height; j++) {
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

    private int getNum(int row, int col) {
        return row * width + col;
    }

    private int rowFromNum(int i) {
        return (i) / width;
    }

    private int colFromNum(int i) {
        return (i) % width;
    }

    private double deltaSquared(int minus, int plus) {

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

    public double energy(int row, int col) {

        if (row < 0 || row >= height || col < 0 || col >= width) {
            throw new java.lang.IndexOutOfBoundsException();
        }
        if (row == 0 || row == height - 1 || col == 0 || col == width - 1) {
            return 195075;
        }
        //set top to  energy 1
        if (row == 0 && col != 0 && col != width - 1) {
            return 1;

        }
        return deltaSquared(pixelMatrix[row - 1][col], pixelMatrix[row + 1][col])
                + deltaSquared(pixelMatrix[row][col - 1], pixelMatrix[row][col + 1]);

    }

    private void relax(int i, int j) {
        for (int k = j - 1; k <= j + 1; k++) {
            if (energyMatrix[i + 1][k] + distTo[getNum(i, j)] < distTo[getNum(i + 1, k)]) {
                distTo[getNum(i + 1, k)] = energyMatrix[i + 1][k] + distTo[getNum(i, j)];
                edgeTo[getNum(i + 1, k)] = getNum(i, j);
            }
        }
    }

    public int[] findVerticalSeam() {
        int[] seam = new int[height];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                relax(i, j);
            }
        }
        double minPathLength = Double.POSITIVE_INFINITY;
        int kValue = 0;

        for (int k = 0; k < width; k++) {
            if (edgeTo[getNum(height - 1, k)] < minPathLength) {
                minPathLength = edgeTo[getNum(height - 1, k)];
                kValue = k;
            }
        }

        int seamPixel = getNum(height - 1, kValue);
        int seamIndex = height - 1;
        int row;

        while (seamPixel > 0) {
            seam[seamIndex] = colFromNum(seamPixel);
            seamPixel = edgeTo[seamPixel];
            seamIndex--;

        }

        return seam;
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
