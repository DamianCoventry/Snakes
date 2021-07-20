package com.snakegame.client;

import com.snakegame.opengl.GLTexture;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class NumberFont {
    public static final float s_FrameWidth = 26.0f;
    public static final float s_FrameHeight = 37.0f;
    private static final int s_NumDigits = 10;
    private final GLTexture m_NumberGLTexture;
    private final Character[] m_Characters;
    
    private static class Character {
        float m_U0, m_V0;
        float m_U1, m_V1;
        public Character(float u0, float v0, float u1, float v1) {
            m_U0 = u0;  m_V0 = v0;
            m_U1 = u1;  m_V1 = v1;
        }
    }
    
    public NumberFont() throws IOException {
        m_NumberGLTexture = new GLTexture(ImageIO.read(new File("images\\Numbers.png")));
        m_Characters = new Character[s_NumDigits];
        final float deltaU = s_FrameWidth / m_NumberGLTexture.getWidth();
        final float deltaV = s_FrameHeight / m_NumberGLTexture.getHeight();
        for (int i = 0; i < s_NumDigits; ++i) {
            m_Characters[i] = new Character(
                    i * deltaU,0.0f,
                    (i * deltaU) + deltaU, deltaV
            );
        }
    }

    public void freeNativeResource() {
        m_NumberGLTexture.freeNativeResource();
    }
    
    public void drawNumber(long number, float x, float y) {
//        glColor4d(1.0, 1.0, 1.0, 1.0);
//        glEnable(GL_TEXTURE_2D);
//        glBindTexture(GL_TEXTURE_2D, m_NumberGLTexture.getId());
//        String text = String.valueOf(number);
//        glBegin(GL_QUADS);
//        for (int i = 0; i < text.length(); ++i) {
//            int j = text.charAt(i) - '0'; // Convert the character to an index into the m_Characters array
//            glTexCoord2d(m_Characters[j].m_U0, m_Characters[j].m_V0); glVertex2d(x, y + s_FrameHeight);
//            glTexCoord2d(m_Characters[j].m_U0, m_Characters[j].m_V1); glVertex2d(x , y);
//            glTexCoord2d(m_Characters[j].m_U1, m_Characters[j].m_V1); glVertex2d(x + s_FrameWidth, y);
//            glTexCoord2d(m_Characters[j].m_U1, m_Characters[j].m_V0); glVertex2d(x + s_FrameWidth, y + s_FrameHeight);
//            x += s_FrameWidth;
//        }
//        glEnd();
    }

    public float calculateWidth(long number) {
        String text = String.valueOf(number);
        return text.length() * s_FrameWidth;
    }
}
