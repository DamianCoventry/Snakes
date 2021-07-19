package com.snakegame.client;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

// https://www.glfw.org/docs/latest/window_guide.html
// https://github.com/glfw/glfw
// https://en.wikipedia.org/wiki/GLFW
public class OpenGLWindow {
    private static final double s_FovYDegrees = 60.0;
    private static final double s_NearClipPlane = 1.0;
    private static final double s_FarClipPlane = 1000.0;

    private final long m_Window;
    private float m_ActualWidth;
    private float m_ActualHeight;

    public OpenGLWindow(int desiredWindowWidth, int desiredWindowHeight, String windowTitle) {
        glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err));

        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        m_Window = glfwCreateWindow(desiredWindowWidth, desiredWindowHeight, windowTitle, NULL, NULL);
        if (m_Window == NULL) {
            glfwTerminate();
            throw new RuntimeException("Failed to create the GLFW window");
        }

        GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        if (vidMode == null) {
            glfwTerminate();
            throw new RuntimeException("Failed to create the GLFW window");
        }

        glfwSetWindowPos(m_Window,
                (vidMode.width() - desiredWindowWidth) / 2,
                (vidMode.height() - desiredWindowHeight) / 2);
        glfwMakeContextCurrent(m_Window);

        GL.createCapabilities();

        glfwSwapInterval(1); // Sync to monitor's refresh rate

        glfwSetWindowSizeCallback(m_Window, new GLFWWindowSizeCallback() {
            @Override
            public void invoke(long window, int width, int height) {
                if (glfwGetCurrentContext() > 0) {
                    glViewport(0, 0, width, height);
                }
                m_ActualWidth = (float)Math.max(width, 1);
                m_ActualHeight = (float)Math.max(height, 1);
            }
        });

        glViewport(0, 0, desiredWindowWidth, desiredWindowHeight);
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        m_ActualWidth = (float)desiredWindowWidth;
        m_ActualHeight = (float)desiredWindowHeight;
    }

    public float getActualWidth() {
        return m_ActualWidth;
    }
    public float getActualHeight() {
        return m_ActualHeight;
    }
    
    public void freeNativeResources() {
        glfwDestroyWindow(m_Window);
        glfwTerminate();
    }

    public boolean quitRequested() {
        return glfwWindowShouldClose(m_Window);
    }

    public void setKeyCallback(GLFWKeyCallbackI callback) {
        glfwSetKeyCallback(m_Window, callback);
    }

    public void beginDrawing() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void endDrawing() {
        glfwSwapBuffers(m_Window);
        glfwPollEvents();
    }

    public void set3d(float cameraZ) {
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        setPerspectiveProjection(m_ActualWidth / m_ActualHeight);

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glTranslated(0.0, 0.0, cameraZ); // Place the camera
    }

    public void set2d() {
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0.0, m_ActualWidth, 0.0, m_ActualHeight, -1.0f, 1.0f);

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        // No camera in 2d mode
    }

    // https://www.khronos.org/opengl/wiki/GluPerspective_code
    private static void setPerspectiveProjection(double aspect) {
        double tangent = Math.tan(Math.toRadians(s_FovYDegrees / 2.0));
        double height = s_NearClipPlane * tangent;
        double width = height * aspect;
        glFrustum(-width, width, -height, height, s_NearClipPlane, s_FarClipPlane);
    }
}
