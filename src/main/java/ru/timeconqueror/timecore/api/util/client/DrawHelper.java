package ru.timeconqueror.timecore.api.util.client;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;

public class DrawHelper {
    /**
     * Draws textured rectangle.
     * <p>
     * Term, used in parameters:
     * Parts are used to determine the actual size of {@code textureX, textureY, textureWidth, textureHeight} and coordinates relative to the entire texture.
     * The example:
     * Total texture has 64x64 resolution.
     * Let's consider axis X:
     * If we provide 4 as {@code texturePartCount} it wil be splitted into 4 pieces with width 16.
     * Then if we set {@code textureX} to 1 and {@code textureWidth} to 3, it will mean, that we need a texture with start at (1 * 16) by X and with end at (1 + 3) * 16 by X.
     *
     * @param x0               start x-coordinate. (x of left-top corner)
     * @param y0               start y-coordinate. (y of left-top corner)
     * @param width            Represents coordinate length along the axis X.
     * @param height           Represents coordinate length along the axis Y.
     * @param zLevel           z-coordinate.
     * @param textureX         index of start subtexture part on axis X (x of left-top texture corner).
     * @param textureY         index of start subtexture part on axis Y (y of left-top texture corner).
     * @param textureWidth     subtexture width in parts.
     * @param textureHeight    subtexture height in parts.
     * @param texturePartCount in how many parts texture must be divided in both axis. Part description is mentioned above.
     */
    public static void drawTexturedRectByParts(float x0, float y0, float width, float height, float zLevel, float textureX, float textureY, float textureWidth, float textureHeight, float texturePartCount) {
        float portionFactor = 1 / texturePartCount;
        drawTexturedRect(x0, y0, width, height, zLevel, textureX, textureY, textureWidth, textureHeight, portionFactor);
    }

    /**
     * Draws textured rectangle.
     * <p>
     * Term, used in parameters:
     * Parts are used to determine the actual size of {@code textureX, textureY, textureWidth, textureHeight} and coordinates relative to the entire texture.
     * The example:
     * Total texture has 64x64 resolution.
     * Let's consider axis X:
     * If we provide 4 as {@code texturePartCount} it wil be splitted into 4 pieces with width 16.
     * Then if we set {@code textureX} to 1 and {@code textureWidth} to 3, it will mean, that we need a texture with start at (1 * 16) by X and with end at (1 + 3) * 16 by X.
     *
     * @param x0                  start x-coordinate. (x of left-top corner)
     * @param y0                  start y-coordinate. (y of left-top corner)
     * @param width               Represents coordinate length along the axis X.
     * @param height              Represents coordinate length along the axis Y.
     * @param zLevel              z-coordinate.
     * @param textureX            index of start subtexture part on axis X (x of left-top texture corner).
     * @param textureY            index of start subtexture part on axis Y (y of left-top texture corner).
     * @param textureWidth        subtexture width in parts.
     * @param textureHeight       subtexture height in parts.
     * @param textureDivideFactor represents the value equal to 1 / parts. Part count determines in how many parts texture must be divided in both axis. Part description is mentioned above.
     */
    private static void drawTexturedRect(float x0, float y0, float width, float height, float zLevel, float textureX, float textureY, float textureWidth, float textureHeight, float textureDivideFactor) {
        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();
        tess.addVertexWithUV(x0, y0, zLevel, textureX * textureDivideFactor, textureY * textureDivideFactor);
        tess.addVertexWithUV(x0, y0 + height, zLevel, textureX * textureDivideFactor, (textureY + textureHeight) * textureDivideFactor);
        tess.addVertexWithUV(x0 + width, y0 + height, zLevel, (textureX + textureWidth) * textureDivideFactor, (textureY + textureHeight) * textureDivideFactor);
        tess.addVertexWithUV(x0 + width, y0, zLevel, (textureX + textureWidth) * textureDivideFactor, textureY * textureDivideFactor);
        tess.draw();
    }

    /**
     * Draws textured rectangle.
     * <p>
     * Term, used in parameters:
     * Parts are used to determine the actual size of {@code textureX, textureY, textureWidth, textureHeight} and coordinates relative to the entire texture.
     * The example:
     * Total texture has 64x64 resolution.
     * Let's consider axis X:
     * If we provide 4 as {@code texturePartCount} it wil be splitted into 4 pieces with width 16.
     * Then if we set {@code textureX} to 1 and {@code textureWidth} to 3, it will mean, that we need a texture with start at (1 * 16) by X and with end at (1 + 3) * 16 by X.
     *
     * @param x0               start x-coordinate. (x of left-top corner)
     * @param y0               start y-coordinate. (y of left-top corner)
     * @param width            Represents coordinate length along the axis X.
     * @param height           Represents coordinate length along the axis Y.
     * @param zLevel           z-coordinate.
     * @param textureX         index of start subtexture part on axis X (x of left-top texture corner).
     * @param textureY         index of start subtexture part on axis Y (y of left-top texture corner).
     * @param textureWidth     subtexture width in parts.
     * @param textureHeight    subtexture height in parts.
     * @param texturePartCount in how many parts texture must be divided in both axis. Part description is mentioned above.
     * @param argbColor        color which will be applied to the texture
     */
    public static void drawTexturedRectByParts(float x0, float y0, float width, float height, float zLevel, float textureX, float textureY, float textureWidth, float textureHeight, float texturePartCount, int argbColor) {
        float portionFactor = 1 / texturePartCount;
        drawTexturedRect(x0, y0, width, height, zLevel, textureX, textureY, textureWidth, textureHeight, portionFactor, argbColor);
    }

    /**
     * Draws textured rectangle.
     * <p>
     * Term, used in parameters:
     * Parts are used to determine the actual size of {@code textureX, textureY, textureWidth, textureHeight} and coordinates relative to the entire texture.
     * The example:
     * Total texture has 64x64 resolution.
     * Let's consider axis X:
     * If we provide 4 as {@code texturePartCount} it wil be splitted into 4 pieces with width 16.
     * Then if we set {@code textureX} to 1 and {@code textureWidth} to 3, it will mean, that we need a texture with start at (1 * 16) by X and with end at (1 + 3) * 16 by X.
     *
     * @param x0                  start x-coordinate. (x of left-top corner)
     * @param y0                  start y-coordinate. (y of left-top corner)
     * @param width               Represents coordinate length along the axis X.
     * @param height              Represents coordinate length along the axis Y.
     * @param zLevel              z-coordinate.
     * @param textureX            index of start subtexture part on axis X (x of left-top texture corner).
     * @param textureY            index of start subtexture part on axis Y (y of left-top texture corner).
     * @param textureWidth        subtexture width in parts.
     * @param textureHeight       subtexture height in parts.
     * @param textureDivideFactor represents the value equal to 1 / parts. Part count determines in how many parts texture must be divided in both axis. Part description is mentioned above.
     * @param argbColor           color which will be applied to the texture
     */
    private static void drawTexturedRect(float x0, float y0, float width, float height, float zLevel, float textureX, float textureY, float textureWidth, float textureHeight, float textureDivideFactor, int argbColor) {
        int r = getRed(argbColor);
        int g = getGreen(argbColor);
        int b = getBlue(argbColor);
        int a = getAlpha(argbColor);

        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();

        tess.setColorRGBA(r, g, b, a);

        tess.addVertexWithUV(x0, y0, zLevel, textureX * textureDivideFactor, textureY * textureDivideFactor);
        tess.addVertexWithUV(x0, y0 + height, zLevel, textureX * textureDivideFactor, (textureY + textureHeight) * textureDivideFactor);
        tess.addVertexWithUV(x0 + width, y0 + height, zLevel, (textureX + textureWidth) * textureDivideFactor, (textureY + textureHeight) * textureDivideFactor);
        tess.addVertexWithUV(x0 + width, y0, zLevel, (textureX + textureWidth) * textureDivideFactor, textureY * textureDivideFactor);

        tess.draw();
    }

    /**
     * Draws textured rectangle with autoexpandable width.
     * How it works: this method renders left and right part of rectangle, depending on given {@code requiredWidth}, and then repeats center element until it fill all remaining width.
     * <p>
     * If {@code requiredWidth} is less than the sum of {@code startElement, endElement} width, it will be expanded to this sum.
     * <p>
     * Term, used in parameters:
     * Parts are used to determine the actual size of {@code textureX, textureY, textureWidth, textureHeight} and coordinates relative to the entire texture.
     * The example:
     * Total texture has 64x64 resolution.
     * Let's consider axis X:
     * If we provide 4 as {@code texturePartCount} it wil be splitted into 4 pieces with width 16.
     * Then if we set {@code textureX} to 1 and {@code textureWidth} to 3, it will mean, that we need a texture with start at (1 * 16) by X and with end at (1 + 3) * 16 by X.
     *
     * @param x0               start x-coordinate. (x of left-top corner)
     * @param y0               start y-coordinate. (y of left-top corner)
     * @param requiredWidth    what coordinate width must rectangle have.
     * @param zLevel           z-coordinate.
     * @param startElement     element, that represents left rectangle part.
     * @param repeatElement    element, that represents repeat rectangle part.
     * @param endElement       element, that represents right rectangle part.
     * @param texturePartCount in how many parts texture must be divided in both axis. Part description is mentioned above.
     *///TODO improve draw calls
    public static void drawWidthExpandableTexturedRect(float x0, float y0, float requiredWidth, float zLevel, TexturedRect startElement, TexturedRect repeatElement, TexturedRect endElement, float texturePartCount) {
        float startWidth = startElement.width;
        float endWidth = endElement.width;
        float minWidth = startWidth + endWidth;

        if (requiredWidth <= minWidth) {
            DrawHelper.drawTexturedRectByParts(x0, y0, startWidth, startElement.height, zLevel, startElement.textureX, startElement.textureY, startElement.textureWidth, startElement.textureHeight, texturePartCount);
            DrawHelper.drawTexturedRectByParts(x0 + startWidth, y0, endWidth, endElement.height, zLevel, endElement.textureX, endElement.textureY, endElement.textureWidth, endElement.textureHeight, texturePartCount);
        } else {
            float remainingWidth = requiredWidth - minWidth;
            float repeatWidth = repeatElement.width;
            float repeatTimes = remainingWidth / repeatWidth;

            int fullTimes = (int) repeatTimes;
            float fracPart = repeatTimes - (int) repeatTimes;

            DrawHelper.drawTexturedRectByParts(x0, y0, startWidth, startElement.height, zLevel, startElement.textureX, startElement.textureY, startElement.textureWidth, startElement.textureHeight, texturePartCount);

            float extraX = startWidth;
            for (int i = 0; i < fullTimes; i++) {
                DrawHelper.drawTexturedRectByParts(x0 + extraX, y0, repeatElement.width, repeatElement.height, zLevel, repeatElement.textureX, repeatElement.textureY, repeatElement.textureWidth, repeatElement.textureHeight, texturePartCount);
                extraX += repeatElement.width;
            }

            DrawHelper.drawTexturedRectByParts(x0 + extraX, y0, repeatWidth * fracPart, repeatElement.height, zLevel, repeatElement.textureX, repeatElement.textureY, repeatElement.textureWidth * fracPart, repeatElement.textureHeight, texturePartCount);
            extraX += repeatWidth * fracPart;

            DrawHelper.drawTexturedRectByParts(x0 + extraX, y0, endWidth, endElement.height, zLevel, endElement.textureX, endElement.textureY, endElement.textureWidth, endElement.textureHeight, texturePartCount);
        }
    }

    /**
     * Returns color with changed alpha
     *
     * @param alpha should be in range from 0 to 255.
     */
    public static int changeAlpha(int argb, int alpha) {
        if (alpha < 0 || alpha > 255)
            throw new IllegalArgumentException("Alpha should be positive and less than 255. Provided: " + alpha);
        argb &= 0x00FFFFFF;
        return argb | alpha << 24;
    }

    /**
     * Returns red channel data of the ARGB color.
     */
    public static int getRed(int argb) {
        return argb >> 16 & 0xFF;
    }

    /**
     * Returns green channel data of the ARGB color.
     */
    public static int getGreen(int argb) {
        return argb >> 8 & 0xFF;
    }

    /**
     * Returns blue channel data of the ARGB color.
     */
    public static int getBlue(int argb) {
        return argb & 0xFF;
    }

    /**
     * Returns alpha channel data of the ARGB color.
     */
    public static int getAlpha(int argb) {
        return argb >> 24 & 0xFF;
    }

    /**
     * Draws y-centered string with shadow.
     *
     * @param text  text to be displayed.
     * @param x     start x-coordinate (left)
     * @param y     center y-coordinate
     * @param color HTML color. Example: 0xFF0000 -> red.
     */
    public static void drawYCenteredStringWithShadow(FontRenderer fontRendererIn, String text, int x, int y, int color) {
        drawStringWithShadow(fontRendererIn, text, x, y - fontRendererIn.FONT_HEIGHT / 2, color);
    }

    /**
     * Draws string with shadow.
     *
     * @param text  text to be displayed.
     * @param x     start x-coordinate (left)
     * @param y     start y-coordinate (top)
     * @param color HTML color. Example: 0xFF0000 -> red.
     */
    public static void drawStringWithShadow(FontRenderer fontRendererIn, String text, int x, int y, int color) {
        fontRendererIn.drawStringWithShadow(text, x, y, color);
    }

    public static class TexturedRect {
        /**
         * Represents coordinate length along the axis X.
         */
        private final float width;
        /**
         * Represents coordinate length along the axis Y.
         */
        private final float height;
        /**
         * Start texture x-point (x of left-top texture corner).
         */
        private final float textureX;
        /**
         * Start texture y-point (y of left-top texture corner).
         */
        private final float textureY;
        /**
         * Texture width in points.
         * Point is a relative texture coordinate. It is used in {@code textureX, textureY, textureWidth, textureHeight} to determine its sizes and coordinates relative to the entire texture.
         */
        private final float textureWidth;
        /**
         * Texture height in points.
         * Point is a relative texture coordinate. It is used in {@code textureX, textureY, textureWidth, textureHeight} to determine its sizes and coordinates relative to the entire texture.
         */
        private final float textureHeight;

        public TexturedRect(float width, float height, float textureX, float textureY, float textureWidth, float textureHeight) {
            this.width = width;
            this.height = height;
            this.textureX = textureX;
            this.textureY = textureY;
            this.textureWidth = textureWidth;
            this.textureHeight = textureHeight;
        }
    }
}
