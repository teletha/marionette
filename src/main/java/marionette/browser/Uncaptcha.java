/*
 * Copyright (C) 2020 marionette Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package marionette.browser;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import kiss.I;

/**
 * @version 2017/07/29 7:00:36
 */
public class Uncaptcha {

    private final Color White = new Color(-1, -1, 0x00FFFFFF);

    /** The current image. */
    private BufferedImage image;

    /**
     * @param file
     * @throws IOException
     */
    public Uncaptcha(Path file) throws IOException {
        this(ImageIO.read(file.toFile()));
    }

    /**
     * @param url
     * @throws IOException
     */
    public Uncaptcha(URL url) throws IOException {
        this(ImageIO.read(url));
    }

    /**
     * @param image
     */
    public Uncaptcha(BufferedImage image) {
        this.image = image;
    }

    /**
     * @return
     */
    public Uncaptcha binarize(int threshold) {
        process((column, row) -> {
            Color color = color(column, row);

            if (!color.isBlack(threshold)) {
                color.set(White);
            }
        });

        return this;
    }

    /**
     * <p>
     * Helper to edit image.
     * </p>
     * 
     * @param process
     */
    private void process(ImageProcessor process) {
        for (int row = 0; ++row < image.getHeight();) {
            for (int column = 0; ++column < image.getWidth();) {
                process.draw(column, row);
            }
        }
    }

    /**
     * <p>
     * Compute color by position.
     * </p>
     * 
     * @param column
     * @param row
     * @return
     */
    private Color color(int column, int row) {
        return new Color(column, row, image.getRGB(column, row));
    }

    /**
     * <p>
     * Try to read characters.
     * </p>
     * 
     * @return
     * @throws IOException
     */
    public String read() throws IOException {
        // // create image data as encoded string
        // ByteArrayOutputStream out = new ByteArrayOutputStream();
        // ImageIO.write(image, "png", out);
        // ByteString encoded = ByteString.readFrom(new ByteArrayInputStream(out.toByteArray()));
        //
        // // create request
        // AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
        // .addFeatures(Feature.newBuilder().setType(Type.TEXT_DETECTION).build())
        // .setImage(Image.newBuilder().setContent(encoded).build())
        // .build();
        //
        // // process response
        // return
        // I.signal(ImageAnnotatorClient.create().batchAnnotateImages(Arrays.asList(request)))
        // .flatIterable(BatchAnnotateImagesResponse::getResponsesList)
        // .map(response -> response.getTextAnnotations(0))
        // .map(EntityAnnotation::getDescription)
        // .to()
        // .get();

        // If this exception will be thrown, it is bug of this program. So we must rethrow the
        // wrapped error in here.
        throw new Error();
    }

    /**
     * @param path
     * @return
     */
    public Uncaptcha write(String path) {
        Path out = Paths.get(path);
        String name = out.getFileName().toString();
        String extension = name.substring(name.lastIndexOf(".") + 1);

        try {
            ImageIO.write(image, extension, out.toFile());
        } catch (IOException e) {
            throw I.quiet(e);
        }
        return this;
    }

    /**
     * @version 2017/02/27 10:57:36
     */
    private static interface ImageProcessor {

        void draw(int column, int row);
    }

    /**
     * @version 2017/02/27 11:00:44
     */
    private class Color {

        private final int x;

        private final int y;

        private final int color;

        @SuppressWarnings("unused")
        private final int alpha;

        private final int red;

        private final int green;

        private final int blue;

        /**
         * @param color
         */
        private Color(int x, int y, int color) {
            this.x = x;
            this.y = y;
            this.color = color;
            this.alpha = (color >> 24) & 0xFF;
            this.red = (color >> 16) & 0xFF;
            this.green = (color >> 8) & 0xFF;
            this.blue = (color >> 0) & 0xFF;
        }

        /**
         * @param color
         */
        public void set(Color color) {
            image.setRGB(x, y, color.color);
        }

        /**
         * @param range
         * @return
         */
        public boolean isBlack(int range) {
            int baser = 10; // base red
            int baseg = 10; // base green
            int baseb = 10; // base blue

            if (red < baser - range || baser + range < red) {
                return false;
            }

            if (green < baseg - range || baseg + range < green) {
                return false;
            }

            if (blue < baseb - range || baseb + range < blue) {
                return false;
            }

            return true;
        }
    }
}