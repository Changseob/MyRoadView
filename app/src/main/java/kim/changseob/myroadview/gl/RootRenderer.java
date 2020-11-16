package kim.changseob.myroadview.gl;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class RootRenderer implements GLSurfaceView.Renderer{
    private static final String TAG = "GL_renderer";

    YUV2RGBAImageRenderer mCameraPreviewRenderer;

    int mWindowWidth = 800;
    int mWindowHeight = 600;

    public void DrawCameraPreview() {

    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        mCameraPreviewRenderer = new YUV2RGBAImageRenderer();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mWindowWidth = width;
        mWindowHeight = height;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        DrawCameraPreview();

        //TODO: Draw sphere view
    }

    public void SetNextCameraFrame(byte[] data) {

    }
}
